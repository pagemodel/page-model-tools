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

package org.pagemodel.tools;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.pagemodel.web.DefaultWebTestContext;
import org.pagemodel.web.utils.PageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public abstract class WebDriverFactory {
	private static final Logger log = LoggerFactory.getLogger(WebDriverFactory.class);

	public static final int DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS = 20;
	public static final int DEFAULT_IMPLICITLY_WAIT_SECONDS = 20;
	public static final int DEFAULT_SCRIPT_TIMEOUT_SECONDS = 20;
	public final static String DOWNLOAD_DIRECTORY;

	public final static BrowserOptions browserOptions = new BrowserOptions();
	private final static Map<String, Callable<WebDriver>> browserFactoryMap = new HashMap<>();

	static {
		String home = System.getProperty("user.home");
		DOWNLOAD_DIRECTORY = home + "/Downloads";
		System.setProperty("webdriver.chrome.silentOutput", "true");

		browserFactoryMap.put("chrome", WebDriverFactory::openChrome);
		browserFactoryMap.put("headless", WebDriverFactory::openChromeHeadless);
		browserFactoryMap.put("firefox", WebDriverFactory::openFirefox);
		browserFactoryMap.put("ie", WebDriverFactory::openInternetExplorer);
		browserFactoryMap.put("edge", WebDriverFactory::openEdge);
		browserFactoryMap.put("htmlunit", WebDriverFactory::openHtmlUnit);
	}

	public static WebDriver open(String browser, String url) {
		log.info("Opening url [" + url + "] in browser [" + browser + "]");
		WebDriver driver = getWebDriver(browser);
		try {
			driver.get(url);
			clickThroughCertErrorPage(driver);
		} catch (Throwable e) {
			close(driver);
			throw new PageException(new DefaultWebTestContext(driver),
					"Error: Failed opening url [" + url + "] in browser [" + browser + "]");
		}
		return driver;
	}

	public static void close(WebDriver driver) {
		if (driver == null) {
			return;
		}
		try {
			driver.close();
			driver.quit();
		} catch (Throwable e) {
			log.debug("Exception caught closing WebDriver.", e);
		}
	}

	private static void clickThroughCertErrorPage(WebDriver driver) {
		if (driver.getPageSource().contains("overridelink")) {
			driver.findElement(By.id("overridelink")).click();
		}
	}

	public static WebDriver getWebDriver(String browser) {
		if(!browserFactoryMap.containsKey(browser)){
			throw new RuntimeException("Error: Unknown browser type [" + browser + "].  "
					+ "Available broswer types are: " + Arrays.toString(browserFactoryMap.keySet().toArray(new String[0])));
		}
		try {
			WebDriver driver = browserFactoryMap.get(browser).call();
			driver.manage().timeouts().pageLoadTimeout(DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			driver.manage().timeouts().implicitlyWait(DEFAULT_IMPLICITLY_WAIT_SECONDS, TimeUnit.SECONDS);
			driver.manage().timeouts().setScriptTimeout(DEFAULT_SCRIPT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			return driver;
		} catch (Exception ex){
			throw new RuntimeException("Error: Unable to open browser with type [" + browser + "]", ex);
		}
	}

	public static DesiredCapabilities makeDesiredCapabilities(String platformName, String platformVersion, String deviceName){
		DesiredCapabilities caps = new DesiredCapabilities();
		if (platformName != null) {
			caps.setCapability(CapabilityType.PLATFORM_NAME, platformName);
		}
		if (platformVersion != null) {
			caps.setCapability("platformVersion", platformVersion);
		}
		if (deviceName != null) {
			caps.setCapability("deviceName", deviceName);
		}
		return caps;
	}

	public static RemoteWebDriver getRemoteWebDriver(String browser, String remoteURL, DesiredCapabilities caps) {
		try {
			caps.setBrowserName(browser);
			return new RemoteWebDriver(new URL(remoteURL), caps);
		} catch (MalformedURLException e) {
			log.error("Could not open web driver to remote URL: " + remoteURL, e);
			throw new RuntimeException(e);
		}
	}

	private static WebDriver openChrome() {
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
		options.addArguments(browserOptions.getBrowserOptions("chrome"));
		return new ChromeDriver(options);
	}

	private static WebDriver openChromeHeadless() {
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
		options.setHeadless(true);
		options.addArguments(browserOptions.getBrowserOptions("headless"));
		ChromeDriverService driverService = ChromeDriverService.createDefaultService();
		WebDriver driver = new ChromeDriver(driverService, options);

		// The following code adds a special command to enable downloading files with headless chrome
		// by default headless chrome is set to not allow downloads for security.
		// Downloading works on MacOS, but is failing on Windows VM
		String command = new StringBuilder().append("{")
				.append("\"cmd\":\"Page.setDownloadBehavior\"").append(",")
				.append("\"params\":{")
				.append("\"downloadPath\":\"" + DOWNLOAD_DIRECTORY + "\"").append(",")
				.append("\"behavior\":\"allow\"").append("}")
				.append("}").toString();
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			String u = driverService.getUrl().toString() + "/session/" + ((ChromeDriver) driver).getSessionId() + "/chromium/send_command";
			HttpPost request = new HttpPost(u);
			request.addHeader("content-type", "application/json");
			request.setEntity(new StringEntity(command));
			httpClient.execute(request);
		} catch (Exception ex) {
			log.error("Error starting chrome headless", ex);
			throw new RuntimeException(ex);
		}
		return driver;
	}

	private static WebDriver openFirefox() {
		WebDriverManager.firefoxdriver().setup();
		FirefoxOptions options = new FirefoxOptions();
		options.addArguments(browserOptions.getBrowserOptions("firefox"));
//        FirefoxProfile fp = new FirefoxProfile();
//        fp.setAcceptUntrustedCertificates(true);
//        fp.setAssumeUntrustedCertificateIssuer(true);
//        DesiredCapabilities dc = DesiredCapabilities.firefox();
//        dc.setCapability(CapabilityType.ACCEPT_SSL_CERTS,true);
		return new FirefoxDriver(options);
	}

	private static WebDriver openInternetExplorer() {
		WebDriverManager.iedriver().setup();
		InternetExplorerOptions options = new InternetExplorerOptions();
		options.addCommandSwitches(browserOptions.getBrowserOptions("ie").toArray(new String[0]));
		return new InternetExplorerDriver(options);
	}

	private static WebDriver openEdge() {
		WebDriverManager.edgedriver().setup();
		return new EdgeDriver();
	}

	private static WebDriver openHtmlUnit() {
		return new HtmlUnitDriver(true);
	}
}
