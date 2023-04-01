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
/**
 * A MailMessageBuilder provides a fluent interface for building a MailMessage object.
 * It wraps a javax.mail.Message object and gives access to all message properties.
 * The builder can be used to set the sender, recipients, subject, body, headers, and attachments of the message.
 * It also provides methods for reading and writing MIME messages from/to files.
 * The builder can be used to send the message using a specified SMTP server, or to store the message in a TestContext for later use.
 * @param <R> the type of the return object
 */
public class MailMessageBuilder<R> {
	protected MailMessage mailMessage;
	protected TestContext testContext;
	protected R returnObj;
	private TestEvaluator testEvaluator;

	/**
	 * Constructs a new MailMessageBuilder with the given return object, test context, and test evaluator.
	 * Initializes the MailMessage with a unique subject and body.
	 * @param returnObj the return object
	 * @param testContext the test context
	 * @param testEvaluator the test evaluator
	 */
	public MailMessageBuilder(R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
		this.mailMessage = new MailMessage();
		this.testContext = testContext;
		this.returnObj = returnObj;
		this.testEvaluator = testEvaluator;
		mailMessage.setSubject(Unique.string("MailMessage subject"));
		mailMessage.setBody(Unique.string("MailMessage body"));
	}

	/**
	 * Returns the test evaluator associated with this builder.
	 * @return the test evaluator
	 */
	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	/**
	 * Returns the MailMessage being built by this builder.
	 * @return the MailMessage
	 */
	public MailMessage getMailMessage(){
		return mailMessage;
	}

	/**
	 * Sends the MailMessage using the specified SMTP server and returns a SentMailTester for testing the sent message.
	 * @param smtpServer the SMTP server to use for sending the message
	 * @return a SentMailTester for testing the sent message
	 */
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

	/**
	 * Stores the MailMessage in the TestContext with the specified key and returns the return object.
	 * @param key the key to use for storing the message in the TestContext
	 * @return the return object
	 */
	public R store(String key){
		testContext.store(key, getMailMessage());
		return returnObj;
	}

	/**
	 * Sends the MailMessage using the specified SMTP server, stores the message in the TestContext with the specified key,
	 * and returns a SentMailTester for testing the sent message.
	 * @param key the key to use for storing the message in the TestContext
	 * @param smtpServer the SMTP server to use for sending the message
	 * @return a SentMailTester for testing the sent message
	 */
	public MailTester.SentMailTester<R> sendAndStore(String key, SmtpServer smtpServer){
		store(key);
		return send(smtpServer);
	}

	/**
	 * Sets the sender of the MailMessage to the specified address.
	 * @param address the address of the sender
	 * @return this MailMessageBuilder
	 */
	public MailMessageBuilder<R> from(String address){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set from", op -> op
						.addValue("value", address),
				() -> mailMessage.setSender(address),
				this, testContext);
	}

	/**
	 * Adds the specified addresses to the list of recipients of the MailMessage.
	 * @param addresses the addresses of the recipients
	 * @return this MailMessageBuilder
	 */
	public MailMessageBuilder<R> to(String...addresses){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set to", op -> op
						.addValue("value", Arrays.toString(addresses)),
				() -> mailMessage.addRecipientsTo(addresses),
				this, testContext);
	}

	/**
	 * Adds the specified addresses to the list of CC recipients of the MailMessage.
	 * @param addresses the addresses of the CC recipients
	 * @return this MailMessageBuilder
	 */
	public MailMessageBuilder<R> cc(String...addresses){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set cc", op -> op
						.addValue("value", Arrays.toString(addresses)),
				() -> mailMessage.addRecipientsCc(addresses),
				this, testContext);
	}

	/**
	 * Adds the specified addresses to the list of BCC recipients of the MailMessage.
	 * @param addresses the addresses of the BCC recipients
	 * @return this MailMessageBuilder
	 */
	public MailMessageBuilder<R> bcc(String...addresses){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set bcc", op -> op
						.addValue("value", Arrays.toString(addresses)),
				() -> mailMessage.addRecipientsBcc(addresses),
				this, testContext);
	}

	/**
	 * Clears all recipients (To, CC, and BCC) from the MailMessage.
	 * @return this MailMessageBuilder
	 */
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

	/**
	 * Sets the subject of the MailMessage to a unique string.
	 * @return this MailMessageBuilder
	 */
	public MailMessageBuilder<R> tagSubject(){
		return subject(mailMessage.getSubject());
	}

	/**
	 * Sets the subject of the MailMessage to the specified string.
	 * @param subject the subject of the message
	 * @return this MailMessageBuilder
	 */
	public MailMessageBuilder<R> subjectUntagged(String subject){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set subject", op -> op
						.addValue("value", subject),
				() -> mailMessage.setSubject(subject),
				this, testContext);
	}

	/**
	 * Sets the subject of the MailMessage to a unique string based on the specified subject.
	 * @param subject the subject of the message
	 * @return this MailMessageBuilder
	 */
	public MailMessageBuilder<R> subject(String subject){
		return subjectUntagged(Unique.string(subject));
	}

	/**
	 * Sets the body of the MailMessage to the specified string.
	 * @param content the body of the message
	 * @return this MailMessageBuilder
	 */
	public MailMessageBuilder<R> bodyUntagged(String content){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set body", op -> op
						.addValue("value", content),
				() -> mailMessage.setBody(content),
				this, testContext);
	}

	/**
	 * Sets the body of the MailMessage to a unique string based on the specified content.
	 * @param content the body of the message
	 * @return this MailMessageBuilder
	 */
	public MailMessageBuilder<R> body(String content){
		return bodyUntagged(Unique.string(content));
	}

	/**
	 * Sets the HTML body of the MailMessage to the specified string.
	 * @param content the HTML body of the message
	 * @return this MailMessageBuilder
	 */
	public MailMessageBuilder<R> htmlBodyUntagged(String content){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set html body", op -> op
						.addValue("value", content),
				() -> mailMessage.setHtmlBody(content),
				this, testContext);
	}

	/**
	 * Sets the HTML body of the MailMessage to a unique string based on the specified content.
	 * @param content the HTML body of the message
	 * @return this MailMessageBuilder
	 */
	public MailMessageBuilder<R> htmlBody(String content){
		return htmlBodyUntagged(Unique.string(content));
	}

	/**
	 * Sets the text body of the MailMessage to the specified string.
	 * @param content the text body of the message
	 * @return this MailMessageBuilder
	 */
	public MailMessageBuilder<R> textBodyUntagged(String content){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set text body", op -> op
						.addValue("value", content),
				() -> mailMessage.setTextBody(content),
				this, testContext);
	}

	/**
	 * Sets the text body of the MailMessage to a unique string based on the specified content.
	 * @param content the text body of the message
	 * @return this MailMessageBuilder
	 */
	public MailMessageBuilder<R> textBody(String content){
		return textBodyUntagged(Unique.string(content));
	}

	/**
	 * Sets the specified header of the MailMessage to the specified value.
	 * @param headerName the name of the header
	 * @param value the value of the header
	 * @return this MailMessageBuilder
	 */
	public MailMessageBuilder<R> header(String headerName, String value){
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"set header", op -> op
						.addValue("name", headerName)
						.addValue("value", value),
				() -> mailMessage.setHeader(headerName, value),
				this, testContext);
	}

	/**
	 * Reads a MIME message from the specified file and sets the MailMessage to the contents of the MIME message.
	 * @param file the file containing the MIME message
	 * @return this MailMessageBuilder
	 */
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

	/**
	 * Writes the MailMessage to the specified file in MIME format.
	 * @param file the file to write the MIME message to
	 * @return this MailMessageBuilder
	 */
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

	/**
	 * Adds an attachment to the MailMessage with the contents of the specified file and the specified content type.
	 * @param contents the file containing the attachment contents
	 * @param contentType the content type of the attachment
	 * @return this MailMessageBuilder
	 */
	public MailMessageBuilder<R> withAttachment(File contents, String contentType) {
		return getEvaluator().testRun(
				TestEvaluator.TEST_BUILD,
				"add attachment", op -> op
						.addValue("contentType", contentType)
						.addValue("value", contents.getAbsolutePath()),
				() -> mailMessage.addAttachment(contents, contentType),
				this, testContext);
	}

	/**
	 * Adds an attachment to the MailMessage with the specified filename, contents, and content type.
	 * @param filename the filename of the attachment
	 * @param contents the contents of the attachment
	 * @param contentType the content type of the attachment
	 * @return this MailMessageBuilder
	 */
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

	/**
	 * Adds an attachment to the MailMessage with the specified filename, contents file, and content type.
	 * @param filename the filename of the attachment
	 * @param contents the file containing the attachment contents
	 * @param contentType the content type of the attachment
	 * @return this MailMessageBuilder
	 */
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

	/**
	 * Adds an attachment to the MailMessage with the specified filename, contents byte array, and content type.
	 * @param filename the filename of the attachment
	 * @param contents the byte array containing the attachment contents
	 * @param contentType the content type of the attachment
	 * @return this MailMessageBuilder
	 */
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

	/**
	 * Adds a default attachment to the MailMessage with a unique filename and contents based on the current timestamp and a unique string.
	 * The content type of the attachment is "text/plain".
	 * @return this MailMessageBuilder
	 */
	public MailMessageBuilder<R> withAttachment() {
		String ts = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
		return withAttachment("file-" + Unique.shortString() + ".txt", "Body: " + ts + ", msg-" + Unique.shortString());
	}

	/**
	 * Adds an attachment to the MailMessage with the specified filename and contents.
	 * The content type of the attachment is "text/plain".
	 * @param filename the filename of the attachment
	 * @param contents the contents of the attachment
	 * @return this MailMessageBuilder
	 */
	public MailMessageBuilder<R> withAttachment(String filename, String contents) {
		return withAttachment(filename, contents, "text/plain");
	}
}