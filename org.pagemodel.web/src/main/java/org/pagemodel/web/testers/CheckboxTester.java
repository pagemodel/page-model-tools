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

import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.json.JsonBuilder;
import org.pagemodel.web.PageModel;

import java.util.Arrays;
import java.util.List;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * This class represents a tester for a checkbox element. It extends the WebElementTester class and
 * provides methods for setting and checking the state of the checkbox.
 *
 * @param <R> the type of the return object
 * @param <N> the type of the page model
 */
public class CheckboxTester<R, N extends PageModel<? super N>> extends WebElementTester<R, N> {

	/**
	 * Constructs a CheckboxTester with a return object, click action, and test evaluator.
	 *
	 * @param returnObj the return object
	 * @param clickAction the click action
	 * @param testEvaluator the test evaluator
	 */
	public CheckboxTester(R returnObj, ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		super(returnObj, clickAction, testEvaluator);
	}

	/**
	 * Constructs a CheckboxTester with a click action and test evaluator.
	 *
	 * @param clickAction the click action
	 * @param testEvaluator the test evaluator
	 */
	public CheckboxTester(ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		super(clickAction, testEvaluator);
	}

	/**
	 * Sets the state of the checkbox to the given value.
	 *
	 * @param value the value to set the checkbox to
	 * @return the return object
	 */
	public R setCheckbox(boolean value) {
		if (value) {
			return setChecked();
		} else {
			return setUnchecked();
		}
	}

	/**
	 * Sets the state of the checkbox to checked.
	 *
	 * @return the return object
	 */
	public R setChecked() {
		getEvaluator().logEvent(TestEvaluator.TEST_EXECUTE, "set checked", op -> op
				.addValue("element", getElementJson()));
		getEvaluator().quiet().testCondition("exists", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement(), page, page.getContext());
		try {
			if (callRef().getAttribute("checked") != null) {
				return getReturnObj();
			}
			callRef().click();
			return getEvaluator().quiet().testCondition("checked", op -> op
						.addValue("element", getElementJson()),
					() -> callRef().getAttribute("checked") != null, getReturnObj(), page.getContext());
		}catch (Exception ex){
			throw page.getContext().createException(JsonBuilder.toMap(getEvaluator().getExecuteEvent(
					"set checked", op -> op.addValue("element", getElementJson()))), ex);
		}
	}

	/**
	 * Sets the state of the checkbox to unchecked.
	 *
	 * @return the return object
	 */
	public R setUnchecked() {
		getEvaluator().logEvent(TestEvaluator.TEST_EXECUTE, "set unchecked", op -> op
				.addValue("element", getElementJson()));
		getEvaluator().quiet().testCondition("exists", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().hasElement(), page, page.getContext());
		try{
			if (callRef().getAttribute("checked") == null) {
				return getReturnObj();
			}
			callRef().click();
			return getEvaluator().quiet().testCondition("unchecked", op -> op
						.addValue("element", getElementJson()),
					() -> callRef().getAttribute("checked") == null, getReturnObj(), page.getContext());
		}catch (Exception ex){
			throw page.getContext().createException(JsonBuilder.toMap(getEvaluator().getExecuteEvent(
					"set unchecked", op -> op.addValue("element", getElementJson()))), ex);
		}
	}

	/**
	 * A list of attribute values that indicate the checkbox is checked.
	 */
	private final static List<String> checkedAttrVals = Arrays.asList("true", "checked");

	/**
	 * Checks if the checkbox is not selected.
	 *
	 * @return the return object
	 */
	@Override
	public R notSelected() {
		return getEvaluator().testCondition(
				"unchecked", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().getAttribute("checked") == null,
				getReturnObj(), page.getContext());
	}

	/**
	 * Checks if the checkbox is selected.
	 *
	 * @return the return object
	 */
	@Override
	public R isSelected() {
		return getEvaluator().testCondition(
				"checked", op -> op
						.addValue("element", getElementJson()),
				() -> callRef().getAttribute("checked") != null,
				getReturnObj(), page.getContext());
	}
}