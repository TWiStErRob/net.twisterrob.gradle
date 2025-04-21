package net.twisterrob.gradle.build.publishing

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.attributes.DocsType
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.internal.JavaConfigurationVariantMapping
import org.gradle.api.plugins.internal.JvmPluginsHelper
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByName

/**
 * @param project [Project] this extension belongs to
 * @param artifactTask that produces the Documentation output files
 *
 * @see org.gradle.api.plugins.internal.DefaultJavaPluginExtension.withJavadocJar
 * @see org.gradle.jvm.component.internal.DefaultJvmSoftwareComponent.withJavadocJar
 * @see org.gradle.api.plugins.jvm.internal.DefaultJvmFeature.withJavadocJar
 */
fun JavaPluginExtension.withDokkaJar(project: Project, artifactTask: TaskProvider<out Task>) {
	val javadoc = this.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
	val kdocVariant = JvmPluginsHelper.createDocumentationVariantWithArtifact(
		javadoc.javadocElementsConfigurationName,
		null,
		DocsType.JAVADOC,
		emptySet(),
		javadoc.javadocJarTaskName,
		artifactTask,
		project as ProjectInternal,
	)
	val java = project.components.getByName<AdhocComponentWithVariants>("java")
	java.addVariantsFromConfiguration(kdocVariant, JavaConfigurationVariantMapping("runtime", true))
}
