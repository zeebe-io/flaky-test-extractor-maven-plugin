package io.zeebe.flakytestextractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Based on {@code org.apache.maven.plugins.surefire.report.ReportTestSuite} (licensed under
 * http://www.apache.org/licenses/LICENSE-2.0).
 *
 * <p>This implementation uses {@code ExtendedReportTestCase} instead of {@code ReportTestCase} to
 * capture more information on flaky test runs
 */
public class ExtendedReportTestSuite {
  private final List<ExtendedReportTestCase> testCases = new ArrayList<>();

  private int numberOfErrors;

  private int numberOfFailures;

  private int numberOfSkipped;

  private int numberOfFlakes;

  private Integer numberOfTests;

  private String name;

  private String fullClassName;

  private String packageName;

  private float timeElapsed;

  public List<ExtendedReportTestCase> getTestCases() {
    return testCases;
  }

  public int getNumberOfErrors() {
    return numberOfErrors;
  }

  public ExtendedReportTestSuite setNumberOfErrors(int numberOfErrors) {
    this.numberOfErrors = numberOfErrors;
    return this;
  }

  public ExtendedReportTestSuite incrementNumberOfErrors() {
    ++numberOfErrors;
    return this;
  }

  public int getNumberOfFailures() {
    return numberOfFailures;
  }

  public ExtendedReportTestSuite setNumberOfFailures(int numberOfFailures) {
    this.numberOfFailures = numberOfFailures;
    return this;
  }

  public ExtendedReportTestSuite incrementNumberOfFailures() {
    ++numberOfFailures;
    return this;
  }

  public int getNumberOfSkipped() {
    return numberOfSkipped;
  }

  public ExtendedReportTestSuite setNumberOfSkipped(int numberOfSkipped) {
    this.numberOfSkipped = numberOfSkipped;
    return this;
  }

  public ExtendedReportTestSuite incrementNumberOfSkipped() {
    ++numberOfSkipped;
    return this;
  }

  public int getNumberOfFlakes() {
    return numberOfFlakes;
  }

  public ExtendedReportTestSuite setNumberOfFlakes(int numberOfFlakes) {
    this.numberOfFlakes = numberOfFlakes;
    return this;
  }

  public ExtendedReportTestSuite incrementNumberOfFlakes() {
    ++numberOfFlakes;
    return this;
  }

  public int getNumberOfTests() {
    return numberOfTests == null ? testCases.size() : numberOfTests;
  }

  public ExtendedReportTestSuite setNumberOfTests(int numberOfTests) {
    this.numberOfTests = numberOfTests;
    return this;
  }

  public String getName() {
    return name;
  }

  public ExtendedReportTestSuite setName(String name) {
    this.name = name;
    return this;
  }

  public String getFullClassName() {
    return fullClassName;
  }

  public ExtendedReportTestSuite setFullClassName(String fullClassName) {
    this.fullClassName = fullClassName;
    if (fullClassName != null) {
      int lastDotPosition = fullClassName.lastIndexOf(".");
      if (name == null) {
        name = fullClassName.substring(lastDotPosition + 1, fullClassName.length());
      }
      packageName = lastDotPosition == -1 ? "" : fullClassName.substring(0, lastDotPosition);
    }
    return this;
  }

  public String getPackageName() {
    return packageName;
  }

  public ExtendedReportTestSuite setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  public float getTimeElapsed() {
    return this.timeElapsed;
  }

  public ExtendedReportTestSuite setTimeElapsed(float timeElapsed) {
    this.timeElapsed = timeElapsed;
    return this;
  }

  ExtendedReportTestSuite setTestCases(List<ExtendedReportTestCase> testCases) {
    this.testCases.clear();
    this.testCases.addAll(testCases);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return fullClassName
        + " ["
        + getNumberOfTests()
        + "/"
        + getNumberOfFailures()
        + "/"
        + getNumberOfErrors()
        + "/"
        + getNumberOfSkipped()
        + "]";
  }
}
