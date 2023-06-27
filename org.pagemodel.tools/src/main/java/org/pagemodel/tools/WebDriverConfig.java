
package org.pagemodel.tools;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.pagemodel.core.utils.json.JsonBuilder;
import org.pagemodel.core.utils.json.JsonObjectBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * This class represents a configuration for a WebDriver instance. It contains information about the browser to be used, its capabilities, and the remote URL if applicable.
 * It also provides methods for registering default profiles, creating local WebDriver configurations, and modifying the capabilities of an existing configuration.
 */
public class WebDriverConfig {

	/**
	 * The remote URL for the WebDriver instance.
	 */
	private String remoteUrl;

	/**
	 * The capabilities of the WebDriver instance.
	 */
	private Map<String,Object> capabilities;

	/**
	 * The mutable capabilities of the WebDriver instance.
	 */
	transient private MutableCapabilities caps;

	/**
	 * Registers default profiles for various browsers.
	 */
	public static void registerDefaultProfiles() {
		// Create a map of default profiles for various browsers
		Map<String, WebDriverConfig> defaultProfile = new HashMap<>();

		// Create a ChromeOptions object with specific arguments and add it to the default profile map
		ChromeOptions chromeOpts = new ChromeOptions();
		chromeOpts.addArguments("--ignore-certificate-errors", "--disable-dev-shm-usage", "--silent", "--log-level=3");
		defaultProfile.put("chrome", local("chrome", chromeOpts.asMap()));

		// Create a ChromeOptions object with specific arguments for headless mode and add it to the default profile map
		ChromeOptions chromeHeadlessOpts = new ChromeOptions();
		chromeHeadlessOpts.addArguments("--ignore-certificate-errors", "--disable-dev-shm-usage", "--silent",
				"--log-level=3", "--headless", "--disable-gpu", "--window-size=1920,1080");
		defaultProfile.put("headless", local("chrome", chromeHeadlessOpts.asMap()));

		// Create a FirefoxOptions object with specific arguments for headless mode and add it to the default profile map
		FirefoxOptions firefoxHeadlessOpts = new FirefoxOptions();
		firefoxHeadlessOpts.addArguments("--ignore-certificate-errors", "--disable-dev-shm-usage", "--silent",
				"--log-level=3", "--headless", "--disable-gpu", "--window-size=1920,1080");
		defaultProfile.put("headless-firefox", local("firefox", firefoxHeadlessOpts.asMap()));

		// Add default profiles for Firefox, Edge, IE, Safari, Opera, and Opera Blink to the map
		defaultProfile.put("firefox", local("firefox"));
		defaultProfile.put("edge", local("edge"));
		defaultProfile.put("ie", local("ie"));
		defaultProfile.put("safari", local("safari"));
		defaultProfile.put("opera", local("opera"));
		defaultProfile.put("operablink", local("operablink"));
		defaultProfile.put("htmlunit", local("htmlunit"));

		// Add the default profile map to the ProfileMap
		ProfileMap.addDefaults(WebDriverConfig.class, defaultProfile);
	}

	/**
	 * Creates a local WebDriver configuration for the specified browser.
	 * @param browser The name of the browser to use.
	 * @return A new WebDriverConfig object with the specified browser and default capabilities.
	 */
	public static WebDriverConfig local(String browser){
		WebDriverConfig webDriverConfig = new WebDriverConfig();
		webDriverConfig.caps = new MutableCapabilities();
		webDriverConfig.caps.setCapability(CapabilityType.BROWSER_NAME, browser);
		webDriverConfig.capabilities = new TreeMap<>(webDriverConfig.caps.asMap());
		return webDriverConfig;
	}

	/**
	 * Creates a local WebDriver configuration for the specified browser with the specified capabilities.
	 * @param browser The name of the browser to use.
	 * @param capabilities The capabilities to set for the WebDriver instance.
	 * @return A new WebDriverConfig object with the specified browser and capabilities.
	 */
	public static WebDriverConfig local(String browser, Map<String,Object> capabilities){
		WebDriverConfig webDriverConfig = new WebDriverConfig();
		webDriverConfig.caps = new MutableCapabilities();
		webDriverConfig.caps.setCapability(CapabilityType.BROWSER_NAME, browser);
		for(Map.Entry<String,Object> entry : capabilities.entrySet()){
			webDriverConfig.caps.setCapability(entry.getKey(), entry.getValue());
		}
		webDriverConfig.capabilities = new TreeMap<>(webDriverConfig.caps.asMap());
		return webDriverConfig;
	}

	/**
	 * Gets the remote URL for the WebDriver instance.
	 * @return The remote URL for the WebDriver instance.
	 */
	public String getRemoteUrl() {
		return remoteUrl;
	}

	/**
	 * Sets the remote URL for the WebDriver instance.
	 * @param remoteUrl The remote URL to set.
	 */
	public void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
	}

	/**
	 * Gets the mutable capabilities for the WebDriver instance.
	 * @return The mutable capabilities for the WebDriver instance.
	 */
	public MutableCapabilities getCapabilities() {
		if(caps == null){
			caps = new MutableCapabilities(capabilities);
		}
		return caps;
	}

	/**
	 * Sets the capabilities for the WebDriver instance.
	 * @param capabilities The capabilities to set.
	 */
	public void setCapabilities(MutableCapabilities capabilities) {
		this.capabilities = capabilities.asMap();
		this.caps = capabilities;
	}

	/**
	 * Updates a specific field in the capabilities of the WebDriver instance.
	 * @param field The name of the field to update.
	 * @param objUpdate A Consumer that updates the JsonObjectBuilder for the field.
	 */
	public void updateField(String field, Consumer<JsonObjectBuilder> objUpdate){
		caps = null;
		JsonBuilder.object(capabilities).updateObject(field, objUpdate);
	}

	/**
	 * Sets a specific field in the capabilities of the WebDriver instance.
	 * @param field The name of the field to set.
	 * @param value The value to set for the field.
	 */
	public void setField(String field, Object value){
		caps = null;
		capabilities.put(field, value);
	}

	/**
	 * Modifies the capabilities of the WebDriver instance using a JsonObjectBuilder.
	 * @return A JsonObjectBuilder for modifying the capabilities of the WebDriver instance.
	 */
	public JsonObjectBuilder modify(){
		caps = null;
		return JsonBuilder.object(capabilities);
	}
}