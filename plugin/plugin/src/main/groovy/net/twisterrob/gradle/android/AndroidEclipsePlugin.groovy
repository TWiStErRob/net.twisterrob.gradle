package net.twisterrob.gradle.android

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.AndroidSourceSet
import net.twisterrob.gradle.common.BaseExposedPlugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.XmlProvider
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.plugins.ide.api.XmlFileContentMerger
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.plugins.ide.eclipse.model.EclipseModel

class AndroidEclipsePlugin extends BaseExposedPlugin {
	private BaseExtension android
	private EclipseModel eclipse
	private EclipsePlugin plugin
	private Configuration eclipseProject

	@Override
	void apply(Project target) {
		super.apply(target)
		this.android = project.android
		project.apply plugin: 'eclipse'
		this.eclipse = project.eclipse

		project.plugins.withType(EclipsePlugin) { plugin = it }
		if (plugin == null) {
			return
		}

		plugin.lifecycleTask.dependsOn plugin.cleanTask
		plugin.lifecycleTask.doFirst {
			project.file('gen').mkdirs()
		}

		// TODO dynamic configs (exploded aar or ../project)
		//eclipseProject = project.configurations.maybeCreate('eclipseProject')

		eclipse.project.with {
			natures 'com.android.ide.eclipse.adt.AndroidNature'
			buildCommands.clear();
			buildCommand "com.android.ide.eclipse.adt.ResourceManagerBuilder"
			buildCommand "com.android.ide.eclipse.adt.PreCompilerBuilder"
			buildCommand "org.eclipse.jdt.core.javabuilder"
			buildCommand "com.android.ide.eclipse.adt.ApkBuilder"

			addProjectFilters(file)
		}

		eclipse.classpath.with {
			//ConfigurationContainer c = project.configurations
			//plusConfigurations += [c.compile]
			//minusConfigurations += [c.eclipseProject]
			//noExportConfigurations += [c.eclipseProject]

			containers 'com.android.ide.eclipse.adt.ANDROID_FRAMEWORK'
			containers 'com.android.ide.eclipse.adt.LIBRARIES'
			containers 'com.android.ide.eclipse.adt.DEPENDENCIES'

			fixClasspathAttributes(file)
		}

		addChildTask(plugin.lifecycleTask, createEclipseProjectPropertiesTask())


		def eclipseExplodeAARs = project.task('eclipseExplodeAARs')
		addChildTask(plugin.lifecycleTask, eclipseExplodeAARs)

		project.afterEvaluate {
			def targetFolder = project.file("libs-aars/")
			project.configurations.compile.filter { it.name.endsWith 'aar' }.each { File aarFile ->
				Task explodeAAR = createExplodeAARTask(aarFile, targetFolder)
				addChildTask(eclipseExplodeAARs, explodeAAR)
			}

			eclipse.project.with {
				// TODO referencedProjects for source code debug?
				//addReferences()

				def base = 'PROJECT_LOC/src/main'
				linkedResource name: 'AndroidManifest.xml', type: '1', location: "${base}/AndroidManifest.xml"
				linkedResource name: 'res', type: '2', location: "${base}/res"
				linkedResource name: 'assets', type: '2', location: "${base}/assets"
			}

			eclipse.classpath.with {
				sourceSets = synthesizeSourceSets()
			}
		}
	}

	public void addChildTask(Task parent, Task child) {
		Task cleanParent = getCleanTask(parent)
		Delete cleanChild = project.tasks.create(cleanName(child.name), Delete.class);
		cleanChild.delete(child.outputs.files)

		parent.dependsOn child
		cleanParent.dependsOn cleanChild
	}
	public Task getCleanTask(Task worker) {
		return project.tasks[cleanName(worker.getName())];
	}
	protected static String cleanName(String taskName) {
		return String.format("clean%s", taskName.capitalize());
	}

	private Task createExplodeAARTask(File aarFile, File targetFolder) {
		def baseFilename = aarFile.name.lastIndexOf('.').with { it != -1 ? aarFile.name[0..<it] : aarFile.name }
		// based on http://www.nodeclipse.org/projects/gradle/android
		return project.task("eclipseExplodeAAR${baseFilename}", type: Copy) { Copy task ->
			task.description = 'Explodes AAR dependency ${baseFilename} to the libs directory.'
			task.from project.zipTree(aarFile)
			task.into new File(targetFolder, baseFilename)
			// move classes.jar into libs so Eclipse picks it up
			task.rename { String fileName -> fileName.replace('classes.jar', 'libs/' + baseFilename + '.jar') }
		}
	}

	def addReferences() {
		for (Dependency dep : eclipseProject.dependencies) {
			eclipse.project.referencedProjects.add(dep.name)
		}
	}

	private List synthesizeSourceSets() {
		SourceSetContainer sourceSets = project.sourceSets // from java base plugin

		SourceSet generatedCode = sourceSets.create('generatedCode')
		generatedCode.java.srcDir 'gen'

		SourceSet syntheticMain = sourceSets.create('syntheticMain')
		AndroidSourceSet androidMain = android.sourceSets.getAt("main")
		syntheticMain.java.srcDirs(androidMain.java.srcDirs.toArray())
		syntheticMain.resources.srcDirs(androidMain.resources.srcDirs.toArray())

		SourceSet syntheticTest = sourceSets.create('syntheticTest')
		AndroidSourceSet androidTest = android.sourceSets.getAt("androidTest")
		syntheticTest.java.srcDirs(androidTest.java.srcDirs.toArray())
		syntheticTest.resources.srcDirs(androidTest.resources.srcDirs.toArray())
		return [ generatedCode, syntheticMain, syntheticTest ]
	}

	private static void fixClasspathAttributes(XmlFileContentMerger file) {
		file.whenMerged { classpath ->
			classpath.entries.each {
				// Print all entries (stripping full paths)
				// path='C:/Users/TWiStEr/.gradle/caches/modules-2/files-2.1/com.caverock/androidsvg/1.2.1/d0cb3453e18fffeb053e9b7f052af3dcec9f75b4/androidsvg-1.2.1.jar'
				// file='C:\Users\TWiStEr\.gradle\caches\modules-2\files-2.1\com.caverock\androidsvg\1.2.1\36cf3956e6dcdbfa3250edca68198f64656382d7\androidsvg-1.2.1-sources.jar'
				// jarUrl='jar:file:/C:/Users/TWiStEr/.gradle/caches/modules-2/files-2.1/com.caverock/androidsvg/1.2.1/36cf3956e6dcdbfa3250edca68198f64656382d7/androidsvg-1.2.1-sources.jar!/'
				println it.toString().replaceAll("'([^']*)(?<![a-zA-Z])[a-zA-Z]:[^']*(?<!!)[/\\\\]", "'\$1")
			}
		}
		file.withXml { XmlProvider xmlProvider ->
			// <attributes><attribute name="ignore_optional_problems" value="true"/></attributes>
			xmlProvider.asNode().grep {
				it.name() == 'classpathentry' && it['@kind'] == 'src' && it['@path'] == 'gen'
			}.each { Node cpe ->
				Node attrs = cpe['attributes'].find() as Node ?: cpe.appendNode("attributes")
				Node attr = attrs.find {
					it.name() == 'attribute' && it['@name'] == 'ignore_optional_problems'
				} as Node ?: attrs.appendNode('attribute', [ "name": 'ignore_optional_problems' ])
				attr.attributes()['value'] = true
			}
		}
	}

	private Task createEclipseProjectPropertiesTask() {
		return project.task('eclipseProjectProperties') { Task task ->
			task.description = 'Generate the Eclipse ADT project.properties file'
			def projectPropertiesFile = project.file('project.properties')
			task.outputs.file projectPropertiesFile
			task.doLast {
				new FileOutputStream(projectPropertiesFile, false).close() // truncate

				project.plugins.withType(BasePlugin) { androidPlugin ->
					androidPlugin.ensureTargetSetup()
					def target = androidPlugin.androidBuilder.target // compileSdkVersion
					// TODO handle mapsV1
					projectPropertiesFile.append "# ${target.description} ${target.version}r${target.revision}\n"
					projectPropertiesFile.append "target=android-${target.version.apiLevel}\n"
				}

				project.plugins.withType(LibraryPlugin) {
					projectPropertiesFile.append 'android.library=true\n'
				}

				// TODO Are these links for resources?
				int counter = 0
				for (String proj : eclipse.project.referencedProjects) {
					projectPropertiesFile.append "android.library.reference.${++counter}=../${proj}\n"
				}

				println "Import to workspace: ${android.sdkDirectory}/extras/google/google_play_services/libproject/google-play-services_lib"
			}
		}
	}

	private static void addProjectFilters(XmlFileContentMerger file) {
		def filters = '''<filteredResources>
			<filter>
				<name/>
				<type>26</type>
				<matcher>
					<id>org.eclipse.ui.ide.orFilterMatcher</id>
					<arguments>
						<matcher><!-- usual temp folder -->
							<id>org.eclipse.ui.ide.multiFilter</id>
							<arguments>1.0-name-matches-true-false-temp</arguments>
						</matcher>
						<!-- eclipse's build folder (bin) is really jumpy, so let's keep it -->
						<matcher><!-- gradle build -->
							<id>org.eclipse.ui.ide.multiFilter</id>
							<arguments>1.0-name-matches-true-false-build</arguments>
						</matcher>
						<matcher><!-- gradle wrapper -->
							<id>org.eclipse.ui.ide.multiFilter</id>
							<arguments>1.0-name-matches-true-false-gradle</arguments>
						</matcher>
						<matcher><!-- maven -->
							<id>org.eclipse.ui.ide.multiFilter</id>
							<arguments>1.0-name-matches-true-false-target</arguments>
						</matcher>
					</arguments>
				</matcher>
			</filter>
		</filteredResources>'''
		file.withXml { XmlProvider xmlProvider ->
			xmlProvider.asNode().append(new XmlParser().parseText(filters))
		}
	}

/*
def linkLib(libName) {
    def Files = java.nio.file.Files
    def linkFile = file('libs/' + libName)
    def linkPath = linkFile.toPath()
    if (linkFile.exists() && !Files.isSymbolicLink(linkPath)) {
        delete linkFile
    }
    if (!linkFile.exists()) {
        Files.createSymbolicLink(linkPath, file('../' + libName).toPath())
    }
}

task createLink << {
    linkLib('twister-lib-android')
    linkLib('twister-lib-java')
}
eclipse {
	classpath {
		file {
			// TODO move to whenMerged
			withXml { xmlProvider ->
				def classpath = xmlProvider.asNode()
				classpath.appendNode "classpathentry", ["combineaccessrules": "false", "kind": "src", "path": "/twister-lib-java", "exported": "true" ]
			}
		}
	}
}
task deps << {
	[configurations.default, configurations.compile, configurations.runtime, configurations.provided, configurations.eclipseProjects].each { c ->
		println 'Configuration: ' + c.name
		println '\tdirect deps artifacts:'
	    c.resolvedConfiguration.firstLevelModuleDependencies.moduleArtifacts.each {
	         println '\t\t' + it.file
	    }
	    println '\tall deps artifacts:'
	    c.resolvedConfiguration.firstLevelModuleDependencies.allModuleArtifacts.each {
	         println '\t\t' + it.file
	    }
	}
}
*/
}
