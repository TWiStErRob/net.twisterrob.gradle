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
			// it's all in the template
		""".stripIndent()

		when:
		def result = gradle.basedOn('simpleAndroid', this)
		                   .run(script, 'checkstyleAll')
		                   .build()

		then:
		assert result.task(':checkstyleAll').outcome == TaskOutcome.SUCCESS
		assert result.task(':checkstyleDebug').outcome == TaskOutcome.SUCCESS
		assert result.task(':checkstyleRelease').outcome == TaskOutcome.SUCCESS
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
				apply plugin: 'net.twisterrob.checkstyle'
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

		when:
		def result = gradle.run(rootProject, 'checkstyleAll').build()

		then:
		def tasksNames = [ 'checkstyleAll', 'checkstyleRelease', 'checkstyleDebug' ]
		List<String> tasks = ([ modules, tasksNames ].combinations() as List<List<String>>).collect {it.join(':')}
		assert result.taskPaths(TaskOutcome.SUCCESS).containsAll(tasks)
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
			def manifest = """<manifest package="project${it.replace(':', '.')}" />"""

			def subPath = it.split(':')
			gradle.file(subProject, subPath + [ 'build.gradle' ])
			gradle.file(manifest, subPath + [ 'src', 'main', 'AndroidManifest.xml' ])
		}

		gradle.file(gradle.templateFile('checkstyle-empty.xml').text, 'config', 'checkstyle', 'checkstyle.xml')
		def rootProject = gradle.templateFile('android-multiproject-root.gradle').text

		when:
		def result = gradle.run(rootProject, 'checkstyleAll').build()

		then:
		def tasksNames = [ 'checkstyleAll', 'checkstyleRelease', 'checkstyleDebug' ]
		List<String> tasks = ([ applyTo, tasksNames ].combinations() as List<List<String>>).collect {it.join(':')}
		def successfulTasks = result.taskPaths(TaskOutcome.SUCCESS)
		assert successfulTasks.containsAll(tasks)
		assert !(successfulTasks - tasks).any {it.toLowerCase().contains('checkstyle')}
	}
}
