package net.twisterrob.gradle.common.grouper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.NullObject;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.collect;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.groupBy;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.plus;

import groovy.lang.Closure;
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
		return new Start<>(Collections.unmodifiableList(list), findRepresentative(list));
	}

	private final @Nonnull List<String> fields;

	private Grouper(@Nonnull List<?> list, @Nonnull Object representative, @Nonnull List<String> fields) {
		super(list, representative);
		this.fields = fields;
	}

	@Override
	public @Nonnull Grouper<K, Map<?, V>> by(@Nonnull String fieldName) {
		return new Grouper<>(list, representative, plus(fields, fieldName));
	}

	@Override
	@SuppressWarnings("unchecked")
	public @Nonnull Grouper<K, Map<?, V>> getAt(@Nonnull String fieldName) {
		return (Grouper<K, Map<?, V>>)super.getAt(fieldName);
	}

	@Override
	public Object getProperty(String fieldName) {
		return super.getProperty(fieldName);
	}

	@SuppressWarnings({"unchecked", "rawtypes", "serial"})
	public @Nonnull Map<K, V> group() {
		// return list.groupBy(fields.collect { field -> { obj -> obj[field] } })
		List<Closure<Object>> closures = collect(fields, new Closure<Closure<Object>>(this) {
			@Override public Closure<Object> call(Object field) {
				return new Closure<Object>(this) {
					@Override public Object call(Object obj) {
						return DefaultGroovyMethods.getAt(obj, (String)field);
					}
				};
			}
		});
		// help overload resolution, if type doesn't match exactly Object... variant is picked up
		return (Map<K, V>)groupBy((Iterable<V>)list, (List<Closure>)(List<?>)closures);
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

		private Start(@Nonnull List<T> list, @Nonnull Object representative) {
			super(list, representative);
		}

		@SuppressWarnings("unchecked")
		public @Nonnull List<T> getList() {
			return (List<T>)list;
		}

		@Override
		public @Nonnull Grouper<?, List<T>> by(@Nonnull String fieldName) {
			return new Grouper<>(list, representative, Collections.singletonList(fieldName));
		}

		@SuppressWarnings("unchecked")
		@Override
		public @Nonnull Grouper<?, List<T>> getAt(@Nonnull String fieldName) {
			return (Grouper<?, List<T>>)super.getAt(fieldName);
		}

		@Override
		public Object getProperty(String fieldName) {
			return super.getProperty(fieldName);
		}
	}
}

abstract class AbstractGrouper extends GroovyObjectSupport {

	protected final @Nonnull List<?> list;
	protected final @Nonnull Object representative;

	protected AbstractGrouper(@Nonnull List<?> list, @Nonnull Object representative) {
		this.list = list;
		this.representative = representative;
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
