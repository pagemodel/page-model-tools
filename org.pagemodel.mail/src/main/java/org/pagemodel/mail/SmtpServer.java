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

import com.sun.mail.smtp.SMTPTransport;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class SmtpServer extends MailServer{
	private int smtpPort = 25;

	public SmtpServer(MailAuthenticator mailAuthenticator) {
		super(mailAuthenticator);
		this.smtpPort = mailAuthenticator.getSmtpPort();
	}

	public SmtpServer(String domain, String ip, String user, String password) {
		super(domain, ip, user, password, false);
	}

	public SmtpServer(String domain, String ip, String user, String password, int smtpPort) {
		super(domain, ip, user, password, false);
		this.smtpPort = smtpPort;
	}

	public SmtpServer(String domain, String ip, String user, String password, int smtpPort, boolean useTls) {
		super(domain, ip, user, password, useTls);
		this.smtpPort = smtpPort;
	}

	public Integer getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(Integer smtpPort) {
		this.smtpPort = smtpPort;
	}

	public MailMessage send(MailMessage mailMessage) throws MessagingException, IOException {
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.host", getHost());
		props.put("mail.smtp.port", getSmtpPort());
		props.put("mail.smtp.from", mailMessage.getSender());
		props.put("mail.smtp.starttls.enable", getUseTls());
		Authenticator authenticator = null;
		if (getUsername() != null && !getUsername().isEmpty() && getPassword() != null && !getPassword().isEmpty()) {

			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.user", getUsername());
			props.put("mail.smtp.password", getPassword());

			authenticator = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(getUsername(), getPassword());
				}
			};
		}

		Session session = Session.getInstance(props, authenticator);

		SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
		transport.connect();

		List<Address> recipients = new ArrayList<>();
		for (String r : mailMessage.getRecipientsTo()) {
			recipients.add(new InternetAddress(r));
		}
		for (String r : mailMessage.getRecipientsCc()) {
			recipients.add(new InternetAddress(r));
		}
		for (String r : mailMessage.getRecipientsBcc()) {
			recipients.add(new InternetAddress(r));
		}
		MimeMessage mimeMessage = MimeMailAdapter.createMimeMessage(mailMessage, session);
		transport.sendMessage(mimeMessage, recipients.toArray(new Address[0]));
		transport.close();
		mailMessage.setSentDate(new Date());
		return mailMessage;
	}
}
