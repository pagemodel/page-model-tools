# Page Model Project Structure

This document describes the directories and files of a generated project.

Our project is generated with the following command:
```
java -jar org.pagemodel.gen.project-0.8.1.jar XYZ com.example.pmt XYZTest
```
command output:
```
Generating org.pagemodel project:
ProjectName: XYZ
ProjectGroup: com.example.pmt
ProjectPackage: com.example.pmt.xyz
ProjectDir: XYZTest
```
The project name `XYZ` and the lowercase `xyz` is used as a prefix for project, class, and variable names.

The project package `com.example.pmt.xyz` combines the group name `com.example.pmt` and the project name `xyz`

All occurrences of `XYZ` or `xyz` will be replaced with your project name when generating a new project.  If `FooBar` is used for a project name instead of `XYZ`: `profiles.xyz.json` -> `profiles.foobar.json`, `XYZUserDetails.java` -> `FooBarUserDetails.java`, etc ...

## Project files:

A directory `XYZTest` is created with the generated project.
```
XYZTest
├── XYZPageModels
│   ├── src/main
│   │   ├── java/com/example/pmt/xyz/tools
│   │   │   ├── XYZConfig.java
│   │   │   ├── XYZTestContext.java
│   │   │   ├── XYZUser.java
│   │   │   └── XYZUserDetails.java
│   │   └── resources/pagemodels
│   │       ├── HomePage.pagemodel
│   │       ├── LoginPage.pagemodel
│   │       ├── ManageUsersPage.pagemodel
│   │       └── XYZInternalPage.pagemodel
│   └── build.gradle
├── XYZTestSanity
│   ├── src/test
│   │   ├── java/com/example/pmt/xyz/test/sanity
│   │   │   ├── PageTests.java
│   │   │   └── XYZTestBase.java
│   │   └── resources
│   │           ├── profiles.driver.json
│   │           ├── profiles.mail.json
│   │           └── profiles.xyz.json
│   └── build.gradle
├── example_html
│   ├── home.html
│   ├── login.html
│   ├── manage_users.html
│   └── style.css
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── scripts
│   ├── docker
│   │   ├── build-docker.sh
│   │   └── xyz-headless-chrome.dockerfile
│   └── dtest
├── build.gradle
├── gradlew
├── gradlew.bat
└── settings.gradle
```

## XYZTest project root directory:
### Gradle files:
These are standard gradle files for setting up a gradle project.
* **build.gradle** - defines the parent gradle project
* **settings.gradl**e - lists the directories of gradle sub-projects: `XYZPageModels` and `XYZTestSanity`
* **gradlew** - shell script for running gradle tasks
* **gradlew.bat** - windows batch script for running gradle commands
* **gradle/** - properties defining gradle version, and gradle wrapper jar

### Sub-projects:
* **XYZPageModels/** - page model definitions
* **XYZTestSanity/** - tests project
### Extras:
* **example_html/** - example html corresponding to example pagemodel files and example tests
* **scripts/** - docker scripts

## XYZPageModels project
This project describes your web application.  It contains the `.pagemodel` files and a small number of a java classes that are customized to your application.
### Page model files:
* **src/main/resources/pagemodels** - `.pagemodel` files here and in sub-directories will generate java classes on build
* **src/gen/java** - pagemodel java classes are generated here. This directory is ignored by git
### Java classes:
* **src/main/java/com/example/pmt/xyz/tools** - contains 4 java classes which need to be customized to fit your application.
##### Record objects:
record objects are simple serializable classes with only data fields and no logic.  Other record objects are WebDriverConfig, MailAuthenticator, and SSHAuthenticator.
* **XYZUserDetails.java** - application user data used for testing: username, password, email, etc.
* **XYZConfig.java** - data needed for an application instance: protocol, hostname, and port of the application, user details, and could include an SSHAuthenticator
`XYZConfig` along with `XYZUserDetails` are loaded from `profiles.xyz.json` during test setup.
##### Test objects:
these are stateful objects used during testing which are instantiated from record objects.
* **XYZTestContext.java** - holds a single browser instance for testing, and can navigate to the login page and other external landing pages.
* **XYZUser.java** - created from an XYZUserDetails and XYZTestContext, represents a user browser session in a test.  Customized to add common repeated user actions such as login, create, and delete.

## XYZTestSanity project
This project is where tests are added.  This project can be copied and renamed to organize your tests into multiple projects. (make sure to add any new projects to the `settings.gradle` file)

This project depends on the `XYZPageModels` project, and can depend on other application page model projects when tests involve visiting multiple applications.

This project uses `JUnit4` for testing and `Logback` as the `SLF4J` logger implementation.  These can be changed to suit your preferences.

### Resources:
**src/test/resources** - these resources are loaded during test setup in `XYZTestBase.java`
* **profiles.xyz.json** - `XYZConfig` profiles to target different application instances e.g. dev, test, and prod servers 
* **profiles.driver.json** - `WebDriverConfig` profiles to target different local or remote browsers with desired capabilities
* **profiles.mail.json** - `MailAuthenticator` profiles for SMTP and POP3 mail servers

### Tests:
**src/test/java/com/example/pmt/xyz/test/sanity**
* **XYZTestBase.java** - base class for tests to inherit, loads profiles and sets up a TestContext for tests
* **PageTests.java** - contains an example test running against the html files in `example_html`
