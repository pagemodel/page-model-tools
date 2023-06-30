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
import org.pagemodel.core.testers.ComparableTester;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.ThrowingFunction;
import org.pagemodel.core.utils.json.JsonBuilder;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.PageUtils;
import org.pagemodel.web.WebTestContext;
import org.pagemodel.web.paths.PageFlow;
import org.pagemodel.web.utils.RectangleUtils;
import org.pagemodel.web.utils.RefreshTracker;
import org.pagemodel.web.utils.Screenshot;
import org.pagemodel.web.utils.WindowHelper;

import javax.imageio.ImageIO;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.pagemodel.web.PageUtils.DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class PageTester<P extends PageModel<? super P>> extends PageTesterBase<P> {
	protected static final int DEFAULT_SCRIPT_TIMEOUT_SECONDS = 20;

	public PageTester(P page, WebTestContext testContext, TestEvaluator testEvaluator) {
		super(page, testContext, testEvaluator);
	}

	public WebElementTester<P, P> testFocusedElement() {
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"focused element", op -> op
						.addValue("model", page.getClass().getName()));
		return new WebElementTester<>(ClickAction.make(() -> testContext.getDriver().switchTo().activeElement(), page, getEvaluator()), getEvaluator());
	}

	public WebElementTester<P, P> testHTMLElement() {
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"page html", op -> op
						.addValue("model", page.getClass().getName()));
		return new WebElementTester<>(ClickAction.make(() -> testContext.getDriver().findElement(By.tagName("html")), page, getEvaluator()), getEvaluator());
	}

	public WebElementTester<P, P> testBodyElement() {
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"page body", op -> op
						.addValue("model", page.getClass().getName()));
		return new WebElementTester<>(ClickAction.make(() -> testContext.getDriver().findElement(By.tagName("body")), page, getEvaluator()), getEvaluator());
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

	private String getModelName(){
		return page == null ? null : page.getClass().getSimpleName();
	}

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

	public PageWait<P> waitFor() {
		return new PageWait<>(page, testContext, new WebTestEvaluator.Wait(testContext, WebElementTester.WebElementWait.DEFAULT_WAIT_SEC));
	}

	public PageTesterBase<P> waitAndRefreshFor() {
		return new PageTesterBase<>(page, testContext, new WebTestEvaluator.WaitAndRefresh<>(testContext, WebElementTester.WebElementWait.DEFAULT_WAIT_SEC, page, page));
	}

	public RectangleTester<P> testBrowserSize() {
		Dimension dim = testContext.getDriver().manage().window().getSize();
		Rectangle rectangle = new Rectangle(0, 0, dim.getHeight(), dim.getWidth());
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"window size", op -> op
						.addObject("size", RectangleUtils.rectangleJson(rectangle)));
		return new RectangleTester<>(() -> rectangle, page, testContext, getEvaluator());
	}

	public ComparableTester<Integer,P> testViewHeight() {
		return executeJavaScript("return window.innerHeight;", 5, false).testReturnLong().transform(Long::intValue);
	}

	public ComparableTester<Integer,P> testViewWidth() {
		return executeJavaScript("return window.innerWidth;", 5, false).testReturnLong().transform(Long::intValue);
	}

	public RectangleTester<P> testLocation(int x, int y, int width, int height) {
		Rectangle rectangle = new Rectangle(x, y, height, width);
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"location", op -> op
						.addValue("location", RectangleUtils.rectangleJson(rectangle)));
		return new RectangleTester<>(() -> rectangle, page, testContext, getEvaluator());
	}

	public RectangleTester<P> testLocation(Rectangle rect){
		return testLocation(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	public RectangleTester<P> testLocation(String storedRectKey){
		return testLocation(page.getContext().<Rectangle>load(storedRectKey));
	}

	public RectangleTester<P> testLocation(ThrowingFunction<P,HasPageBounds,?> getBounds){
		return testLocation(ThrowingFunction.unchecked(getBounds).apply(page).getBounds());
	}

	public P setWindowSize(int width, int height) {
		return getEvaluator().testExecute(
				"set window size", op -> op
						.addValue("value", width + " X " + height)
						.addValue("model",getModelName()),
				() -> testContext.getDriver().manage().window().setSize(new Dimension(width, height)),
				page, page.getContext());
	}

	public P setWindowSizeToInclude(int setWidth, int setHeight, ThrowingFunction<P,HasPageBounds,?> getBounds){
		Dimension dim = testContext.getDriver().manage().window().getSize();
		int newWidth = dim.getWidth();
		if(setWidth != 0 && setWidth != dim.getWidth()){
			newWidth = setWidth;
		}
		int newHeight = dim.getHeight();
		if(setHeight != 0 && setHeight != dim.getHeight()){
			newHeight = setHeight;
		}
		if(newWidth != dim.getWidth() || newHeight != dim.getHeight()){
			setWindowSize(newWidth, newHeight);
		}
		WindowHelper.storeBrowserOffset(testContext);
		Rectangle rectangle = RectangleUtils.pad(ThrowingFunction.unchecked(getBounds).apply(page).getBounds(),
				0, testContext.load("wtcWidthOffset"), testContext.load("wtcHeightOffset"), 0,
				getEvaluator());
		return setWindowSize(Math.max(setWidth, rectangle.x + rectangle.width), Math.max(setHeight, rectangle.y + rectangle.height));
	}

	public P setWindowSizeToInclude(ThrowingFunction<P,HasPageBounds,?> getBounds){
		return setWindowSizeToInclude(0, 0, getBounds);
	}

	public P maximizeWindow() {
		return getEvaluator().testExecute(
				"maximize window", op -> op
						.addValue("model", getModelName()),
				() -> testContext.getDriver().manage().window().maximize(),
				page, testContext);
	}

	public P fullscreenWindow() {
		return getEvaluator().testExecute(
				"fullscreen window", op -> op
						.addValue("model", getModelName()),
				() -> testContext.getDriver().manage().window().fullscreen(),
				page, testContext);
	}

	public P setWindowPosition(int x, int y) {
		return getEvaluator().testExecute(
				"set window position", op -> op
						.addValue("value", x + ", " + y)
						.addValue("model",getModelName()),
				() -> testContext.getDriver().manage().window().setPosition(new Point(x, y)),
				page, testContext);
	}

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

	public P takeScreenshot(String filePrefix) {
		Screenshot.takeScreenshot(testContext, filePrefix + "_" + page.getClass().getSimpleName());
		return page;
	}

	public ImageAnnotator<P> editScreenshot(){
		String filename = Screenshot.takeScreenshot(testContext, "edit");
		return new ImageAnnotator<>(new Point(0,0), () -> ImageIO.read(new File(filename)), page, testContext, getEvaluator());
	}

	public P logPageSource(String...messages){
		PageUtils.logPageSource(page.getContext(), TestEvaluator.TEST_LOG, messages);
		return page;
	}

	public P refreshPage() {
		return RefreshTracker.refreshPage(page);
	}

	public <T extends PageModel<? super T>> T switchToDefaultContent(Class<T> clazz) {
		String className = clazz == null ? null : clazz.getSimpleName();
		return getEvaluator().testExecute(
				"switch to default content", op -> op
						.addValue("expected", className)
						.addValue("model", getModelName()),
				() -> page.getContext().getDriver().switchTo().defaultContent(),
				page.testPage().testPageModel(clazz), page.getContext());
	}

	public <T extends PageModel<? super T>> T switchToParentFrame(Class<T> clazz) {
		String className = clazz == null ? null : clazz.getSimpleName();
		return getEvaluator().testExecute(
				"switch to parent frame", op -> op
						.addValue("expected", className)
						.addValue("model", getModelName()),
				() -> page.getContext().getDriver().switchTo().parentFrame(),
				page.testPage().testPageModel(clazz), page.getContext());
	}

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

	public <T extends PageModel<? super T>> T navigateTo(String url, Class<T> clazz) {
		return navigateTo(url, clazz, DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS);
	}

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
