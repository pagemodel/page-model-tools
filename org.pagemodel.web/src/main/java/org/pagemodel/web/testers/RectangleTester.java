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
import org.pagemodel.web.utils.RectangleUtils;
import org.pagemodel.web.utils.Screenshot;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class RectangleTester<R> extends HasPageBounds {
	protected final R returnObj;
	protected final Callable<Rectangle> ref;
	protected final WebTestContext testContext;
	private final TestEvaluator testEvaluator;
	protected final String name;

	public RectangleTester(String name, Callable<Rectangle> ref, R returnObj, WebTestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
		this.name = name;
	}

	public RectangleTester(Callable<Rectangle> ref, R returnObj, WebTestContext testContext, TestEvaluator testEvaluator) {
		this("Location", ref, returnObj, testContext, testEvaluator);
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

	protected String getName() {
		return name;
	}

	public R storeValue(String key) {
		testContext.store(key, callRef());
		return returnObj;
	}

	public RectangleTester<R> top() {
		return transformRect("top", rect -> rect == null ? null : new Rectangle(rect.getX(), rect.getY(), 0, rect.getWidth()));
	}

	public RectangleTester<R> right() {
		return transformRect("right", rect -> rect == null ? null : new Rectangle(rect.getX() + rect.getWidth(), rect.getY(), rect.getHeight(), 0));
	}

	public RectangleTester<R> bottom() {
		return transformRect("bottom", rect -> rect == null ? null : new Rectangle(rect.getX(), rect.getY() + rect.getHeight(), 0, rect.getWidth()));
	}

	public RectangleTester<R> left() {
		return transformRect("left", rect -> rect == null ? null : new Rectangle(rect.getX(), rect.getY(), rect.getHeight(), 0));
	}

	public PointTester<R> center() {
		return new PointTester<>(getName() + ".center", () -> getPoint(r -> new Point(r.x + r.width/2, r.y + r.height/2)), returnObj, testContext, getEvaluator());
	}

	public PointTester<R> topLeft() {
		return new PointTester<>(getName() + ".topLeft", () -> getPoint(0, 0), returnObj, testContext, getEvaluator());
	}

	public PointTester<R> topRight() {
		return new PointTester<>(getName() + ".topRight", () -> getPoint(1, 0), returnObj, testContext, getEvaluator());
	}

	public PointTester<R> bottomLeft() {
		return new PointTester<>(getName() + ".bottomLeft", () -> getPoint(0, 1), returnObj, testContext, getEvaluator());
	}

	public PointTester<R> bottomRight() {
		return new PointTester<>(getName() + ".bottomRight", () -> getPoint(1, 1), returnObj, testContext, getEvaluator());
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

	private RectangleTester<R> transformRect(String name, ThrowingFunction<Rectangle,Rectangle,?> transform) {
		return new RectangleTester<>(getName() + "." + name, () -> ThrowingFunction.unchecked(transform).apply(callRef()), returnObj, testContext, getEvaluator());
	}

	public RectangleTester<R> transform(ThrowingFunction<Rectangle,Rectangle,?> transform) {
		return transformRect("transform", transform);
	}

	public RectangleTester<R> translate(int x, int y) {
		return transformRect("translate(" + x + "," + y +")", rect -> new Rectangle(rect.x + x, rect.y + y, rect.height, rect.width));
	}

	public RectangleTester<R> setOrigin(int x, int y) {
		return transformRect("setOrigin(" + x + "," + y +")", rect -> new Rectangle(x, y, rect.height, rect.width));
	}

	public RectangleTester<R> pad(int...padding) {
		return transformRect("pad(" + Arrays.toString(padding) + ")", rect ->
				RectangleUtils.pad(getEvaluator(), callRef(), padding));
	}

	public BoundsTester<R> extend(Rectangle includeBounds){
		Rectangle bounds = callRef();
		Rectangle newBounds = RectangleUtils.merge(bounds, includeBounds);
		getEvaluator().logEvent(TestEvaluator.TEST_BUILD,
				"bounds", op -> op
						.addObject("current", RectangleUtils.rectangleJson(bounds))
						.addObject("include", RectangleUtils.rectangleJson(includeBounds))
						.addObject("new", RectangleUtils.rectangleJson(newBounds)));
		return new BoundsTester<>(includeBounds, ref, returnObj, testContext, getEvaluator());
	}

	public BoundsTester<R> extend(Point point){
		Rectangle includeBounds = new Rectangle(point.getX(), point.getY(), 0, 0);
		return extend(includeBounds);
	}

	public BoundsTester<R> extend(int x, int y){
		Rectangle includeBounds = new Rectangle(x, y, 0, 0);
		return extend(includeBounds);
	}

	public RectangleTester<R> extend(ThrowingFunction<R,? extends HasPageBounds,?> getBounds){
		return new RectangleTester<>(getName() + ".extend",
				() -> RectangleUtils.merge(callRef(), ThrowingFunction.unchecked(getBounds).apply(returnObj).getBounds()),
				returnObj, testContext, getEvaluator());
//		Rectangle includeBounds = ThrowingFunction.unchecked(getBounds).apply(returnObj).getBounds();
//		return extend(includeBounds);
	}

	public RectangleTester<R> extendWidth(ThrowingFunction<R,? extends HasPageBounds,?> getBounds){
		return new RectangleTester<>(getName() + ".extendWidth",
				() -> RectangleUtils.mergeWidth(callRef(), ThrowingFunction.unchecked(getBounds).apply(returnObj).getBounds()),
				returnObj, testContext, getEvaluator());
	}

	public RectangleTester<R> extendHeight(ThrowingFunction<R,? extends HasPageBounds,?> getBounds){
		return new RectangleTester<>(getName() + ".extendHeight",
				() -> RectangleUtils.mergeHeight(callRef(), ThrowingFunction.unchecked(getBounds).apply(returnObj).getBounds()),
				returnObj, testContext, getEvaluator());
	}

	public R takeScreenshot(String filename){
		Screenshot.takeScreenshot(testContext.getDriver(), callRef(), filename, false, 0);
		return returnObj;
	}

	public ImageAnnotator<R> editScreenshot(){
		Rectangle rect = callRef();
		BufferedImage image = Screenshot.getScreenshot(testContext.getDriver(), rect, true, 0);
		return new ImageAnnotator<>(rect.getPoint(), () -> image, returnObj, testContext, getEvaluator());
	}

	@Override
	protected Rectangle getBounds() {
		return callRef();
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

	protected Point getPoint(ThrowingFunction<Rectangle,Point,?> pointFunc) {
		Rectangle rect = callRef();
		if (rect == null) {
			return null;
		}
		return ThrowingFunction.unchecked(pointFunc).apply(rect);
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
