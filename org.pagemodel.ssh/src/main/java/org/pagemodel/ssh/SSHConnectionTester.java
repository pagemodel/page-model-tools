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
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.LocalSourceFile;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.ThrowingFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URI;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class SSHConnectionTester<R> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	protected SSHTester<R> sshTester;
	private TestEvaluator testEvaluator;

	public SSHConnectionTester(R returnObj, SSHTestContext testContext, TestEvaluator testEvaluator) {
		sshTester = new SSHTester<>(returnObj, testContext, testEvaluator);
		this.testEvaluator = testEvaluator;
	}

	public SSHTestContext getContext(){
		return sshTester.testContext;
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	public SSHTester<R> connect() {
		return sshTester.connect();
	}

	public SSHTester<R> connect(SSHAuthenticator authenticator) {
		return sshTester.connect(authenticator);
	}

	public SSHTester<R> connect(String ipAddress, String username, String password, String sudoPassword) {
		return sshTester.connect(SSHAuthenticator.passwordAuth(ipAddress, username, password, sudoPassword));
	}

	public R testConnectionFails() {
		return sshTester.testConnectionFails();
	}

	public R testConnectionFails(String ipAddress, String username, String password) {
		return sshTester.testConnectionFails(SSHAuthenticator.passwordAuth(ipAddress, username, password, password));
	}

	public R testConnectionFails(SSHAuthenticator authenticator) {
		return sshTester.testConnectionFails(authenticator);
	}

	public SSHConnectionTester<R> scpFileUpload(String resourceFile, String remotePath) {
		return scpFileUpload(sshTester.getAuthenticator(), resourceFile, remotePath);
	}

	public SSHConnectionTester<R> scpFileDownload(String remotePath, String localPath) {
		return scpFileDownload(sshTester.getAuthenticator(), remotePath, localPath);
	}

	public SSHConnectionTester<R> scpFileUpload(SSHAuthenticator authenticator, String resourceFilePath, String remotePath) {
		log.info("Transferring file: Local Path [" + resourceFilePath + "], Remote Path [" + remotePath + "], " +
				"Host [" + authenticator.getHost() + "], Username [" + authenticator.getUsername() + "], Password [" + authenticator.getPassword() + "]");
		try {
			SSHClient ssh = authenticator.connectAndAuthenticate();
			URI uri = this.getClass().getResource(resourceFilePath).toURI();
			LocalSourceFile file = new FileSystemFile(new File(uri));
			ssh.newSCPFileTransfer().upload(file, remotePath);
			ssh.disconnect();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return this;
	}

	public SSHConnectionTester<R> scpFileDownload(SSHAuthenticator authenticator, String remotePath, String localPath) {
		log.info("Transferring file: Remote Path [" + remotePath + "], Remote Path [" + localPath + "], " +
				"Host [" + authenticator.getHost() + "], Username [" + authenticator.getUsername() + "], Password [" + authenticator.getPassword() + "]");
		try {
			SSHClient ssh = authenticator.connectAndAuthenticate();
			ssh.newSCPFileTransfer().download(remotePath, localPath);
			ssh.disconnect();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return this;
	}

	public R doAction(ThrowingFunction<? super SSHConnectionTester<R>, R, ?> sshAction) {
		return ThrowingFunction.unchecked(sshAction).apply(this);
	}
}
