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

import org.pagemodel.core.testers.ComparableTester;
import org.pagemodel.core.testers.StringTester;
import org.pagemodel.core.TestContext;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Sean Hale <shale@tetrazoid.net>
 */
public class HttpResponseTester<R> {
	private final TestContext testContext;
	private HttpsURLConnection connection;
	private HttpTester<?> parent;

	public HttpResponseTester(HttpsURLConnection connection, HttpTester<?> parent, TestContext testContext) {
		this.connection = connection;
		this.testContext = testContext;
		this.parent = parent;
	}

	public ComparableTester<Integer, HttpResponseTester<R>> testResponseCode() {
		return new ComparableTester<>(() -> connection.getResponseCode(), this, testContext);
	}

	public StringTester<HttpResponseTester<R>> testResponseBody() {
		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader br =
					new BufferedReader(
							new InputStreamReader(connection.getInputStream()));

			String aux = "";

			while ((aux = br.readLine()) != null) {
				builder.append(aux);
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String body = builder.toString();
		return new StringTester<>(() -> body, this, testContext);
	}

	public R disconnect() {
		return (R) parent.disconnect();
	}
}