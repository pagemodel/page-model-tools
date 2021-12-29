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

package org.pagemodel.tests.myapp.tools;

import org.pagemodel.ssh.SSHAuthenticator;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
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
