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
import org.pagemodel.core.utils.ThrowingConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 * @author Allen Moore <allen.moore@improving.org>
 */
public class PopServer extends MailServer{
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private int popPort = -1;

	public PopServer(MailAuthenticator mailAuthenticator) {
		super(mailAuthenticator);
		this.popPort = mailAuthenticator.getPopPort();
	}


	public PopServer(String domain, String ip, String user, String password) {
		this(domain, ip, user, password, -1, false, false);
	}

	public PopServer(String domain, String ip, String user, String password, int popPort) {
		this(domain, ip, user, password, popPort, false, false);
	}

	public PopServer(String domain, String ip, String user, String password, boolean useTls) {
		this(domain, ip, user, password, -1, useTls, false);
	}

	public PopServer(String domain, String ip, String user, String password, int popPort, boolean useTls, boolean allowInsecure) {
		super(domain, ip, user, password, useTls, allowInsecure);
		this.popPort = popPort;
	}

	public Integer getPopPort() {
		return popPort;
	}

	public void setPopPort(Integer popPort) {
		this.popPort = popPort;
	}

	public MailMessage waitForMail(TestContext testContext, ThrowingConsumer<MailMessageTester<?>,?> mailPredicate, int timeoutSeconds) {
		return waitForMail(testContext, mailPredicate, 1, 1, timeoutSeconds).get(0);
	}

	public MailMessage waitForMail(TestContext testContext, Callable<MailMessage> sentMailRef, MailTester.SentMailFilter sentMailFilter, int timeoutSeconds) {
		return waitForMail(testContext, mp -> sentMailFilter.filter(sentMailRef.call(), mp), 1, 1, timeoutSeconds).get(0);
	}

	public List<MailMessage> waitForMail(TestContext testContext, Callable<MailMessage> sentMailRef, MailTester.SentMailFilter sentMailFilter, int minMailLimit, int maxMailLimit, int timeoutSeconds) {
		return waitForMail(testContext, mp -> sentMailFilter.filter(sentMailRef.call(), mp), minMailLimit, maxMailLimit, timeoutSeconds);
	}

	public List<MailMessage> waitForMail(TestContext testContext, ThrowingConsumer<MailMessageTester<?>,?> mailPredicate, int minMailLimit, int maxMailLimit, int timeoutSeconds) {
		List<MailMessage> results = new ArrayList<>();
		Set<Integer> checkedMessageIds = new HashSet<>();
		long start = System.currentTimeMillis();
		long end = start + (timeoutSeconds * 1000);
		log.info("Waiting for mail with timeout: [" + timeoutSeconds + "], matching:\n" + logMailPredicate(testContext, mailPredicate));
		int batchSize = Math.min(Math.max(20, maxMailLimit * 4), 80);
		while (results.size() < minMailLimit && System.currentTimeMillis() < end) {
			try {
				List<MailMessage> found = getAllMail(testContext, mailPredicate, maxMailLimit - results.size(), batchSize, checkedMessageIds);
				for (MailMessage mailMessage : found) {
					if (!results.contains(mailMessage)) {
						results.add(mailMessage);
					}
				}
				if (results.size() >= minMailLimit) {
					return results.subList(0, Math.min(results.size(), maxMailLimit));
				}
			}catch (Exception ex){
				log.info("Error: exception caught while fetching mail.", ex);
			}
			try {
				Thread.sleep(1000);
			}catch (InterruptedException ex){
				Thread.currentThread().interrupt();
				throw new RuntimeException("Error: Mail fetch interrupted.", ex);
			}
			long elapsed = (System.currentTimeMillis() - start)/1000;
			log.info("Waiting for mail with timeout: [" + timeoutSeconds + "], elapsed: [" + elapsed + "]");
		}
		throw new RuntimeException("Error: Unable to find [" + minMailLimit + "] message.  Found [" + results.size() + "] matching:\n" + logMailPredicate(testContext, mailPredicate));
	}

	public List<MailMessage> waitForMailNotFound(TestContext testContext, ThrowingConsumer<MailMessageTester<?>,?> mailPredicate, int timeoutSeconds) {
		return waitForMailNotFound(testContext, mailPredicate, 0, timeoutSeconds);
	}

	public List<MailMessage> waitForMailNotFound(TestContext testContext, ThrowingConsumer<MailMessageTester<?>,?> mailPredicate, int foundMailLimit, int timeoutSeconds) {
		List<MailMessage> results = new ArrayList<>();
		Set<Integer> checkedMessageIds = new HashSet<>();
		long start = System.currentTimeMillis();
		long end = start + (timeoutSeconds * 1000);
		log.info("Waiting for mail not found with timeout: [" + timeoutSeconds + "], found limit: [" + foundMailLimit + "], matching:\n" + logMailPredicate(testContext, mailPredicate));
		int batchSize = Math.min(Math.max(20, foundMailLimit * 4), 80);
		while (results.size() <= foundMailLimit && System.currentTimeMillis() < end) {
			try {
				List<MailMessage> found = getAllMail(testContext, mailPredicate, foundMailLimit - results.size(), batchSize, checkedMessageIds);
				for (MailMessage mailMessage : found) {
					if (!results.contains(mailMessage)) {
						results.add(mailMessage);
						if (results.size() > foundMailLimit) {
							throw new RuntimeException("Error: Expected to find at most [" + foundMailLimit + "] messages.  Found [" + results.size() + "] matching:\n" + logMailPredicate(testContext, mailPredicate));
						}
					}
				}
			}catch (Exception ex){
				log.info("Error: exception caught while fetching mail.", ex);
			}
			try {
				Thread.sleep(1000);
			}catch (InterruptedException ex){
				Thread.currentThread().interrupt();
				throw new RuntimeException("Error: Mail fetch interrupted.", ex);
			}
			long elapsed = (System.currentTimeMillis() - start)/1000;
			log.info("Waiting for mail with timeout: [" + timeoutSeconds + "], elapsed: [" + elapsed + "]");
		}
		return results;
	}

	private String logMailPredicate(TestContext testContext, ThrowingConsumer<MailMessageTester<?>,?> mailPredicate){
		TestEvaluator.LogTests testEvaluator = new TestEvaluator.LogTests(new TestEvaluator.Now());
		MailMessageTester tester = new MailMessageTester(() -> null, null, testContext, testEvaluator);
		tester.returnObj = tester;
		try {
			mailPredicate.accept(tester);
		}catch (Throwable ex){}
		return testEvaluator.getTestLog();
	}

	private List<MailMessage> getAllMail(TestContext testContext, ThrowingConsumer<MailMessageTester<?>,?> mailPredicate, int maxMailLimit, int batchSize, Set<Integer> checkedMessageIds) throws MessagingException {
		return getReceivedMail(testContext, () -> null, (sent, filter) -> ThrowingConsumer.unchecked(mailPredicate).accept(filter), maxMailLimit, batchSize, checkedMessageIds);
	}

	/**
	 * Check batchSize number of mail messages for messages matching the mailPredicate, starting with the most recent
	 * message.  Stop if maxMailLimit messages are found.
	 * If checkedMessageIds is not null, those messages will be ignored.  After a message is checked, the message number
	 * will be added to checkedMessageIds.  If checkedMessageIds is null, no messages will be skipped.
	 * @param testContext
	 * @param sentMailRef reference to a MailMessage to be passed into the mailPredicate SentMailFilter.
	 * @param mailPredicate test to apply to each MailMessage in batch
	 * @param maxMailLimit max number of MailMessages returned
	 * @param batchSize max number of MailMessages to check
	 * @param checkedMessageIds message numbers not to include in the batch.  If not null, messages in this batch will be added.
	 * @return list of MailMessages matching the mailPredicate found in this batch.  Size will not exceed maxMailLimit.
	 * @throws MessagingException for POP server errors
	 */
	private List<MailMessage> getReceivedMail(TestContext testContext, Callable<MailMessage> sentMailRef, MailTester.SentMailFilter mailPredicate, int maxMailLimit, int batchSize, Set<Integer> checkedMessageIds) throws MessagingException {
		Properties properties = System.getProperties();
		if(this.getUseTls()) {
			properties.setProperty("mail.pop3.ssl.enable", "true");
			properties.setProperty("mail.pop3s.ssl.enable", "true");
		}

		if(this.getAllowInsecure()){
			properties.setProperty("mail.pop3.ssl.trust", "*");
			properties.setProperty("mail.pop3s.ssl.trust", "*");
			properties.setProperty("mail.pop3.ssl.checkserveridentity", "false");
			properties.setProperty("mail.pop3s.ssl.checkserveridentity", "false");
		}

		Session session = Session.getDefaultInstance(properties);
		Store store = session.getStore("pop3");
		store.connect(getHost(), getPopPort(),  getUsername(), getPassword());
		Folder inbox = store.getFolder("Inbox");
		inbox.open(Folder.READ_WRITE);
		try {
			List<MailMessage> found = new ArrayList<>();
			int checkCount = 0;
			// messageNumbers start at 1 instead of 0.  The message count will be the most recent message. 1 will be the oldest.
			for (int i = inbox.getMessageCount(); i > 0 && checkCount < batchSize; i--){
				if (checkedMessageIds != null && checkedMessageIds.contains(i)){
					continue;
				}
				checkCount++;
				Message message = inbox.getMessage(i);
				LazyMailMessage mailMessage = new LazyMailMessage(message);
				TestEvaluator contextEvaluator = testContext.getEvaluator();
				TestEvaluator.NoException testEvaluator = new TestEvaluator.NoException(new TestEvaluator.Now());
				MailMessageTester tester = new MailMessageTester(() -> mailMessage, null, testContext, testEvaluator);
				tester.returnObj = tester;
				try{
					testContext.setEvaluator(testEvaluator);
					mailPredicate.filter(sentMailRef.call(), tester);
					if(checkedMessageIds !=null) {
						checkedMessageIds.add(i);
					}
					if(!testEvaluator.getTestStatus()){
						continue;
					}
				} catch (Exception ex){
					continue;
				}finally{
					testContext.setEvaluator(contextEvaluator);
				}
				mailMessage.loadAll();
				found.add(mailMessage);
				if (maxMailLimit > 0 && found.size() == maxMailLimit) {
					return found;
				}
			}
			return found;
		}finally {
			inbox.close(false);
			store.close();
		}
	}
}
