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

package org.pagemodel.web;

import org.openqa.selenium.*;

import java.util.List;
import java.util.function.Function;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class LocatedWebElement implements WebElement {
	protected WebElement element;
	protected WebElement parent;
	protected String locator;

	public LocatedWebElement(WebElement element, By by, WebElement parent) {
		this.element = element;
		this.locator = by.toString();
		this.parent = parent;
	}

	public LocatedWebElement(WebElement element, String locator, WebElement parent) {
		this.element = element;
		this.locator = locator;
		this.parent = parent;
	}

	public WebElement getElement() {
		if(element != null && LocatedWebElement.class.isAssignableFrom(element.getClass())){
			return ((LocatedWebElement)element).getElement();
		}
		return element;
	}

	@Override
	public void click() {
		getElement().click();
	}

	@Override
	public void submit() {
		getElement().submit();
	}

	@Override
	public void sendKeys(CharSequence... keysToSend) {
		getElement().sendKeys(keysToSend);
	}

	@Override
	public void clear() {
		getElement().clear();
	}

	@Override
	public String getTagName() {
		return getElement().getTagName();
	}

	@Override
	public String getAttribute(String name) {
		return getElement().getAttribute(name);
	}

	@Override
	public boolean isSelected() {
		return getElement().isSelected();
	}

	@Override
	public boolean isEnabled() {
		return getElement().isEnabled();
	}

	@Override
	public String getText() {
		return getElement().getText();
	}

	@Override
	public List<WebElement> findElements(By by) {
		return getElement().findElements(by);
	}

	@Override
	public WebElement findElement(By by) {
		return getElement().findElement(by);
	}

	@Override
	public boolean isDisplayed() {
		return getElement().isDisplayed();
	}

	@Override
	public Point getLocation() {
		return getElement().getLocation();
	}

	@Override
	public Dimension getSize() {
		return getElement().getSize();
	}

	@Override
	public Rectangle getRect() {
		return getElement().getRect();
	}

	@Override
	public String getCssValue(String propertyName) {
		return getElement().getCssValue(propertyName);
	}

	@Override
	public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
		return getElement().getScreenshotAs(target);
	}

	public String getElementLocator() {
		return locator;
	}

	public WebElement getLocatorParent() {
		return parent;
	}

	public boolean hasElement() {
		return element != null;
	}

	public boolean hasParent() {
		return parent != null;
	}

	public boolean hasLocator() {
		return locator != null;
	}

	public String getElementDisplay() {
		return getElementDisplay(this, "element");
	}

	public static LocatedWebElement wrap(WebElement el) {
		if (el == null) {
			return new LocatedWebElement(null, (String)null, null);
		}
		if (LocatedWebElement.class.isAssignableFrom(el.getClass())) {
			return (LocatedWebElement) el;
		} else {
			return new LocatedWebElement(el, (String)null, null);
		}
	}

	private static String getElementDisplay(WebElement el, String label) {
		LocatedWebElement lwe = wrap(el);
		StringBuilder disp = new StringBuilder();
		disp.append(label + "(");
		if (lwe.hasLocator()) {
			disp.append("by(" + lwe.getElementLocator().toString() + "), ");
		} else {
			disp.append("by(null), ");
		}
		if (lwe.hasElement()) {
			if (label.equals("parent")) {
				disp.append("foundParent(");
			} else {
				disp.append("found(");
			}
			boolean separator = false;
			separator = addElementDisplay("tag", lwe.getElement().getTagName(), disp, separator, str -> str.toLowerCase());
			separator = addElementDisplay("id", lwe.getElement().getAttribute("id"), disp, separator);
			separator = addElementDisplay("name", lwe.getElement().getAttribute("name"), disp, separator);
			if (!label.equals("parent")) {
				separator = addElementDisplay("value", lwe.getElement().getAttribute("value"), disp, separator);
				separator = addElementDisplay("class", lwe.getElement().getAttribute("class"), disp, separator);
				separator = addElementDisplay("href", lwe.getElement().getAttribute("href"), disp, separator);
				addElementDisplay("text", lwe.getElement().getText(), disp, separator, str -> str.trim().replaceAll("\\s+", " "));
			}
			disp.append(")");
		} else {
			disp.append("found(null)");
		}
		if (lwe.hasParent()) {
			disp.append(", ")
					.append(getElementDisplay(lwe.getLocatorParent(), "parent"));
		}
		return disp.append(")").toString();
	}

	private static boolean addElementDisplay(String label, String value, StringBuilder disp, boolean seperator) {
		return addElementDisplay(label, value, disp, seperator, null);
	}

	private static boolean addElementDisplay(String label, String value, StringBuilder disp, boolean seperator, Function<String,String> transform) {
		if(value != null && transform != null){
			value = transform.apply(value);
		}
		if(value != null && !value.isEmpty()) {
			if(seperator){
				disp.append(", ");
			}
			disp.append(label + ":[" + value + "]");
			return true;
		}
		return seperator;
	}
}
