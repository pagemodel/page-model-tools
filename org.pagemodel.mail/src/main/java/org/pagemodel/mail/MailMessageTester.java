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
import org.pagemodel.core.testers.ComparableTester;
import org.pagemodel.core.testers.StringListTester;
import org.pagemodel.core.testers.StringTester;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.ThrowingConsumer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * This class wraps a javax.mail.Message class and allows testing of all message properties.
 * It provides methods to test the sender, recipients (To, Cc, Bcc), subject, text body, HTML body,
 * headers, sent date, attachments, and attachment count of the message.
 * It also allows performing actions on the message using the doAction method.
 * The class uses a TestEvaluator to evaluate the test results and a TestContext to provide context for the tests.
 * @param <R> the return type of the MailMessageTester
 */
public class MailMessageTester<R> {
	protected R returnObj;
	protected final Callable<MailMessage> ref;
	protected final TestContext testContext;
	private TestEvaluator testEvaluator;

	/**
	 * Constructs a new MailMessageTester with the given Callable reference to a MailMessage,
	 * return object, TestContext, and TestEvaluator.
	 * @param ref the Callable reference to a MailMessage
	 * @param returnObj the return object
	 * @param testContext the TestContext
	 * @param testEvaluator the TestEvaluator
	 */
	public MailMessageTester(Callable<MailMessage> ref, R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Calls the Callable reference to get the MailMessage object.
	 * @return the MailMessage object
	 */
	protected MailMessage callRef(){
		try{
			return ref.call();
		}catch (Throwable t){
			return null;
		}
	}

	/**
	 * Gets the TestEvaluator used by this MailMessageTester.
	 * @return the TestEvaluator
	 */
	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	/**
	 * Closes the MailMessage and returns the return object.
	 * @return the return object
	 */
	public R closeMail(){
		return returnObj;
	}

	/**
	 * Tests the sender of the MailMessage.
	 * @return a StringTester for the sender
	 */
	public StringTester<MailMessageTester<R>> testSender() {
		getEvaluator().setSourceFindEvent("sender", op -> op.addValue("mail", getMessageJson()));
		return new StringTester<>(() -> callRef().getSender(), this, testContext, getEvaluator());
	}

	/**
	 * Tests the recipients (To) of the MailMessage.
	 * @return a StringListTester for the recipients (To)
	 */
	public StringListTester<MailMessageTester<R>> testRecipientsTo() {
		getEvaluator().setSourceFindEvent("recipients To", op -> op.addValue("mail", getMessageJson()));
		return new StringListTester<>(() -> callRef().getRecipientsTo(), this, testContext, getEvaluator());
	}

	/**
	 * Tests the recipients (Cc) of the MailMessage.
	 * @return a StringListTester for the recipients (Cc)
	 */
	public StringListTester<MailMessageTester<R>> testRecipientsCc() {
		getEvaluator().setSourceFindEvent("recipients CC", op -> op.addValue("mail", getMessageJson()));
		return new StringListTester<>(() -> callRef().getRecipientsCc(), this, testContext, getEvaluator());
	}

	/**
	 * Tests the recipients (Bcc) of the MailMessage.
	 * @return a StringListTester for the recipients (Bcc)
	 */
	public StringListTester<MailMessageTester<R>> testRecipientsBcc() {
		getEvaluator().setSourceFindEvent("recipients BCC", op -> op.addValue("mail", getMessageJson()));
		return new StringListTester<>(() -> callRef().getRecipientsBcc(), this, testContext, getEvaluator());
	}

	/**
	 * Tests all recipients (To, Cc, Bcc) of the MailMessage.
	 * @return a StringListTester for all recipients
	 */
	public StringListTester<MailMessageTester<R>> testRecipientsAll() {
		getEvaluator().setSourceFindEvent("all recipients", op -> op.addValue("mail", getMessageJson()));
		return new StringListTester<>(() -> {
			List<String> recipients = new ArrayList<>(callRef().getRecipientsTo());
			recipients.addAll(callRef().getRecipientsCc());
			recipients.addAll(callRef().getRecipientsBcc());
			return recipients;
		}, this, testContext, getEvaluator());
	}

	/**
	 * Tests the subject of the MailMessage.
	 * @return a StringTester for the subject
	 */
	public StringTester<MailMessageTester<R>> testSubject() {
		getEvaluator().setSourceFindEvent("subject", op -> op.addValue("mail", getMessageJson()));
		return new StringTester<>(() -> callRef().getSubject(), this, testContext, getEvaluator());
	}

	/**
	 * Tests the text body of the MailMessage.
	 * @return a StringTester for the text body
	 */
	public StringTester<MailMessageTester<R>> testTextBody() {
		getEvaluator().setSourceFindEvent("text body", op -> op.addValue("mail", getMessageJson()));
		return new StringTester<>(() -> callRef().getTextBody(), this, testContext, getEvaluator());
	}

	/**
	 * Tests the HTML body of the MailMessage.
	 * @return a StringTester for the HTML body
	 */
	public StringTester<MailMessageTester<R>> testHtmlBody() {
		getEvaluator().setSourceFindEvent("html body", op -> op.addValue("mail", getMessageJson()));
		return new StringTester<>(() -> callRef().getHtmlBody(), this, testContext, getEvaluator());
	}

	/**
	 * Tests the header with the given name of the MailMessage.
	 * @param header the name of the header to test
	 * @return a StringTester for the header
	 */
	public StringTester<MailMessageTester<R>> testHeader(String header) {
		getEvaluator().setSourceFindEvent("header", op -> op
				.addValue("value", header)
				.addValue("mail", getMessageJson()));
		return new StringTester<>(() -> callRef().getHeader(header), this, testContext, getEvaluator());
	}

	/**
	 * Tests the list of headers with the given name of the MailMessage.
	 * @param header the name of the header to test
	 * @return a StringListTester for the list of headers
	 */
	public StringListTester<MailMessageTester<R>> testHeaderList(String header) {
		getEvaluator().setSourceFindEvent("header", op -> op
				.addValue("value", header)
				.addValue("mail", getMessageJson()));
		return new StringListTester<>(() -> callRef().getHeaderList(header), this, testContext, getEvaluator());
	}

	/**
	 * Tests the sent date of the MailMessage.
	 * @return a ComparableTester for the sent date
	 */
	public ComparableTester<Date, MailMessageTester<R>> testSentDate() {
		getEvaluator().setSourceFindEvent("sent date", op -> op.addValue("mail", getMessageJson()));
		return new ComparableTester<>(() -> callRef().getSentDate(), this, testContext, getEvaluator());
	}

	/**
	 * Tests the attachment with the given filename of the MailMessage.
	 * @param filename the filename of the attachment to test
	 * @return an AttachmentTester for the attachment
	 */
	public AttachmentTester<MailMessageTester<R>> testAttachment(String filename) {
		getEvaluator().setSourceFindEvent("attachment", op -> op
				.addValue("value", filename)
				.addValue("mail", getMessageJson()));
		return new AttachmentTester<>(() -> getAttachmentByFilename(filename), this, testContext, getEvaluator());
	}

	/**
	 * Tests the attachment at the given index of the MailMessage.
	 * @param i the index of the attachment to test
	 * @return an AttachmentTester for the attachment
	 */
	public AttachmentTester<MailMessageTester<R>> testAttachment(int i) {
		getEvaluator().setSourceFindEvent("attachment", op -> op
				.addValue("value", i)
				.addValue("mail", getMessageJson()));
		return new AttachmentTester<>(() -> getAttachmentByIndex(i), this, testContext, getEvaluator());
	}

	/**
	 * Tests the attachment count of the MailMessage.
	 * @return a ComparableTester for the attachment count
	 */
	public ComparableTester<Integer, MailMessageTester<R>> testAttachmentCount() {
		getEvaluator().setSourceFindEvent("attachment count", op -> op.addValue("mail", getMessageJson()));
		return new ComparableTester<>(() -> callRef().getAttachments().size(), this, testContext, getEvaluator());
	}

	/**
	 * Gets the attachment with the given filename from the MailMessage.
	 * @param filename the filename of the attachment to get
	 * @return the Attachment object with the given filename, or null if not found
	 */
	protected Attachment getAttachmentByFilename(String filename){
		MailMessage mailMessage = callRef();
		if(mailMessage == null){
			return null;
		}
		for(Attachment attachment : mailMessage.getAttachments()){
			if(attachment.getFilename().equals(filename)){
				return attachment;
			}
		}
		return null;
	}

	/**
	 * Performs the given action on this MailMessageTester and returns this MailMessageTester.
	 * @param action the action to perform
	 * @return this MailMessageTester
	 */
	public MailMessageTester<R> doAction(ThrowingConsumer<MailMessageTester<R>, ?> action){
		ThrowingConsumer.unchecked(action).accept(this);
		return this;
	}

	/**
	 * Gets the underlying MailMessage object being tested.
	 * @return the MailMessage object being tested
	 */
	public MailMessage getMailMessage(){
		return callRef();
	}

	/**
	 * Gets the attachment at the given index from the MailMessage.
	 * @param i the index of the attachment to get
	 * @return the Attachment object at the given index, or null if not found
	 */
	protected Attachment getAttachmentByIndex(int i){
		MailMessage mailMessage = callRef();
		if(mailMessage == null){
			return null;
		}
		if(mailMessage.getAttachments().size() <= i){
			return null;
		}
		return mailMessage.getAttachments().get(i);
	}

	/**
	 * Gets a display string representation of the MailMessage being tested.
	 * @return a display string representation of the MailMessage being tested
	 */
	protected String getMessageDisplay(){
		MailMessage message = callRef();
		return message == null ? null : message.toString();
	}

	/**
	 * Gets a JSON representation of the MailMessage being tested.
	 * @return a Map representing the JSON of the MailMessage being tested
	 */
	protected Map<String,Object> getMessageJson(){
		MailMessage message = callRef();
		return message == null ? null : message.toJson();
	}
}