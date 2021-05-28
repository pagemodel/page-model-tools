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

package org.pagemodel.test;

import org.junit.Test;
import org.pagemodel.core.utils.Unique;
import org.pagemodel.tests.myapp.tools.MyAppUser;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class PageTests extends MyAppTestBase {

	@Test
	public void testMyAppPages() {
		MyAppUser admin = MyAppUser.admin(context);
		MyAppUser.admin(context).loginToMainPage()
				.log("Starting test")
				.doAction(page -> {
					Throwable t = context.catchException(() -> {
						page.testTopNav().testManageUsersLink().text().contains("Sds");
					});
					if(t != null){
						context.log("Comparison failed", t);
					}
				})
				.testSiteStatusDisplay().text().equals("Online")
				.log("my action", op -> op.addValue("field", "value"))
				.testSiteVersionDisplay().text().startsWith("0.8.1")
				.testStatusDateDisplay().text().storeValue("status_date")
				.testStatusDateDisplay().text().asDate().storeValue("status_date3")
//				.testPage().waitFor().numberOfSeconds(1)
//				.testUpdateStatusButton().click()
//				.testStatusDateDisplay().text().notEquals(context.load("status_date"))
//
				.testStatusDateDisplay().waitAndRefreshFor().text().asDate()
				.transform(date -> (int) TimeUnit.SECONDS.convert(date.getTime() - context.loadDate("status_date3").getTime(), TimeUnit.MILLISECONDS))
				.greaterThan(5)
				.testStatusDateDisplay().text().asDate().storeValue("status_date2")
				.testStatusDateDisplay().waitAndRefreshFor().text().asDate().greaterThan(context.load("status_date2"))

				.testTopNav().testUserInfoDisplay().text().equals("Logged in as " + admin.getUsername())
				.testTopNav().testUserInfoDisplay().text().endsWith(admin.getUsername())
				.testTopNav().testUserInfoDisplay().text().testMatch("Logged in as (.*)", 1).equals(admin.getUsername())
				.testTopNav().testUserInfoDisplay().text().transform(str -> str.replace("Logged in as ", "")).equals(admin.getUsername())

				.testNotificationDisplay(1).testTitleDisplay().text().equals("Notification 1")
				.testNotificationDisplay(1).testDismissLink().click()
				.testNotificationDisplay(1).testTitleDisplay().waitFor().text().equals("Notification 2")

				.testPage().testAccessibility(
				"<html> element must have a lang attribute",
				"Form elements must have labels",
				"Page must have one main landmark",
				"All page content must be contained by landmarks")

				.testTopNav().testManageUsersLink().click()
				.testAddUserButton().click()
				.testUsernameField().sendKeys("newUser")
				.testEmailField().sendKeys(Unique.string("%s@example.com"))
				.testPasswordField().sendKeys("password")
				.testAdminCheckbox().setChecked()
				.testCancelButton().click()

				.testUserRow("admin").testDeleteButton().notEnabled()
				.testUserRow("bob.hall").testDeleteButton().click()
				.testUserRow("bob.hall").waitFor().notExists()
				.testUserRow("bob.hall").testDeleteButton().waitAndRefreshFor().click()
				.testUserRow("bob.hall").waitFor().notExists()

				.testTopNav().testHomeLink().click()
				.testNotificationDisplay(1).testTitleDisplay().text().storeValue("title1")
				.testNotificationDisplay(1).testDismissLink().click()
				.testNotificationDisplay(context.loadString("title1")).waitFor().notExists()

				.testItemReviewRow("Item 1").testUserDisplay().text().startsWith("bob")
				.testItemReviewRow("Item 1").testDeleteLink().click()
				.testItemReviewRow("Item 1").waitFor().notExists()

				.testItemReviewRow("Item 2").asSection()
				.testStatusDropDown().attribute("value").equals("Pending")
				.testUserDisplay().text().contains("sam")
				.testRoutingDisplay().text().endsWith("-a")
				.testSectionParent()

				.testTopNav().testSignOutLink().click()
				.closeBrowser();
	}

	@Test
	public void testSelect() {
		MyAppUser.admin(context).loginToMainPage()
				.testItemReviewRow("Item 2").asSection()
				.testStatusDropDown().selectText("Approved")
				.testStatusDropDown().optionsTextList().equalsOrdered(Arrays.asList("Pending", "Approved", "Denied"))
				.testStatusDropDown().optionsTextList().equalsOrdered("Pending", "Approved", "Denied")
				.testStatusDropDown().optionsValueList().equalsOrdered(Arrays.asList("Pending", "Approved", "Denied"))
				.testStatusDropDown().optionsValueList().equalsOrdered("Pending", "Approved", "Denied")
				.testStatusDropDown().optionsValueList().equalsUnordered("Approved", "Pending", "Denied")
				.testStatusDropDown().optionsValueList().notEqualsUnordered("Extra", "Approved", "Pending", "Denied")
				.testStatusDropDown().optionsTextList().storeValue("text")
				.testStatusDropDown().optionsValueList().equalsOrdered(context.<List<String>>load("text"))

				.testStatusDropDown().testOption(1).text().equals("Pending")
				.testStatusDropDown().testOptionByText("Approved").isSelected()
				.testStatusDropDown().testOption(1).setSelected()
				.testStatusDropDown().testOptionByText("Pending").isSelected()
				.testStatusDropDown().testOption(3).notSelected()
				.testStatusDropDown().testOptionByText("Denied").setSelected()
				.testStatusDropDown().testOptionByText("Pending").notSelected()
				.testStatusDropDown().testOptionByText("Denied").isSelected()
				.testStatusDropDown().testOption(3).isSelected()
				.testSectionParent()

				.testTopNav().testSignOutLink().click()
				.closeBrowser();
	}

	@Test
	public void testClickAndClickWait() {
		MyAppUser.admin(context).loginToMainPage()

				.testItemReviewRow("Item 1").testDeleteLink().click()
				.testItemReviewRow("Item 1").waitFor().notExists()
//				.testPage().refreshPage()
//				.testItemReviewRow("Item 1").testDeleteLink().waitFor().click()
//				.testItemReviewRow("Item 1").waitFor().notExists()
//				.testItemReviewRow("Item 1").testDeleteLink().waitAndRefreshFor().click()
//				.testItemReviewRow("Item 1").waitFor().notExists()
				.testTopNav().testSignOutLink().click()
				.closeBrowser();
	}
}
