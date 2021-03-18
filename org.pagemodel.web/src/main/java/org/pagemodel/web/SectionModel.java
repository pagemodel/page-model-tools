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
import org.pagemodel.core.utils.ThrowingConsumer;
import org.pagemodel.core.utils.ThrowingFunction;
import org.pagemodel.core.utils.ThrowingRunnable;
import org.pagemodel.web.testers.AlertTester;
import org.pagemodel.web.testers.ClickAction;
import org.pagemodel.web.testers.PageTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public abstract class SectionModel<S extends SectionModel<? super S, P, R>, P extends PageModel<? super P>, R extends PageModel<? super R>> extends ComponentModel<S, R, S> implements PageModel<S> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected WebTestContext testContext;

	public static <S extends SectionModel<? super S, P, R>, P extends PageModel<? super P>, R extends PageModel<? super R>> S make(final Class<S> sectionClass, ClickAction<P, R> clickAction) {
		try {
			return sectionClass.getConstructor(ClickAction.class).newInstance(clickAction);
		} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
	}

	protected P parentPage;
	private TestEvaluator testEvaluator;

	public SectionModel(ClickAction<P, ?> clickAction) {
		super(null, ClickAction.makeNav(clickAction.getElementRef(), null, (R)null), clickAction.getPage().getEvaluator());
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

	protected void setTestEvaluator(TestEvaluator testEvaluator){
		this.testEvaluator = testEvaluator;
	}

	@Override
	public TestEvaluator getEvaluator() {
		return testEvaluator;
	}

	public P testSectionParent() {
		return parentPage;
	}

	@Override
	public WebTestContext getContext() {
		return testContext;
	}

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

	@Override
	protected S asSection() {
		return (S)this;
	}

	protected void logModelDisplayed() {
		testModelDisplayed().accept((S) this);
	}

	protected Consumer<S> testModelDisplayed(){
		return page -> {};
	}

	@Override
	public PageTester<S> testPage() {
		return new PageTester<>((S) this, getContext());
	}

	@Override
	public AlertTester<S> testAlert() {
		return new AlertTester<>((S) this, getContext());
	}

	@Override
	public <R extends PageModel<? super R>> R expectRedirect(Class<R> returnPageClass) {
		R returnPage = PageUtils.makeInstance(returnPageClass, getContext());
		return PageUtils.waitForModelDisplayed(returnPage);
	}

	@Override
	public void onPageLeave() {
	}

	@Override
	public void onPageLoad() {
	}

	@Override
	public void closeBrowser() {
		testContext.quit();
	}

	@Override
	public S doAction(ThrowingRunnable<?> action) {
		try {
			action.run();
			return (S) this;
		} catch (Throwable t) {
			throw testContext.createException("Error: Page action failed.", t);
		}
	}

	@Override
	public <R> R doAction(Callable<R> action) {
		try {
			return action.call();
		} catch (Throwable t) {
			throw testContext.createException("Error: Page action failed.", t);
		}
	}

	@Override
	public <R> R doAction(ThrowingFunction<S, R, ?> action) {
		try {
			return action.apply((S) this);
		} catch (Throwable t) {
			throw testContext.createException("Error: Page action failed.", t);
		}
	}

	@Override
	public S doAction(ThrowingConsumer<S, ?> action) {
		try {
			S t = (S) this;
			action.accept(t);
			return t;
		} catch (Throwable t) {
			throw testContext.createException("Error: Page action failed.", t);
		}
	}
}
