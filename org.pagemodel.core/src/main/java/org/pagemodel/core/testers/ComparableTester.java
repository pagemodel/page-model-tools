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

import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * A class for testing objects that implement the Comparable interface.
 * @param <C> the type of the object being tested, which must implement Comparable
 * @param <R> the type of the return object for the test methods
 */
public class ComparableTester<C extends Comparable<C>, R> {
	/**
	 * The return object for the test methods.
	 */
	protected R returnObj;
	/**
	 * The reference to the object being tested.
	 */
	protected final Callable<C> ref;
	/**
	 * The test context for the test methods.
	 */
	protected final TestContext testContext;
	/**
	 * The test evaluator for the test methods.
	 */
	private TestEvaluator testEvaluator;

	/**
	 * Constructs a new ComparableTester with the given reference, return object, test context, and test evaluator.
	 * @param ref the reference to the object being tested
	 * @param returnObj the return object for the test methods
	 * @param testContext the test context for the test methods
	 * @param testEvaluator the test evaluator for the test methods
	 */
	public ComparableTester(Callable<C> ref, R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Calls the reference to the object being tested.
	 * @return the result of calling the reference
	 */
	protected C callRef() {
		try {
			return ref.call();
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Gets the test evaluator for the test methods.
	 * @return the test evaluator
	 */
	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	/**
	 * Sets the test evaluator for the test methods.
	 * @param testEvaluator the test evaluator to set
	 */
	protected void setEvaluator(TestEvaluator testEvaluator) {
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Gets the return object for the test methods.
	 * @return the return object
	 */
	protected R getReturnObj() {
		return returnObj;
	}

	/**
	 * Sets the return object for the test methods.
	 * @param returnObj the return object to set
	 */
	protected void setReturnObj(R returnObj) {
		this.returnObj = returnObj;
	}

	/**
	 * Tests if the object being tested is equal to the given value.
	 * @param val the value to compare to the object being tested
	 * @return the return object for the test methods
	 */
	public R equals(C val) {
		return getEvaluator().testCondition(
				"equals", op -> op.addValue("value", val).addValue("actual",callRef()),
				() -> {
					C refVal = callRef();
					return (refVal == null && val == null) || (refVal != null && val != null && refVal.compareTo(val) == 0);
				},
				returnObj, testContext);
	}

	/**
	 * Tests if the object being tested is not equal to the given value.
	 * @param val the value to compare to the object being tested
	 * @return the return object for the test methods
	 */
	public R notEquals(C val) {
		return getEvaluator().testCondition(
				"not equals", op -> op.addValue("value", val).addValue("actual",callRef()),
				() -> {
					C refVal = callRef();
					return (refVal == null && val != null) || (refVal != null && (val == null || refVal.compareTo(val) != 0));
				},
				returnObj, testContext);
	}

	/**
	 * Tests if the object being tested is greater than the given value.
	 * @param val the value to compare to the object being tested
	 * @return the return object for the test methods
	 */
	public R greaterThan(C val) {
		return getEvaluator().testCondition(
				"greater than", op -> op.addValue("value", val).addValue("actual",callRef()),
				() -> callRef().compareTo(val) > 0, returnObj, testContext);
	}

	/**
	 * Tests if the object being tested is not greater than the given value.
	 * @param val the value to compare to the object being tested
	 * @return the return object for the test methods
	 */
	public R notGreaterThan(C val) {
		return getEvaluator().testCondition(
				"not greater than", op -> op.addValue("value", val).addValue("actual",callRef()),
				() -> callRef().compareTo(val) <= 0, returnObj, testContext);
	}

	/**
	 * Tests if the object being tested is less than the given value.
	 * @param val the value to compare to the object being tested
	 * @return the return object for the test methods
	 */
	public R lessThan(C val) {
		return getEvaluator().testCondition(
				"less than", op -> op.addValue("value", val).addValue("actual",callRef()),
				() -> callRef().compareTo(val) < 0, returnObj, testContext);
	}

	/**
	 * Tests if the object being tested is not less than the given value.
	 * @param val the value to compare to the object being tested
	 * @return the return object for the test methods
	 */
	public R notLessThan(C val) {
		return getEvaluator().testCondition(
				"not less than", op -> op.addValue("value", val).addValue("actual",callRef()),
				() -> callRef().compareTo(val) >= 0, returnObj, testContext);
	}

	/**
	 * Returns a StringTester for testing the string representation of the object being tested.
	 * @return a StringTester for testing the string representation of the object being tested
	 */
	public StringTester<R> asString() {
		return new StringTester<>(() -> callRef().toString(), returnObj, testContext, getEvaluator());
	}

	/**
	 * Returns a new ComparableTester for testing the result of applying the given transform function to the object being tested.
	 * @param transform the transform function to apply to the object being tested
	 * @param <T> the type of the transformed object, which must implement Comparable
	 * @return a new ComparableTester for testing the result of applying the given transform function to the object being tested
	 */
	public <T extends Comparable<T>> ComparableTester<T, R> transform(ThrowingFunction<C,T,?> transform) {
		return new ComparableTester<>(() -> ThrowingFunction.unchecked(transform).apply(callRef()), returnObj, testContext, getEvaluator());
	}

	/**
	 * Stores the value of the object being tested in the test context with the given key.
	 * @param key the key to store the value under
	 * @return the return object for the test methods
	 */
	public R storeValue(String key) {
		testContext.store(key, callRef());
		return returnObj;
	}

	/**
	 * Stores the value of the object being tested in the test context with the given key, using the given default value if the object being tested is null.
	 * @param key the key to store the value under
	 * @param defaultVal the default value to use if the object being tested is null
	 * @return the return object for the test methods
	 */
	public R storeValue(String key, C defaultVal) {
		C val = callRef();
		if (val == null) {
			val = defaultVal;
		}
		testContext.store(key, val);
		return returnObj;
	}
}