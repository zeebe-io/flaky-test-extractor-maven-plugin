package io.zeebe.flakytestextractor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.plugin.logging.Log;

public class TestLogger implements Log {

	private static final Logger logger = Logger.getLogger("com.github.pihme.flakytestextractor");

	@Override
	public boolean isDebugEnabled() {
		return logger.isLoggable(Level.FINE);
	}

	@Override
	public void debug(CharSequence content) {
		logger.log(Level.FINE, content.toString());
	}

	@Override
	public void debug(CharSequence content, Throwable error) {
		logger.log(Level.FINE, content.toString(), error);
	}

	@Override
	public void debug(Throwable error) {
		logger.log(Level.FINE, error.getMessage(), error);
	}

	@Override
	public boolean isInfoEnabled() {
		return logger.isLoggable(Level.INFO);
	}

	@Override
	public void info(CharSequence content) {
		logger.log(Level.INFO, content.toString());
	}

	@Override
	public void info(CharSequence content, Throwable error) {
		logger.log(Level.INFO, content.toString(), error);
	}

	@Override
	public void info(Throwable error) {
		logger.log(Level.INFO, error.getMessage(), error);
	}

	@Override
	public boolean isWarnEnabled() {
		return logger.isLoggable(Level.WARNING);
	}

	@Override
	public void warn(CharSequence content) {
		logger.log(Level.WARNING, content.toString());
	}

	@Override
	public void warn(CharSequence content, Throwable error) {
		logger.log(Level.WARNING, content.toString(), error);
	}

	@Override
	public void warn(Throwable error) {
		logger.log(Level.WARNING, error.getMessage(), error);
	}

	@Override
	public boolean isErrorEnabled() {
		return logger.isLoggable(Level.SEVERE);
	}

	@Override
	public void error(CharSequence content) {
		logger.log(Level.SEVERE, content.toString());
	}

	@Override
	public void error(CharSequence content, Throwable error) {
		logger.log(Level.SEVERE, content.toString(), error);
	}

	@Override
	public void error(Throwable error) {
		logger.log(Level.SEVERE, error.getMessage(), error);
	}

}
