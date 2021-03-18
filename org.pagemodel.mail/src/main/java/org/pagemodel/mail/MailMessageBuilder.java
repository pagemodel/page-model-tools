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

import org.pagemodel.core.TestContext;
import org.pagemodel.core.utils.Unique;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class MailMessageBuilder<R> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected MailMessage mailMessage;
	protected TestContext testContext;
	protected R returnObj;

	public MailMessageBuilder(TestContext testContext, R returnObj) {
		this.mailMessage = new MailMessage();
		this.testContext = testContext;
		this.returnObj = returnObj;
		subject("MailMessage subject");
		body("MailMessage body");
	}

	public MailMessage getMailMessage(){
		return mailMessage;
	}

	public MailTester.SentMailTester<R> send(SmtpServer smtpServer){
		try {
			MailMessage sent = smtpServer.send(getMailMessage());
			return new MailTester.SentMailTester<>(testContext, returnObj, () -> sent);
		}catch (Exception ex){
			throw new RuntimeException("Error: unable to send email [" + mailMessage.getSubject() + "], from:[" + mailMessage.getSender() + "], to:" + Arrays.toString(mailMessage.getRecipientsTo().toArray(new String[0])), ex);
		}
	}

	public R store(String key){
		try {
			testContext.store(key, getMailMessage());
			return returnObj;
		}catch (Exception ex){
			throw new RuntimeException("Error: unable to store email [" + mailMessage.getSubject() + "], from:[" + mailMessage.getSender() + "], to:" + Arrays.toString(mailMessage.getRecipientsTo().toArray(new String[0])), ex);
		}
	}

	public MailTester.SentMailTester<R> sendAndStore(String key, SmtpServer smtpServer){
		store(key);
		return send(smtpServer);
	}

	public MailMessageBuilder<R> from(String address){
		return logAndRun("Set [from] address [" + address + "]",
				() -> mailMessage.setSender(address));
	}

	public MailMessageBuilder<R> to(String...addresses){
		return logAndRun("Add [to] addresses " + Arrays.toString(addresses),
				() -> mailMessage.addRecipientsTo(addresses));
	}

	public MailMessageBuilder<R> cc(String...addresses){
		return logAndRun("Add [cc] addresses " + Arrays.toString(addresses),
				() -> mailMessage.addRecipientsCc(addresses));
	}

	public MailMessageBuilder<R> bcc(String...addresses){
		return logAndRun("Add [bcc] addresses " + Arrays.toString(addresses),
				() -> mailMessage.addRecipientsBcc(addresses));
	}

	public MailMessageBuilder<R> clearRecipients(){
		return logAndRun("Clearing all recipients",
				() -> {
					mailMessage.recipientsTo.clear();
					mailMessage.recipientsCc.clear();
					mailMessage.recipientsBcc.clear();
				});
	}


	public MailMessageBuilder<R> tagSubject(){
		return subject(mailMessage.getSubject());
	}

	public MailMessageBuilder<R> subjectUntagged(String subject){
		return logAndRun("Set subject [" + subject + "]",
				() -> mailMessage.setSubject(subject));
	}

	public MailMessageBuilder<R> subject(String subject){
		return subjectUntagged(Unique.string(subject));
	}

	public MailMessageBuilder<R> bodyUntagged(String content){
		return logAndRun("Set body [" + content + "]",
				() -> mailMessage.setBody(content));
	}

	public MailMessageBuilder<R> body(String content){
		return bodyUntagged(Unique.string(content));
	}

	public MailMessageBuilder<R> header(String headerName, String value){
		return logAndRun("Add header, name [" + headerName + "],  value [" + value + "]",
				() -> mailMessage.setHeader(headerName, value));
	}

	public MailMessageBuilder<R> withAttachment() {
		String ts = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
		return withAttachment("file-" + Unique.shortString() + ".txt", "Body: " + ts + ", msg-" + Unique.shortString());
	}

	public MailMessageBuilder<R> withAttachment(File contents, String contentType) {
		return logAndRun("Add attachment with file path [" + contents.getAbsolutePath() + "], and content type [" + contentType + "]",
				() -> mailMessage.addAttachment(contents, contentType));
	}

	public MailMessageBuilder<R> withAttachment(String filename, String contents) {
		return withAttachment(filename, contents, "text/plain");
	}

	public MailMessageBuilder<R> withAttachment(String filename, String contents, String contentType) {
		return logAndRun("Add attachment with filename [" + filename + "], contents [" + contents + "], and content type [" + contentType + "]",
				() -> mailMessage.addAttachment(filename, contents, contentType));
	}

	public MailMessageBuilder<R> withAttachment(String filename, File contents, String contentType) {
		return logAndRun("Add attachment with filename [" + filename + "], contents [" + contents.getAbsolutePath() + "], and content type [" + contentType + "]",
				() -> mailMessage.addAttachment(filename, contents, contentType));
	}

	public MailMessageBuilder<R> withAttachment(String filename, byte[] contents, String contentType) {
		return logAndRun("Add attachment with filename [" + filename + "], contents size [" + contents.length + "], and content type [" + contentType + "]",
				() -> mailMessage.addAttachment(filename, contents, contentType));
	}

	private MailMessageBuilder<R> logAndRun(String message, Runnable runnable){
		log.info(message);
		runnable.run();
		return this;
	}
}
