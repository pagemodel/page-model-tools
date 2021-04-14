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

import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.PageUtils;
import org.pagemodel.web.WebTestContext;
import org.pagemodel.web.paths.PageFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class WebActionTester<R> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final PageModel page;
	private final WebTestContext testContext;
	private final WebElementTester<R, ?> keySender;
	private TestEvaluator testEvaluator;

	public WebActionTester(WebTestContext testContext, PageModel page, WebElementTester<R, ?> keySender, TestEvaluator testEvaluator) {
		this.testContext = testContext;
		this.page = page;
		this.keySender = keySender;
		this.testEvaluator = testEvaluator;
	}

	public AlertTester<R> testAlert() {
		return new AlertTester<>(page, keySender.getReturnObj(), testContext, getEvaluator());
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	public <R extends PageModel<? super R>> R expectRedirect(Class<R> returnPageClass) {
		R retPageInst = null;
		try{
			retPageInst = PageUtils.makeInstance(returnPageClass, testContext);
		}catch (Throwable t){}
		final R retPage = retPageInst;
		return getEvaluator().testRun(
				TestEvaluator.TEST_ASSERT,
				() -> "redirect: from [" + page.getClass().getSimpleName() + "] to [" + returnPageClass.getSimpleName() + "]",
				() -> PageUtils.waitForModelDisplayed(retPage).onPageLoad(),
				retPage, testContext);
	}

	public R noRedirect() {
		//TODO: why do we have to cast? intellij shows it as redundant, but will not compile without
		return (R)getEvaluator().testRun(
				TestEvaluator.TEST_ASSERT,
				() -> "no redirect: [" + page.getClass().getSimpleName() + "]",
				() -> PageUtils.waitForModelDisplayed(page),
				keySender.getReturnObj(), testContext);
	}

	public R sendKeys(CharSequence... keys) {
		return keySender.sendKeys(keys);
	}

	public WebActionTester<R> sendKeysAnd(CharSequence... keys) {
		return keySender.sendKeysAnd(keys);
	}

	public <N extends PageModel<? super N>> N testFlow(PageFlow<N> flow) {
		return flow.testPaths();
	}

	public <N extends PageModel<? super N>> PageFlow<N> testPathsToPage(Class<N> navPageClass) {
		return new PageFlow<>(testContext, (Class<? extends PageModel<?>>) page.getClass());
	}
}
