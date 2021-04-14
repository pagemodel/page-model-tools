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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class StringListTester<R> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected R returnObj;
	protected final Callable<List<String>> ref;
	protected final TestContext testContext;
	private TestEvaluator testEvaluator;

	public StringListTester(Callable<List<String>> ref, R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	protected List<String> callRef() {
		try {
			return ref.call();
		} catch (Exception ex) {
			return Collections.emptyList();
		}
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	public StringListTester<R> subList(int fromIndex, int toIndex){
		return new StringListTester<>(() -> callRef().subList(fromIndex, toIndex), returnObj, testContext, getEvaluator());
	}

	public R contains(String string) {
		return getEvaluator().testCondition(() -> Arrays.toString(callRef().toArray()) + " contains [" + string + "]",
				() -> callRef().contains(string), returnObj, testContext);
	}

	public R notContains(String string) {
		return getEvaluator().testCondition(() -> Arrays.toString(callRef().toArray()) + " not contains [" + string + "]",
				() -> !callRef().contains(string), returnObj, testContext);
	}

	public R containsAll(Collection<? extends  String> items) {
		return getEvaluator().testCondition(() -> Arrays.toString(callRef().toArray()) + " contains all " + Arrays.toString(items.toArray()),
				() -> callRef().containsAll(items), returnObj, testContext);
	}

	public R notContainsAll(Collection<? extends  String> items) {
		return getEvaluator().testCondition(() -> Arrays.toString(callRef().toArray()) + " not contains all " + Arrays.toString(items.toArray()),
				() -> !callRef().containsAll(items), returnObj, testContext);
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

	public ComparableTester<Integer, R> indexOf(String item) {
		return new ComparableTester<>(() -> callRef().indexOf(item), returnObj, testContext, getEvaluator());
	}

	public ComparableTester<Integer, R> lastIndexOf(String item) {
		return new ComparableTester<>(() -> callRef().lastIndexOf(item), returnObj, testContext, getEvaluator());
	}

	public StringTester<R> itemAt(int index) {
		return new StringTester<>(() -> callRef().get(index), returnObj, testContext, getEvaluator());
	}

	public StringTester<R> firstItem() {
		return new StringTester<>(() -> callRef().get(0), returnObj, testContext, getEvaluator());
	}

	public StringTester<R> lastItem() {
		return new StringTester<>(() -> {
				List<String> list = callRef();
				if(list == null || list.isEmpty()) {
					return null;
				}
				return list.get(list.size() - 1);
			}, returnObj, testContext, getEvaluator());
	}
}
