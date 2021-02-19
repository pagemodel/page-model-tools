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

package org.pagemodel.core.testers;

import org.pagemodel.core.TestContext;
import org.pagemodel.core.utils.ThrowingCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public abstract class TestEvaluator implements Function<Callable<Boolean>,Boolean> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	protected String label;
	protected Callable<String> testMessageRef;
	protected Callable<String> sourceDisplay;

	public <T> T testCondition(Callable<String> messageRef, Callable<Boolean> test, T returnObj, TestContext testContext) {
		setTestMessageRef(messageRef);
		log.info(getTestMessage(getTestMessageRef(), getSourceDisplayRef()));
		try {
			if(apply(test)){
				return returnObj;
			}
		} catch (Throwable ex) {
			throw testContext.createException(getTestMessage(getTestMessageRef(), getSourceDisplayRef()), ex);
		}
		throw testContext.createException(getTestMessage(getTestMessageRef(), getSourceDisplayRef()));
	}

	public Quiet quiet(){
		if(Quiet.class.isAssignableFrom(this.getClass())){
			return (Quiet)this;
		}
		return new Quiet(this);
	}

	public Callable<String> getTestMessageRef() {
		return testMessageRef;
	}

	protected void setTestMessageRef(Callable<String> testMessageRef) {
		this.testMessageRef = testMessageRef;
	}

	public Callable<String> getSourceDisplayRef() {
		return sourceDisplay;
	}

	public void setSourceDisplayRef(Callable<String> sourceDisplay) {
		this.sourceDisplay = sourceDisplay;
	}

	public String getFindMessage(Callable<String> messageRef) {
		return "Find" + getLabel() + " " + ThrowingCallable.nullOnError(messageRef).call();
	}

	public String getTestMessage(Callable<String> messageRef){
		return getTestMessage(messageRef, sourceDisplay);
	}

	public String getTestMessage(Callable<String> messageRef, Callable<String> sourceRef){
		StringBuilder sb = new StringBuilder();
		if (sourceRef != null) {
			sb.append(String.format("Find%s %s\n%17s",getLabel(),ThrowingCallable.nullOnError(sourceRef).call(),"\t"));
		}
		sb.append(String.format("Assert%s %s", getLabel(), ThrowingCallable.nullOnError(messageRef).call()));
		return sb.toString();
	}

	public String getActionMessage(Callable<String> messageRef){
		return "Execute" + getLabel() + " " + ThrowingCallable.nullOnError(messageRef).call();
	}

	public String getLabel() {
		return label;
	}

	public static class Now extends  TestEvaluator {
		public Now() {
			this.label = "";
		}

		@Override
		public Boolean apply(Callable<Boolean> test) {
			return ThrowingCallable.unchecked(test).call();
		}
	}

	public static class Quiet extends TestEvaluator {
		private TestEvaluator testEvaluator;

		public Quiet(TestEvaluator testEvaluator) {
			this.testEvaluator = testEvaluator;
		}

		public <T> T testCondition(Callable<String> messageRef, Callable<Boolean> test, T returnObj, TestContext testContext) {
			setTestMessageRef(messageRef);
			log.debug(getTestMessage(getTestMessageRef(), getSourceDisplayRef()));
			try {
				if(apply(test)){
					return returnObj;
				}
			} catch (Throwable ex) {
				throw testContext.createException(getTestMessage(getTestMessageRef()), ex);
			}
			throw testContext.createException(getTestMessage(getTestMessageRef()));
		}

		public TestEvaluator getInnerEvaluator(){
			return testEvaluator;
		}

		@Override
		public Callable<String> getTestMessageRef() {
			return testEvaluator.getTestMessageRef();
		}

		@Override
		protected void setTestMessageRef(Callable<String> testMessageRef) {
			testEvaluator.setTestMessageRef(testMessageRef);
		}

		@Override
		public String getTestMessage(Callable<String> messageRef) {
			return testEvaluator.getTestMessage(messageRef);
		}

		@Override
		public String getActionMessage(Callable<String> messageRef) {
			return testEvaluator.getActionMessage(messageRef);
		}

		@Override
		public String getLabel() {
			return testEvaluator.getLabel();
		}

		@Override
		public Boolean apply(Callable<Boolean> test) {
			return testEvaluator.apply(test);
		}
	}

	public static class NoException extends TestEvaluator {
		private TestEvaluator testEvaluator;
		private boolean testStatus = true;

		public NoException(TestEvaluator testEvaluator) {
			this.testEvaluator = testEvaluator;
		}

		public <T> T testCondition(Callable<String> messageRef, Callable<Boolean> test, T returnObj, TestContext testContext) {
			if(!testStatus){
				return returnObj;
			}
			setTestMessageRef(messageRef);
			log.debug(getTestMessage(getTestMessageRef(), getSourceDisplayRef()));
			try {
				if(apply(test)){
					return returnObj;
				}
			} catch (Throwable ex) { }
			testStatus = false;
			return returnObj;
		}

		public TestEvaluator getInnerEvaluator(){
			return testEvaluator;
		}

		public boolean getTestStatus() {
			return testStatus;
		}

		public void setTestStatus(boolean testStatus) {
			this.testStatus = testStatus;
		}

		@Override
		public Callable<String> getTestMessageRef() {
			return testEvaluator.getTestMessageRef();
		}

		@Override
		protected void setTestMessageRef(Callable<String> testMessageRef) {
			testEvaluator.setTestMessageRef(testMessageRef);
		}

		@Override
		public String getTestMessage(Callable<String> messageRef) {
			return testEvaluator.getTestMessage(messageRef);
		}

		@Override
		public String getActionMessage(Callable<String> messageRef) {
			return testEvaluator.getActionMessage(messageRef);
		}

		@Override
		public String getLabel() {
			return testEvaluator.getLabel();
		}

		@Override
		public Boolean apply(Callable<Boolean> test) {
			return testEvaluator.apply(test);
		}
	}

	public static class LogTests extends TestEvaluator {
		private TestEvaluator testEvaluator;
		private StringBuilder testLog = new StringBuilder();

		public LogTests(TestEvaluator testEvaluator) {
			this.testEvaluator = testEvaluator;
		}

		public <T> T testCondition(Callable<String> messageRef, Callable<Boolean> test, T returnObj, TestContext testContext) {
			setTestMessageRef(messageRef);
			testLog.append(getTestMessage(getTestMessageRef(), getSourceDisplayRef())).append("\n");
			return returnObj;
		}

		public TestEvaluator getInnerEvaluator(){
			return testEvaluator;
		}

		public String getTestLog(){
			return testLog.toString();
		}

		@Override
		public Callable<String> getTestMessageRef() {
			return testEvaluator.getTestMessageRef();
		}

		@Override
		protected void setTestMessageRef(Callable<String> testMessageRef) {
			testEvaluator.setTestMessageRef(testMessageRef);
		}

		@Override
		public String getTestMessage(Callable<String> messageRef) {
			return testEvaluator.getTestMessage(messageRef);
		}

		@Override
		public String getActionMessage(Callable<String> messageRef) {
			return testEvaluator.getActionMessage(messageRef);
		}

		@Override
		public String getLabel() {
			return testEvaluator.getLabel();
		}

		@Override
		public Boolean apply(Callable<Boolean> test) {
			return testEvaluator.apply(test);
		}
	}
}
