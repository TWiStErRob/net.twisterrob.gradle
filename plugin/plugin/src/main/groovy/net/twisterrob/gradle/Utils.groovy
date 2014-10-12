package net.twisterrob.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet

class Utils {
	public static String toString(ApplicationVariant variant) {
		return "${variant.class}, name=${variant.name}, desc=${variant.description}, base=${variant.baseName}, dir=${variant.dirName}, pkg=${variant.applicationId}, flav=${variant.flavorName}, ver=${variant.versionName}, code=${variant.versionCode}"
	}

	public static DomainObjectSet<? extends BaseVariant> getVariants(BaseExtension android) {
		if (android instanceof AppExtension) {
			return android.applicationVariants
		} else if (android instanceof LibraryExtension) {
			return android.libraryVariants
		} else {
			return Collections.emptyList()
		}
	}
}