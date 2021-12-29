package org.pagemodel.web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.TestRuntimeException;
import org.pagemodel.core.utils.ThrowingConsumer;
import org.pagemodel.core.utils.ThrowingFunction;
import org.pagemodel.core.utils.ThrowingRunnable;
import org.pagemodel.web.testers.AlertTester;
import org.pagemodel.web.testers.PageTester;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * @param <P> PageModel implementation type
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public interface PageModel<P extends PageModel<? super P>> extends ModelBase {

	WebTestContext getContext();

	default public PageTester<P> testPage() {
		return new PageTester<>((P) this, getContext(), getEvaluator());
	}

	default public AlertTester<P> testAlert() {
		return new AlertTester<>(this, (P) this, getContext(), getEvaluator());
	}

	default public <R extends PageModel<? super R>> R expectRedirect(Class<R> returnPageClass) {
		return PageUtils.waitForNavigateToPage(returnPageClass, getContext());
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
		} catch (TestRuntimeException t) {
			throw t;
		} catch (Throwable t) {
			throw getContext().createException("Error: Page action failed.", t);
		}
	}

	default public <R> R doAction(Callable<R> action) {
		try {
			return action.call();
		} catch (TestRuntimeException t) {
			throw t;
		} catch (Throwable t) {
			throw getContext().createException("Error: Page action failed.", t);
		}
	}

	default public <R> R doAction(ThrowingFunction<P, R, ?> action) {

		try {
			return action.apply((P) this);
		} catch (TestRuntimeException t) {
			throw t;
		} catch (Throwable t) {
			throw getContext().createException("Error: Page action failed.", t);
		}
	}

	default public P doAction(ThrowingConsumer<P, ?> action) {
		try {
			P t = (P) this;
			action.accept(t);
			return t;
		} catch (TestRuntimeException t) {
			throw t;
		} catch (Throwable t) {
			throw getContext().createException("Error: Page action failed.", t);
		}
	}

	public static abstract class DefaultPageModel<T extends PageModel<? super T>> implements PageModel<T> {
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

		protected LocatedWebElement findPageElement(String name, By by) {
			try {
				WebElement el = getContext().getDriver().findElement(by);
				return new LocatedWebElement(el, name, by, this, null);
			} catch (Exception e) {
				return new LocatedWebElement(null, name, by, this, null);
			}
		}

		protected <T extends LocatedWebElement> List<? super T> findPageElements(String name, By by) {
			try {
				return Arrays.asList(getContext().getDriver().findElements(by).stream().map(el -> new LocatedWebElement(el, name, by, this, null)).toArray(WebElement[]::new));
			} catch (Exception e) {
				return Arrays.asList(new LocatedWebElement(null, name, by, this, null));
			}
		}
	}
}
