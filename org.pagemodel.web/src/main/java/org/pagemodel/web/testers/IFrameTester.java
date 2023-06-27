package org.pagemodel.web.testers;

import org.openqa.selenium.Beta;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.PageUtils;

/**
 * This class represents a tester for an iFrame element. It extends the WebElementTester class and
 * provides methods to test the iFrame element.
 *
 * @param <R> the type of the return object
 * @param <N> the type of the page model
 */
public class IFrameTester<R, N extends PageModel<? super N>> extends WebElementTester<R,N> {

	/**
	 * Constructs an IFrameTester object with the given return object, click action, and test evaluator.
	 *
	 * @param returnObj the return object
	 * @param clickAction the click action
	 * @param testEvaluator the test evaluator
	 */
	public IFrameTester(R returnObj, ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		super(returnObj, clickAction, testEvaluator);
	}

	/**
	 * Constructs an IFrameTester object with the given click action and test evaluator.
	 *
	 * @param clickAction the click action
	 * @param testEvaluator the test evaluator
	 */
	public IFrameTester(ClickAction<?, N> clickAction, TestEvaluator testEvaluator) {
		super(clickAction, testEvaluator);
	}

	/**
	 * Tests the iFrame element and returns the page model.
	 *
	 * @return the page model
	 */
	@Beta
	public N testIFrame(){
		return getEvaluator().testCondition(
				"switch to iframe", op -> op
						.addValue("element", getElementJson()),
				() -> page.getContext().getDriver().switchTo().frame(callRef()) != null,
				PageUtils.waitForModelDisplayed(clickAction.getReturnPage()), page.getContext());
	}

	/**
	 * Tests the iFrame element and returns the page model of the specified class.
	 *
	 * @param clazz the class of the page model
	 * @param <T> the type of the page model
	 * @return the page model of the specified class
	 */
	public <T extends PageModel<? super T>> T testIFrame(Class<T> clazz){
		return getEvaluator().testCondition(
				"switch to iframe", op -> op
						.addValue("expected", clazz.getSimpleName())
						.addValue("element", getElementJson()),
				() -> page.getContext().getDriver().switchTo().frame(callRef()) != null,
				page.testPage().testPageModel(clazz), page.getContext());
	}
}