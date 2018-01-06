package net.twisterrob.gradle.test

import groovy.transform.CompileStatic
import org.gradle.internal.impldep.org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.intellij.lang.annotations.Language
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Simplified {@link org.junit.Rule} around {@link GradleRunner} to reduce code repetition.
 */
@CompileStatic
class GradleRunnerRule implements TestRule {

	private final TemporaryFolder temp = new TemporaryFolder()
	private File buildFile

	GradleRunner runner

	//region TestRule
	@Override
	Statement apply(Statement base, Description description) {
		def setUpTestProject = new Statement() {

			@Override
			void evaluate() {
				setUp()
				base.evaluate()
			}
		}
		// by applying this other rule around our statement we get the temp folder before our code is called
		return temp.apply(setUpTestProject, description)
	}
	//endregion

	//region GradleRunner wrapper
	//@Before(automatic with @Rule)
	private void setUp() {
		buildFile = temp.newFile('build.gradle')
		runner = GradleRunner
				.create()
				.forwardOutput()
				.withProjectDir(temp.root)
				.withPluginClasspath()
		fixClassPath(runner)
	}

	//@Test:when
	GradleRunner run(@Language("gradle") String script, String... tasks) {
		buildFile << script
		def args = tasks + [ '--stacktrace' ]
		println """
				Running `gradle ${args}` on ${buildFile.absolutePath}:
				```gradle
${buildFile.text.trim().split('\n').collect {'\t\t\t\t\t' + it}.join('\n')}
				```
			""".stripIndent()
		return runner.withArguments(args)
	}

	/**
	 * This is a workaround because runner.withPluginClasspath() doesn't seem to work.
	 */
	private static void fixClassPath(GradleRunner runner) {
		def buildFile = new File(runner.projectDir, 'build.gradle')
		def classPaths = runner
				.pluginClasspath
				.collect {"\t\t\t\t\tclasspath files('${it.absolutePath.replace('\\', '\\\\')}')"}
				.join('\n')
		@Language("gradle")
		def buildscript = """\
			buildscript {
				dependencies {
${classPaths}
				}
			}
		""".stripIndent()
		buildFile << buildscript
	}
	//endregion

	//region Helper methods
	//@Test:given/@Before
	void setGradleVersion(String version) {
		def distributionUrl = URI.create("https://services.gradle.org/distributions/gradle-${version}-all.zip")
		runner
				.withGradleVersion(version)
				.withGradleDistribution(distributionUrl)
	}

	//@Test:given/@Before
	void file(String contents, String path) {
		file(contents, path.split(/[\/\\]/))
	}

	//@Test:given/@Before
	void file(String contents, String... path) {
		temp.newFolder(path[0..path.length - 2] as String[])
		temp.newFile(path.join(File.separator)) << contents
	}

	//@Test:given/@Before
	GradleRunnerRule basedOn(String folder, Object relativeTo) {
		def path = relativeTo != null? "/${relativeTo.class.package.name}" : ""
		def template = new File(this.class.getResource("${path}/${folder}/").file)
		return basedOn(template)
	}

	//@Test:given/@Before
	GradleRunnerRule basedOn(File folder) {
		FileUtils.copyDirectory(folder, temp.root, new FileFilter() {

			@Override
			boolean accept(File pathname) {
				return pathname.getName() != "build.gradle"
			}
		})
		buildFile << new File(folder, "build.gradle").text
		return this
	}
	//endregion
}
