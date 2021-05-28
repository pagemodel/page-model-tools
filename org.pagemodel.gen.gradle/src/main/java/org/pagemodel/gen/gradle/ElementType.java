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

enum ElementType {
	Field,
	Button,
	Link,
	Display,
	FileUpload,
	Checkbox("CheckboxTester"),
	DropDown("DropDownTester"),
	Select("SelectTester"),
	MultiSelect("MultiSelectTester"),
	Radio,
	Image,
	Tab,
	Control,
	IFrame("IFrameTester"),
	Row,
	Dialog,
	Modal,
	Nav,
	Menu,
	Section,
	Component;

	private String testerType;

	ElementType() {
		this("WebElementTester");
	}

	ElementType(String testerType) {
		this.testerType = testerType;
	}

	public String getTesterType() {
		return testerType;
	}

	public static String getTesterType(String elementName) {
		for (ElementType type : ElementType.values()) {
			if (elementName.endsWith(type.name())) {
				return type.getTesterType();
			}
		}
		return "WebElementTester";
	}
}
