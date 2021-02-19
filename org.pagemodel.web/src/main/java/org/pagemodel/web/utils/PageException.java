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

import org.pagemodel.web.WebTestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class PageException extends RuntimeException {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	static boolean TAKE_SCREENSHOT_ON_ERROR = true;
	private WebTestContext testContext;
	private String screenshotPath;
	private boolean screenshotFlag;

	public PageException(WebTestContext testContext, boolean takeScreenshot) {
		captureExceptionDetails(testContext, takeScreenshot);
	}

	public PageException(WebTestContext testContext) {
		this(testContext, TAKE_SCREENSHOT_ON_ERROR);
	}

	public PageException(WebTestContext testContext, boolean takeScreenshot, String message) {
		super(message);
		captureExceptionDetails(testContext, takeScreenshot);
	}

	public PageException(WebTestContext testContext, String message) {
		this(testContext, TAKE_SCREENSHOT_ON_ERROR, message);
	}

	public PageException(WebTestContext testContext, boolean takeScreenshot, Throwable cause) {
		super(cause);
		captureExceptionDetails(testContext, takeScreenshot);
	}

	public PageException(WebTestContext testContext, Throwable cause) {
		this(testContext, TAKE_SCREENSHOT_ON_ERROR, cause);
	}

	public PageException(WebTestContext testContext, boolean takeScreenshot, String message, Throwable cause) {
		super(message, cause);
		captureExceptionDetails(testContext, takeScreenshot);
	}

	public PageException(WebTestContext testContext, String message, Throwable cause) {
		this(testContext, TAKE_SCREENSHOT_ON_ERROR, message, cause);
	}

	public PageException(WebTestContext testContext, boolean takeScreenshot, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		captureExceptionDetails(testContext, takeScreenshot);
	}

	public PageException(WebTestContext testContext, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		this(testContext, TAKE_SCREENSHOT_ON_ERROR, message, cause, enableSuppression, writableStackTrace);
	}

	protected void captureExceptionDetails(WebTestContext testContext, boolean screenshotFlag) {
		this.testContext = testContext;
		this.screenshotFlag = screenshotFlag;
		if(screenshotFlag){
			log.error("Test Failure: " + getMessage(), this);
		}
		takeScreenshot();
	}

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

	protected boolean takeScreenshotOnError() {
		return true;
	}

	private void takeScreenshot() {
		if (!screenshotFlag) {
			return;
		}
		if (testContext == null || testContext.getDriver() == null) {
			return;
		}
		screenshotPath = Screenshot.takeScreenshot(testContext, "TestFailure");
	}
}
