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
import org.pagemodel.web.LocatedWebElement;
import org.pagemodel.web.PageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * @param <R> return type for method chaining
 * @param <N> return type for clicking element
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class WebElementTester<R, N extends PageModel<? super N>> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected R returnObj;
	protected Callable<WebElement> elementRef;
	protected PageModel<?> page;
	protected ClickAction<?, N> clickAction;
	protected boolean screenshotOnError = true;
	private TestEvaluator testEvaluator;

	public WebElementTester(R returnObj, ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		this.returnObj = returnObj;
		this.elementRef = clickAction.elementRef;
		this.page = clickAction.page;
		this.clickAction = clickAction;
		this.testEvaluator = testEvaluator;
	}

	public WebElementTester(ClickAction<?, N> clickAction) {
		this((R)clickAction.page, clickAction, clickAction.page.getEvaluator());
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	protected void setEvaluator(TestEvaluator testEvaluator){
		this.testEvaluator = testEvaluator;
	}

	protected LocatedWebElement callRef() {
		try {
			WebElement el = elementRef.call();
			if (el == null) {
				return new LocatedWebElement(null, null, null);
			}
			if (LocatedWebElement.class.isAssignableFrom(el.getClass())) {
				return (LocatedWebElement) el;
			} else {
				return new LocatedWebElement(el, null, null);
			}
		} catch (Throwable ex) {
			return new LocatedWebElement(null, null, null);
		}
	}

	protected R getReturnObj() {
		return returnObj;
	}

	protected void setReturnObj(R returnObj) {
		this.returnObj = returnObj;
	}

	public R exists() {
		return getEvaluator().testCondition(() -> "exists: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> callRef().hasElement(), getReturnObj(), page.getContext());
	}

	public R notExists() {
		return getEvaluator().testCondition(() -> "not exists: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> !callRef().hasElement(), getReturnObj(), page.getContext());
	}

	public R isSelected() {
		return getEvaluator().testCondition(() -> "selected: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> callRef().hasElement() && callRef().isSelected(), getReturnObj(), page.getContext());
	}

	public R notSelected() {
		return getEvaluator().testCondition(() -> "not selected: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> callRef().hasElement() && !callRef().isSelected(), getReturnObj(), page.getContext());
	}

	public R isFocused() {
		return getEvaluator().testCondition(() -> "focused: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> callRef().hasElement() && page.getContext().getDriver().switchTo().activeElement().equals(callRef().getElement()), getReturnObj(), page.getContext());
	}

	public R notFocused() {
		return getEvaluator().testCondition(() -> "not focused: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> callRef().hasElement() && !page.getContext().getDriver().switchTo().activeElement().equals(callRef().getElement()), getReturnObj(), page.getContext());
	}

	public R isEnabled() {
		return getEvaluator().testCondition(() -> "enabled: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> callRef().hasElement() && callRef().isEnabled(), getReturnObj(), page.getContext());
	}

	public R notEnabled() {
		return getEvaluator().testCondition(() -> "not enabled: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> callRef().hasElement() && !callRef().isEnabled(), getReturnObj(), page.getContext());
	}

	public R isDisplayed() {
		return getEvaluator().testCondition(() -> "displayed: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> callRef().hasElement() && callRef().isDisplayed(), getReturnObj(), page.getContext());
	}

	public R notDisplayed() {
		return getEvaluator().testCondition(() -> "not displayed: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> !(callRef().hasElement() && callRef().isDisplayed()), getReturnObj(), page.getContext());
	}

	public R isClickable() {
		return getEvaluator().testCondition(() -> "clickable: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> ExpectedConditions.and((ExpectedCondition<Boolean>) driver -> callRef().hasElement(),
						ExpectedConditions.elementToBeClickable(callRef()))
						.apply(page.getContext().getDriver()), getReturnObj(), page.getContext());
	}

	public R notClickable() {
		return getEvaluator().testCondition(() -> "not clickable: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> ExpectedConditions.and((ExpectedCondition<Boolean>) driver -> callRef().hasElement(),
						ExpectedConditions.not(ExpectedConditions.elementToBeClickable(callRef())))
						.apply(page.getContext().getDriver()), getReturnObj(), page.getContext());
	}

	public StringTester<R> text() {
		getEvaluator().setSourceDisplayRef(() -> "text: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]");
		return new StringTester<>(() -> callRef().getText(), getReturnObj(), page.getContext(), getEvaluator());
	}

	public StringTester<R> tagName() {
		getEvaluator().setSourceDisplayRef(() -> "tag name: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]");
		return new StringTester<>(() -> callRef().getTagName(), getReturnObj(), page.getContext(), getEvaluator());
	}

	public StringTester<R> attribute(String attribute) {
		getEvaluator().setSourceDisplayRef(() -> "attribute: name:[" + attribute + "], " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]");
		return new StringTester<>(() -> callRef().getAttribute(attribute), getReturnObj(), page.getContext(), getEvaluator());
	}

	public StringTester<R> cssValue(String string) {
		getEvaluator().setSourceDisplayRef(() -> "css value: name:[" + string + "], " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]");
		return new StringTester<>(() -> callRef().getCssValue(string), getReturnObj(), page.getContext(), getEvaluator());
	}

	public DimensionTester<R> size() {
		getEvaluator().setSourceDisplayRef(() -> "size: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]");
		return new DimensionTester<>(() -> callRef().getSize(), getReturnObj(), page.getContext(), getEvaluator());
	}

	public RectangleTester<R> location() {
		getEvaluator().setSourceDisplayRef(() -> "location: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]");
		return new RectangleTester<>(() -> callRef().getRect(), getReturnObj(), page.getContext(), getEvaluator());
	}

	public R clearText() {
		log.info(getEvaluator().getActionMessage(() -> "clear text: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]"));
		getEvaluator().quiet().testCondition(() -> "exists: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> callRef().hasElement(), getReturnObj(), page.getContext());
		callRef().clear();
		return getReturnObj();
	}

	public R sendKeys(CharSequence... keys) {
		doSendKeys(keys);
		return getReturnObj();
	}

	public WebActionTester<R> sendKeysAnd(CharSequence... keys) {
		doSendKeys(keys);
		return new WebActionTester<>(page.getContext(), page, this);
	}

	protected void doSendKeys(CharSequence... keys) {
		log.info(getEvaluator().getActionMessage(() -> "send keys: " + Arrays.toString(keys) + " to " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]"));
		getEvaluator().quiet().testCondition(() -> "exists: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> callRef().hasElement(), getReturnObj(), page.getContext());
		callRef().sendKeys(keys);
	}

	public N click() {
		log.info(getEvaluator().getActionMessage(() -> "click: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]"));
		return clickAction.click(() -> getEvaluator().quiet().testCondition(() -> "clickable: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> ExpectedConditions.and((ExpectedCondition<Boolean>) driver -> callRef().hasElement(),
						ExpectedConditions.elementToBeClickable(callRef()))
						.apply(page.getContext().getDriver()), getReturnObj(), page.getContext()));
	}

	public WebActionTester<R> clickAnd() {
		log.info(getEvaluator().getActionMessage(() -> "clickAnd: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]"));
		clickAction.doClick(() -> getEvaluator().quiet().testCondition(() -> "clickable: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> ExpectedConditions.and((ExpectedCondition<Boolean>) driver -> callRef().hasElement(),
						ExpectedConditions.elementToBeClickable(callRef()))
						.apply(page.getContext().getDriver()), getReturnObj(), page.getContext()));
		return new WebActionTester<>(page.getContext(), page, this);
	}

	public WebElementWait<R, N> waitFor() {
		return new WebElementWait<>(clickAction, new WebTestEvaluator.Wait(page.getContext(), WebElementWait.DEFAULT_WAIT_SEC));
	}

	public WebElementRefresh<R, N> waitAndRefreshFor() {
		return new WebElementRefresh<>(getReturnObj(), clickAction, new WebTestEvaluator.WaitAndRefresh<>(page.getContext(), WebElementRefresh.DEFAULT_WAIT_SEC, getReturnObj(), page));
	}

	protected String getElementDisplay() {
		try {
			return callRef().getElementDisplay();
		} catch (Throwable t) {
			return "element(null)";
		}
	}

	protected String getAttributeDisplay(String attribute) {
		try {
			LocatedWebElement el = callRef();
			if (!el.hasElement()) {
				return "attribute(" + attribute + ":[null])";
			}
			return "attribute(" + attribute + ":[" + el.getAttribute(attribute) + "])";
		} catch (Throwable t) {
			return "attribute(" + attribute + ":[null])";
		}
	}

	protected String getCSSValueDisplay(String cssValue) {
		try {
			LocatedWebElement el = callRef();
			if (!el.hasElement()) {
				return "cssValue(" + cssValue + ":[null])";
			}
			return "cssValue(" + cssValue + ":[" + el.getCssValue(cssValue) + "])";
		} catch (Throwable t) {
			return "cssValue(" + cssValue + ":[null])";
		}
	}

	protected String getLocationDisplay() {
		LocatedWebElement el = callRef();
		if (!el.hasElement()) {
			return "location(null)";
		}
		return "location(x1:[" + el.getRect().getX() + "], y1:[" + el.getRect().getX() + "], x2:[" + el.getRect().getX() + el.getRect().getWidth() + "], y2:[" + el.getRect().getY() + el.getRect().getHeight() + "])";
	}

	protected String getSizeDisplay() {
		LocatedWebElement el = callRef();
		if (!el.hasElement()) {
			return "size(null)";
		}
		return "size(width:[" + el.getSize().getWidth() + "], height:[" + el.getSize().getHeight() + "])";
	}

	/**
	 * @param <R> return type for method chaining
	 * @author Matt Stevenson <matt@pagemodel.org>
	 */
	public static class WebElementWait<R, N extends PageModel<? super N>> extends WebElementTester<R, N> {
		private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
		public static int DEFAULT_WAIT_SEC = 10;

		private WebTestEvaluator.Wait testEvaluator;

		public WebElementWait(ClickAction<?, N> clickAction, WebTestEvaluator.Wait testEvaluator) {
			super(clickAction);
			this.testEvaluator = testEvaluator;
		}

		@Override
		protected WebTestEvaluator.Wait getEvaluator() {
			return testEvaluator;
		}

		public WebElementWait<R, N> withTimeout(int waitSec) {
			getEvaluator().withTimeout(waitSec);
			return this;
		}
	}

	/**
	 * @param <R> return type for method chaining
	 * @author Matt Stevenson <matt@pagemodel.org>
	 */
	public static class WebElementRefresh<R, N extends PageModel<? super N>> extends WebElementTester<R,N> {
		private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
		public static int DEFAULT_WAIT_SEC = 10;

		private WebTestEvaluator.WaitAndRefresh<R> testEvaluator;

		public WebElementRefresh(R returnObj, ClickAction<?, N> clickAction, WebTestEvaluator.WaitAndRefresh<R> testEvaluator) {
			super(returnObj, clickAction, testEvaluator);
			this.testEvaluator = testEvaluator;
		}

		protected WebTestEvaluator.WaitAndRefresh<R> getEvaluator(){
			return testEvaluator;
		}

		public WebElementRefresh<R, N> withPageSetup(Function<R, R> pageSetupFunction) {
			testEvaluator.withPageSetup(pageSetupFunction);
			return this;
		}

		public WebElementRefresh<R, N> withTimeout(int waitSec) {
			testEvaluator.withTimeout(waitSec);
			return this;
		}
	}
}
