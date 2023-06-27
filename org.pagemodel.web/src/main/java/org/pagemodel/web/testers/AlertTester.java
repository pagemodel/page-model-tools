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
public class AlertTester<R> {
	protected final R returnObj;
	protected final WebTestContext testContext;
	protected PageModel<?> page;
	private TestEvaluator testEvaluator;

	public AlertTester(PageModel<?> page, R returnObj, WebTestContext testContext, TestEvaluator testEvaluator) {
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	public R exists() {
		return getEvaluator().testCondition(
				"exists", op -> op
						.addValue("alert", getAlertJson()),
				() -> getAlert() != null, returnObj, testContext);
	}

	public R notExists() {
		return getEvaluator().testCondition(
				"not exists", op -> op
						.addValue("alert", getAlertJson()),
				() -> getAlert() == null, returnObj, testContext);
	}

	public R accept() {
		getEvaluator().logEvent(TestEvaluator.TEST_EXECUTE, "accept", op -> op.merge(getAlertJson()));
		exists();
		getAlert().accept();
		return returnObj;
	}

	public R dismiss() {
		getEvaluator().logEvent(TestEvaluator.TEST_EXECUTE,"dismiss", op -> op.merge(getAlertJson()));
		exists();
		getAlert().dismiss();
		return returnObj;
	}

	public StringTester<R> text() {
		return new StringTester<>(() -> getAlert().getText(), returnObj, testContext, getEvaluator());
	}

	public R sendKeys(String text) {
		getEvaluator().logEvent(TestEvaluator.TEST_EXECUTE,"send keys", op -> op
				.addValue("value", text)
				.merge(getAlertJson()));
		exists();
		getAlert().sendKeys(text);
		return returnObj;
	}

	public AlertTester<R> waitFor() {
		return new AlertTester<>(page, returnObj, testContext, new WebTestEvaluator.Wait(testContext, WebElementTester.WebElementWait.DEFAULT_WAIT_SEC));
	}

	public AlertTester<R> waitAndRefreshFor() {
		return new AlertTester<>(page, returnObj, testContext, new WebTestEvaluator.WaitAndRefresh<>(testContext, WebElementTester.WebElementRefresh.DEFAULT_WAIT_SEC, returnObj, page));
	}

	protected Alert getAlert() {
		try {
			return testContext.getDriver().switchTo().alert();
		} catch (NoAlertPresentException ex) {
			return null;
		}
	}

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
