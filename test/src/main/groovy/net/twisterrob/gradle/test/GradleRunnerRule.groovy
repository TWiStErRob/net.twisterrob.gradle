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
				try {
					base.evaluate()
				} finally {
					tearDown()
				}
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
		assert this.buildFile == this.getBuildFile()
		fixClassPath(runner)
	}

	private void tearDown() {
		// not used yet, but useful for debugging
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
	private void fixClassPath(GradleRunner runner) {
		def buildFile = getBuildFile()
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

	File getBuildFile() {
		return new File(runner.projectDir, 'build.gradle')
	}

	File settingsFile() {
		return new File(runner.projectDir, 'settings.gradle')
	}

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
	void file(String contents, Collection<String> path) {
		file(contents, path as String[])
	}

	void file(String contents, String... path) {
		if (path.length == 1 && path[0] == 'build.gradle') {
			buildFile << contents
			return
		}
		temp.newFolder(path[0..path.length - 2] as String[])
		temp.newFile(path.join(File.separator)) << contents
	}

	//@Test:given/@Before
	GradleRunnerRule basedOn(String folder, Object relativeTo = null) {
		return basedOn(templateFile(folder, relativeTo))
	}

	File templateFile(String path, Object relativeTo = null) {
		def container = relativeTo != null? "/${relativeTo.class.package.name}" : ""
		def resource = this.class.getResource("${container}/${path}")
		return new File(resource.file)
	}

	//@Test:given/@Before
	GradleRunnerRule basedOn(File folder) {
		def originalBuildFile = buildFile.text
		println "Deploying ${folder} into ${temp.root}"
		FileUtils.copyDirectory(folder, temp.root)
		if (buildFile.exists()) {
			// merge two files
			def newBuildFile = buildFile.text
			buildFile.delete()
			buildFile << originalBuildFile + System.lineSeparator() + newBuildFile
		}
		return this
	}
	//endregion
}
