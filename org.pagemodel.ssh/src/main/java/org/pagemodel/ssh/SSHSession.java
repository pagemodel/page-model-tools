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
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Shell;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.Result;
import net.sf.expectit.matcher.Matcher;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.filter.Filters.removeColors;
import static net.sf.expectit.filter.Filters.removeNonPrintable;
import static net.sf.expectit.matcher.Matchers.*;

/**
 * @author Sean Hale <shale@tetrazoid.net>
 */
public class SSHSession {
	private static final String sudoCommand = "sudo -i";
	private static final String sudoPasswordPrompt = "[sudo] password for ";
	private static final String defaultPromptRegex = "\r\n(-?bash-[^ ]*|\\[[^\\]]*\\])(\\$|\\#) $";

	private Expect commandLine;
	private SSHClient client;
	private Session session;
	private Shell shell;
	private SSHAuthenticator authenticator;
	private boolean sudoed;
	private String promptRegex;

	public SSHSession(SSHAuthenticator authenticator) {
		this(authenticator, defaultPromptRegex);
	}

	public SSHSession(SSHAuthenticator authenticator, String promptRegex) {
		this.authenticator = authenticator;
		this.sudoed = false;
		this.promptRegex = promptRegex;
	}

	public String runCommandAndGetOutput(String command, int timeoutInSeconds) throws IOException {
		commandLine.sendLine(command);
		try {
			String output = waitForPrompt(timeoutInSeconds).getBefore().trim();
			String unwrappedOutput = undoLineWrapping(command, output);
			return unwrappedOutput.substring(command.length()).trim();
		} catch (IOException ex) {
			commandLine.sendBytes(new byte[]{3});
			commandLine.sendLine("kill 0");
			close();
			throw new RuntimeException("Command [" + command + "] failed to exit after [" + timeoutInSeconds + "] seconds.");
		}
	}

	public void sendInput(String input) throws IOException {
		commandLine.send(input);
	}

	public void sudoToRoot(String rootPassword, String sudoOptions) throws IOException {
		String cmd = sudoCommand;
		if (sudoOptions != null && !sudoOptions.isEmpty()) {
			cmd = cmd + " " + sudoOptions;
		}
		sendLineAndExpectOutput(cmd, contains(sudoPasswordPrompt));
		sendLineAndExpectOutput(rootPassword, regexp(promptRegex));
		sudoed = true;
	}

	public void exitRoot() throws IOException {
		if (sudoed) {
			sendLineAndExpectOutput("exit", regexp(promptRegex));
			sudoed = false;
		}
	}

	public void close() throws IOException {
		client.disconnect();
	}

	public void connect() throws IOException, UserAuthException, TransportException, ConnectionException {
		client = authenticator.connectAndAuthenticate();
		session = client.startSession();
		session.allocateDefaultPTY();
		shell = session.startShell();
		commandLine = new ExpectBuilder()
				.withOutput(shell.getOutputStream())
				.withInputs(shell.getInputStream(), shell.getErrorStream())
				.withEchoInput(System.out)
				.withInputFilters(removeColors(), removeNonPrintable())
				.withExceptionOnFailure()
				.withTimeout(5, TimeUnit.SECONDS)
				.build();
		commandLine.expect(times(1, regexp(promptRegex)));
	}

	private void sendLineAndExpectOutput(String lineString, Matcher<Result> expectedOutput) throws IOException {
		commandLine.sendLine(lineString);
		commandLine.expect(expectedOutput);
	}

	private Result waitForPrompt(int timeoutInSeconds) throws IOException {
		int timeoutInMs = timeoutInSeconds * 1000;
		return commandLine.withTimeout(timeoutInMs, TimeUnit.MILLISECONDS).expect(regexp(promptRegex));
	}

	private String undoLineWrapping(String command, String output) {
		//Unwraps the line if the line is over a certain character limit (I think it's 55)
		String cmdFromOutput = output.substring(0, command.length());
		while (!cmdFromOutput.equals(command)) {
			output = output.replaceFirst(" \r", "");
			cmdFromOutput = output.substring(0, command.length());
		}
		return output;
	}

	public void sendCtrlC() throws IOException {
		commandLine.sendBytes(new byte[]{3});
		waitForPrompt(10);
	}
}
