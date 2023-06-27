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

import org.openqa.selenium.Point;
import org.pagemodel.core.TestContext;
import org.pagemodel.core.testers.ComparableTester;
import org.pagemodel.core.testers.TestEvaluator;

import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * The PointTester class is used to test the x and y coordinates of a Point object.
 * It contains methods to retrieve a ComparableTester object for the x and y coordinates.
 * @param <R> the type of the return object
 */
public class PointTester<R> {

	/**
	 * The return object of the PointTester.
	 */
	protected final R returnObj;

	/**
	 * The Callable object that returns a Point to be tested.
	 */
	protected final Callable<Point> ref;

	/**
	 * The TestContext object that contains information about the test.
	 */
	protected final TestContext testContext;

	/**
	 * The TestEvaluator object used to evaluate the test results.
	 */
	private TestEvaluator testEvaluator;

	/**
	 * Constructs a new PointTester object with the given parameters.
	 * @param ref the Callable object that returns a Point to be tested
	 * @param returnObj the return object of the PointTester
	 * @param testContext the TestContext object that contains information about the test
	 * @param testEvaluator the TestEvaluator object used to evaluate the test results
	 */
	public PointTester(Callable<Point> ref, R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Calls the Callable object to retrieve the Point object to be tested.
	 * @return the Point object to be tested
	 */
	protected Point callRef() {
		try {
			return ref.call();
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Returns the TestEvaluator object used to evaluate the test results.
	 * @return the TestEvaluator object used to evaluate the test results
	 */
	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	/**
	 * Returns a ComparableTester object for the x coordinate of the Point object.
	 * @return a ComparableTester object for the x coordinate of the Point object
	 */
	public ComparableTester<Integer, R> x() {
		return new ComparableTester<>(() -> callRef().getX(), returnObj, testContext, getEvaluator());
	}

	/**
	 * Returns a ComparableTester object for the y coordinate of the Point object.
	 * @return a ComparableTester object for the y coordinate of the Point object
	 */
	public ComparableTester<Integer, R> y() {
		return new ComparableTester<>(() -> callRef().getY(), returnObj, testContext, getEvaluator());
	}

}