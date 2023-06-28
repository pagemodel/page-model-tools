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
import org.pagemodel.core.utils.json.JsonObjectBuilder;
import org.pagemodel.web.WebTestContext;
import org.pagemodel.web.utils.Screenshot;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class RectangleTester<R> {

	protected final R returnObj;
	protected final Callable<Rectangle> ref;
	protected final WebTestContext testContext;
	private TestEvaluator testEvaluator;

	public RectangleTester(Callable<Rectangle> ref, R returnObj, WebTestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	protected Rectangle callRef() {
		try {
			return ref.call();
		} catch (Exception ex) {
			return null;
		}
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	public R storeValue(String key) {
		testContext.store(key, callRef());
		return returnObj;
	}

	public PointTester<R> topLeft() {
		return new PointTester<>(() -> getPoint(0, 0), returnObj, testContext, getEvaluator());
	}

	public PointTester<R> topRight() {
		return new PointTester<>(() -> getPoint(1, 0), returnObj, testContext, getEvaluator());
	}

	public PointTester<R> bottomLeft() {
		return new PointTester<>(() -> getPoint(0, 1), returnObj, testContext, getEvaluator());
	}

	public PointTester<R> bottomRight() {
		return new PointTester<>(() -> getPoint(1, 1), returnObj, testContext, getEvaluator());
	}

	public ComparableTester<Integer, R> x1() {
		return new ComparableTester<>(() -> getX(0), returnObj, testContext, getEvaluator());
	}

	public ComparableTester<Integer, R> y1() {
		return new ComparableTester<>(() -> getY(0), returnObj, testContext, getEvaluator());
	}

	public ComparableTester<Integer, R> x2() {
		return new ComparableTester<>(() -> getX(1), returnObj, testContext, getEvaluator());
	}

	public ComparableTester<Integer, R> y2() {
		return new ComparableTester<>(() -> getY(1), returnObj, testContext, getEvaluator());
	}

	public ComparableTester<Integer, R> width() {
		return new ComparableTester<>(() -> callRef().getWidth(), returnObj, testContext, getEvaluator());
	}

	public ComparableTester<Integer, R> height() {
		return new ComparableTester<>(() -> callRef().getHeight(), returnObj, testContext, getEvaluator());
	}

	public BoundsTester<R> include(Rectangle includeBounds){
		Rectangle bounds = callRef();
		Rectangle newBounds = merge(bounds, includeBounds);
		getEvaluator().logEvent(TestEvaluator.TEST_BUILD,
				"bounds", op -> op
						.addObject("current", rectangleJson(bounds))
						.addObject("include", rectangleJson(includeBounds))
						.addObject("new", rectangleJson(newBounds)));
		return new BoundsTester<>(includeBounds, ref, returnObj, testContext, getEvaluator());
	}

	public BoundsTester<R> include(Point includeBounds){
		Rectangle bounds = callRef();
		Rectangle newBounds = merge(bounds, includeBounds);
		getEvaluator().logEvent(TestEvaluator.TEST_BUILD,
				"bounds", op -> op
						.addObject("current", rectangleJson(bounds))
						.addObject("include", pointJson(includeBounds))
						.addObject("new", rectangleJson(newBounds)));
		return new BoundsTester<>(newBounds, ref, returnObj, testContext, getEvaluator());
	}

	public R takeScreenshot(String filename, int padding){
		Screenshot.takeScreenshot(testContext.getDriver(), callRef(), padding, filename, false);
		return returnObj;
	}

	public R takeScreenshot(String filename){
		return takeScreenshot(filename, 0);
	}

	protected Rectangle merge(Rectangle a, Rectangle b){
		if(a == null){
			return b;
		}else if(b == null){
			return a;
		}
		int xmin = Math.min(a.x, b.x);
		int xmax = Math.max(a.x + a.width, b.x + b.width);
		int ymin = Math.min(a.y, b.y);
		int ymax = Math.max(a.y + a.height, b.y + b.height);
		return new Rectangle(xmin, ymin, ymax - ymin, xmax - xmin);
	}

	protected Rectangle merge(Rectangle a, Point b){
		if(a == null && b == null){
			return null;
		}else if(a == null){
			return new Rectangle(b.x, b.y, 1, 1);
		}else if(b == null){
			return a;
		}
		int xmin = Math.min(a.x, b.x);
		int xmax = Math.max(a.x + a.width, b.x);
		int ymin = Math.min(a.y, b.y);
		int ymax = Math.max(a.y + a.height, b.y);
		return new Rectangle(xmin, ymin, ymax - ymin, xmax - xmin);
	}

	protected Consumer<JsonObjectBuilder> rectangleJson(Rectangle a){
		if(a == null){
			return json -> json
					.addValue("x", "null")
					.addValue("y", "null");
		}
		return json -> json
				.addValue("x", a.x)
				.addValue("y", a.y)
				.addValue("width", a.width)
				.addValue("height", a.height);
	}

	protected Consumer<JsonObjectBuilder> pointJson(Point a){
		if(a == null){
			return json -> json
				.addValue("x", "null")
				.addValue("y", "null");
		}
		return json -> json
				.addValue("x", a.x)
				.addValue("y", a.y);
	}

	protected Point getPoint(int x_i, int y_i) {
		Rectangle rect = callRef();
		if (rect == null) {
			return null;
		}
		int x = x_i == 0 ? rect.x : rect.x + rect.width;
		int y = y_i == 0 ? rect.y : rect.y + rect.height;
		return new Point(x, y);
	}

	protected Integer getX(int i) {
		Rectangle rect = callRef();
		if (rect == null) {
			return null;
		}

		return i == 0 ? rect.x : rect.x + rect.width;
	}

	protected Integer getY(int i) {
		Rectangle rect = callRef();
		if (rect == null) {
			return null;
		}
		return i == 0 ? rect.y : rect.y + rect.height;
	}
}
