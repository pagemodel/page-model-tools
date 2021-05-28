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

import static org.pagemodel.gen.gradle.PageModelJavaWriter.INDENT;

public class ComponentModelWriter extends PageModelWriter {

	public StringBuilder generateClassStart(PageModelConfig pageModel, StringBuilder sb, String indent){
		String classIndent = indent + INDENT;
		String methodIndent = classIndent + INDENT;
		String innerIndent = methodIndent + INDENT;
		sb.append(System.lineSeparator())
				.append(indent).append(getComponentClassDef(pageModel)).append(System.lineSeparator())
				.append(classIndent).append("public ").append(pageModel.modelName)
				.append("(ClickAction<?, ").append(getComponentPageParam(pageModel)).append("> clickAction, TestEvaluator testEvaluator) {").append(System.lineSeparator())
				.append(methodIndent).append("super(clickAction, testEvaluator);").append(System.lineSeparator())
				.append(classIndent).append("}").append(System.lineSeparator()).append(System.lineSeparator())

				.append(classIndent).append("public ").append(pageModel.modelName)
				.append("(R returnObj, ClickAction<?, ").append(getComponentPageParam(pageModel)).append("> clickAction, TestEvaluator testEvaluator) {").append(System.lineSeparator())
				.append(methodIndent).append("super(returnObj, clickAction, testEvaluator);").append(System.lineSeparator())
				.append(classIndent).append("}").append(System.lineSeparator()).append(System.lineSeparator())

				.append(classIndent).append("public class ").append(pageModel.modelName).append("_section extends ")
				.append(pageModel.modelName).append("<").append(getComponentSectionTypeParams(pageModel)).append("> {").append(System.lineSeparator())
				.append(methodIndent).append("public ").append(pageModel.modelName).append("_section(ClickAction<?, ")
				.append(getComponentPageParam(pageModel)).append("> clickAction, TestEvaluator testEvaluator) {").append(System.lineSeparator())
				.append(innerIndent).append("super(clickAction, testEvaluator);").append(System.lineSeparator())
				.append(innerIndent).append("setReturnObj(this);").append(System.lineSeparator())
				.append(methodIndent).append("}").append(System.lineSeparator()).append(System.lineSeparator())

				.append(methodIndent).append("public ").append(getComponentPageParam(pageModel)).append(" testSectionParent() {").append(System.lineSeparator())
				.append(innerIndent).append("return ").append(getTesterPageReturnObj(pageModel)).append(";").append(System.lineSeparator())
				.append(methodIndent).append("}").append(System.lineSeparator())
				.append(classIndent).append("}").append(System.lineSeparator()).append(System.lineSeparator())

				.append(classIndent).append("public ").append(pageModel.modelName).append("_section asSection(){").append(System.lineSeparator())
				.append(methodIndent).append("return new ").append(pageModel.modelName).append("_section(clickAction, getEvaluator());").append(System.lineSeparator())
				.append(classIndent).append("}").append(System.lineSeparator());

		return sb;
	}

	protected String getComponentClassDef(PageModelConfig pageModel) {
		return String.format("public class %s<R, P extends ExtendedModelBase<? super P>> extends ComponentModel<R,P,%s<R,P>> {",
				pageModel.modelName, pageModel.modelName);
	}

	protected String getComponentPageParam(PageModelConfig pageModel) {
		return "P";
	}

	protected String getComponentSectionTypeParams(PageModelConfig pageModel) {
		return pageModel.modelName + "_section, P";
	}

	@Override
	protected String getModelDisplayedType(PageModelConfig pageModel) {
		return pageModel.modelName + "_section";
	}

	@Override
	protected String getModelDisplayedStart(PageModelConfig pageModel) {
		return "return page -> page";
	}

	@Override
	protected String getModelDisplayedEnd(PageModelConfig pageModel) {
		return ";";
	}

	@Override
	protected String getPageTypeParam(PageModelConfig pageModel) {
		return "R";
	}

	@Override
	protected String getPageNavTypeParam(PageModelConfig pageModel) {
		return "P";
	}

	@Override
	protected String getTesterNavReturnValue(PageModelConfig pageModel) {
		return "(P)page";
	}

	@Override
	protected String getTesterPageReturnObj(PageModelConfig pageModel) {
		return "(P)page";
	}

	protected String getClickActionWrap(PageModelConfig pageModel, String clickAction){
		return "getReturnObj(), " + clickAction + ", getEvaluator()";
	}

	@Override
	protected String getTesterNavParentPageType(PageModelConfig pageModel, ElementConfig elem){
		return "P";
	}

	@Override
	protected String getTesterNavParentPageValue(PageModelConfig pageModel, ElementConfig elem){
		return "(P)page";
	}
}
