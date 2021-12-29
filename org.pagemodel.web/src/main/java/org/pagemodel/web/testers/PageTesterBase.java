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

import org.pagemodel.core.testers.StringTester;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.WebTestContext;

import java.util.concurrent.TimeUnit;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class PageTesterBase<P extends PageModel<? super P>> {

	protected final P page;
	protected final WebTestContext testContext;
	protected TestEvaluator testEvaluator;

	public PageTesterBase(P page, WebTestContext testContext, TestEvaluator testEvaluator) {
		this.testContext = testContext;
		this.page = page;
		this.testEvaluator = testEvaluator;
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	public StringTester<P> title() {
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"page title", op -> op
						.addValue("model", page.getClass().getName()));
		return new StringTester<>(() -> testContext.getDriver().getTitle(), page, testContext, getEvaluator());
	}

	public StringTester<P> url() {
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"page url", op -> op
						.addValue("model", page.getClass().getName()));
		return new StringTester<>(() -> testContext.getDriver().getCurrentUrl(), page, testContext, getEvaluator());
	}

	public StringTester<P> pageSource() {
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"page source", op -> op
						.addValue("model", page.getClass().getName()));
		return new StringTester<>(() -> testContext.getDriver().getPageSource(), page, testContext, getEvaluator());
	}

	public DimensionTester<P> windowSize(){
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"window size", op -> op
						.addValue("model", page.getClass().getName()));
		return new DimensionTester<>(() -> testContext.getDriver().manage().window().getSize(), page, testContext, getEvaluator());
	}

	public PointTester<P> windowPosition(){
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"window position", op -> op
						.addValue("model", page.getClass().getName()));
		return new PointTester<>(() -> testContext.getDriver().manage().window().getPosition(), page, testContext, getEvaluator());
	}

	/**
	 * @author Matt Stevenson [matt@pagemodel.org]
	 */
	public static class PageWait<P extends PageModel<? super P>> extends PageTesterBase<P> {

		private WebTestEvaluator.Wait testEvaluator;

		public PageWait(P page, WebTestContext testContext, WebTestEvaluator.Wait testEvaluator) {
			super(page, testContext, testEvaluator);
			this.testEvaluator = testEvaluator;
		}

		public P numberOfMinutes(int minutes) {
			return waitDuration(minutes, TimeUnit.MINUTES);
		}

		public P numberOfSeconds(int seconds) {
			return waitDuration(seconds, TimeUnit.SECONDS);
		}

		public P numberOfMilliseconds(int millisec) {
			return waitDuration(millisec, TimeUnit.MILLISECONDS);
		}

		private P waitDuration(long duration, TimeUnit unit) {
			return getEvaluator().testRun(
					TestEvaluator.TEST_EXECUTE,
					"sleep", op -> op.addValue("value", duration + " " + unit.toString().toLowerCase()),
					() -> unit.sleep(duration),
					page, testContext);
		}
	}
}
