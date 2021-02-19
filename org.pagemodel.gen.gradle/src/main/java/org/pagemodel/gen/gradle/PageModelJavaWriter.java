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

package org.pagemodel.gen.gradle;

import org.pagemodel.gen.gradle.writers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class PageModelJavaWriter {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public final static String INDENT = "\t";

	public String generatePageModelJavaFile(PageModelConfig pageModel) {
		StringBuilder sb = new StringBuilder();
		generateFileHeader(pageModel, sb);
		generatePageModelClass(pageModel, sb, "");
		return sb.toString();
	}

	private StringBuilder generateFileHeader(PageModelConfig pageModel, StringBuilder sb){
		sb.append("package ").append(pageModel.modelPackage).append(";")
				.append(System.lineSeparator()).append(System.lineSeparator());
		sb.append("import org.pagemodel.web.*;").append(System.lineSeparator())
				.append("import org.pagemodel.web.testers.*;").append(System.lineSeparator())
				.append("import org.pagemodel.core.testers.*;").append(System.lineSeparator())
				.append("import org.pagemodel.tools.*;").append(System.lineSeparator())
				.append("import java.util.function.Consumer;").append(System.lineSeparator())
				.append("import org.openqa.selenium.By;").append(System.lineSeparator())
				.append("import org.openqa.selenium.WebElement;").append(System.lineSeparator());
		for(String imprt : pageModel.imports){
			sb.append(imprt).append(System.lineSeparator());
		}
		return sb;
	}

	private StringBuilder generatePageModelClass(PageModelConfig pageModel, StringBuilder sb, String indent){
		PageModelWriter writer = getWriterType(pageModel);
		writer.generateClassStart(pageModel, sb, indent);
		writer.generateModelDisplayed(pageModel, sb, indent);
		writer.generateElementGetters(pageModel, sb, indent);
		writer.generateElementTesters(pageModel, sb, indent);
		generateInnerComponents(pageModel, sb, indent);
		generateCustomJava(pageModel, sb, indent);
		writer.generateClassEnd(pageModel, sb, indent);
		return sb;
	}

	private StringBuilder generateCustomJava(PageModelConfig pageModel, StringBuilder sb, String indent){
		for(List<String> block : pageModel.customJava){
			sb.append(System.lineSeparator());
			for(String line : block){
				sb.append(System.lineSeparator()).append(indent).append(line);
			}
		}
		return sb;
	}

	private StringBuilder generateInnerComponents(PageModelConfig pageModel, StringBuilder sb, String indent){
		for(PageModelConfig inner : pageModel.innerModels){
			sb.append(System.lineSeparator());
			generatePageModelClass(inner, sb, indent + INDENT);
		}
		return sb;
	}

	private static PageModelWriter getWriterType(PageModelConfig pageModel){
		switch (pageModel.modelType){
			case "PageModel" : return new PageModelWriter();
			case "AbstractPageModel" : return new AbstractPageModelWriter();
			case "ComponentModel" : return (pageModel.parentPage == null) ? new ComponentModelWriter() : new InnerComponentModelWriter();
			case "SectionModel" : return (pageModel.parentPage == null) ? new SectionModelWriter() : new InnerSectionModelWriter();
			default: return new PageModelWriter();
		}
	}
}
