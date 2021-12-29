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

import org.pagemodel.core.DefaultTestContext;
import org.pagemodel.core.TestContext;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public interface SSHTestContext extends TestContext {
	public SSHAuthenticator getSshAuthenticator();
	public void setSshAuthenticator(SSHAuthenticator sshAuthenticator);

	public static class DefaultSSHTestContext extends DefaultTestContext implements SSHTestContext {
		protected SSHAuthenticator sshAuthenticator;

		public DefaultSSHTestContext(SSHAuthenticator sshAuthenticator) {
			super();
			this.sshAuthenticator = sshAuthenticator;
		}

		public SSHAuthenticator getSshAuthenticator() {
			return sshAuthenticator;
		}

		public void setSshAuthenticator(SSHAuthenticator sshAuthenticator) {
			this.sshAuthenticator = sshAuthenticator;
		}
	}
}
