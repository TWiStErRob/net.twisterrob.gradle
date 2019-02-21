plugins {
	`java-library`
}

val VERSION_JUNIT: String by project
val VERSION_JUNIT_JUPITER: String by project
val VERSION_HAMCREST: String by project
val VERSION_JFIXTURE: String by project

val VERSION_MOCKITO: String by project
val VERSION_MOCKITO_KOTLIN: String by project
val VERSION_MOCKK: String by project

val VERSION_JETBRAINS_ANNOTATIONS: String by project
val VERSION_LINT: String by project

dependencies {
	api(gradleApi())
	api(gradleTestKit())

	api("org.jetbrains:annotations:${VERSION_JETBRAINS_ANNOTATIONS}")

	api("junit:junit:${VERSION_JUNIT}")
	api("org.junit.jupiter:junit-jupiter-api:$VERSION_JUNIT_JUPITER")
	api("org.junit.jupiter:junit-jupiter-migrationsupport:$VERSION_JUNIT_JUPITER")
	api("org.junit.jupiter:junit-jupiter-params:$VERSION_JUNIT_JUPITER")
	runtimeOnly("org.junit.jupiter:junit-jupiter-engine:$VERSION_JUNIT_JUPITER")
	runtimeOnly("org.junit.vintage:junit-vintage-engine:$VERSION_JUNIT_JUPITER")

	api("org.hamcrest:java-hamcrest:${VERSION_HAMCREST}")

	api("org.mockito:mockito-core:${VERSION_MOCKITO}")
	api("com.nhaarman.mockitokotlin2:mockito-kotlin:${VERSION_MOCKITO_KOTLIN}")

	api("io.mockk:mockk:${VERSION_MOCKK}")

	api("com.flextrade.jfixture:jfixture:${VERSION_JFIXTURE}")

	// TODO use buildSrc sourceOnly configuration
	// only here so IDEA can browse the source files of this dependency when getting a stack trace or finding usages
	testRuntimeOnly("com.android.tools.lint:lint:${VERSION_LINT}") { isTransitive = false }
	testRuntimeOnly("com.android.tools.lint:lint-api:${VERSION_LINT}") { isTransitive = false }
	testRuntimeOnly("com.android.tools.lint:lint-gradle:${VERSION_LINT}") { isTransitive = false }
	testRuntimeOnly("com.android.tools.lint:lint-gradle-api:${VERSION_LINT}") { isTransitive = false }
	testRuntimeOnly("com.android.tools.lint:lint-checks:${VERSION_LINT}") { isTransitive = false }
	testRuntimeOnly("com.android.tools.lint:lint-kotlin:${VERSION_LINT}") { isTransitive = false }
}
