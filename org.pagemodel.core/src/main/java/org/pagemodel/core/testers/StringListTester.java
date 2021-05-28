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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class StringListTester<R> {
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
		return getEvaluator().testCondition(
				"contains", op -> op
						.addValue("value", string)
						.addValue("actual", callRef()),
				() -> callRef().contains(string), returnObj, testContext);
	}

	public R notContains(String string) {
		return getEvaluator().testCondition(
				"not contains", op -> op
						.addValue("value", string)
						.addValue("actual", callRef()),
				() -> !callRef().contains(string), returnObj, testContext);
	}

	public R containsAll(String...items) {
		return containsAll(Arrays.asList(items));
	}

	public R containsAll(Collection<? extends  String> items) {
		return getEvaluator().testCondition(
				"contains all", op -> op
						.addValue("value", items)
						.addValue("actual", callRef()),
				() -> callRef().containsAll(items), returnObj, testContext);
	}

	public R notContainsAll(String...items) {
		return notContainsAll(Arrays.asList(items));
	}

	public R notContainsAll(Collection<? extends  String> items) {
		return getEvaluator().testCondition(
				"not contains all", op -> op
						.addValue("value", items)
						.addValue("actual", callRef()),
				() -> !callRef().containsAll(items), returnObj, testContext);
	}

	public R disjoint(String...items) {
		return disjoint(Arrays.asList(items));
	}

	public R disjoint(Collection<? extends  String> items) {
		return getEvaluator().testCondition(
				"disjoint", op -> op
						.addValue("value", items)
						.addValue("actual", callRef()),
				() -> Collections.disjoint(callRef(), items), returnObj, testContext);
	}

	public R notDisjoint(String...items) {
		return notDisjoint(Arrays.asList(items));
	}

	public R notDisjoint(Collection<? extends  String> items) {
		return getEvaluator().testCondition(
				"not disjoint", op -> op
						.addValue("value", items)
						.addValue("actual", callRef()),
				() -> !Collections.disjoint(callRef(), items), returnObj, testContext);
	}

	public R isEmpty() {
		return getEvaluator().testCondition(
				"is empty", op -> op
						.addValue("actual", callRef()),
				() -> callRef().isEmpty(), returnObj, testContext);
	}

	public R notEmpty() {
		return getEvaluator().testCondition(
				"not empty", op -> op
						.addValue("actual", callRef()),
				() -> !callRef().isEmpty(), returnObj, testContext);
	}

	public R equalsOrdered(String...items) {
		return equalsOrdered(Arrays.asList(items));
	}

	public R equalsOrdered(Collection<? extends String> items){
		return getEvaluator().testCondition(
				"equals ordered", op -> op
						.addValue("value", items)
						.addValue("actual", callRef()),
				() -> callRef().equals(items), returnObj, testContext);
	}

	public R equalsUnordered(String...items) {
		return equalsUnordered(Arrays.asList(items));
	}

	public R equalsUnordered(Collection<? extends  String> items) {
		return getEvaluator().testCondition(
				"equals unordered", op -> op
						.addValue("value", items)
						.addValue("actual", callRef()),
				() -> isEqualUnordered(items),
				returnObj, testContext);
	}

	public R notEqualsUnordered(String...items) {
		return notEqualsUnordered(Arrays.asList(items));
	}

	public R notEqualsUnordered(Collection<? extends  String> items) {
		return getEvaluator().testCondition(
				"not equals unordered", op -> op
						.addValue("value", items)
						.addValue("actual", callRef()),
				() -> !isEqualUnordered(items),
				returnObj, testContext);
	}

	private boolean isEqualUnordered(Collection<? extends  String> items){
		List<String> vals = callRef();
		if(vals == null || items == null){
			return vals == null && items == null;
		}
		return items.size() == vals.size() && items.containsAll(vals);
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

	public R storeValue(String key) {
		testContext.store(key, callRef());
		return returnObj;
	}
}
