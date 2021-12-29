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
public class PageException extends TestRuntimeException {
	private String screenshotPath;

	public PageException(TestContext testContext, boolean takeScreenshot) {
		super(testContext, takeScreenshot);
	}

	public PageException(TestContext testContext) {
		super(testContext);
	}

	public PageException(TestContext testContext, boolean takeScreenshot, String message) {
		super(testContext, takeScreenshot, message);
	}

	public PageException(TestContext testContext, String message) {
		super(testContext, message);
	}

	public PageException(TestContext testContext, boolean takeScreenshot, Throwable cause) {
		super(testContext, takeScreenshot, cause);
	}

	public PageException(TestContext testContext, Throwable cause) {
		super(testContext, cause);
	}

	public PageException(TestContext testContext, boolean takeScreenshot, String message, Throwable cause) {
		super(testContext, takeScreenshot, message, cause);
	}

	public PageException(TestContext testContext, String message, Throwable cause) {
		super(testContext, message, cause);
	}

	public PageException(TestContext testContext, boolean takeScreenshot, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(testContext, takeScreenshot, message, cause, enableSuppression, writableStackTrace);
	}

	public PageException(TestContext testContext, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(testContext, message, cause, enableSuppression, writableStackTrace);
	}

	protected void captureExceptionDetails(TestContext testContext, boolean logException) {
		super.captureExceptionDetails(testContext, logException);
		if(logOnError()){
			takeScreenshot();
		}
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
