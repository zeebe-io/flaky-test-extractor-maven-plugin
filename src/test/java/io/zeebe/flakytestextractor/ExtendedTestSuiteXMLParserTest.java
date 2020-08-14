package io.zeebe.flakytestextractor;



import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import io.zeebe.flakytestextractor.ExtendedReportTestCase;
import io.zeebe.flakytestextractor.ExtendedReportTestSuite;
import io.zeebe.flakytestextractor.ExtendedTestSuiteXMLParser;

public class ExtendedTestSuiteXMLParserTest {
	private static final ClassLoader CLASS_LOADER = Thread.currentThread().getContextClassLoader();

	private final ExtendedTestSuiteXMLParser sut = new ExtendedTestSuiteXMLParser(new TestLogger());

	@Test
	public void testParseOutputOfPassingTest() throws ParserConfigurationException, SAXException, IOException {
		try (InputStreamReader reader = getReaderForClassPathResource(
				"surefire-reports/TEST-com.github.pihme.jenkinstestbed.module1.PassingTest.xml")) {
			List<ExtendedReportTestSuite> testSuites = sut.parse(reader);

			assertThat(testSuites).hasSize(1);

			ExtendedReportTestSuite testSuite = testSuites.get(0);
			assertThat(testSuite.getNumberOfTests()).isEqualTo(1);
			assertThat(testSuite.getNumberOfFailures()).isEqualTo(0);
			assertThat(testSuite.getNumberOfErrors()).isEqualTo(0);
		}
	}

	@Test
	public void testParseOutputOfFailingTest() throws ParserConfigurationException, SAXException, IOException {
		try (InputStreamReader reader = getReaderForClassPathResource(
				"surefire-reports/TEST-com.github.pihme.jenkinstestbed.module1.FailingTest.xml")) {
			List<ExtendedReportTestSuite> testSuites = sut.parse(reader);

			assertThat(testSuites).hasSize(1);

			ExtendedReportTestSuite testSuite = testSuites.get(0);
			assertThat(testSuite.getNumberOfTests()).isEqualTo(1);
			assertThat(testSuite.getNumberOfFailures()).isEqualTo(1);
			assertThat(testSuite.getNumberOfErrors()).isEqualTo(0);
			assertThat(testSuite.getNumberOfFlakes()).isEqualTo(0);

			ExtendedReportTestCase testCase = testSuite.getTestCases().get(0);
			assertThat(testCase.isFlake()).isFalse();
			assertThat(testCase.getSystemOut()).isEqualTo("Failing test\n");
		}
	}

	@Test
	public void testParseOutputOfErrorTest() throws ParserConfigurationException, SAXException, IOException {
		try (InputStreamReader reader = getReaderForClassPathResource(
				"surefire-reports/TEST-com.github.pihme.jenkinstestbed.module1.ErrorTest.xml")) {
			List<ExtendedReportTestSuite> testSuites = sut.parse(reader);

			assertThat(testSuites).hasSize(1);

			ExtendedReportTestSuite testSuite = testSuites.get(0);
			assertThat(testSuite.getNumberOfTests()).isEqualTo(1);
			assertThat(testSuite.getNumberOfFailures()).isEqualTo(0);
			assertThat(testSuite.getNumberOfErrors()).isEqualTo(1);
			assertThat(testSuite.getNumberOfFlakes()).isEqualTo(0);

			ExtendedReportTestCase testCase = testSuite.getTestCases().get(0);
			assertThat(testCase.isFlake()).isFalse();
			assertThat(testCase.getSystemOut()).isEqualTo("Error test\n");
		}
	}

	@Test
	public void testParseOutputOfFlakyTest() throws ParserConfigurationException, SAXException, IOException {
		try (InputStreamReader reader = getReaderForClassPathResource(
				"surefire-reports/TEST-com.github.pihme.jenkinstestbed.module1.FlakyTest.xml")) {
			List<ExtendedReportTestSuite> testSuites = sut.parse(reader);

			assertThat(testSuites).hasSize(1);

			ExtendedReportTestSuite testSuite = testSuites.get(0);
			assertThat(testSuite.getNumberOfTests()).isEqualTo(1);
			assertThat(testSuite.getNumberOfFailures()).isEqualTo(0);
			assertThat(testSuite.getNumberOfErrors()).isEqualTo(0);
			assertThat(testSuite.getNumberOfFlakes()).isEqualTo(1);

			ExtendedReportTestCase testCase = testSuite.getTestCases().get(0);
			assertThat(testCase.isFlake()).isTrue();
			assertThat(testCase.getClassName()).isEqualTo("FlakyTest");
			assertThat(testCase.getFailureType()).isEqualTo("java.lang.AssertionError");
			assertThat(testCase.getName()).isEqualTo("flakyTest");
			assertThat(testCase.getFailureDetail()).isEqualTo("java.lang.AssertionError: failed\n"
					+ "\tat com.github.pihme.jenkinstestbed.module1.FlakyTest.flakyTest(FlakyTest.java:16)\n");
			assertThat(testCase.getSystemOut()).isEqualTo("Flaky test\n");
		}
	}

	@Test
	public void testParseOutputOfFlakyError() throws ParserConfigurationException, SAXException, IOException {
		try (InputStreamReader reader = getReaderForClassPathResource(
				"surefire-reports/TEST-com.github.pihme.jenkinstestbed.module1.FlakyErrorTest.xml")) {
			List<ExtendedReportTestSuite> testSuites = sut.parse(reader);

			assertThat(testSuites).hasSize(1);

			ExtendedReportTestSuite testSuite = testSuites.get(0);
			assertThat(testSuite.getNumberOfTests()).isEqualTo(1);
			assertThat(testSuite.getNumberOfFailures()).isEqualTo(0);
			assertThat(testSuite.getNumberOfErrors()).isEqualTo(0);
			assertThat(testSuite.getNumberOfFlakes()).isEqualTo(1);

			ExtendedReportTestCase testCase = testSuite.getTestCases().get(0);
			assertThat(testCase.isFlake()).isTrue();
			assertThat(testCase.getClassName()).isEqualTo("FlakyErrorTest");
			assertThat(testCase.getFailureType()).isEqualTo("java.lang.RuntimeException");
			assertThat(testCase.getName()).isEqualTo("failNever");
			assertThat(testCase.getFailureDetail()).isEqualTo("java.lang.RuntimeException: Oops, something happeeed\n"
					+ "	at com.github.pihme.jenkinstestbed.module1.FlakyErrorTest.setUp(FlakyErrorTest.java:16)\n");
			assertThat(testCase.getSystemOut()).isEqualTo("Flaky error\n");
		}
	}

	private InputStreamReader getReaderForClassPathResource(String fileName) {
		return new InputStreamReader(CLASS_LOADER.getResourceAsStream(fileName));
	}

}
