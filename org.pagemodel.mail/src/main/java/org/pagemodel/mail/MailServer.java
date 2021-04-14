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

import org.pagemodel.core.utils.Unique;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.lang.invoke.MethodHandles;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class MailServer {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private String domain;
	private String host;
	private String username;
	private String password;
	private Boolean useTls;
	private Boolean allowInsecure;

	public MailServer(MailAuthenticator mailAuthenticator) {
		this(mailAuthenticator.getDomain(), mailAuthenticator.getHost(), mailAuthenticator.getUsername(), mailAuthenticator.getPassword(), mailAuthenticator.getUseTls(), mailAuthenticator.getAllowInsecure());
	}

	public MailServer(String domain, String host, String username, String password, boolean useTls, boolean allowInsecure){
		this(domain, host, username, password, useTls);
		this.allowInsecure = allowInsecure;

	}
	public MailServer(String domain, String host, String username, String password, boolean useTls) {
		this.domain = domain;
		this.host = host;
		this.username = username;
		this.password = password;
		this.useTls = useTls;
	}

	public boolean getAllowInsecure() { return allowInsecure;}

	public void setAllowInsecure(boolean allowInsecure){this.allowInsecure = allowInsecure; }

	public boolean getUseTls() { return useTls;}

	public void setUseTls(boolean useTls){ this.useTls = useTls; }

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


	public String generateRandomEmailAddress() {
		return createAddress(Unique.shortString());
	}

	public String createAddress(String localPart) {
		if (getDomain() == null) {
			throw new NullPointerException("Null mail domain for " + getHost());
		}
		String email = localPart + "@" + getDomain();
		try {
			return new InternetAddress(email).toString();
		} catch (AddressException e) {
			throw new RuntimeException(e);
		}
	}
}
