package net.twisterrob.gradle.checkstyle

import net.twisterrob.gradle.test.GradleRunnerRule
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

class CheckStylePluginTest {

	private static final String endl = System.lineSeparator()

	@Rule public final GradleRunnerRule gradle = new GradleRunnerRule()

	@BeforeClass static void setUp() {
//		System.properties['java.io.tmpdir'] = $/P:\temp/$
	}

	@Test void "does not apply to empty project"() {
		given:
		@Language('gradle')
		def script = """\
			apply plugin: 'net.twisterrob.checkstyle'
		""".stripIndent()

		when:
		def result = gradle.run(script, 'checkstyle').buildAndFail()

		then:
		assert result.failReason() =~ /^Task 'checkstyle' not found/
	}

	@Test void "does not apply to a Java project"() {
		given:
		@Language('gradle')
		def script = """\
			apply plugin: 'java'
			apply plugin: 'net.twisterrob.checkstyle'
		""".stripIndent()

		when:
		def result = gradle.run(script, 'checkstyle').buildAndFail()

		then:
		assert result.failReason() =~ /^Task 'checkstyle' not found/
	}

	@Test void "applies without a hitch to an Android project"() {
		given:
		@Language('gradle')
		def script = """\
			apply plugin: 'net.twisterrob.checkstyle'
		""".stripIndent()

		when:
		def result = gradle.basedOn('simpleAndroid', this)
		                   .run(script, 'checkstyleAll')
		                   .build()

		then:
		assert result.task(':checkstyleAll').outcome == TaskOutcome.UP_TO_DATE
		assert result.task(':checkstyleDebug').outcome == TaskOutcome.NO_SOURCE
		assert result.task(':checkstyleRelease').outcome == TaskOutcome.NO_SOURCE
	}

	@Test void "applies to all types of subprojects"() {
		given:
		gradle.file(gradle.templateFile('checkstyle-empty.xml').text, 'config', 'checkstyle', 'checkstyle.xml')
		def script = gradle.templateFile('android-multiproject-root.gradle').text
		script += """
			allprojects {
				apply plugin: 'net.twisterrob.checkstyle'
			}
		""".stripIndent()
		// ':instant' is not supported yet 
		def modules = [ ':app', ':feature', ':base', ':library', ':library:nested', ':test' ]

		when:
		def result = gradle.basedOn('allKindsOfSubProjects', this)
		                   .run(script, 'checkstyleAll')
		                   .build()

		then:
		// these tasks are not generated because their modules are special
		def exceptions = [ ':test:checkstyleRelease' ]
		assert result.taskPaths(TaskOutcome.NO_SOURCE)
		             .containsAll(tasksIn(modules, 'checkstyleRelease', 'checkstyleDebug') - exceptions)
		assert result.taskPaths(TaskOutcome.UP_TO_DATE)
		             .containsAll(tasksIn(modules, 'checkstyleAll'))
		def allTasks = result.tasks.collect {it.path}
		def tasks = tasksIn(modules, 'checkstyleAll', 'checkstyleRelease', 'checkstyleDebug') - exceptions
		assert !(allTasks - tasks).any {it.toLowerCase().contains('checkstyle')}
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
			def manifest = """<manifest package="project${it.replace(':', '.')}" />"""

			def subPath = it.split(':')
			gradle.file(subProject, subPath + [ 'build.gradle' ])
			gradle.file(manifest, subPath + [ 'src', 'main', 'AndroidManifest.xml' ])
		}

		gradle.file(gradle.templateFile('checkstyle-empty.xml').text, 'config', 'checkstyle', 'checkstyle.xml')
		def rootProject = gradle.templateFile('android-multiproject-root.gradle').text
		rootProject += """
			allprojects {
				apply plugin: 'net.twisterrob.checkstyle'
			}
		""".stripIndent()

		when:
		def result = gradle.run(rootProject, 'checkstyleAll').build()

		then:
		assert result.taskPaths(TaskOutcome.NO_SOURCE)
		             .containsAll(tasksIn(modules, 'checkstyleRelease', 'checkstyleDebug'))
		assert result.taskPaths(TaskOutcome.UP_TO_DATE)
		             .containsAll(tasksIn(modules, 'checkstyleAll'))
		def allTasks = result.tasks.collect {it.path}
		def tasks = tasksIn(modules, 'checkstyleAll', 'checkstyleRelease', 'checkstyleDebug')
		assert !(allTasks - tasks).any {it.toLowerCase().contains('checkstyle')}
	}

	@Test void "applies to individual subprojects"() {
		given:
		def modules = [
				':module1',
				':module2',
				':module2:sub1',
				':module2:sub2',
				':module3:sub1',
				':module3:sub2'
		]
		def applyTo = [ ':module2', ':module2:sub1', ':module3:sub2' ]
		@Language('gradle')
		def subProjectNotApplied = """\
			apply plugin: 'com.android.library'
		""".stripIndent()
		@Language('gradle')
		def subProjectApplied = """\
			apply plugin: 'net.twisterrob.checkstyle'
			apply plugin: 'com.android.library'
		""".stripIndent()
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

		gradle.file(gradle.templateFile('checkstyle-empty.xml').text, 'config', 'checkstyle', 'checkstyle.xml')
		def rootProject = gradle.templateFile('android-multiproject-root.gradle').text

		when:
		def result = gradle.run(rootProject, 'checkstyleAll').build()

		then:
		def allTasks = result.tasks.collect {it.path}
		def tasks = tasksIn(applyTo, 'checkstyleAll', 'checkstyleRelease', 'checkstyleDebug')
		assert !(allTasks - tasks).any {it.toLowerCase().contains('checkstyle')}

		assert result.taskPaths(TaskOutcome.NO_SOURCE)
		             .containsAll(tasksIn(applyTo, 'checkstyleRelease', 'checkstyleDebug'))
		assert result.taskPaths(TaskOutcome.UP_TO_DATE)
		             .containsAll(tasksIn(applyTo, 'checkstyleAll'))
	}

	private static List<String> tasksIn(List<String> modules, String... taskNames) {
		List<List<String>> moduleTaskPairs = [ modules, taskNames ].combinations()
		return moduleTaskPairs.collect {
			// build task from [module, taskName] as "module:taskName"
			it.join(':')
		}
	}
}
