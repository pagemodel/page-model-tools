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

import org.pagemodel.web.WebTestContext;
import org.pagemodel.core.utils.Unique;
import org.pagemodel.web.PageUtils;
import org.pagemodel.web.testers.ClickAction;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.testers.WebElementTester;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class RefreshTracker extends PageModel.DefaultPageModel<RefreshTracker> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private String id;

	public static <T extends PageModel<? super T>> T refreshPage(T page) {
		log.info("Refreshing page [" + page.getContext().getDriver().getTitle() + "] after wait.");
		return new RefreshTracker(page.getContext())
				.addRefreshTrackingElement()
				.testRefreshTrackingElement().waitFor().exists()
				.doAction(page::onPageLeave)
				.doAction(() -> page.getContext().getDriver().navigate().refresh())
				.testRefreshTrackingElement().waitFor().notExists()
				.doAction(() -> { PageUtils.waitForModelDisplayed(page); })
				.doAction(page::onPageLoad)
				.testPage().testPageModel((Class<T>)page.getClass());
	}

	public RefreshTracker(WebTestContext testContext) {
		super(testContext);
	}

	public boolean modelDisplayed() {
		return true;
	}

	protected WebElement getRefreshTrackingElement() {
		return findPageElement(By.id(id));
	}

	private WebElementTester<RefreshTracker, RefreshTracker> testRefreshTrackingElement() {
		return new WebElementTester<>(ClickAction.make(this::getRefreshTrackingElement, this));
	}

	protected RefreshTracker addRefreshTrackingElement() {
		id = Unique.string("refresh-%s");
		log.info("Inserting <div id='" + id + "'/> to verify page refresh.");
		JavascriptExecutor jse = (JavascriptExecutor) getContext().getDriver();
		jse.executeScript("var iDiv = document.createElement('div');" +
				"iDiv.id = '" + id + "';" +
				"document.getElementsByTagName('body')[0].appendChild(iDiv);");
		return this;
	}
}
