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
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.Unique;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.PageUtils;
import org.pagemodel.web.WebTestContext;
import org.pagemodel.web.testers.ClickAction;
import org.pagemodel.web.testers.WebElementTester;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class RefreshTracker extends PageModel.DefaultPageModel<RefreshTracker> {
	private String id;

	public static <T extends PageModel<? super T>> T refreshPage(T page) {
		page.getEvaluator().logEvent(TestEvaluator.TEST_EXECUTE, "refresh page", op -> op
				.addValue("model", page.getClass().getSimpleName()));
		TestEvaluator contextEval = page.getContext().getEvaluator();
		page.getContext().setEvaluator(contextEval.quiet());
		TestEvaluator origEval = page.getEvaluator();
		TestEvaluator quiet = origEval.quiet();
		PageUtils.trySetEvaluator(page, quiet);
		RefreshTracker rt = new RefreshTracker(page.getContext());
		rt.setTestEvaluator(quiet);
		T retPage = rt.addRefreshTrackingElement()
				.doAction(page::onPageLeave)
				.doAction(() -> page.getContext().getDriver().navigate().refresh())
				.testRefreshTrackingElement().waitFor().notExists()
				.doAction(() -> { PageUtils.waitForModelDisplayed(page); })
				.doAction(page::onPageLoad)
				.testPage().testPageModel((Class<T>)page.getClass());
		PageUtils.trySetEvaluator(retPage, origEval);
		PageUtils.trySetEvaluator(page, origEval);
		retPage.getContext().setEvaluator(contextEval);
		return retPage;
	}

	public RefreshTracker(WebTestContext testContext) {
		super(testContext);
	}

	public boolean modelDisplayed() {
		return true;
	}

	protected WebElement getRefreshTrackingElement() {
		return findPageElement("RefreshElement", By.id(id));
	}

	private WebElementTester<RefreshTracker, RefreshTracker> testRefreshTrackingElement() {
		return new WebElementTester<>(this, ClickAction.make(this::getRefreshTrackingElement, this, getEvaluator()), getEvaluator());
	}

	protected RefreshTracker addRefreshTrackingElement() {
		id = Unique.string("refresh-%s");
		return testPage().testJavaScriptWithReturn(
				"var iDiv = document.createElement('div');"
				+ "iDiv.id = '" + id + "';"
				+ "document.getElementsByTagName('body')[0].appendChild(iDiv);"
				+ "return iDiv;", 5)
				.testReturnElement().exists();
	}
}
