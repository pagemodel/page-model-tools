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
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class MailMessageTester<R> {
	protected R returnObj;
	protected final Callable<MailMessage> ref;
	protected final TestContext testContext;
	private TestEvaluator testEvaluator;

	public MailMessageTester(Callable<MailMessage> ref, R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	protected MailMessage callRef(){
		try{
			return ref.call();
		}catch (Throwable t){
			return null;
		}
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	public R closeMail(){
		return returnObj;
	}

	public StringTester<MailMessageTester<R>> testSender() {
		getEvaluator().setSourceFindEvent("sender", op -> op.addValue("mail", getMessageJson()));
		return new StringTester<>(() -> callRef().getSender(), this, testContext, getEvaluator());
	}

	public StringListTester<MailMessageTester<R>> testRecipientsTo() {
		getEvaluator().setSourceFindEvent("recipients To", op -> op.addValue("mail", getMessageJson()));
		return new StringListTester<>(() -> callRef().getRecipientsTo(), this, testContext, getEvaluator());
	}

	public StringListTester<MailMessageTester<R>> testRecipientsCc() {
		getEvaluator().setSourceFindEvent("recipients CC", op -> op.addValue("mail", getMessageJson()));
		return new StringListTester<>(() -> callRef().getRecipientsCc(), this, testContext, getEvaluator());
	}

	public StringListTester<MailMessageTester<R>> testRecipientsBcc() {
		getEvaluator().setSourceFindEvent("recipients BCC", op -> op.addValue("mail", getMessageJson()));
		return new StringListTester<>(() -> callRef().getRecipientsBcc(), this, testContext, getEvaluator());
	}

	public StringListTester<MailMessageTester<R>> testRecipientsAll() {
		getEvaluator().setSourceFindEvent("all recipients", op -> op.addValue("mail", getMessageJson()));
		return new StringListTester<>(() -> {
			List<String> recipients = new ArrayList<>(callRef().getRecipientsTo());
			recipients.addAll(callRef().getRecipientsCc());
			recipients.addAll(callRef().getRecipientsBcc());
			return recipients;
		}, this, testContext, getEvaluator());
	}

	public StringTester<MailMessageTester<R>> testSubject() {
		getEvaluator().setSourceFindEvent("subject", op -> op.addValue("mail", getMessageJson()));
		return new StringTester<>(() -> callRef().getSubject(), this, testContext, getEvaluator());
	}

	public StringTester<MailMessageTester<R>> testTextBody() {
		getEvaluator().setSourceFindEvent("text body", op -> op.addValue("mail", getMessageJson()));
		return new StringTester<>(() -> callRef().getTextBody(), this, testContext, getEvaluator());
	}

	public StringTester<MailMessageTester<R>> testHtmlBody() {
		getEvaluator().setSourceFindEvent("html body", op -> op.addValue("mail", getMessageJson()));
		return new StringTester<>(() -> callRef().getHtmlBody(), this, testContext, getEvaluator());
	}

	public StringTester<MailMessageTester<R>> testHeader(String header) {
		getEvaluator().setSourceFindEvent("header", op -> op
				.addValue("value", header)
				.addValue("mail", getMessageJson()));
		return new StringTester<>(() -> callRef().getHeader(header), this, testContext, getEvaluator());
	}

	public StringListTester<MailMessageTester<R>> testHeaderList(String header) {
		getEvaluator().setSourceFindEvent("header", op -> op
				.addValue("value", header)
				.addValue("mail", getMessageJson()));
		return new StringListTester<>(() -> callRef().getHeaderList(header), this, testContext, getEvaluator());
	}

	public ComparableTester<Date, MailMessageTester<R>> testSentDate() {
		getEvaluator().setSourceFindEvent("sent date", op -> op.addValue("mail", getMessageJson()));
		return new ComparableTester<>(() -> callRef().getSentDate(), this, testContext, getEvaluator());
	}

	public AttachmentTester<MailMessageTester<R>> testAttachment(String filename) {
		getEvaluator().setSourceFindEvent("attachment", op -> op
				.addValue("value", filename)
				.addValue("mail", getMessageJson()));
		return new AttachmentTester<>(() -> getAttachmentByFilename(filename), this, testContext, getEvaluator());
	}

	public AttachmentTester<MailMessageTester<R>> testAttachment(int i) {
		getEvaluator().setSourceFindEvent("attachment", op -> op
				.addValue("value", i)
				.addValue("mail", getMessageJson()));
		return new AttachmentTester<>(() -> getAttachmentByIndex(i), this, testContext, getEvaluator());
	}

	public ComparableTester<Integer, MailMessageTester<R>> testAttachmentCount() {
		getEvaluator().setSourceFindEvent("attachment count", op -> op.addValue("mail", getMessageJson()));
		return new ComparableTester<>(() -> callRef().getAttachments().size(), this, testContext, getEvaluator());
	}

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

	public MailMessageTester<R> doAction(ThrowingConsumer<MailMessageTester<R>, ?> action){
		ThrowingConsumer.unchecked(action).accept(this);
		return this;
	}

	public MailMessage getMailMessage(){
		return callRef();
	}

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

	protected String getMessageDisplay(){
		MailMessage message = callRef();
		return message == null ? null : message.toString();
	}

	protected Map<String,Object> getMessageJson(){
		MailMessage message = callRef();
		return message == null ? null : message.toJson();
	}
}
