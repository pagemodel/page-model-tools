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

import org.openqa.selenium.Dimension;
import org.pagemodel.core.TestContext;
import org.pagemodel.core.testers.ComparableTester;
import org.pagemodel.core.testers.TestEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class DimensionTester<R> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected final R returnObj;
	protected final Callable<Dimension> ref;
	protected final TestContext testContext;
	private TestEvaluator testEvaluator;

	public DimensionTester(Callable<Dimension> ref, R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	protected Dimension callRef() {
		try {
			return ref.call();
		} catch (Exception ex) {
			return null;
		}
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	public ComparableTester<Integer, R> width() {
		return new ComparableTester<>(() -> callRef().getWidth(), returnObj, testContext, getEvaluator());
	}

	public ComparableTester<Integer, R> height() {
		return new ComparableTester<>(() -> callRef().getHeight(), returnObj, testContext, getEvaluator());
	}
}
