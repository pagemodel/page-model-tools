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
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public abstract class ComponentModel<R, N extends PageModel<? super N>, C extends ComponentModel<? super R, N, ? super C>> extends WebElementTester<R, N> implements ModelBase {

	public ComponentModel(R returnObj, ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		super(returnObj, clickAction, testEvaluator);
	}

	public ComponentModel(ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		super(clickAction, testEvaluator);
	}

	protected LocatedWebElement findComponentElement(String name, By by) {
		WebElement element = callRef();
		try {
			WebElement el = element.findElement(by);
			return new LocatedWebElement(el, name, by, this, element);
		} catch (Exception e) {
			return new LocatedWebElement(null, name, by, this,  element);
		}
	}

	protected <T extends LocatedWebElement> List<? super T> findComponentElements(String name, By by) {
		WebElement element = callRef();
		try {
			return Arrays.asList(element.findElements(by).stream().map(el -> new LocatedWebElement(el, name, by, this, element)).toArray(WebElement[]::new));
		} catch (Exception e) {
			return Arrays.asList(new LocatedWebElement(null, name, by, this, element));
		}
	}

	protected LocatedWebElement findPageElement(String name, By by) {
		WebElement element = callRef();
		try {
			WebElement el = page.getContext().getDriver().findElement(by);
			return new LocatedWebElement(el, name, by, this, element);
		} catch (Exception e) {
			return new LocatedWebElement(null, name, by, this, element);
		}
	}

	protected <T extends LocatedWebElement> List<? super T> findPageElements(String name, By by) {
		WebElement element = callRef();
		try {
			return Arrays.asList(page.getContext().getDriver().findElements(by).stream().map(el -> new LocatedWebElement(el, name, by, this, element)).toArray(WebElement[]::new));
		} catch (Exception e) {
			return Arrays.asList(new LocatedWebElement(null, name, by, this, element));
		}
	}

	@Override
	public R isDisplayed() {
		R ret = super.isDisplayed();
		if(!modelDisplayed()){
			logModelDisplayed();
		}
		return ret;
	}

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

	protected void logModelDisplayed() {
			testModelDisplayed().accept(asSection());
	}

	protected <Z extends ComponentModel<Z, N, ? extends C>> Consumer<Z> testModelDisplayed(){
		return page -> {};
	}

	abstract protected <Z extends ComponentModel<Z, N, ? extends C>> Z asSection();
}
