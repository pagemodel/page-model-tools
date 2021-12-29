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

import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.ThrowingConsumer;
import org.pagemodel.core.utils.ThrowingFunction;
import org.pagemodel.web.LocatedWebElement;
import org.pagemodel.web.PageModel;
import org.pagemodel.web.PageUtils;
import org.pagemodel.web.SectionModel;
import org.pagemodel.web.utils.PageException;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class ClickAction<P extends PageModel<? super P>, N extends PageModel<? super N>> {
	protected final Callable<WebElement> elementRef;
	protected P page;
	protected N returnPage;
	protected TestEvaluator testEvaluator;

	public static enum AlertAction {
		ACCEPT,
		DISMISS,
		NONE
	}

	public static int DEFAULT_NAV_WAIT_SEC = 10;
	public static boolean DEFAULT_ALERT_WAIT_FLAG = false;
	public static AlertAction DEFAULT_ALERT_ACTION = AlertAction.NONE;
	public static int DEFAULT_ALERT_WAIT_SEC = 1;

	private boolean navFlag;
	private int navWait;
	private boolean alertWaitFlag;
	private int alertWaitSec;
	private AlertAction alertAction;
	private ThrowingConsumer<P, ?> preAction;
	private ThrowingConsumer<P, ?> postAction;
	private ThrowingFunction<ClickContext, N, ?> doClickOverride;

	public static <T extends PageModel<? super T>> ClickAction<T, T> make(Callable<WebElement> elementRef, T page, TestEvaluator testEvaluator) {
		return new ClickAction<T, T>(elementRef, page, (Class<T>) page.getClass(), false,
				DEFAULT_NAV_WAIT_SEC, DEFAULT_ALERT_WAIT_FLAG, DEFAULT_ALERT_WAIT_SEC, DEFAULT_ALERT_ACTION, testEvaluator);
	}

	public static <T extends PageModel<? super T>, U extends PageModel<? super U>> ClickAction<T, U> makeNav(Callable<WebElement> elementRef, T page, Class<U> returnType, TestEvaluator testEvaluator) {
		return new ClickAction<T, U>(elementRef, page, returnType, true,
				DEFAULT_NAV_WAIT_SEC, DEFAULT_ALERT_WAIT_FLAG, DEFAULT_ALERT_WAIT_SEC, DEFAULT_ALERT_ACTION, testEvaluator);
	}

	public static <T extends PageModel<? super T>, U extends PageModel<? super U>> ClickAction<T, U> makeNav(Callable<WebElement> elementRef, T page, U returnPage, TestEvaluator testEvaluator) {
		return new ClickAction<T, U>(elementRef, page, returnPage, true,
				DEFAULT_NAV_WAIT_SEC, DEFAULT_ALERT_WAIT_FLAG, DEFAULT_ALERT_WAIT_SEC, DEFAULT_ALERT_ACTION, testEvaluator);
	}

	private ClickAction(Callable<WebElement> elementRef, P page, Class<N> returnType, boolean navFlag, int navWaitSec,
			boolean waitForAlert, int alertWaitSec, AlertAction alertAction, TestEvaluator testEvaluator) {
		this(elementRef, page, determineClickReturn(elementRef, page, returnType), navFlag, navWaitSec, waitForAlert, alertWaitSec, alertAction, testEvaluator);
	}

	private static <T extends PageModel<? super T>, S extends SectionModel<? super S, T, T>> T determineClickReturn(
			Callable<WebElement> elementRef, PageModel<?> currentPage, Class<? extends T> returnClass) {
		if (currentPage.getClass().equals(returnClass)) {
			return (T) currentPage;
		} else if (SectionModel.class.isAssignableFrom(returnClass)) {
			return (T) SectionModel.make((Class<S>) returnClass, ClickAction.make(elementRef, (T)currentPage, currentPage.getEvaluator()), currentPage.getEvaluator());
		}
		return PageUtils.makeInstance(returnClass, currentPage.getContext());
	}

	private ClickAction(Callable<WebElement> elementRef, P page, N returnPage, boolean navFlag, int navWaitSec,
			boolean waitForAlert, int alertWaitSec, AlertAction alertAction, TestEvaluator testEvaluator) {
		this.elementRef = elementRef;
		this.page = page;
		this.returnPage = returnPage;
		this.navFlag = navFlag;
		this.navWait = navWaitSec;
		this.alertWaitFlag = waitForAlert;
		this.alertWaitSec = alertWaitSec;
		this.alertAction = alertAction;
		this.testEvaluator = testEvaluator;
	}

	public ClickAction(Callable<WebElement> elementRef, P page, Class<N> returnType, TestEvaluator testEvaluator) {
		this(elementRef, page, returnType, true,
				DEFAULT_NAV_WAIT_SEC, DEFAULT_ALERT_WAIT_FLAG, DEFAULT_ALERT_WAIT_SEC, DEFAULT_ALERT_ACTION, testEvaluator);
	}

	public ClickAction(Callable<WebElement> elementRef, P page, TestEvaluator testEvaluator) {
		this(elementRef, page, (Class<N>) page.getClass(), false,
				DEFAULT_NAV_WAIT_SEC, DEFAULT_ALERT_WAIT_FLAG, DEFAULT_ALERT_WAIT_SEC, DEFAULT_ALERT_ACTION, testEvaluator);
	}

	protected LocatedWebElement callRef() {
		try {
			WebElement el = elementRef.call();
			if (el == null) {
				return new LocatedWebElement(null, null, (String)null, page, null);
			}
			if (LocatedWebElement.class.isAssignableFrom(el.getClass())) {
				return (LocatedWebElement) el;
			} else {
				return new LocatedWebElement(el, null, (String)null, page, null);
			}
		} catch (Throwable ex) {
			return new LocatedWebElement(null, null, (String)null, page, null);
		}
	}

	public TestEvaluator getTestEvaluator() {
		return testEvaluator;
	}

	public void setTestEvaluator(TestEvaluator testEvaluator) {
		this.testEvaluator = testEvaluator;
	}

	public Callable<WebElement> getElementRef() {
		return elementRef;
	}

	public P getPage() {
		return page;
	}

	public void withPage(P page) {
		this.page = page;
	}

	public ClickAction<P, N> withAlertWait(int waitSec) {
		this.alertWaitFlag = true;
		this.alertWaitSec = waitSec;
		return this;
	}

	public ClickAction<P, N> withAlertAccept() {
		return withAlertAction(AlertAction.ACCEPT);
	}

	public ClickAction<P, N> withAlertDismiss() {
		return withAlertAction(AlertAction.DISMISS);
	}

	protected ClickAction<P, N> withAlertAction(AlertAction alertAction) {
		this.alertAction = alertAction;
		return this;
	}

	public ClickAction<P, N> withWaitForPageLoad(int waitSec) {
		this.navWait = waitSec;
		return this;
	}

	public ClickAction<P, N> withPreAction(ThrowingConsumer<P, ?> action) {
		this.preAction = action;
		return this;
	}

	public ClickAction<P, N> withPostAction(ThrowingConsumer<P, ?> action) {
		this.postAction = action;
		return this;
	}

	public ClickAction<P, N> withClickActionOverride(ThrowingFunction<ClickContext, N, ?> action) {
		this.doClickOverride = action;
		return this;
	}

	public N getReturnPage() {
		return returnPage;
	}

	public <N extends PageModel<? super N>> ClickAction<P, N> withReturnType(Class<N> returnType) {
		ClickAction<P, N> retClick = (ClickAction<P, N>) this;
		if (!this.page.getClass().equals(returnType)) {
			retClick.navFlag = true;
		}
		retClick.returnPage = determineClickReturn(elementRef, page, returnType);
		return retClick;
	}

	public <N extends PageModel<? super N>> ClickAction<P, N> withReturnPage(N returnPage) {
		ClickAction<P, N> retClick = (ClickAction<P, N>) this;
		if (!this.page.getClass().equals(returnPage.getClass())) {
			retClick.navFlag = true;
		}
		retClick.returnPage = returnPage;
		return retClick;
	}

	public N click(Runnable testClickable) {
		try {
			beforeAction();
		} catch (Throwable t) {
			if(t instanceof PageException){
				throw (PageException)t;
			}
			throw this.page.getContext().createException("Error: Click beforeAction failed", t);
		}
		if (doClickOverride != null) {
			try {
				N ret = doClickOverride.apply(new ClickContext());
				afterAction();
				return ret;
			} catch (Throwable t) {
				if(t instanceof PageException){
					throw (PageException)t;
				}
				throw this.page.getContext().createException("Error: Click failed", t);
			}
		} else {
			try {
				if(navWait != 0){
					getPage().getContext().getDriver().manage().timeouts().pageLoadTimeout(navWait, TimeUnit.SECONDS);
				}
				doClick(testClickable);
				if(navWait != 0){
					getPage().getContext().getDriver().manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
				}
				return afterAction();
			} catch (Throwable t) {
				if(t instanceof PageException){
					throw (PageException)t;
				}
				throw this.page.getContext().createException("Error: Click failed", t);
			}
		}
	}

	protected void doClick(Runnable testClickable) {
		if(testClickable == null) {
			testEvaluator.quiet().testCondition(
					"clickable", op -> op
							.addValue("element", callRef().getElementJson(page)),
					() -> ExpectedConditions.and((ExpectedCondition<Boolean>) driver -> callRef().hasElement(),
							ExpectedConditions.elementToBeClickable(callRef()))
							.apply(page.getContext().getDriver()),
					this, page.getContext());
		}else{
			testClickable.run();
		}
		callRef().click();
	}

	protected void beforeAction() {
		if (preAction != null) {
			ThrowingConsumer.unchecked(preAction).accept(page);
		}
	}

	protected N afterAction() {
		try {
			if (alertWaitFlag) {
				WebDriverWait wait = new WebDriverWait(page.getContext().getDriver(), alertWaitSec);
				wait.until(ExpectedConditions.alertIsPresent());
			}
			if (alertAction == AlertAction.ACCEPT) {
				page.getContext().getDriver().switchTo().alert().accept();
			} else if (alertAction == AlertAction.DISMISS) {
				page.getContext().getDriver().switchTo().alert().dismiss();
			}
		} catch (NoAlertPresentException ex) {
		}
		if (postAction != null) {
			ThrowingConsumer.unchecked(postAction).accept(page);
		}
		return navToPage(returnPage);
	}

	protected <T extends PageModel<? super T>> T navToPage(T navPage) {
		if (navFlag) {
			page.onPageLeave();
		}
		navPage = PageUtils.waitForModelDisplayed(navPage, navWait);
		if (navFlag) {
			navPage.onPageLoad();
		}
		return navPage;
	}

	public class ClickContext {
		public P getPage() {
			return page;
		}

		public void doClick(Runnable testClickable) {
			ClickAction.this.doClick(testClickable);
		}
	}
}
