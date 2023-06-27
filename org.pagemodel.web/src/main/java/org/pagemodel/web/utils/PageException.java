/*
 * Copyright 2021 Matthew Stevenson <pagemodel.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pagemodel.web.utils;

import org.pagemodel.core.TestContext;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.TestRuntimeException;
import org.pagemodel.web.PageUtils;
import org.pagemodel.web.WebTestContext;

import java.io.File;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * This exception is thrown when an error occurs during a page test, navigation, or interaction.
 * It will log the causing exception, page source, and page screenshot.
 */
public class PageException extends TestRuntimeException {

	/**
	 * The path to the screenshot taken when the exception occurred.
	 */
	private String screenshotPath;

	/**
	 * Constructs a new PageException with the given TestContext and takeScreenshot flag.
	 * @param testContext The TestContext associated with the exception.
	 * @param takeScreenshot A flag indicating whether or not to take a screenshot when the exception occurs.
	 */
	public PageException(TestContext testContext, boolean takeScreenshot) {
		super(testContext, takeScreenshot);
	}

	/**
	 * Constructs a new PageException with the given TestContext.
	 * @param testContext The TestContext associated with the exception.
	 */
	public PageException(TestContext testContext) {
		super(testContext);
	}

	/**
	 * Constructs a new PageException with the given TestContext, takeScreenshot flag, and message.
	 * @param testContext The TestContext associated with the exception.
	 * @param takeScreenshot A flag indicating whether or not to take a screenshot when the exception occurs.
	 * @param message The message associated with the exception.
	 */
	public PageException(TestContext testContext, boolean takeScreenshot, String message) {
		super(testContext, takeScreenshot, message);
	}

	/**
	 * Constructs a new PageException with the given TestContext and message.
	 * @param testContext The TestContext associated with the exception.
	 * @param message The message associated with the exception.
	 */
	public PageException(TestContext testContext, String message) {
		super(testContext, message);
	}

	/**
	 * Constructs a new PageException with the given TestContext, takeScreenshot flag, and cause.
	 * @param testContext The TestContext associated with the exception.
	 * @param takeScreenshot A flag indicating whether or not to take a screenshot when the exception occurs.
	 * @param cause The cause of the exception.
	 */
	public PageException(TestContext testContext, boolean takeScreenshot, Throwable cause) {
		super(testContext, takeScreenshot, cause);
	}

	/**
	 * Constructs a new PageException with the given TestContext and cause.
	 * @param testContext The TestContext associated with the exception.
	 * @param cause The cause of the exception.
	 */
	public PageException(TestContext testContext, Throwable cause) {
		super(testContext, cause);
	}

	/**
	 * Constructs a new PageException with the given TestContext, takeScreenshot flag, message, and cause.
	 * @param testContext The TestContext associated with the exception.
	 * @param takeScreenshot A flag indicating whether or not to take a screenshot when the exception occurs.
	 * @param message The message associated with the exception.
	 * @param cause The cause of the exception.
	 */
	public PageException(TestContext testContext, boolean takeScreenshot, String message, Throwable cause) {
		super(testContext, takeScreenshot, message, cause);
	}

	/**
	 * Constructs a new PageException with the given TestContext, message, and cause.
	 * @param testContext The TestContext associated with the exception.
	 * @param message The message associated with the exception.
	 * @param cause The cause of the exception.
	 */
	public PageException(TestContext testContext, String message, Throwable cause) {
		super(testContext, message, cause);
	}

	/**
	 * Constructs a new PageException with the given TestContext, takeScreenshot flag, message, cause, enableSuppression flag, and writableStackTrace flag.
	 * @param testContext The TestContext associated with the exception.
	 * @param takeScreenshot A flag indicating whether or not to take a screenshot when the exception occurs.
	 * @param message The message associated with the exception.
	 * @param cause The cause of the exception.
	 * @param enableSuppression A flag indicating whether or not to enable suppression.
	 * @param writableStackTrace A flag indicating whether or not to enable writable stack trace.
	 */
	public PageException(TestContext testContext, boolean takeScreenshot, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(testContext, takeScreenshot, message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Constructs a new PageException with the given TestContext, message, cause, enableSuppression flag, and writableStackTrace flag.
	 * @param testContext The TestContext associated with the exception.
	 * @param message The message associated with the exception.
	 * @param cause The cause of the exception.
	 * @param enableSuppression A flag indicating whether or not to enable suppression.
	 * @param writableStackTrace A flag indicating whether or not to enable writable stack trace.
	 */
	public PageException(TestContext testContext, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(testContext, message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Captures the details of the exception, including the page source and screenshot if the logOnError flag is set.
	 * @param testContext The TestContext associated with the exception.
	 * @param logException A flag indicating whether or not to log the exception.
	 */
	protected void captureExceptionDetails(TestContext testContext, boolean logException) {
		super.captureExceptionDetails(testContext, logException);
		if(logOnError()){
			takeScreenshot();
		}
	}

	/**
	 * Removes the screenshot taken when the exception occurred.
	 */
	public void removeScreenshot() {
		if (screenshotPath == null || screenshotPath.isEmpty()) {
			return;
		}
		File screenshot = new File(screenshotPath);
		if (screenshot.exists()) {
			if (screenshot.delete()) {
				screenshotPath = null;
			}
		}
	}

	/**
	 * Takes a screenshot of the page when the exception occurs, if the logOnError flag is set.
	 */
	private void takeScreenshot() {
		if (!logOnError()) {
			return;
		}
		if(testContext == null || !(testContext instanceof WebTestContext)){
			return;
		}
		WebTestContext webContext = (WebTestContext) testContext;
		if (webContext.getDriver() == null) {
			return;
		}
		PageUtils.logPageSource(webContext, TestEvaluator.TEST_ERROR);
	}
}