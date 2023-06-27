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

import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * The DimensionTester class is used to test the width and height of a Dimension object.
 * @param <R> the type of the object being tested
 */
public class DimensionTester<R> {

	/**
	 * The object to be returned after testing.
	 */
	protected final R returnObj;

	/**
	 * The callable reference to the Dimension object being tested.
	 */
	protected final Callable<Dimension> ref;

	/**
	 * The TestContext object used for testing.
	 */
	protected final TestContext testContext;

	/**
	 * The TestEvaluator object used for testing.
	 */
	private TestEvaluator testEvaluator;

	/**
	 * Constructs a new DimensionTester object.
	 * @param ref the callable reference to the Dimension object being tested
	 * @param returnObj the object to be returned after testing
	 * @param testContext the TestContext object used for testing
	 * @param testEvaluator the TestEvaluator object used for testing
	 */
	public DimensionTester(Callable<Dimension> ref, R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Calls the callable reference to get the Dimension object being tested.
	 * @return the Dimension object being tested
	 */
	protected Dimension callRef() {
		try {
			return ref.call();
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Gets the TestEvaluator object used for testing.
	 * @return the TestEvaluator object used for testing
	 */
	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	/**
	 * Creates a new ComparableTester object to test the width of the Dimension object being tested.
	 * @return a new ComparableTester object to test the width of the Dimension object being tested
	 */
	public ComparableTester<Integer, R> width() {
		return new ComparableTester<>(() -> callRef().getWidth(), returnObj, testContext, getEvaluator());
	}

	/**
	 * Creates a new ComparableTester object to test the height of the Dimension object being tested.
	 * @return a new ComparableTester object to test the height of the Dimension object being tested
	 */
	public ComparableTester<Integer, R> height() {
		return new ComparableTester<>(() -> callRef().getHeight(), returnObj, testContext, getEvaluator());
	}
}