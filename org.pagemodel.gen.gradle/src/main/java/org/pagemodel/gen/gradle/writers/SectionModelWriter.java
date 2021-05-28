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

public class SectionModelWriter extends PageModelWriter {

	public StringBuilder generateClassStart(PageModelConfig pageModel, StringBuilder sb, String indent){
		String classIndent = indent + INDENT;
		String methodIndent = classIndent + INDENT;
		sb.append(System.lineSeparator())
				.append(indent).append(getComponentClassDef(pageModel)).append(System.lineSeparator())
				.append(classIndent).append("public ").append(pageModel.modelName).append("(ClickAction<")
				.append(getSectionPageParam(pageModel)).append(", ").append(getSectionPageParam(pageModel)).append("> clickAction, TestEvaluator testEvaluator) {").append(System.lineSeparator())
				.append(methodIndent).append("super(clickAction, testEvaluator);").append(System.lineSeparator())
				.append(classIndent).append("}").append(System.lineSeparator());

		return sb;
	}

	protected String getComponentClassDef(PageModelConfig pageModel) {
		return String.format("public class %s<P extends ExtendedModelBase<? super P>> extends ExtendedSectionModel<%s,P,%s> {",
				pageModel.modelName, getSectionTypeParam(pageModel), getSectionTypeParam(pageModel));
	}

	protected String getSectionTypeParam(PageModelConfig pageModel) {
		return pageModel.modelName + "<P>";
	}

	protected String getSectionPageParam(PageModelConfig pageModel) {
		return "P";
	}

	@Override
	protected String getModelDisplayedType(PageModelConfig pageModel) {
		return getSectionTypeParam(pageModel);
	}

	@Override
	protected String getPageTypeParam(PageModelConfig pageModel) {
		return getSectionTypeParam(pageModel);
	}

	@Override
	protected String getTesterNavReturnValue(PageModelConfig pageModel) {
		return "this";
	}

	@Override
	protected String getTesterPageReturnObj(PageModelConfig pageModel) {
		return "this";
	}

	@Override
	protected String getTesterNavParentPageType(PageModelConfig pageModel, ElementConfig elem){
		return getSectionPageParam(pageModel);
	}

	@Override
	protected String getTesterNavParentPageValue(PageModelConfig pageModel, ElementConfig elem){
		return "parentPage";
	}
}
