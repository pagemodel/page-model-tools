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
import org.openqa.selenium.Rectangle;
import org.pagemodel.core.TestContext;
import org.pagemodel.core.testers.ComparableTester;
import org.pagemodel.core.testers.TestEvaluator;

import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * The RectangleTester class provides methods for testing the properties of a Rectangle object.
 * It contains methods for testing the coordinates of the four corners of the rectangle, as well as
 * its width and height. It also provides methods for getting the x and y coordinates of the top-left,
 * top-right, bottom-left, and bottom-right corners of the rectangle.
 *
 * @param <R> the type of the object being tested
 */
public class RectangleTester<R> {

	/**
	 * The object to be returned by the test methods.
	 */
	protected final R returnObj;

	/**
	 * The Callable object that returns the Rectangle to be tested.
	 */
	protected final Callable<Rectangle> ref;

	/**
	 * The TestContext object that provides context for the tests.
	 */
	protected final TestContext testContext;

	/**
	 * The TestEvaluator object that evaluates the test results.
	 */
	private TestEvaluator testEvaluator;

	/**
	 * Constructs a new RectangleTester object with the given Callable, return object, TestContext,
	 * and TestEvaluator.
	 *
	 * @param ref the Callable object that returns the Rectangle to be tested
	 * @param returnObj the object to be returned by the test methods
	 * @param testContext the TestContext object that provides context for the tests
	 * @param testEvaluator the TestEvaluator object that evaluates the test results
	 */
	public RectangleTester(Callable<Rectangle> ref, R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Calls the Callable object to get the Rectangle to be tested.
	 *
	 * @return the Rectangle to be tested, or null if an exception occurs
	 */
	protected Rectangle callRef() {
		try {
			return ref.call();
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Gets the TestEvaluator object that evaluates the test results.
	 *
	 * @return the TestEvaluator object that evaluates the test results
	 */
	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	/**
	 * Returns a PointTester object for testing the top-left corner of the rectangle.
	 *
	 * @return a PointTester object for testing the top-left corner of the rectangle
	 */
	public PointTester<R> topLeft() {
		return new PointTester<>(() -> getPoint(0, 0), returnObj, testContext, getEvaluator());
	}

	/**
	 * Returns a PointTester object for testing the top-right corner of the rectangle.
	 *
	 * @return a PointTester object for testing the top-right corner of the rectangle
	 */
	public PointTester<R> topRight() {
		return new PointTester<>(() -> getPoint(1, 0), returnObj, testContext, getEvaluator());
	}

	/**
	 * Returns a PointTester object for testing the bottom-left corner of the rectangle.
	 *
	 * @return a PointTester object for testing the bottom-left corner of the rectangle
	 */
	public PointTester<R> bottomLeft() {
		return new PointTester<>(() -> getPoint(0, 1), returnObj, testContext, getEvaluator());
	}

	/**
	 * Returns a PointTester object for testing the bottom-right corner of the rectangle.
	 *
	 * @return a PointTester object for testing the bottom-right corner of the rectangle
	 */
	public PointTester<R> bottomRight() {
		return new PointTester<>(() -> getPoint(1, 1), returnObj, testContext, getEvaluator());
	}

	/**
	 * Returns a ComparableTester object for testing the x-coordinate of the top-left corner of the rectangle.
	 *
	 * @return a ComparableTester object for testing the x-coordinate of the top-left corner of the rectangle
	 */
	public ComparableTester<Integer, R> x1() {
		return new ComparableTester<>(() -> getX(0), returnObj, testContext, getEvaluator());
	}

	/**
	 * Returns a ComparableTester object for testing the y-coordinate of the top-left corner of the rectangle.
	 *
	 * @return a ComparableTester object for testing the y-coordinate of the top-left corner of the rectangle
	 */
	public ComparableTester<Integer, R> y1() {
		return new ComparableTester<>(() -> getY(0), returnObj, testContext, getEvaluator());
	}

	/**
	 * Returns a ComparableTester object for testing the x-coordinate of the bottom-right corner of the rectangle.
	 *
	 * @return a ComparableTester object for testing the x-coordinate of the bottom-right corner of the rectangle
	 */
	public ComparableTester<Integer, R> x2() {
		return new ComparableTester<>(() -> getX(1), returnObj, testContext, getEvaluator());
	}

	/**
	 * Returns a ComparableTester object for testing the y-coordinate of the bottom-right corner of the rectangle.
	 *
	 * @return a ComparableTester object for testing the y-coordinate of the bottom-right corner of the rectangle
	 */
	public ComparableTester<Integer, R> y2() {
		return new ComparableTester<>(() -> getY(1), returnObj, testContext, getEvaluator());
	}

	/**
	 * Returns a ComparableTester object for testing the width of the rectangle.
	 *
	 * @return a ComparableTester object for testing the width of the rectangle
	 */
	public ComparableTester<Integer, R> width() {
		return new ComparableTester<>(() -> callRef().getWidth(), returnObj, testContext, getEvaluator());
	}

	/**
	 * Returns a ComparableTester object for testing the height of the rectangle.
	 *
	 * @return a ComparableTester object for testing the height of the rectangle
	 */
	public ComparableTester<Integer, R> height() {
		return new ComparableTester<>(() -> callRef().getHeight(), returnObj, testContext, getEvaluator());
	}

	/**
	 * Calculates and returns the Point object at the specified corner of the rectangle.
	 *
	 * @param x_i 0 for the left side, 1 for the right side
	 * @param y_i 0 for the top side, 1 for the bottom side
	 * @return the Point object at the specified corner of the rectangle, or null if an exception occurs
	 */
	protected Point getPoint(int x_i, int y_i) {
		Rectangle rect = callRef();
		if (rect == null) {
			return null;
		}
		int x = x_i == 0 ? rect.x : rect.x + rect.width;
		int y = y_i == 0 ? rect.y : rect.y + rect.height;
		return new Point(x, y);
	}

	/**
	 * Calculates and returns the x-coordinate of the specified side of the rectangle.
	 *
	 * @param i 0 for the left side, 1 for the right side
	 * @return the x-coordinate of the specified side of the rectangle, or null if an exception occurs
	 */
	protected Integer getX(int i) {
		Rectangle rect = callRef();
		if (rect == null) {
			return null;
		}

		return i == 0 ? rect.x : rect.x + rect.width;
	}

	/**
	 * Calculates and returns the y-coordinate of the specified side of the rectangle.
	 *
	 * @param i 0 for the top side, 1 for the bottom side
	 * @return the y-coordinate of the specified side of the rectangle, or null if an exception occurs
	 */
	protected Integer getY(int i) {
		Rectangle rect = callRef();
		if (rect == null) {
			return null;
		}
		return i == 0 ? rect.y : rect.y + rect.height;
	}
}