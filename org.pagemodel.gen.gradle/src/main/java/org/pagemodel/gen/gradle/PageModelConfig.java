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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class PageModelConfig {
	public static final String PAGE_MODEL = "PageModel";
	public static final String ABSTRACT_PAGE_MODEL = "AbstractPageModel";
	public static final String COMPONENT_MODEL = "ComponentModel";
	public static final String SECTION_MODEL = "SectionModel";
	public static final List<String> MODEL_TYPES = Collections.unmodifiableList(Arrays.asList(
			PAGE_MODEL, ABSTRACT_PAGE_MODEL, COMPONENT_MODEL, SECTION_MODEL));

	public String modelName;
	public String modelInherit;
	public String modelType;
	public String modelPackage;
	public List<String> imports = new ArrayList<>();
	public List<ElementConfig> elements = new ArrayList<>();
	public List<List<String>> customJava = new ArrayList<>();
	public List<PageModelConfig> innerModels = new ArrayList<>();
	public PageModelConfig parentPage;
}
