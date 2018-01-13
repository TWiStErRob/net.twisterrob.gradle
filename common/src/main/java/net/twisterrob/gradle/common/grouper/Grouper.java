package net.twisterrob.gradle.common.grouper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.collect;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.groupBy;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.plus;

import groovy.lang.Closure;

/**
 * Usage from Java: {@code Grouper.create(asList(a,b,c)).getGrouper().by("f1").by("f2").group()}
 * <br/>
 * Usage from Groovy: {@code Grouper.create([a,b,c]).grouper['f1']['f2'].group()}
 */
public class Grouper<K, V> {

	public static <T> Start<T> create(List<T> obj) {
		return new Start<>(obj);
	}

	private final List<?> obj;
	private final List<String> fields;

	private Grouper(List<?> obj, List<String> fields) {
		this.obj = obj;
		this.fields = fields;
	}

	public Grouper<K, Map<?, V>> by(String fieldName) {
		return new Grouper<>(obj, plus(fields, fieldName));
	}

	/**
	 * for Groovy to have [] operator
	 */
	@SuppressWarnings("unused")
	public Grouper<K, Map<?, V>> getAt(String fieldName) {
		return by(fieldName);
	}

	@SuppressWarnings({"unchecked", "rawtypes", "serial"})
	public Map<K, V> group() {
		// return obj.groupBy(fields.collect { field -> { obj -> obj[field] } })
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
		return (Map<K, V>)groupBy((Iterable<V>)obj, (List<Closure>)(List<?>)closures);
	}

	public static class Start<T> {

		private final List<T> obj;

		private Start(List<T> obj) {
			this.obj = obj;
		}

		public List<T> getList() {
			return obj;
		}

		public Grouper<?, List<T>> by(String fieldName) {
			return new Grouper<>(obj, Collections.singletonList(fieldName));
		}

		@SuppressWarnings("unused")
		public Grouper<?, List<T>> getAt(String fieldName) {
			return by(fieldName);
		}
	}
}
