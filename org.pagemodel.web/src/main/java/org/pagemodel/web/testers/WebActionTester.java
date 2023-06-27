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

import static org.pagemodel.web.PageUtils.DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * The WebActionTester class is responsible for testing web actions and interactions on a web page.
 * It contains methods for testing alerts, redirects, sending keys, and testing page flows.
 *
 * @param <R> the return type of the web action being tested
 */
public class WebActionTester<R> {

	private final PageModel page;
	private final WebTestContext testContext;
	private final WebElementTester<R, ?> keySender;
	private TestEvaluator testEvaluator;

	/**
	 * Constructs a new WebActionTester object with the given parameters.
	 *
	 * @param testContext  the web test context
	 * @param page         the page model to test
	 * @param keySender    the web element tester for sending keys
	 * @param testEvaluator the test evaluator for evaluating test results
	 */
	public WebActionTester(WebTestContext testContext, PageModel page, WebElementTester<R, ?> keySender, TestEvaluator testEvaluator) {
		this.testContext = testContext;
		this.page = page;
		this.keySender = keySender;
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Returns a new AlertTester object for testing alerts on the current page.
	 *
	 * @return a new AlertTester object
	 */
	public AlertTester<R> testAlert() {
		return new AlertTester<>(page, keySender.getReturnObj(), testContext, getEvaluator());
	}

	/**
	 * Returns the test evaluator for evaluating test results.
	 *
	 * @return the test evaluator
	 */
	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	/**
	 * Tests that the current page redirects to the specified page model class with the default timeout.
	 *
	 * @param returnPageClass the page model class to expect a redirect to
	 * @param <R>             the return type of the page model class
	 * @return the page model instance of the specified class
	 */
	public <R extends PageModel<? super R>> R expectRedirect(Class<R> returnPageClass) {
		return expectRedirect(returnPageClass, DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS);
	}

	/**
	 * Tests that the current page redirects to the specified page model class.
	 *
	 * @param returnPageClass the page model class to expect a redirect to
	 * @param timeoutSec      the timeout in seconds for waiting for the redirect
	 * @param <R>             the return type of the page model class
	 * @return the page model instance of the specified class
	 */
	public <R extends PageModel<? super R>> R expectRedirect(Class<R> returnPageClass, int timeoutSec) {
		R retPageInst = null;
		try{
			retPageInst = PageUtils.makeInstance(returnPageClass, testContext);
		}catch (Throwable t){}
		final R retPage = retPageInst;
		return getEvaluator().testRun(
				TestEvaluator.TEST_ASSERT,
				"expect redirect", op -> op
						.addValue("expected", returnPageClass.getSimpleName())
						.addValue("model", page.getClass().getSimpleName()),
				() -> PageUtils.waitForModelDisplayed(retPage, timeoutSec).onPageLoad(),
				retPage, testContext);
	}

	/**
	 * Tests that the current page does not redirect.
	 *
	 * @return the page model instance of the current page
	 */
	public R noRedirect() {
		//TODO: why do we have to cast? intellij shows it as redundant, but will not compile without
		return (R)getEvaluator().testRun(
				TestEvaluator.TEST_ASSERT,
				"no redirect", op -> op.addValue("model", page.getClass().getSimpleName()),
				() -> PageUtils.waitForModelDisplayed(page),
				keySender.getReturnObj(), testContext);
	}

	/**
	 * Sends the specified keys to the web element being tested.
	 *
	 * @param keys the keys to send
	 * @return the return object of the web element tester
	 */
	public R sendKeys(CharSequence... keys) {
		return keySender.sendKeys(keys);
	}

	/**
	 * Sends the specified keys to the web element being tested and returns a new WebActionTester object.
	 *
	 * @param keys the keys to send
	 * @return a new WebActionTester object
	 */
	public WebActionTester<R> sendKeysAnd(CharSequence... keys) {
		return keySender.sendKeysAnd(keys);
	}

	/**
	 * Tests the specified page flow.
	 *
	 * @param flow the page flow to test
	 * @param <N>  the return type of the page model class
	 * @return the page model instance of the last page in the flow
	 */
	public <N extends PageModel<? super N>> N testFlow(PageFlow<N> flow) {
		return flow.testPaths();
	}

	/**
	 * Returns a new PageFlow object for testing page flows to the specified page model class.
	 *
	 * @param navPageClass the page model class to navigate to
	 * @param <N>          the return type of the page model class
	 * @return a new PageFlow object
	 */
	public <N extends PageModel<? super N>> PageFlow<N> testPathsToPage(Class<N> navPageClass) {
		return new PageFlow<>(testContext, page.getClass());
	}
}
