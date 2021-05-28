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
import org.pagemodel.core.utils.json.JsonBuilder;
import org.pagemodel.core.utils.json.JsonObjectBuilder;

import java.util.List;
import java.util.Map;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class LocatedWebElement implements WebElement {
	protected WebElement element;
	protected WebElement parent;
	protected String locator;
	protected String friendlyName;
	protected ModelBase model;

	public LocatedWebElement(WebElement element, String friendlyName, By by, ModelBase model, WebElement parent) {
		this(element, friendlyName, by.toString(), model, parent);
	}

	public LocatedWebElement(WebElement element, String friendlyName, String locator, ModelBase model, WebElement parent) {
		this.element = element;
		this.friendlyName = friendlyName;
		this.locator = locator;
		this.model = model;
		this.parent = parent;
	}

	public WebElement getElement() {
		if(element != null && LocatedWebElement.class.isAssignableFrom(element.getClass())){
			return ((LocatedWebElement)element).getElement();
		}
		return element;
	}

	public String getFriendlyName(){
		return friendlyName;
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

	public boolean hasFriendlyName() {
		return friendlyName != null;
	}

	public Map<String,Object> getElementJson(ModelBase model) {
		return getElementJson(this, "element", model);
	}

	public static LocatedWebElement wrap(WebElement el, ModelBase model) {
		if (el == null) {
			return new LocatedWebElement(null, null, (String)null, model, null);
		}
		if (LocatedWebElement.class.isAssignableFrom(el.getClass())) {
			return (LocatedWebElement) el;
		} else {
			return new LocatedWebElement(el, null, (String)null, model, null);
		}
	}

	private static Map<String,Object> getElementJson(WebElement el, String label, ModelBase model) {
		LocatedWebElement lwe = wrap(el, model);
		return JsonBuilder.object()
				.addValue("name", lwe.friendlyName)
				.addValue("model", lwe.model == null ? null : lwe.model.getClass().getSimpleName())
				.addValue("by", lwe.getElementLocator())
				.doAdd(ob -> {
					if(!lwe.hasElement()){
						ob.addValue("found", "null");
					} else {
						ob.addObject("found", o -> {
							String tag = lwe.getTagName();
							String tagLower = tag == null ? null : tag.toLowerCase();
							String text = lwe.getText();
							if(tag != null && !tag.equals(tagLower)){
								tag = tagLower;
								text = text == null ? null : text.trim().replaceAll("\\s+", " ");
							}
							addNonEmptyField(o, "tag", tag);
							addNonEmptyField(o, "id", lwe.getAttribute("id"));
							addNonEmptyField(o, "name", lwe.getAttribute("name"));
							if(!label.equals("parent")) {
								addNonEmptyField(o, "value", lwe.getAttribute("value"));
								addNonEmptyField(o, "class", lwe.getAttribute("class"));
								addNonEmptyField(o, "href", lwe.getAttribute("href"));
								addNonEmptyField(o, "text", text);
							}
						});
					}
					if(lwe.hasParent()) {
						ob.addValue("parent", getElementJson(lwe.getLocatorParent(), "parent", model));
					}
				}).toMap();
	}

	private static void addNonEmptyField(JsonObjectBuilder obj, String field, String value){
		if(value == null || value.isEmpty()){
			return;
		}
		obj.addValue(field, value);
	}
}
