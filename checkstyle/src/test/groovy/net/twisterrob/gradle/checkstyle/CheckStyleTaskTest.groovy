package net.twisterrob.gradle.checkstyle

import net.twisterrob.gradle.test.GradleRunnerRule
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test

class CheckStyleTaskTest {

	private static final List<String> CONFIG_PATH = [ 'config', 'checkstyle', 'checkstyle.xml' ]

	@Rule public final GradleRunnerRule gradle = new GradleRunnerRule()

	@Test void "uses rootProject checkstyle config as a fallback"() {
		given:
		@Language('gradle')
		def settings = """\
			include ':module'
		""".stripIndent()

		@Language('gradle')
		def subProject = """\
			apply plugin: 'net.twisterrob.checkstyle'
			apply plugin: 'com.android.library'
		""".stripIndent()

		@Language('xml')
		def manifest = """\
			<manifest package="module" />
		"""

		gradle.settingsFile() << settings
		gradle.file(subProject, 'module', 'build.gradle')
		gradle.file(manifest, 'module', 'src', 'main', 'AndroidManifest.xml')
		gradle.file(gradle.templateFile('checkstyle-empty.xml').text, CONFIG_PATH)

		def rootProject = gradle.templateFile('android-multiproject-root.gradle').text

		when:
		def result = gradle.run(rootProject, 'checkstyleDebug').build()

		then:
		assert result.task(':module:checkstyleDebug').outcome == TaskOutcome.SUCCESS
		// if build passes the right config was used (otherwise CheckstyleException: Unable to find: ...xml)
		// TODO but it would be nicer to explicitly verify
	}

	@Test void "uses local module checkstyle config if available"() {
		given:
		@Language('gradle')
		def settings = """\
			include ':module'
		""".stripIndent()

		@Language('gradle')
		def module = """\
			apply plugin: 'net.twisterrob.checkstyle'
			apply plugin: 'com.android.library'
		""".stripIndent()

		@Language('xml')
		def manifest = """
			<manifest package="module" />
		"""

		gradle.settingsFile() << settings
		gradle.file(module, 'module', 'build.gradle')
		gradle.file(manifest, 'module', 'src', 'main', 'AndroidManifest.xml')
		gradle.file(gradle.templateFile('checkstyle-empty.xml').text, [ 'module' ] + CONFIG_PATH)

		def rootProject = gradle.templateFile('android-multiproject-root.gradle').text

		when:
		def result = gradle.run(rootProject, 'checkstyleDebug').build()

		then:
		assert result.task(':module:checkstyleDebug').outcome == TaskOutcome.SUCCESS
		// if build passes the right config was used (otherwise CheckstyleException: Unable to find: ...xml)
		// TODO but it would be nicer to explicitly verify
	}

	@Test void "uses local module checkstyle config over rootProject checkstyle config"() {
		given:
		@Language('gradle')
		def settings = """\
			include ':module'
		""".stripIndent()

		@Language('gradle')
		def module = """\
			apply plugin: 'net.twisterrob.checkstyle'
			apply plugin: 'com.android.library'
		""".stripIndent()

		@Language('xml')
		def manifest = """
			<manifest package="module" />
		"""

		gradle.settingsFile() << settings
		gradle.file(module, 'module', 'build.gradle')
		gradle.file(manifest, 'module', 'src', 'main', 'AndroidManifest.xml')
		gradle.file(gradle.templateFile('checkstyle-empty.xml').text, CONFIG_PATH)
		gradle.file(gradle.templateFile('checkstyle-empty.xml').text, [ 'module' ] + CONFIG_PATH)

		def rootProject = gradle.templateFile('android-multiproject-root.gradle').text

		when:
		def result = gradle.run(rootProject, 'checkstyleDebug').build()

		then:
		assert result.task(':module:checkstyleDebug').outcome == TaskOutcome.SUCCESS
		// TODO how to verify it?
	}
}
