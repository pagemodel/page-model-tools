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

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class MimeMailAdapter {
	public static MailMessage readMessage(Message message) throws MessagingException, IOException {
		if(message == null){
			throw new NullPointerException("Error: MimeMessage is null");
		}
		MailMessage mailMessage = new MailMessage();
		mailMessage.setSender(message.getFrom().toString());
		for(Address address : getRecipients(message, Message.RecipientType.TO)){
			mailMessage.addRecipientsTo(address.toString());
		}
		for(Address address : getRecipients(message, Message.RecipientType.CC)){
			mailMessage.addRecipientsCc(address.toString());
		}
		for(Address address : getRecipients(message, Message.RecipientType.BCC)){
			mailMessage.addRecipientsBcc(address.toString());
		}
		mailMessage.setSubject(message.getSubject());
		readHeaders(message, mailMessage);
		readBody(message.getContent(), mailMessage);
		readAttachments(message, mailMessage);
		mailMessage.setSentDate(message.getSentDate());
		MimeMessage messageCopy = new MimeMessage((MimeMessage) message);
		mailMessage.setMessage(messageCopy);
		return mailMessage;
	}

	protected static void readHeaders(Message message, MailMessage mailMessage) throws MessagingException {
		Enumeration<Header> headers = message.getAllHeaders();
		while (headers.hasMoreElements()) {
			Header header = headers.nextElement();
			if(header != null && header.getName() != null) {
				mailMessage.setHeader(header.getName(), header.getValue());
			}
		}
	}

	protected static void readAttachments(Part message, MailMessage mailMessage) throws MessagingException, IOException {
		if(message.getContentType().contains("multipart")) {
			MimeMultipart mp = (MimeMultipart) message.getContent();
			for (int i = 0; i < mp.getCount(); ++i){
				BodyPart bp = mp.getBodyPart(i);
				String filename = bp.getFileName();
				Object content = bp.getContent();
				String contentType = bp.getContentType();
				if (filename != null && content != null && contentType != null){
					if(bp.getContentType().equals("text/plain")) {
						mailMessage.addAttachment(new Attachment.TextAttachment(filename, content.toString(), contentType));
					}else if(bp.getContentType().contains("multipart")) {
						readAttachments(bp, mailMessage);
					}else{
						byte[] bytes = getStreamBytes(bp.getDataHandler().getInputStream());
						mailMessage.addAttachment(new Attachment(filename, bytes, contentType));
					}
				}
			}
		}
	}

	private static byte[] getStreamBytes(InputStream in){
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[16384];
			while ((nRead = in.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			return buffer.toByteArray();
		}catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}

	protected static Address[] getRecipients(Message message, Message.RecipientType recipientType) throws MessagingException {
		Address[] addresses = message.getRecipients(recipientType);
		if (addresses == null) {
			return new Address[0];
		}
		return addresses;
	}

	protected static void readBody(Object content, MailMessage mailMessage) throws IOException, MessagingException {
		if (content == null) {
			mailMessage.setTextBody("");
			mailMessage.setHtmlBody("");
		} else if (Multipart.class.isAssignableFrom(content.getClass())) {
			Multipart mp = (Multipart) content;
			for (int i = 0; i < mp.getCount(); ++i) {
				readBody(mp.getBodyPart(i), mailMessage);
			}
		} else if (BodyPart.class.isAssignableFrom(content.getClass())) {
			BodyPart bp = (BodyPart) content;
			if (bp.getContentType().contains("multipart")) {
				readBody(bp.getContent(), mailMessage);
			} else {
				if (bp.getDisposition() != null && bp.getDisposition().contains("attachment")){
					return;
				} else if (bp.getContentType().toLowerCase().contains("text/plain")) {
					mailMessage.setTextBody(bp.getContent().toString());
				} else if (bp.getContentType().toLowerCase().contains("text/html")) {
					mailMessage.setHtmlBody(bp.getContent().toString());
				}
			}
		} else {
			mailMessage.setTextBody(content.toString());
			mailMessage.setHtmlBody(content.toString());
		}
	}

	public static MimeMessage createMimeMessage(MailMessage mailMessage, Session session) throws MessagingException, IOException {
		if(mailMessage == null){
			throw new NullPointerException("Error: MailMessage is null");
		}
		MimeMessage mime = new MimeMessage(session);
//
//		if (simulateThunderbirdStyleMime) {
//			simulateThunderbirdSubjectMime(mime);
//		}

		mime.setFrom(new InternetAddress(mailMessage.getSender()));

		for (String r : mailMessage.getRecipientsTo()) {
			mime.addRecipient(Message.RecipientType.TO, new InternetAddress(r));
		}
		for (String r : mailMessage.getRecipientsCc()) {
			mime.addRecipient(Message.RecipientType.CC, new InternetAddress(r));
		}
		for (String r : mailMessage.getRecipientsBcc()) {
			mime.addRecipient(Message.RecipientType.BCC, new InternetAddress(r));
		}
		mime.setSubject(mailMessage.getSubject());
		Set<String> headers = mailMessage.getHeaders().keySet();
		for(String s : headers){
			for(String val : mailMessage.getHeaderList(s)) {
				mime.addHeader(s, val);
			}
		}

		MimeMultipart multi = buildMultipart(mailMessage);
		mime.setContent(multi);

		if(mailMessage.getSentDate() != null){
			mime.setSentDate(mailMessage.getSentDate());
		} else {
			mime.setSentDate(new Date());
		}
		return mime;
	}

	public static void simulateThunderbirdSubjectMime(MimeMessage mime) throws MessagingException {
//		log.trace("modifying mime to simulate Thunderbird-style mime");
		Pattern p = Pattern.compile("(=[0-9A-Fa-f][0-9A-Fa-f])");
		for (String s : mime.getHeader("Subject")) {
			Matcher m = p.matcher(s);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				m.appendReplacement(sb, m.group(1).toLowerCase());
			}
			m.appendTail(sb);
//			log.info("reseting subject to thunderbird style: " + sb.toString());
			mime.setHeader("Subject", sb.toString());
		}
	}

	private static MimeMultipart buildMultipart(MailMessage mailMessage) throws MessagingException, IOException {
		MimeMultipart mp = new MimeMultipart("alternative");
		MimeBodyPart textPart = new MimeBodyPart();
		textPart.setText(fixBodyPart(mailMessage.getTextBody()), "us-ascii", "plain");
		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setText(fixBodyPart(mailMessage.getHtmlBody()), "us-ascii", "html");
		mp.addBodyPart(textPart);
		mp.addBodyPart(htmlPart);

		List<Attachment> atts = mailMessage.getAttachments();
		if (atts == null || atts.size() == 0) {
			return mp;
		}

		MimeMultipart mp2 = new MimeMultipart("mixed");
		MimeBodyPart body = new MimeBodyPart();
		body.setContent(mp);
		mp2.addBodyPart(body);

		for(Attachment a : atts) {
			mp2.addBodyPart(createBodyPart(a));
		}
		return mp2;
	}

	private static BodyPart createBodyPart(Attachment attachment) throws MessagingException {
		MimeBodyPart bodyPart = new MimeBodyPart();
		bodyPart.setFileName(attachment.getFilename());
		if(Attachment.TextAttachment.class.isAssignableFrom(attachment.getClass()) || attachment.getContentType().equals("text/plain")){
			bodyPart.setContent(((Attachment.TextAttachment)attachment).getTextContent(), attachment.getContentType());
		}else {
			bodyPart.setContent(attachment.getByteContent(), attachment.getContentType());
		}
		return bodyPart;
	}


	private static String fixBodyPart(String textBody)
	{
		// ensure the body ends in a empty line because of bug in JavaMail;
		// this work-around fix to a SMTP IN bug did not involve, this did
		// not gain the approval of Mr. Brand Hilton

		if (textBody.endsWith("\n\n"))
		{
			return textBody;
		}
		return textBody + "\n";

		// what?   you also want to know what the SMTP IN bug is?  don't
		// you have better things to do with your time than worry about
		// things that will never be fixed?
		//
		// okay fine, if you insist; well you see if a body part ends with
		// no blank line between it and the boundary separator in the mime;
		// SMTP IN throws an exception when parsing. Bounce...bad juju...
		// In the real world this probably doesn't happen because it looks
		// like sending mail through postfix/gateways modifies the mime and
		// fixes it.  (although I didn't think they were supposed to do that)
		// but what do I know. - unknown
	}
}
