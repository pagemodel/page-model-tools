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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.lang.invoke.MethodHandles;
import java.net.URL;

/**
 * @author Sean Hale <shale@tetrazoid.net>
 */
public class HttpTester<R> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	protected HttpResponseTester<R> responseTester;

	protected R returnObj;
	private final TestContext testContext;
	private HttpsURLConnection con;

	public HttpTester(R returnObj, TestContext testContext) {
		this.testContext = testContext;
		this.returnObj = returnObj;
		this.con = null;
	}

	public HttpResponseTester<HttpTester<R>> testSend(String fullUrl) {
		log.info("Calling api at " + fullUrl);
		try {
			URL url = new URL(fullUrl);
			this.con = (HttpsURLConnection) url.openConnection();
			TrustModifier.relaxHostChecking(this.con);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return new HttpResponseTester<>(this.con, this, testContext);
	}

	public R disconnect() {
		this.con.disconnect();
		return returnObj;
	}
}