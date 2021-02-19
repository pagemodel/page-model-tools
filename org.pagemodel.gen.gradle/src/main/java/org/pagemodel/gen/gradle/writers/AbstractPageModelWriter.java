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

public class AbstractPageModelWriter extends PageModelWriter {

	public StringBuilder generateClassStart(PageModelConfig pageModel, StringBuilder sb, String indent){
		String classIndent = indent + INDENT;
		String methodIndent = classIndent + INDENT;
		sb.append(System.lineSeparator())
				.append(indent).append(String.format("public abstract class %s<P extends %s<? super P>> extends %s<P> {",
						pageModel.modelName, pageModel.modelName, pageModel.modelInherit)).append(System.lineSeparator())
				.append(classIndent).append("public ").append(pageModel.modelName)
				.append("(ExtendedTestContext testContext) {").append(System.lineSeparator())
				.append(methodIndent).append("super(testContext);").append(System.lineSeparator())
				.append(classIndent).append("}").append(System.lineSeparator());
		return sb;
	}

	@Override
	protected String getPageTypeParam(PageModelConfig pageModel) {
		return "P";
	}

	@Override
	protected String getTesterNavReturnValue(PageModelConfig pageModel) {
		return "(P)this";
	}

	@Override
	protected String getTesterPageReturnObj(PageModelConfig pageModel) {
		return "(P)this";
	}

	@Override
	protected String getTesterNavParentPageType(PageModelConfig pageModel, ElementConfig elem){
		return "P";
	}

	@Override
	protected String getTesterNavParentPageValue(PageModelConfig pageModel, ElementConfig elem){
		return "(P)this";
	}
}
