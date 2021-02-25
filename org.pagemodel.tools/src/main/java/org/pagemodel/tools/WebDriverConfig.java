
package org.pagemodel.tools;

import java.util.HashMap;
import java.util.Map;

public class WebDriverConfig {

	private String remoteUrl;
	private String localBrowserName;
	private String[] localBrowserOptions;
	private Map<String, String> browserOptions;
	private String browserName;
	private String version;
	private String platForm;

	public static void registerDefaultProfiles() {
		Map<String, WebDriverConfig> defaultProfile = new HashMap<>();
		defaultProfile.put("chrome", local("chrome"));
		defaultProfile.put("headless", local("headless"));
		defaultProfile.put("firefox", local("firefox"));
		defaultProfile.put("edge", local("edge"));
		defaultProfile.put("ie", local("ie"));
		defaultProfile.put("htmlunit", local("htmlunit"));
		ProfileMap.addDefaults(WebDriverConfig.class, defaultProfile);
	}

	public static WebDriverConfig local(String browser){
		WebDriverConfig webDriverConfig = new WebDriverConfig();
		webDriverConfig.setLocalBrowserName(browser);
		return webDriverConfig;
	}

	public String getRemoteUrl() {
		return remoteUrl;
	}

	public void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
	}

	public String getLocalBrowserName() {
		return localBrowserName;
	}

	public void setLocalBrowserName(String localBrowserName) {
		this.localBrowserName = localBrowserName;
	}

	public String[] getLocalBrowserOptions() {
		return localBrowserOptions;
	}

	public void setLocalBrowserOptions(String[] localBrowserOptions) {
		this.localBrowserOptions = localBrowserOptions;
	}

	public Map<String, String> getBrowserOptions() {
		return browserOptions;
	}

	public void setBrowserOptions(Map<String, String> browserOptions) {
		this.browserOptions = browserOptions;
	}

	public String getBrowserName() {
		return browserName;
	}

	public void setBrowserName(String browserName) {
		this.browserName = browserName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPlatForm() {
		return platForm;
	}

	public void setPlatForm(String platForm) {
		this.platForm = platForm;
	}
}
