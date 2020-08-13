package com.github.pihme.flakytestextractor;

import static org.apache.maven.surefire.shared.utils.StringUtils.isNotBlank;

/**
 * Based on {@code org.apache.maven.plugins.surefire.report.ReportTestCase} (licensed under http://www.apache.org/licenses/LICENSE-2.0). 
 * 
 * This implementation adds a flag to indicate whether the failure or error was a flake or not 
 * and a field to capture the system output
 *
 */
public class ExtendedReportTestCase {
	private String fullClassName;

	private String className;

	private String fullName;

	private String name;

	private float time;

	private String failureMessage;

	private String failureType;

	private String failureErrorLine;

	private String failureDetail;
	
	private String systemOut;
	private String systemError;

	private boolean hasFailure;

	private boolean isFlake = false;

	public String getName() {
		return name;
	}

	public ExtendedReportTestCase setName(String name) {
		this.name = name;
		return this;
	}

	public String getFullClassName() {
		return fullClassName;
	}

	public ExtendedReportTestCase setFullClassName(String name) {
		fullClassName = name;
		return this;
	}

	public String getClassName() {
		return className;
	}

	public ExtendedReportTestCase setClassName(String name) {
		className = name;
		return this;
	}

	public float getTime() {
		return time;
	}

	public ExtendedReportTestCase setTime(float time) {
		this.time = time;
		return this;
	}

	public String getFullName() {
		return fullName;
	}

	public ExtendedReportTestCase setFullName(String fullName) {
		this.fullName = fullName;
		return this;
	}

	public String getFailureMessage() {
		return failureMessage;
	}

	public ExtendedReportTestCase setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
		this.hasFailure = true;
		return this;
	}

	public String getFailureType() {
		return failureType;
	}

	public String getFailureErrorLine() {
		return failureErrorLine;
	}

	public ExtendedReportTestCase setFailureErrorLine(String failureErrorLine) {
		this.failureErrorLine = failureErrorLine;
		this.hasFailure = true;
		return this;
	}

	public String getFailureDetail() {
		return failureDetail;
	}

	public ExtendedReportTestCase setFailureDetail(String failureDetail) {
		this.failureDetail = failureDetail;
		this.hasFailure = true;
		return this;
	}
	
	public String getSystemOut() {
		return systemOut;
	}
	
	public ExtendedReportTestCase setSystemOut(String systemOut) {
		this.systemOut = systemOut;
		return this;
	}
	
	public String getSystemError() { 
		return systemError;
	}
	
	public ExtendedReportTestCase setSystemError(String systemError) {
		this.systemError = systemError;
		return this;
	}
	
	public ExtendedReportTestCase setSkipped(String message) {
		hasFailure = false;
		isFlake = false;
		return setFailureMessage(message).setFailureType("skipped");
	}
	
	public ExtendedReportTestCase setFlake() {
		this.isFlake = true;
		return this;
	}

	public boolean isSuccessful() {
		return !hasFailure() && !isFlake();
	}

	public boolean hasFailure() {
		return hasFailure;
	}

	public boolean isFlake() {
		return isFlake;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return fullName;
	}

	public ExtendedReportTestCase setFailureType(String failureType) {
		this.failureType = failureType;
		return this;
	}
}
