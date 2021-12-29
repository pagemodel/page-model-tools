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

package org.pagemodel.ssh;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

import java.io.File;
import java.io.IOException;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class SSHAuthenticator {
	private String ipAddress;
	private String username;
	private String password;
	private String keyFilePath;
	private String keyFilePassword;
	private String sudoPassword;

	public static SSHAuthenticator passwordAuth(String ipAddress, String username, String password, String sudoPassword) {
		return new SSHAuthenticator(ipAddress, username, password, null, null, sudoPassword);
	}

	public static SSHAuthenticator keyAuth(String ipAddress, String username, String keyFilePath, String keyFilePassword, String sudoPassword) {
		return new SSHAuthenticator(ipAddress, username, null, keyFilePath, keyFilePassword, sudoPassword);
	}

	public SSHAuthenticator(String ipAddress, String username, String password, String keyFilePath, String keyFilePassword, String sudoPassword) {
		this.ipAddress = ipAddress;
		this.username = username;
		this.password = password;
		this.keyFilePath = keyFilePath;
		this.keyFilePassword = keyFilePassword;
		this.sudoPassword = sudoPassword;
	}

	public SSHClient connectAndAuthenticate() throws IOException {
		if(keyFilePath != null && new File(keyFilePath).exists()) {
			return connectAndAuthenticateKeyFile();
		}else {
			return connectAndAuthenticatePassword();
		}
	}

	public SSHClient connectAndAuthenticatePassword() throws IOException {
		SSHClient ssh = new SSHClient();
		ssh.addHostKeyVerifier(new PromiscuousVerifier());
		ssh.connect(ipAddress);
		ssh.authPassword(username, password);
		return ssh;
	}

	public SSHClient connectAndAuthenticateKeyFile() throws IOException {
		SSHClient ssh = new SSHClient();
		ssh.addHostKeyVerifier(new PromiscuousVerifier());
		ssh.connect(ipAddress);
		KeyProvider kp;
		if (keyFilePassword == null) {
			kp = ssh.loadKeys(keyFilePath);
		} else {
			kp = ssh.loadKeys(keyFilePath, keyFilePassword);
		}
		ssh.authPublickey(username, kp);
		return ssh;
	}

	public String getHost() {
		return ipAddress;
	}

	public void setHost(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getKeyFilePath() {
		return keyFilePath;
	}

	public void setKeyFilePath(String keyFilePath) {
		this.keyFilePath = keyFilePath;
	}

	public String getKeyFilePassword() {
		return keyFilePassword;
	}

	public void setKeyFilePassword(String keyFilePassword) {
		this.keyFilePassword = keyFilePassword;
	}

	public String getSudoPassword() {
		return sudoPassword;
	}

	public void setSudoPassword(String sudoPassword) {
		this.sudoPassword = sudoPassword;
	}
}
