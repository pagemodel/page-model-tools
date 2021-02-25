package org.pagemodel.tests.myapp.tools;

import org.pagemodel.tests.myapp.pages.HomePage;
import org.pagemodel.tests.myapp.pages.LoginPage;
import org.pagemodel.web.PageModel;

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
		return testContext.getLoginPage()
				.testUsernameField().sendKeys(userDetails.getUsername())
				.testPasswordField().sendKeys(userDetails.getPassword())
				.testSignInButton().click();
	}

	public <T extends PageModel<? super T>> T loginExpecting(Class<T> pageModel) {
		if (!accountCreated) {
			createAccount();
		}
		return testContext.getLoginPage()
				.testUsernameField().sendKeys(userDetails.getUsername())
				.testPasswordField().sendKeys(userDetails.getPassword())
				.testSignInButton().clickAnd().expectRedirect(pageModel);
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
