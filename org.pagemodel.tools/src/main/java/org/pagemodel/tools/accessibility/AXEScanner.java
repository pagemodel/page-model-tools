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
import org.pagemodel.web.WebTestContext;
import org.pagemodel.web.utils.Screenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class AXEScanner {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected static final URL SCRIPT_URL = AXEScanner.class.getResource("/axe.min.js");

	public final static String LOG_DIR = "results/accessibility/current";
	public final static String ACCESSIBILITY_RUN_LOG = "accessibilityRun.log";
	public final static String ACCESSIBILITY_SUCCESS_LOG = "accessibilitySuccess.log";
	public final static String ACCESSIBILITY_FAILURE_LOG = "accessibilityFailure.log";

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
		return analyzeAccessibility(testContext, testName, LOG_DIR, ACCESSIBILITY_RUN_LOG, ACCESSIBILITY_SUCCESS_LOG, ACCESSIBILITY_FAILURE_LOG);
	}

	public <T> boolean analyzeAccessibility(WebTestContext testContext, String testName, String logDir, String accessibilityLogName, String accessibilitySuccessName, String accessibilityFailureName) {
		File destFolder = new File(logDir);
		if (!destFolder.exists()) {
			destFolder.mkdirs();
		}
		String outFileName = convertTestNameToFileName(testName);
		try {
			logAccessibilityRun(testName, logDir, accessibilityLogName);
			if (getScreenshotFlag()) {
				Screenshot.takeScreenshot(testContext, outFileName);
			}
			testContext.getDriver().manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
			JSONObject responseJSON = new AXE.Builder(testContext.getDriver(), SCRIPT_URL).analyze();
//            page.getDriver().manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
			JSONArray violation = responseJSON.getJSONArray("violations");
			if (violation.length() != 0) {
				boolean passScan = true;
				for (int i = 0; i < violation.length(); i++) {
					String violationName = violation.getJSONObject(i).getString("help");
					if (!expectedViolations.contains(violationName)) {
						passScan = false;
						log.error("Found unexpected violation: " + violationName);
					} else {
						log.info("Ignoring expected violation: " + violationName);
					}
				}
				if (!passScan) {
					logAccessibilityRun(testName, logDir, accessibilityFailureName);
					AXE.writeResults(logDir + "/" + outFileName + "_" + System.currentTimeMillis(), violation);
					return false;
				}
			}
			logAccessibilityRun(testName, logDir, accessibilitySuccessName);
			return true;
		} catch (IOException ex) {
			log.error("Error writing to accessibility log.", ex);
			return false;
		} finally {
		}
	}

	private <T> void logAccessibilityRun(String testName, String destDir, String accessibilityLogPath) throws IOException {
		File accessibilityLogFile = new File(destDir + "/" + accessibilityLogPath);
		try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(accessibilityLogFile, true))) {
			logWriter.append(testName);
			logWriter.newLine();
			logWriter.flush();
		}
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
