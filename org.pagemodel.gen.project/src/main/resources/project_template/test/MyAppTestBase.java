package org.pagemodel.tests.myapp.test.sanity;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.pagemodel.mail.MailAuthenticator;
import org.pagemodel.mail.PopServer;
import org.pagemodel.mail.SmtpServer;
import org.pagemodel.tests.myapp.tools.MyAppConfig;
import org.pagemodel.tests.myapp.tools.MyAppTestContext;
import org.pagemodel.tools.ProfileMap;
import org.pagemodel.tools.WebDriverConfig;

import static org.pagemodel.core.utils.SystemProperties.readSystemProperty;

public class MyAppTestBase {
	protected static boolean configLoaded = false;
	private static ProfileMap<MyAppConfig> myAppProfiles;
	private static ProfileMap<MailAuthenticator> mailProfiles;
	private static ProfileMap<WebDriverConfig> driverProfiles;
	private static MailAuthenticator externalMailAuth;

	protected static MyAppConfig myAppConfig;
	protected static PopServer externalPop;
	protected static SmtpServer externalSmtp;
	protected static WebDriverConfig webDriverConfig;

	protected MyAppTestContext context;

	@BeforeClass
	public static synchronized void loadConfig() {
		if (!configLoaded) {
			myAppProfiles = ProfileMap.loadFile(MyAppConfig.class, "src/test/resources/profiles.myapp.json");
			mailProfiles = ProfileMap.loadFile(MailAuthenticator.class, "src/test/resources/profiles.mail.json");
			driverProfiles = ProfileMap.loadFile(WebDriverConfig.class, "src/test/resources/profiles.driver.json");

			webDriverConfig = driverProfiles.getProfile(readSystemProperty("driver", "chrome"));
			myAppConfig = myAppProfiles.getProfile(readSystemProperty("myapp", "dev"));
			externalMailAuth = mailProfiles.getProfile(readSystemProperty("mail.external", "dev"));

			externalPop = new PopServer(externalMailAuth);
			externalSmtp = new SmtpServer(externalMailAuth);
			configLoaded = true;
		}
	}

	@Before
	public void createTestContext() {
		context = new MyAppTestContext(myAppConfig, webDriverConfig);
	}

	@After
	public void closeBrowsers() {
		context.quit();
	}
}
