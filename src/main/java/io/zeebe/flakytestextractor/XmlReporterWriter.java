package io.zeebe.flakytestextractor;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.maven.plugin.surefire.report.FileReporterUtils.stripIllegalFilenameChars;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.maven.plugin.surefire.booterclient.output.InPluginProcessDumpSingleton;
import org.apache.maven.surefire.api.report.ReporterException;
import org.apache.maven.surefire.shared.utils.xml.PrettyPrintXMLWriter;
import org.apache.maven.surefire.shared.utils.xml.XMLWriter;

@SuppressWarnings({ "javadoc" })
/**
 * Based on {@code org.apache.maven.plugin.surefire.report.StatelessXmlReporter}
 * (licensed under http://www.apache.org/licenses/LICENSE-2.0).
 * 
 */
public class XmlReporterWriter {

	private final File reportsDirectory;

	private final String xsdSchemaLocation = "https://maven.apache.org/surefire/maven-surefire-plugin/xsd/surefire-test-report-3.0.xsd";

	private final String xsdVersion = "3.0";

	public XmlReporterWriter(File reportsDirectory) {
		this.reportsDirectory = reportsDirectory;
	}

	public void writeXMLReport(File originalReport, List<ExtendedReportTestSuite> testSuites) {

		OutputStream outputStream = getOutputStream(originalReport);
		try (OutputStreamWriter fw = getWriter(outputStream)) {
			XMLWriter ppw = new PrettyPrintXMLWriter(fw);
			ppw.setEncoding(UTF_8.name());

			for (ExtendedReportTestSuite testSuite : testSuites) {
				createTestSuiteElement(ppw, testSuite); // TestSuite

				serializeTestClassWithoutRerun(outputStream, fw, ppw, testSuite.getTestCases());

				ppw.endElement(); // TestSuite
			}
		} catch (Exception e) {
			// It's not a test error.
			// This method must be sail-safe and errors are in a dump log.
			// The control flow must not be broken in TestSetRunListener#testSetCompleted.
			InPluginProcessDumpSingleton.getSingleton().dumpException(e, e.getLocalizedMessage(), reportsDirectory);
		}
	}

	private void serializeTestClassWithoutRerun(OutputStream outputStream, OutputStreamWriter fw, XMLWriter ppw,
			List<ExtendedReportTestCase> testCases) {
		for (ExtendedReportTestCase testCase : testCases) {
			startTestElement(ppw, testCase);
			if (!testCase.isSuccessful()) {
				getTestProblems(fw, ppw, testCase, outputStream);
			}
			createOutErrElements(fw, ppw, testCase, outputStream);
			ppw.endElement();
		}
	}

	private OutputStream getOutputStream(File originalReport) {
		String originalFileName = originalReport.getName();
		String originalFilenameWithoutXML = originalFileName.substring(0, originalFileName.length() - 4);

		File reportFile = new File(reportsDirectory,
				stripIllegalFilenameChars(originalFilenameWithoutXML + "-FLAKY.xml"));

		try {
			return new BufferedOutputStream(new FileOutputStream(reportFile), 64 * 1024);
		} catch (Exception e) {
			throw new ReporterException("When writing report", e);
		}
	}

	private static OutputStreamWriter getWriter(OutputStream fos) {
		return new OutputStreamWriter(fos, UTF_8);
	}

	private void createTestSuiteElement(XMLWriter ppw, ExtendedReportTestSuite testSuite) {
		ppw.startElement("testsuite");

		ppw.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		ppw.addAttribute("xsi:noNamespaceSchemaLocation", xsdSchemaLocation);
		ppw.addAttribute("version", xsdVersion);

		String reportName = testSuite.getName();
		ppw.addAttribute("name", reportName == null ? "" : extraEscapeAttribute(reportName));
		ppw.addAttribute("time", String.valueOf(testSuite.getTimeElapsed()));
		ppw.addAttribute("tests", String.valueOf(testSuite.getNumberOfTests()));
		ppw.addAttribute("errors", String.valueOf(testSuite.getNumberOfErrors()));
		ppw.addAttribute("skipped", String.valueOf(testSuite.getNumberOfSkipped()));
		ppw.addAttribute("failures", String.valueOf(testSuite.getNumberOfFailures()));
	}

	private void startTestElement(XMLWriter ppw, ExtendedReportTestCase testCase) {
		ppw.startElement("testcase");
		String name = testCase.getName();
		ppw.addAttribute("name", name == null ? "" : extraEscapeAttribute(name));

		String className = testCase.getClassName();
		if (className != null) {
			ppw.addAttribute("classname", extraEscapeAttribute(className));
		}

		ppw.addAttribute("time", String.valueOf(testCase.getTime()));
	}

	private static void getTestProblems(OutputStreamWriter outputStreamWriter, XMLWriter ppw,
			ExtendedReportTestCase testCase, OutputStream fw) {
		ppw.startElement("failure"); // failure

		addAttributeIfNotEmpty(ppw, "message", testCase.getFailureMessage());
		addAttributeIfNotEmpty(ppw, "type", testCase.getFailureType());

		String stackTrace = testCase.getFailureDetail();
		if (stackTrace != null) {
			extraEscapeElementValue(stackTrace, outputStreamWriter, ppw, fw);
		}

		ppw.endElement(); // failure
	}

	private static void addAttributeIfNotEmpty(XMLWriter ppw, String attributeName, String valueToWrite) {
		if (valueToWrite != null && !valueToWrite.isEmpty()) {
			ppw.addAttribute(attributeName, extraEscapeAttribute(valueToWrite));
		}
	}

	// Create system-out and system-err elements
	private static void createOutErrElements(OutputStreamWriter outputStreamWriter, XMLWriter ppw,
			ExtendedReportTestCase testCase, OutputStream fw) {
		EncodingOutputStream eos = new EncodingOutputStream(fw);

		addOutputStreamElement(outputStreamWriter, eos, ppw, testCase.getSystemOut(), "system-out");
		addOutputStreamElement(outputStreamWriter, eos, ppw, testCase.getSystemError(), "system-err");
	}

	private static void addOutputStreamElement(OutputStreamWriter outputStreamWriter, EncodingOutputStream eos,
			XMLWriter xmlWriter, String content, String name) {
		if (content != null && !content.isEmpty()) {
			xmlWriter.startElement(name);

			try {
				xmlWriter.writeText(""); // Cheat sax to emit element
				outputStreamWriter.flush();
				eos.getUnderlying().write(ByteConstantsHolder.CDATA_START_BYTES); // emit cdata
				eos.write(content.getBytes(UTF_8));
				eos.getUnderlying().write(ByteConstantsHolder.CDATA_END_BYTES);
				eos.flush();
			} catch (IOException e) {
				throw new ReporterException("When writing xml report stdout/stderr", e);
			}
			xmlWriter.endElement();
		}
	}

	/**
	 * Handle stuff that may pop up in java that is not legal in xml.
	 *
	 * @param message The string
	 * @return The escaped string or returns itself if all characters are legal
	 */
	private static String extraEscapeAttribute(String message) {
		// Someday convert to xml 1.1 which handles everything but 0 inside string
		return containsEscapesIllegalXml10(message) ? escapeXml(message, true) : message;
	}

	/**
	 * Writes escaped string or the message within CDATA if all characters are
	 * legal.
	 *
	 * @param message The string
	 */
	private static void extraEscapeElementValue(String message, OutputStreamWriter outputStreamWriter,
			XMLWriter xmlWriter, OutputStream fw) {
		// Someday convert to xml 1.1 which handles everything but 0 inside string
		if (containsEscapesIllegalXml10(message)) {
			xmlWriter.writeText(escapeXml(message, false));
		} else {
			try {
				EncodingOutputStream eos = new EncodingOutputStream(fw);
				xmlWriter.writeText(""); // Cheat sax to emit element
				outputStreamWriter.flush();
				eos.getUnderlying().write(ByteConstantsHolder.CDATA_START_BYTES);
				eos.write(message.getBytes(UTF_8));
				eos.getUnderlying().write(ByteConstantsHolder.CDATA_END_BYTES);
				eos.flush();
			} catch (IOException e) {
				throw new ReporterException("When writing xml element", e);
			}
		}
	}

	private static final class EncodingOutputStream extends FilterOutputStream {
		private int c1;

		private int c2;

		EncodingOutputStream(OutputStream out) {
			super(out);
		}

		OutputStream getUnderlying() {
			return out;
		}

		private boolean isCdataEndBlock(int c) {
			return c1 == ']' && c2 == ']' && c == '>';
		}

		@Override
		public void write(int b) throws IOException {
			if (isCdataEndBlock(b)) {
				out.write(ByteConstantsHolder.CDATA_ESCAPE_STRING_BYTES);
			} else if (isIllegalEscape(b)) {
				// uh-oh! This character is illegal in XML 1.0!
				// http://www.w3.org/TR/1998/REC-xml-19980210#charsets
				// we're going to deliberately doubly-XML escape it...
				// there's nothing better we can do! :-(
				// SUREFIRE-456
				out.write(ByteConstantsHolder.AMP_BYTES);
				out.write(String.valueOf(b).getBytes(UTF_8));
				out.write(';'); // & Will be encoded to amp inside xml encodingSHO
			} else {
				out.write(b);
			}
			c1 = c2;
			c2 = b;
		}
	}

	private static boolean containsEscapesIllegalXml10(String message) {
		int size = message.length();
		for (int i = 0; i < size; i++) {
			if (isIllegalEscape(message.charAt(i))) {
				return true;
			}

		}
		return false;
	}

	private static boolean isIllegalEscape(char c) {
		return isIllegalEscape((int) c);
	}

	private static boolean isIllegalEscape(int c) {
		return c >= 0 && c < 32 && c != '\n' && c != '\r' && c != '\t';
	}

	/**
	 * escape for XML 1.0
	 *
	 * @param text      The string
	 * @param attribute true if the escaped value is inside an attribute
	 * @return The escaped string
	 */
	private static String escapeXml(String text, boolean attribute) {
		StringBuilder sb = new StringBuilder(text.length() * 2);
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (isIllegalEscape(c)) {
				// uh-oh! This character is illegal in XML 1.0!
				// http://www.w3.org/TR/1998/REC-xml-19980210#charsets
				// we're going to deliberately doubly-XML escape it...
				// there's nothing better we can do! :-(
				// SUREFIRE-456
				sb.append(attribute ? "&#" : "&amp#").append((int) c).append(';'); // & Will be encoded to amp inside
																					// xml encodingSHO
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	private static final class ByteConstantsHolder {
		private static final byte[] CDATA_START_BYTES;

		private static final byte[] CDATA_END_BYTES;

		private static final byte[] CDATA_ESCAPE_STRING_BYTES;

		private static final byte[] AMP_BYTES;

		static {
			CDATA_START_BYTES = "<![CDATA[".getBytes(UTF_8);
			CDATA_END_BYTES = "]]>".getBytes(UTF_8);
			CDATA_ESCAPE_STRING_BYTES = "]]><![CDATA[>".getBytes(UTF_8);
			AMP_BYTES = "&amp#".getBytes(UTF_8);
		}
	}
}
