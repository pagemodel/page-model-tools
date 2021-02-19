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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import static org.pagemodel.gen.gradle.PageModelJavaWriter.INDENT;

public class PageModelReader {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public PageModelConfig readPageModel(String filepath) throws IOException {
		BufferedReader reader;
		PageModelConfig rootPageModel = new PageModelConfig();
		rootPageModel.parentPage = null;
		PageModelConfig pageModel = rootPageModel;
		boolean topLineParsed = false;
		boolean inJavaBlock = false;
		pageModel.modelName = new File(filepath).getName().replaceAll("\\.pagemodel", "");

		reader = new BufferedReader(new FileReader(filepath));
		String line = reader.readLine();
		while (line != null) {
			if(!topLineParsed){
				if(!line.isEmpty()) {
					pageModel = parseTopLine(line, pageModel);
					topLineParsed = true;
				}
			}else if(inJavaBlock){
				if(parseJavaEnd(line, pageModel)){
					inJavaBlock = false;
				}else{
					List<String> block = pageModel.customJava.get(pageModel.customJava.size()-1);
					if(block.size() > 0 && block.get(0).equals("\t@Override")){
						line = INDENT + line;
					}
					block.add(line);
				}
			}else if(parseJavaStart(line, pageModel)){
				inJavaBlock = true;
			}else{
				pageModel = parseModelLine(line, pageModel);
			}
			line = reader.readLine();
		}
		reader.close();
		return rootPageModel;
	}

	private PageModelConfig parseTopLine(String line, PageModelConfig pageModel){
		List<String> parts = parseArgString(line);
		String modelInherit = parts.get(0);
		String modelType = modelInherit;
		if(!PageModelConfig.MODEL_TYPES.contains(modelType)){
			modelType = PageModelConfig.PAGE_MODEL;
		}
		if(modelInherit.startsWith("@")){
			modelInherit = modelInherit.substring(1);
			modelType = PageModelConfig.ABSTRACT_PAGE_MODEL;
		}
		if(modelInherit.equals(PageModelConfig.PAGE_MODEL) || modelInherit.equals(PageModelConfig.ABSTRACT_PAGE_MODEL)){
			modelInherit = "ExtendedPageModel";
		}else if(modelInherit.equals(PageModelConfig.SECTION_MODEL)){
			modelInherit = "ExtendedSectionModel";
		}
		pageModel.modelInherit = modelInherit;
		pageModel.modelType = modelType;
		pageModel.modelPackage = parts.get(1);
		return pageModel;
	}

	private boolean parseJavaStart(String line, PageModelConfig pageModel){
		if(!line.trim().startsWith("%")){
			return false;
		}
		if(line.trim().equals("%%start")){
			pageModel.customJava.add(new ArrayList<>());
			return true;
		}
		List<String> javaBlock = new ArrayList<>();
		javaBlock.add(INDENT + "@Override");
		javaBlock.add(INDENT + "public void " + line.trim().substring(1) + "() {");
		pageModel.customJava.add(javaBlock);
		return true;
	}

	private boolean parseJavaEnd(String line, PageModelConfig pageModel){
		if(line.trim().equals("%%end")){
			return true;
		}else if(line.trim().equals("%end")){
			pageModel.customJava.get(pageModel.customJava.size()-1)
					.add(INDENT + "}" + System.lineSeparator());
			return true;
		}
		return false;
	}

	private PageModelConfig parseModelLine(String line, PageModelConfig pageModel){
		List<String> parts = parseArgString(line);
		if(parts.isEmpty() || parts.get(0).startsWith("#")){
			return pageModel;
		}
		String first = parts.get(0);
		if(first.equals("import")){
			String imprt = line;
			if(!imprt.endsWith(";")){
				imprt = imprt + ";";
			}
			pageModel.imports.add(imprt);
			return pageModel;
		}
		if(first.equals("@ComponentModel")){
			PageModelConfig inner = new PageModelConfig();
			inner.parentPage = pageModel;
			inner.modelPackage = pageModel.modelPackage;
			inner.modelType = PageModelConfig.COMPONENT_MODEL;
			inner.modelInherit = PageModelConfig.COMPONENT_MODEL;
			inner.modelName = parts.get(1);
			pageModel.innerModels.add(inner);
			return inner;
		}
		if(first.equals("@SectionModel")){
			PageModelConfig inner = new PageModelConfig();
			inner.parentPage = pageModel;
			inner.modelPackage = pageModel.modelPackage;
			inner.modelType = PageModelConfig.SECTION_MODEL;
			inner.modelInherit = PageModelConfig.SECTION_MODEL;
			inner.modelName = parts.get(1);
			pageModel.innerModels.add(inner);
			return inner;
		}
		if(first.equals("@EndComponent") || first.equals("@EndSection")){
			return pageModel.parentPage;
		}
		return parseElementLine(parts, pageModel);
	}

	private PageModelConfig parseElementLine(List<String> parts, PageModelConfig pageModel){
		ElementConfig elem = new ElementConfig();
		int i = 0;
		if(parts.get(i).equals("*")) {
			elem.displayed = true;
			elem.displayTest = ".isDisplayed()";
			i++;
		}
		elem.name = parts.get(i++);
		if(parts.get(i).startsWith("@")){
			elem.testerType = parts.get(i++);
		}else{
			elem.testerType = ElementType.getTesterType(elem.name);
		}
		elem.byType = parts.get(i++);
		if(pageModel.modelType.equals("ComponentModel") || pageModel.modelType.equals("SectionModel")){
			elem.findMethod = "findComponentElement";
			if (elem.byType.startsWith("^")) {
				elem.byType = elem.byType.substring(1);
				elem.findMethod = "findPageElement";
			}
		}else {
			elem.findMethod = "findPageElement";
		}
		elem.byLocator = parts.get(i++);
		while(i < parts.size()){
			if(parts.get(i).startsWith("*")){
				elem.displayTest = parts.get(i++).substring(1);
			}else if(parts.get(i).startsWith("_")){
				elem.clickModifier = parts.get(i++).substring(1);
			}else {
				if (elem.returnType != null) {
					log.warn("Warning: Unknown element parameter [" + parts.get(i++) + "] in line [" + String.join(" ", parts) + "]");
					continue;
				}
				elem.returnType = parts.get(i++);
			}
		}
		if(elem.returnType != null){
			elem.clickNav = true;
		}else{
			elem.clickNav = false;
		}
		pageModel.elements.add(elem);
		return pageModel;
	}

	private List<String> parseArgString(String line){
		List<String> parts = new ArrayList<>();
		line = line.trim();
		StringBuilder part = new StringBuilder();
		boolean inQuote = false;
		boolean partialQutoe = false;
		Character quoteStart = null;
		for(Character c : line.toCharArray()){
			if(inQuote){
				if(c == quoteStart){
					inQuote = false;
					quoteStart = null;
					if(partialQutoe){
						part.append(c);
						partialQutoe = false;
					}else {
						parts.add(part.toString());
						part = new StringBuilder();
					}
				}else{
					part.append(c);
				}
			}else if(c.equals('"') || c.equals('\'')){
				quoteStart = c;
				inQuote = true;
				if(part.toString().isEmpty()){
					partialQutoe = false;
				}else {
					part.append(c);
					partialQutoe = true;
				}
			}else if(c.equals(' ') || c.equals('\t')){
				if(!part.toString().trim().isEmpty()){
					parts.add(part.toString());
					part = new StringBuilder();
				}
			}else{
				part.append(c);
			}
		}
		if(!part.toString().isEmpty()){
			parts.add(part.toString());
		}
		return parts;
	}
}