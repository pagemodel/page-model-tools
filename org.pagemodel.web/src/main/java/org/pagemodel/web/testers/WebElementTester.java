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

import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.pagemodel.core.testers.StringTester;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.json.JsonBuilder;
import org.pagemodel.web.LocatedWebElement;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.utils.Screenshot;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * @param <R> return type for method chaining
 * @param <N> return type for clicking element
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class WebElementTester<R, N extends PageModel<? super N>> extends HasPageBounds {

	protected R returnObj;
	protected Callable<WebElement> elementRef;
	protected PageModel<?> page;
	protected ClickAction<?, N> clickAction;
	protected boolean screenshotOnError = true;
	protected TestEvaluator testEvaluator;

	public WebElementTester(R returnObj, ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		this.returnObj = returnObj;
		this.elementRef = clickAction.elementRef;
		this.page = clickAction.page;
		this.clickAction = clickAction;
		this.testEvaluator = testEvaluator;
	}

	public WebElementTester(ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		this((R)clickAction.page, clickAction, testEvaluator);
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	protected void setEvaluator(TestEvaluator testEvaluator){
		this.testEvaluator = testEvaluator;
		this.clickAction.setTestEvaluator(testEvaluator);
	}

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

	protected R getReturnObj() {
		return returnObj;
	}

	protected void setReturnObj(R returnObj) {
		this.returnObj = returnObj;
	}

	public R exists() {
		return getEvaluator().testCondition("exists", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement(),
				getReturnObj(), page.getContext());
	}

	public R notExists() {
		return getEvaluator().testCondition("not exists", op -> op
						.addValue("element", getElementJson()),
				() -> !callRef().hasElement(),
				getReturnObj(), page.getContext());
	}

	public R isSelected() {
		return getEvaluator().testCondition("selected", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement() && callRef().isSelected(),
				getReturnObj(), page.getContext());
	}

	public R notSelected() {
		return getEvaluator().testCondition("not selected", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement() && !callRef().isSelected(),
				getReturnObj(), page.getContext());
	}

	public R isFocused() {
		return getEvaluator().testCondition("focused", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement() && page.getContext().getDriver().switchTo().activeElement().equals(callRef().getElement()),
				getReturnObj(), page.getContext());
	}

	public R notFocused() {
		return getEvaluator().testCondition("not focused", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement() && !page.getContext().getDriver().switchTo().activeElement().equals(callRef().getElement()),
				getReturnObj(), page.getContext());
	}

	public R isEnabled() {
		return getEvaluator().testCondition("enabled", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement() && callRef().isEnabled(),
				getReturnObj(), page.getContext());
	}

	public R notEnabled() {
		return getEvaluator().testCondition("not enabled", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement() && !callRef().isEnabled(),
				getReturnObj(), page.getContext());
	}

	public R isDisplayed() {
		return getEvaluator().testCondition("displayed", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement() && callRef().isDisplayed(),
				getReturnObj(), page.getContext());
	}

	public R notDisplayed() {
		return getEvaluator().testCondition("not displayed", op -> op
						.addValue("element", getElementJson()),
				() -> !(callRef().hasElement() && callRef().isDisplayed()),
				getReturnObj(), page.getContext());
	}

	public R isClickable() {
		return getEvaluator().testCondition("clickable", op -> op
						.addValue("element", getElementJson()),
				() -> ExpectedConditions.and((ExpectedCondition<Boolean>) driver -> callRef().hasElement(),
						ExpectedConditions.elementToBeClickable(callRef()))
						.apply(page.getContext().getDriver()),
				getReturnObj(), page.getContext());
	}

	public R notClickable() {
		return getEvaluator().testCondition("not clickable", op -> op
						.addValue("element", getElementJson()),
				() -> ExpectedConditions.and((ExpectedCondition<Boolean>) driver -> callRef().hasElement(),
						ExpectedConditions.not(ExpectedConditions.elementToBeClickable(callRef())))
						.apply(page.getContext().getDriver()),
				getReturnObj(), page.getContext());
	}

	public StringTester<R> text() {
		getEvaluator().setSourceFindEvent("text", op -> op.addValue("element", getElementJson()));
		return new StringTester<>(() -> callRef().getText() == null ? null : callRef().getText().trim().replaceAll("\\s+", " "), getReturnObj(), page.getContext(), getEvaluator());
	}

	public StringTester<R> tagName() {
		getEvaluator().setSourceFindEvent("tag name", op -> op.addValue("element", getElementJson()));
		return new StringTester<>(() -> callRef().getTagName() == null ? null : callRef().getTagName().toLowerCase(), getReturnObj(), page.getContext(), getEvaluator());
	}

	public StringTester<R> attribute(String attribute) {
		getEvaluator().setSourceFindEvent("text", op -> op
				.addValue("value", attribute)
				.addValue("element", getElementJson()));
		return new StringTester<>(() -> callRef().getAttribute(attribute), getReturnObj(), page.getContext(), getEvaluator());
	}

	public StringTester<R> cssValue(String string) {
		getEvaluator().setSourceFindEvent("css value", op -> op
				.addValue("value", string)
				.addValue("element", getElementJson()));
		return new StringTester<>(() -> callRef().getCssValue(string), getReturnObj(), page.getContext(), getEvaluator());
	}

	public DimensionTester<R> size() {
		getEvaluator().setSourceFindEvent("size", op -> op
				.addValue("element", getElementJson()));
		return new DimensionTester<>(() -> callRef().getSize(), getReturnObj(), page.getContext(), getEvaluator());
	}

	public RectangleTester<R> location() {
		getEvaluator().setSourceFindEvent("location", op -> op
				.addValue("element", getElementJson()));
		return new RectangleTester<>(() -> callRef().getRect(), getReturnObj(), page.getContext(), getEvaluator());
	}

	public R clearText() {
		return getEvaluator().testExecute("clear text", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().clear(),
				getReturnObj(), page.getContext());
	}

	public R sendKeys(CharSequence... keys) {
		return doSendKeys(keys);
	}

	public WebActionTester<R> sendKeysAnd(CharSequence... keys) {
		doSendKeys(keys);
		return new WebActionTester<>(page.getContext(), page, this, getEvaluator());
	}

	protected R doSendKeys(CharSequence... keys) {
		String keyStr = Arrays.toString(keys);
		final String finalKeyStr = keyStr.substring(1,keyStr.length()-1);
		return getEvaluator().testExecute("send keys", op -> op
						.addValue("value", finalKeyStr)
						.addValue("element",getElementJson()),
				() -> callRef().sendKeys(keys),
				getReturnObj(), page.getContext());
	}

	public N click() {
		getEvaluator().logEvent(TestEvaluator.TEST_EXECUTE,
				"click", op -> op
						.addValue("element", getElementJson()));
		return clickAction.click(null);
	}

	public WebActionTester<R> clickAnd() {
		getEvaluator().logEvent(TestEvaluator.TEST_EXECUTE,
				"click and", op -> op
						.addValue("element", getElementJson()));
		clickAction.doClick(null);
		return new WebActionTester<>(page.getContext(), page, this, getEvaluator());
	}

	public R takeScreenshot(String filename, int padding){
		Screenshot.takeScreenshot(page.getContext().getDriver(), callRef().getRect(), padding, filename, false);
		return returnObj;
	}

	public R takeScreenshot(String filename){
		return takeScreenshot(filename, 0);
	}

	public WebElementWait<R, N> waitFor() {
		return new WebElementWait<>(clickAction, new WebTestEvaluator.Wait(page.getContext(), WebElementWait.DEFAULT_WAIT_SEC));
	}

	public WebElementRefresh<R, N> waitAndRefreshFor() {
		return new WebElementRefresh<>(getReturnObj(), clickAction, new WebTestEvaluator.WaitAndRefresh<>(page.getContext(), WebElementRefresh.DEFAULT_WAIT_SEC, getReturnObj(), page));
	}

	protected Map<String,Object> getElementJson() {
		try {
			return callRef().getElementJson(page);
		} catch (Throwable t) {
			return JsonBuilder.object().toMap();
		}
	}

	@Override
	protected Rectangle getBounds() {
		return location().getBounds();
	}

	/**
	 * @param <R> return type for method chaining
	 * @author Matt Stevenson [matt@pagemodel.org]
	 */
	public static class WebElementWait<R, N extends PageModel<? super N>> extends WebElementTester<R, N> {
		public static int DEFAULT_WAIT_SEC = 10;

		public WebElementWait(ClickAction<?, N> clickAction, WebTestEvaluator.Wait testEvaluator) {
			super(clickAction, testEvaluator);
			clickAction.setTestEvaluator(testEvaluator);
		}

		@Override
		protected WebTestEvaluator.Wait getEvaluator() {
			return (WebTestEvaluator.Wait)testEvaluator;
		}

		public void setEvaluator(TestEvaluator testEvaluator) {
			if(!(testEvaluator instanceof WebTestEvaluator.Wait)){
				throw new IllegalArgumentException("TestEvaluator must be Wait, got: " + testEvaluator);
			}
			super.setEvaluator(testEvaluator);
		}

		public WebElementWait<R, N> withTimeout(int waitSec) {
			getEvaluator().withTimeout(waitSec);
			return this;
		}
	}

	/**
	 * @param <R> return type for method chaining
	 * @author Matt Stevenson [matt@pagemodel.org]
	 */
	public static class WebElementRefresh<R, N extends PageModel<? super N>> extends WebElementTester<R,N> {
		public static int DEFAULT_WAIT_SEC = 10;

		public WebElementRefresh(R returnObj, ClickAction<?, N> clickAction, WebTestEvaluator.WaitAndRefresh<R> testEvaluator) {
			super(returnObj, clickAction, testEvaluator);
			clickAction.setTestEvaluator(testEvaluator);
		}

		protected WebTestEvaluator.WaitAndRefresh<R> getEvaluator(){
			return (WebTestEvaluator.WaitAndRefresh<R>)testEvaluator;
		}

		public void setEvaluator(TestEvaluator testEvaluator) {
			if(!(testEvaluator instanceof WebTestEvaluator.WaitAndRefresh)){
				throw new IllegalArgumentException("TestEvaluator must be WaitAndRefresh, got: " + testEvaluator);
			}
			super.setEvaluator(testEvaluator);
		}

		public WebElementRefresh<R, N> withPageSetup(Function<R, R> pageSetupFunction) {
			getEvaluator().withPageSetup(pageSetupFunction);
			return this;
		}

		public WebElementRefresh<R, N> withTimeout(int waitSec) {
			getEvaluator().withTimeout(waitSec);
			return this;
		}
	}
}
