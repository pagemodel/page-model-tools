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

package org.pagemodel.web.testers;

import org.pagemodel.web.WebTestContext;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.utils.PageException;
import org.pagemodel.web.PageUtils;
import org.pagemodel.web.utils.RefreshTracker;
import org.pagemodel.web.utils.Screenshot;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class PageTester<P extends PageModel<? super P>> extends PageTesterBase<P> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public PageTester(P page, WebTestContext testContext, TestEvaluator testEvaluator) {
		super(page, testContext, testEvaluator);
	}

	public PageTester(P page, WebTestContext testContext) {
		this(page, testContext, new TestEvaluator.Now());
	}

	public WebElementTester<P, P> testFocusedElement() {
		return new WebElementTester<>(ClickAction.make(() -> testContext.getDriver().switchTo().activeElement(), page));
	}

	public WebElementTester<P, P> testHTMLElement() {
		return new WebElementTester<>(ClickAction.make(() -> testContext.getDriver().findElement(By.tagName("html")), page));
	}

	public WebElementTester<P, P> testBodyElement() {
		return new WebElementTester<>(ClickAction.make(() -> testContext.getDriver().findElement(By.tagName("body")), page));
	}

	public PageWait<P> waitFor() {
		return new PageWait<>(page, testContext, new WebTestEvaluator.Wait(testContext, WebElementTester.WebElementWait.DEFAULT_WAIT_SEC));
	}

	public PageTesterBase<P> waitAndRefreshFor() {
		return new PageTesterBase<>(page, testContext, new WebTestEvaluator.WaitAndRefresh<>(testContext, WebElementTester.WebElementWait.DEFAULT_WAIT_SEC, page, page));
	}

	public P setWindowSize(int width, int height) {
		log.info("Set window size to dimension (" + width + " X " + height + ") on page [" + page.getClass().getSimpleName() + "]");
		testContext.getDriver().manage().window().setSize(new Dimension(width, height));
		return page;
	}

	public P maximizeWindow() {
		return getEvaluator().testCondition(() -> "Maximize window on page [" + page.getClass().getSimpleName() + "]", () -> {
			testContext.getDriver().manage().window().maximize();
			return true;
		}, page, testContext);
	}

	public P fullscreenWindow() {
		return getEvaluator().testCondition(() -> "Fullscreen window on page [" + page.getClass().getSimpleName() + "]", () -> {
			testContext.getDriver().manage().window().fullscreen();
			return true;
		}, page, testContext);
	}

	public P setWindowPosition(int x, int y) {
		log.info("Set window position to point (x:" + x + " ,y:" + y + ") on page [" + page.getClass().getSimpleName() + "]");
		testContext.getDriver().manage().window().setPosition(new Point(x, y));
		return page;
	}

	public P moveWindowPosition(int offsetX, int offsetY) {
		log.info("Moving window position by offset (x:" + offsetX + " ,y:" + offsetY + ") on page [" + page.getClass().getSimpleName() + "]");
		Point pos = testContext.getDriver().manage().window().getPosition();
		testContext.getDriver().manage().window().setPosition(new Point(pos.x + offsetX, pos.y + offsetY));
		return page;
	}

	public P takeScreenshot(String filePrefix) {
		log.info("Taking screenshot of page [" + page.getClass().getSimpleName() + "], title: [" + testContext.getDriver().getTitle() + "], url: [" + testContext.getDriver().getCurrentUrl() + "]");
		Screenshot.takeScreenshot(testContext, filePrefix + "_" + page.getClass().getSimpleName());
		return page;
	}

	public P refreshPage() {
		return RefreshTracker.refreshPage(page);
	}

	public <T extends PageModel<? super T>> T testPageModel(Class<T> clazz) {
		try {
			log.info("Testing page model [" + clazz.getSimpleName() + "] from page [" + page.getClass().getSimpleName() + "]");
			for(Constructor<?> c : clazz.getConstructors()){
				if(c.getParameterTypes().length == 1 && c.getParameterTypes()[0].isAssignableFrom(testContext.getClass())){
					return (T)c.newInstance(testContext);
				}
			}
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
			throw testContext.createException("Error: Unable to create instance of [" + clazz.getName() + "]", ex);
		}
		throw testContext.createException("Error: Unable to create instance of [" + clazz.getName() + "]");
	}

	public <T extends PageModel<? super T>> T navigateTo(String url, Class<T> clazz) {
		try {
			log.info(getEvaluator().getActionMessage(() -> "navigate to: [" + clazz.getName() + "], url [" + url + "]"));
			testContext.getDriver().navigate().to(url);
			return PageUtils.waitForNavigateToPage(clazz, testContext);
		} catch (PageException ex) {
			throw ex;
		} catch (Exception ex) {
			throw testContext.createException("navigate to: [" + clazz.getName() + "], url [" + url + "]", ex);
		}
	}
}
