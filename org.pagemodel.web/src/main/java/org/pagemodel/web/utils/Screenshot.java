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

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.WebTestContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class Screenshot {

	private static int SCREENSHOT_NUMBER = 1;
	public static String SCREENSHOT_DEST = "build/screenshots/";

	public static <T> String takeScreenshot(WebTestContext testContext, String filenamePrefix) {
		TestEvaluator.Now eval = new TestEvaluator.Now();
		if (testContext == null) {
			eval.logMessage("Error: Unable to take screenshot, null TestContext.");
			return null;
		}
		if (testContext.getDriver() == null) {
			eval.logMessage("Error: Unable to take screenshot, null WebDriver in TestContext.");
			return null;
		}
		return takeScreenshot(testContext.getDriver(), filenamePrefix);
	}

	public static String takeScreenshot(WebDriver driver, String filenamePrefix) {
		File destFolder = new File(SCREENSHOT_DEST);
		TestEvaluator.Now eval = new TestEvaluator.Now();
		if (!destFolder.exists()) {
			eval.quiet().testRun(TestEvaluator.TEST_EXECUTE,
					"create directory", op -> op
							.addValue("value", destFolder.getAbsolutePath()),
					() -> destFolder.mkdirs(),
					null, null);
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
		String date = simpleDateFormat.format(new Date());
		String filename = String.format("%03d_%s_%s.png", SCREENSHOT_NUMBER++, filenamePrefix, date);
		File screenshot = new File(destFolder, filename);

		byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
		String base64Encoded = Base64.getEncoder().encodeToString(bytes);
		eval.logEvent(TestEvaluator.TEST_EXECUTE, "save screenshot", obj -> obj
				.addValue("value", "file://" + screenshot.getAbsolutePath())
				.addValue("img-base64", base64Encoded));
		try (FileOutputStream fos = new FileOutputStream(screenshot)) {
			fos.write(bytes);
		} catch (IOException ex) {
			eval.logException(TestEvaluator.TEST_ERROR, "save screenshot", obj -> obj
					.addValue("value", "file://" + screenshot.getAbsolutePath())
					, null, ex);
		}
		return screenshot.getAbsolutePath();
	}
}
