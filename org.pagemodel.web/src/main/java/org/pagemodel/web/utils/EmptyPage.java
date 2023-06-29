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

package org.pagemodel.web.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.WebTestContext;
import org.pagemodel.web.testers.ClickAction;
import org.pagemodel.web.testers.WebElementTester;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class EmptyPage extends PageModel.DefaultPageModel<EmptyPage> {

	public EmptyPage(WebTestContext testContext) {
		super(testContext);
	}

	@Override
	public boolean modelDisplayed() {
		return true;
	}

	protected WebElement getElement(String name, By by) {
		return findPageElement( name, by);
	}

	private WebElementTester<EmptyPage, EmptyPage> testElement(String name, By by) {
		return new WebElementTester<>(this, ClickAction.make(() -> this.getElement(name, by), this, getEvaluator()), getEvaluator());
	}
}
