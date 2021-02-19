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

package org.pagemodel.gen.gradle.writers;

import org.pagemodel.gen.gradle.ElementConfig;
import org.pagemodel.gen.gradle.PageModelConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.pagemodel.gen.gradle.PageModelJavaWriter.INDENT;

public class PageModelWriter {
	public StringBuilder generateClassStart(PageModelConfig pageModel, StringBuilder sb, String indent){
		String classIndent = indent + INDENT;
		String methodIndent = classIndent + INDENT;
		sb.append(System.lineSeparator())
				.append(indent).append("public class ").append(pageModel.modelName)
					.append(" extends ").append(pageModel.modelInherit).append("<")
					.append(pageModel.modelName).append("> {").append(System.lineSeparator())
				.append(classIndent).append("public ").append(pageModel.modelName)
					.append("(ExtendedTestContext testContext) {").append(System.lineSeparator())
				.append(methodIndent).append("super(testContext);").append(System.lineSeparator())
				.append(classIndent).append("}").append(System.lineSeparator());
		return sb;
	}

	protected String getModelDisplayedType(PageModelConfig pageModel){
		return getPageTypeParam(pageModel);
	}

	protected String getModelDisplayedStart(PageModelConfig pageModel){
		return "return super.testModelDisplayed().andThen(page -> page";
	}

	protected String getModelDisplayedEnd(PageModelConfig pageModel){
		return ");";
	}

	protected String getPageTypeParam(PageModelConfig pageModel) {
		return pageModel.modelName;
	}

	protected String getPageNavTypeParam(PageModelConfig pageModel) {
		return getPageTypeParam(pageModel);
	}

	protected String getTesterNavReturnValue(PageModelConfig pageModel){
		return pageModel.modelName + ".class";
	}

	protected String getTesterNavParentPageType(PageModelConfig pageModel, ElementConfig elem){
		return pageModel.modelName;
	}

	protected String getTesterNavParentPageValue(PageModelConfig pageModel, ElementConfig elem){
		return "this";
	}

	protected String getClickActionWrap(PageModelConfig pageModel, String clickAction){
		return clickAction;
	}

	protected String getTesterPageReturnObj(PageModelConfig pageModel){
		return "this";
	}

	public StringBuilder generateClassEnd(PageModelConfig pageModel, StringBuilder sb, String indent){
		return sb.append(indent).append("}").append(System.lineSeparator());
	}

	public StringBuilder generateModelDisplayed(PageModelConfig pageModel, StringBuilder sb, String indent){
		String classIndent = indent + INDENT;
		String methodIndent = classIndent + INDENT;
		String continueIndent = methodIndent + INDENT + INDENT;
		boolean display = false;
		for (ElementConfig elem : pageModel.elements){
			if(elem.displayed){
				display = true;
				break;
			}
		}
		if(!display){
			return sb;
		}
		sb.append(System.lineSeparator())
				.append(classIndent).append("@Override").append(System.lineSeparator())
				.append(classIndent).append("protected Consumer<").append(getModelDisplayedType(pageModel))
					.append("> testModelDisplayed() {").append(System.lineSeparator())
				.append(methodIndent).append(getModelDisplayedStart(pageModel));
		for(ElementConfig elem : pageModel.elements){
			if(elem.displayed){
				sb.append(System.lineSeparator())
						.append(continueIndent).append(".test").append(elem.name).append("()").append(elem.displayTest);
			}
		}
		sb.append(getModelDisplayedEnd(pageModel)).append(System.lineSeparator())
				.append(classIndent).append("}").append(System.lineSeparator());
		return sb;
	}

	public StringBuilder generateElementGetters(PageModelConfig pageModel, StringBuilder sb, String indent){
		String classIndent = indent + INDENT;
		sb.append(System.lineSeparator())
				.append(classIndent).append("// ================ begin protected web elements ==============");
		for(ElementConfig elem : pageModel.elements){
			generateElementGetter(pageModel, elem, sb, indent);
		}
		sb.append(classIndent).append("// ================ end protected web elements ================")
				.append(System.lineSeparator());
		return sb;
	}

	public StringBuilder generateElementGetter(PageModelConfig pageModel, ElementConfig elem, StringBuilder sb, String indent){
		String classIndent = indent + INDENT;
		String methodIndent = classIndent + INDENT;
		String locator = elem.byLocator;
		List<LocatorArg> args = findVars(locator);
		for(LocatorArg arg : args){
			locator = locator.replaceAll(arg.locatorString, "\" + " + arg.name + " + \"");
		}
		String methodArgs = String.join(", ", args.stream().map(arg -> arg.argString).toArray(String[]::new));
		sb.append(System.lineSeparator()).append(classIndent).append("protected WebElement get")
				.append(elem.name).append("(").append(methodArgs).append(") {").append(System.lineSeparator())
				.append(methodIndent).append("return ").append(elem.findMethod).append("(By.").append(elem.byType)
					.append("(\"").append(locator).append("\"));").append(System.lineSeparator())
				.append(classIndent).append("}").append(System.lineSeparator());
		return sb;
	}

	public StringBuilder generateElementTesters(PageModelConfig pageModel, StringBuilder sb, String indent){
		String classIndent = indent + INDENT;
		sb.append(System.lineSeparator())
				.append(classIndent).append("// ================ begin public testers ======================");
		for(ElementConfig elem : pageModel.elements){
			generateElementTester(pageModel, elem, sb, indent);
		}
		sb.append(classIndent).append("// ================ end public testers ========================")
				.append(System.lineSeparator());
		return sb;
	}

	public StringBuilder generateElementTester(PageModelConfig pageModel, ElementConfig elem, StringBuilder sb, String indent){
		String classIndent = indent + INDENT;
		String methodIndent = classIndent + INDENT;
		String continueIndent = methodIndent + INDENT + INDENT;
		String locator = elem.byLocator;
		List<LocatorArg> args = findVars(locator);
		String methodArgs = String.join(", ", args.stream().map(arg -> arg.argString).toArray(String[]::new));
		String callArgs = String.join(", ", args.stream().map(arg -> arg.name).toArray(String[]::new));
		String clickAction = getClickAction(pageModel, elem, callArgs, continueIndent);
		TesterType tester = getTesterType(pageModel, elem);
		sb.append(System.lineSeparator())
				.append(classIndent).append("public ").append(tester.type).append(tester.params).append(" test").append(elem.name)
					.append("(").append(methodArgs).append(") {").append(System.lineSeparator())
				.append(methodIndent).append("return new ").append(tester.type).append(tester.infer)
					.append("(").append(clickAction).append(");").append(System.lineSeparator())
				.append(classIndent).append("}").append(System.lineSeparator());
		return sb;
	}

	protected TesterType getTesterType(PageModelConfig pageModel, ElementConfig elem){
		TesterType tester = new TesterType();
		String returnType = getTesterNavTypeParam(pageModel, elem);
		if(elem.testerType.startsWith("@@@")){
			tester.type = elem.testerType.substring(3);
			tester.params = "<" + getPageTypeParam(pageModel) + ", " + returnType + ">";
		}else if(elem.testerType.startsWith("@@")){
			tester.type = elem.testerType.substring(2);
			tester.params = "<" + getPageTypeParam(pageModel) + ">";
		}else if(elem.testerType.startsWith("@")){
			tester.type = elem.testerType.substring(1);
			tester.params = "";
		}else{
			tester.type = elem.testerType;
			tester.params = "<" + getPageTypeParam(pageModel) + ", " + returnType + ">";
		}
		tester.infer = tester.params.isEmpty() ? "" : "<>";
		return tester;
	}

	protected String getTesterNavTypeParam(PageModelConfig pageModel, ElementConfig elem) {
		if (elem.returnType == null) {
			return getPageNavTypeParam(pageModel);
		}else if(elem.returnType.equals("P") || elem.returnType.startsWith("P:")){
			return getTesterNavParentPageType(pageModel, elem);
		}else if(elem.returnType.contains(":")){
			return elem.returnType.split(":")[0];
		}else{
			return elem.returnType;
		}
	}

	protected String getReturnValue(PageModelConfig pageModel, ElementConfig elem){
		if(elem.returnType == null){
			return getTesterNavReturnValue(pageModel);
		}else if(elem.returnType.equals("P") || elem.returnType.startsWith("P:")){
			return getTesterNavParentPageValue(pageModel, elem);
		}else if(elem.returnType.contains(":")){
			return "test" + elem.returnType.split(":")[1] + "()";
		}else{
			return elem.returnType + ".class";
		}
	}

	protected String getClickAction(PageModelConfig pageModel, ElementConfig elem, String callArgs, String continueIndent){
		String clickAction = null;
		String elemGetter = callArgs == null || callArgs.isEmpty() ?
				"this::get" + elem.name :
				"() -> get" + elem.name + "(" + callArgs + ")";
		if(elem.returnType == null){
			clickAction = "ClickAction.make(" + elemGetter + ", " + getTesterPageReturnObj(pageModel) + ")";
		}else{
			clickAction = "ClickAction.makeNav(" + elemGetter + ", " + getTesterPageReturnObj(pageModel) + ", " + getReturnValue(pageModel, elem) + ")";
		}
		if(elem.clickModifier != null && !elem.clickModifier.isEmpty()){
			clickAction += System.lineSeparator() + continueIndent + elem.clickModifier;
		}
		return getClickActionWrap(pageModel, clickAction);
	}

	protected List<LocatorArg> findVars(String locator){
		List<LocatorArg> args = new ArrayList<>();
		Matcher matcher = Pattern.compile("[si]%[^ %]+%").matcher(locator);
		while(matcher.find()){
			args.add(new LocatorArg(matcher.group()));
		}
		return args;
	}

	class TesterType {
		public String type;
		public String params;
		public String infer;
	}

	class LocatorArg {
		public String locatorString;
		public String argString;
		public String name;

		public LocatorArg(String locatorString) {
			this.locatorString = locatorString;
			this.argString = locatorString.startsWith("i") ? "int" : "String";
			this.name = locatorString.substring(2, locatorString.length() - 1);
			this.argString = argString + " " + this.name;
		}
	}
}
