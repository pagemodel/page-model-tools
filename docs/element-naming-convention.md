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
Image - image element
Tab - tab style button
Control - interactive non-button element(s)
IFrame - iframe element

Component element types:
Row - a table row or row of elements
Dialog - a dialog box with child elements
Modal - a modal dialog box that takes focus from the rest of the page
Nav - a navigation component with navigation buttons or links
Menu - a menu component
Section - generic section
Component - generic component


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
```