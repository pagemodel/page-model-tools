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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Quotes;
import org.openqa.selenium.support.ui.Select;
import org.pagemodel.core.testers.StringListTester;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.ComponentModel;
import org.pagemodel.web.LocatedWebElement;
import org.pagemodel.web.PageModel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class SelectTester<R, P extends PageModel<? super P>> extends ComponentModel<R,P,SelectTester<R,P>> {
	public SelectTester(ClickAction<?, P> clickAction, TestEvaluator testEvaluator) {
		super(clickAction, testEvaluator);
	}

	public SelectTester(R returnObj, ClickAction<?, P> clickAction, TestEvaluator testEvaluator) {
		super(returnObj, clickAction, testEvaluator);
	}

	public class SelectTesterNew_section extends SelectTester<SelectTesterNew_section, P> {
		public SelectTesterNew_section(ClickAction<?, P> clickAction, TestEvaluator testEvaluator) {
			super(clickAction, testEvaluator);
			setReturnObj(this);
		}

		public P testSectionParent() {
			return (P)page;
		}
	}

	public SelectTesterNew_section asSection(){
		return new SelectTesterNew_section(clickAction, getEvaluator());
	}

	// ================ begin protected web elements ==============
	protected LocatedWebElement getOption(int num) {
		return findComponentElement("Option", By.xpath("./option[" + num + "]"));
	}

	protected LocatedWebElement getOptionByText(String text) {
		return findComponentElement("OptionByText", By.xpath(".//option[normalize-space(.) = " + Quotes.escape(text) + "]"));
	}

	protected LocatedWebElement getOptionByValue(String value) {
		return findComponentElement("OptionByValue", By.xpath("./option[@value = " + Quotes.escape(value) + "]"));
	}
	// ================ end protected web elements ================

	// ================ begin public testers ======================
	public OptionTester<R> testOption(int index) {
		return new OptionTester<>("index", index, this, getReturnObj(), ClickAction.make(() -> getOption(index), (P)page, getEvaluator()), getEvaluator());
	}

	public OptionTester<R> testOptionByText(String text) {
		return new OptionTester<>("text", text, this, getReturnObj(), ClickAction.make(() -> getOptionByText(text), (P)page, getEvaluator()), getEvaluator());
	}

	public OptionTester<R> testOptionByValue(String value) {
		return new OptionTester<>("value", value, this, getReturnObj(), ClickAction.make(() -> getOptionByValue(value), (P)page, getEvaluator()), getEvaluator());
	}
	// ================ end public testers ========================

	public R selectText(String text) {
		return getEvaluator().testRun(TestEvaluator.TEST_EXECUTE,
				"select text", op -> op
						.addValue("value", text)
						.addValue("element", getElementJson()),
				() -> new Select(callRef()).selectByVisibleText(text),
				getReturnObj(), page.getContext());
	}
	public R selectValue(String value) {
		return getEvaluator().testRun(TestEvaluator.TEST_EXECUTE,
				"select value", op -> op
						.addValue("value", value)
						.addValue("element", getElementJson()),
				() -> new Select(callRef()).selectByValue(value),
				getReturnObj(), page.getContext());
	}

	public R selectIndex(int index) {
		return getEvaluator().testRun(TestEvaluator.TEST_EXECUTE,
				"select index", op -> op
						.addValue("value", index)
						.addValue("element", getElementJson()),
				() -> new Select(callRef()).selectByIndex(index),
				getReturnObj(), page.getContext());
	}

	public StringListTester<R> optionsTextList(){
		return new StringListTester<>(this::getSelectOptionsText, getReturnObj(), click().getContext(), getEvaluator());
	}
	public StringListTester<R> optionsValueList(){
		return new StringListTester<>(this::getSelectOptionsValues, getReturnObj(), click().getContext(), getEvaluator());
	}

	protected List<String> getSelectOptionsText(){
		List<WebElement> options = new Select(callRef()).getOptions();
		return options.stream().map(WebElement::getText).collect(Collectors.toList());

	}

	protected List<String> getSelectOptionsValues(){
		List<WebElement> options = new Select(callRef()).getOptions();
		return options.stream().map(x -> x.getAttribute("value")).collect(Collectors.toList());

	}

	public class OptionTester<R> extends WebElementTester<R,P> {
		protected String type;
		protected Object val;
		protected SelectTester<R,P> select;

		public OptionTester(String type, Object val, SelectTester<R,P> select, R returnObj, ClickAction<?, P> clickAction, TestEvaluator testEvaluator) {
			super(returnObj, clickAction, testEvaluator);
			this.type = type;
			this.val = val;
			this.select = select;
		}

		public R setSelected(){
			return getEvaluator().testRun(TestEvaluator.TEST_EXECUTE,
					"set selected", op -> op
							.addValue("element", getElementJson()),
					() -> selectByType(),
					getReturnObj(), page.getContext());
		}

		protected void selectByType(){
			if("index".equals(type)){
				new Select(select.callRef()).selectByIndex((int)val);
			}else if("text".equals(type)){
				new Select(select.callRef()).selectByVisibleText((String)val);
			}else if("value".equals(type)){
				new Select(select.callRef()).selectByValue((String)val);
			}
		}
	}

}
