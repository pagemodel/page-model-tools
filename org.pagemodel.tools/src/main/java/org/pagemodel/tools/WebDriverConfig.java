
package org.pagemodel.tools;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.pagemodel.core.utils.json.JsonBuilder;
import org.pagemodel.core.utils.json.JsonObjectBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class WebDriverConfig {

	private String remoteUrl;
	private Map<String,Object> capabilities;
	transient private MutableCapabilities caps;

	public static void registerDefaultProfiles() {
		Map<String, WebDriverConfig> defaultProfile = new HashMap<>();

		ChromeOptions chromeOpts = new ChromeOptions();
		chromeOpts.addArguments("--ignore-certificate-errors", "--disable-dev-shm-usage", "--silent", "--log-level=3");
		defaultProfile.put("chrome", local("chrome", chromeOpts.asMap()));

		ChromeOptions chromeHeadlessOpts = new ChromeOptions();
		chromeHeadlessOpts.addArguments("--ignore-certificate-errors", "--disable-dev-shm-usage", "--silent",
				"--log-level=3", "--headless", "--disable-gpu", "--window-size=1920,1080");
		defaultProfile.put("headless", local("chrome", chromeHeadlessOpts.asMap()));

		defaultProfile.put("firefox", local("firefox"));
		defaultProfile.put("edge", local("edge"));
		defaultProfile.put("ie", local("ie"));
		defaultProfile.put("safari", local("safari"));
		defaultProfile.put("opera", local("opera"));
		defaultProfile.put("operablink", local("operablink"));
		defaultProfile.put("htmlunit", local("htmlunit"));
		ProfileMap.addDefaults(WebDriverConfig.class, defaultProfile);
	}

	public static WebDriverConfig local(String browser){
		WebDriverConfig webDriverConfig = new WebDriverConfig();
		webDriverConfig.caps = new MutableCapabilities();
		webDriverConfig.caps.setCapability(CapabilityType.BROWSER_NAME, browser);
		webDriverConfig.capabilities = new TreeMap<>(webDriverConfig.caps.asMap());
		return webDriverConfig;
	}

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

	public String getRemoteUrl() {
		return remoteUrl;
	}

	public void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
	}

	public MutableCapabilities getCapabilities() {
		if(caps == null){
			caps = new MutableCapabilities(capabilities);
		}
		return caps;
	}

	public void setCapabilities(MutableCapabilities capabilities) {
		this.capabilities = capabilities.asMap();
		this.caps = capabilities;
	}

	public void updateField(String field, Consumer<JsonObjectBuilder> objUpdate){
		caps = null;
		JsonBuilder.object(capabilities).updateObject(field, objUpdate);
	}

	public void setField(String field, Object value){
		caps = null;
		capabilities.put(field, value);
	}

	public JsonObjectBuilder modify(){
		caps = null;
		return JsonBuilder.object(capabilities);
	}
}
