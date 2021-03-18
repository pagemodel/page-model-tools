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

import com.google.gson.Gson;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.pagemodel.web.DefaultWebTestContext;
import org.pagemodel.web.utils.PageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public abstract class WebDriverFactory {
	private static final Logger log = LoggerFactory.getLogger(WebDriverFactory.class);

	public static final int DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS = 20;
	public static final int DEFAULT_IMPLICITLY_WAIT_SECONDS = 20;
	public static final int DEFAULT_SCRIPT_TIMEOUT_SECONDS = 20;
	public final static String DOWNLOAD_DIRECTORY;

//	public final static BrowserOptions browserOptions = new BrowserOptions();
	private final static Map<String, Function<MutableCapabilities,WebDriver>> browserFactoryMap = new HashMap<>();

	static {
		String home = System.getProperty("user.home");
		DOWNLOAD_DIRECTORY = home + "/Downloads";
		System.setProperty("webdriver.chrome.silentOutput", "true");

		browserFactoryMap.put("chrome", WebDriverFactory::openChrome);
		browserFactoryMap.put("headless", WebDriverFactory::openChromeHeadless);
		browserFactoryMap.put("firefox", WebDriverFactory::openFirefox);
		browserFactoryMap.put("ie", WebDriverFactory::openInternetExplorer);
		browserFactoryMap.put("edge", WebDriverFactory::openEdge);
		browserFactoryMap.put("safari", WebDriverFactory::openSafari);
		browserFactoryMap.put("opera", WebDriverFactory::openOpera);
		browserFactoryMap.put("operablink", WebDriverFactory::openOpera);
		browserFactoryMap.put("htmlunit", WebDriverFactory::openHtmlUnit);
	}

	/**
	 *
	 * @deprecated Provided for backwards compatibility.  Please use the create method instead.
	 */
	@Deprecated()
	public static WebDriver open(String browser, String url) {
		WebDriverConfig webDriverConfig = WebDriverConfig.local(browser);
		return create(webDriverConfig, url);
	}

	public static WebDriver create(WebDriverConfig config, String url){
		WebDriver driver;
		if (config.getRemoteUrl() != null) {
			log.info("Opening url [" + url + "] with capablities: " + new Gson().toJson(config.getCapabilities().toJson()) + ", remote url: [" + config.getRemoteUrl() + "]");
			driver = getRemoteWebDriver(config.getRemoteUrl(), config.getCapabilities());

		}else{
			log.info("Opening url [" + url + "] with capablities: " + new Gson().toJson(config.getCapabilities().toJson()));
			driver = getWebDriver(config.getCapabilities());
		}
		try {
			driver.get(url);
			clickThroughCertErrorPage(driver);
		} catch (Throwable e){
			close(driver);
			throw new PageException(new DefaultWebTestContext(driver),
					"Error: Failed opening url ["+ url + "] with capablities: " + new Gson().toJson(config));
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

	public static WebDriver getWebDriver(MutableCapabilities capabilities) {
		String browser = capabilities.getBrowserName();
		if(!browserFactoryMap.containsKey(browser)){
			throw new RuntimeException("Error: Unknown browser type [" + browser + "].  "
					+ "Available browser types are: " + Arrays.toString(browserFactoryMap.keySet().toArray(new String[0])));
		}
		try {
			WebDriver driver = browserFactoryMap.get(browser).apply(capabilities);
			driver.manage().timeouts().pageLoadTimeout(DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			driver.manage().timeouts().implicitlyWait(DEFAULT_IMPLICITLY_WAIT_SECONDS, TimeUnit.SECONDS);
			driver.manage().timeouts().setScriptTimeout(DEFAULT_SCRIPT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			return driver;
		} catch (Exception ex){
			throw new RuntimeException("Error: Unable to open browser with type [" + browser + "]", ex);
		}
	}

	public static RemoteWebDriver getRemoteWebDriver(String remoteURL, Capabilities caps) {
		try {
			RemoteWebDriver driver = new RemoteWebDriver(new URL(remoteURL), caps);
			driver.setFileDetector(new LocalFileDetector());

			return driver;
		} catch (MalformedURLException e) {
			log.error("Could not open web driver to remote URL: " + remoteURL, e);
			throw new RuntimeException(e);
		}
	}

	private static WebDriver openChrome(MutableCapabilities capabilities) {
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = loadChromeOptions(capabilities);
		ChromeDriverService driverService = ChromeDriverService.createDefaultService();
		ChromeDriver driver = new ChromeDriver(driverService, options);
		if(new Gson().toJson(capabilities.toJson()).contains("--headless")) {
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
		}
		return driver;
	}

	/**
	 * Convert a Capabilities to ChromeOptions, fixes bug in ChromeOptions that ignores and overwrites any
	 * goog:chromeOptions in ChromeOptions.merge(Capabilities)
	 * @param capabilities
	 * @return
	 */
	protected static ChromeOptions loadChromeOptions(Capabilities capabilities){
		if(ChromeOptions.class.isAssignableFrom(capabilities.getClass())){
			return (ChromeOptions) capabilities;
		}
		ChromeOptions options = new ChromeOptions().merge(capabilities);
		Map<String,Object> capMap = capabilities.asMap();
		if(capMap.containsKey(ChromeOptions.CAPABILITY)){
			Object chromeOptObj = capMap.get(ChromeOptions.CAPABILITY);
			if(Map.class.isAssignableFrom(chromeOptObj.getClass())){
				Map<Object,Object> chromeOptMap = (Map<Object,Object>)chromeOptObj;
				for(Map.Entry entry : chromeOptMap.entrySet()) {
					if (entry.getKey().equals("args")) {
						if (String[].class.isAssignableFrom(entry.getValue().getClass())) {
							String[] args = (String[]) entry.getValue();
							options.addArguments(args);
						}else if (List.class.isAssignableFrom(entry.getValue().getClass())) {
							List args = (List) entry.getValue();
							options.addArguments(args);
						}
					}else if (entry.getKey().equals("binary")) {
						if (String.class.isAssignableFrom(entry.getValue().getClass())) {
							String binary = (String) entry.getValue();
							options.setBinary(binary);
						}
					}else if (entry.getKey().equals("extensions")) {
						if (String[].class.isAssignableFrom(entry.getValue().getClass())) {
							String[] extensions = (String[]) entry.getValue();
							options.addEncodedExtensions(extensions);
						}else if (List.class.isAssignableFrom(entry.getValue().getClass())) {
							List extensions = (List) entry.getValue();
							options.addEncodedExtensions(extensions);
						}
					}else {
						if (String.class.isAssignableFrom(entry.getKey().getClass())) {
							options.setExperimentalOption((String)entry.getKey(), entry.getValue());
						}
					}
				}
			}
		}
		options.setCapability(CapabilityType.BROWSER_NAME, "chrome");
		return options;
	}

	private static WebDriver openChromeHeadless(Capabilities capabilities) {
		ChromeOptions options = loadChromeOptions(capabilities);
		options.setHeadless(true);
		return openChrome(options);
	}

	private static WebDriver openFirefox(Capabilities capabilities) {
		WebDriverManager.firefoxdriver().setup();
		FirefoxOptions options = new FirefoxOptions(capabilities);
		return new FirefoxDriver(options);
	}

	private static WebDriver openInternetExplorer(Capabilities capabilities) {
		WebDriverManager.iedriver().setup();
		InternetExplorerOptions options = new InternetExplorerOptions(capabilities);
		return new InternetExplorerDriver(options);
	}

	private static WebDriver openEdge(Capabilities capabilities) {
		WebDriverManager.edgedriver().setup();
		return new EdgeDriver(new EdgeOptions().merge(capabilities));
	}

	private static WebDriver openOpera(Capabilities capabilities) {
		WebDriverManager.operadriver().setup();
		// Replicate loadChromeOptions for Opera to fix use of deprecated constructor.  OperaOptions has the same bug as ChromeOptions.
		return new OperaDriver(capabilities);
	}

	private static WebDriver openSafari(Capabilities capabilities) {
		SafariOptions options = new SafariOptions(capabilities);
		return new SafariDriver(options);
	}

	private static WebDriver openHtmlUnit(Capabilities capabilities) {
		return new HtmlUnitDriver(true);
	}
}
