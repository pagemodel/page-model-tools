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
import org.pagemodel.core.DefaultTestContext;
import org.pagemodel.web.utils.PageException;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * This class manages a WebDriver, can store and load values during a test, and captures a screenshot and page source on exception.
 * It extends the DefaultTestContext class and implements the WebTestContext interface.
 */
public class DefaultWebTestContext extends DefaultTestContext implements WebTestContext {

	/**
	 * The WebDriver instance used by this context.
	 */
	protected WebDriver driver;

	/**
	 * A flag indicating whether to capture a screenshot on exception.
	 */
	protected boolean screenshotErrorFlag;

	/**
	 * Constructs a new DefaultWebTestContext with the specified WebDriver instance.
	 * @param driver the WebDriver instance to use
	 */
	public DefaultWebTestContext(WebDriver driver) {
		super();
		this.driver = driver;
		this.screenshotErrorFlag = true;
	}

	/**
	 * Returns the WebDriver instance used by this context.
	 * @return the WebDriver instance
	 */
	@Override
	public WebDriver getDriver() {
		return driver;
	}

	/**
	 * Sets the WebDriver instance used by this context.
	 * @param driver the WebDriver instance to use
	 */
	@Override
	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}

	/**
	 * Creates a new PageException with the specified parameters.
	 * @param screenshotOnError a flag indicating whether to capture a screenshot on exception
	 * @param message the error message
	 * @param cause the cause of the exception
	 * @return a new PageException instance
	 */
	@Override
	public PageException createException(boolean screenshotOnError, String message, Throwable cause) {
		return new PageException(this, screenshotOnError, message, cause);
	}

	/**
	 * Sets the flag indicating whether to capture a screenshot on exception.
	 * @param flag the flag value to set
	 */
	@Override
	public void setLogExceptions(boolean flag){
		this.screenshotErrorFlag = flag;
	}

	/**
	 * Returns the flag indicating whether to capture a screenshot on exception.
	 * @return the flag value
	 */
	@Override
	public boolean getLogExceptions(){
		return this.screenshotErrorFlag;
	}
}