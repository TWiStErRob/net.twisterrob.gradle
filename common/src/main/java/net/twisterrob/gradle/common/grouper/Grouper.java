package net.twisterrob.gradle.common.grouper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.NullObject;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.plus;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;

/**
 * A grouper for uniform lists. Non-uniform lists may misbehave depending on order due to
 * {@linkplain AbstractGrouper#representative implementation details}.
 * <p>
 * Usage from Java: {@code Grouper.create(asList(a,b,c)).getGrouper().by("f1").by("f2").group()}
 * <br/>
 * Usage from Groovy: {@code Grouper.create([a,b,c]).grouper['f1']['f2'].group()}
 * </p>
 */
public class Grouper<K, V> extends AbstractGrouper {

	public static @Nonnull <T> Start<T> create(@Nonnull List<T> list) {
		return create(list, Collectors.reducing(0, e -> 1, Integer::sum));
	}

	public static @Nonnull <T> Start<T> create(@Nonnull List<T> list, @Nonnull Collector<T, ?, ?> finalCollector) {
		return new Start<>(Collections.unmodifiableList(list), findRepresentative(list), finalCollector);
	}

	private final @Nonnull List<String> fields;

	private Grouper(
			@Nonnull List<?> list,
			@Nonnull Object representative,
			@Nonnull List<String> fields,
			@Nonnull Collector<?, ?, ?> finalCollector) {
		super(list, representative, finalCollector);
		this.fields = fields;
	}

	@Override
	public @Nonnull Grouper<K, Map<?, V>> by(@Nonnull String fieldName) {
		return new Grouper<>(list, representative, plus(fields, fieldName), finalCollector);
	}

	@Override
	@SuppressWarnings("unchecked")
	public @Nonnull Grouper<K, Map<?, V>> getAt(@Nonnull String fieldName) {
		return (Grouper<K, Map<?, V>>)super.getAt(fieldName);
	}

	@SuppressWarnings("EmptyMethod") // Groovy needs this
	@Override
	public Object getProperty(String fieldName) {
		return super.getProperty(fieldName);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public @Nonnull Map<K, V> group() {
		List<String> reversed = new ArrayList<>(fields);
		Collections.reverse(reversed);

		Collector collectors = finalCollector;
		for (String field : reversed) {
			Function access = obj -> DefaultGroovyMethods.getAt(obj, field);
			collectors = Collectors.groupingBy(access, LinkedHashMap::new, collectors);
		}
		return (Map)list.stream().collect(collectors);
	}

	@Override
	public String toString() {
		return String.format("%d %s grouped on %s", this.list.size(), representative.getClass(), fields);
	}

	private static Object findRepresentative(@Nonnull Collection<?> list) {
		Object found = DefaultGroovyMethods.find(list);
		if (found != null) {
			return found;
		} else {
			return NullObject.getNullObject();
		}
	}

	public static class Start<T> extends AbstractGrouper {

		private Start(
				@Nonnull List<T> list,
				@Nonnull Object representative,
				@Nonnull Collector<T, ?, ?> finalCollector) {
			super(list, representative, finalCollector);
		}

		@SuppressWarnings("unchecked")
		public @Nonnull List<T> getList() {
			return (List<T>)list;
		}

		@Override
		public @Nonnull Grouper<?, List<T>> by(@Nonnull String fieldName) {
			return new Grouper<>(list, representative, Collections.singletonList(fieldName), Collectors.toList());
		}

		@SuppressWarnings("unchecked")
		public @Nonnull <R> Counter<T, R> count() {
			return new Counter<>(getList(), representative, (Collector<T, ?, R>)finalCollector);
		}

		@SuppressWarnings("unchecked")
		@Override
		public @Nonnull Grouper<?, List<T>> getAt(@Nonnull String fieldName) {
			return (Grouper<?, List<T>>)super.getAt(fieldName);
		}

		@SuppressWarnings("EmptyMethod") // Groovy needs this
		@Override
		public Object getProperty(String fieldName) {
			return super.getProperty(fieldName);
		}
	}

	public static class Counter<T, R> extends AbstractGrouper {

		private Counter(
				@Nonnull List<T> list,
				@Nonnull Object representative,
				@Nonnull Collector<T, ?, R> finalCollector) {
			super(list, representative, finalCollector);
		}

		@Override
		public @Nonnull Grouper<?, R> by(@Nonnull String fieldName) {
			return new Grouper<>(list, representative, Collections.singletonList(fieldName), finalCollector);
		}

		@SuppressWarnings("unchecked")
		@Override
		public @Nonnull Grouper<?, R> getAt(@Nonnull String fieldName) {
			return (Grouper<?, R>)super.getAt(fieldName);
		}

		@SuppressWarnings("EmptyMethod") // Groovy needs this
		@Override
		public Object getProperty(String fieldName) {
			return super.getProperty(fieldName);
		}
	}
}

abstract class AbstractGrouper extends GroovyObjectSupport {

	protected final @Nonnull List<?> list;
	protected final @Nonnull Object representative;
	protected final @Nonnull Collector<?, ?, ?> finalCollector;

	protected AbstractGrouper(
			@Nonnull List<?> list,
			@Nonnull Object representative,
			@Nonnull Collector<?, ?, ?> finalCollector) {
		this.list = list;
		this.representative = representative;
		this.finalCollector = finalCollector;
	}

	/**
	 * for Groovy to have [] operator
	 */
	@SuppressWarnings("unused")
	public @Nonnull Grouper<?, ?> getAt(@Nonnull String fieldName) {
		return by(fieldName);
	}

	public @Nonnull abstract Grouper<?, ?> by(@Nonnull String fieldName);

	/**
	 * for Groovy to have . operator, needs to be repeated in every type.
	 */
	@Override
	public Object getProperty(String fieldName) {
		if ("by".equals(fieldName)) {
			return this;
		}
		if (hasField(representative, fieldName)) {
			return by(fieldName);
		} else {
			return super.getProperty(fieldName);
		}
	}

	private static boolean hasField(Object obj, String fieldName) {
		try {
			DefaultGroovyMethods.getAt(obj, fieldName);
			return true;
		} catch (MissingPropertyException ex) {
			// property not found
			return false;
		}
	}
}
