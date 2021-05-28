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

package org.pagemodel.tools.accessibility;

import com.deque.axe.AXE;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.WebTestContext;
import org.pagemodel.web.utils.Screenshot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class AXEScanner {
	protected static final URL SCRIPT_URL = AXEScanner.class.getClassLoader().getResource("axe.min.js");

	public final static int DEFAULT_TIMEOUT = 30;

	public final static String LOG_DIR = "build/accessibility/";

	private static boolean SCREENSHOT_FLAG = true;

	private List<String> expectedViolations = Collections.EMPTY_LIST;

	public void setExpectedViolations(String... expectedViolations) {
		this.expectedViolations = Arrays.asList(expectedViolations);
	}

	public void clearExpectedViolations() {
		setExpectedViolations();
	}

	public static void setScreenshotFlag(boolean val) {
		SCREENSHOT_FLAG = val;
	}

	public static boolean getScreenshotFlag() {
		return SCREENSHOT_FLAG;
	}

	public <T> boolean analyzeAccessibility(WebTestContext testContext, String testName) {
		return analyzeAccessibility(testContext, testName, LOG_DIR, DEFAULT_TIMEOUT);
	}

	public <T> boolean analyzeAccessibility(WebTestContext testContext, String testName, int timeoutSec) {
		return analyzeAccessibility(testContext, testName, LOG_DIR, timeoutSec);
	}

	public <T> boolean analyzeAccessibility(WebTestContext testContext, String testName, String logDir, int timeoutSec) {
		File destFolder = new File(logDir);
		if (!destFolder.exists()) {
			destFolder.mkdirs();
		}
		String outFileName = convertTestNameToFileName(testName);
		if (getScreenshotFlag()) {
			Screenshot.takeScreenshot(testContext, outFileName);
		}
		testContext.getDriver().manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		JSONObject responseJSON = new AXE.Builder(testContext.getDriver(), SCRIPT_URL)
				.setTimeout(timeoutSec)
				.analyze();
		JSONArray violation = responseJSON.getJSONArray("violations");
		List<String> foundExpectedViolations = new ArrayList<>();
		List<String> foundViolations = new ArrayList<>();
		if (violation.length() != 0) {
			boolean passScan = true;
			for (int i = 0; i < violation.length(); i++) {
				String violationName = violation.getJSONObject(i).getString("help");
				if (!expectedViolations.contains(violationName)) {
					passScan = false;
					foundViolations.add(violationName);
				} else {
					foundExpectedViolations.add(violationName);
				}
			}
			TestEvaluator eval = testContext.getEvaluator();
			if(!foundExpectedViolations.isEmpty()){
				eval.logEvent(TestEvaluator.TEST_ASSERT,
						"ignore expected violations", obj -> obj
							.addValue("value", String.join(", ",foundExpectedViolations)));
			}
			if(!foundViolations.isEmpty()){
				eval.logEvent(TestEvaluator.TEST_ERROR,
						"accessibility violations", obj -> obj
							.addValue("value", String.join(", ",foundViolations)));
			}
			if (!passScan) {
				AXE.writeResults(logDir + "/" + outFileName + "_" + System.currentTimeMillis(), violation);
				return false;
			}
		}
		return true;
	}

	private String convertTestNameToFileName(String testName) {
		return testName.replaceAll("[^a-zA-Z0-9]+", "");
	}

	public static String getAXEVersion() {
		BufferedReader reader = null;
		try {
			URLConnection connection = SCRIPT_URL.openConnection();
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.matches(".*aXe v([0-9]\\.?)+.*")) {
					return line.substring(line.indexOf("aXe v")).trim();
				}
			}
		} catch (Exception e) {
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignored) {
				}
			}
		}
		return "unknown";
	}
}
