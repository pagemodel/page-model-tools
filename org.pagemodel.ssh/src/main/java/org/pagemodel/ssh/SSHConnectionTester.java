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

import java.io.File;
import java.net.URI;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class SSHConnectionTester<R> {
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

	public R scpFileUpload(String resourceFile, String remotePath) {
		return scpFileUpload(sshTester.getAuthenticator(), resourceFile, remotePath);
	}

	public R scpFileDownload(String remotePath, String localPath) {
		return scpFileDownload(sshTester.getAuthenticator(), remotePath, localPath);
	}

	public R scpFileUpload(SSHAuthenticator authenticator, String resourceFilePath, String remotePath) {
		return getEvaluator().testExecute("scp upload", op -> op
				.addValue("local", resourceFilePath)
				.addValue("remote", remotePath)
				.addValue("server", authenticator.getHost())
				.addValue("user", authenticator.getUsername()),
				() -> {
					SSHClient ssh = authenticator.connectAndAuthenticate();
					URI uri = this.getClass().getResource(resourceFilePath).toURI();
					LocalSourceFile file = new FileSystemFile(new File(uri));
					ssh.newSCPFileTransfer().upload(file, remotePath);
					ssh.disconnect();
				},
				this.sshTester.getReturnObj(), getContext());
	}

	public R testScpUploadFails(SSHAuthenticator authenticator, String resourceFilePath, String remotePath) {
		return getEvaluator().testCondition("scp upload fails", op -> op
						.addValue("local", resourceFilePath)
						.addValue("remote", remotePath)
						.addValue("server", authenticator.getHost())
						.addValue("user", authenticator.getUsername()),
				() -> {
					try {
						SSHClient ssh = authenticator.connectAndAuthenticate();
						URI uri = this.getClass().getResource(resourceFilePath).toURI();
						LocalSourceFile file = new FileSystemFile(new File(uri));
						ssh.newSCPFileTransfer().upload(file, remotePath);
						ssh.disconnect();
					} catch (RuntimeException ex) {
						return true;
					}
					return false;
				},
				this.sshTester.getReturnObj(), getContext());
	}

	public R scpFileDownload(SSHAuthenticator authenticator, String remotePath, String localPath) {
		return getEvaluator().testExecute("scp download", op -> op
						.addValue("remote", remotePath)
						.addValue("local", localPath)
						.addValue("server", authenticator.getHost())
						.addValue("user", authenticator.getUsername()),
				() -> {
					SSHClient ssh = authenticator.connectAndAuthenticate();
					ssh.newSCPFileTransfer().download(remotePath, localPath);
					ssh.disconnect();
				},
				this.sshTester.getReturnObj(), getContext());
	}

	public R testScpDownloadFails(SSHAuthenticator authenticator, String remotePath, String localPath) {
		return getEvaluator().testCondition("scp download fails", op -> op
						.addValue("remote", remotePath)
						.addValue("local", localPath)
						.addValue("server", authenticator.getHost())
						.addValue("user", authenticator.getUsername()),
				() -> {
					try {
						SSHClient ssh = authenticator.connectAndAuthenticate();
						ssh.newSCPFileTransfer().download(remotePath, localPath);
						ssh.disconnect();
					} catch (RuntimeException ex) {
						return true;
					}
					return false;
				},
				this.sshTester.getReturnObj(), getContext());
	}

	public R doAction(ThrowingFunction<? super SSHConnectionTester<R>, R, ?> sshAction) {
		return ThrowingFunction.unchecked(sshAction).apply(this);
	}
}
