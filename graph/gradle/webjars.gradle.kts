val webjars by configurations.registering {
	attributes {
		attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
	}
	isVisible = false
	isCanBeConsumed = false
	isCanBeResolved = true
}
// Expose it to the compiler, so it's visible as a dependency too.
configurations.named("compileOnly").configure { extendsFrom(webjars.get()) }

val extractWebJars by tasks.registering(ExtractWebJarsTask::class) {
	configuration.setFrom(project.configurations["webjars"])
	artifacts.set(project.configurations["webjars"].incoming.artifactView { }.artifacts.resolvedArtifacts)
	val sourceSets = project.extensions.getByType<SourceSetContainer>()
	outputDirectory.set(project.layout.dir(sourceSets.named("main").map { it.resources.srcDirs.first() }))
}
tasks.named("processResources").configure { dependsOn(extractWebJars) }

// Note: alternative config creating another resource folder. It works, but doesn't allow local testing. 
//ExtractWebJarsTask.this.cleanFirst.set(true)
//ExtractWebJarsTask.this.outputDirectory.set(project.layout.buildDirectory.dir("webjars-as-resources"))
//sourceSets.main.configure { resources.srcDir(extractWebJars) }

@UntrackedTask(because = "The output directory might overlap with existing source folder.")
abstract class ExtractWebJarsTask @Inject constructor(
	private val files: FileSystemOperations,
	private val workers: WorkerExecutor,
) : DefaultTask() {

	@get:CompileClasspath
	@get:SkipWhenEmpty
	abstract val configuration: ConfigurableFileCollection

	@get:Internal
	abstract val artifacts: SetProperty<ResolvedArtifactResult>

	@get:Input
	@get:Optional
	abstract val cleanFirst: Property<Boolean>

	@get:OutputDirectory
	abstract val outputDirectory: DirectoryProperty

	init {
		// See https://docs.gradle.org/current/userguide/configuration_cache.html#config_cache:requirements
		@Suppress("LeakingThis")
		notCompatibleWithConfigurationCache(
			"Doc says 'Provider<Set<ResolvedArtifactResult>> that can be mapped as an input to your task'."
		)
	}

	@TaskAction
	fun extract() {
		if (cleanFirst.getOrElse(false)) {
			files.delete { delete(outputDirectory) }
		}
		val work = workers.noIsolation()
		artifacts.get().forEach { artifact ->
			val id = artifact.id.componentIdentifier as? ModuleComponentIdentifier ?: return@forEach
			work.submit(ExtractWebJarAction::class) {
				this.localWebJar.set(artifact.file)
				this.artifactId.set("${id.group}:${id.module}:${id.version}")
				this.outputDirectory.set(this@ExtractWebJarsTask.outputDirectory)
			}
		}
		work.await()
	}

	abstract class ExtractWebJarAction @Inject constructor(
		private val files: FileSystemOperations,
		private val archives: ArchiveOperations,
	) : WorkAction<ExtractWebJarAction.Parameters> {

		interface Parameters : WorkParameters {
			val localWebJar: RegularFileProperty
			val artifactId: Property<String>
			val outputDirectory: DirectoryProperty
		}

		override fun execute() {
			files.copy {
				duplicatesStrategy = DuplicatesStrategy.FAIL
				into(parameters.outputDirectory)
				from(archives.zipTree(parameters.localWebJar))
				configureFiles(parameters.artifactId.get())
				eachFile {
					// https://docs.gradle.org/current/userguide/working_with_files.html#ex-unpacking-a-subset-of-a-zip-file
					relativePath = RelativePath(true, relativePath.segments.last())
				}
				includeEmptyDirs = false
			}
		}

		// Can't use CopySpec.with, because .copySpec() is not available in any injected API.
		private fun CopySpec.configureFiles(artifactCoordinate: String) {
			val (group, module, version) = artifactCoordinate.split(":")
			when (group) {
				"org.webjars" -> {
					val folder = "META-INF/resources/webjars/${module}/${version}"
					include("${folder}/*.js")
					include("${folder}/*.min.js")
					exclude("${folder}/webjars-requirejs.js")
				}
				"org.webjars.npm" -> {
					val folder = "META-INF/resources/webjars/${module}/${version}/dist"
					include("${folder}/${module}.js")
					include("${folder}/${module}.min.js")
					exclude("${folder}/package.js")
				}
				else -> {
					error("Unknown webjar group: ${group}")
				}
			}
		}
	}
}
