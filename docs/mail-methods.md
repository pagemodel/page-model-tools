1. [WebElementTester](#webelementtester)
    1. [WebElementWait](#webelementwait)
    2. [WebElementRefresh](#webelementrefresh)
2. [StringTester](#stringtester)
3. [ComparableTester](#comparabletesterc)
4. [PageModel](#pagemodel)
5. [PageTester](#pagetester)
6. [AlertTester](#alerttester)
7. [JavaScriptReturnTester](#javascriptreturntester)
8. [WebActionTester](#webactiontester)
9. [TestContext](#testcontext)
10. [RectangleTester](#rectangletester)
11. [PointTester](#pointtester)
12. [DimensionTester](#dimensiontester)

## MailTester

| Test method | parameters | return type | description |
|-------------|:---------:|:-----------:|:------------|
| **tagName** | | [StringTester](#stringtester) | test element tag name |
| **text** | | [StringTester](#stringtester) | test element text |
| **location** | | [RectangleTester](#rectangletester) | test element location coordinates |
| **size** | | [DimensionTester](#dimensiontester) | test element width and height |
| **attribute** | String name | [StringTester](#stringtester) | test element attribute named `name` |
| **cssValue** | String name | [StringTester](#stringtester) | test element cssValue named `name` |
||
| **click** | | [PageModel](#pagemodel) | click element and perform click actions and navigation define in pagemodel |
| **clickAnd** | | [WebActionTester](#webactiontester) | click element and return [WebActionTester](#webactiontester).  Do not perform pagemodel click actions and navigation. |
| **clearText** | | [PageModel](#pagemodel) | clear the text for input elements |
| **sendKeys** | String keys | [PageModel](#pagemodel) | send the key sequence `keys` to the element.  Set text for text inputs, selects option of select element, set the file location for fileUpload element. |
| **sendKeysAnd** | String keys | [WebActionTester](#webactiontester) | send the key sequence `keys` to the element and return [WebActionTester](#webactiontester). |
||
| **exists** | | [PageModel](#pagemodel) | assert element exists in DOM |
| **notExists** | | [PageModel](#pagemodel) | assert element does not exist in DOM |
| **isDisplayed** | | [PageModel](#pagemodel) | assert element is visible on page |
| **notDisplayed** | | [PageModel](#pagemodel) | assert element is not visible on page, or element does not exist in DOM |
| **isEnabled** | | [PageModel](#pagemodel) | assert element is enabled |
| **notEnabled** | | [PageModel](#pagemodel) | assert element is disabled |
| **isClickable** | | [PageModel](#pagemodel) | assert element is clickable (displayed and enabled) |
| **notClickable** | | [PageModel](#pagemodel) | assert element is not clickable |
| **isFocused** | | [PageModel](#pagemodel) | assert element has page focus | 
| **notFocused** | | [PageModel](#pagemodel) | assert element does not have page focus |
| **isSelected** | | [PageModel](#pagemodel) | assert element is selected |
| **notSelected** | | [PageModel](#pagemodel) | assert element is not selected |
||
| **waitFor** | | [WebElementWait](#webelementwait) | perform test waiting until condition is true or timeout |
| **waitAndRefreshFor** | | [WebElementRefresh](#webelementrefresh) | perform test repeatedly waiting and refreshing page until condition is true or timeout |

### WebElementWait
WebElementWait provides all the tests for [WebElementTester](#webelementtester)

| Test method | parameters | return type | description |
|-------------|:---------:|:-----------:|:------------|
| **withTimeout** | int seconds | [WebElementWait](#webelementwait) | set the timeout for wait before failure |

### WebElementRefresh
WebElementRefresh provides all the tests for [WebElementTester](#webelementtester)

| Test method | parameters | return type | description |
|-------------|:---------:|:-----------:|:------------|
| **withTimeout** | int seconds | [WebElementRefresh](#webelementrefresh)  | set the timeout for wait and refresh before failure |
| **withPageSetup** | λ:`PageModel -> {}` | [WebElementRefresh](#webelementrefresh) | pege actions to perform before initial test and after each refresh |

## StringTester
| Test method | parameters | return type | description |
|-------------|:---------:|:-----------:|:------------|
| **isEmpty** | | [PageModel](#pagemodel) | assert string is null or empty |
| **notEmpty** | | [PageModel](#pagemodel) | assert string is not empty |
| **equals** | String value | [PageModel](#pagemodel) | assert string equals `value` |
| **notEquals** | String value | [PageModel](#pagemodel) | assert string not equals `value` |
| **contains** | String value | [PageModel](#pagemodel) | assert string contains `value` |
| **notContains** | String value | [PageModel](#pagemodel) | assert string not contains `value` |
| **containedBy** | String value | [PageModel](#pagemodel) | assert `value` contains string |
| **notContainedBy** | String value | [PageModel](#pagemodel) | assert `value` not contains  |
| **endsWith** | String value | [PageModel](#pagemodel) | assert string ends with given `value` |
| **notEndsWith** | String value | [PageModel](#pagemodel) | assert string not ends with `value` |
| **startsWith** | String value | [PageModel](#pagemodel) | assert string starts with `value` |
| **notStartsWith** | String value | [PageModel](#pagemodel) | assert string not starts with `value` |
| **matches** | String regex | [PageModel](#pagemodel) | assert string matches `regex` |
| **notMatches** | String regex | [PageModel](#pagemodel) | assert string not matches `regex` |
| **length** | | [ComparableTester](#comparabletesterc) | test the string length |
||
| **storeValue** | String key | [PageModel](#pagemodel) | store string to [TestContext](#testcontext) with the given `key` |
||
| **asInteger** | | [ComparableTester](#comparabletesterc) | test string parsed as Integer |
| **asDate** | | [ComparableTester](#comparabletesterc) | test string parse as Date |
| **testMatch** | String regex | [StringTester](#stringtester) | test first group matched by given `regex` |
| **testMatch** | String regex, int group | [StringTester](#stringtester) | test group number `group` matched by given `regex` |
| **transform** | λ:`String -> String` | [StringTester](#stringtester) | transform string and test |
| **transformCompare** | λ:`String -> Comparable` | [ComparableTester](#comparabletesterc) | transform string to a Comparable type and test |

## ComparableTester\<C>
C is a Comparable type: [Integer, Double, Long, Date]

| Test method | parameters | return type | description |
|-------------|:---------:|:-----------:|:------------|
| **equals** | C value | [PageModel](#pagemodel) | assert comparable equals `value`, == |
| **notEquals** | C value | [PageModel](#pagemodel) | assert comparable not equals `value`, != |
| **greaterThan** | C value | [PageModel](#pagemodel) | assert comparable greater than `value`, > |
| **notGreaterThan** | C value | [PageModel](#pagemodel) | assert comparable not greater than `value`, <= |
| **lessThan** | C value | [PageModel](#pagemodel) | assert comparable less than `value`, < |
| **notLessThan** | C value | [PageModel](#pagemodel) | assert comparable not less than `value`, >= |
||
| **storeValue** | C value | [PageModel](#pagemodel) | store comparable to [TestContext](#testcontext) with the given `key` |
| **storeValue** | C value, C default | [PageModel](#pagemodel) | store comparable to [TestContext](#testcontext) with the given `key`, store `defaultVal` if comparable is null |
||
| **transform** | λ:`C -> Comparable` | [ComparableTester](#comparabletesterc) | transform comparable to same or different Comparable type and test |
| **asString** | | [StringTester](#stringtester) | test comparable as string using `toString()`

## PageModel
| Test method | parameters | return type | description |
|-------------|:---------:|:-----------:|:------------|
| **testAlert** |  | [AlertTester](#alerttester) | test page alert |
| **testPage** |  | [PageTester](#pagetester) | common page tests and actions |
| **expectRedirect** | [PageModel](#pagemodel) page | [PageModel](#pagemodel) | expect browser to be redirect to `page` |
| **closeBrowser** |  | void | close TextContext browser and end page test chain |
||
| **testMail** |  | MailTester | test email |
| **testSSH** |  | SSHTester | test shh |
| **testSSH** | SSHAuthenticator sshAuth | SSHTester | test ssh connecting with `sshAuth` |
||
| **doAction** | λ:`PageModel -> R` | R | perform custom action |
| **doAction** | λ:`PageModel -> {}` | [PageModel](#pagemodel) | perform custom action |
| **doAction** | λ:`() -> R` | R | perform custom action |
| **doAction** | λ:`() -> {}` | [PageModel](#pagemodel) | perform custom action |

## PageTester
| Test method | parameters | return type | description |
|-------------|:---------:|:-----------:|:------------|
| **pageSource** |  | [StringTester](#stringtester) | test page source |
| **title** |  | [StringTester](#stringtester) | test page title |
| **url** |  | [StringTester](#stringtester) | test page url |
| **testBodyElement** |  | [WebElementTester](#webelementtester) | test the `<body>` element of the page |
| **testFocusedElement** |  | [WebElementTester](#webelementtester) | test the current focused element on the page |
| **testHTMLElement** |  | WebElementTest | test the `<html>` element of the page |
||
| **navigateTo** | String url, Class\<[PageModel](#pagemodel)> model | [PageModel](#pagemodel) | navigate browser to `url` and return [PageModel](#pagemodel) for `model` |
| **refreshPage** |  | [PageModel](#pagemodel) | refresh and wait for page to load |
| **windowSize** |  | [DimensionTester](#dimensiontester) | test window width and height |
| **fullscreenWindow** |  | [PageModel](#pagemodel) | set browser fullscreen |
| **maximizeWindow** |  | [PageModel](#pagemodel) | maximize browser window |
| **setWindowPosition** | int x, int y | [PageModel](#pagemodel) | set window position to `x`, `y` |
| **moveWindowPositionByOffset** | int offsetX, int offsetY | [PageModel](#pagemodel) | move browser window to offset from current location |
| **setWindowSize** | int width, int height | [PageModel](#pagemodel) | set window size to `width`x`height` |
| **switchToDefaultContent** | Class\<[PageModel](#pagemodel)> model | [PageModel](#pagemodel) | switch from the current frame to page default content, expected to be [PageModel](#pagemodel) type `model` |
| **switchToParentFrame** | Class\<[PageModel](#pagemodel)> model | [PageModel](#pagemodel) | switch from the current frame to the frame's parent, expected to be [PageModel](#pagemodel) type `model`.  If the WebDriver is set to `frameA` within `frameB`, `testParentFrame` will switch to `frameB` |
| **testPageModel** | [PageModel](#pagemodel) model | [PageModel](#pagemodel) | use [PageModel](#pagemodel) `model` for testing current page |
| **takeScreenshot** | String filePrefix | [PageModel](#pagemodel) | take screenshot and save png to ./build/screenshots/`XXX`_`filePrefix`_`yyyy-MM-dd_HH.mm.ss`.png, where XXX is the screenshot number within the current test suite run. |
||
| **testJavaScript** | String javascript | [PageModel](#pagemodel) | execute the given `javascript` on the current page |
| **testJavaScriptAsync** | String javascript | [PageModel](#pagemodel) | execute the given `javascript` using the selenium javascript async |
| **testJavaScriptAsyncWithReturn** | String javascript | [JavaScriptReturnTester](#javascriptreturntester) | execute the given `javascript` using the selenium javascript async and test the returned object |
| **testJavaScriptWithReturn** | String javascript | [JavaScriptReturnTester](#javascriptreturntester) | execute the given `javascript` on the current page and test the returned object |
||
| **testAccessibility** |  | [PageModel](#pagemodel) | run AXE accesibility scanner and assert no WCAG violations |
| **testAccessibility** | String...expectedViolations | [PageModel](#pagemodel) | run AXE accesibility scanner and assert no WCAG errors other then the provided `expectedViolations` |
||
| **waitFor** |  | [PageTester](#pagetester) | perform page tests with wait |
| **waitAndRefreshFor** |  | [PageTester](#pagetester) | perform page tests with wait and refresh |

## AlertTester
| Test method | parameters | return type | description |
|-------------|:---------:|:-----------:|:------------|
| **accept** |  | [PageModel](#pagemodel) | assert alert exists and accept |
| **dismiss** |  | [PageModel](#pagemodel) | assert alert exists and dismiss |
| **exists** |  | [PageModel](#pagemodel) | assert alert exists |
| **notExists** |  | [PageModel](#pagemodel) | assert alert does not exist |
| **sendKeys** | String keys | [PageModel](#pagemodel) | type `keys` into alert |
| **text** |  | [StringTester](#stringtester) | test alert text |
||
| **waitFor** |  | [AlertTester](#alerttester) | test alert with wait |
| **waitAndRefreshFor** |  | [AlertTester](#alerttester) | test alert with wait and refresh |

## JavaScriptReturnTester
| Test method | parameters | return type | description |
|-------------|:---------:|:-----------:|:------------|
| **testReturn** | λ:`Object -> Boolean` | [PageModel](#pagemodel) | perform test on return object |
| **testReturnString** |  | [StringTester](#stringtester) | test return object as String |
| **testReturnInteger** |  |  [ComparableTester](#comparabletesterc) | test return object as Integer |
| **testReturnDouble** |  | [ComparableTester](#comparabletesterc) | test return object as Double |
| **testReturnLong** |  | [ComparableTester](#comparabletesterc) | test return object as Long |
| **testReturnElement** |  | [WebElementTester](#webelementtester) | test return object as WebElement |

## WebActionTester
| Test method | parameters | return type | description |
|-------------|:---------:|:-----------:|:------------|
| **expectRedirect** | Class\<[PageModel](#pagemodel)> model | [PageModel](#pagemodel) | expect redirected to `model` |
| **noRedirect** |  | [PageModel](#pagemodel) | assert still on current page |
| **sendKeys** | String keys | [PageModel](#pagemodel) | type `keys` to current element |
| **testAlert** |  | [AlertTester](#alerttester) | test alert on page |

## TestContext
| Test method | parameters | return type | description |
|-------------|:---------:|:-----------:|:------------|
| **store** | String key, Object value | void | store `value` with `key` |
| **removeStored** | String key | void | remove value stored with `key` |
| **load** | String key | T | load value store with key and infer type |
| **load** | String key, Class\<T> type | T | load value stored with key as `type` |
| **loadDate** | String key | Date | load Date stored with `key` |
| **loadInteger** | String key | Integer | load Integer stored with `key` |
| **loadString** | String key | String | load String stored with `key` |
| **loadMail** | String key | Mail | load Mail stored with `key` |
| **getDriver** |  | WebDriver | get WebDriver currently in use by [TestContext](#testcontext) |

## RectangleTester
| Test method | parameters | return type | description |
|-------------|:---------:|:-----------:|:------------|
| **topLeft** |  | [PointTester](#pointtester) | test top left point `(x1, y1)` |
| **topRight** |  | [PointTester](#pointtester) | test top right point `(x2, y1)` |
| **bottomLeft** |  | [PointTester](#pointtester) | test bottom left point `(x1, y2)` |
| **bottomRight** |  | [PointTester](#pointtester) | test bottom right point `(x2, y2)` |
| **height** |  | [ComparableTester](#comparabletesterc) | test height `(y2 - y1)` |
| **width** |  | [ComparableTester](#comparabletesterc) | test width `(x2 - x1)` |
| **x1** |  | [ComparableTester](#comparabletesterc) | test `x1` value |
| **x2** |  | [ComparableTester](#comparabletesterc) | test `x2` value |
| **y1** |  | [ComparableTester](#comparabletesterc) | test `y1` value |
| **y2** |  | [ComparableTester](#comparabletesterc) | test `y2` value |

## PointTester
| Test method | parameters | return type | description |
|-------------|:---------:|:-----------:|:------------|
| **x** |  | [ComparableTester](#comparabletesterc) | test `x` value |
| **y** |  | [ComparableTester](#comparabletesterc) | test `y` value |

## DimensionTester
| Test method | parameters | return type | description |
|-------------|:---------:|:-----------:|:------------|
| **height** |  | [ComparableTester](#comparabletesterc) | test height |
| **width** |  | [ComparableTester](#comparabletesterc) | test width |