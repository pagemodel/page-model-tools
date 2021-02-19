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

import org.pagemodel.gen.gradle.PageModelConfig;

public class InnerComponentModelWriter extends ComponentModelWriter {

	@Override
	protected String getComponentClassDef(PageModelConfig pageModel) {
		return String.format("public class %s<R> extends ComponentModel<R,%s,%s<R>> {",
				pageModel.modelName, getComponentPageParam(pageModel), pageModel.modelName);
	}

	@Override
	protected String getComponentPageParam(PageModelConfig pageModel) {
		switch (pageModel.parentPage.modelType){
			case "PageModel": return pageModel.parentPage.modelName;
			case "AbstractPageModel": return "P";
			case "ComponentModel": return pageModel.parentPage.parentPage == null ? "P" : getComponentPageParam(pageModel.parentPage);
			case "SectionModel": return pageModel.parentPage.parentPage == null ? pageModel.parentPage.modelName + "<P>" : pageModel.parentPage.modelName;
		}
		return "P";
	}

	@Override
	protected String getComponentSectionTypeParams(PageModelConfig pageModel) {
		return pageModel.modelName + "_section";
	}

	@Override
	protected String getPageTypeParam(PageModelConfig pageModel) {
		return "R";
	}

	@Override
	protected String getPageNavTypeParam(PageModelConfig pageModel) {
		return getComponentPageParam(pageModel);
	}

	@Override
	protected String getTesterNavReturnValue(PageModelConfig pageModel) {
		return "(" + getComponentPageParam(pageModel) + ")page";
	}

	@Override
	protected String getTesterPageReturnObj(PageModelConfig pageModel) {
		return "(" + getComponentPageParam(pageModel) + ")page";
	}
}
