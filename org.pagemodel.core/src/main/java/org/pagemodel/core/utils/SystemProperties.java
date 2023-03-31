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
/**
 * This class provides utility methods for reading system properties, environment variables, and user defaults.
 */
public class SystemProperties {

	/**
	 * A TestEvaluator instance used for logging events related to property loading.
	 */
	private static TestEvaluator evalLogger = new TestEvaluator.Now();

	/**
	 * The path to the user defaults file.
	 */
	public final static String USER_DEFAULTS_FILE = "../user.defaults";

	/**
	 * A Properties object containing the user defaults loaded from the USER_DEFAULTS_FILE.
	 */
	private static Properties userDefaults;

	/**
	 * Attempts to read a system property with the given name. If the property is not found, attempts to read an environment variable with the same name.
	 * If neither the system property nor the environment variable are available, returns the default value.
	 * @param property The name of the system property or environment variable to read.
	 * @param defaultVal The default value to return if the property is not found.
	 * @return The value of the system property, environment variable, or default value.
	 */
	public static String readSystemProperty(String property, String defaultVal) {
		return readProperty(property, defaultVal, false);
	}

	/**
	 * Attempts to read a secret property with the given name.
	 * Secret properties are masked during logging.  Passwords and API keys should be loaded with readSecret.
	 * Secrets are masked with a sha256 fragment.  This can be used to verify the correct value is loaded.
	 * If the property is not found, attempts to read an environment variable with the same name.
	 * If neither the system property nor the environment variable are available, returns the default value.
	 * @param property The name of the system property or environment variable to read.
	 * @param defaultVal The default value to return if the property is not found.
	 * @return The value of the system property, environment variable, or default value.
	 */
	public static String readSecret(String property, String defaultVal) {
		return readProperty(property, defaultVal, true);
	}

	/**
	 * Attempts to read a property with the given name. If the property is not found, attempts to read an environment variable with the same name.
	 * If neither the system property nor the environment variable are available, returns the default value.
	 * @param property The name of the system property or environment variable to read.
	 * @param defaultVal The default value to return if the property is not found.
	 * @param secret Whether the property is a secret or not. If true, the property value will be masked in the logs.
	 * @return The value of the system property, environment variable, or default value.
	 */
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

	/**
	 * Logs the property value and source, and optionally masks the value if it is a secret.
	 * @param property The name of the property.
	 * @param value The value of the property.
	 * @param source The source of the property (system property, environment variable, user defaults, or default value).
	 * @param secret Whether the property is a secret or not. If true, the property value will be masked in the logs.
	 * @return The value of the property.
	 */
	private static String logProperty(String property, String value, String source, boolean secret){
		if(secret) {
			OutputFilter.addMaskedString(value);
		}
		evalLogger.logEvent(TestEvaluator.TEST_LOAD, "property", op -> op
				.addValue("property", property)
				.addValue("value", value)
				.doAdd(o -> {
					if (secret){
						o.addValue("hash", sha256Substring(value));
					}
				}).addValue("source", source));
		return value;
	}

	/**
	 * Loads the user defaults from the USER_DEFAULTS_FILE into the userDefaults Properties object.
	 * @param filePath The path to the user defaults file.
	 */
	private static void loadUserDefaults(String filePath){
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(filePath));
		}catch(Exception ex){ }
		userDefaults = props;
	}

	/**
	 * Computes the SHA-256 hash of the given string and returns the first 6 characters of the Base64-encoded hash.
	 * @param text The string to hash.
	 * @return The first 6 characters of the Base64-encoded SHA-256 hash of the string.
	 */
	private static String sha256Substring(String text){
		if(text == null){
			return "null";
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hash).substring(0,6);
		}catch(Exception ex){
			return "null";
		}
	}
}