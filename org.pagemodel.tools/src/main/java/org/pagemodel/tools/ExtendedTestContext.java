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

package org.pagemodel.tools;

import org.openqa.selenium.WebDriver;
import org.pagemodel.mail.MailTestContext;
import org.pagemodel.ssh.SSHAuthenticator;
import org.pagemodel.ssh.SSHTestContext;
import org.pagemodel.web.DefaultWebTestContext;
import org.pagemodel.web.utils.PageException;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class ExtendedTestContext extends DefaultWebTestContext implements SSHTestContext, MailTestContext {
	public static String DEFAULT_BROWSER = "headless";

	private String browser = DEFAULT_BROWSER;
	protected SSHAuthenticator sshAuthenticator;

	public ExtendedTestContext(WebDriver driver, SSHAuthenticator sshAuthenticator, String browser) {
		super(driver);
		this.sshAuthenticator = sshAuthenticator;
		if(browser != null && !browser.isEmpty()){
			setBrowser(browser);
		}
	}

	public String getBrowser() {
		return browser;
	}

	protected void setBrowser(String browser) {
		this.browser = browser;
	}

	protected void openPage(String url) {
		if (getDriver() == null) {
			setDriver(WebDriverFactory.open(getBrowser(), url));
		}else{
			getDriver().get(url);
		}
	}

	@Override
	public SSHAuthenticator getSshAuthenticator() {
		return sshAuthenticator;
	}

	@Override
	public void setSshAuthenticator(SSHAuthenticator sshAuthenticator) {
		this.sshAuthenticator = sshAuthenticator;
	}

	@Override
	public PageException createException(String message, Throwable cause) {
		return createException(true, message, cause);
	}
}
