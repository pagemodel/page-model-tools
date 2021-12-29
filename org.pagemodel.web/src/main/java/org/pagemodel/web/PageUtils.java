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
public class PageUtils {
	public static final int DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS = 20;

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

	static public <T extends PageModel<? super T>> T waitForNavigateToPage(final Class<T> clazz, final WebTestContext context) {
		return waitForNavigateToPage(clazz, null, null, context, DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS);
	}

	static public <T extends PageModel<? super T>> T waitForNavigateToPage(final Class<T> clazz, final WebTestContext context, int timeoutSeconds) {
		return waitForNavigateToPage(clazz, null, null, context, timeoutSeconds);
	}

	static public <T extends PageModel<? super T>> T waitForNavigateToPage(final Class<T> clazz, PageModel<?> sectionParent, Callable<WebElement> elementRef, final WebTestContext context) {
		return waitForNavigateToPage(clazz, sectionParent, elementRef, context, DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS);
	}

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

	static public <T extends PageModel<? super T>> T waitForModelDisplayed(T page) {
		return waitForModelDisplayed(page, DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS);
	}

	static public <T extends PageModel<? super T>> T waitForModelDisplayed(T page, int timeout) {
		if (waitForPageIsDisplayed(page, timeout)) {
			return page;
		}
		throw new RuntimeException("Page not displayed: " + page.getClass());
	}

	static public boolean waitForPageIsDisplayed(PageModel page) {
		return waitForPageIsDisplayed(page, DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS);
	}

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

	public static TestEvaluator getEvaluator(PageModel<?> page){
		return page.getEvaluator();
	}

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
