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
import org.pagemodel.core.utils.ThrowingFunction;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * This class contains methods for testing strings
 * @param <R> the return type of the test method
 */
public class StringTester<R> {

	// The return object of the test method
	protected R returnObj;
	// The callable reference to the string being tested
	protected final Callable<String> ref;
	// The test context for storing values
	protected final TestContext testContext;
	// The test evaluator for evaluating test conditions
	private TestEvaluator testEvaluator;

	/**
	 * Constructor for StringTester
	 * @param ref the callable reference to the string being tested
	 * @param returnObj the return object of the test method
	 * @param testContext the test context for storing values
	 * @param testEvaluator the test evaluator for evaluating test conditions
	 */
	public StringTester(Callable<String> ref, R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Calls the callable reference to the string being tested
	 * @return the string being tested
	 */
	protected String callRef() {
		try {
			return ref.call();
		} catch (Exception ex) {
			return "";
		}
	}

	/**
	 * Gets the test evaluator for evaluating test conditions
	 * @return the test evaluator
	 */
	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	/**
	 * Sets the test evaluator for evaluating test conditions
	 * @param testEvaluator the test evaluator
	 */
	protected void setEvaluator(TestEvaluator testEvaluator) {
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Gets the return object of the test method
	 * @return the return object
	 */
	protected R getReturnObj() {
		return returnObj;
	}

	/**
	 * Sets the return object of the test method
	 * @param returnObj the return object
	 */
	protected void setReturnObj(R returnObj) {
		this.returnObj = returnObj;
	}

	/**
	 * Tests if the string being tested contains the given string
	 * @param string the string to test for
	 * @return the return object of the test method
	 */
	public R contains(String string) {
		return getEvaluator().testCondition(
				"contains", op -> op.addValue("value", string).addValue("actual",callRef()),
				() -> callRef().contains(string), returnObj, testContext);
	}

	/**
	 * Tests if the string being tested does not contain the given string
	 * @param string the string to test for
	 * @return the return object of the test method
	 */
	public R notContains(String string) {
		return getEvaluator().testCondition(
				"not contains", op -> op.addValue("value", string).addValue("actual",callRef()),
				() -> !callRef().contains(string), returnObj, testContext);
	}

	/**
	 * Tests if the given string contains the string being tested
	 * @param string the string to test
	 * @return the return object of the test method
	 */
	public R containedBy(String string) {
		return getEvaluator().testCondition(
				"contained by", op -> op.addValue("value", string).addValue("actual",callRef()),
				() -> string.contains(callRef()), returnObj, testContext);
	}

	/**
	 * Tests if the given string does not contain the string being tested
	 * @param string the string to test
	 * @return the return object of the test method
	 */
	public R notContainedBy(String string) {
		return getEvaluator().testCondition(
				"not contained by", op -> op.addValue("value", string).addValue("actual",callRef()),
				() -> !string.contains(callRef()), returnObj, testContext);
	}

	/**
	 * Tests if the string being tested equals the given string
	 * @param string the string to test for equality
	 * @return the return object of the test method
	 */
	public R equals(String string) {
		return getEvaluator().testCondition(
				"equals", op -> op.addValue("value", string).addValue("actual",callRef()),
				() -> callRef() == null && string == null || callRef().equals(string), returnObj, testContext);
	}

	/**
	 * Tests if the string being tested does not equal the given string
	 * @param string the string to test for inequality
	 * @return the return object of the test method
	 */
	public R notEquals(String string) {
		return getEvaluator().testCondition(
				"not equals", op -> op.addValue("value", string).addValue("actual",callRef()),
				() -> callRef() == null && string != null || !callRef().equals(string), returnObj, testContext);
	}

	/**
	 * Tests if the string being tested matches the given regex pattern
	 * @param regex the regex pattern to test for
	 * @return the return object of the test method
	 */
	public R matches(String regex) {
		return getEvaluator().testCondition(
				"matches regex", op -> op.addValue("value", regex).addValue("actual",callRef()),
				() -> callRef().matches(regex), returnObj, testContext);
	}

	/**
	 * Tests if the string being tested does not match the given regex pattern
	 * @param regex the regex pattern to test for
	 * @return the return object of the test method
	 */
	public R notMatches(String regex) {
		return getEvaluator().testCondition(
				"not matches regex", op -> op.addValue("value", regex).addValue("actual",callRef()),
				() -> !callRef().matches(regex), returnObj, testContext);
	}

	/**
	 * Tests if the string being tested starts with the given string
	 * @param string the string to test for at the beginning of the string being tested
	 * @return the return object of the test method
	 */
	public R startsWith(String string) {
		return getEvaluator().testCondition(
				"starts with", op -> op.addValue("value", string).addValue("actual",callRef()),
				() -> callRef().startsWith(string), returnObj, testContext);
	}

	/**
	 * Tests if the string being tested does not start with the given string
	 * @param string the string to test for not being at the beginning of the string being tested
	 * @return the return object of the test method
	 */
	public R notStartsWith(String string) {
		return getEvaluator().testCondition(
				"not starts with", op -> op.addValue("value", string).addValue("actual",callRef()),
				() -> !callRef().startsWith(string), returnObj, testContext);
	}

	public R notEndsWith(String string) {
		return getEvaluator().testCondition(
				"not ends with", op -> op.addValue("value", string).addValue("actual",callRef()),
				() -> !callRef().endsWith(string), returnObj, testContext);
	}

	public R isEmpty() {
		return getEvaluator().testCondition(
				"is empty", op -> op.addValue("actual", callRef()),
				() -> (callRef() == null || callRef().isEmpty()), returnObj, testContext);
	}

	public R notEmpty() {
		return getEvaluator().testCondition(
				"not empty", op -> op.addValue("actual", callRef()),
				() -> (callRef() != null && !callRef().isEmpty()), returnObj, testContext);
	}

	public R storeValue(String key) {
		testContext.store(key, callRef());
		return returnObj;
	}

	public R storeMatch(String key, String pattern) {
		return storeMatch(key, pattern, 0);
	}

	public R storeMatch(String key, String pattern, int group) {
		return getEvaluator().testRun(
				TestEvaluator.TEST_FIND,
				"matches regex", op -> op.addValue("value", pattern).addValue("actual",callRef()).addValue("group",group),
				() -> {
					Matcher matcher = Pattern.compile(pattern).matcher(callRef());
					matcher.find();
					testContext.store(key, matcher.group(group));
				}, returnObj, testContext);
	}

	public StringTester<R> testMatch(String pattern) {
		return testMatch(pattern, 0);
	}

	public StringTester<R> testMatch(String pattern, int group) {
		return new StringTester<>(() -> {
			Matcher matcher = Pattern.compile(pattern).matcher(callRef());
			matcher.find();
			return matcher.group(group);
		}, returnObj, testContext, getEvaluator());
	}

	public StringTester<R> transform(ThrowingFunction<String,String,?> transform) {
		return new StringTester<>(() -> ThrowingFunction.unchecked(transform).apply(callRef()), returnObj, testContext, getEvaluator());
	}

	public <T extends Comparable<T>> ComparableTester<T, R> transformCompare(ThrowingFunction<String,T,?> transform) {
		return new ComparableTester<>(() -> ThrowingFunction.unchecked(transform).apply(callRef()), returnObj, testContext, getEvaluator());
	}

	public ComparableTester<Integer, R> length() {
		return new ComparableTester<>(() -> {
			String val = callRef();
			return val == null ? null : val.length();
		}, returnObj, testContext, getEvaluator());
	}

	public ComparableTester<Integer,R> asInteger() {
		return new ComparableTester<>(() -> {
			String val = callRef();
			try {
				if (val == null || val.isEmpty()) {
					return null;
				}
				return Integer.parseInt(callRef());
			} catch (Exception ex) {
				throw testContext.createException("Error: Unable to parse integer from string [" + val + "]");
			}
		}, returnObj, testContext, getEvaluator());
	}

	public ComparableTester<Date,R> asDate() {
		return new ComparableTester<>(() -> {
			String val = callRef();
			try {
				if (val == null || val.isEmpty()) {
					return null;
				}
				return new Date(callRef());
			} catch (Exception ex) {
				throw testContext.createException("Error: Unable to parse date from string [" + val + "]");
			}
		}, returnObj, testContext, getEvaluator());
	}
}
