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

package org.pagemodel.mail;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class MailAuthenticator {
	private String domain;
	private String host;
	private String username;
	private String password;
	private int popPort;
	private int smtpPort;
	private boolean useTls;
	private boolean allowInsecure;

	public MailAuthenticator(String domain, String host, String username, String password, int popPort, int smtpPort){
		this(domain, host, username, password, popPort, smtpPort, false);
	}

	public MailAuthenticator(String domain, String host, String username, String password, int popPort, int smtpPort, boolean useTls){
		this(domain, host, username, password, popPort, smtpPort, useTls, false);
	}

	public MailAuthenticator(String domain, String host, String username, String password, int popPort, int smtpPort, boolean useTls, boolean allowInsecure) {
		this.domain = domain;
		this.host = host;
		this.username = username;
		this.password = password;
		this.popPort = popPort;
		this.smtpPort = smtpPort;
		this.useTls = useTls;
		this.allowInsecure = allowInsecure;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getHost() {
		return host;
	}

	protected void setHost(String host) {
		this.host = host;
	}

	public String getUsername() {
		return username;
	}

	protected void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	protected void setPassword(String password) {
		this.password = password;
	}

	public Integer getPopPort() {
		return popPort;
	}

	public void setPopPort(Integer popPort) {
		this.popPort = popPort;
	}

	public Integer getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(Integer smtpPort) {
		this.smtpPort = smtpPort;
	}

	public boolean getUseTls() {
		return useTls;
	}

	public void setUseTls(boolean useTls) {
		this.useTls = useTls;
	}

	public boolean getAllowInsecure() {return allowInsecure;}

	public void setAllowInsecure(boolean allowInsecure)  {this.allowInsecure = allowInsecure;}
}
