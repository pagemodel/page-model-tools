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

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.PageUtils;
import org.pagemodel.web.WebTestContext;
import org.pagemodel.web.utils.RefreshTracker;
import org.pagemodel.web.utils.Screenshot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class PageTester<P extends PageModel<? super P>> extends PageTesterBase<P> {
	protected static final int DEFAULT_SCRIPT_TIMEOUT_SECONDS = 20;

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

	public P testJavaScript(String javascript, int timeoutSeconds, Object...args) {
		executeJavaScript(javascript, timeoutSeconds, false, args);
		return page;
	}

	public JavaScriptReturnTester<P,P> testJavaScriptWithReturn(String javascript, int timeoutSeconds, Object...args) {
		return executeJavaScript(javascript, timeoutSeconds, false, args);
	}

	public P testJavaScriptAsync(String javascript, int timeoutSeconds, Object...args) {
		executeJavaScript(javascript, timeoutSeconds, true, args);
		return page;
	}

	public JavaScriptReturnTester<P,P> testJavaScriptAsyncWithReturn(String javascript, int timeoutSeconds, Object...args) {
		return executeJavaScript(javascript, timeoutSeconds, true, args);
	}

	private JavaScriptReturnTester<P,P> executeJavaScript(String javascript, int timeoutSeconds, boolean async, Object...args) {
		Object[] objRef = new Object[1];
		return getEvaluator().testExecute(
				() -> "javaScript [" + javascript + "] on page:[" + page.getClass().getSimpleName() + "]",
				() -> {
					testContext.getDriver().manage().timeouts().setScriptTimeout(timeoutSeconds, TimeUnit.SECONDS);
					JavascriptExecutor jse = (JavascriptExecutor) testContext.getDriver();
					objRef[0] = async ? jse.executeAsyncScript(javascript, args) : jse.executeScript(javascript, args);
				},
				new JavaScriptReturnTester<>(() -> objRef[0], page, javascript, page, getEvaluator()),
				page.getContext());
	}

	public PageWait<P> waitFor() {
		return new PageWait<>(page, testContext, new WebTestEvaluator.Wait(testContext, WebElementTester.WebElementWait.DEFAULT_WAIT_SEC));
	}

	public PageTesterBase<P> waitAndRefreshFor() {
		return new PageTesterBase<>(page, testContext, new WebTestEvaluator.WaitAndRefresh<>(testContext, WebElementTester.WebElementWait.DEFAULT_WAIT_SEC, page, page));
	}

	public P setWindowSize(int width, int height) {
		return getEvaluator().testExecute(
				() -> "set window size to dimension (" + width + " X " + height + ") on page [" + page.getClass().getSimpleName() + "]",
				() -> testContext.getDriver().manage().window().setSize(new Dimension(width, height)),
				page, page.getContext());
	}

	public P maximizeWindow() {
		return getEvaluator().testExecute(
				() -> "maximize window on page [" + page.getClass().getSimpleName() + "]",
				() -> testContext.getDriver().manage().window().maximize(),
				page, testContext);
	}

	public P fullscreenWindow() {
		return getEvaluator().testExecute(
				() -> "fullscreen window on page [" + page.getClass().getSimpleName() + "]",
				() -> testContext.getDriver().manage().window().fullscreen(),
				page, testContext);
	}

	public P setWindowPosition(int x, int y) {
		return getEvaluator().testExecute(
				() -> "set window position to point (x:" + x + " ,y:" + y + ") on page [" + page.getClass().getSimpleName() + "]",
				() -> testContext.getDriver().manage().window().setPosition(new Point(x, y)),
				page, testContext);
	}

	public P moveWindowPositionByOffset(int offsetX, int offsetY) {
		return getEvaluator().testExecute(
				() -> "move window position by offset (x:" + offsetX + " ,y:" + offsetY + ") on page [" + page.getClass().getSimpleName() + "]",
				() -> {
					Point pos = testContext.getDriver().manage().window().getPosition();
					testContext.getDriver().manage().window().setPosition(new Point(pos.x + offsetX, pos.y + offsetY));
				},
				page, testContext);
	}

	public P takeScreenshot(String filePrefix) {
		return getEvaluator().testExecute(
				() ->  "take screenshot of page [" + page.getClass().getSimpleName() + "], title: [" + testContext.getDriver().getTitle() + "], url: [" + testContext.getDriver().getCurrentUrl() + "]",
				() -> Screenshot.takeScreenshot(testContext, filePrefix + "_" + page.getClass().getSimpleName()),
				page, page.getContext());
	}

	public P refreshPage() {
		return RefreshTracker.refreshPage(page);
	}

	public <T extends PageModel<? super T>> T switchToDefaultContent(Class<T> clazz) {
		return getEvaluator().testExecute(
				() ->  "switch to default content on page [" + page.getClass().getSimpleName() + "]",
				() -> page.getContext().getDriver().switchTo().defaultContent(),
				page.testPage().testPageModel(clazz),
				page.getContext());
	}

	public <T extends PageModel<? super T>> T switchToParentFrame(Class<T> clazz) {
		return getEvaluator().testExecute(
				() ->  "switch to parent frame on page [" + page.getClass().getSimpleName() + "]",
				() -> page.getContext().getDriver().switchTo().parentFrame(),
				page.testPage().testPageModel(clazz),
				page.getContext());
	}

	public <T extends PageModel<? super T>> T testPageModel(Class<T> clazz) {
		try {
			getEvaluator().log("test page model [" + clazz.getSimpleName() + "] from page [" + page.getClass().getSimpleName() + "]");
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
		return getEvaluator().testExecute(
				() ->  "navigate to: [" + clazz.getName() + "], url [" + url + "]",
				() -> testContext.getDriver().navigate().to(url),
				PageUtils.waitForNavigateToPage(clazz, testContext),
				page.getContext());
	}
}
