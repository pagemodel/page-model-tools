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
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class MailTester<R> {

	protected TestContext testContext;
	protected R returnObj;
	private TestEvaluator testEvaluator;

	public MailTester(TestContext testContext, R returnObj, TestEvaluator testEvaluator) {
		this.testContext = testContext;
		this.returnObj = returnObj;
		this.testEvaluator = testEvaluator;
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	public MailMessageBuilder<R> composeMail(){
		getEvaluator().logEvent(TestEvaluator.TEST_EXECUTE, "compose mail", null);
		return new MailMessageBuilder<>(returnObj, testContext, getEvaluator());
	}

	public R closeMail(){
		return returnObj;
	}

	public MailMessageTester<R> test(MailMessage mailMessage){
		return new MailMessageTester<R>(() -> mailMessage, returnObj, testContext, new TestEvaluator.Now());
	}

	public MailMessageTester<R> fetchMail(PopServer popServer, int timeoutSec, ThrowingConsumer<MailMessageTester<?>, ?> mailPredicate){
		MailMessage mailMessage = popServer.waitForMail(testContext, mailPredicate, timeoutSec);
		return new MailMessageTester<>(() -> mailMessage, returnObj, testContext, new TestEvaluator.Now());
	}

	public MailMessageTester<R> fetchMail(PopServer popServer, int timeoutSec, String subject, String recipient){
		return fetchMail(popServer, timeoutSec, mail -> mail
				.testSubject().equals(subject)
				.testRecipientsAll().contains(recipient));
	}

	public MailMessageTester<R> fetchMail(PopServer popServer, int timeoutSec, String subject){
		return fetchMail(popServer, timeoutSec, mail -> mail
				.testSubject().equals(subject));
	}

	public R mailNotFound(PopServer popServer, int timeoutSec, ThrowingConsumer<MailMessageTester<?>, ?> mailPredicate) {
		popServer.waitForMailNotFound(testContext, mailPredicate, timeoutSec);
		return returnObj;
	}

	public R mailNotFound(PopServer popServer, int timeoutSec, String subject, String recipient){
		return mailNotFound(popServer, timeoutSec, mail -> mail
				.testSubject().equals(subject)
				.testRecipientsAll().contains(recipient));
	}

	public R mailNotFound(PopServer popServer, int timeoutSec, String subject){
		return mailNotFound(popServer, timeoutSec, mail -> mail
				.testSubject().equals(subject));
	}

	public static class SentMailTester<R> extends MailTester<R> {
		private Callable<MailMessage> sentMailRef;

		public SentMailTester(Callable<MailMessage> sentMailRef, R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
			super(testContext, returnObj, testEvaluator);
			this.sentMailRef = sentMailRef;
		}

		public MailMessageTester<R> fetchSent(PopServer popServer, int timeoutSec, SentMailFilter mailPredicate){
			MailMessage mailMessage = popServer.waitForMail(testContext, sentMailRef, mailPredicate, timeoutSec);
			return new MailMessageTester<>(() -> mailMessage, returnObj, testContext, new TestEvaluator.Now());
		}

		public MailMessageTester<R> fetchSentBySubject(PopServer popServer, int timeoutSec){
			return fetchSent(popServer, timeoutSec, (sent, filter) -> filter
					.testSubject().equals(sent.getSubject()));
		}

		public MailMessageTester<R> fetchSentBySubjectAndRecipient(PopServer popServer, int timeoutSec, String recipient){
			return fetchSent(popServer, timeoutSec, (sent, filter) -> filter
					.testSubject().equals(sent.getSubject())
					.testRecipientsAll().contains(recipient));
		}

		public MailMessage getMailMessage(){
			return ThrowingCallable.unchecked(sentMailRef).call();
		}

		public R store(String key){
			try {
				testContext.store(key, getMailMessage());
				return returnObj;
			}catch (Exception ex){
				throw new RuntimeException("Error: unable to store email [" + getMailMessage().getSubject() + "], from:[" + getMailMessage().getSender() + "], to:" + Arrays.toString(getMailMessage().getRecipientsTo().toArray(new String[0])));
			}
		}

		public R closeMail(){
			return returnObj;
		}
	}

	@FunctionalInterface
	public static interface SentMailFilter{
		public void filter(MailMessage sent, MailMessageTester<?> filter);
	}
}
