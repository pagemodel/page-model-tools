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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class MailMessageTester<R> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
		getEvaluator().setSourceDisplayRef(() -> "sender");
		return new StringTester<>(() -> callRef().getSender(), this, testContext, getEvaluator());
	}

	public StringListTester<MailMessageTester<R>> testRecipientsTo() {
		getEvaluator().setSourceDisplayRef(() -> "recipients [to]");
		return new StringListTester<>(() -> callRef().getRecipientsTo(), this, testContext, getEvaluator());
	}

	public StringListTester<MailMessageTester<R>> testRecipientsCc() {
		getEvaluator().setSourceDisplayRef(() -> "recipients [cc]");
		return new StringListTester<>(() -> callRef().getRecipientsCc(), this, testContext, getEvaluator());
	}

	public StringListTester<MailMessageTester<R>> testRecipientsBcc() {
		getEvaluator().setSourceDisplayRef(() -> "recipients [bcc]");
		return new StringListTester<>(() -> callRef().getRecipientsBcc(), this, testContext, getEvaluator());
	}

	public StringListTester<MailMessageTester<R>> testRecipientsAll() {
		getEvaluator().setSourceDisplayRef(() -> "recipients [to,cc,bcc]");
		return new StringListTester<>(() -> {
			List<String> recipients = new ArrayList<>(callRef().getRecipientsTo());
			recipients.addAll(callRef().getRecipientsCc());
			recipients.addAll(callRef().getRecipientsBcc());
			return recipients;
		}, this, testContext, getEvaluator());
	}

	public StringTester<MailMessageTester<R>> testSubject() {
		getEvaluator().setSourceDisplayRef(() -> "subject");
		return new StringTester<>(() -> callRef().getSubject(), this, testContext, getEvaluator());
	}

	public StringTester<MailMessageTester<R>> testTextBody() {
		getEvaluator().setSourceDisplayRef(() -> "text body");
		return new StringTester<>(() -> callRef().getTextBody(), this, testContext, getEvaluator());
	}

	public StringTester<MailMessageTester<R>> testHtmlBody() {
		getEvaluator().setSourceDisplayRef(() -> "html body");
		return new StringTester<>(() -> callRef().getHtmlBody(), this, testContext, getEvaluator());
	}

	public StringTester<MailMessageTester<R>> testHeader(String header) {
		getEvaluator().setSourceDisplayRef(() -> "header [" + header + "]");
		return new StringTester<>(() -> callRef().getHeader(header), this, testContext, getEvaluator());
	}

	public ComparableTester<Date, MailMessageTester<R>> testSentDate() {
		getEvaluator().setSourceDisplayRef(() -> "sent date");
		return new ComparableTester<>(() -> callRef().getSentDate(), this, testContext, getEvaluator());
	}

	public AttachmentTester<MailMessageTester<R>> testAttachment(String filename) {
		getEvaluator().setSourceDisplayRef(() -> "attachment with filename: [" + filename + "]");
		return new AttachmentTester<>(() -> getAttachmentByFilename(filename), this, testContext, getEvaluator());
	}

	public AttachmentTester<MailMessageTester<R>> testAttachment(int i) {
		getEvaluator().setSourceDisplayRef(() -> "attachment at index: [" + i + "]");
		return new AttachmentTester<>(() -> getAttachmentByIndex(i), this, testContext, getEvaluator());
	}

	public ComparableTester<Integer, MailMessageTester<R>> testAttachmentCount() {
		getEvaluator().setSourceDisplayRef(() -> "attachment count");
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
}
