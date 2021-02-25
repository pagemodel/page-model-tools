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

package org.pagemodel.test;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.pagemodel.mail.MailAuthenticator;
import org.pagemodel.mail.PopServer;
import org.pagemodel.mail.SmtpServer;
import org.pagemodel.tests.myapp.tools.MyAppConfig;
import org.pagemodel.tests.myapp.tools.MyAppTestContext;
import org.pagemodel.tools.ProfileMap;
import org.pagemodel.tools.WebDriverConfig;

import static org.pagemodel.core.utils.SystemProperties.readSystemProperty;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class MyAppTestBase {
	protected static boolean configLoaded = false;
	private static ProfileMap<MyAppConfig> myAppProfiles;
	private static ProfileMap<MailAuthenticator> mailProfiles;
	private static ProfileMap<WebDriverConfig> driverProfiles;

	protected static WebDriverConfig webDriverConfig;
	protected static MailAuthenticator externalMailAuth;
	protected static MyAppConfig myAppConfig;

	protected static PopServer externalPop;
	protected static SmtpServer externalSmtp;

	protected MyAppTestContext context;

	@BeforeClass
	public static synchronized void loadConfig() {
		if (!configLoaded) {
			myAppProfiles = ProfileMap.loadFile(MyAppConfig.class, "src/test/resources/profiles.myapp.json");
			mailProfiles = ProfileMap.loadFile(MailAuthenticator.class, "src/test/resources/profiles.mail.json");
			driverProfiles = ProfileMap.loadFile(WebDriverConfig.class, "src/test/resources/profiles.driver.json");

			webDriverConfig = driverProfiles.getProfile(readSystemProperty("driver", "chrome"));
			myAppConfig = myAppProfiles.getProfile(readSystemProperty("myapp", "dev"));
			externalMailAuth = mailProfiles.getProfile(readSystemProperty("mail.external", "dev"));

			externalPop = new PopServer(externalMailAuth);
			externalSmtp = new SmtpServer(externalMailAuth);
			configLoaded = true;
		}
	}

	@Before
	public void createTestContext() {
		context = new MyAppTestContext(myAppConfig, webDriverConfig);
	}

	@After
	public void closeBrowsers() {
		context.quit();
	}
}
