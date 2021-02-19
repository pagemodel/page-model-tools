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

package org.pagemodel.core.testers;

import org.pagemodel.core.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public abstract class ListTester<V,R,T> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected R returnObj;
	protected final Callable<List<V>> ref;
	protected final TestContext testContext;
	private TestEvaluator testEvaluator;

	public ListTester(Callable<List<V>> ref, R returnObj, TestContext testContext) {
		this(ref, returnObj, testContext, new TestEvaluator.Now());
	}

	public ListTester(Callable<List<V>> ref, R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	protected List<V> callRef() {
		try {
			return ref.call();
		} catch (Exception ex) {
			return Collections.emptyList();
		}
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	public R isEmpty() {
		return getEvaluator().testCondition(() -> Arrays.toString(callRef().toArray()) + " is empty",
				() -> callRef().isEmpty(), returnObj, testContext);
	}

	public R notEmpty() {
		return getEvaluator().testCondition(() -> Arrays.toString(callRef().toArray()) + " not empty",
				() -> !callRef().isEmpty(), returnObj, testContext);
	}

	public ComparableTester<Integer, R> size() {
		return new ComparableTester<>(callRef()::size, returnObj, testContext, getEvaluator());
	}

	public T itemAt(int index) {
		return makeTester(() -> callRef().get(index), returnObj, testContext, getEvaluator());
	}

	public T firstItem() {
		return makeTester(() -> callRef().get(0), returnObj, testContext, getEvaluator());
	}

	public T lastItem() {
		return makeTester(() -> {
				List<V> list = callRef();
				if(list == null || list.isEmpty()) {
					return null;
				}
				return list.get(list.size() - 1);
			}, returnObj, testContext, getEvaluator());
	}

	protected abstract T makeTester(Callable<V> ref, R returnObj, TestContext testContext, TestEvaluator testEvaluator);
}
