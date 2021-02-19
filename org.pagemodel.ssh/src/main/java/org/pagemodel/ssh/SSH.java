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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class SSH {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static SSHInner testSSH(SSHTestContext testContext) {
		return new SSHInner(testContext);
	}

	public static SSHInner testSSH(SSHAuthenticator authenticator) {
		SSHTestContext testContext = new SSHTestContext.DefaultSSHTestContext(authenticator);
		return new SSHInner(testContext);
	}

	public static class SSHInner extends SSHConnectionTester<SSHInner> {
		public SSHInner(SSHTestContext testContext) {
			super(null, testContext);
			this.sshTester = new SSHTester<>(this, testContext);
		}
	}
}
