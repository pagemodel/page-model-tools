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

package org.pagemodel.web;

import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.TestRuntimeException;
import org.pagemodel.core.utils.ThrowingConsumer;
import org.pagemodel.core.utils.ThrowingFunction;
import org.pagemodel.core.utils.ThrowingRunnable;
import org.pagemodel.web.testers.AlertTester;
import org.pagemodel.web.testers.ClickAction;
import org.pagemodel.web.testers.PageTester;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * This class represents a complex reusable component of a page, or a component which captures the page focus, such as a dialog.
 * It is treated as both a PageModel and WebElementTester.
 * It is similar to a ComponentModel, but maintains test focus returning itself rather than its parent.
 */
public abstract class SectionModel<S extends SectionModel<? super S, P, R>, P extends PageModel<? super P>, R extends PageModel<? super R>> extends ComponentModel<S, R, S> implements PageModel<S> {

	protected WebTestContext testContext;

	/**
	 * Creates a new instance of the SectionModel class.
	 * @param clickAction The click action.
	 * @param testEvaluator The test evaluator.
	 * @return The new instance of the SectionModel class.
	 */
	public static <S extends SectionModel<? super S, P, R>, P extends PageModel<? super P>, R extends PageModel<? super R>> S make(final Class<S> sectionClass, ClickAction<P, R> clickAction, TestEvaluator testEvaluator) {
		try {
			return sectionClass.getConstructor(ClickAction.class, TestEvaluator.class).newInstance(clickAction, testEvaluator);
		} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
	}

	protected P parentPage;
	private TestEvaluator testEvaluator;

	/**
	 * Creates a new instance of the SectionModel class.
	 * @param clickAction The click action.
	 * @param testEvaluator The test evaluator.
	 */
	public SectionModel(ClickAction<P, ?> clickAction, TestEvaluator testEvaluator) {
		super(null, ClickAction.makeNav(clickAction.getElementRef(), null, (R)null, testEvaluator), testEvaluator);
		setReturnObj((S)this);
		this.page = (S) this;
		this.parentPage = clickAction.getPage();
		ClickAction<S, R> sClick = (ClickAction<S, R>) clickAction;
		if(clickAction.getPage().getClass().equals(clickAction.getReturnPage().getClass())) {
			sClick.withPage((S) this);
			sClick.withReturnPage((R) this);
		}else{
			sClick.withPage((S) this);
		}
		this.clickAction = sClick;
		this.testContext = parentPage.getContext();
		this.testEvaluator = new TestEvaluator.Now();
	}

	/**
	 * Sets the test evaluator.
	 * @param testEvaluator The test evaluator.
	 */
	protected void setTestEvaluator(TestEvaluator testEvaluator){
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Gets the test evaluator.
	 * @return The test evaluator.
	 */
	@Override
	public TestEvaluator getEvaluator() {
		return testEvaluator;
	}

	/**
	 * Tests the section parent.
	 * @return The section parent.
	 */
	public P testSectionParent() {
		return parentPage;
	}

	/**
	 * Gets the web test context.
	 * @return The web test context.
	 */
	@Override
	public WebTestContext getContext() {
		return testContext;
	}

	/**
	 * Tests if the model is displayed.
	 * @return True if the model is displayed, false otherwise.
	 */
	@Override
	public boolean modelDisplayed() {
		TestEvaluator contextEvaluator = getContext().getEvaluator();
		TestEvaluator.NoException eval = new TestEvaluator.NoException(getEvaluator());
		setTestEvaluator(eval);
		try {
			getContext().setEvaluator(eval);
			testModelDisplayed().accept((S) this);
			return eval.getTestStatus();
		}finally{
			getContext().setEvaluator(contextEvaluator);
			setTestEvaluator(eval.getInnerEvaluator());
		}
	}

	/**
	 * Casts the section to a section model.
	 * @return The section model.
	 */
	@Override
	protected S asSection() {
		return (S)this;
	}

	/**
	 * Logs the model displayed.
	 */
	protected void logModelDisplayed() {
		testModelDisplayed().accept((S) this);
	}

	/**
	 * Tests the model displayed.
	 * @return The consumer.
	 */
	protected Consumer<S> testModelDisplayed(){
		return page -> {};
	}

	/**
	 * Tests the page.
	 * @return The page tester.
	 */
	@Override
	public PageTester<S> testPage() {
		return new PageTester<>((S) this, getContext(), getEvaluator());
	}

	/**
	 * Tests the alert.
	 * @return The alert tester.
	 */
	@Override
	public AlertTester<S> testAlert() {
		return new AlertTester<>(this, (S) this, getContext(), getEvaluator());
	}

	/**
	 * Expects a redirect.
	 * @param returnPageClass The return page class.
	 * @return The return page.
	 */
	@Override
	public <R extends PageModel<? super R>> R expectRedirect(Class<R> returnPageClass) {
		return PageUtils.waitForNavigateToPage(returnPageClass, getContext());
	}

	/**
	 * Called when the page is left.
	 */
	@Override
	public void onPageLeave() {
	}

	/**
	 * Called when the page is loaded.
	 */
	@Override
	public void onPageLoad() {
	}

	/**
	 * Closes the browser.
	 */
	@Override
	public void closeBrowser() {
		testContext.quit();
	}

	/**
	 * Performs an action.
	 * @param action The action.
	 * @return The section model.
	 */
	@Override
	public S doAction(ThrowingRunnable<?> action) {
		try {
			action.run();
			return (S) this;
		} catch (TestRuntimeException t) {
			throw t;
		} catch (Throwable t) {
			throw testContext.createException("Error: Page action failed.", t);
		}
	}

	/**
	 * Performs an action.
	 * @param action The action.
	 * @return The result of the action.
	 */
	@Override
	public <R> R doAction(Callable<R> action) {
		try {
			return action.call();
		} catch (TestRuntimeException t) {
			throw t;
		} catch (Throwable t) {
			throw testContext.createException("Error: Page action failed.", t);
		}
	}

	/**
	 * Performs an action.
	 * @param action The action.
	 * @return The result of the action.
	 */
	@Override
	public <R> R doAction(ThrowingFunction<S, R, ?> action) {
		try {
			return action.apply((S) this);
		} catch (TestRuntimeException t) {
			throw t;
		} catch (Throwable t) {
			throw testContext.createException("Error: Page action failed.", t);
		}
	}

	/**
	 * Performs an action.
	 * @param action The action.
	 * @return The section model.
	 */
	@Override
	public S doAction(ThrowingConsumer<S, ?> action) {
		try {
			S t = (S) this;
			action.accept(t);
			return t;
		} catch (TestRuntimeException t) {
			throw t;
		} catch (Throwable t) {
			throw testContext.createException("Error: Page action failed.", t);
		}
	}
}