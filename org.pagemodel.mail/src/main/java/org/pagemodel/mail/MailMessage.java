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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class MailMessage {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	protected String sender;
	protected List<String> recipientsTo = new ArrayList<>();
	protected List<String> recipientsCc = new ArrayList<>();
	protected List<String> recipientsBcc = new ArrayList<>();
	protected String subject;
	protected String textBody;
	protected String htmlBody;
	protected Map<String, String> headers = new HashMap<>();
	protected Date sentDate;
	protected List<Attachment> attachments = new ArrayList<>();
	protected Message message;

	public MailMessage(){}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public List<String> getRecipientsTo() {
		return recipientsTo;
	}

	public void addRecipientsTo(String...recipients) {
		for(String recipient : recipients) {
			if(!recipientsTo.contains(recipient)) {
				this.recipientsTo.add(recipient);
			}
		}
	}

	public List<String> getRecipientsCc() {
		return recipientsCc;
	}

	public void addRecipientsCc(String...recipients) {
		for(String recipient : recipients) {
			if(!recipientsCc.contains(recipient)) {
				this.recipientsCc.add(recipient);
			}
		}
	}

	public List<String> getRecipientsBcc() {
		return recipientsBcc;
	}

	public void addRecipientsBcc(String...recipients) {
		for(String recipient : recipients) {
			if(!recipientsBcc.contains(recipient)) {
				this.recipientsBcc.add(recipient);
			}
		}
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public Date getSentDate() {
		return sentDate;
	}

	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	public String getTextBody() {
		return textBody;
	}

	public void setTextBody(String messageBody) {
		this.textBody = messageBody;
	}

	public String getHtmlBody() {
		return htmlBody;
	}

	public void setHtmlBody(String htmlBody) {
		this.htmlBody = htmlBody;
	}

	public void setBody(String messageBody) {
		this.textBody = messageBody;
		this.htmlBody = messageBody;
	}

	public void setHeader(String name, String value) {
		headers.put(name, value);
	}
	public String getHeader(String name) {
		return headers.get(name);
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void addAttachment(Attachment attachment) {
		this.attachments.add(attachment);
	}

	public void addAttachment(File contents, String contentType) {
		addAttachment(new Attachment(contents, contentType));
	}

	public void addAttachment(String filename, File contents, String contentType) {
		addAttachment(new Attachment(filename, contents, contentType));
	}

	public void addAttachment(String filename, String contents, String contentType)	{
		attachments.add(new Attachment.TextAttachment(filename, contents, contentType));
	}

	public void addAttachment(String filename, byte[] contents, String contentType) {
		attachments.add(new Attachment(filename, contents, contentType));
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "mail(subject:[" + subject + "], sender:[" + sender + "], recipientsTo:[" + String.join(", ", recipientsTo) + "])";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MailMessage))
			return false;
		MailMessage that = (MailMessage) o;
		return sender.equals(that.sender) &&
				recipientsTo.equals(that.recipientsTo) &&
				recipientsCc.equals(that.recipientsCc) &&
				recipientsBcc.equals(that.recipientsBcc) &&
				Objects.equals(subject, that.subject) &&
				Objects.equals(textBody, that.textBody) &&
				Objects.equals(htmlBody, that.htmlBody) &&
				headers.equals(that.headers) &&
				Objects.equals(sentDate, that.sentDate) &&
				attachments.equals(that.attachments);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sender, recipientsTo, recipientsCc, recipientsBcc, subject, textBody, htmlBody, headers, sentDate, attachments);
	}

	//	private List<BodyPart> getAttachments() throws MessagingException,
//			IOException {
//		List<BodyPart> result = new ArrayList<BodyPart>();
//		if (getMime() != null) {
//			if (getMime() != null
//					&& getMime().getContentType().contains("multipart")) {
//				MimeMultipart mp = (MimeMultipart) getMime().getContent();
//				for (int i = 0; i < mp.getCount(); ++i)
//					result.add(mp.getBodyPart(i));
//			}
//		} else {
//			return atts;
//		}
//		return result;
//	}

//    public static MailMessage createFromFile(File f) throws MessagingException, FileNotFoundException
//    {
//        MimeMessage mime = new MimeMessage(null, new FileInputStream(f));
//        MailMessage result = new MailMessage();
//        result.setMime(mime);
//        result.setSender(mime.getFrom()[0].toString());
//        result.addToRecipient(mime.getRecipients(RecipientType.TO)[0].toString());
//        result.setSubject(mime.getSubject());
//        return result;
//    }
//
//    public static MailMessage createFromFileForMimeBlast(File f) throws MessagingException, FileNotFoundException
//    {
//    	//createFromFile was automatically adding the mime recipients as smtp recipients. We don't want to do this for mimeblast
//        MimeMessage mime = new MimeMessage(null, new FileInputStream(f));
//        MailMessage result = new MailMessage();
//        result.setMime(mime);
//        result.setSubject(mime.getSubject());
//        return result;
//    }

//	public BodyPart getAttachment(String attachmentName) throws IOException, MessagingException {
//		for(BodyPart attachment : getAttachments()){
//			if(attachment.getFileName() != null && attachment.getFileName().equals(attachmentName)){
//				return attachment;
//			}
//		}
//		throw new IOException("attachment " + attachmentName + " not found");
//	}
//
//	public void saveMime(String filePath) throws FileNotFoundException, IOException, MessagingException {
//		FileOutputStream os = new FileOutputStream(new File(filePath));
//		mime.writeTo(os);
//		os.close();
//	}
}
