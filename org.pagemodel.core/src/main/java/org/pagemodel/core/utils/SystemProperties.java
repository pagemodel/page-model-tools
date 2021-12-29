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

package org.pagemodel.core.utils;

import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.json.OutputFilter;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Properties;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class SystemProperties {
	private static TestEvaluator evalLogger = new TestEvaluator.Now();
	public final static String USER_DEFAULTS_FILE = "../user.defaults";
	private static Properties userDefaults;

	/***
	 * Attempts to read system property, if not found attempts to read environment variable.
	 * If neither the system property or environment variable are available defaultVal is returned
	 * @param property
	 * @param defaultVal
	 * @return system property, environment variable, or defaultVal
	 */
	public static String readSystemProperty(String property, String defaultVal) {
		return readProperty(property, defaultVal, false);
	}

	public static String readSecret(String property, String defaultVal) {
		return readProperty(property, defaultVal, true);
	}

	private static String readProperty(String property, String defaultVal, boolean secret) {
		String propVal = System.getProperty(property);
		if (propVal != null && !propVal.isEmpty()) {
			return logProperty(property, propVal, "system property", secret);
		}
		propVal = System.getenv(property);
		if (propVal != null && !propVal.isEmpty() && !propVal.equals("null")) {
			return logProperty(property, propVal, "environment variable", secret);
		}
		if(userDefaults == null){
			loadUserDefaults(USER_DEFAULTS_FILE);
		}
		String val = userDefaults.getProperty(property, null);
		if(val != null){
			return logProperty(property, val, "user.defaults", secret);
		}
		return logProperty(property, defaultVal, "default value (not found)", secret);
	}

	private static String logProperty(String property, String value, String source, boolean secret){
		if(secret) {
			OutputFilter.addMaskedString(value);
		}
		evalLogger.logEvent(TestEvaluator.TEST_LOAD, "property", op -> op
				.addValue("property", property)
				.addValue("value", value)
				.doAdd(o -> {
					if (secret){
						o.addValue("hash", sha256(value).substring(0,6));
					}
				}).addValue("source", source));
		return value;
	}

	private static void loadUserDefaults(String filePath){
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(filePath));
		}catch(Exception ex){ }
		userDefaults = props;
	}

	private static String sha256(String text){
		if(text == null){
			return "null";
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hash);
		}catch(Exception ex){
			return "null";
		}
	}
}
