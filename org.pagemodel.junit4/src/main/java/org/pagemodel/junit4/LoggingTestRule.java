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

package org.pagemodel.junit4;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.recovery.ResilientFileOutputStream;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.TestRuntimeException;
import org.pagemodel.core.utils.Unique;
import org.pagemodel.core.utils.json.StacktraceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.Iterator;

public class LoggingTestRule implements MethodRule {
	private static final Logger log = LoggerFactory.getLogger(LoggingTestRule.class.getName());
	private static SynchronizedCounter testQueueCount = new SynchronizedCounter();
	private static SynchronizedCounter testStartCount = new SynchronizedCounter();
	private static SynchronizedCounter testEndCount = new SynchronizedCounter();
	private static SynchronizedCounter testFailCount = new SynchronizedCounter();
	private static SynchronizedCounter testPassCount = new SynchronizedCounter();
	private static Date firstStart = new Date();
	private static boolean loggedReportPath = false;
	private static String reportPath = null;

	private TestEvaluator evalLogger = new TestEvaluator.Now();
	private FrameworkMethod method;

	public LoggingTestRule(){
		startLoggers();
	}

	private static void startLoggers(){
		if(loggedReportPath){
			return;
		}
		LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();

		for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
			for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext();) {
				Appender<ILoggingEvent> appender = index.next();
				if(!appender.getName().equals("HTML")){
					continue;
				}
				if (appender instanceof FileAppender) {
					FileAppender<ILoggingEvent> fa = (FileAppender<ILoggingEvent>)appender;
					ResilientFileOutputStream rfos = (ResilientFileOutputStream)fa.getOutputStream();
					File file = rfos.getFile();
					log.info("Html Test Report: file://" + file.getAbsolutePath());
					reportPath = file.getAbsolutePath();
					loggedReportPath = true;
					return;
				}
			}
		}
	}

	@Override
	public Statement apply(Statement base, FrameworkMethod method, Object target) {
		this.method = method;
		testQueueCount.increment();
		StacktraceFilter.highlights.addMethodHighlight(method.getMethod());

		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				int count = testStartCount.increment();
				if(count == 1){
					firstStart = new Date();
				}
				String testId = Unique.shortString();
				Date start = new Date();
				evalLogger.logEvent("Test", "start", op -> op
								.addValue("class", method.getDeclaringClass().getName())
								.addValue("method", method.getName())
								.addValue("count", count)
								.addValue("total", testQueueCount.getCount())
								.addValue("start", start)
								.addValue("testId", testId));
				try {
					base.evaluate();
				}catch (Throwable t){
					logTestEnd(start, "fail", testId, count);
					if(!(t instanceof TestRuntimeException)){
						evalLogger.logException(t);
					}
					throw t;
				}
				logTestEnd(start, "pass", testId, count);
			}
		};
	}

	private void logTestEnd(Date start, String status, String testId, int count){
		Date end = new Date();
		long durationMsec = end.getTime() - start.getTime();
		int endCount = testEndCount.increment();
		if(status.equals("pass")){
			testPassCount.increment();
		}else if(status.equals("fail")){
			testFailCount.increment();
			log.info("Html Test Report: file://" + reportPath);
		}
		evalLogger.logEvent("Test", "end", op -> op
						.addValue("class", method.getDeclaringClass().getName())
						.addValue("method", method.getName())
						.addValue("testId", testId)
						.addValue("count", endCount)
						.addValue("total", testQueueCount.getCount())
						.addValue("duration", durationMsec)
						.addValue("end", end));
		evalLogger.logEvent("Test", status, op -> op
						.addValue("class", method.getDeclaringClass().getName())
						.addValue("method", method.getName())
						.addValue("testId", testId)
						.addValue("duration", durationMsec)
						.addValue("start", start)
						.addValue("end", end));

		long totalTimeMsec = end.getTime() - firstStart.getTime();
		evalLogger.logEvent("Test", "summary", op -> op
				.addValue("pass", testPassCount.getCount())
				.addValue("fail", testFailCount.getCount())
				.addValue("total", testQueueCount.getCount())
				.addValue("duration", totalTimeMsec)
				.addValue("start", firstStart)
				.addValue("end", end));
	}

	public String getDeclaringClass(){
		return method.getDeclaringClass().getName();
	}

	public String getMethod(){
		return method.getName();
	}

	static class SynchronizedCounter {
		private int count = 0;

		synchronized int getCount(){
			return count;
		}

		synchronized int increment(){
			return ++count;
		}
	}
}
