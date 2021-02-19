# Page Model Tools

Page Model Tools is a set of tools for developing page object models for fluent testing.

## Fluent tests:
Tests are written as fluent method chains:
```java
MyAppUser.admin(context).loginToMainPage() // HomePage
        .testSiteStatusDisplay().text().equals("Online")
        .testSiteVersionDisplay().text().startsWith("0.7")
        .testStatusDateDisplay().text().storeValue("status_date")
        .testPage().waitFor().numberOfSeconds(1)
        .testUpdateStatusButton().click()
        .testStatusDateDisplay().text().notEquals(context.load("status_date"))

        .testNotificationDisplay(1).testTitleDisplay().text().equals("Notification 1")
        .testNotificationDisplay(1).testDismissLink().click()
        .testNotificationDisplay(1).testTitleDisplay().waitFor().text().equals("Notification 2")

        .testItemReviewRow("Item 1").testUserDisplay().text().startsWith("bob")
        .testItemReviewRow("Item 1").testDeleteLink().click()
        .testItemReviewRow("Item 1").notExists()

        .testItemReviewRow("Item 2").asSection() // HomePage.ItemReviewRow
            .testStatusDropDown().attribute("value").equals("Pending")
            .testUserDisplay().text().contains("sam")
            .testRoutingDisplay().text().endsWith("-a")
            .testStatusDropDown().selectValue("Approved")
            .testSectionParent() // HomePage

        .testStatusDateDisplay().text().asDate().storeValue("status_date2")
        .testStatusDateDisplay().waitAndRefreshFor().text().asDate().greaterThan(context.load("status_date2"))

        .testTopNav().testManageUsersLink().click() // ManageUsersPage
        .testAddUserButton().click() // ManageUsersPage.AddUserSection
        .testUsernameField().sendKeys("newUser")
        .testEmailField().sendKeys(Unique.string("%s@example.com"))
        .testPasswordField().sendKeys("password")
        .testAdminCheckbox().setChecked()
        .testCancelButton().click() // ManageUsersPage

        .testTopNav().testSignOutLink().click() // LoginPage
        .closeBrowser();
```
_*return type annotations in comments are provided automatically by IntelliJ_

## Simple page models:
Page Models are defined in simple .pagemodel files declaring page elements and reusable components.  Syntax highlighers for the .pagemodel file format are included.

Java page model classes are generated from .pagemodel files automatically with a gradle plugin.

[.pagemodel readme](docs/page-model-gen-readme.md)

###### HomePage.pagemodel:
```java
MyAppInternalPage com.example.xyz.test.pages

* HeaderDisplay xpath "//h1[1]" *.text().equals("MyApp Home")
* SiteStatusDisplay id "siteStatus"
* SiteVersionDisplay id "siteVersion"
* StatusDateDisplay id "statusDate"
* UpdateStatusButton id "updateStatus"

NotificationDisplay @@NotificationDialog xpath "//div[@id='notifications']/div[i%notificationNum%]"
NotificationDisplay @@NotificationDialog xpath "//div[@id='notifications']/div/div[1][text()='s%title%']//parent::div"

ItemReviewRow @@ItemReviewRow xpath "//table[@id='itemReviewTable']/tbody/tr/td[1][text()='s%itemName%']//parent::tr"

@ComponentModel NotificationDialog
  TitleDisplay cssSelector "div:nth-child(1)"
  MessageDisplay cssSelector "div:nth-child(2)"
  DismissLink cssSelector "a.notif-dismiss"
@EndComponent

@ComponentModel ItemReviewRow
  NameDisplay cssSelector "td:nth-child(1)"
  StatusDropDown cssSelector "td:nth-child(2) > select"
  RoutingDisplay cssSelector "td:nth-child(3)"
  UserDisplay cssSelector "td:nth-child(4)"
  SaveLink cssSelector "td:nth-child(5) > a"
  DeleteLink cssSelector "td:nth-child(6) > a"
@EndComponent
```

## Getting Started:

Until org.pagemodel is published to a maven repository, it must be built from source and published to your local maven repository.
#### Build page-model-tools from source:

1. Clone the repository https://github.com/pagemodel/page-model-tools
2. build org.pagemodel.gen.gradle and publish to maven local
3. build and test org.pagemodel projects and publish to maven local
###### Linux and MacOS:
```
git clone https://github.com/pagemodel/page-model-tools.git
cd page-model-tools/org.pagemodel.gen.gradle
../gradlew --rerun-tasks clean build publishToMavenLocal --console=plain
cd ..
./gradlew --rerun-tasks clean build publishToMavenLocal --parallel --console=plain
cd ..
```
###### Windows:
```
git clone https://github.com/pagemodel/page-model-tools.git
cd page-model-tools\org.pagemodel.gen.gradle
..\gradlew.bat --rerun-tasks clean build publishToMavenLocal --console=plain
cd ..
gradlew.bat --rerun-tasks clean build publishToMavenLocal --parallel --console=plain
cd ..
```

#### Create a new testing project :

1. after building the sources, get the built `org.pagemodel.gen.project-0.7.0-SNAPSHOT.jar`
2. run the jar to generate a new project
3. run the sample tests in your project
 
###### Linux and MacOS:
```
cp page-model-tools/org.pagemodel.gen.project/build/libs/org.pagemodel.gen.project-0.7.0-SNAPSHOT.jar .
java -jar org.pagemodel.gen.project-0.7.0-SNAPSHOT.jar XYZ com.example.xyz.test ./XYZTests/
cd XYZTests
./gradlew --rerun-tasks test --console=plain
```

###### Windows:
```
cp page-model-tools\org.pagemodel.gen.project\build\libs\org.pagemodel.gen.project-0.7.0-SNAPSHOT.jar .
java -jar org.pagemodel.gen.project-0.7.0-SNAPSHOT.jar XYZ com.example.xyz.test XYZTests
cd XYZTests
gradlew.bat --rerun-tasks test --console=plain
```

#### Docker setup:
Build a docker image that supports headless chrome with prepopulated gradle dependencies for pipelne testing.

If not using docker, skip ahead to `Configure project for your web application`

Until the pagemodel-headless-chrom docker image is published to a docker registry, it must be built locally.
1. build `pagemodel-headless-chrome` base docker image
2. build `xyz-headless-chrome` docker image
3. run tests in docker
###### Linux and MacOS:
```
cd page-model-tools/
./scripts/docker/build-docker.sh
cd ../XYZTests/
./scripts/docker/build-docker.sh
./scripts/dtest ./gradlew --rerun-tasks test --console=plain -Dbrowser=headless
```
###### Windows:
```
cd page-model-tools
docker build -f "scripts\docker\pagemodel-headless-chrome.dockerfile" -t pagemodel-headless-chrome:0.7.0 .
cd ..\XYZTests
docker build -f "scripts\docker\xyz-headless-chrome.dockerfile" -t xyz-headless-chrome:1.0.0 .
docker run --rm -ti -u seluser:seluser -v %PWD%:/home/seluser/dev:rw,delegated -w /home/seluser/dev xyz-headless-chrome:1.0.0 ./gradlew --rerun-tasks test --console=plain -Dbrowser=headless
```

#### Configure project for your web application:
##### Edit server profiles to point to application servers:
```
XYZTestSanity/src/test/resources/profiles.xyz.json
```

##### Edit .pagemodel files for your application:
```
XYZPageModels/src/main/resources/pagemodels/
```

##### Rebuild to regenerate page model classes:
###### Linux and MacOS:
```
./gradlew --rerun-tasks XYZPageModels:build
```
###### Windows:
```
gradlew.bat --rerun-tasks XYZPageModels:build
```

##### Update tests in XYZTestSanity and run tests:
###### Linux and MacOS:
```
./gradlew --rerun-tasks XYZTestSanity:test --console=plain
```
###### Windows:
```
gradlew.bat --rerun-tasks XYZTestSanity:test --console=plain
```
--------------------------------------------------------------------------------
## Testing beyond Selenium:
Page Model tools adds testing tools for **Email**, **SSH**, and **Accessibility** testing for comprehensive end-to-end testing.

Fetch emails for account registration code.  Fetch automated emails to test the content and styling.  Send emails to test auto-reply emails or other email workflows.

Use `.testMail()` or `Mail.testMail(context)` to begin mail testing.

Many things needed to test on a server are not available through a web UI.  SSH into servers to check logs or run commands during tests.

Use `.testSSH()` or `SSH.testSSH(context)` to begin SSH testing.

Run the accessibility scanner to test for WCAG violations.

Use `.testPage().testAccessibility()` to run an accessibility scan.

## Page Model Tools classes:

### PageModel
represents a web page; a collection of named Web Elements

### WebElementTester
allows testing and interacting with a Web Element

### ClickAction
defines expectations and behavior when clicking a Web Element

### ComponentModel
special WebElementTester with a collection of named child Web Elements

### SectionModel
special ComponentModel which is larger or requires more user interaction

### TestContext
manages a single browser instance and holds test variables and state information

provides storage to store and load captured values during testing

created for each test to allow parallel testing

## User-created application classes:

### XYZConfig
server information, admin user details, ssh credentials, mail credentials

contains any extra variables needed by the tests
            
### XYZUserDetails
user information which can be saved, loaded, and randomly generated

username, email, password, role
                
### XYZUser
active user object loaded from user details

can create, register, and login a user

### XYZTestContext
contains a XYZConfig defining the XYZ application server to test and external mail server to use

holds a single browser connection to the XYZ application server

can navigate to the login page and other external pages

### XYZPageModels/src/main/resources/pagemodels/
directory containing .pagemodel files for each page in the XYZ web application