plugins {
	id("net.twisterrob.gradle.build.module.browser")
}

// Current version: com.android.Version.ANDROID_TOOLS_BASE_VERSION

dependencies {
	sourcesOnly(libs.android.lint.main)
	sourcesOnly(libs.android.lint.api)
	sourcesOnly(libs.android.lint.gradle)
	sourcesOnly(libs.android.lint.checks)
	sourcesOnly(libs.androidx.lint)
}
