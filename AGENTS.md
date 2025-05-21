## Project Overview
Page Model Tools provides a Java-based testing framework built around
page object models and a fluent API. It includes utilities for web,
SSH, and mail testing plus generators for project scaffolding and page
model code.

## Package Guide
- **org.pagemodel.core** – Core testing utilities. Defines `TestContext`
and `TestEvaluator` along with helper testers (`StringTester`,
`ComparableTester`) and JSON/logging helpers.
- **org.pagemodel.web** – Web testing layer built on Selenium. Contains
  `PageModel`, `ComponentModel`, `SectionModel`, `WebTestContext`, and
  element testers. Relies on `org.pagemodel.core` for evaluation.
- **org.pagemodel.junit4** – JUnit4 integration. Provides
  `LoggingTestRule` to capture logs during tests.
- **org.pagemodel.syntax** – Editor support files (.pagemodel syntax
  highlighters).
- **org.pagemodel.gen.gradle** – Gradle plugin and tools for generating
  Java page models from `.pagemodel` files. Key classes: `PageModelReader`
  and `PageModelJavaWriter`.
- **org.pagemodel.gen.project** – Command‑line project generator for
  creating ready-to-run test projects.
- **org.pagemodel.mail** – Utilities for email testing, including
  `MailServer`, `MailMessage`, `MailTestContext`, and testers for email
  contents.
- **org.pagemodel.ssh** – SSH testing support with `SSHTestContext`,
  `SSHSession`, and command testers.
- **org.pagemodel.tools** – Extended helpers used by example tests.
  Includes `ExtendedTestContext`, `WebDriverFactory`, and accessibility
  scanning via `AXEScanner`.
- **org.pagemodel.tests** – Example page models and contexts demonstrating
  usage of the above packages.

## Core Classes & Interactions
- **TestContext** (`org.pagemodel.core`) – Stores test state and provides
  access to a `TestEvaluator`. Implemented by `DefaultTestContext` and
  extended by `WebTestContext`, `MailTestContext`, and `SSHTestContext`.
- **TestEvaluator** (`org.pagemodel.core`) – Handles logging and
  evaluation of test actions. Used throughout the framework and wrapped
  by specialized evaluators (e.g., `WebTestEvaluator`).
- **PageModel** / **ComponentModel** / **SectionModel**
  (`org.pagemodel.web`) – Abstract representations of pages and elements.
  Provide fluent actions and rely on a `WebTestContext` for WebDriver
  access. Element testers like `WebElementTester`, `PageTester`, and
  `AlertTester` operate on these models.
- **LoggingTestRule** (`org.pagemodel.junit4`) – Integrates the
  evaluator’s logging with JUnit4 test execution.
- **ExtendedTestContext** (`org.pagemodel.tools`) – Combines web,
  mail, and SSH contexts and creates WebDrivers via `WebDriverFactory`.
  Example test packages depend on this for full-stack testing.
- **PageModel generators** (`org.pagemodel.gen.gradle` and
  `org.pagemodel.gen.project`) – Read `.pagemodel` files and produce Java
  classes or entire project structures, enabling automated model creation.
