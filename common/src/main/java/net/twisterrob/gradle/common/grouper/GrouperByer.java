package net.twisterrob.gradle.common.grouper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.collect;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.groupBy;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.plus;

import groovy.lang.Closure;

public interface GrouperByer<K, V> {

	GrouperByer<K, Map<?, V>> getAt(String fieldName);

	Map<K, V> build();

	static <T> Chain<T> group(List<T> obj) {
		return new GrouperByerImpls.Chain<T>(obj);
	}

	interface Chain<T> {

		GrouperByer<?, List<T>> getAt(String fieldName);

		List<T> build();
	}
}

@SuppressWarnings("serial") class GrouperByerImpls {

	static class Chain<T> implements GrouperByer.Chain<T> {

		private final List<T> obj;

		Chain(List<T> obj) {
			this.obj = obj;
		}

		@Override
		public GrouperByer<?, List<T>> getAt(String fieldName) {
			return new Grouped<>(obj, Collections.singletonList(fieldName));
		}

		public List<T> build() {
			return obj;
		}
	}

	static class Grouped<K, V> implements GrouperByer<K, V> {

		private final List<?> obj;
		private final List<String> fields;

		Grouped(List<?> obj, List<String> fields) {
			this.obj = obj;
			this.fields = fields;
		}

		@Override
		public GrouperByer<K, Map<?, V>> getAt(String fieldName) {
			return new Grouped<>(obj, plus(fields, fieldName));
		}

		@Override
		@SuppressWarnings({"unchecked", "rawtypes"})
		public Map<K, V> build() {
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
	}
}
