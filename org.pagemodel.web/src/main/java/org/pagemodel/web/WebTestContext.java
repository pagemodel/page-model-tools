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
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.json.JsonLogConsoleOut;
import org.pagemodel.web.utils.PageException;

import java.util.Map;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * This interface represents a context for web tests. It extends the TestContext interface and adds methods for managing a WebDriver, storing and loading values during a test, and capturing a screenshot and page source on exception.
 */
public interface WebTestContext extends TestContext {

	/**
	 * Returns the WebDriver instance managed by this context.
	 * @return the WebDriver instance
	 */
	WebDriver getDriver();

	/**
	 * Sets the WebDriver instance to be managed by this context.
	 * @param driver the WebDriver instance to set
	 */
	void setDriver(WebDriver driver);

	/**
	 * Quits the WebDriver instance managed by this context and sets it to null.
	 * If the WebDriver instance is already null, this method does nothing.
	 */
	default void quit() {
		if (getDriver() == null) {
			return;
		}
		this.getEvaluator().logEvent(TestEvaluator.TEST_EXECUTE, "close browser", json -> {});
		try {
			getDriver().quit();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		setDriver(null);
	}

	/**
	 * Creates a PageException with the given screenshotOnError flag, message, and cause.
	 * @param screenshotOnError true if a screenshot should be taken on exception, false otherwise
	 * @param message the error message
	 * @param cause the cause of the exception
	 * @return the created PageException
	 */
	PageException createException(boolean screenshotOnError, String message, Throwable cause);

	/**
	 * Creates a PageException with the given message and cause, using the current logExceptions flag.
	 * @param message the error message
	 * @param cause the cause of the exception
	 * @return the created PageException
	 */
	default PageException createException(String message, Throwable cause) {
		return createException(getLogExceptions(), message, cause);
	}

	/**
	 * Creates a PageException with the given screenshotOnError flag, event, and cause.
	 * @param screenshotOnError true if a screenshot should be taken on exception, false otherwise
	 * @param event the event that triggered the exception
	 * @param cause the cause of the exception
	 * @return the created PageException
	 */
	default PageException createException(boolean screenshotOnError, Map<String,Object> event, Throwable cause) {
		return createException(screenshotOnError, JsonLogConsoleOut.formatEvent(event), cause);
	}

	/**
	 * Sets the logExceptions flag to the given value.
	 * @param flag the value to set the logExceptions flag to
	 */
	public void setLogExceptions(boolean flag);

	/**
	 * Returns the current value of the logExceptions flag.
	 * @return true if exceptions should be logged, false otherwise
	 */
	public boolean getLogExceptions();
}