plugins {
	`java-library`
}

val VERSION_JFIXTURE: String by project

val VERSION_MOCKK: String by project

val VERSION_LINT: String by project

dependencies {
	api(gradleApi())
	api(gradleTestKit())

	api(Libs.Annotations.jetbrains)

	api(Libs.JUnit4.library) // needed for GradleRunnerRule superclass even when using Extension
	api(Libs.JUnit5.api)
	api(Libs.JUnit5.params)
	runtimeOnly(Libs.JUnit5.engine)

	api(Libs.Hamcrest.new)

	api(Libs.Mockito.core)
	api(Libs.Mockito.junit5)
	api(Libs.Mockito.kotlin)

	api("io.mockk:mockk:${VERSION_MOCKK}")

	api("com.flextrade.jfixture:jfixture:${VERSION_JFIXTURE}")

	// TODO use buildSrc sourceOnly configuration
	// only here so IDEA can browse the source files of this dependency when getting a stack trace or finding usages
	testRuntimeOnly("com.android.tools.lint:lint:${VERSION_LINT}") { isTransitive = false }
	testRuntimeOnly("com.android.tools.lint:lint-api:${VERSION_LINT}") { isTransitive = false }
	testRuntimeOnly("com.android.tools.lint:lint-gradle:${VERSION_LINT}") { isTransitive = false }
	testRuntimeOnly("com.android.tools.lint:lint-gradle-api:${VERSION_LINT}") { isTransitive = false }
	testRuntimeOnly("com.android.tools.lint:lint-checks:${VERSION_LINT}") { isTransitive = false }
}
