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

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.testers.ClickAction;
import org.pagemodel.web.testers.WebTestEvaluator;
import org.pagemodel.web.utils.Screenshot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * A utility class for working with page models in a web testing context.
 */
public class PageUtils {

	/**
	 * The default page load timeout in seconds.
	 */
	public static int DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS = 20;

	/**
	 * Creates an instance of the specified page model class using the provided web test context.
	 *
	 * @param clazz the class of the page model to create
	 * @param context the web test context to use
	 * @return an instance of the specified page model class
	 * @throws RuntimeException if an error occurs while creating the page model
	 */
	public static <T extends PageModel<? super T>> T makeInstance(final Class<T> clazz, final WebTestContext context) {
		try {
			for(Constructor<?> c : clazz.getConstructors()){
				if(c.getParameterTypes().length == 1 && c.getParameterTypes()[0].isAssignableFrom(context.getClass())){
					T page = (T)c.newInstance(context);
					//TODO: fix test evaluator message source to be cleared out
//					return PageUtils.trySetEvaluator(page, context.getEvaluator());
					return page;
				}
			}
			throw new RuntimeException("Error creating Page Model for class: " + clazz.getName() + ".  Unable to find valid constructor.");
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Waits for the specified page model class to be displayed in the web test context.
	 *
	 * @param clazz the class of the page model to wait for
	 * @param context the web test context to use
	 * @return the displayed page model instance
	 * @throws RuntimeException if the page model is not displayed within the default timeout
	 */
	static public <T extends PageModel<? super T>> T waitForNavigateToPage(final Class<T> clazz, final WebTestContext context) {
		return waitForNavigateToPage(clazz, null, null, context, DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS);
	}

	/**
	 * Waits for the specified page model class to be displayed in the web test context with the specified timeout.
	 *
	 * @param clazz the class of the page model to wait for
	 * @param context the web test context to use
	 * @param timeoutSeconds the timeout in seconds to wait for the page model to be displayed
	 * @return the displayed page model instance
	 * @throws RuntimeException if the page model is not displayed within the specified timeout
	 */
	static public <T extends PageModel<? super T>> T waitForNavigateToPage(final Class<T> clazz, final WebTestContext context, int timeoutSeconds) {
		return waitForNavigateToPage(clazz, null, null, context, timeoutSeconds);
	}

	/**
	 * Waits for the specified page model class to be displayed in the web test context as a child of the specified section parent and with the specified element reference.
	 *
	 * @param clazz the class of the page model to wait for
	 * @param sectionParent the parent section model of the page model to wait for
	 * @param elementRef the element reference of the page model to wait for
	 * @param context the web test context to use
	 * @return the displayed page model instance
	 * @throws RuntimeException if the page model is not displayed within the default timeout
	 */
	static public <T extends PageModel<? super T>> T waitForNavigateToPage(final Class<T> clazz, PageModel<?> sectionParent, Callable<WebElement> elementRef, final WebTestContext context) {
		return waitForNavigateToPage(clazz, sectionParent, elementRef, context, DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS);
	}

	/**
	 * Waits for the specified page model class to be displayed in the web test context as a child of the specified section parent and with the specified element reference and timeout.
	 *
	 * @param clazz the class of the page model to wait for
	 * @param sectionParent the parent section model of the page model to wait for
	 * @param elementRef the element reference of the page model to wait for
	 * @param context the web test context to use
	 * @param timeout the timeout in seconds to wait for the page model to be displayed
	 * @return the displayed page model instance
	 * @throws RuntimeException if the page model is not displayed within the specified timeout
	 */
	static public <T extends PageModel<? super T>> T waitForNavigateToPage(final Class<T> clazz, PageModel<?> sectionParent, Callable<WebElement> elementRef, final WebTestContext context, int timeout) {
		try {
			T page = null;
			if (SectionModel.class.isAssignableFrom(clazz)) {
				page = (T) SectionModel.make((Class) clazz, ClickAction.make(elementRef, (PageModel) sectionParent, sectionParent.getEvaluator()), sectionParent.getEvaluator());
			} else {
				for(Constructor<?> c : clazz.getConstructors()){
					if(c.getParameterTypes().length == 1 && c.getParameterTypes()[0].isAssignableFrom(context.getClass())){
						page = (T)c.newInstance(context);
					}
				}
				if(page == null){
					throw new RuntimeException("Error creating Page Model for class: " + clazz.getName() + ".  Unable to find valid constructor.");
				}
			}
			//TODO: fix test evaluator message source to be cleared out
//			PageUtils.trySetEvaluator(page, context.getEvaluator());
			boolean isDisplayed = waitForPageIsDisplayed(page, timeout);
			if (isDisplayed) {
				page.onPageLoad();
				return page;
			} else {
				throw new RuntimeException("Unable to find page: " + clazz.getName());
			}
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
			Screenshot.takeScreenshot(context, "ERROR+" + clazz.getName());
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Waits for the specified page model instance to be displayed in the web test context.
	 *
	 * @param page the page model instance to wait for
	 * @return the displayed page model instance
	 * @throws RuntimeException if the page model is not displayed within the default timeout
	 */
	static public <T extends PageModel<? super T>> T waitForModelDisplayed(T page) {
		return waitForModelDisplayed(page, DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS);
	}

	/**
	 * Waits for the specified page model instance to be displayed in the web test context with the specified timeout.
	 *
	 * @param page the page model instance to wait for
	 * @param timeout the timeout in seconds to wait for the page model to be displayed
	 * @return the displayed page model instance
	 * @throws RuntimeException if the page model is not displayed within the specified timeout
	 */
	static public <T extends PageModel<? super T>> T waitForModelDisplayed(T page, int timeout) {
		if (waitForPageIsDisplayed(page, timeout)) {
			return page;
		}
		throw new RuntimeException("Page not displayed: " + page.getClass());
	}

	/**
	 * Waits for the specified page model instance to be displayed in the web test context.
	 *
	 * @param page the page model instance to wait for
	 * @return true if the page model is displayed within the default timeout, false otherwise
	 */
	static public boolean waitForPageIsDisplayed(PageModel page) {
		return waitForPageIsDisplayed(page, DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS);
	}

	/**
	 * Waits for the specified page model instance to be displayed in the web test context with the specified timeout.
	 *
	 * @param page the page model instance to wait for
	 * @param timeout the timeout in seconds to wait for the page model to be displayed
	 * @return true if the page model is displayed within the specified timeout, false otherwise
	 */
	static public boolean waitForPageIsDisplayed(PageModel page, int timeout) {
		WebDriverWait wait = new WebDriverWait(page.getContext().getDriver(), timeout);
		try {
			new WebTestEvaluator.Wait(page.getContext(),timeout).logEvent(
					TestEvaluator.TEST_ASSERT,
					"model displayed", op -> op
							.addValue("model",  page.getClass().getSimpleName()));
			wait.until(d -> {
				page.getContext().getDriver().manage().timeouts().implicitlyWait(50, TimeUnit.MILLISECONDS);
				try {
					return page.modelDisplayed();
				} catch (NullPointerException | NoSuchElementException | StaleElementReferenceException ex) {
					return false;
				}
			});
		} catch (TimeoutException te) {
			new WebTestEvaluator.Wait(page.getContext(),timeout).logEvent(
					TestEvaluator.TEST_ASSERT,
					"model displayed", op -> op
							.addValue("model",  page.getClass().getSimpleName()));
			if(PageModel.DefaultPageModel.class.isAssignableFrom(page.getClass())){
				((PageModel.DefaultPageModel)page).logModelDisplayed();
			}else if(SectionModel.class.isAssignableFrom(page.getClass())){
				((SectionModel)page).logModelDisplayed();
//			} else {
//				Screenshot.takeScreenshot(page.getContext(), "ERROR+" + page.getClass().getName());
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the test evaluator for the specified page model instance.
	 *
	 * @param page the page model instance to get the test evaluator for
	 * @return the test evaluator for the specified page model instance
	 */
	public static TestEvaluator getEvaluator(PageModel<?> page){
		return page.getEvaluator();
	}

	/**
	 * Sets the test evaluator for the specified page model instance.
	 *
	 * @param page the page model instance to set the test evaluator for
	 ** @param testEvaluator the test evaluator to set for the page model instance
	 * @return the page model instance with the test evaluator set
	 */
	public static <T extends PageModel<? super T>> T trySetEvaluator(T page, TestEvaluator testEvaluator){
		if(page instanceof PageModel.DefaultPageModel) {
			((PageModel.DefaultPageModel)page).setTestEvaluator(testEvaluator);
			return page;
		}else if(page instanceof SectionModel){
			((SectionModel)page).setTestEvaluator(testEvaluator);
			return page;
		}
		try {
			page.getClass().getMethod("setTestEvaluator", TestEvaluator.class).invoke(page, testEvaluator);
		}catch (Exception ex){ }
		return page;
	}

	/**
	 * Logs the page source for the specified web test context with the specified test type and messages.
	 *
	 * @param testContext the web test context to log the page source for
	 * @param testType the test type to use for the log event
	 * @param messages the messages to include in the log event
	 */
	public static void logPageSource(WebTestContext testContext, String testType, String...messages){
		String msg = String.join("\n", messages);
		if(testType == null || testType.isEmpty()){
			testType = TestEvaluator.TEST_LOG;
		}
		Screenshot.takeScreenshot(testContext, ("Page" + testType).replaceAll("\\s", ""));
		testContext.getEvaluator().logEvent(testType, "page source", op -> op
				.doAdd(o -> {
					if(!msg.isEmpty()){
						o.addValue("message", msg);
					}
				})
				.addValue("url", testContext.getDriver().getCurrentUrl())
				.addValue("title", testContext.getDriver().getTitle())
				.addValue("handle", testContext.getDriver().getWindowHandle())
				.addValue("iframe", testContext.getDriver().getPageSource())
				.addValue("html-src", testContext.getDriver().getPageSource()));
	}
}