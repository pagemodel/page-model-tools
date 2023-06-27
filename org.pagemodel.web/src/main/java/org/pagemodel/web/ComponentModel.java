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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.testers.ClickAction;
import org.pagemodel.web.testers.WebElementTester;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * This class represents a reusable component of a page. It is treated as both a PageModel and WebElementTester.
 * @param <R> the return type of the WebElementTester methods
 * @param <N> the type of the parent page model
 * @param <C> the type of the component model
 */
public abstract class ComponentModel<R, N extends PageModel<? super N>, C extends ComponentModel<? super R, N, ? super C>> extends WebElementTester<R, N> implements ModelBase {

	/**
	 * Constructor for a ComponentModel with a return object, click action, and test evaluator.
	 * @param returnObj the return object
	 * @param clickAction the click action
	 * @param testEvaluator the test evaluator
	 */
	public ComponentModel(R returnObj, ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		super(returnObj, clickAction, testEvaluator);
	}

	/**
	 * Constructor for a ComponentModel with a click action and test evaluator.
	 * @param clickAction the click action
	 * @param testEvaluator the test evaluator
	 */
	public ComponentModel(ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		super(clickAction, testEvaluator);
	}

	/**
	 * Finds a single component element by name and By selector.
	 * @param name the name of the element
	 * @param by the By selector
	 * @return the LocatedWebElement of the element found
	 */
	protected LocatedWebElement findComponentElement(String name, By by) {
		WebElement element = callRef();
		try {
			WebElement el = element.findElement(by);
			return new LocatedWebElement(el, name, by, this, element);
		} catch (Exception e) {
			return new LocatedWebElement(null, name, by, this,  element);
		}
	}

	/**
	 * Finds a list of component elements by name and By selector.
	 * @param name the name of the elements
	 * @param by the By selector
	 * @param <T> the type of the LocatedWebElement
	 * @return a list of LocatedWebElements of the elements found
	 */
	protected <T extends LocatedWebElement> List<? super T> findComponentElements(String name, By by) {
		WebElement element = callRef();
		try {
			return Arrays.asList(element.findElements(by).stream().map(el -> new LocatedWebElement(el, name, by, this, element)).toArray(WebElement[]::new));
		} catch (Exception e) {
			return Arrays.asList(new LocatedWebElement(null, name, by, this, element));
		}
	}

	/**
	 * Finds a single page element by name and By selector.
	 * @param name the name of the element
	 * @param by the By selector
	 * @return the LocatedWebElement of the element found
	 */
	protected LocatedWebElement findPageElement(String name, By by) {
		WebElement element = callRef();
		try {
			WebElement el = page.getContext().getDriver().findElement(by);
			return new LocatedWebElement(el, name, by, this, element);
		} catch (Exception e) {
			return new LocatedWebElement(null, name, by, this, element);
		}
	}

	/**
	 * Finds a list of page elements by name and By selector.
	 * @param name the name of the elements
	 * @param by the By selector
	 * @param <T> the type of the LocatedWebElement
	 * @return a list of LocatedWebElements of the elements found
	 */
	protected <T extends LocatedWebElement> List<? super T> findPageElements(String name, By by) {
		WebElement element = callRef();
		try {
			return Arrays.asList(page.getContext().getDriver().findElements(by).stream().map(el -> new LocatedWebElement(el, name, by, this, element)).toArray(WebElement[]::new));
		} catch (Exception e) {
			return Arrays.asList(new LocatedWebElement(null, name, by, this, element));
		}
	}

	/**
	 * Overrides the isDisplayed method of WebElementTester to also check if the component model is displayed.
	 * @return the return object of the isDisplayed method
	 */
	@Override
	public R isDisplayed() {
		R ret = super.isDisplayed();
		if(!modelDisplayed()){
			logModelDisplayed();
		}
		return ret;
	}

	/**
	 * Checks if the component model is displayed.
	 * @return true if the model is displayed, false otherwise
	 */
	protected boolean modelDisplayed() {
		WebTestContext context = page.getContext();
		TestEvaluator contextEvaluator = context.getEvaluator();
		TestEvaluator.NoException eval = new TestEvaluator.NoException(getEvaluator());
		this.setEvaluator(eval);
		try {
			context.setEvaluator(eval);
			testModelDisplayed().accept(asSection());
			return eval.getTestStatus();
		}finally{
			context.setEvaluator(contextEvaluator);
			setEvaluator(eval.getInnerEvaluator());
		}
	}

	/**
	 * Logs that the component model is displayed.
	 */
	protected void logModelDisplayed() {
		testModelDisplayed().accept(asSection());
	}

	/**
	 * Abstract method to be implemented by subclasses to return a Consumer that tests if the component model is displayed.
	 * @param <Z> the type of the component model
	 * @return a Consumer that tests if the component model is displayed
	 */
	protected <Z extends ComponentModel<Z, N, ? extends C>> Consumer<Z> testModelDisplayed(){
		return page -> {};
	}

	/**
	 * Abstract method to be implemented by subclasses to return a new instance of the component model.
	 * @param <Z> the type of the component model
	 * @return a new instance of the component model
	 */
	abstract protected <Z extends ComponentModel<Z, N, ? extends C>> Z asSection();
}