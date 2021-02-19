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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class SystemProperties {
	private static final Logger log = LoggerFactory.getLogger(SystemProperties.class);
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
		String propVal = System.getProperty(property);
		if (propVal != null && !propVal.isEmpty()) {
			log.info("Found property -D" + property + "=" + propVal);
			return propVal;
		}
		propVal = System.getenv(property);
		if (propVal != null && !propVal.isEmpty()) {
			log.info("Found environment variable " + property + "=" + propVal);
			return propVal;
		}
		log.trace("Unable to find " + property + ", using default: " + defaultVal);
		return getUserDefault(property, defaultVal);
	}

	private static String getUserDefault(String property, String defaultVal){
		if(userDefaults == null){
			loadUserDefaults(USER_DEFAULTS_FILE);
		}
		String val = userDefaults.getProperty(property, null);
		if(val != null){
			log.info("Using user.default for property: [" + property +"], value: [" + val + "]");
			return val;
		}
		return defaultVal;
	}

	private static void loadUserDefaults(String filePath){
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(filePath));
		}catch(Exception ex){ }
		userDefaults = props;
	}
}
