package net.twisterrob.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.FeatureExtension
import com.android.build.gradle.FeaturePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestExtension
import com.android.build.gradle.TestPlugin
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer

class Utils {

	/**
	 * Call {@code extensions} property on the object dynamically.
	 */
	static ExtensionContainer getExtensions(Object obj) {
		return obj.extensions
	}

	static String toString(ApplicationVariant variant) {
		return "${variant.class}, name=${variant.name}, desc=${variant.description}, base=${variant.baseName}, dir=${variant.dirName}, pkg=${variant.applicationId}, flav=${variant.flavorName}, ver=${variant.versionName}, code=${variant.versionCode}"
	}

	static DomainObjectSet<? extends BaseVariant> getVariants(BaseExtension android) {
		if (android instanceof AppExtension) {
			return android.applicationVariants
		} else if (android instanceof FeatureExtension) {
			return android.featureVariants
		} else if (android instanceof LibraryExtension) {
			return android.libraryVariants
		} else if (android instanceof TestExtension) {
			return android.applicationVariants
		} else if (android instanceof TestedExtension) {
			return android.testVariants
		} else {
			throw new IllegalArgumentException("Unknown extension: $android")
		}
	}

	static boolean hasAndroid(Project project) {
		return project.plugins.hasPlugin(AppPlugin) ||
				project.plugins.hasPlugin(LibraryPlugin) ||
				project.plugins.hasPlugin(FeaturePlugin) ||
				project.plugins.hasPlugin(TestPlugin)
	}
}
