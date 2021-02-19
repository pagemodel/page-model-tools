# .pagemodel file syntax

## Syntax Highlighter

#### TextMate Bundle
TextMate bundles can be imported into TextMate, IntelliJ, VS Code, and other editors.
The .pagemodel TextMate Bundle is in [org.pagemodel.syntax/textmate/pagemodel.tmbundle](../org.pagemodel.syntax/textmate/pagemodel.tmbundle)

###### Add to IntelliJ:
1. Ensure `pagemodel.tmbundle` is in a permanent location as IntelliJ will link to it and not copy.
2. `Preferences > Editor > TextMate Bundles`
3. Click the `+` button below the bundle list.
4. Add `pagemodel.tmbundle`
5. Click `OK`
6. Restart IntelliJ

#### Atom Package
The .pagemodel Atom Package is in [org.pagemodel.syntax/atom/language-pagemodel](../org.pagemodel.syntax/atom/language-pagemodel)

###### Add to Atom:
1. Ensure `language-pagemodel` is in a permanent location as Atom will link to it and not copy.
2. Navigate into the `language-pagemodel` directory on the command line
3. Run `apm link`
4. Restart Atom

## File format 
pagemodel files are in the form:
```
page_type package
[import my.pacakage.Class]

element_line

inner_model

custom_java
```

```
page_type = PageModel | ComponentModel | SectionModel | AbstractPageModel | user_defined_page_model
user_defined_type = custom AbstractPageModel type
```

### package:
The full package for the class being generated, and defines the java src path for where the class is generated

### import:
Any line starting with `import ` will be add as an import statement, a `;` will be added if needed.

### element_line:
Each element_line is a single line defining an element on the page or within a component.
Element lines are in the form:
```
element_line = [displayed] element_name [element_tester] find_by [click_nav] [modifiers]

displayed = '*'
element_tester = [@@]@element_tester_type
find_by = [^]by_type by_locator
click_nav = click_nav_type[:nav_element]
modifiers = ['*.display_test'] ['_.click_action']
```

##### displayed:
Element lines starting with `*` are marked as always displayed on the page.  
These elements are added to the testModelDisplayed method and checked to determine if the page is currently displayed.  
An elements marked as displayed must always be displayed in all configurations of the page.  

##### element_name:
Element names should match the visible element label a user would see.  
Element names must end with an element type from the naming convention, this is used to determine the WebElementTester type.
```
Web element types:
Field - text input, text area
Button - button, input button, clickable elements
Link - web links
Display - non-interactive display elements span, div, label, h1, etc...
FileUpload - file upload selection button
Checkbox - checkbox input
DropDown - drop down style select input
Select - non-drop down select (multiple selections not yet supported)
Radio - radio input
Control - interactive non-button element(s)

Component element types:
Row - a table row or row of elements
Dialog - a dialog box with child elements
Modal - a modal dialog box that takes focus from the rest of the page
Nav - a navigation component with navigation buttons or links
Section - generic section
Component - generic component

e.x.:
testUsernameField
testPageHeaderDisplay
testHelpLink
testUserRoleDropDown
testPolicyFileUpload
testAdminRightsCheckbox
UserRow
NotificationDialog
ConfirmationModal
TopNav
```
##### element_tester:
The `element_tester` can define a `ComponentModel`, `SectionModel`, or other custom `WebElementTester` for testing an element.
Use `@TesterClass` for a tester class with no type parameters. (Inner SectionModel)
`@@TesterClass` for a tester class with 1 type parameter. (Inner ComponentModel, External SectionModel)
`@@@TesterClass` for a tester class with 2 type parameters. (External ComponentModel)
When an `element_tester` is not defined then a WebElementTester (or subclass such as CheckboxTester or SelectTester) will be used.
e.g:
```
ConfirmDeleteDialog @ConfirmDeleteSection id "confirmDelete"
UserRow @@UserRow xpath "//div[contains(@class, 'username') and text() = 's%username%']/..//parent::tr"
TopNav @@@MyTopNavClass id "topNav"
```
##### find_by:
The find_by consists of a by_type and by_locator, representing a Selenium By locator.
Available by_types are:
  `id`, `cssSelector`, `xpath`, `className`, `linkText`, `tagName`
The `by_locator` is the locator string for the given `by_type`.
The locator may contain string or integer variables for a user to select from multiple elements (such as a row by row number) or match dynamic content (such as a user element by username).
A `username` string variable would be added as `s%username%`.
A `rowNumber` integer variable would be added as `i%rowNumber%`

By default a ComponentModel or SectionModel will search for descendant elements of the component or section.
For elements that are not direct descendants, add `^` before the by_type to use findPageElement instead of findComponentElement.

`ConfirmSaveButton className "save-button"` : will generate `findComponentElement(By.className("save-button"))`

`ConfirmSaveButton ^className "save-button"` : will generate `findPageElement(By.className("save-button"))`

##### click_nav:
If clicking an element is expected to navigate to another page, the `PageModel` class is defined as the `click_nav_type`:
```
EditUserButton id editUser EditUserPage
```
If clicking an element opens a section, an element with a matching element_tester must be defined along with the SectionModel class as `SectionModel:ElementName`:
```
DeleteUserButton id deleteUser ConfirmDeleteSection:ConfirmDeleteDialog
ConfirmDeleteDialog @ConfirmDeleteSection id "confirmDelete"
```
##### modifiers:
Modifiers can modify how an element is tested in the model's testModelDisplayed method, or modify an element's ClickAction.
If the modifier contains spaces or double-quotes, the modifier must be surrounded by sing-quotes.  It is best to surround all modifier by single-quotes.

`display_modifier = '*.element_test'`

element_test must be valid java code for a test or action on the element's WebElementTester or ComponentModel type.
By default, elements marked as displayed with `*` are tested by calling the `WebElementTester.isDisplayed` method.
This can be modified to perform any test on the element by adding a modifier starting with `*.`. e.x.: 
```
* HeaderDisplay xpath "//h1[1]" '*.text().contains("Add User")'
```
This will call `testHeaderDisplay().text().contains("Add User")` instead of `testHeaderDisplay().isDisplayed()` in the testModelDisplayed method.

```
click_modifier = _.click_action
```

`click_action` must be valid java code that can be called from a `ClickAction`.
A WebElementTester's ClickAction can provide additional actions to perform before and after clicking the element, such as expecting and dismissing an alert.
The provided click_action java code is called on the ClickAction when it is created. e.x:
```
SubmitButton id submit '_.withAlertAccept().withWaitForPageLoad(60)'
```
This will append the modifier to the ClickAction.make call and produce:
```
return new WebElementTester<>(ClickAction.make(this::getDeleteUserButton, this)
                .withAlertAccept().withWaitForPageLoad(60));
```

### inner_model:
Models may define inner `ComponentModels` or `SectionModels` to be generated as inner classes.
Inner models are defined as:
```
@ComponentModel model_name
  element_line
  inner_model
  custom_java
@EndComponent

@SectionModel model_name
  element_line
  inner_model
  custom_java
@EndSection
```

Use `@ComponentModel` or `@SectionModel` followed by the model_name to start an inner model.
model_name is used as the name for the generated inner class.
`element_lines` are defined the same for an inner model
An inner model may contain new inner_models, allowing models to match complex DOM hierarchies

### custom_java:
The onPageLoad and onPageLeave PageModel methods may be overridden using `%onPageLoad` or `%onPageLeave` and `%end`:
```
%onPageLoad
    testApplyButton().waitFor().isClickable();
%end
```
The method body for overriding onPageLoad is provided for a generated @Override method.

Custom java code can be added to the page between `%%start` and `%%end` lines:
```
%%start
  private void doSetup(){
   // java code
  }
%%end
```
Custom java code is added to the java class without modification

---

```
Copyright 2021 Matthew Stevenson <pagemodel.org>
This work is licensed under a Creative Commons Attribution 4.0 International License
http://creativecommons.org/licenses/by/4.0/
@author: Matt Stevenson <matt@pagemodel.org>
```