package org.pagemodel.web.testers;

import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.PageModel;

public class DropDownTester<R, P extends PageModel<? super P>> extends SelectTester<R, P> {
	public DropDownTester(ClickAction<?, P> clickAction, TestEvaluator testEvaluator) {
		super(clickAction, testEvaluator);
	}

	public DropDownTester(R returnObj, ClickAction<?, P> clickAction, TestEvaluator testEvaluator) {
		super(returnObj, clickAction, testEvaluator);
	}

	// ================ begin public testers ======================
	public DropDownOptionTester<R> testOption(int index) {
		return new DropDownOptionTester<>("index", index, this, getReturnObj(), ClickAction.make(() -> getOption(index), (P) page, getEvaluator()), getEvaluator());
	}

	public DropDownOptionTester<R> testOptionByText(String text) {
		return new DropDownOptionTester<>("text", text, this, getReturnObj(), ClickAction.make(() -> getOptionByText(text), (P) page, getEvaluator()), getEvaluator());
	}

	public DropDownOptionTester<R> testOptionByValue(String value) {
		return new DropDownOptionTester<>("value", value, this, getReturnObj(), ClickAction.make(() -> getOptionByValue(value), (P) page, getEvaluator()), getEvaluator());
	}
	// ================ end public testers ========================

	public class DropDownOptionTester<R> extends OptionTester<R> {
		public DropDownOptionTester(String type, Object val, SelectTester<R, P> select, R returnObj, ClickAction<?, P> clickAction, TestEvaluator testEvaluator) {
			super(type, val, select, returnObj, clickAction, testEvaluator);
		}

		protected void selectByType() {
			String text = callRef().getText();
			select.selectText(text);
		}
	}
}
