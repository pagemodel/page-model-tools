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

import org.pagemodel.core.testers.TestEvaluator;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class SSH {

	public static SSHInner testSSH(SSHTestContext testContext) {
		return new SSHInner(testContext, testContext.getEvaluator());
	}

	public static SSHInner testSSH(SSHAuthenticator authenticator) {
		SSHTestContext testContext = new SSHTestContext.DefaultSSHTestContext(authenticator);
		return new SSHInner(testContext, testContext.getEvaluator());
	}

	public static class SSHInner extends SSHConnectionTester<SSHInner> {
		public SSHInner(SSHTestContext testContext, TestEvaluator testEvaluator) {
			super(null, testContext, testEvaluator);
			this.sshTester = new SSHTester<>(this, testContext, testEvaluator);
		}
	}
}
