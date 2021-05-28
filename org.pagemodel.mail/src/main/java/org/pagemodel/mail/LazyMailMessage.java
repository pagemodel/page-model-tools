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

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class LazyMailMessage extends MailMessage {
	private boolean recipientsLoaded = false;
	private boolean bodyLoaded = false;
	private boolean attachmentsLoaded = false;
	private boolean headersLoaded = false;

	public LazyMailMessage(Message message) {
		try {
			MimeMessage messageCopy = new MimeMessage((MimeMessage) message);
			setMessage(messageCopy);
		}catch (MessagingException ex){
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String getSender() {
		loadSender();
		return super.getSender();
	}

	@Override
	public List<String> getRecipientsTo() {
		loadRecipients();
		return super.getRecipientsTo();
	}

	@Override
	public List<String> getRecipientsCc() {
		loadRecipients();
		return super.getRecipientsCc();
	}

	@Override
	public List<String> getRecipientsBcc() {
		loadRecipients();
		return super.getRecipientsBcc();
	}

	@Override
	public String getSubject() {
		loadSubject();
		return super.getSubject();
	}

	@Override
	public Date getSentDate() {
		loadSentDate();
		return super.getSentDate();
	}

	@Override
	public String getTextBody() {
		loadBody();
		return super.getTextBody();
	}

	@Override
	public String getHtmlBody() {
		loadBody();
		return super.getHtmlBody();
	}

	@Override
	public String getHeader(String name) {
		loadHeaders();
		return super.getHeader(name);
	}

	@Override
	public Map<String, List<String>> getHeaders() {
		loadHeaders();
		return super.getHeaders();
	}

	@Override
	public List<Attachment> getAttachments() {
		loadAttachments();
		return super.getAttachments();
	}

	public void loadAll(){
		loadSender();
		loadRecipients();
		loadSubject();
		loadSentDate();
		loadBody();
		loadHeaders();
		loadAttachments();
	}

	public void loadSender() {
		if(sender == null) {
			try {
				Address[] from = message.getFrom();
				if(from == null){
					setSender(null);
				}else {
					setSender(String.join(",",
							Arrays.stream(message.getFrom()).map(this::getEmail).toArray(String[]::new)));
				}
			} catch (MessagingException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private String getEmail(Address address){
		if(address instanceof InternetAddress){
			return ((InternetAddress)address).getAddress();
		}
		return address.toString();
	}

	public void loadRecipients(){
		if(recipientsLoaded){
			return;
		}
		try {
			for (Address address : MimeMailAdapter.getRecipients(message, Message.RecipientType.TO)) {
				addRecipientsTo(getEmail(address));
			}
			for (Address address : MimeMailAdapter.getRecipients(message, Message.RecipientType.CC)) {
				addRecipientsCc(getEmail(address));
			}
			for (Address address : MimeMailAdapter.getRecipients(message, Message.RecipientType.BCC)) {
				addRecipientsBcc(getEmail(address));
			}
		}catch (MessagingException ex) {
			throw new RuntimeException(ex);
		}
		recipientsLoaded = true;
	}

	public void loadSubject() {
		if(subject == null) {
			try {
				setSubject(message.getSubject());
			} catch (MessagingException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	public void loadSentDate() {
		if(sentDate == null){
			try {
				setSentDate(message.getSentDate());
			} catch (MessagingException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	public void loadBody() {
		if (!bodyLoaded) {
			try {
				MimeMailAdapter.readBody(message.getContent(), this);
			} catch (IOException | MessagingException ex) {
				throw new RuntimeException(ex);
			}
			bodyLoaded = true;
		}
	}

	public void loadHeaders() {
		if(!headersLoaded){
			try {
				MimeMailAdapter.readHeaders(message, this);
			}catch (MessagingException ex){
				throw new RuntimeException(ex);
			}
			headersLoaded = true;
		}
	}

	public void loadAttachments() {
		if(!attachmentsLoaded){
			try {
				MimeMailAdapter.readAttachments(message, this);
			}catch (IOException | MessagingException ex){
				throw new RuntimeException(ex);
			}
			attachmentsLoaded = true;
		}
	}
}
