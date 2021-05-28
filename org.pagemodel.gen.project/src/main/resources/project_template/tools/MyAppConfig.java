package org.pagemodel.tests.myapp.tools;

import org.pagemodel.ssh.SSHAuthenticator;

public class MyAppConfig {
	private String protocol;
	private String port;
	private String hostname;
	String ipAddress;
	private MyAppUserDetails adminDetails;
	private SSHAuthenticator sshAuth;

	public MyAppConfig() {	}

	public MyAppConfig(String protocol, String hostname, String ipAddress, String port, MyAppUserDetails adminDetails, SSHAuthenticator sshAuth) {
		this.protocol = protocol;
		this.port = port;
		this.hostname = hostname;
		this.ipAddress = ipAddress;
		this.adminDetails = adminDetails;
		this.sshAuth = sshAuth;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public MyAppUserDetails getAdminDetails() {
		return adminDetails;
	}

	public void setAdminDetails(MyAppUserDetails adminDetails) {
		this.adminDetails = adminDetails;
	}

	public SSHAuthenticator getSshAuth() {
		return sshAuth;
	}

	public void setSshAuth(SSHAuthenticator sshAuth) {
		this.sshAuth = sshAuth;
	}
}
