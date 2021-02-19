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
import org.pagemodel.web.PageModel;
import org.pagemodel.web.utils.PageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class CheckboxTester<R, N extends PageModel<? super N>> extends WebElementTester<R, N> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public CheckboxTester(R returnObj, ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		super(returnObj, clickAction, testEvaluator);
	}

	public CheckboxTester(ClickAction<?, N> clickAction) {
		super(clickAction);
	}

	public R setCheckbox(boolean value) {
		if (value) {
			return setChecked();
		} else {
			return setUnchecked();
		}
	}

	public R setChecked() {
		log.info(getEvaluator().getActionMessage(() -> "set checked: " + getElementDisplay() + " on page [" + page.getClass().getName() + "]"));
		getEvaluator().quiet().testCondition(() -> "exists: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> callRef().hasElement(), page, page.getContext());
		try {
			if (checkedAttrVals.contains(callRef().getAttribute("checked"))) {
				return getReturnObj();
			}
			callRef().click();
			return getEvaluator().quiet().testCondition(() -> "checked: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
					() -> checkedAttrVals.contains(callRef().getAttribute("checked")), getReturnObj(), page.getContext());
		}catch (PageException ex){
			throw ex;
		}catch (Exception ex){
			throw page.getContext().createException("set checked: " + getElementDisplay() + " on page [" + page.getClass().getName() + "]", ex);
		}
	}

	public R setUnchecked() {
		log.info(getEvaluator().getActionMessage(() -> "set unchecked: " + getElementDisplay() + " on page [" + page.getClass().getName() + "]"));
		getEvaluator().quiet().testCondition(() -> "exists: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> callRef().hasElement(), page, page.getContext());
		try{
			if (!checkedAttrVals.contains(callRef().getAttribute("checked"))) {
				return getReturnObj();
			}
			callRef().click();
			return getEvaluator().quiet().testCondition(() -> "unchecked: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
					() -> !checkedAttrVals.contains(callRef().getAttribute("checked")), getReturnObj(), page.getContext());
		}catch (PageException ex){
			throw ex;
		}catch (Exception ex){
			throw page.getContext().createException("set unchecked: " + getElementDisplay() + " on page [" + page.getClass().getName() + "]", ex);
		}
	}

	private final static List<String> checkedAttrVals = Arrays.asList("true", "checked");

	@Override
	public R notSelected() {
		return getEvaluator().testCondition(() -> "unchecked: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> !checkedAttrVals.contains(callRef().getAttribute("checked")), getReturnObj(), page.getContext());
	}

	@Override
	public R isSelected() {
		return getEvaluator().testCondition(() -> "checked: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> checkedAttrVals.contains(callRef().getAttribute("checked")), getReturnObj(), page.getContext());
	}
}
