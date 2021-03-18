package org.pagemodel.web.testers;

import org.openqa.selenium.Beta;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class IFrameTester<R, N extends PageModel<? super N>> extends WebElementTester<R,N> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public IFrameTester(R returnObj, ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		super(returnObj, clickAction, testEvaluator);
	}

	public IFrameTester(ClickAction<?, N> clickAction) {
		super(clickAction);
	}

	@Beta
	public N testIFrame(){
		return getEvaluator().testCondition(() ->  "switch to iframe: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> page.getContext().getDriver().switchTo().frame(callRef()) != null,
				PageUtils.waitForModelDisplayed(clickAction.getReturnPage()), page.getContext());
	}

	public <T extends PageModel<? super T>> T testIFrame(Class<T> clazz){
		return getEvaluator().testCondition(() ->  "switch to iframe: " + getElementDisplay() + " on page [" + page.getClass().getSimpleName() + "]",
				() -> page.getContext().getDriver().switchTo().frame(callRef()) != null,
				page.testPage().testPageModel(clazz), page.getContext());
	}
}
