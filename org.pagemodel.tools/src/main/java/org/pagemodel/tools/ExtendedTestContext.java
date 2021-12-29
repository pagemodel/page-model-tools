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
import org.pagemodel.core.testers.ComparableTester;
import org.pagemodel.core.testers.StringTester;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.testers.TesterHelper;
import org.pagemodel.core.utils.TestRuntimeException;
import org.pagemodel.core.utils.json.JsonObjectBuilder;
import org.pagemodel.mail.MailTestContext;
import org.pagemodel.ssh.SSHAuthenticator;
import org.pagemodel.ssh.SSHTestContext;
import org.pagemodel.web.DefaultWebTestContext;
import org.pagemodel.web.utils.PageException;

import java.util.function.Consumer;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class ExtendedTestContext extends DefaultWebTestContext implements SSHTestContext, MailTestContext {
	public static String DEFAULT_BROWSER = "headless";

	private WebDriverConfig webDriverConfig;
	protected SSHAuthenticator sshAuthenticator;

	public ExtendedTestContext(WebDriver driver, SSHAuthenticator sshAuthenticator, WebDriverConfig webDriverConfig) {
		super(driver);
		this.sshAuthenticator = sshAuthenticator;
		if(webDriverConfig == null){
			webDriverConfig = WebDriverConfig.local(DEFAULT_BROWSER);
		}
		this.webDriverConfig = webDriverConfig;
	}

	private void openBrowser(String url){
		if (getDriver() != null) {
			quit();
		}
		setDriver(WebDriverFactory.create(webDriverConfig, url));
	}

	protected void openPage(String url){
		openBrowser(url);
	}

	protected void openPageRetry(String url, int retries) {
		for( ; retries > 0; retries--) {
			try {
				openBrowser(url);
				return;
			} catch (Throwable t) {
				if (retries <= 0) {
					throw t;
				}
				try{
					Thread.sleep(500);
				}catch (InterruptedException ex){
					throw new TestRuntimeException(this, ex);
				}
			}
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
		return createException(getLogExceptions(), message, cause);
	}

	@Experimental
	public <T extends StringTester<T>> T testString(String string){
		T tester = (T)new StringTester<>(() -> string, (T)null, this, getEvaluator());
		TesterHelper.setReturn(tester, tester);
		return tester;
	}

	@Experimental
	public <T extends StringTester<T>> T testStoredString(String key){
		return testString(load(key));
	}

	@Experimental
	public <T extends ComparableTester<C, T>, C extends Comparable<C>> T testComparable(C value){
		T tester = (T)new ComparableTester<>(() -> value, (T)null, this, getEvaluator());
		TesterHelper.setReturn(tester, tester);
		return tester;
	}

	@Experimental
	public <T extends ComparableTester<C, T>, C extends Comparable<C>> T testStoredComparable(Class<C> clazz, String key){
		return testComparable(load(clazz, key));
	}

	@Experimental
	public boolean ignoreException(Runnable action){
		boolean logExceptions = getLogExceptions();
		setLogExceptions(false);
		try {
			action.run();
			return true;
		}catch(Throwable t){
		}finally {
			setLogExceptions(logExceptions);
		}
		return false;
	}

	@Experimental
	public Throwable catchException(Runnable action){
		boolean logExceptions = getLogExceptions();
		setLogExceptions(false);
		try {
			action.run();
		}catch(Throwable t){
			return t;
		}finally {
			setLogExceptions(logExceptions);
		}
		return null;
	}

	@Experimental
	public void log(String message){
		this.getEvaluator().logMessage(message);
	}

	@Experimental
	public void log(String message, Throwable t){
		this.getEvaluator().logException(message, t);

	}

	@Experimental
	public void log(String action, Consumer<JsonObjectBuilder> eventParams){
		this.getEvaluator().logEvent(TestEvaluator.TEST_LOG, action, eventParams);
	}
}
