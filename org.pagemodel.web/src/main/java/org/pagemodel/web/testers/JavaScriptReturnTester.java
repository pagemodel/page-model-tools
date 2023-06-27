package org.pagemodel.web.testers;

import org.openqa.selenium.WebElement;
import org.pagemodel.core.testers.ComparableTester;
import org.pagemodel.core.testers.StringTester;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.ThrowingFunction;
import org.pagemodel.web.LocatedWebElement;
import org.pagemodel.web.PageModel;

import java.util.concurrent.Callable;

/**
 * The JavaScriptReturnTester class is used to test the return value of a JavaScript command executed on a web page.
 * @param <P> The type of the PageModel being tested.
 * @param <R> The type of the expected return value.
 */
public class JavaScriptReturnTester<P extends PageModel<? super P>, R> {

	/**
	 * The expected return value.
	 */
	protected final R returnObj;

	/**
	 * The Callable object that executes the JavaScript command.
	 */
	protected final Callable<Object> ref;

	/**
	 * The JavaScript command being tested.
	 */
	protected final String javascriptCommand;

	/**
	 * The PageModel being tested.
	 */
	protected final P page;

	/**
	 * The TestEvaluator used to evaluate the test results.
	 */
	private TestEvaluator testEvaluator;

	/**
	 * Constructs a new JavaScriptReturnTester object.
	 * @param ref The Callable object that executes the JavaScript command.
	 * @param returnObj The expected return value.
	 * @param javascriptCommand The JavaScript command being tested.
	 * @param page The PageModel being tested.
	 * @param testEvaluator The TestEvaluator used to evaluate the test results.
	 */
	public JavaScriptReturnTester(Callable<Object> ref, R returnObj, String javascriptCommand, P page, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.javascriptCommand = javascriptCommand;
		this.page = page;
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Calls the Callable object that executes the JavaScript command.
	 * @return The result of the JavaScript command.
	 */
	protected Object callRef() {
		try {
			return ref.call();
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Gets the TestEvaluator used to evaluate the test results.
	 * @return The TestEvaluator used to evaluate the test results.
	 */
	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	/**
	 * Gets the result of the Callable object as a String.
	 * @return The result of the Callable object as a String.
	 */
	protected String getRefString(){
		Object ref = callRef();
		return ref == null ? null : ref.toString();
	}

	/**
	 * Tests the return value of the JavaScript command using the provided ThrowingFunction.
	 * @param test The ThrowingFunction used to test the return value.
	 * @return The expected return value.
	 */
	public R testReturn(ThrowingFunction<Object,Boolean,?> test){
		return getEvaluator().testCondition(
				"javascript return", op -> op.addValue("value", javascriptCommand).addValue("actual",getRefString()).addValue("model",page == null ? null : page.getClass().getSimpleName()),
				() -> ThrowingFunction.unchecked(test).apply(callRef()), returnObj, page.getContext());
	}

	/**
	 * Tests the return value of the JavaScript command as a String.
	 * @return A StringTester object used to test the return value.
	 */
	public StringTester<R> testReturnString(){
		return new StringTester<>(() -> (String)callRef(), returnObj, page.getContext(), getEvaluator());
	}

	/**
	 * Tests the return value of the JavaScript command as an Integer.
	 * @return A ComparableTester object used to test the return value.
	 */
	public ComparableTester<Integer, R> testReturnInteger(){
		return new ComparableTester<>(() -> (Integer)callRef(), returnObj, page.getContext(), getEvaluator());
	}

	/**
	 * Tests the return value of the JavaScript command as a Long.
	 * @return A ComparableTester object used to test the return value.
	 */
	public ComparableTester<Long, R> testReturnLong(){
		return new ComparableTester<>(() -> (Long)callRef(), returnObj, page.getContext(), getEvaluator());
	}

	/**
	 * Tests the return value of the JavaScript command as a Double.
	 * @return A ComparableTester object used to test the return value.
	 */
	public ComparableTester<Double, R> testReturnDouble(){
		return new ComparableTester<>(() -> (Double)callRef(), returnObj, page.getContext(), getEvaluator());
	}

	/**
	 * Tests the return value of the JavaScript command as a Boolean.
	 * @return A ComparableTester object used to test the return value.
	 */
	public ComparableTester<Boolean, R> testReturnBool(){
		return new ComparableTester<>(() -> (Boolean)callRef(), returnObj, page.getContext(), getEvaluator());
	}

	/**
	 * Tests the return value of the JavaScript command as a WebElement.
	 * @return A WebElementTester object used to test the return value.
	 */
	public WebElementTester<R,P> testReturnElement(){
		return new WebElementTester<>(ClickAction.make(() -> new LocatedWebElement((WebElement)callRef(), "JsElement", "javascript", page, null), page, getEvaluator()), getEvaluator());
	}
}