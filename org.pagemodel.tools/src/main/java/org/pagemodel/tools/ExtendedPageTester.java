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

package org.pagemodel.tools;

import org.pagemodel.core.testers.StringTester;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.tools.accessibility.AXEScanner;
import org.pagemodel.tools.screenshots.LocationGroup;
import org.pagemodel.tools.screenshots.LocationGroupsTester;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.testers.*;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class ExtendedPageTester<P extends PageModel<? super P>> extends PageTester<P> {
	public ExtendedPageTester(P page, ExtendedTestContext testContext, TestEvaluator testEvaluator) {
		super(page, testContext, testEvaluator);
	}

	@Override
	public WebElementTester<P, P> testFocusedElement() {
		return super.testFocusedElement();
	}

	@Override
	public WebElementTester<P, P> testHTMLElement() {
		return super.testHTMLElement();
	}

	@Override
	public WebElementTester<P, P> testBodyElement() {
		return super.testBodyElement();
	}

	@Override
	public PageWait<P> waitFor() {
		return super.waitFor();
	}

	@Override
	public PageTesterBase<P> waitAndRefreshFor() {
		return super.waitAndRefreshFor();
	}

	@Override
	public P setWindowSize(int width, int height) {
		return super.setWindowSize(width, height);
	}

	@Override
	public P maximizeWindow() {
		return super.maximizeWindow();
	}

	@Override
	public P fullscreenWindow() {
		return super.fullscreenWindow();
	}

	@Override
	public P setWindowPosition(int x, int y) {
		return super.setWindowPosition(x, y);
	}

	@Override
	public P moveWindowPositionByOffset(int offsetX, int offsetY) {
		return super.moveWindowPositionByOffset(offsetX, offsetY);
	}

	@Override
	public P takeScreenshot(String filePrefix) {
		return super.takeScreenshot(filePrefix);
	}

	@Override
	public <T extends PageModel<? super T>> T testPageModel(Class<T> clazz) {
		return super.testPageModel(clazz);
	}

	@Override
	public <T extends PageModel<? super T>> T navigateTo(String url, Class<T> clazz) {
		return super.navigateTo(url, clazz);
	}

	@Override
	public StringTester<P> title() {
		return super.title();
	}

	@Override
	public StringTester<P> url() {
		return super.url();
	}

	@Override
	public StringTester<P> pageSource() {
		return super.pageSource();
	}

	@Override
	public DimensionTester<P> windowSize(){
		return super.windowSize();
	}

	@Override
	public PointTester<P> windowPosition(){
		return super.windowPosition();
	}

	public LocationGroup<P> testLocationGroup() {
		return new LocationGroup<>(page);
	}

	public LocationGroupsTester<P> testLocationGroups(String orderPrefix, int startOrder) {
		return new LocationGroupsTester<>(orderPrefix, startOrder, page, null);
	}

	public LocationGroupsTester<P> testLocationGroups() {
		return testLocationGroups("", 1);
	}

	public P testAccessibility() {
		return testAccessibility(AXEScanner.DEFAULT_TIMEOUT);
	}

	public P testAccessibility(int timeoutSec) {
		return getEvaluator().testCondition(
				"accessibility", op -> op
						.addValue("model", page.getClass().getSimpleName()),
				() -> new AXEScanner().analyzeAccessibility(testContext, page.getClass().getSimpleName(), timeoutSec),
				page,
				testContext);
	}

	public P testAccessibility(String... expectedViolations) {
		return testAccessibility(AXEScanner.DEFAULT_TIMEOUT, expectedViolations);
	}

	public P testAccessibility(int timeoutSec, String... expectedViolations) {
		return getEvaluator().testCondition(
				"accessibility", op -> op
						.addValue("expectedViolations", String.join(", ", expectedViolations))
						.addValue("model", page.getClass().getSimpleName()),
				() -> {
					AXEScanner scanner = new AXEScanner();
					scanner.setExpectedViolations(expectedViolations);
					return scanner.analyzeAccessibility(testContext, page.getClass().getSimpleName(), timeoutSec);
				},
				page,
				testContext
		);
	}
}
