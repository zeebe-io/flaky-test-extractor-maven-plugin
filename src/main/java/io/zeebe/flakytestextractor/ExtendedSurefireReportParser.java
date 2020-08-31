package io.zeebe.flakytestextractor;

import static org.apache.maven.surefire.shared.utils.StringUtils.split;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.surefire.shared.utils.io.DirectoryScanner;
import org.xml.sax.SAXException;

/**
 * Based on
 * {@code org.apache.maven.plugins.surefire.report.SurefireReportParser}
 * (licensed under http://www.apache.org/licenses/LICENSE-2.0).
 * 
 * This implementation uses the extended data objects to capture more
 * information on flaky tests
 *
 */
public class ExtendedSurefireReportParser {
	private static final String INCLUDES = "*.xml";

	private static final String EXCLUDES = "*.txt, testng-failed.xml, testng-failures.xml, testng-results.xml, failsafe-summary*.xml";

	private final Log logger;

	private final List<File> reportsDirectories;

	public ExtendedSurefireReportParser(List<File> reportsDirectories, Locale locale, Log logger) {
		this.reportsDirectories = reportsDirectories;
		this.logger = logger;
	}

	public Map<File, List<ExtendedReportTestSuite>> parseXMLReportFiles() throws ParsingException {
		final Collection<File> xmlReportFiles = new ArrayList<>();
		for (File reportsDirectory : reportsDirectories) {
			if (reportsDirectory.exists()) {
				for (String xmlReportFile : getIncludedFiles(reportsDirectory, INCLUDES, EXCLUDES)) {
					xmlReportFiles.add(new File(reportsDirectory, xmlReportFile));
				}
			}
		}

		final Map<File, List<ExtendedReportTestSuite>> result = new HashMap<>();

		final ExtendedTestSuiteXMLParser parser = new ExtendedTestSuiteXMLParser(logger);
		for (File xmlReportFile : xmlReportFiles) {
			try {
				result.put(xmlReportFile, parser.parse(xmlReportFile.getAbsolutePath()));
			} catch (ParserConfigurationException e) {
				throw new ParsingException("Error setting up parser for JUnit XML report", e);
			} catch (SAXException e) {
				logger.info("Skipping " + xmlReportFile.getName() + " because of parsing exception:"
						+ e.getLocalizedMessage());
			} catch (IOException e) {
				throw new ParsingException("Error reading JUnit XML report " + xmlReportFile, e);
			}
		}

		return result;
	}

	private static String[] getIncludedFiles(File directory, String includes, String excludes) {
		DirectoryScanner scanner = new DirectoryScanner();

		scanner.setBasedir(directory);

		scanner.setIncludes(split(includes, ","));

		scanner.setExcludes(split(excludes, ","));

		scanner.scan();

		return scanner.getIncludedFiles();
	}
}
