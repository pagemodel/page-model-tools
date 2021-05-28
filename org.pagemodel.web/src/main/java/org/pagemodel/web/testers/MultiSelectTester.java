package org.pagemodel.web.testers;

import org.openqa.selenium.support.ui.Select;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.PageModel;

public class MultiSelectTester<R, P extends PageModel<? super P>> extends SelectTester<R, P> {
	public MultiSelectTester(ClickAction<?, P> clickAction, TestEvaluator testEvaluator) {
		super(clickAction, testEvaluator);
	}

	public MultiSelectTester(R returnObj, ClickAction<?, P> clickAction, TestEvaluator testEvaluator) {
		super(returnObj, clickAction, testEvaluator);
	}

	// ================ begin public testers ======================
	public MultiSelectOptionTester<R> testOption(int index) {
		return new MultiSelectOptionTester<>("index", index, this, getReturnObj(), ClickAction.make(() -> getOption(index), (P) page, getEvaluator()), getEvaluator());
	}

	public MultiSelectOptionTester<R> testOptionByText(String text) {
		return new MultiSelectOptionTester<>("text", text, this, getReturnObj(), ClickAction.make(() -> getOptionByText(text), (P) page, getEvaluator()), getEvaluator());
	}

	public MultiSelectOptionTester<R> testOptionByValue(String value) {
		return new MultiSelectOptionTester<>("value", value, this, getReturnObj(), ClickAction.make(() -> getOptionByValue(value), (P) page, getEvaluator()), getEvaluator());
	}
	// ================ end public testers ========================

	public R clearSelected() {
		return getEvaluator().testRun(TestEvaluator.TEST_EXECUTE,
				"clear selected", op -> op
						.addValue("element", getElementJson()),
				() -> new Select(callRef()).deselectAll(),
				getReturnObj(), page.getContext());
	}

	public class MultiSelectOptionTester<R> extends OptionTester<R> {
		public MultiSelectOptionTester(String type, Object val, SelectTester<R, P> select, R returnObj, ClickAction<?, P> clickAction, TestEvaluator testEvaluator) {
			super(type, val, select, returnObj, clickAction, testEvaluator);
		}

		@Override
		protected void selectByType() {
			if ("index".equals(type)) {
				new Select(select.callRef()).selectByIndex((int) val);
			} else if ("text".equals(type)) {
				new Select(select.callRef()).selectByVisibleText((String) val);
			} else if ("value".equals(type)) {
				new Select(select.callRef()).selectByValue((String) val);
			}
		}

		public R setUnselected(){
			return getEvaluator().testRun(TestEvaluator.TEST_EXECUTE,
					"set unselected", op -> op
							.addValue("element", getElementJson()),
					this::unselectByType,
					getReturnObj(), page.getContext());
		}

		protected void unselectByType() {
			if ("index".equals(type)) {
				new Select(select.callRef()).deselectByIndex((int) val);
			} else if ("text".equals(type)) {
				new Select(select.callRef()).deselectByVisibleText((String) val);
			} else if ("value".equals(type)) {
				new Select(select.callRef()).deselectByValue((String) val);
			}
		}
	}
}
