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
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.Unique;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class MailMessageBuilder<R> {
	protected MailMessage mailMessage;
	protected TestContext testContext;
	protected R returnObj;
	private TestEvaluator testEvaluator;

	public MailMessageBuilder(R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
		this.mailMessage = new MailMessage();
		this.testContext = testContext;
		this.returnObj = returnObj;
		this.testEvaluator = testEvaluator;
		mailMessage.setSubject(Unique.string("MailMessage subject"));
		mailMessage.setBody(Unique.string("MailMessage body"));
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	public MailMessage getMailMessage(){
		return mailMessage;
	}

	public MailTester.SentMailTester<R> send(SmtpServer smtpServer){
		MailMessage[] refMail = new MailMessage[1];
		return getEvaluator().testRun(
				TestEvaluator.TEST_EXECUTE,
				"send mail", op -> op
						.addValue("mail", getMailMessage().toJson()),
				() -> refMail[0] = smtpServer.send(getMailMessage()),
				new MailTester.SentMailTester<>(() -> refMail[0], returnObj, testContext, getEvaluator()),
				testContext);
	}

	public R store(String key){
		testContext.store(key, getMailMessage());
		return returnObj;
	}

	public MailTester.SentMailTester<R> sendAndStore(String key, SmtpServer smtpServer){
		store(key);
		return send(smtpServer);
	}

	public MailMessageBuilder<R> from(String address){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set from", op -> op
						.addValue("value", address),
				() -> mailMessage.setSender(address),
				this, testContext);
	}

	public MailMessageBuilder<R> to(String...addresses){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set to", op -> op
						.addValue("value", Arrays.toString(addresses)),
				() -> mailMessage.addRecipientsTo(addresses),
				this, testContext);
	}

	public MailMessageBuilder<R> cc(String...addresses){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set cc", op -> op
						.addValue("value", Arrays.toString(addresses)),
				() -> mailMessage.addRecipientsCc(addresses),
				this, testContext);
	}

	public MailMessageBuilder<R> bcc(String...addresses){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set bcc", op -> op
						.addValue("value", Arrays.toString(addresses)),
				() -> mailMessage.addRecipientsBcc(addresses),
				this, testContext);
	}

	public MailMessageBuilder<R> clearRecipients(){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"clear recipients", op -> op
						.addValue("actual", getMailMessage().getRecipientsAll()),
				() -> {
					mailMessage.recipientsTo.clear();
					mailMessage.recipientsCc.clear();
					mailMessage.recipientsBcc.clear();
				}, this, testContext);
	}


	public MailMessageBuilder<R> tagSubject(){
		return subject(mailMessage.getSubject());
	}

	public MailMessageBuilder<R> subjectUntagged(String subject){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set subject", op -> op
						.addValue("value", subject),
				() -> mailMessage.setSubject(subject),
				this, testContext);
	}

	public MailMessageBuilder<R> subject(String subject){
		return subjectUntagged(Unique.string(subject));
	}

	public MailMessageBuilder<R> bodyUntagged(String content){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set body", op -> op
						.addValue("value", content),
				() -> mailMessage.setBody(content),
				this, testContext);
	}

	public MailMessageBuilder<R> body(String content){
		return bodyUntagged(Unique.string(content));
	}

	public MailMessageBuilder<R> htmlBodyUntagged(String content){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set html body", op -> op
						.addValue("value", content),
				() -> mailMessage.setHtmlBody(content),
				this, testContext);
	}

	public MailMessageBuilder<R> htmlBody(String content){
		return htmlBodyUntagged(Unique.string(content));
	}

	public MailMessageBuilder<R> textBodyUntagged(String content){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set text body", op -> op
						.addValue("value", content),
				() -> mailMessage.setTextBody(content),
				this, testContext);
	}

	public MailMessageBuilder<R> textBody(String content){
		return textBodyUntagged(Unique.string(content));
	}

	public MailMessageBuilder<R> header(String headerName, String value){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set header", op -> op
						.addValue("name", headerName)
						.addValue("value", value),
				() -> mailMessage.setHeader(headerName, value),
				this, testContext);
	}

	public MailMessageBuilder<R> readMime(File file) {
		String path = file == null ? null : file.getAbsolutePath();
		return getEvaluator().testRun(
				TestEvaluator.TEST_LOAD,
				"read MIME", op -> op
						.addValue("value", path),
				() -> {
					try {
						MimeMessage mime = new MimeMessage(null, new FileInputStream(file));
						LazyMailMessage mail = new LazyMailMessage(mime);
						mail.loadAll();
						this.mailMessage = mail;
					} catch (Exception ex) {
						throw testContext.createException("Unable to read MIME from file:[" + path + "]");
					}
				}, this, testContext);
	}

	public MailMessageBuilder<R> writeMime(File file) {
		String path = file == null ? null : file.getAbsolutePath();
		return getEvaluator().testRun(
				TestEvaluator.TEST_STORE,
				"write MIME", op -> op
						.addValue("value", path),
				() -> {
					try {
						MimeMessage mime = MimeMailAdapter.createMimeMessage(mailMessage, null);
						mime.writeTo(new FileOutputStream(file));
					} catch (Exception ex) {
						throw testContext.createException("Unable to write MIME to file:[" + path + "]");
					}
				}, this, testContext);
	}

	public MailMessageBuilder<R> withAttachment(File contents, String contentType) {
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"add attachment", op -> op
						.addValue("contentType", contentType)
						.addValue("value", contents.getAbsolutePath()),
				() -> mailMessage.addAttachment(contents, contentType),
				this, testContext);
	}

	public MailMessageBuilder<R> withAttachment(String filename, String contents, String contentType) {
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"add attachment", op -> op
						.addValue("value", contents)
						.addValue("filename",filename)
						.addValue("contentType",contentType),
				() -> mailMessage.addAttachment(filename, contents, contentType),
				this, testContext);
	}

	public MailMessageBuilder<R> withAttachment(String filename, File contents, String contentType) {
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"add attachment", op -> op
						.addValue("value", contents.getAbsolutePath())
						.addValue("filename",filename)
						.addValue("contentType",contentType),
				() -> mailMessage.addAttachment(filename, contents, contentType),
				this, testContext);
	}

	public MailMessageBuilder<R> withAttachment(String filename, byte[] contents, String contentType) {
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"add attachment", op -> op
						.addValue("value", "contentSize: " + contents.length)
						.addValue("filename", filename)
						.addValue("contentType", contentType),
				() -> mailMessage.addAttachment(filename, contents, contentType),
				this, testContext);
	}

	public MailMessageBuilder<R> withAttachment() {
		String ts = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
		return withAttachment("file-" + Unique.shortString() + ".txt", "Body: " + ts + ", msg-" + Unique.shortString());
	}

	public MailMessageBuilder<R> withAttachment(String filename, String contents) {
		return withAttachment(filename, contents, "text/plain");
	}
}
