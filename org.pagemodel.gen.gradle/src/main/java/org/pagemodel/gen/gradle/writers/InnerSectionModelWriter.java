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

public class InnerSectionModelWriter extends SectionModelWriter {


	protected String getComponentClassDef(PageModelConfig pageModel) {
		return String.format("public class %s extends ExtendedSectionModel<%s, %s ,%s> {",
				pageModel.modelName, getSectionTypeParam(pageModel), getSectionPageParam(pageModel), getSectionTypeParam(pageModel));
	}

	protected String getSectionTypeParam(PageModelConfig pageModel) {
		return pageModel.modelName;
	}

	@Override
	protected String getSectionPageParam(PageModelConfig pageModel) {
		switch (pageModel.parentPage.modelType){
			case "PageModel": return pageModel.parentPage.modelName;
			case "AbstractPageModel": return "P";
			case "ComponentModel": return pageModel.parentPage.parentPage == null ? "P" : new InnerComponentModelWriter().getComponentPageParam(pageModel.parentPage);
			case "SectionModel": return pageModel.parentPage.modelName + (pageModel.parentPage.parentPage == null ? "<P>" : "");
		}
		return pageModel.parentPage.modelName;
	}
}
