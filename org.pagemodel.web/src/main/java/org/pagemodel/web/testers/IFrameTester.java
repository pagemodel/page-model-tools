package org.pagemodel.web.testers;

import org.openqa.selenium.Beta;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.PageUtils;

public class IFrameTester<R, N extends PageModel<? super N>> extends WebElementTester<R,N> {

	public IFrameTester(R returnObj, ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		super(returnObj, clickAction, testEvaluator);
	}

	public IFrameTester(ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		super(clickAction, testEvaluator);
	}

	@Beta
	public N testIFrame(){
		return getEvaluator().testCondition(
				"switch to iframe", op -> op
						.addValue("element", getElementJson()),
				() -> page.getContext().getDriver().switchTo().frame(callRef()) != null,
				PageUtils.waitForModelDisplayed(clickAction.getReturnPage()), page.getContext());
	}

	public <T extends PageModel<? super T>> T testIFrame(Class<T> clazz){
		return getEvaluator().testCondition(
				"switch to iframe", op -> op
						.addValue("expected", clazz.getSimpleName())
						.addValue("element", getElementJson()),
				() -> page.getContext().getDriver().switchTo().frame(callRef()) != null,
				page.testPage().testPageModel(clazz), page.getContext());
	}
}
