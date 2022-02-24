package io.zeebe.flakytestextractor;

import static java.util.Collections.sort;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

public class FlakyTestExtractorPluginTest {

	private final ExtendedTestSuiteXMLParser PARSER = new ExtendedTestSuiteXMLParser(new TestLogger());

	private static String[] RESSOURCES = new String[] {
			"surefire-reports/TEST-com.github.pihme.jenkinstestbed.module1.ErrorTest.xml",
			"surefire-reports/TEST-com.github.pihme.jenkinstestbed.module1.FailingTest.xml",
			"surefire-reports/TEST-com.github.pihme.jenkinstestbed.module1.FlakyErrorTest.xml",
			"surefire-reports/TEST-com.github.pihme.jenkinstestbed.module1.FlakyTest.xml",
			"surefire-reports/TEST-com.github.pihme.jenkinstestbed.module1.PassingTest.xml", };

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUpFiles() throws IOException {
		TestUtil.copyClassPatHResourcesToFolder(RESSOURCES, tempFolder.getRoot());
	}

	@Test
	public void testExecute() throws Exception {
		FlakyTestExtractorPlugin sut = new FlakyTestExtractorPlugin();

		sut.reportDir = tempFolder.getRoot();

		assertThatThrownBy(() -> sut.execute()).hasMessage("Flaky tests encountered");

		File[] createdFiles = tempFolder.getRoot().listFiles(file -> file.getName().endsWith("-FLAKY.xml"));

		assertThat(createdFiles).hasSize(2);

		List<File> generatedReports = Arrays.asList(createdFiles);
		sort(generatedReports);

		inspectFlakyErrorReport(generatedReports.get(0));
		inspectFlakyReport(generatedReports.get(1));
	}

	@Test
	public void testExecuteWithFailBuildFalse() throws Exception {
		FlakyTestExtractorPlugin sut = new FlakyTestExtractorPlugin();

		sut.reportDir = tempFolder.getRoot();
		sut.failBuild = false;

		assertThatCode(() -> {
			sut.execute();
		}).doesNotThrowAnyException();
	}

	@Test
	public void testExecuteWithSkipped() {
		FlakyTestExtractorPlugin sut = new FlakyTestExtractorPlugin();
		sut.reportDir = tempFolder.getRoot();
		sut.skip = true;

		assertThatCode(sut::execute).doesNotThrowAnyException();

		File[] createdFiles = tempFolder.getRoot().listFiles(file -> file.getName().endsWith("-FLAKY.xml"));
		assertThat(createdFiles).isEmpty();
	}

	private void inspectFlakyErrorReport(File flakyErrorReport)
			throws ParserConfigurationException, SAXException, IOException, FileNotFoundException {

		assertThat(flakyErrorReport).hasName("TEST-com.github.pihme.jenkinstestbed.module1.FlakyErrorTest-FLAKY.xml");

		try (InputStreamReader reader = getReaderForFile(flakyErrorReport)) {
			List<ExtendedReportTestSuite> testSuites = PARSER.parse(reader);

			assertThat(testSuites).hasSize(1);

			ExtendedReportTestSuite testSuite = testSuites.get(0);
			assertThat(testSuite.getNumberOfTests()).isEqualTo(1);
			assertThat(testSuite.getNumberOfFailures()).isEqualTo(1);
			assertThat(testSuite.getNumberOfErrors()).isEqualTo(0);
			assertThat(testSuite.getNumberOfFlakes()).isEqualTo(0);
			assertThat(testSuite.getName()).isEqualTo("FlakyErrorTest");

			ExtendedReportTestCase testCase = testSuite.getTestCases().get(0);
			assertThat(testCase.isFlake()).isFalse();
			assertThat(testCase.getName()).isEqualTo("failNever (Flaky Test)");
			assertThat(testCase.getSystemOut()).isEqualTo("Flaky error\n");
		}
	}

	private void inspectFlakyReport(File flakyReport)
			throws ParserConfigurationException, SAXException, IOException, FileNotFoundException {

		assertThat(flakyReport).hasName("TEST-com.github.pihme.jenkinstestbed.module1.FlakyTest-FLAKY.xml");

		try (InputStreamReader reader = getReaderForFile(flakyReport)) {
			List<ExtendedReportTestSuite> testSuites = PARSER.parse(reader);

			assertThat(testSuites).hasSize(1);

			ExtendedReportTestSuite testSuite = testSuites.get(0);
			assertThat(testSuite.getNumberOfTests()).isEqualTo(1);
			assertThat(testSuite.getNumberOfFailures()).isEqualTo(1);
			assertThat(testSuite.getNumberOfErrors()).isEqualTo(0);
			assertThat(testSuite.getNumberOfFlakes()).isEqualTo(0);
			assertThat(testSuite.getName()).isEqualTo("FlakyTest");

			ExtendedReportTestCase testCase = testSuite.getTestCases().get(0);
			assertThat(testCase.isFlake()).isFalse();
			assertThat(testCase.getName()).isEqualTo("flakyTest (Flaky Test)");
			assertThat(testCase.getSystemOut()).isEqualTo("Flaky test\n");
		}
	}

	private InputStreamReader getReaderForFile(File file) throws FileNotFoundException {
		return new InputStreamReader(new FileInputStream(file));
	}

}
