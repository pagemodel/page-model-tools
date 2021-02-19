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
import org.pagemodel.web.WebTestContext;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.ThrowingCallable;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.PageUtils;
import org.pagemodel.web.utils.RefreshTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class WebTestEvaluator {
	public static class Wait extends TestEvaluator.Now {
		protected WebTestContext testContext;
		protected int waitSec;

		public Wait(WebTestContext testContext, int waitSec) {
			this.label = " wait for";
			this.testContext = testContext;
			this.waitSec = waitSec;
		}

		public Wait withTimeout(int waitSec) {
			this.waitSec = waitSec;
			return this;
		}

		public WebTestContext getTestContext() {
			return testContext;
		}

		public int getWaitSec() {
			return waitSec;
		}

		@Override
		public Boolean apply(Callable<Boolean> test) {
			FluentWait wait = new WebDriverWait(testContext.getDriver(), waitSec)
					.ignoring(Throwable.class).ignoring(Exception.class);
			wait.until(driver -> ThrowingCallable.unchecked(test).call());
			return true;
		}
	}

	public static class WaitAndRefresh<R> extends Wait {
		private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
		protected Function<R, R> pageSetupFunction;
		protected R returnObj;
		protected PageModel page;

		public WaitAndRefresh(WebTestContext testContext, int waitSec, R returnObj, PageModel page) {
			super(testContext, waitSec);
			this.label = " wait and refresh for";
			this.returnObj = returnObj;
			this.page = page;
		}

		public WaitAndRefresh<R> withPageSetup(Function<R, R> pageSetupFunction) {
			this.pageSetupFunction = pageSetupFunction;
			return this;
		}

		public WaitAndRefresh<R> withTimeout(int waitSec) {
			this.waitSec = waitSec;
			return this;
		}

		public R getReturnObj() {
			return returnObj;
		}

		public Function<R, R> getPageSetupFunction() {
			return pageSetupFunction;
		}

		@Override
		public Boolean apply(Callable<Boolean> test) {
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
					log.info(getTestMessage(getTestMessageRef(), getSourceDisplayRef()));
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
