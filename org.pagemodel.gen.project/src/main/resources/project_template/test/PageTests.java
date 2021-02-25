package org.pagemodel.tests.myapp.test.sanity;

import org.junit.Test;
import org.pagemodel.core.utils.Unique;
import org.pagemodel.tests.myapp.tools.MyAppUser;

public class PageTests extends MyAppTestBase {

	@Test
	public void testMyAppPages(){
		MyAppUser admin = MyAppUser.admin(context);
		MyAppUser.admin(context).loginToMainPage()
				.testSiteStatusDisplay().text().equals("Online")
				.testSiteVersionDisplay().text().startsWith("0.8.0")
				.testStatusDateDisplay().text().storeValue("status_date")
				.testPage().waitFor().numberOfSeconds(1)
				.testUpdateStatusButton().click()
				.testStatusDateDisplay().text().notEquals(context.load("status_date"))

				.testStatusDateDisplay().text().asDate().storeValue("status_date2")
				.testStatusDateDisplay().waitAndRefreshFor().text().asDate().greaterThan(context.load("status_date2"))

				.testTopNav().testUserInfoDisplay().text().equals("Logged in as " + admin.getUsername())
				.testTopNav().testUserInfoDisplay().text().endsWith(admin.getUsername())
				.testTopNav().testUserInfoDisplay().text().testMatch("Logged in as (.*)").equals(admin.getUsername())
				.testTopNav().testUserInfoDisplay().text().transform(str -> str.replace("Logged in as ", "")).equals(admin.getUsername())

				.testNotificationDisplay(1).testTitleDisplay().text().equals("Notification 1")
				.testNotificationDisplay(1).testDismissLink().click()
				.testNotificationDisplay(1).testTitleDisplay().waitFor().text().equals("Notification 2")

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
				.testStatusDropDown().selectValue("Approved")
				.testSectionParent()

				.testTopNav().testSignOutLink().click()
				.closeBrowser();
	}
}
