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
import org.pagemodel.core.utils.ThrowingCallable;
import org.pagemodel.core.utils.ThrowingConsumer;

import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * This class wraps a javax.mail mail server and allows fetching at testing different emails.
 * It provides methods to compose and fetch emails, as well as to test and store them.
 * @param <R> the return type of the MailTester
 */
public class MailTester<R> {

	protected TestContext testContext;
	protected R returnObj;
	private TestEvaluator testEvaluator;

	/**
	 * Constructor for the MailTester class.
	 * @param testContext the TestContext object
	 * @param returnObj the return object of the MailTester
	 * @param testEvaluator the TestEvaluator object
	 */
	public MailTester(TestContext testContext, R returnObj, TestEvaluator testEvaluator) {
		this.testContext = testContext;
		this.returnObj = returnObj;
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Returns the TestEvaluator object.
	 * @return the TestEvaluator object
	 */
	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	/**
	 * Composes a new email message.
	 * @return a new MailMessageBuilder object
	 */
	public MailMessageBuilder<R> composeMail(){
		getEvaluator().logEvent(TestEvaluator.TEST_EXECUTE, "compose mail", null);
		return new MailMessageBuilder<>(returnObj, testContext, getEvaluator());
	}

	/**
	 * Closes the email.
	 * @return the return object of the MailTester
	 */
	public R closeMail(){
		return returnObj;
	}

	/**
	 * Tests an email message.
	 * @param mailMessage the MailMessage object to test
	 * @return a new MailMessageTester object
	 */
	public MailMessageTester<R> test(MailMessage mailMessage){
		return new MailMessageTester<R>(() -> mailMessage, returnObj, testContext, new TestEvaluator.Now());
	}

	/**
	 * Fetches an email message from the mail server.
	 * @param popServer the PopServer object
	 * @param timeoutSec the timeout in seconds
	 * @param mailPredicate the mail predicate to test
	 * @return a new MailMessageTester object
	 */
	public MailMessageTester<R> fetchMail(PopServer popServer, int timeoutSec, ThrowingConsumer<MailMessageTester<?>, ?> mailPredicate){
		MailMessage mailMessage = popServer.waitForMail(testContext, mailPredicate, timeoutSec);
		return new MailMessageTester<>(() -> mailMessage, returnObj, testContext, new TestEvaluator.Now());
	}

	/**
	 * Fetches an email message from the mail server by subject and recipient.
	 * @param popServer the PopServer object
	 * @param timeoutSec the timeout in seconds
	 * @param subject the subject of the email
	 * @param recipient the recipient of the email
	 * @return a new MailMessageTester object
	 */
	public MailMessageTester<R> fetchMail(PopServer popServer, int timeoutSec, String subject, String recipient){
		return fetchMail(popServer, timeoutSec, mail -> mail
				.testSubject().equals(subject)
				.testRecipientsAll().contains(recipient));
	}

	/**
	 * Fetches an email message from the mail server by subject.
	 * @param popServer the PopServer object
	 * @param timeoutSec the timeout in seconds
	 * @param subject the subject of the email
	 * @return a new MailMessageTester object
	 */
	public MailMessageTester<R> fetchMail(PopServer popServer, int timeoutSec, String subject){
		return fetchMail(popServer, timeoutSec, mail -> mail
				.testSubject().equals(subject));
	}

	/**
	 * Waits for an email message to not be found on the mail server.
	 * @param popServer the PopServer object
	 * @param timeoutSec the timeout in seconds
	 * @param mailPredicate the mail predicate to test
	 * @return the return object of the MailTester
	 */
	public R mailNotFound(PopServer popServer, int timeoutSec, ThrowingConsumer<MailMessageTester<?>, ?> mailPredicate) {
		popServer.waitForMailNotFound(testContext, mailPredicate, timeoutSec);
		return returnObj;
	}

	/**
	 * Waits for an email message to not be found on the mail server by subject and recipient.
	 * @param popServer the PopServer object
	 * @param timeoutSec the timeout in seconds
	 * @param subject the subject of the email
	 * @param recipient the recipient of the email
	 * @return the return object of the MailTester
	 */
	public R mailNotFound(PopServer popServer, int timeoutSec, String subject, String recipient){
		return mailNotFound(popServer, timeoutSec, mail -> mail
				.testSubject().equals(subject)
				.testRecipientsAll().contains(recipient));
	}

	/**
	 * Waits for an email message to not be found on the mail server by subject.
	 * @param popServer the PopServer object
	 * @param timeoutSec the timeout in seconds
	 * @param subject the subject of the email
	 * @return the return object of the MailTester
	 */
	public R mailNotFound(PopServer popServer, int timeoutSec, String subject){
		return mailNotFound(popServer, timeoutSec, mail -> mail
				.testSubject().equals(subject));
	}

	/**
	 * This class extends MailTester and provides additional methods to fetch and store sent emails.
	 * @param <R> the return type of the MailTester
	 */
	public static class SentMailTester<R> extends MailTester<R> {
		private Callable<MailMessage> sentMailRef;

		/**
		 * Constructor for the SentMailTester class.
		 * @param sentMailRef the Callable object for the sent email
		 * @param returnObj the return object of the MailTester
		 * @param testContext the TestContext object
		 * @param testEvaluator the TestEvaluator object
		 */
		public SentMailTester(Callable<MailMessage> sentMailRef, R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
			super(testContext, returnObj, testEvaluator);
			this.sentMailRef = sentMailRef;
		}

		/**
		 * Fetches a sent email message from the mail server.
		 * @param popServer the PopServer object
		 * @param timeoutSec the timeout in seconds
		 * @param mailPredicate the mail predicate to test
		 * @return a new MailMessageTester object
		 */
		public MailMessageTester<R> fetchSent(PopServer popServer, int timeoutSec, SentMailFilter mailPredicate){
			MailMessage mailMessage = popServer.waitForMail(testContext, sentMailRef, mailPredicate, timeoutSec);
			return new MailMessageTester<>(() -> mailMessage, returnObj, testContext, new TestEvaluator.Now());
		}

		/**
		 * Fetches a sent email message from the mail server by subject.
		 * @param popServer the PopServer object
		 * @param timeoutSec the timeout in seconds
		 * @return a new MailMessageTester object
		 */
		public MailMessageTester<R> fetchSentBySubject(PopServer popServer, int timeoutSec){
			return fetchSent(popServer, timeoutSec, (sent, filter) -> filter
					.testSubject().equals(sent.getSubject()));
		}

		/**
		 * Fetches a sent email message from the mail server by subject and recipient.
		 * @param popServer the PopServer object
		 * @param timeoutSec the timeout in seconds
		 * @param recipient the recipient of the email
		 * @return a new MailMessageTester object
		 */
		public MailMessageTester<R> fetchSentBySubjectAndRecipient(PopServer popServer, int timeoutSec, String recipient){
			return fetchSent(popServer, timeoutSec, (sent, filter) -> filter
					.testSubject().equals(sent.getSubject())
					.testRecipientsAll().contains(recipient));
		}

		/**
		 * Returns the MailMessage object for the sent email.
		 * @return the MailMessage object for the sent email
		 */
		public MailMessage getMailMessage(){
			return ThrowingCallable.unchecked(sentMailRef).call();
		}

		/**
		 * Stores the sent email in the TestContext object.
		 * @param key the key to store the email under
		 * @return the return object of the MailTester
		 */
		public R store(String key){
			try {
				testContext.store(key, getMailMessage());
				return returnObj;
			}catch (Exception ex){
				throw new RuntimeException("Error: unable to store email [" + getMailMessage().getSubject() + "], from:[" + getMailMessage().getSender() + "], to:" + Arrays.toString(getMailMessage().getRecipientsTo().toArray(new String[0])));
			}
		}

		/**
		 * Closes the email.
		 * @return the return object of the MailTester
		 */
		public R closeMail(){
			return returnObj;
		}
	}

	/**
	 * Functional interface for filtering sent emails.
	 */
	@FunctionalInterface
	public static interface SentMailFilter{
		public void filter(MailMessage sent, MailMessageTester<?> filter);
	}
}