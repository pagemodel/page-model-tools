PageModelTools classes wrap and expose core selenium functionality:
```
TestContext
  WebDriver.new
  WebDriver.get
  WebDriver.quit
  
PageModel
  WebDriver.findElement
  
ComponentModel
  WebElement.findElement
  
WebElementTester
  WebElement.clear
  WebElement.isDisplayed
  WebElement.isEnabled
  WebElement == null
  WebElement.isSelected
  WebElement.sendKeys
  WebElement.click
  WebElement.getAttribute
  WebElement.getCssValue
  WebElement.getRect
  WebElement.getSize
  WebElement.getTagName
  WebElement.getText
  WebDriverWait
  
PageTester (PageModel.testPage)
  WebDriver.getTitle
  WebDriver.getCurrentUrl
  WebDriver.getPageSource
  WebDriver.getScreenshotAs
  WebDriver.manage.window.getPosition
  WebDriver.manage.window.getSize
  WebDriver.manage.window.setPosition
  WebDriver.manage.window.setSize
  WebDriver.manage.window.maximize
  WebDriver.manage.window.fullscreen
  WebDriver.navigate.refresh
  WebDriver.navigate.to
  WebDriver.switchTo.activeElement
  
AlertTester (PageModel.testAlert)
  WebDriver.switchTo.alert
  Alert.dismiss
  Alert.accept
  Alert.getText
  Alert.sendKeys

RectangleTester
  Rectangle.getX
  Rectangle.getY
  Rectangle.getWidth
  Rectangle.getHeight

DimensionTester
  Dimension.getWidth
  Dimension.getHeight

PointTester
  Point.getX
  Point.getY
```
  
PageModelTools testers wrap standard Java classes:
```
StringTester
  String.contains
  String.equals
  String.isEmpty
  String.matches
  String.startsWith
  String.endsWith
  String.length

ComparableTester (Integer, Date, Double, Float, Long)
  Comparable.compareTo (==, !=, >, <, >=, <=)
```

Copyright 2021 Matthew Stevenson <pagemodel.org>