package com.android.build.gradle.internal.test.report

import java.io.File

/**
 * This class is the bridge between the public world and AGP's internals.
 */
class ResilientTestReport(
	reportType: ReportType,
	resultDir: File,
	reportDir: File
) : TestReport(reportType, resultDir, reportDir) {

	override fun generateReport() {
		val model = loadModel()
		val allClassResults = model.packages.flatMap { it.classes }
		allClassResults.forEach(::polyfillResults)
		generateFiles(model)
	}

	private fun polyfillResults(classResults: ClassTestResults) {
		val results = classResults.testResultsMap
		val template = results.values.first().values.first()
		for (testName in results.flatMap { it.value.keys }) {
			for (device in results.keys) {
				if (!results.getValue(device).containsKey(testName)) {
					val test = classResults.addTest(
						testName, 0, device, template.project, template.flavor
					)
					test.ignored()
					//test.addFailure("Missing run data", "N/A", device, template.project, template.flavor)
				}
			}
		}
	}

	/**
	 * Internal AGP API not designed for extensibility, so need to hack around a bit.
	 */
	companion object {

		/**
		 * @see TestReport.loadModel
		 */
		private fun TestReport.loadModel() =
			TestReport::class.java.getDeclaredMethod("loadModel")
				.apply { isAccessible = true }
				.invoke(this) as AllTestResults

		/**
		 * @see TestReport.generateFiles
		 */
		private fun TestReport.generateFiles(model: AllTestResults) =
			TestReport::class.java.getDeclaredMethod("generateFiles", AllTestResults::class.java)
				.apply { isAccessible = true }
				.invoke(this, model)
				.let { Unit } // "void" method, so let's return Unit
	}
}
