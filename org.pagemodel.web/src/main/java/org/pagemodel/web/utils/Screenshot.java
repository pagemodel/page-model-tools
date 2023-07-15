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
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.WebTestContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import static org.pagemodel.web.utils.RectangleUtils.*;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class Screenshot {

	private static int SCREENSHOT_NUMBER = 1;
	public static String SCREENSHOT_DEST = "build/screenshots/";

	public static <T> String takeScreenshot(WebTestContext testContext, String filenamePrefix) {
		return takeScreenshot(testContext, filenamePrefix, true);
	}

	public static <T> String takeScreenshot(WebTestContext testContext, String filenamePrefix, boolean formatName) {
		TestEvaluator.Now eval = new TestEvaluator.Now();
		if (testContext == null) {
			eval.logMessage("Error: Unable to take screenshot, null TestContext.");
			return null;
		}
		if (testContext.getDriver() == null) {
			eval.logMessage("Error: Unable to take screenshot, null WebDriver in TestContext.");
			return null;
		}
		return takeScreenshot(testContext.getDriver(), filenamePrefix, formatName);
	}

	public static String takeScreenshot(WebDriver driver, String filenamePrefix) {
		return takeScreenshot(driver, filenamePrefix, true);
	}

	public static String takeScreenshot(WebDriver driver, String filenamePrefix, boolean formatName) {
		File destFolder = new File(SCREENSHOT_DEST);
		TestEvaluator.Now eval = new TestEvaluator.Now();
		if (!destFolder.exists()) {
			eval.quiet().testRun(TestEvaluator.TEST_EXECUTE,
					"create directory", op -> op
							.addValue("value", destFolder.getAbsolutePath()),
					() -> destFolder.mkdirs(),
					null, null);
		}
		File screenshot;
		if(formatName){
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
			String date = simpleDateFormat.format(new Date());
			String filename = String.format("%03d_%s_%s.png", SCREENSHOT_NUMBER++, filenamePrefix, date);
			screenshot = new File(destFolder, filename);
		}else{
			screenshot = new File(destFolder, filenamePrefix+".png");
		}

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

	public static String takeScreenshot(WebDriver driver, Rectangle bounds, String filename, int...padding) {
		return takeScreenshot(driver, bounds, filename, false, padding);
	}

	public static String takeScreenshot(WebDriver driver, Rectangle bounds, String filenamePrefix, boolean formatName, int...padding) {
		File destFolder = new File(SCREENSHOT_DEST);
		TestEvaluator.Now eval = new TestEvaluator.Now();
		if (!destFolder.exists()) {
			eval.quiet().testRun(TestEvaluator.TEST_EXECUTE,
					"create directory", op -> op
							.addValue("value", destFolder.getAbsolutePath()),
					() -> destFolder.mkdirs(),
					null, null);
		}

		File screenshot;
		if(formatName){
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
			String date = simpleDateFormat.format(new Date());
			String filename = String.format("%03d_%s_%s.png", SCREENSHOT_NUMBER++, filenamePrefix, date);
			screenshot = new File(destFolder, filename);
		}else{
			screenshot = new File(destFolder, filenamePrefix+".png");
		}

		byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
		try {
			int[] pads = RectangleUtils.getPads(padding);
			BufferedImage fullImg = ImageIO.read(new ByteArrayInputStream(bytes));
			int xmin = Math.min(fullImg.getWidth(), Math.max(0, bounds.getX() - pads[LEFT]));
			int xmax = Math.min(fullImg.getWidth() - xmin, Math.max(0, bounds.getWidth() + pads[LEFT] + pads[RIGHT]));
			int ymin = Math.min(fullImg.getHeight(), Math.max(0, bounds.getY() - pads[TOP]));
			int ymax = Math.min(fullImg.getHeight() - ymin, Math.max(0, bounds.getHeight() + pads[TOP] + pads[BOTTOM]));
			BufferedImage eleScreenshot= fullImg.getSubimage(xmin, ymin, xmax, ymax);

			ImageIO.write(eleScreenshot, "png", screenshot);
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			ImageIO.write(eleScreenshot, "png", outBytes);
			String base64Encoded = Base64.getEncoder().encodeToString(outBytes.toByteArray());
			eval.logEvent(TestEvaluator.TEST_EXECUTE, "save screenshot", obj -> obj
					.addValue("value", "file://" + screenshot.getAbsolutePath())
					.addValue("img-base64", base64Encoded));

		}catch (Exception ex){
			eval.logException(TestEvaluator.TEST_ERROR, "save screenshot", obj -> obj
							.addValue("value", "file://" + screenshot.getAbsolutePath())
					, null, ex);
		}
		return screenshot.getAbsolutePath();
	}

	public static BufferedImage getScreenshot(WebDriver driver, Rectangle bounds, boolean formatName, int...padding) {
		TestEvaluator.Now eval = new TestEvaluator.Now();
		byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
		try {
			BufferedImage fullImg = ImageIO.read(new ByteArrayInputStream(bytes));
			BufferedImage cropped = bounds == null ? fullImg : crop(fullImg, bounds, padding);

			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			ImageIO.write(cropped, "png", outBytes);
			String base64Encoded = Base64.getEncoder().encodeToString(outBytes.toByteArray());
			eval.logEvent(TestEvaluator.TEST_EXECUTE, "get screenshot", obj -> obj
					.addObject("bounds", RectangleUtils.rectangleJson(bounds))
					.addValue("img-base64", base64Encoded));
			return cropped;
		}catch (Exception ex){
			eval.logException(TestEvaluator.TEST_ERROR, "get screenshot", obj -> obj
							.addValue("bounds", RectangleUtils.rectangleJson(bounds))
					, null, ex);
		}
		return null;
	}

	public static BufferedImage crop(BufferedImage fullImg, Rectangle bounds, int...padding) {
		Rectangle padded = RectangleUtils.pad(bounds, padding);
		return fullImg.getSubimage(padded.getX(), padded.getY(), padded.getWidth(), padded.getHeight());
	}

	public static String save(BufferedImage image, String filenamePrefix, boolean formatName) {
		File destFolder = new File(SCREENSHOT_DEST);
		TestEvaluator.Now eval = new TestEvaluator.Now();
		if (!destFolder.exists()) {
			eval.quiet().testRun(TestEvaluator.TEST_EXECUTE,
					"create directory", op -> op
							.addValue("value", destFolder.getAbsolutePath()),
					() -> destFolder.mkdirs(),
					null, null);
		}
		File screenshot;
		if(formatName){
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
			String date = simpleDateFormat.format(new Date());
			String filename = String.format("%03d_%s_%s.png", SCREENSHOT_NUMBER++, filenamePrefix, date);
			screenshot = new File(destFolder, filename);
		}else{
			screenshot = new File(destFolder, filenamePrefix+".png");
		}
		try {
			ImageIO.write(image, "png", screenshot);
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			ImageIO.write(image, "png", outBytes);
			String base64Encoded = Base64.getEncoder().encodeToString(outBytes.toByteArray());
			eval.logEvent(TestEvaluator.TEST_EXECUTE, "save screenshot", obj -> obj
					.addValue("value", "file://" + screenshot.getAbsolutePath())
					.addValue("img-base64", base64Encoded));

		}catch (Exception ex){
			eval.logException(TestEvaluator.TEST_ERROR, "save screenshot", obj -> obj
							.addValue("value", "file://" + screenshot.getAbsolutePath())
					, null, ex);
		}
		return screenshot.getAbsolutePath();
	}
}