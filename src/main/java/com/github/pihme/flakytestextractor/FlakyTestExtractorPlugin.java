package com.github.pihme.flakytestextractor;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "extract-flaky-tests", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class FlakyTestExtractorPlugin extends AbstractMojo {

	private static final ReportTransformer TRANSFORMER = new ReportTransformer();

	@Parameter(defaultValue = "${project.build.directory}/surefire-reports", property = "reportDir")
	protected File reportDir;

	@Parameter(defaultValue = "true", property = "failBuild", required = false)
	protected boolean failBuild = true;

	public void execute() throws MojoFailureException {
		getLog().info("FlakyTestExtractorPlugin - starting");
		getLog().info("reportDir: " + reportDir.getAbsolutePath());
		getLog().info("failBuild: " + failBuild);
		
		boolean foundFlakyTests = false;

		XmlReporterWriter reportWriter = new XmlReporterWriter(reportDir);

		ExtendedSurefireReportParser reportsParser = new ExtendedSurefireReportParser(
				Collections.singletonList(reportDir), Locale.getDefault(), getLog());

		try {
			Map<File, List<ExtendedReportTestSuite>> testReports = reportsParser.parseXMLReportFiles();
			
			getLog().debug("testReports.size: " + testReports.size());
			
			for (File reportFile : testReports.keySet()) {
				List<ExtendedReportTestSuite> testSuites = testReports.get(reportFile);

				List<ExtendedReportTestSuite> testSuitesWithOnlyFlakyTests = testSuites.stream()
						.map(TRANSFORMER::transform).filter(Optional::isPresent).map(Optional::get)
						.collect(Collectors.toList());

				getLog().debug("testSuitesWithOnlyFlakyTests.size: " + testSuitesWithOnlyFlakyTests.size());
				
				if (!testSuitesWithOnlyFlakyTests.isEmpty()) {
					foundFlakyTests = true;
					reportWriter.writeXMLReport(reportFile, testSuitesWithOnlyFlakyTests);
				}
			}
		} catch (ParsingException e) {
			getLog().error(e);
		}

		if (foundFlakyTests && failBuild) {
			getLog().info("FlakyTestExtractorPlugin - finished and about to fail the build");
			throw new MojoFailureException("Flaky tests encountered");
		}

		getLog().info("FlakyTestExtractorPlugin - finished");
	}

}
