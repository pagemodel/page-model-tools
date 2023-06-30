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

import org.openqa.selenium.Rectangle;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.WebTestContext;
import org.pagemodel.web.utils.RectangleUtils;

import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class BoundsTester<R> extends RectangleTester<R> {
	private Rectangle bounds;

	public BoundsTester(Rectangle bounds, Callable<Rectangle> ref, R returnObj, WebTestContext testContext, TestEvaluator testEvaluator) {
		super(ref, returnObj, testContext, testEvaluator);
		this.bounds = bounds;
	}

	@Override
	public Rectangle callRef(){
		Rectangle ref = super.callRef();
		if(ref == null){
			return bounds;
		}
		if(bounds == null){
			return ref;
		}
		return RectangleUtils.merge(ref, bounds);
	}

	@Override
	public BoundsTester<R> extend(Rectangle includeBounds){
		Rectangle bounds = callRef();
		Rectangle merged = RectangleUtils.merge(this.bounds, includeBounds);
		Rectangle newBounds = RectangleUtils.merge(bounds, includeBounds);
		getEvaluator().logEvent(TestEvaluator.TEST_BUILD,
				"bounds", op -> op
						.addObject("current", RectangleUtils.rectangleJson(bounds))
						.addObject("include", RectangleUtils.rectangleJson(includeBounds))
						.addObject("new", RectangleUtils.rectangleJson(newBounds)));
		return new BoundsTester<>(merged, ref, returnObj, testContext, getEvaluator());
	}
}
