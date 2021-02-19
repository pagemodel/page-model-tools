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
import org.openqa.selenium.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class PageTesterBase<P extends PageModel<? super P>> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
		return new StringTester<>(() -> testContext.getDriver().getTitle(), page, testContext, getEvaluator());
	}

	public StringTester<P> url() {
		return new StringTester<>(() -> testContext.getDriver().getCurrentUrl(), page, testContext, getEvaluator());
	}

	public StringTester<P> pageSource() {
		return new StringTester<>(() -> testContext.getDriver().getPageSource(), page, testContext, getEvaluator());
	}

	public RectangleTester<P> windowSize() {
		return new RectangleTester<>(() -> new Rectangle(
				testContext.getDriver().manage().window().getPosition(),
				testContext.getDriver().manage().window().getSize()), page, testContext, getEvaluator());
	}

	/**
	 * @author Matt Stevenson <matt@pagemodel.org>
	 */
	public static class PageWait<P extends PageModel<? super P>> extends PageTesterBase<P> {
		private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
			log.info("Sleeping " + duration + " " + unit.toString().toLowerCase() + ".");
			try {
				unit.sleep(duration);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(ex);
			}
			return page;
		}
	}
}
