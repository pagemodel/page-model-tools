# Element Naming Convention:
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


e.g.
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
---
```
Copyright 2021 Matthew Stevenson <pagemodel.org>
This work is licensed under a Creative Commons Attribution 4.0 International License
http://creativecommons.org/licenses/by/4.0/
@author: Matt Stevenson <matt@pagemodel.org>
```