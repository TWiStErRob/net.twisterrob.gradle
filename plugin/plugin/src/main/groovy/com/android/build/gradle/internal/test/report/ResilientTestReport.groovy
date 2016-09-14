package com.android.build.gradle.internal.test.report

public class ResilientTestReport extends TestReport {
	ResilientTestReport(ReportType reportType, File resultDir, File reportDir) {
		super(reportType, resultDir, reportDir)
	}
	@SuppressWarnings("GroovyAccessibility")
	@Override public void generateReport() {
		AllTestResults model = super.loadModel();
		for (PackageTestResults packageResults : model.getPackages()) {
			for (ClassTestResults classResults : packageResults.getClasses()) {
				Map<String, Map<String, TestResult>> results = classResults.getTestResultsMap();
				def template = results.values().first().values().first()
				for (String testName : results.collectMany { it.value.keySet() }) {
					for (String device : results.keySet()) {
						if (!results.get(device).containsKey(testName)) {
							def test = classResults.addTest(testName, 0, device, template.project, template.flavor)
							test.ignored()
							//test.addFailure("Missing run data", "N/A", device, template.project, template.flavor)
						}
					}
				}
			}
		}
		super.generateFiles(model);
	}
}
