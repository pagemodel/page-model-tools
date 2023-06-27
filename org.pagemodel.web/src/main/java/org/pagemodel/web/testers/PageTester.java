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

import org.openqa.selenium.*;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.json.JsonBuilder;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.PageUtils;
import org.pagemodel.web.WebTestContext;
import org.pagemodel.web.paths.PageFlow;
import org.pagemodel.web.utils.RefreshTracker;
import org.pagemodel.web.utils.Screenshot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.pagemodel.web.PageUtils.DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * This class represents a tester for a web page. It extends the PageTesterBase class and uses a PageModel object as a type parameter.
 * It provides methods to test various elements of the page, execute JavaScript code, wait for page elements to load, take screenshots, and navigate to other pages.
 * @param <P> a PageModel object or any of its subclasses
 */
public class PageTester<P extends PageModel<? super P>> extends PageTesterBase<P> {

	/**
	 * The default script timeout in seconds.
	 */
	protected static final int DEFAULT_SCRIPT_TIMEOUT_SECONDS = 20;

	/**
	 * Constructs a new PageTester object with the given PageModel, WebTestContext, and TestEvaluator objects.
	 * @param page a PageModel object or any of its subclasses
	 * @param testContext a WebTestContext object
	 * @param testEvaluator a TestEvaluator object
	 */
	public PageTester(P page, WebTestContext testContext, TestEvaluator testEvaluator) {
		super(page, testContext, testEvaluator);
	}

	/**
	 * Tests the currently focused element on the page.
	 * @return a WebElementTester object
	 */
	public WebElementTester<P, P> testFocusedElement() {
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"focused element", op -> op
						.addValue("model", page.getClass().getName()));
		return new WebElementTester<>(ClickAction.make(() -> testContext.getDriver().switchTo().activeElement(), page, getEvaluator()), getEvaluator());
	}

	/**
	 * Tests the HTML element of the page.
	 * @return a WebElementTester object
	 */
	public WebElementTester<P, P> testHTMLElement() {
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"page html", op -> op
						.addValue("model", page.getClass().getName()));
		return new WebElementTester<>(ClickAction.make(() -> testContext.getDriver().findElement(By.tagName("html")), page, getEvaluator()), getEvaluator());
	}

	/**
	 * Tests the body element of the page.
	 * @return a WebElementTester object
	 */
	public WebElementTester<P, P> testBodyElement() {
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"page body", op -> op
						.addValue("model", page.getClass().getName()));
		return new WebElementTester<>(ClickAction.make(() -> testContext.getDriver().findElement(By.tagName("body")), page, getEvaluator()), getEvaluator());
	}

	/**
	 * Executes JavaScript code on the page and returns the PageModel object.
	 * @param javascript a String representing the JavaScript code to execute
	 * @param timeoutSeconds an int representing the timeout in seconds
	 * @param args an array of Objects representing the arguments to pass to the JavaScript code
	 * @return a PageModel object or any of its subclasses
	 */
	public P testJavaScript(String javascript, int timeoutSeconds, Object...args) {
		executeJavaScript(javascript, timeoutSeconds, false, args);
		return page;
	}

	/**
	 * Executes JavaScript code on the page and returns a JavaScriptReturnTester object.
	 * @param javascript a String representing the JavaScript code to execute
	 * @param timeoutSeconds an int representing the timeout in seconds
	 * @param args an array of Objects representing the arguments to pass to the JavaScript code
	 * @return a JavaScriptReturnTester object
	 */
	public JavaScriptReturnTester<P,P> testJavaScriptWithReturn(String javascript, int timeoutSeconds, Object...args) {
		return executeJavaScript(javascript, timeoutSeconds, false, args);
	}

	/**
	 * Executes asynchronous JavaScript code on the page and returns the PageModel object.
	 * @param javascript a String representing the JavaScript code to execute
	 * @param timeoutSeconds an int representing the timeout in seconds
	 * @param args an array of Objects representing the arguments to pass to the JavaScript code
	 * @return a PageModel object or any of its subclasses
	 */
	public P testJavaScriptAsync(String javascript, int timeoutSeconds, Object...args) {
		executeJavaScript(javascript, timeoutSeconds, true, args);
		return page;
	}

	/**
	 * Executes asynchronous JavaScript code on the page and returns a JavaScriptReturnTester object.
	 * @param javascript a String representing the JavaScript code to execute
	 * @param timeoutSeconds an int representing the timeout in seconds
	 * @param args an array of Objects representing the arguments to pass to the JavaScript code
	 * @return a JavaScriptReturnTester object
	 */
	public JavaScriptReturnTester<P,P> testJavaScriptAsyncWithReturn(String javascript, int timeoutSeconds, Object...args) {
		return executeJavaScript(javascript, timeoutSeconds, true, args);
	}

	/**
	 * Returns the name of the PageModel class.
	 * @return a String representing the name of the PageModel class
	 */
	private String getModelName(){
		return page == null ? null : page.getClass().getSimpleName();
	}

	/**
	 * Executes JavaScript code on the page and returns a JavaScriptReturnTester object.
	 * @param javascript a String representing the JavaScript code to execute
	 * @param timeoutSeconds an int representing the timeout in seconds
	 * @param async a boolean indicating whether the JavaScript code should be executed asynchronously
	 * @param args an array of Objects representing the arguments to pass to the JavaScript code
	 * @return a JavaScriptReturnTester object
	 */
	private JavaScriptReturnTester<P,P> executeJavaScript(String javascript, int timeoutSeconds, boolean async, Object...args) {
		Object[] objRef = new Object[1];
		return getEvaluator().testExecute(
				"javaScript", op -> op
						.addValue("value", javascript)
						.addValue("model", getModelName()),
				() -> {
					testContext.getDriver().manage().timeouts().setScriptTimeout(timeoutSeconds, TimeUnit.SECONDS);
					JavascriptExecutor jse = (JavascriptExecutor) testContext.getDriver();
					objRef[0] = async ? jse.executeAsyncScript(javascript, args) : jse.executeScript(javascript, args);
				},
				new JavaScriptReturnTester<>(() -> objRef[0], page, javascript, page, getEvaluator()),
				page.getContext());
	}

	/**
	 * Returns a PageWait object for waiting for page elements to load.
	 * @return a PageWait object
	 */
	public PageWait<P> waitFor() {
		return new PageWait<>(page, testContext, new WebTestEvaluator.Wait(testContext, WebElementTester.WebElementWait.DEFAULT_WAIT_SEC));
	}

	/**
	 * Returns a PageTesterBase object for waiting for page elements to load and refreshing the page.
	 * @return a PageTesterBase object
	 */
	public PageTesterBase<P> waitAndRefreshFor() {
		return new PageTesterBase<>(page, testContext, new WebTestEvaluator.WaitAndRefresh<>(testContext, WebElementTester.WebElementWait.DEFAULT_WAIT_SEC, page, page));
	}

	/**
	 * Sets the size of the browser window and returns the PageModel object.
	 * @param width an int representing the width of the window
	 * @param height an int representing the height of the window
	 * @return a PageModel object or any of its subclasses
	 */
	public P setWindowSize(int width, int height) {
		return getEvaluator().testExecute(
				"set window size", op -> op
						.addValue("value", width + " X " + height)
						.addValue("model",getModelName()),
				() -> testContext.getDriver().manage().window().setSize(new Dimension(width, height)),
				page, page.getContext());
	}

	/**
	 * Maximizes the browser window and returns the PageModel object.
	 * @return a PageModel object or any of its subclasses
	 */
	public P maximizeWindow() {
		return getEvaluator().testExecute(
				"maximize window", op -> op
						.addValue("model", getModelName()),
				() -> testContext.getDriver().manage().window().maximize(),
				page, testContext);
	}

	/**
	 * Makes the browser window fullscreen and returns the PageModel object.
	 * @return a PageModel object or any of its subclasses
	 */
	public P fullscreenWindow() {
		return getEvaluator().testExecute(
				"fullscreen window", op -> op
						.addValue("model", getModelName()),
				() -> testContext.getDriver().manage().window().fullscreen(),
				page, testContext);
	}

	/**
	 * Sets the position of the browser window and returns the PageModel object.
	 * @param x an int representing the x-coordinate of the window
	 * @param y an int representing the y-coordinate of the window
	 * @return a PageModel object or any of its subclasses
	 */
	public P setWindowPosition(int x, int y) {
		return getEvaluator().testExecute(
				"set window position", op -> op
						.addValue("value", x + ", " + y)
						.addValue("model",getModelName()),
				() -> testContext.getDriver().manage().window().setPosition(new Point(x, y)),
				page, testContext);
	}

	/**
	 * Moves the browser window by the given offset and returns the PageModel object.
	 * @param offsetX an int representing the x-offset to move the window
	 * @param offsetY an int representing the y-offset to move the window
	 * @return a PageModel object or any of its subclasses
	 */
	public P moveWindowPositionByOffset(int offsetX, int offsetY) {
		return getEvaluator().testExecute(
				"move window", op -> op
						.addValue("value", offsetX + ", " + offsetY)
						.addValue("model",getModelName()),
				() -> {
					Point pos = testContext.getDriver().manage().window().getPosition();
					testContext.getDriver().manage().window().setPosition(new Point(pos.x + offsetX, pos.y + offsetY));
				},
				page, testContext);
	}

	/**
	 * Takes a screenshot of the current page and saves it with the given file prefix.
	 * @param filePrefix a String representing the prefix to use for the file name
	 * @return a PageModel object or any of its subclasses
	 */
	public P takeScreenshot(String filePrefix) {
		Screenshot.takeScreenshot(testContext, filePrefix + "_" + page.getClass().getSimpleName());
		return page;
	}

	/**
	 * Logs the page source with the given messages and returns the PageModel object.
	 * @param messages an array of Strings representing the messages to log with the page source
	 * @return a PageModel object or any of its subclasses
	 */
	public P logPageSource(String...messages){
		PageUtils.logPageSource(page.getContext(), TestEvaluator.TEST_LOG, messages);
		return page;
	}

	/**
	 * Refreshes the current page and returns the PageModel object.
	 * @return a PageModel object or any of its subclasses
	 */
	public P refreshPage() {
		return RefreshTracker.refreshPage(page);
	}

	/**
	 * Switches to the default content and returns a PageModel object of the given class.
	 * @param clazz a Class object representing the class of the PageModel to return
	 * @return a PageModel object or any of its subclasses
	 */
	public <T extends PageModel<? super T>> T switchToDefaultContent(Class<T> clazz) {
		String className = clazz == null ? null : clazz.getSimpleName();
		return getEvaluator().testExecute(
				"switch to default content", op -> op
						.addValue("expected", className)
						.addValue("model", getModelName()),
				() -> page.getContext().getDriver().switchTo().defaultContent(),
				page.testPage().testPageModel(clazz), page.getContext());
	}

	/**
	 * Switches to the parent frame and returns a PageModel object of the given class.
	 * @param clazz a Class object representing the class of the PageModel to return
	 * @return a PageModel object or any of its subclasses
	 */
	public <T extends PageModel<? super T>> T switchToParentFrame(Class<T> clazz) {
		String className = clazz == null ? null : clazz.getSimpleName();
		return getEvaluator().testExecute(
				"switch to parent frame", op -> op
						.addValue("expected", className)
						.addValue("model", getModelName()),
				() -> page.getContext().getDriver().switchTo().parentFrame(),
				page.testPage().testPageModel(clazz), page.getContext());
	}

	/**
	 * Tests a PageModel object of the given class and returns it.
	 * @param clazz a Class object representing the class of the PageModel to test and return
	 * @return a PageModel object or any of its subclasses
	 * @throws IllegalAccessException if the constructor of the PageModel object cannot be accessed
	 * @throws InstantiationException if the PageModel object cannot be instantiated
	 * @throws InvocationTargetException if the constructor of the PageModel object throws an exception
	 */
	public <T extends PageModel<? super T>> T testPageModel(Class<T> clazz) {
		try {
			getEvaluator().logEvent(TestEvaluator.TEST_EXECUTE,
					"test model", op -> op
					.addValue("expected", clazz.getSimpleName())
					.addValue("model", page.getClass().getSimpleName()));
			for(Constructor<?> c : clazz.getConstructors()){
				if(c.getParameterTypes().length == 1 && c.getParameterTypes()[0].isAssignableFrom(testContext.getClass())){
					return (T)c.newInstance(testContext);
				}
			}
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
			throw testContext.createException(JsonBuilder.toMap(getEvaluator().getExecuteEvent(
					"test model", op -> op
					.addValue("expected", clazz.getSimpleName())
					.addValue("model", page.getClass().getSimpleName()))), ex);
		}
		throw testContext.createException(JsonBuilder.toMap(getEvaluator().getExecuteEvent(
				"test model", op -> op
				.addValue("expected", clazz.getSimpleName())
				.addValue("model", page.getClass().getSimpleName()))));
	}

	/**
	 * Navigates to the given URL and returns a PageModel object of the given class.
	 * @param url a String representing the URL to navigate to
	 * @param clazz a Class object representing the class of the PageModel to return
	 * @return a PageModel object or any of its subclasses
	 */
	public <T extends PageModel<? super T>> T navigateTo(String url, Class<T> clazz) {
		return navigateTo(url, clazz, DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS);
	}

	/**
	 * Navigates to the given URL and returns a PageModel object of the given class, waiting for the page to load for the given timeout.
	 * @param url a String representing the URL to navigate to
	 * @param clazz a Class object representing the class of the PageModel to return
	 * @param timeoutSec an int representing the timeout in seconds to wait for the page to load
	 * @return a PageModel object or any of its subclasses
	 */
	public <T extends PageModel<? super T>> T navigateTo(String url, Class<T> clazz, int timeoutSec) {
		T retPage = PageUtils.makeInstance(clazz, testContext);
		String className = clazz == null ? null : clazz.getSimpleName();
		return getEvaluator().testExecute(
				"navigate", op -> op
						.addValue("value", url)
						.addValue("expected",className)
						.addValue("model",getModelName()),
				() -> {
					testContext.getDriver().navigate().to(url);
					PageUtils.waitForModelDisplayed(retPage, timeoutSec);
				},
				retPage, page.getContext());
	}

	/**
	 * Navigates to the given URL and returns a PageModel object of the given class, waiting for the page to load for the given timeout and executing the given page flow.
	 * @param url a String representing the URL to navigate to
	 * @param clazz a Class object representing the class of the PageModel to return
	 * @param timeoutSec an int representing the timeout in seconds to wait for the page to load
	 * @param flow a Consumer object representing the page flow to execute
	 * @return a PageModel object or any of its subclasses
	 */
	public <T extends PageModel<? super T>> T navigateFlowTo(String url, Class<T> clazz, int timeoutSec, Consumer<PageFlow<T>> flow) {
		T retPage = PageUtils.makeInstance(clazz, testContext);
		String className = clazz == null ? null : clazz.getSimpleName();
		return getEvaluator().testExecute(
				"navigate", op -> op
						.addValue("value", url)
						.addValue("expected",className)
						.addValue("model",getModelName()),
				() -> {
					PageFlow<T> pageFlow = new PageFlow<>(page.getContext(), clazz);
					flow.accept(pageFlow);
					testContext.getDriver().navigate().to(url);
					pageFlow.testPaths(timeoutSec);
				},
				retPage, page.getContext());
	}
}