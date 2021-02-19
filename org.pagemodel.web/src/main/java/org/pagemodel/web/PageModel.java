package org.pagemodel.web;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.pagemodel.web.WebTestContext;
import org.pagemodel.web.testers.AlertTester;
import org.pagemodel.web.testers.PageTester;
import org.pagemodel.core.testers.TestEvaluator;
import org.openqa.selenium.*;
import org.pagemodel.core.utils.ThrowingConsumer;
import org.pagemodel.core.utils.ThrowingFunction;
import org.pagemodel.core.utils.ThrowingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <P> PageModel implementation type
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public interface PageModel<P extends PageModel<? super P>> {

	WebTestContext getContext();

	default public PageTester<P> testPage() {
		return new PageTester<>((P) this, getContext());
	}

	default public AlertTester<P> testAlert() {
		return new AlertTester<>((P) this, getContext());
	}

	default public <R extends PageModel<? super R>> R expectRedirect(Class<R> returnPageClass) {
		R returnPage = PageUtils.makeInstance(returnPageClass, getContext());
		return PageUtils.waitForModelDisplayed(returnPage);
	}

	public TestEvaluator getEvaluator();

	public boolean modelDisplayed();

	default public void onPageLeave() {
	}

	default public void onPageLoad() {
	}

	default public void closeBrowser() {
		getContext().quit();
	}

	default public P doAction(ThrowingRunnable<?> action) {
		try {
			action.run();
			return (P) this;
		} catch (Throwable t) {
			throw getContext().createException("Error: Page action failed.", t);
		}
	}

	default public <R> R doAction(Callable<R> action) {
		try {
			return action.call();
		} catch (Throwable t) {
			throw getContext().createException("Error: Page action failed.", t);
		}
	}

	default public <R> R doAction(ThrowingFunction<P, R, ?> action) {

		try {
			return action.apply((P) this);
		} catch (Throwable t) {
			throw getContext().createException("Error: Page action failed.", t);
		}
	}

	default public P doAction(ThrowingConsumer<P, ?> action) {
		try {
			P t = (P) this;
			action.accept(t);
			return t;
		} catch (Throwable t) {
			throw getContext().createException("Error: Page action failed.", t);
		}
	}

	public static abstract class DefaultPageModel<T extends PageModel<? super T>> implements PageModel<T> {
		private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

		protected final WebTestContext testContext;
		private TestEvaluator testEvaluator;

		public DefaultPageModel(WebTestContext testContext) {
			this.testContext = testContext;
			this.testEvaluator = new TestEvaluator.Now();
		}

		protected void setTestEvaluator(TestEvaluator testEvaluator){
			this.testEvaluator = testEvaluator;
		}

		@Override
		public TestEvaluator getEvaluator() {
			return testEvaluator;
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
				testModelDisplayed().accept((T) this);
				return eval.getTestStatus();
			}finally{
				getContext().setEvaluator(contextEvaluator);
				setTestEvaluator(eval.getInnerEvaluator());
			}
		}

		protected void logModelDisplayed() {
			testModelDisplayed().accept((T) this);
		}

		protected Consumer<T> testModelDisplayed(){
			return page -> {};
		}

		protected WebElement findPageElement(By by) {
			try {
				WebElement el = getContext().getDriver().findElement(by);
				return new LocatedWebElement(el, by, null);
			} catch (Exception e) {
				return new LocatedWebElement(null, by, null);
			}
		}

		protected List<WebElement> findPageElements(By by) {
			try {
				return Arrays.asList(getContext().getDriver().findElements(by).stream().map(el -> new LocatedWebElement(el, by, null)).toArray(WebElement[]::new));
			} catch (Exception e) {
				return Arrays.asList(new LocatedWebElement(null, by, null));
			}
		}
	}
}
