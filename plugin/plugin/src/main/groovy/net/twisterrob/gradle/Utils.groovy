package net.twisterrob.gradle

import com.android.build.gradle.*
import com.android.build.gradle.api.*
import org.gradle.api.*

class Utils {
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
			return Collections.emptyList()
		}
	}
	static boolean hasAndroid(Project project) {
		return project.plugins.hasPlugin(AppPlugin) ||
				project.plugins.hasPlugin(LibraryPlugin) ||
				project.plugins.hasPlugin(FeaturePlugin) ||
				project.plugins.hasPlugin(TestPlugin)
	}
}
