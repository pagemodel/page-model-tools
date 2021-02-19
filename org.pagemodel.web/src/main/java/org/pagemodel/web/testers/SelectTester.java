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

import org.openqa.selenium.support.ui.Select;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.PageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class SelectTester<R, N extends PageModel<? super N>> extends WebElementTester<R, N> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public SelectTester(R returnObj, ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		super(returnObj, clickAction, testEvaluator);
	}

	public SelectTester(ClickAction<?, N> clickAction) {
		super(clickAction);
	}

	public R selectValue(String value) {
		log.info(getEvaluator().getActionMessage(() -> "set select: " + getElementDisplay() + " to [" + value + "] on page [" + page.getClass().getName() + "]"));
		getEvaluator().quiet().testCondition(() -> "exists: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> callRef().hasElement(), page, page.getContext());
		try {
			Select dropdown = new Select(callRef());
			dropdown.selectByVisibleText(value);
		} catch (Exception ex) {
			throw page.getContext().createException("set select: " + getElementDisplay() + " to [" + value + "] on page [" + page.getClass().getName() + "]");
		}
		return getReturnObj();
	}

}
