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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.pagemodel.core.testers.StringTester;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.json.JsonBuilder;
import org.pagemodel.web.LocatedWebElement;
import org.pagemodel.web.PageModel;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * @param <R> return type for method chaining
 * @param <N> return type for clicking element
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * This class wraps a selenium WebElement class and allows testing properties of the element.
 * It provides methods to test if the element exists, is selected, focused, enabled, displayed, clickable,
 * and to retrieve its text, tag name, attribute, css value, size, and location.
 * It also allows setting a TestEvaluator to evaluate the test conditions and a ClickAction to perform click actions on the element.
 * @param <R> the return type of the WebElementTester
 * @param <N> the type of the PageModel
 */
public class WebElementTester<R, N extends PageModel<? super N>> {

	protected R returnObj;
	protected Callable<WebElement> elementRef;
	protected PageModel<?> page;
	protected ClickAction<?, N> clickAction;
	protected boolean screenshotOnError = true;
	protected TestEvaluator testEvaluator;

	/**
	 * Constructs a new WebElementTester with the given return object, ClickAction, and TestEvaluator.
	 * @param returnObj the return object of the WebElementTester
	 * @param clickAction the ClickAction to perform click actions on the element
	 * @param testEvaluator the TestEvaluator to evaluate the test conditions
	 */
	public WebElementTester(R returnObj, ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		this.returnObj = returnObj;
		this.elementRef = clickAction.elementRef;
		this.page = clickAction.page;
		this.clickAction = clickAction;
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Constructs a new WebElementTester with the given ClickAction and TestEvaluator.
	 * The return object is set to the page of the ClickAction.
	 * @param clickAction the ClickAction to perform click actions on the element
	 * @param testEvaluator the TestEvaluator to evaluate the test conditions
	 */
	public WebElementTester(ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		this((R)clickAction.page, clickAction, testEvaluator);
	}

	/**
	 * Returns the TestEvaluator of the WebElementTester.
	 * @return the TestEvaluator of the WebElementTester
	 */
	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	/**
	 * Sets the TestEvaluator of the WebElementTester and the ClickAction.
	 * @param testEvaluator the TestEvaluator to set
	 */
	protected void setEvaluator(TestEvaluator testEvaluator){
		this.testEvaluator = testEvaluator;
		this.clickAction.setTestEvaluator(testEvaluator);
	}

	/**
	 * Calls the element reference and returns a LocatedWebElement.
	 * @return a LocatedWebElement representing the element
	 */
	protected LocatedWebElement callRef() {
		try {
			WebElement el = elementRef.call();
			if (el == null) {
				return new LocatedWebElement(null, null, (String)null, page, null);
			}
			if (LocatedWebElement.class.isAssignableFrom(el.getClass())) {
				return (LocatedWebElement) el;
			} else {
				return new LocatedWebElement(el, null, (String)null, page, null);
			}
		} catch (Throwable ex) {
			return new LocatedWebElement(null, null, (String)null, page, null);
		}
	}

	/**
	 * Returns the return object of the WebElementTester.
	 * @return the return object of the WebElementTester
	 */
	protected R getReturnObj() {
		return returnObj;
	}

	/**
	 * Sets the return object of the WebElementTester.
	 * @param returnObj the return object to set
	 */
	protected void setReturnObj(R returnObj) {
		this.returnObj = returnObj;
	}

	/**
	 * Tests if the element exists.
	 * @return the return object of the WebElementTester
	 */
	public R exists() {
		return getEvaluator().testCondition("exists", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement(),
				getReturnObj(), page.getContext());
	}

	/**
	 * Tests if the element does not exist.
	 * @return the return object of the WebElementTester
	 */
	public R notExists() {
		return getEvaluator().testCondition("not exists", op -> op
						.addValue("element", getElementJson()),
				() -> !callRef().hasElement(),
				getReturnObj(), page.getContext());
	}

	/**
	 * Tests if the element is selected.
	 * @return the return object of the WebElementTester
	 */
	public R isSelected() {
		return getEvaluator().testCondition("selected", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement() && callRef().isSelected(),
				getReturnObj(), page.getContext());
	}

	/**
	 * Tests if the element is not selected.
	 * @return the return object of the WebElementTester
	 */
	public R notSelected() {
		return getEvaluator().testCondition("not selected", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement() && !callRef().isSelected(),
				getReturnObj(), page.getContext());
	}

	/**
	 * Tests if the element is focused.
	 * @return the return object of the WebElementTester
	 */
	public R isFocused() {
		return getEvaluator().testCondition("focused", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement() && page.getContext().getDriver().switchTo().activeElement().equals(callRef().getElement()),
				getReturnObj(), page.getContext());
	}

	/**
	 * Tests if the element is not focused.
	 * @return the return object of the WebElementTester
	 */
	public R notFocused() {
		return getEvaluator().testCondition("not focused", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement() && !page.getContext().getDriver().switchTo().activeElement().equals(callRef().getElement()),
				getReturnObj(), page.getContext());
	}

	/**
	 * Tests if the element is enabled.
	 * @return the return object of the WebElementTester
	 */
	public R isEnabled() {
		return getEvaluator().testCondition("enabled", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement() && callRef().isEnabled(),
				getReturnObj(), page.getContext());
	}

	/**
	 * Tests if the element is not enabled.
	 * @return the return object of the WebElementTester
	 */
	public R notEnabled() {
		return getEvaluator().testCondition("not enabled", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement() && !callRef().isEnabled(),
				getReturnObj(), page.getContext());
	}

	/**
	 * Tests if the element is displayed.
	 * @return the return object of the WebElementTester
	 */
	public R isDisplayed() {
		return getEvaluator().testCondition("displayed", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement() && callRef().isDisplayed(),
				getReturnObj(), page.getContext());
	}

	/**
	 * Tests if the element is not displayed.
	 * @return the return object of the WebElementTester
	 */
	public R notDisplayed() {
		return getEvaluator().testCondition("not displayed", op -> op
						.addValue("element", getElementJson()),
				() -> !(callRef().hasElement() && callRef().isDisplayed()),
				getReturnObj(), page.getContext());
	}

	/**
	 * Tests if the element is clickable.
	 * @return the return object of the WebElementTester
	 */
	public R isClickable() {
		return getEvaluator().testCondition("clickable", op -> op
						.addValue("element", getElementJson()),
				() -> ExpectedConditions.and((ExpectedCondition<Boolean>) driver -> callRef().hasElement(),
						ExpectedConditions.elementToBeClickable(callRef()))
						.apply(page.getContext().getDriver()),
				getReturnObj(), page.getContext());
	}

	/**
	 * Tests if the element is not clickable.
	 * @return the return object of the WebElementTester
	 */
	public R notClickable() {
		return getEvaluator().testCondition("not clickable", op -> op
						.addValue("element", getElementJson()),
				() -> ExpectedConditions.and((ExpectedCondition<Boolean>) driver -> callRef().hasElement(),
						ExpectedConditions.not(ExpectedConditions.elementToBeClickable(callRef())))
						.apply(page.getContext().getDriver()),
				getReturnObj(), page.getContext());
	}

	/**
	 * Returns a StringTester to test the text of the element.
	 * @return a StringTester to test the text of the element
	 */
	public StringTester<R> text() {
		getEvaluator().setSourceFindEvent("text", op -> op.addValue("element", getElementJson()));
		return new StringTester<>(() -> callRef().getText() == null ? null : callRef().getText().trim().replaceAll("\\s+", " "), getReturnObj(), page.getContext(), getEvaluator());
	}

	/**
	 * Returns a StringTester to test the tag name of the element.
	 * @return a StringTester to test the tag name of the element
	 */
	public StringTester<R> tagName() {
		getEvaluator().setSourceFindEvent("tag name", op -> op.addValue("element", getElementJson()));
		return new StringTester<>(() -> callRef().getTagName() == null ? null : callRef().getTagName().toLowerCase(), getReturnObj(), page.getContext(), getEvaluator());
	}

	/**
	 * Returns a StringTester to test the attribute of the element with the given name.
	 * @param attribute the name of the attribute to test
	 * @return a StringTester to test the attribute of the element with the given name
	 */
	public StringTester<R> attribute(String attribute) {
		getEvaluator().setSourceFindEvent("text", op -> op
				.addValue("value", attribute)
				.addValue("element", getElementJson()));
		return new StringTester<>(() -> callRef().getAttribute(attribute), getReturnObj(), page.getContext(), getEvaluator());
	}

	/**
	 * Returns a StringTester to test the CSS value of the element with the given property name.
	 * @param string the name of the CSS property to test
	 * @return a StringTester to test the CSS value of the element with the given property name
	 */
	public StringTester<R> cssValue(String string) {
		getEvaluator().setSourceFindEvent("css value", op -> op
				.addValue("value", string)
				.addValue("element", getElementJson()));
		return new StringTester<>(() -> callRef().getCssValue(string), getReturnObj(), page.getContext(), getEvaluator());
	}

	/**
	 * Returns a DimensionTester to test the size of the element.
	 * @return a DimensionTester to test the size of the element
	 */
	public DimensionTester<R> size() {
		getEvaluator().setSourceFindEvent("size", op -> op
				.addValue("element", getElementJson()));
		return new DimensionTester<>(() -> callRef().getSize(), getReturnObj(), page.getContext(), getEvaluator());
	}

	/**
	 * Returns a RectangleTester to test the location of the element.
	 * @return a RectangleTester to test the location of the element
	 */
	public RectangleTester<R> location() {
		getEvaluator().setSourceFindEvent("location", op -> op
				.addValue("element", getElementJson()));
		return new RectangleTester<>(() -> callRef().getRect(), getReturnObj(), page.getContext(), getEvaluator());
	}

	/**
	 * Clears the text of the element.
	 * @return the return object of the WebElementTester
	 */
	public R clearText() {
		return getEvaluator().testExecute("clear text", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().clear(),
				getReturnObj(), page.getContext());
	}

	/**
	 * Sends the given keys to the element.
	 * @param keys the keys to send
	 * @return the return object of the WebElementTester
	 */
	public R sendKeys(CharSequence... keys) {
		return doSendKeys(keys);
	}

	/**
	 * Sends the given keys to the element and returns a WebActionTester to perform further actions.
	 * @param keys the keys to send
	 * @return a WebActionTester to perform further actions
	 */
	public WebActionTester<R> sendKeysAnd(CharSequence... keys) {
		doSendKeys(keys);
		return new WebActionTester<>(page.getContext(), page, this, getEvaluator());
	}

	/**
	 * Sends the given keys to the element.
	 * @param keys the keys to send
	 * @return the return object of the WebElementTester
	 */
	protected R doSendKeys(CharSequence... keys) {
		String keyStr = Arrays.toString(keys);
		final String finalKeyStr = keyStr.substring(1,keyStr.length()-1);
		return getEvaluator().testExecute("send keys", op -> op
						.addValue("value", finalKeyStr)
						.addValue("element",getElementJson()),
				() -> callRef().sendKeys(keys),
				getReturnObj(), page.getContext());
	}

	/**
	 * Clicks the element and returns the PageModel of the page after the click.
	 * @return the PageModel of the page after the click
	 */
	public N click() {
		getEvaluator().logEvent(TestEvaluator.TEST_EXECUTE,
				"click", op -> op
						.addValue("element", getElementJson()));
		return clickAction.click(null);
	}

	/**
	 * Clicks the element and returns a WebActionTester to perform further actions.
	 * @return a WebActionTester to perform further actions
	 */
	public WebActionTester<R> clickAnd() {
		getEvaluator().logEvent(TestEvaluator.TEST_EXECUTE,
				"click and", op -> op
						.addValue("element", getElementJson()));
		clickAction.doClick(null);
		return new WebActionTester<>(page.getContext(), page, this, getEvaluator());
	}

	/**
	 * Returns a WebElementWait to wait for the element to meet certain conditions.
	 * @return a WebElementWait to wait for the element to meet certain conditions
	 */
	public WebElementWait<R, N> waitFor() {
		return new WebElementWait<>(clickAction, new WebTestEvaluator.Wait(page.getContext(), WebElementWait.DEFAULT_WAIT_SEC));
	}

	/**
	 * Returns a WebElementRefresh to wait for the element to meet certain conditions and refresh the page.
	 * @return a WebElementRefresh to wait for the element to meet certain conditions and refresh the page
	 */
	public WebElementRefresh<R, N> waitAndRefreshFor() {
		return new WebElementRefresh<>(getReturnObj(), clickAction, new WebTestEvaluator.WaitAndRefresh<>(page.getContext(), WebElementRefresh.DEFAULT_WAIT_SEC, getReturnObj(), page));
	}

	/**
	 * Returns a Map of the JSON representation of the element.
	 * @return a Map of the JSON representation of the element
	 */
	protected Map<String,Object> getElementJson() {
		try {
			return callRef().getElementJson(page);
		} catch (Throwable t) {
			return JsonBuilder.object().toMap();
		}
	}


	/**
	 * This subclass of WebElementTester is used for waiting for an element to meet certain conditions before proceeding with the test.
	 *
	 * @param <R> the return type of the test method
	 * @param <N> the type of the page model being tested
	 */
	public static class WebElementWait<R, N extends PageModel<? super N>> extends WebElementTester<R, N> {

		/**
		 * The default wait time in seconds.
		 */
		public static int DEFAULT_WAIT_SEC = 10;

		/**
		 * Constructs a new WebElementWait object with the given ClickAction and WebTestEvaluator.Wait.
		 *
		 * @param clickAction the ClickAction to perform on the element
		 * @param testEvaluator the WebTestEvaluator.Wait to use for evaluating the element
		 */
		public WebElementWait(ClickAction<?, N> clickAction, WebTestEvaluator.Wait testEvaluator) {
			super(clickAction, testEvaluator);
			clickAction.setTestEvaluator(testEvaluator);
		}

		/**
		 * Returns the WebTestEvaluator.Wait used by this WebElementWait object.
		 *
		 * @return the WebTestEvaluator.Wait used by this WebElementWait object
		 */
		@Override
		protected WebTestEvaluator.Wait getEvaluator() {
			return (WebTestEvaluator.Wait)testEvaluator;
		}

		/**
		 * Sets the TestEvaluator used by this WebElementWait object.
		 *
		 * @param testEvaluator the TestEvaluator to set
		 * @throws IllegalArgumentException if the TestEvaluator is not a WebTestEvaluator.Wait
		 */
		public void setEvaluator(TestEvaluator testEvaluator) {
			if(!(testEvaluator instanceof WebTestEvaluator.Wait)){
				throw new IllegalArgumentException("TestEvaluator must be Wait, got: " + testEvaluator);
			}
			super.setEvaluator(testEvaluator);
		}

		/**
		 * Sets the timeout for this WebElementWait object.
		 *
		 * @param waitSec the timeout in seconds
		 * @return this WebElementWait object
		 */
		public WebElementWait<R, N> withTimeout(int waitSec) {
			getEvaluator().withTimeout(waitSec);
			return this;
		}
	}

	/**
	 * This subclass of WebElementTester is used for refreshing the page and re-evaluating the element after a certain action is performed.
	 *
	 * @param <R> the return type of the test method
	 * @param <N> the type of the page model being tested
	 */
	public static class WebElementRefresh<R, N extends PageModel<? super N>> extends WebElementTester<R,N> {

		/**
		 * The default wait time in seconds.
		 */
		public static int DEFAULT_WAIT_SEC = 10;

		/**
		 * Constructs a new WebElementRefresh object with the given return object, ClickAction, and WebTestEvaluator.WaitAndRefresh.
		 *
		 * @param returnObj the return object of the test method
		 * @param clickAction the ClickAction to perform on the element
		 * @param testEvaluator the WebTestEvaluator.WaitAndRefresh to use for evaluating the element
		 */
		public WebElementRefresh(R returnObj, ClickAction<?, N> clickAction, WebTestEvaluator.WaitAndRefresh<R> testEvaluator) {
			super(returnObj, clickAction, testEvaluator);
			clickAction.setTestEvaluator(testEvaluator);
		}

		/**
		 * Returns the WebTestEvaluator.WaitAndRefresh used by this WebElementRefresh object.
		 *
		 * @return the WebTestEvaluator.WaitAndRefresh used by this WebElementRefresh object
		 */
		protected WebTestEvaluator.WaitAndRefresh<R> getEvaluator(){
			return (WebTestEvaluator.WaitAndRefresh<R>)testEvaluator;
		}

		/**
		 * Sets the TestEvaluator used by this WebElementRefresh object.
		 *
		 * @param testEvaluator the TestEvaluator to set
		 * @throws IllegalArgumentException if the TestEvaluator is not a WebTestEvaluator.WaitAndRefresh
		 */
		public void setEvaluator(TestEvaluator testEvaluator) {
			if(!(testEvaluator instanceof WebTestEvaluator.WaitAndRefresh)){
				throw new IllegalArgumentException("TestEvaluator must be WaitAndRefresh, got: " + testEvaluator);
			}
			super.setEvaluator(testEvaluator);
		}

		/**
		 * Sets the page setup function for this WebElementRefresh object.
		 *
		 * @param pageSetupFunction the page setup function to set
		 * @return this WebElementRefresh object
		 */
		public WebElementRefresh<R, N> withPageSetup(Function<R, R> pageSetupFunction) {
			getEvaluator().withPageSetup(pageSetupFunction);
			return this;
		}

		/**
		 * Sets the timeout for this WebElementRefresh object.
		 *
		 * @param waitSec the timeout in seconds
		 * @return this WebElementRefresh object
		 */
		public WebElementRefresh<R, N> withTimeout(int waitSec) {
			getEvaluator().withTimeout(waitSec);
			return this;
		}
	}
}