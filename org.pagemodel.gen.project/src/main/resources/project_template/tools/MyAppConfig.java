package org.pagemodel.tests.myapp.tools;

public class MyAppConfig {
	private String protocol;
	private String port;
	private String hostname;
	private MyAppUserDetails adminDetails;

	public MyAppConfig(String protocol, String hostname, String port, MyAppUserDetails adminDetails) {
		this.protocol = protocol;
		this.port = port;
		this.hostname = hostname;
		this.adminDetails = adminDetails;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getHostname() {
		return hostname;
	}

	public String getPort() {
		return port;
	}

	public MyAppUserDetails getAdminDetails() {
		return adminDetails;
	}
}
