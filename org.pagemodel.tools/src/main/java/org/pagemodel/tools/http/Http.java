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

package org.pagemodel.tools.http;

import org.pagemodel.core.TestContext;
import org.pagemodel.core.testers.TestEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * @author Sean Hale <shale@tetrazoid.net>
 */
public class Http {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static Http.HttpInner testHttp(TestContext testContext) {
		return new Http.HttpInner(testContext, testContext.getEvaluator());
	}

	public static class HttpInner extends HttpTester<HttpInner> {
		public HttpInner(TestContext testContext, TestEvaluator testEvaluator) {
			super(null, testContext, testEvaluator);
			this.returnObj = this;
		}
	}

}
