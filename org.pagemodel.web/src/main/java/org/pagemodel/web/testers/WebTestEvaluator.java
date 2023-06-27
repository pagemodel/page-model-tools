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

import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.ThrowingCallable;
import org.pagemodel.core.utils.json.JsonObjectBuilder;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.PageUtils;
import org.pagemodel.web.WebTestContext;
import org.pagemodel.web.utils.RefreshTracker;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * The WebTestEvaluator class provides two nested classes, Wait and WaitAndRefresh, for continuously evaluating a test until it passes or times out.
 * Wait class will continuously re-evaluate a test until it is true or times out.
 * WaitAndRefresh class will continuously refresh the page, perform any after refresh setup actions, and re-evaluate the test until it passes or times out.
 */
public class WebTestEvaluator {

	/**
	 * The Wait class extends the TestEvaluator.Now class and provides a method for continuously re-evaluating a test until it is true or times out.
	 */
	public static class Wait extends TestEvaluator.Now {
		protected WebTestContext testContext;
		protected int waitSec;

		/**
		 * Constructs a Wait object with the given WebTestContext and wait time in seconds.
		 * @param testContext the WebTestContext object
		 * @param waitSec the wait time in seconds
		 */
		public Wait(WebTestContext testContext, int waitSec) {
			this.label = "wait";
			this.testContext = testContext;
			this.waitSec = waitSec;
		}

		/**
		 * Sets the wait time in seconds and returns the Wait object.
		 * @param waitSec the wait time in seconds
		 * @return the Wait object
		 */
		public Wait withTimeout(int waitSec) {
			this.waitSec = waitSec;
			return this;
		}

		/**
		 * Returns a Consumer object that adds the timeout value to the evaluation type JSON.
		 * @return a Consumer object that adds the timeout value to the evaluation type JSON
		 */
		@Override
		public Consumer<JsonObjectBuilder> getEvalTypeJson(){
			return super.getEvalTypeJson()
					.andThen(eval -> eval.addValue("timeout", waitSec));
		}

		/**
		 * Returns the WebTestContext object.
		 * @return the WebTestContext object
		 */
		public WebTestContext getTestContext() {
			return testContext;
		}

		/**
		 * Returns the wait time in seconds.
		 * @return the wait time in seconds
		 */
		public int getWaitSec() {
			return waitSec;
		}

		/**
		 * Calls the test repeatedly until it returns true or times out.
		 * @param test the Callable<Boolean> test to be evaluated
		 * @return true if the test passes, false if it times out
		 */
		@Override
		protected Boolean callTest(Callable<Boolean> test) {
			FluentWait wait = new WebDriverWait(testContext.getDriver(), waitSec)
					.ignoring(Throwable.class).ignoring(Exception.class);
			wait.until(driver -> ThrowingCallable.unchecked(test).call());
			return true;
		}
	}

	/**
	 * The WaitAndRefresh class extends the Wait class and provides a method for continuously refreshing the page, performing any after refresh setup actions, and re-evaluating the test until it passes or times out.
	 * @param <R> the type of the return object
	 */
	public static class WaitAndRefresh<R> extends Wait {
		protected Function<R, R> pageSetupFunction;
		protected R returnObj;
		protected PageModel page;

		/**
		 * Constructs a WaitAndRefresh object with the given WebTestContext, wait time in seconds, return object, and PageModel.
		 * @param testContext the WebTestContext object
		 * @param waitSec the wait time in seconds
		 * @param returnObj the return object
		 * @param page the PageModel object
		 */
		public WaitAndRefresh(WebTestContext testContext, int waitSec, R returnObj, PageModel page) {
			super(testContext, waitSec);
			this.label = "refresh";
			this.returnObj = returnObj;
			this.page = page;
		}

		/**
		 * Sets the page setup function and returns the WaitAndRefresh object.
		 * @param pageSetupFunction the page setup function
		 * @return the WaitAndRefresh object
		 */
		public WaitAndRefresh<R> withPageSetup(Function<R, R> pageSetupFunction) {
			this.pageSetupFunction = pageSetupFunction;
			return this;
		}

		/**
		 * Sets the wait time in seconds and returns the WaitAndRefresh object.
		 * @param waitSec the wait time in seconds
		 * @return the WaitAndRefresh object
		 */
		public WaitAndRefresh<R> withTimeout(int waitSec) {
			this.waitSec = waitSec;
			return this;
		}

		/**
		 * Returns a Consumer object that adds the page setup value to the evaluation type JSON.
		 * @return a Consumer object that adds the page setup value to the evaluation type JSON
		 */
		@Override
		public Consumer<JsonObjectBuilder> getEvalTypeJson(){
			return super.getEvalTypeJson()
					.andThen(eval -> eval.addValue("pageSetup", (pageSetupFunction != null)));
		}

		/**
		 * Returns the return object.
		 * @return the return object
		 */
		public R getReturnObj() {
			return returnObj;
		}

		/**
		 * Returns the page setup function.
		 * @return the page setup function
		 */
		public Function<R, R> getPageSetupFunction() {
			return pageSetupFunction;
		}

		/**
		 * Calls the test repeatedly until it returns true or times out, refreshing the page and performing any after refresh setup actions as necessary.
		 * @param test the Callable<Boolean> test to be evaluated
		 * @return true if the test passes, false if it times out
		 */
		@Override
		protected Boolean callTest(Callable<Boolean> test) {
			if (pageSetupFunction != null) {
				returnObj = pageSetupFunction.apply(returnObj);
			}
			long end = System.currentTimeMillis() + (waitSec * 1000);
			long waitStep = Math.max(waitSec / 10, 2);
			try {
				FluentWait wait = new WebDriverWait(testContext.getDriver(), waitStep)
						.ignoring(Throwable.class).ignoring(Exception.class);
				wait.until(driver -> ThrowingCallable.unchecked(test).call());
				return true;
			} catch (Throwable ex) { }

			while (System.currentTimeMillis() < end) {
				try {
					RefreshTracker.refreshPage(page);
					PageUtils.waitForModelDisplayed(page);
					if (pageSetupFunction != null) {
						returnObj = pageSetupFunction.apply(returnObj);
					}
					logEvent(TEST_ASSERT, getActionDisplay(), getEventParams(), getSourceEvents());
					FluentWait wait = new WebDriverWait(testContext.getDriver(), waitStep)
							.ignoring(Throwable.class).ignoring(Exception.class);
					wait.until(driver -> ThrowingCallable.unchecked(test).call());
					return true;
				} catch (Throwable ex) { }
			}
			return false;
		}
	}
}