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

package org.pagemodel.tests.myapp.tools;

import org.pagemodel.tests.myapp.pages.HomePage;
import org.pagemodel.tests.myapp.pages.LoginPage;
import org.pagemodel.web.PageModel;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class MyAppUser {

	private MyAppUserDetails userDetails;
	private MyAppTestContext testContext;
	private boolean accountCreated;
	private boolean accountRegistered;

	public static MyAppUser admin(MyAppTestContext testContext) {
		MyAppUser admin = new MyAppUser(testContext.getMyAppConfig().getAdminDetails(), testContext);
		admin.accountCreated = true;
		admin.accountRegistered = true;
		return admin;
	}

	public static MyAppUser generateUser(MyAppTestContext testContext, String userRole) {
		return new MyAppUser(MyAppUserDetails.generate(userRole), testContext);
	}

	public MyAppUser(MyAppUserDetails userDetails, MyAppTestContext testContext) {
		this.userDetails = userDetails;
		this.accountCreated = false;
		this.accountRegistered = false;
		this.testContext = testContext;
	}

	public String getUsername() {
		return userDetails.getUsername();
	}

	public void setUsername(String username) {
		userDetails.setUsername(username);
	}

	public String getPassword() {
		return userDetails.getPassword();
	}

	public void setPassword(String password) {
		userDetails.setPassword(password);
	}

	public String getEmail() {
		return userDetails.getEmail();
	}

	public void setEmail(String email) {
		userDetails.setEmail(email);
	}

	public String getUserRole() {
		return userDetails.getUserRole();
	}

	public void setUserRole(String userRole) {
		userDetails.setUserRole(userRole);
	}

	public MyAppTestContext getTestContext() {
		return testContext;
	}

	public void setAccountCreated(boolean accountCreated) {
		this.accountCreated = accountCreated;
	}

	public void setAccountRegistered(boolean accountRegistered) {
		this.accountRegistered = accountRegistered;
	}

	public LoginPage getLoginPage() {
		return testContext.getLoginPage();
	}

	public HomePage loginToMainPage() {
		if (!accountCreated) {
			createAccount();
		}
		if (!accountRegistered) {
			verifyAccount();
		}
		return getLoginPage()
				.testUsernameField().sendKeys(userDetails.getUsername())
				.testPasswordField().sendKeys(userDetails.getPassword())
				.testSignInButton().click();
	}

	public <T extends PageModel<? super T>> T loginExpecting(Class<T> pageModel) {
		if (!accountCreated) {
			createAccount();
		}
		return getLoginPage()
				.testUsernameField().sendKeys(userDetails.getUsername())
				.testPasswordField().sendKeys(userDetails.getPassword())
				.testSignInButton().clickAnd().expectRedirect(pageModel);
	}

	public MyAppUser createAccount() {
		if (accountCreated) {
			return this;
		}
		// Login as admin and add user.
		accountCreated = true;
		return this;
	}

	public MyAppUser verifyAccount() {
		if (!accountCreated) {
			createAccount();
		}
		if (accountRegistered) {
			return this;
		}
		// Fetch verification code from email for first time login.
		accountRegistered = true;
		return this;
	}
}
