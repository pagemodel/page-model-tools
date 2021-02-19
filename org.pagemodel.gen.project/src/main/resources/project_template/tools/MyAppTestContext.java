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

package org.pagemodel.tests.myapp.tools;

import org.pagemodel.tests.myapp.pages.LoginPage;
import org.pagemodel.tools.ExtendedTestContext;
import java.io.File;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class MyAppTestContext extends ExtendedTestContext {
	private MyAppConfig myAppConfig;

	public MyAppTestContext(MyAppConfig myAppConfig, String browser) {
		super(null, null, browser);
		this.myAppConfig = myAppConfig;
	}

	public LoginPage getLoginPage() {
		openPage(getApplicationUrl("/login.html"));
		return new LoginPage(this);
	}

	public MyAppConfig getMyAppConfig() {
		return myAppConfig;
	}

	private String getApplicationUrl(String urlPath){
		String port = myAppConfig.getPort().isEmpty() ? "" : ":" + myAppConfig.getPort();
		String hostPath = myAppConfig.getProtocol().equals("file") ? new File(myAppConfig.getHostname()).getAbsolutePath() : myAppConfig.getHostname();
		return String.format("%s://%s%s%s", myAppConfig.getProtocol(), hostPath, port, urlPath);
	}
}
