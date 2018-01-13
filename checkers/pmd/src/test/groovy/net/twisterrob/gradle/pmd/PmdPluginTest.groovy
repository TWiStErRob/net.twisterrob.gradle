package net.twisterrob.gradle.pmd

import net.twisterrob.gradle.test.GradleRunnerRule
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

class PmdPluginTest {

	private static final String endl = System.lineSeparator()

	@Rule public final GradleRunnerRule gradle = new GradleRunnerRule()

	@BeforeClass static void setUp() {
//		System.properties['java.io.tmpdir'] = $/P:\temp/$
	}

	@Test void "does not apply to empty project"() {
		given:
		@Language('gradle')
		def script = """\
			apply plugin: 'net.twisterrob.pmd'
		""".stripIndent()

		when:
		def result = gradle.run(script, 'pmd').buildAndFail()

		then:
		assert result.failReason() =~ /^Task 'pmd' not found/
	}

	@Test void "does not apply to a Java project"() {
		given:
		@Language('gradle')
		def script = """\
			apply plugin: 'java'
			apply plugin: 'net.twisterrob.pmd'
		""".stripIndent()

		when:
		def result = gradle.run(script, 'pmd').buildAndFail()

		then:
		assert result.failReason() =~ /^Task 'pmd' not found/
	}

	@Test void "applies without a hitch to an Android project"() {
		given:
		@Language('gradle')
		def script = """\
			apply plugin: 'net.twisterrob.pmd'
		""".stripIndent()

		when:
		def result = gradle.basedOn('android-root_app')
		                   .run(script, 'pmdEach')
		                   .build()

		then:
		assert result.task(':pmdEach').outcome == TaskOutcome.UP_TO_DATE
		assert result.task(':pmdDebug').outcome == TaskOutcome.NO_SOURCE
		assert result.task(':pmdRelease').outcome == TaskOutcome.NO_SOURCE
	}

	@Test void "applies to all types of subprojects"() {
		given:
		gradle.file(gradle.templateFile('pmd-empty.xml').text, 'config', 'pmd', 'pmd.xml')
		@Language('gradle')
				script = """
			allprojects {
				apply plugin: 'net.twisterrob.pmd'
			}
		""".stripIndent()
		// ':instant' is not supported yet 
		def modules = [ ':app', ':feature', ':base', ':library', ':library:nested', ':test' ]

		when:
		def result = gradle.basedOn('android-all_kinds')
		                   .run(script, 'pmdEach')
		                   .build()

		then:
		// these tasks are not generated because their modules are special
		def exceptions = [ ':test:pmdRelease' ]
		assert result.taskPaths(TaskOutcome.NO_SOURCE)
		             .containsAll(tasksIn(modules, 'pmdRelease', 'pmdDebug') - exceptions)
		assert result.taskPaths(TaskOutcome.UP_TO_DATE)
		             .containsAll(tasksIn(modules, 'pmdEach'))
		def allTasks = result.tasks.collect {it.path}
		def tasks = tasksIn(modules, 'pmdEach', 'pmdRelease', 'pmdDebug') - exceptions
		assert !(allTasks - tasks).any {it.toLowerCase().contains('pmd')}
	}

	@Test void "applies to subprojects from root"() {
		given:
		def modules = [
				':module1',
				':module2',
				':module2:sub1',
				':module2:sub2',
				':module3:sub1',
				':module3:sub2'
		]
		modules.each {
			gradle.settingsFile() << "include '${it}'" << endl

			@Language('gradle')
			def subProject = """\
				apply plugin: 'com.android.library'
			""".stripIndent()

			@Language('xml')
			def manifest = """\
				<manifest package="project${it.replace(':', '.')}" />
			"""

			def subPath = it.split(':')
			gradle.file(subProject, subPath + [ 'build.gradle' ])
			gradle.file(manifest, subPath + [ 'src', 'main', 'AndroidManifest.xml' ])
		}

		gradle.file(gradle.templateFile('pmd-empty.xml').text, 'config', 'pmd', 'pmd.xml')

		@Language('gradle')
		def rootProject = """\
			allprojects {
				apply plugin: 'net.twisterrob.pmd'
			}
		""".stripIndent()

		when:
		def result = gradle.basedOn('android-multi_module')
		                   .run(rootProject, 'pmdEach')
		                   .build()

		then:
		assert result.taskPaths(TaskOutcome.NO_SOURCE)
		             .containsAll(tasksIn(modules, 'pmdRelease', 'pmdDebug'))
		assert result.taskPaths(TaskOutcome.UP_TO_DATE)
		             .containsAll(tasksIn(modules, 'pmdEach'))
		def allTasks = result.tasks.collect {it.path}
		def tasks = tasksIn(modules, 'pmdEach', 'pmdRelease', 'pmdDebug')
		assert !(allTasks - tasks).any {it.toLowerCase().contains('pmd')}
	}

	@Test void "applies to individual subprojects"() {
		given:
		@Language('gradle')
		def subProjectNotApplied = """\
			apply plugin: 'com.android.library'
		""".stripIndent()
		@Language('gradle')
		def subProjectApplied = """\
			apply plugin: 'net.twisterrob.pmd'
			apply plugin: 'com.android.library'
		""".stripIndent()

		def modules = [
				':module1',
				':module2',
				':module2:sub1',
				':module2:sub2',
				':module3:sub1',
				':module3:sub2'
		]
		def applyTo = [ ':module2', ':module2:sub1', ':module3:sub2' ]
		modules.each {
			gradle.settingsFile() << "include '${it}'" << endl

			def subProject = it in applyTo? subProjectApplied : subProjectNotApplied
			@Language('xml')
			def manifest = """\
				<manifest package="project${it.replace(':', '.')}" />
			""".stripIndent()

			def subPath = it.split(':')
			gradle.file(subProject, subPath + [ 'build.gradle' ])
			gradle.file(manifest, subPath + [ 'src', 'main', 'AndroidManifest.xml' ])
		}

		gradle.file(gradle.templateFile('pmd-empty.xml').text, 'config', 'pmd', 'pmd.xml')

		when:
		def result = gradle.basedOn('android-multi_module')
		                   .run(null, 'pmdEach')
		                   .build()

		then:
		def allTasks = result.tasks.collect {it.path}
		def tasks = tasksIn(applyTo, 'pmdEach', 'pmdRelease', 'pmdDebug')
		assert !(allTasks - tasks).any {it.toLowerCase().contains('pmd')}

		assert result.taskPaths(TaskOutcome.NO_SOURCE)
		             .containsAll(tasksIn(applyTo, 'pmdRelease', 'pmdDebug'))
		assert result.taskPaths(TaskOutcome.UP_TO_DATE)
		             .containsAll(tasksIn(applyTo, 'pmdEach'))
	}

	private static List<String> tasksIn(List<String> modules, String... taskNames) {
		List<List<String>> moduleTaskPairs = [ modules, taskNames ].combinations()
		return moduleTaskPairs.collect {
			// build task from [module, taskName] as "module:taskName"
			it.join(':')
		}
	}
}
