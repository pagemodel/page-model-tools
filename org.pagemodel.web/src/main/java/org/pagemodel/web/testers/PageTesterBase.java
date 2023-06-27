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
/**
 * The PageTesterBase class provides a base class for testing web pages. It contains methods for testing the page title,
 * URL, page source, window size, and window position. It also contains a nested PageWait class for waiting for a specified
 * duration before continuing with the test.
 *
 * @param <P> the type of the page model being tested
 */
public class PageTesterBase<P extends PageModel<? super P>> {

	/**
	 * The page model being tested.
	 */
	protected final P page;

	/**
	 * The web test context.
	 */
	protected final WebTestContext testContext;

	/**
	 * The test evaluator.
	 */
	protected TestEvaluator testEvaluator;

	/**
	 * Constructs a new PageTesterBase object with the specified page model, web test context, and test evaluator.
	 *
	 * @param page the page model being tested
	 * @param testContext the web test context
	 * @param testEvaluator the test evaluator
	 */
	public PageTesterBase(P page, WebTestContext testContext, TestEvaluator testEvaluator) {
		this.testContext = testContext;
		this.page = page;
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Returns the test evaluator.
	 *
	 * @return the test evaluator
	 */
	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	/**
	 * Returns a StringTester object for testing the page title.
	 *
	 * @return a StringTester object for testing the page title
	 */
	public StringTester<P> title() {
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"page title", op -> op
						.addValue("model", page.getClass().getName()));
		return new StringTester<>(() -> testContext.getDriver().getTitle(), page, testContext, getEvaluator());
	}

	/**
	 * Returns a StringTester object for testing the page URL.
	 *
	 * @return a StringTester object for testing the page URL
	 */
	public StringTester<P> url() {
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"page url", op -> op
						.addValue("model", page.getClass().getName()));
		return new StringTester<>(() -> testContext.getDriver().getCurrentUrl(), page, testContext, getEvaluator());
	}

	/**
	 * Returns a StringTester object for testing the page source.
	 *
	 * @return a StringTester object for testing the page source
	 */
	public StringTester<P> pageSource() {
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"page source", op -> op
						.addValue("model", page.getClass().getName()));
		return new StringTester<>(() -> testContext.getDriver().getPageSource(), page, testContext, getEvaluator());
	}

	/**
	 * Returns a DimensionTester object for testing the window size.
	 *
	 * @return a DimensionTester object for testing the window size
	 */
	public DimensionTester<P> windowSize(){
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"window size", op -> op
						.addValue("model", page.getClass().getName()));
		return new DimensionTester<>(() -> testContext.getDriver().manage().window().getSize(), page, testContext, getEvaluator());
	}

	/**
	 * Returns a PointTester object for testing the window position.
	 *
	 * @return a PointTester object for testing the window position
	 */
	public PointTester<P> windowPosition(){
		getEvaluator().addSourceEvent(TestEvaluator.TEST_FIND,
				"window position", op -> op
						.addValue("model", page.getClass().getName()));
		return new PointTester<>(() -> testContext.getDriver().manage().window().getPosition(), page, testContext, getEvaluator());
	}

	/**
	 * The PageWait class provides a way to wait for a specified duration before continuing with the test.
	 *
	 * @param <P> the type of the page model being tested
	 */
	public static class PageWait<P extends PageModel<? super P>> extends PageTesterBase<P> {

		/**
		 * The web test evaluator for waiting.
		 */
		private WebTestEvaluator.Wait testEvaluator;

		/**
		 * Constructs a new PageWait object with the specified page model, web test context, and web test evaluator for waiting.
		 *
		 * @param page the page model being tested
		 * @param testContext the web test context
		 * @param testEvaluator the web test evaluator for waiting
		 */
		public PageWait(P page, WebTestContext testContext, WebTestEvaluator.Wait testEvaluator) {
			super(page, testContext, testEvaluator);
			this.testEvaluator = testEvaluator;
		}

		/**
		 * Waits for the specified number of minutes.
		 *
		 * @param minutes the number of minutes to wait
		 * @return the page model being tested
		 */
		public P numberOfMinutes(int minutes) {
			return waitDuration(minutes, TimeUnit.MINUTES);
		}

		/**
		 * Waits for the specified number of seconds.
		 *
		 * @param seconds the number of seconds to wait
		 * @return the page model being tested
		 */
		public P numberOfSeconds(int seconds) {
			return waitDuration(seconds, TimeUnit.SECONDS);
		}

		/**
		 * Waits for the specified number of milliseconds.
		 *
		 * @param millisec the number of milliseconds to wait
		 * @return the page model being tested
		 */
		public P numberOfMilliseconds(int millisec) {
			return waitDuration(millisec, TimeUnit.MILLISECONDS);
		}

		/**
		 * Waits for the specified duration.
		 *
		 * @param duration the duration to wait
		 * @param unit the time unit of the duration
		 * @return the page model being tested
		 */
		private P waitDuration(long duration, TimeUnit unit) {
			return getEvaluator().testRun(
					TestEvaluator.TEST_EXECUTE,
					"sleep", op -> op.addValue("value", duration + " " + unit.toString().toLowerCase()),
					() -> unit.sleep(duration),
					page, testContext);
		}
	}
}