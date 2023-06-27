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
/**
 * The PageModel interface represents a web page model that extends the ModelBase interface.
 * It provides methods for testing the page, handling page actions, and interacting with the web driver.
 * @param <P> the type of the page model
 */
public interface PageModel<P extends PageModel<? super P>> extends ModelBase {

	/**
	 * Returns the WebTestContext associated with this page model.
	 * @return the WebTestContext
	 */
	WebTestContext getContext();

	/**
	 * Returns a PageTester for testing the page.
	 * @return the PageTester
	 */
	default public PageTester<P> testPage() {
		return new PageTester<>((P) this, getContext(), getEvaluator());
	}

	/**
	 * Returns an AlertTester for testing alerts on the page.
	 * @return the AlertTester
	 */
	default public AlertTester<P> testAlert() {
		return new AlertTester<>(this, (P) this, getContext(), getEvaluator());
	}

	/**
	 * Waits for the page to redirect to the specified page class and returns an instance of that class.
	 * @param returnPageClass the class of the page to wait for
	 * @param <R> the type of the page to wait for
	 * @return an instance of the specified page class
	 */
	default public <R extends PageModel<? super R>> R expectRedirect(Class<R> returnPageClass) {
		return PageUtils.waitForNavigateToPage(returnPageClass, getContext());
	}

	/**
	 * Returns the TestEvaluator associated with this page model.
	 * @return the TestEvaluator
	 */
	public TestEvaluator getEvaluator();

	/**
	 * Returns true if the page model is currently displayed.
	 * @return true if the page model is displayed, false otherwise
	 */
	public boolean modelDisplayed();

	/**
	 * Called when the user navigates away from the page.
	 */
	default public void onPageLeave() {
	}

	/**
	 * Called when the user navigates to the page.
	 */
	default public void onPageLoad() {
	}

	/**
	 * Closes the web driver associated with this page model.
	 */
	default public void closeBrowser() {
		getContext().quit();
	}

	/**
	 * Performs the specified action and returns the page model.
	 * @param action the action to perform
	 * @return the page model
	 * @throws TestRuntimeException if the action fails
	 */
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

	/**
	 * Performs the specified action and returns the result.
	 * @param action the action to perform
	 * @param <R> the type of the result
	 * @return the result of the action
	 * @throws TestRuntimeException if the action fails
	 */
	default public <R> R doAction(Callable<R> action) {
		try {
			return action.call();
		} catch (TestRuntimeException t) {
			throw t;
		} catch (Throwable t) {
			throw getContext().createException("Error: Page action failed.", t);
		}
	}

	/**
	 * Performs the specified action and returns the result.
	 * @param action the action to perform
	 * @param <R> the type of the result
	 * @return the result of the action
	 * @throws TestRuntimeException if the action fails
	 */
	default public <R> R doAction(ThrowingFunction<P, R, ?> action) {

		try {
			return action.apply((P) this);
		} catch (TestRuntimeException t) {
			throw t;
		} catch (Throwable t) {
			throw getContext().createException("Error: Page action failed.", t);
		}
	}

	/**
	 * Performs the specified action.
	 * @param action the action to perform
	 * @return the page model
	 * @throws TestRuntimeException if the action fails
	 */
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

	/**
	 * The DefaultPageModel class provides a default implementation of the PageModel interface.
	 * @param <T> the type of the page model
	 */
	public static abstract class DefaultPageModel<T extends PageModel<? super T>> implements PageModel<T> {
		protected final WebTestContext testContext;
		private TestEvaluator testEvaluator;

		/**
		 * Constructs a new DefaultPageModel with the specified WebTestContext.
		 * @param testContext the WebTestContext
		 */
		public DefaultPageModel(WebTestContext testContext) {
			this.testContext = testContext;
			this.testEvaluator = new TestEvaluator.Now();
		}

		/**
		 * Sets the TestEvaluator associated with this page model.
		 * @param testEvaluator the TestEvaluator
		 */
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

		/**
		 * Logs whether the page model is currently displayed.
		 */
		protected void logModelDisplayed() {
			testModelDisplayed().accept((T) this);
		}

		/**
		 * Returns a Consumer that tests whether the page model is displayed.
		 * @return the Consumer
		 */
		protected Consumer<T> testModelDisplayed(){
			return page -> {};
		}

		/**
		 * Finds a web element on the page with the specified name and By selector.
		 * @param name the name of the element
		 * @param by the By selector
		 * @return the LocatedWebElement
		 */
		protected LocatedWebElement findPageElement(String name, By by) {
			try {
				WebElement el = getContext().getDriver().findElement(by);
				return new LocatedWebElement(el, name, by, this, null);
			} catch (Exception e) {
				return new LocatedWebElement(null, name, by, this, null);
			}
		}

		/**
		 * Finds all web elements on the page with the specified name and By selector.
		 * @param name the name of the elements
		 * @param by the By selector
		 * @param <T> the type of the LocatedWebElement
		 * @return a List of LocatedWebElements
		 */
		protected <T extends LocatedWebElement> List<? super T> findPageElements(String name, By by) {
			try {
				return Arrays.asList(getContext().getDriver().findElements(by).stream().map(el -> new LocatedWebElement(el, name, by, this, null)).toArray(WebElement[]::new));
			} catch (Exception e) {
				return Arrays.asList(new LocatedWebElement(null, name, by, this, null));
			}
		}
	}
}