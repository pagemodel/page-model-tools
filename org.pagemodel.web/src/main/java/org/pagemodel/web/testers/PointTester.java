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
import org.pagemodel.core.testers.ComparableTester;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.ThrowingFunction;
import org.pagemodel.web.WebTestContext;

import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class PointTester<R> extends HasPageBounds {
	protected final R returnObj;
	protected final Callable<Point> ref;
	protected final WebTestContext testContext;
	private TestEvaluator testEvaluator;
	protected final String name;

	public PointTester(String name, Callable<Point> ref, R returnObj, WebTestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
		this.name = name;
	}

	public PointTester(Callable<Point> ref, R returnObj, WebTestContext testContext, TestEvaluator testEvaluator) {
		this("Point", ref, returnObj, testContext, testEvaluator);
	}

	protected Point callRef() {
		try {
			return ref.call();
		} catch (Exception ex) {
			return null;
		}
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	public String getName() {
		return name;
	}

	public R storeValue(String key) {
		testContext.store(key, callRef());
		return returnObj;
	}

	public ComparableTester<Integer, R> x() {
		return new ComparableTester<>(() -> callRef().getX(), returnObj, testContext, getEvaluator());
	}

	public ComparableTester<Integer, R> y() {
		return new ComparableTester<>(() -> callRef().getY(), returnObj, testContext, getEvaluator());
	}

	public PointTester<R> transform(ThrowingFunction<Point,Point,?> transform) {
		return new PointTester<>(() -> ThrowingFunction.unchecked(transform).apply(callRef()), returnObj, testContext, getEvaluator());
	}

	public PointTester<R> translate(int x, int y) {
		return transform(rect -> new Point(rect.x + x, rect.y + y));
	}

	public RectangleTester<R> asRectangle(int width, int height) {
		return new RectangleTester<>(() -> toRect(callRef(), width, height), returnObj, testContext, getEvaluator());
	}

	public RectangleTester<R> pad(int...padding){
		return asRectangle().pad(padding);
	}

	public RectangleTester<R> asRectangle(){
		return asRectangle(0, 0);
	}

	private static Rectangle toRect(Point p, int width, int height){
		int x1 = p.x, x2 = p.x + width, y1 = p.y, y2 = p.y + height;
		int xmin = Math.min(x1, x2);
		int xmax = Math.max(x1, x2);
		int ymin = Math.min(y1, y2);
		int ymax = Math.max(y1, y2);
		return new Rectangle(xmin, ymin, ymax - ymin, xmax - xmin);
	}

	@Override
	protected Rectangle getBounds() {
		Point p = callRef();
		if(p == null){
			return null;
		}
		return new Rectangle(p.x, p.y, 0, 0);
	}
}
