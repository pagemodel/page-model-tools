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

package org.pagemodel.web.paths;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.PageUtils;
import org.pagemodel.web.WebTestContext;
import org.pagemodel.web.utils.Screenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * This class provides utility methods for page loading and resolving page types.
 */
public class PageResolver {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public final static PageResolver RESOLVER = new PageResolver();
	public final static int DEFAULT_LOAD_TIMEOUT_SEC = 10;

	/**
	 * Tries to resolve the given page types and returns the first one that is found.
	 * Uses the default load timeout of 10 seconds.
	 * @param testContext the WebTestContext object
	 * @param classList the list of page types to try
	 * @return the first page model found, or null if none are found
	 */
	public PageModel<?> tryPageTypes(WebTestContext testContext, Class<? extends PageModel>... classList) {
		return tryPageTypes(testContext, DEFAULT_LOAD_TIMEOUT_SEC, classList);
	}

	/**
	 * Tries to resolve the given page types and returns the first one that is found.
	 * Uses the specified load timeout.
	 * @param testContext the WebTestContext object
	 * @param timeoutSeconds the load timeout in seconds
	 * @param classList the list of page types to try
	 * @return the first page model found, or null if none are found
	 */
	public <T extends PageModel<? super T>> T tryPageTypes(WebTestContext testContext, int timeoutSeconds, Class<? extends PageModel>... classList) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < (timeoutSeconds * 1000)) {
			T page = (T) tryPageTypesOnce(testContext, timeoutSeconds, classList);
			if (page != null) {
				try {
					return PageUtils.waitForModelDisplayed(page, 1);
				} catch (RuntimeException ex) {
				}
			}
		}
		return null;
	}

	/**
	 * Tries to resolve the given page types once and returns the first one that is found.
	 * Uses the specified load timeout.
	 * @param testContext the WebTestContext object
	 * @param timeoutSeconds the load timeout in seconds
	 * @param classList the list of page types to try
	 * @return the first page model found, or null if none are found
	 */
	public <T extends PageModel<? super T>> T tryPageTypesOnce(WebTestContext testContext, int timeoutSeconds, Class<? extends PageModel>... classList) {
		try {
			testContext.getDriver().manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
			if (!waitForPageLoad(testContext, timeoutSeconds)) {
				Screenshot.takeScreenshot(testContext, "ERROR_" + classList[0].getSimpleName());
				return null;
			}
			testContext.getDriver().manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
			for (Class<? extends PageModel> clazz : classList) {
				T page = tryPageType(clazz, testContext);
				if (page != null) {
					return PageUtils.waitForModelDisplayed(page, 1);
				}
			}
			Screenshot.takeScreenshot(testContext, "ERROR_" + classList[0].getSimpleName());
			return null;
		} finally {
			testContext.getDriver().manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		}
	}

	/**
	 * Waits for the page to load.
	 * @param testContext the WebTestContext object
	 * @param timeoutSeconds the load timeout in seconds
	 * @return true if the page loads within the timeout, false otherwise
	 */
	public boolean waitForPageLoad(final WebTestContext testContext, int timeoutSeconds) {
		try {
			return new WebDriverWait(testContext.getDriver(), timeoutSeconds)
					.ignoring(NoSuchElementException.class)
					.until(d -> {
						return testContext.getDriver().findElement(By.tagName("body")) != null;
					});
		} catch (TimeoutException te) {
			log.info("Timed out waiting for page body to load", te);
			return false;
		}
	}

	/**
	 * Tries to create an instance of the given page type and returns it if it is displayed.
	 * @param clazz the page type to create
	 * @param testContext the WebTestContext object
	 * @return the page model if it is displayed, or null if it is not displayed or cannot be created
	 */
	public <T extends PageModel<? super T>> T tryPageType(Class<? extends PageModel> clazz, WebTestContext testContext) {
		T page = null;
		try {
			for(Constructor<?> c : clazz.getConstructors()){
				if(c.getParameterTypes().length == 1 && c.getParameterTypes()[0].isAssignableFrom(testContext.getClass())){
					page = (T)c.newInstance(testContext);
				}
			}
			if(page == null){
				log.error("Error creating Page Model for class: " + clazz.getName() + ".  Unable to find valid constructor.");
				return null;
			}
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
			log.error("Error creating Page Model for class: " + clazz.getName(), ex);
			return null;
		}
		try {
			if (page.modelDisplayed()) {
				return page;
			}
		} catch (NullPointerException | NoSuchElementException | StaleElementReferenceException ex) {
		}
		return null;
	}
}