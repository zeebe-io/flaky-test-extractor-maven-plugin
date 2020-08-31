package io.zeebe.flakytestextractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * This tests covers cases where the XML test reports are invalid. The desired
 * outcome is that the plugin can skip over invalid XML gracefully
 */
public class InvalidXMLTest {

	private static String[] RESSOURCES = new String[] {
			// bad file that should be skipped
			"invalid-xml-examples/TEST-io.zeebe.broker.it.clustering.BrokerLeaderChangeTest-it-testrun.xml",
			// good files that should be parsed
			"surefire-reports/TEST-com.github.pihme.jenkinstestbed.module1.FlakyTest.xml",
			"surefire-reports/TEST-com.github.pihme.jenkinstestbed.module1.PassingTest.xml" };

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUpFiles() throws IOException {
		TestUtil.copyClassPatHResourcesToFolder(RESSOURCES, tempFolder.getRoot());
	}

	@Test
	public void testShouldIgnoreInvalidXMLAndParseValidXML() throws Exception {
		// given
		FlakyTestExtractorPlugin sut = new FlakyTestExtractorPlugin();
		sut.reportDir = tempFolder.getRoot();

		// when
		assertThatThrownBy(() -> sut.execute()).hasMessage("Flaky tests encountered");

		// then
		File[] createdFiles = tempFolder.getRoot().listFiles(file -> file.getName().endsWith("-FLAKY.xml"));

		assertThat(createdFiles).hasSize(1);
	}

}
