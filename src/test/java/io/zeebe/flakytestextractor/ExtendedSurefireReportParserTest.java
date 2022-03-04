package io.zeebe.flakytestextractor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ExtendedSurefireReportParserTest {

  private static String[] RESSOURCES =
      new String[] {
        // bad file that should be skipped
        "invalid-xml-examples/TEST-io.zeebe.broker.it.clustering.BrokerLeaderChangeTest-it-testrun.xml",
        // good files that should be parsed
        "surefire-reports/TEST-com.github.pihme.jenkinstestbed.module1.FlakyTest.xml",
        "surefire-reports/TEST-com.github.pihme.jenkinstestbed.module1.PassingTest.xml"
      };

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  @Before
  public void setUpFiles() throws IOException {
    TestUtil.copyClassPatHResourcesToFolder(RESSOURCES, tempFolder.getRoot());
  }

  @Test
  public void shouldShouldNotThrowParsingExceptionForInvalidXML() {
    // given
    ExtendedSurefireReportParser sut =
        new ExtendedSurefireReportParser(
            Collections.singletonList(tempFolder.getRoot()), Locale.US, new TestLogger());

    // when
    try {
      sut.parseXMLReportFiles();
    } catch (ParsingException e) {
      Assertions.fail("Parser threw parsing exception");
    }
  }

  @Test
  public void shouldIgnoreInvalidXMLButParseTheRest() throws ParsingException {
    // given
    ExtendedSurefireReportParser sut =
        new ExtendedSurefireReportParser(
            Collections.singletonList(tempFolder.getRoot()), Locale.US, new TestLogger());

    // when
    Map<File, List<ExtendedReportTestSuite>> parsedFiles = sut.parseXMLReportFiles();

    // then
    assertThat(parsedFiles).describedAs("Successfully parsed files").hasSize(2);
  }
}
