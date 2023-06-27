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

import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.pagemodel.core.testers.StringTester;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.json.JsonBuilder;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.WebTestContext;

import java.util.Map;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * This class provides methods to test and interact with alert dialogs in a web page.
 * @param <R> the return type of the test method
 */
public class AlertTester<R> {

	/**
	 * The object to be returned by the test method.
	 */
	protected final R returnObj;

	/**
	 * The context of the web test.
	 */
	protected final WebTestContext testContext;

	/**
	 * The page model of the web page being tested.
	 */
	protected PageModel<?> page;

	/**
	 * The evaluator used to evaluate the test conditions.
	 */
	private TestEvaluator testEvaluator;

	/**
	 * Constructs an AlertTester object with the given parameters.
	 * @param page the page model of the web page being tested
	 * @param returnObj the object to be returned by the test method
	 * @param testContext the context of the web test
	 * @param testEvaluator the evaluator used to evaluate the test conditions
	 */
	public AlertTester(PageModel<?> page, R returnObj, WebTestContext testContext, TestEvaluator testEvaluator) {
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Returns the evaluator used to evaluate the test conditions.
	 * @return the evaluator used to evaluate the test conditions
	 */
	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	/**
	 * Tests if an alert dialog exists.
	 * @return the object to be returned by the test method
	 */
	public R exists() {
		return getEvaluator().testCondition(
				"exists", op -> op
						.addValue("alert", getAlertJson()),
				() -> getAlert() != null, returnObj, testContext);
	}

	/**
	 * Tests if an alert dialog does not exist.
	 * @return the object to be returned by the test method
	 */
	public R notExists() {
		return getEvaluator().testCondition(
				"not exists", op -> op
						.addValue("alert", getAlertJson()),
				() -> getAlert() == null, returnObj, testContext);
	}

	/**
	 * Accepts an alert dialog.
	 * @return the object to be returned by the test method
	 */
	public R accept() {
		getEvaluator().logEvent(TestEvaluator.TEST_EXECUTE, "accept", op -> op.merge(getAlertJson()));
		exists();
		getAlert().accept();
		return returnObj;
	}

	/**
	 * Dismisses an alert dialog.
	 * @return the object to be returned by the test method
	 */
	public R dismiss() {
		getEvaluator().logEvent(TestEvaluator.TEST_EXECUTE,"dismiss", op -> op.merge(getAlertJson()));
		exists();
		getAlert().dismiss();
		return returnObj;
	}

	/**
	 * Returns a StringTester object to test the text of an alert dialog.
	 * @return a StringTester object to test the text of an alert dialog
	 */
	public StringTester<R> text() {
		return new StringTester<>(() -> getAlert().getText(), returnObj, testContext, getEvaluator());
	}

	/**
	 * Sends keys to an alert dialog.
	 * @param text the text to be sent to the alert dialog
	 * @return the object to be returned by the test method
	 */
	public R sendKeys(String text) {
		getEvaluator().logEvent(TestEvaluator.TEST_EXECUTE,"send keys", op -> op
				.addValue("value", text)
				.merge(getAlertJson()));
		exists();
		getAlert().sendKeys(text);
		return returnObj;
	}

	/**
	 * Returns a new AlertTester object with a wait condition.
	 * @return a new AlertTester object with a wait condition
	 */
	public AlertTester<R> waitFor() {
		return new AlertTester<>(page, returnObj, testContext, new WebTestEvaluator.Wait(testContext, WebElementTester.WebElementWait.DEFAULT_WAIT_SEC));
	}

	/**
	 * Returns a new AlertTester object with a wait and refresh condition.
	 * @return a new AlertTester object with a wait and refresh condition
	 */
	public AlertTester<R> waitAndRefreshFor() {
		return new AlertTester<>(page, returnObj, testContext, new WebTestEvaluator.WaitAndRefresh<>(testContext, WebElementTester.WebElementRefresh.DEFAULT_WAIT_SEC, returnObj, page));
	}

	/**
	 * Returns the alert dialog currently displayed in the web page.
	 * @return the alert dialog currently displayed in the web page, or null if no alert dialog is displayed
	 */
	protected Alert getAlert() {
		try {
			return testContext.getDriver().switchTo().alert();
		} catch (NoAlertPresentException ex) {
			return null;
		}
	}

	/**
	 * Returns a JSON object representing the alert dialog currently displayed in the web page.
	 * @return a JSON object representing the alert dialog currently displayed in the web page
	 */
	protected Map<String,Object> getAlertJson() {
		String text;
		try {
			text = testContext.getDriver().switchTo().alert().getText();
		} catch (Exception ex) {
			text = null;
		}
		return JsonBuilder.object()
				.addValue("alert", text)
				.toMap();
	}

	/**
	 * Returns a string representation of the alert dialog currently displayed in the web page.
	 * @return a string representation of the alert dialog currently displayed in the web page
	 */
	protected String getAlertDisplay() {
		String text;
		try {
			text = testContext.getDriver().switchTo().alert().getText();
		} catch (Exception ex) {
			text = null;
		}
		return "\"alert\": \"" + text + "\"";
	}
}