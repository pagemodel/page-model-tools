/*
 * Copyright 2021 Matthew Stevenson <matt@pagemodel.org>
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

package org.pagemodel.core.testers;

import org.pagemodel.core.TestContext;
import org.pagemodel.core.utils.ThrowingFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class ComparableTester<C extends Comparable<C>, R> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected R returnObj;
	protected final Callable<C> ref;
	protected final TestContext testContext;
	private TestEvaluator testEvaluator;

	public ComparableTester(Callable<C> ref, R returnObj, TestContext testContext) {
		this(ref, returnObj, testContext, new TestEvaluator.Now());
	}

	public ComparableTester(Callable<C> ref, R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	protected C callRef() {
		try {
			return ref.call();
		} catch (Exception ex) {
			return null;
		}
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	public R equals(C val) {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] equals [" + val + "]",
				() -> callRef().compareTo(val) == 0, returnObj, testContext);
	}

	public R notEquals(C val) {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] not equals [" + val + "]",
				() -> callRef().compareTo(val) != 0, returnObj, testContext);
	}

	public R greaterThan(C val) {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] greater than [" + val + "]",
				() -> callRef().compareTo(val) > 0, returnObj, testContext);
	}

	public R notGreaterThan(C val) {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] not greater than [" + val + "]",
				() -> callRef().compareTo(val) <= 0, returnObj, testContext);
	}

	public R lessThan(C val) {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] less than [" + val + "]",
				() -> callRef().compareTo(val) < 0, returnObj, testContext);
	}

	public R notLessThan(C val) {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] not less than [" + val + "]",
				() -> callRef().compareTo(val) >= 0, returnObj, testContext);
	}

	public StringTester<R> asString() {
		return new StringTester<>(() -> callRef().toString(), returnObj, testContext, getEvaluator());
	}

	public ComparableTester<C, R> transform(ThrowingFunction<C,C,?> transform) {
		return new ComparableTester<>(() -> ThrowingFunction.unchecked(transform).apply(callRef()), returnObj, testContext, getEvaluator());
	}

	public R storeValue(String key) {
		testContext.store(key, callRef());
		return returnObj;
	}

	public R storeValue(String key, C defaultVal) {
		C val = callRef();
		if (val == null) {
			val = defaultVal;
		}
		testContext.store(key, val);
		return returnObj;
	}
}
