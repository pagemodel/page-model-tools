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

package org.pagemodel.web;

import org.openqa.selenium.WebDriver;
import org.pagemodel.core.TestContext;
import org.pagemodel.core.utils.json.JsonLogConsoleOut;
import org.pagemodel.web.utils.PageException;

import java.util.Map;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public interface WebTestContext extends TestContext {

	WebDriver getDriver();

	void setDriver(WebDriver driver);

	default void quit() {
		if (getDriver() == null) {
			return;
		}
		try {
			getDriver().quit();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		setDriver(null);
	}

	PageException createException(boolean screenshotOnError, String message, Throwable cause);

	default PageException createException(String message, Throwable cause) {
		return createException(getLogExceptions(), message, cause);
	}

	default PageException createException(boolean screenshotOnError, Map<String,Object> event, Throwable cause) {
		return createException(screenshotOnError, JsonLogConsoleOut.formatEvent(event), cause);
	}

	public void setLogExceptions(boolean flag);

	public boolean getLogExceptions();
}
