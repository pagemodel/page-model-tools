package org.pagemodel.web.testers;

import org.openqa.selenium.WebElement;
import org.pagemodel.core.testers.ComparableTester;
import org.pagemodel.core.testers.StringTester;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.ThrowingFunction;
import org.pagemodel.web.LocatedWebElement;
import org.pagemodel.web.PageModel;

import java.util.concurrent.Callable;

public class JavaScriptReturnTester<P extends PageModel<? super P>, R> {
	protected final R returnObj;
	protected final Callable<Object> ref;
	protected final String javascriptCommand;
	protected final P page;
	private TestEvaluator testEvaluator;

	public JavaScriptReturnTester(Callable<Object> ref, R returnObj, String javascriptCommand, P page, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.javascriptCommand = javascriptCommand;
		this.page = page;
		this.testEvaluator = testEvaluator;
	}

	protected Object callRef() {
		try {
			return ref.call();
		} catch (Exception ex) {
			return null;
		}
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	protected String getRefString(){
		Object ref = callRef();
		return ref == null ? null : ref.toString();
	}

	public R testReturn(ThrowingFunction<Object,Boolean,?> test){
		return getEvaluator().testCondition(
				"javascript return", op -> op.addValue("value", javascriptCommand).addValue("actual",getRefString()).addValue("model",page == null ? null : page.getClass().getSimpleName()),
				() -> ThrowingFunction.unchecked(test).apply(callRef()), returnObj, page.getContext());
	}

	public StringTester<R> testReturnString(){
		return new StringTester<>(() -> (String)callRef(), returnObj, page.getContext(), getEvaluator());
	}

	public ComparableTester<Integer, R> testReturnInteger(){
		return new ComparableTester<>(() -> (Integer)callRef(), returnObj, page.getContext(), getEvaluator());
	}

	public ComparableTester<Long, R> testReturnLong(){
		return new ComparableTester<>(() -> (Long)callRef(), returnObj, page.getContext(), getEvaluator());
	}

	public ComparableTester<Double, R> testReturnDouble(){
		return new ComparableTester<>(() -> (Double)callRef(), returnObj, page.getContext(), getEvaluator());
	}

	public ComparableTester<Boolean, R> testReturnBool(){
		return new ComparableTester<>(() -> (Boolean)callRef(), returnObj, page.getContext(), getEvaluator());
	}

	public WebElementTester<R,P> testReturnElement(){
		return new WebElementTester<>(ClickAction.make(() -> new LocatedWebElement((WebElement)callRef(), "JsElement", "javascript", page, null), page, getEvaluator()), getEvaluator());
	}
}
