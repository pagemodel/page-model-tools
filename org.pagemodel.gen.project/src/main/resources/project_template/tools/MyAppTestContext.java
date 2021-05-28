package org.pagemodel.tests.myapp.tools;

import org.pagemodel.tests.myapp.pages.LoginPage;
import org.pagemodel.tools.ExtendedTestContext;
import org.pagemodel.tools.WebDriverConfig;
import org.pagemodel.web.PageUtils;
import java.io.File;

public class MyAppTestContext extends ExtendedTestContext {
	private MyAppConfig myAppConfig;

	public MyAppTestContext(MyAppConfig myAppConfig, WebDriverConfig webDriverConfig) {
		super(null, myAppConfig.getSshAuth(), webDriverConfig);
		this.myAppConfig = myAppConfig;
	}

	public LoginPage getLoginPage() {
		openPage(getApplicationUrl("/login.html"));
		return PageUtils.waitForNavigateToPage(LoginPage.class, this);
	}

	public MyAppConfig getMyAppConfig() {
		return myAppConfig;
	}

	private String getApplicationUrl(String urlPath){
		String port = myAppConfig.getPort();
		port = port == null || port.isEmpty() ? "" : ":" + port;
		String hostPath = myAppConfig.getProtocol().equals("file") ? new File(myAppConfig.getHostname()).getAbsolutePath() : myAppConfig.getHostname();
		return String.format("%s://%s%s%s", myAppConfig.getProtocol(), hostPath, port, urlPath);
	}
}
