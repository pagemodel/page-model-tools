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

import org.pagemodel.core.testers.ComparableTester;
import org.pagemodel.core.testers.StringTester;
import org.pagemodel.core.testers.TestEvaluator;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 * @author Sean Hale <shale@tetrazoid.net>
 */
public class SSHCommandTester<R> {

	private final String command;
	private Integer timeoutSec;
	private final SSHTester<?> parent;
	private String output;
	private Integer returnCode;
	protected R returnObj;
	protected final SSHTestContext testContext;
	private TestEvaluator testEvaluator;

	private boolean executed = false;

	public SSHCommandTester(String command, Integer timeoutSec, SSHTester<?> parent, R returnObj, SSHTestContext testContext, TestEvaluator testEvaluator) {
		this.command = command;
		this.timeoutSec = timeoutSec;
		this.parent = parent;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	public ComparableTester<Integer, SSHCommandTester<R>> testReturnCode() {
		if (!executed) {
			executeCommand();
		}
		if (returnCode == null) {
			SSHCommandTester<R> returnCodeCmd = new SSHCommandTester<>("echo $?", timeoutSec, parent, returnObj, testContext, getEvaluator());
			returnCodeCmd.executeCommand();
			String rcString = returnCodeCmd.output;
			returnCode = Integer.valueOf(rcString);
		}
		return new ComparableTester<>(() -> returnCode, this, testContext, getEvaluator());
	}

	public StringTester<SSHCommandTester<R>> testOutput() {
		if (!executed) {
			executeCommand();
		}
		return new StringTester<>(() -> output, this, testContext, getEvaluator());
	}

	public SSHCommandTester<R> runCommand(String command, int timeoutSec) {
		if (!executed) {
			executeCommand();
		}
		return new SSHCommandTester<>(command, timeoutSec, parent, returnObj, testContext, getEvaluator());
	}

	public SSHCommandTester<R> runCommand(String command) {
		if (!executed) {
			executeCommand();
		}
		return new SSHCommandTester<>(command, SSHTester.DEFAULT_SSH_CMD_TIMEOUT, parent, returnObj, testContext, getEvaluator());
	}

	public SSHCommandTester<R> sudoToRoot() {
		return sudoToRoot("");
	}

	public SSHCommandTester<R> sudoToRoot(String sudoOpts) {
		if (!executed) {
			executeCommand();
		}
		return new SSHCommandTester<>("sudo-root:" + sudoOpts + ":" + parent.getAuthenticator().getSudoPassword(), SSHTester.DEFAULT_SSH_CMD_TIMEOUT, parent, returnObj, testContext, getEvaluator());
	}

	public SSHCommandTester<R> exitSudo() {
		if (!executed) {
			executeCommand();
		}
		return new SSHCommandTester<>("exit-sudo", SSHTester.DEFAULT_SSH_CMD_TIMEOUT, parent, returnObj, testContext, getEvaluator());
	}

	public R disconnect() {
		if (!executed) {
			executeCommand();
		}
		parent.disconnect();
		return returnObj;
	}

	private void executeCommand() {
		if (command.startsWith("sudo-root")) {
			getEvaluator().testExecute("ssh sudo", op -> op
							.addValue("server", parent.getAuthenticator().getHost()),
					() -> {
						String cmdLine = command.substring(10);
						int split = cmdLine.indexOf(':');
						String opts = cmdLine.substring(0, split);
						String password = cmdLine.substring(split + 1);
						parent.getSshSession().sudoToRoot(password, opts);
						output = "";
					},
					null, testContext);
		} else if (command.startsWith("exit-sudo")) {
			getEvaluator().testExecute("ssh exit sudo", op -> op
							.addValue("server", parent.getAuthenticator().getHost()),
					() -> {
						parent.getSshSession().exitRoot();
						output = "";
					},
					null, testContext);
		} else {
			getEvaluator().testExecute("ssh command", op -> op
					.addValue("command",command)
					.addValue("server", parent.getAuthenticator().getHost()),
					() -> {
						output = parent.getSshSession().runCommandAndGetOutput(command, timeoutSec);
					},
					null, testContext);
		}
		executed = true;
	}
}
