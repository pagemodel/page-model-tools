package org.pagemodel.core.utils;

import org.pagemodel.core.TestContext;

public class TestRuntimeException extends RuntimeException{
	static boolean LOG_ON_ERROR = true;
	protected TestContext testContext;
	protected boolean logOnError;

	public TestRuntimeException() {

	}

	public TestRuntimeException(TestContext testContext, boolean logError) {
		captureExceptionDetails(testContext, logError);
	}

	public TestRuntimeException(TestContext testContext) {
		this(testContext, LOG_ON_ERROR);
	}

	public TestRuntimeException(TestContext testContext, boolean logError, String message) {
		super(message);
		captureExceptionDetails(testContext, logError);
	}

	public TestRuntimeException(TestContext testContext, String message) {
		this(testContext, LOG_ON_ERROR, message);
	}

	public TestRuntimeException(TestContext testContext, boolean logError, Throwable cause) {
		super(cause);
		captureExceptionDetails(testContext, logError);
	}

	public TestRuntimeException(TestContext testContext, Throwable cause) {
		this(testContext, LOG_ON_ERROR, cause);
	}

	public TestRuntimeException(TestContext testContext, boolean logError, String message, Throwable cause) {
		super(message, cause);
		captureExceptionDetails(testContext, logError);
	}

	public TestRuntimeException(TestContext testContext, String message, Throwable cause) {
		this(testContext, LOG_ON_ERROR, message, cause);
	}

	public TestRuntimeException(TestContext testContext, boolean logError, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		captureExceptionDetails(testContext, logError);
	}

	public TestRuntimeException(TestContext testContext, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		this(testContext, LOG_ON_ERROR, message, cause, enableSuppression, writableStackTrace);
	}

	protected void captureExceptionDetails(TestContext testContext, boolean logException) {
		this.testContext = testContext;
		this.logOnError = logException;
		if(logOnError()){
			if(testContext != null) {
				testContext.getEvaluator().logException(this);
			}
		}
	}

	protected boolean logOnError() {
		return logOnError;
	}
}
