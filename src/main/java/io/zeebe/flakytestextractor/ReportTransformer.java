package io.zeebe.flakytestextractor;

import java.util.Optional;

public class ReportTransformer {

	public Optional<ExtendedReportTestSuite> transform(ExtendedReportTestSuite testSuite) {
		if (testSuite.getNumberOfFlakes() == 0) {
			return Optional.empty();
		}

		ExtendedReportTestSuite result = new ExtendedReportTestSuite();

		result.setName(testSuite.getName());
		result.setFullClassName(testSuite.getFullClassName());
		result.setNumberOfErrors(0);
		result.setNumberOfFailures(testSuite.getNumberOfFlakes());
		result.setNumberOfFlakes(testSuite.getNumberOfFlakes());
		result.setNumberOfSkipped(0);
		result.setNumberOfTests(testSuite.getNumberOfFlakes());
		result.setTimeElapsed(testSuite.getTimeElapsed());

		for (ExtendedReportTestCase testCase : testSuite.getTestCases()) {
			if (testCase.isFlake()) {
				ExtendedReportTestCase flakyTestCase = new ExtendedReportTestCase();

				flakyTestCase.setFlake()
						.setClassName(testCase.getClassName())
						.setFullClassName(testCase.getFullClassName())							
						.setName(transformName(testCase.getName()))
						.setFullName(transformName(testCase.getFullName()))
						.setFailureType(transformName(testCase.getFailureType()))
						.setFailureDetail(testCase.getFailureDetail())
						.setFailureErrorLine(testCase.getFailureErrorLine())
						.setTime(testCase.getTime())
						.setSystemError(testCase.getSystemError())
						.setSystemOut(testCase.getSystemOut());
				
				result.getTestCases().add(flakyTestCase);
			}
		}

		return Optional.of(result);
	}

	private String transformName(String name) {
		return name + " (Flaky Test)";
	}
}
