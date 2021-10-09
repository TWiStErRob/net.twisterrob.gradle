group = "net.twisterrob.gradle"

subprojects {
	repositories {
		mavenCentral()
		google()
		maven { name = "Gradle libs (for Kotlin-DSL)"; setUrl("https://repo.gradle.org/gradle/libs-releases-local/") }
	}

	group = rootProject.group
	apply(from = resources.text.fromInsecureUri("http://localhost/maven/configure.gradle"))
}

allprojects {
	tasks.withType<JavaCompile> {
		options.compilerArgs.addAll(
			listOf(
				// check everything
				"-Xlint:all",
				// fail on any warning
				"-Werror",
				//warning: [options] bootstrap class path not set in conjunction with -source 1.7
				"-Xlint:-options",
				//The following annotation processors were detected on the compile classpath:
				// 'javaslang.match.PatternsProcessor'
				// 'com.google.auto.value.extension.memoized.MemoizedValidator'
				// 'com.google.auto.value.processor.AutoAnnotationProcessor'
				// 'com.google.auto.value.processor.AutoValueBuilderProcessor'
				// 'com.google.auto.value.processor.AutoValueProcessor'.
				// implies "-Xlint:-processing",
				"-proc:none"
			)
		)
	}
}
