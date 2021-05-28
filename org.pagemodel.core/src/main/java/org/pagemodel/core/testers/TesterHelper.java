package org.pagemodel.core.testers;

public class TesterHelper {
	public static <R> void setReturn(StringTester<R> tester, R returnObj){
		tester.setReturnObj(returnObj);
	}
	public static <R> R getReturn(StringTester<R> tester){
		return tester.getReturnObj();
	}
	public static void setEvaluator(StringTester<?> tester, TestEvaluator evaluator){
		tester.setEvaluator(evaluator);
	}
	public static TestEvaluator getEvaluator(StringTester<?> tester){
		return tester.getEvaluator();
	}

	public static <R> void setReturn(ComparableTester<?, R> tester, R returnObj){
		tester.setReturnObj(returnObj);
	}
	public static <R> R getReturn(ComparableTester<?, R> tester){
		return tester.getReturnObj();
	}
	public static void setEvaluator(ComparableTester tester, TestEvaluator evaluator){
		tester.setEvaluator(evaluator);
	}
	public static TestEvaluator getEvaluator(ComparableTester tester){
		return tester.getEvaluator();
	}
}
