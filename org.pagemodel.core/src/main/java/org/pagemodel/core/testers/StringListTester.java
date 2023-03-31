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
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * A class for testing List<String> objects.
 * @param <R> the type of the object being tested
 */
public class StringListTester<R> {
	/**
	 * The object being returned by the test methods.
	 */
	protected R returnObj;
	/**
	 * The reference to the List<String> being tested.
	 */
	protected final Callable<List<String>> ref;
	/**
	 * The test context for storing and retrieving values during testing.
	 */
	protected final TestContext testContext;
	/**
	 * The test evaluator for evaluating test conditions.
	 */
	private TestEvaluator testEvaluator;

	/**
	 * Constructs a new StringListTester with the given reference, return object, test context, and test evaluator.
	 * @param ref the reference to the List<String> being tested
	 * @param returnObj the object being returned by the test methods
	 * @param testContext the test context for storing and retrieving values during testing
	 * @param testEvaluator the test evaluator for evaluating test conditions
	 */
	public StringListTester(Callable<List<String>> ref, R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Calls the reference to the List<String> being tested.
	 * @return the List<String> returned by the reference, or an empty list if an exception occurs
	 */
	protected List<String> callRef() {
		try {
			return ref.call();
		} catch (Exception ex) {
			return Collections.emptyList();
		}
	}

	/**
	 * Gets the test evaluator for evaluating test conditions.
	 * @return the test evaluator for evaluating test conditions
	 */
	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	/**
	 * Returns a new StringListTester that tests a sub-list of the List<String> being tested.
	 * @param fromIndex the starting index of the sub-list (inclusive)
	 * @param toIndex the ending index of the sub-list (exclusive)
	 * @return a new StringListTester that tests a sub-list of the List<String> being tested
	 */
	public StringListTester<R> subList(int fromIndex, int toIndex){
		return new StringListTester<>(() -> callRef().subList(fromIndex, toIndex), returnObj, testContext, getEvaluator());
	}

	/**
	 * Tests if the List<String> being tested contains the given string.
	 * @param string the string to test for
	 * @return the object being returned by the test methods
	 */
	public R contains(String string) {
		return getEvaluator().testCondition(
				"contains", op -> op
						.addValue("value", string)
						.addValue("actual", callRef()),
				() -> callRef().contains(string), returnObj, testContext);
	}

	/**
	 * Tests if the List<String> being tested does not contain the given string.
	 * @param string the string to test for
	 * @return the object being returned by the test methods
	 */
	public R notContains(String string) {
		return getEvaluator().testCondition(
				"not contains", op -> op
						.addValue("value", string)
						.addValue("actual", callRef()),
				() -> !callRef().contains(string), returnObj, testContext);
	}

	/**
	 * Tests if the List<String> being tested contains all of the given strings.
	 * @param items the strings to test for
	 * @return the object being returned by the test methods
	 */
	public R containsAll(String...items) {
		return containsAll(Arrays.asList(items));
	}

	/**
	 * Tests if the List<String> being tested contains all of the given strings.
	 * @param items the strings to test for
	 * @return the object being returned by the test methods
	 */
	public R containsAll(Collection<? extends  String> items) {
		return getEvaluator().testCondition(
				"contains all", op -> op
						.addValue("value", items)
						.addValue("actual", callRef()),
				() -> callRef().containsAll(items), returnObj, testContext);
	}

	/**
	 * Tests if the List<String> being tested does not contain all of the given strings.
	 * @param items the strings to test for
	 * @return the object being returned by the test methods
	 */
	public R notContainsAll(String...items) {
		return notContainsAll(Arrays.asList(items));
	}

	/**
	 * Tests if the List<String> being tested does not contain all of the given strings.
	 * @param items the strings to test for
	 * @return the object being returned by the test methods
	 */
	public R notContainsAll(Collection<? extends  String> items) {
		return getEvaluator().testCondition(
				"not contains all", op -> op
						.addValue("value", items)
						.addValue("actual", callRef()),
				() -> !callRef().containsAll(items), returnObj, testContext);
	}

	/**
	 * Tests if the List<String> being tested is disjoint with the given strings.
	 * @param items the strings to test for
	 * @return the object being returned by the test methods
	 */
	public R disjoint(String...items) {
		return disjoint(Arrays.asList(items));
	}

	/**
	 * Tests if the List<String> being tested is disjoint with the given strings.
	 * @param items the strings to test for
	 * @return the object being returned by the test methods
	 */
	public R disjoint(Collection<? extends  String> items) {
		return getEvaluator().testCondition(
				"disjoint", op -> op
						.addValue("value", items)
						.addValue("actual", callRef()),
				() -> Collections.disjoint(callRef(), items), returnObj, testContext);
	}

	/**
	 * Tests if the List<String> being tested is not disjoint with the given strings.
	 * @param items the strings to test for
	 * @return the object being returned by the test methods
	 */
	public R notDisjoint(String...items) {
		return notDisjoint(Arrays.asList(items));
	}

	/**
	 * Tests if the List<String> being tested is not disjoint with the given strings.
	 * @param items the strings to test for
	 * @return the object being returned by the test methods
	 */
	public R notDisjoint(Collection<? extends  String> items) {
		return getEvaluator().testCondition(
				"not disjoint", op -> op
						.addValue("value", items)
						.addValue("actual", callRef()),
				() -> !Collections.disjoint(callRef(), items), returnObj, testContext);
	}

	/**
	 * Tests if the List<String> being tested is empty.
	 * @return the object being returned by the test methods
	 */
	public R isEmpty() {
		return getEvaluator().testCondition(
				"is empty", op -> op
						.addValue("actual", callRef()),
				() -> callRef().isEmpty(), returnObj, testContext);
	}

	/**
	 * Tests if the List<String> being tested is not empty.
	 * @return the object being returned by the test methods
	 */
	public R notEmpty() {
		return getEvaluator().testCondition(
				"not empty", op -> op
						.addValue("actual", callRef()),
				() -> !callRef().isEmpty(), returnObj, testContext);
	}

	/**
	 * Tests if the List<String> being tested is equal to the given strings in the same order.
	 * @param items the strings to test for
	 * @return the object being returned by the test methods
	 */
	public R equalsOrdered(String...items) {
		return equalsOrdered(Arrays.asList(items));
	}

	/**
	 * Tests if the List<String> being tested is equal to the given strings in the same order.
	 * @param items the strings to test for
	 * @return the object being returned by the test methods
	 */
	public R equalsOrdered(Collection<? extends String> items){
		return getEvaluator().testCondition(
				"equals ordered", op -> op
						.addValue("value", items)
						.addValue("actual", callRef()),
				() -> callRef().equals(items), returnObj, testContext);
	}

	/**
	 * Tests if the List<String> being tested is equal to the given strings in any order.
	 * @param items the strings to test for
	 * @return the object being returned by the test methods
	 */
	public R equalsUnordered(String...items) {
		return equalsUnordered(Arrays.asList(items));
	}

	/**
	 * Tests if the List<String> being tested is equal to the given strings in any order.
	 * @param items the strings to test for
	 * @return the object being returned by the test methods
	 */
	public R equalsUnordered(Collection<? extends  String> items) {
		return getEvaluator().testCondition(
				"equals unordered", op -> op
						.addValue("value", items)
						.addValue("actual", callRef()),
				() -> isEqualUnordered(items),
				returnObj, testContext);
	}

	/**
	 * Tests if the List<String> being tested is not equal to the given strings in any order.
	 * @param items the strings to test for
	 * @return the object being returned by the test methods
	 */
	public R notEqualsUnordered(String...items) {
		return notEqualsUnordered(Arrays.asList(items));
	}

	/**
	 * Tests if the List<String> being tested is not equal to the given strings in any order.
	 * @param items the strings to test for
	 * @return the object being returned by the test methods
	 */
	public R notEqualsUnordered(Collection<? extends  String> items) {
		return getEvaluator().testCondition(
				"not equals unordered", op -> op
						.addValue("value", items)
						.addValue("actual", callRef()),
				() -> !isEqualUnordered(items),
				returnObj, testContext);
	}

	/**
	 * Checks if the List<String> being tested is equal to the given strings in any order.
	 * @param items the strings to test for
	 * @return true if the List<String> being tested is equal to the given strings in any order, false otherwise
	 */
	private boolean isEqualUnordered(Collection<? extends  String> items){
		List<String> vals = callRef();
		if(vals == null || items == null){
			return vals == null && items == null;
		}
		return items.size() == vals.size() && items.containsAll(vals);
	}

	/**
	 * Returns a ComparableTester for testing the size of the List<String> being tested.
	 * @return a ComparableTester for testing the size of the List<String> being tested
	 */
	public ComparableTester<Integer, R> size() {
		return new ComparableTester<>(callRef()::size, returnObj, testContext, getEvaluator());
	}

	/**
	 * Returns a ComparableTester for testing the index of the given string in the List<String> being tested.
	 * @param item the string to test for
	 * @return a ComparableTester for testing the index of the given string in the List<String> being tested
	 */
	public ComparableTester<Integer, R> indexOf(String item) {
		return new ComparableTester<>(() -> callRef().indexOf(item), returnObj, testContext, getEvaluator());
	}

	/**
	 * Returns a ComparableTester for testing the last index of the given string in the List<String> being tested.
	 * @param item the string to test for
	 * @return a ComparableTester for testing the last index of the given string in the List<String> being tested
	 */
	public ComparableTester<Integer, R> lastIndexOf(String item) {
		return new ComparableTester<>(() -> callRef().lastIndexOf(item), returnObj, testContext, getEvaluator());
	}

	/**
	 * Returns a StringTester for testing the item at the given index in the List<String> being tested.
	 * @param index the index of the item to test
	 * @return a StringTester for testing the item at the given index in the List<String> being tested
	 */
	public StringTester<R> itemAt(int index) {
		return new StringTester<>(() -> callRef().get(index), returnObj, testContext, getEvaluator());
	}

	/**
	 * Returns a StringTester for testing the first item in the List<String> being tested.
	 * @return a StringTester for testing the first item in the List<String> being tested
	 */
	public StringTester<R> firstItem() {
		return new StringTester<>(() -> callRef().get(0), returnObj, testContext, getEvaluator());
	}

	/**
	 * Returns a StringTester for testing the last item in the List<String> being tested.
	 * @return a StringTester for testing the last item in the List<String> being tested
	 */
	public StringTester<R> lastItem() {
		return new StringTester<>(() -> {
			List<String> list = callRef();
			if(list == null || list.isEmpty()) {
				return null;
			}
			return list.get(list.size() - 1);
		}, returnObj, testContext, getEvaluator());
	}

	/**
	 * Stores the List<String> being tested in the test context with the given key.
	 * @param key the key to store the List<String> under
	 * @return the object being returned by the test methods
	 */
	public R storeValue(String key) {
		testContext.store(key, callRef());
		return returnObj;
	}
}
