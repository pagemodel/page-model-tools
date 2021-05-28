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
import org.pagemodel.core.utils.TestRuntimeException;
import org.pagemodel.core.utils.ThrowingCallable;
import org.pagemodel.core.utils.ThrowingRunnable;
import org.pagemodel.core.utils.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.pagemodel.core.logging.Logging.*;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public abstract class TestEvaluator {
	private static final Logger plaintextLogger = LoggerFactory.getLogger(TestEvaluator.class.getName() + ".plaintext");
	private static final Logger jsonLogger = LoggerFactory.getLogger(TestEvaluator.class.getName() + ".json");
	private static final Logger htmlLogger = LoggerFactory.getLogger(TestEvaluator.class.getName() + ".html");

	static {
		startLoggers();
	}

	public final static String TEST_ASSERT = "Assert";
	public final static String TEST_EXECUTE = "Execute";
	public final static String TEST_STORE = "Store";
	public final static String TEST_LOAD = "Load";
	public final static String TEST_BUILD = "Build";
	public final static String TEST_FIND = "Find";
	public final static String TEST_ERROR = "Error";
	public final static String TEST_LOG = "Log";

	protected String testType = TEST_ASSERT;
	protected String label;
	protected String actionDisplay;
	protected Consumer<JsonObjectBuilder> eventParams;
	protected List<Consumer<JsonObjectBuilder>> sourceEvents = new LinkedList<>();

	public <T> T testCondition(String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, Callable<Boolean> test, T returnObj, TestContext testContext) {
		return doTest(TEST_ASSERT, actionDisplay, jsonEvent, test, returnObj, testContext);
	}

	public <T> T testExecute(String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, ThrowingRunnable<?> test, T returnObj, TestContext testContext) {
		return testRun(TEST_EXECUTE, actionDisplay, jsonEvent, test, returnObj, testContext);
	}

	public <T> T testRun(String testType, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, ThrowingRunnable<?> test, T returnObj, TestContext testContext) {
		return doTest(testType, actionDisplay, jsonEvent,
				() -> {
					test.run();
					return true;
				},
				returnObj, testContext);
	}

	protected <T> T doTest(String testType, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, Callable<Boolean> test, T returnObj, TestContext testContext) {
		try {
			setTestEventRef(testType, actionDisplay, jsonEvent);
			logEvent(testType, actionDisplay, getEventParams(), getSourceEvents());
			try {
				if (callTest(test)) {
					return returnObj;
				}
			} catch (TestRuntimeException ex) {
				logEvent(testType + "-failed", actionDisplay, getEventParams(), getSourceEvents());
				throw ex;
			} catch (Throwable ex) {
				throw testContext.createException(JsonBuilder.toMap(getAssertEvent(actionDisplay, getEventParams(), getSourceEvents())), ex);
			}
			throw testContext.createException(JsonBuilder.toMap(getAssertEvent(actionDisplay, getEventParams(), getSourceEvents())));
		}finally {
			setSourceFindEvent(null, null);
		}
	}

	public void logMessage(String message){
		logEvent("Log", "log", op -> op.addValue("message", message));
	}

	public void logException(String message, Throwable t){
		logEvent(TEST_ERROR, "exception", op -> op
				.addValue("error", message)
				.merge(exceptionJson(t)));
	}

	public void logException(Throwable t){
		logEvent(TEST_ERROR, "exception", op -> op
				.merge(exceptionJson(t)));
	}
	private Map<String,Object> exceptionJson(Throwable t){
		return exceptionJson(t, true);
	}

	private Map<String,Object> exceptionJson(Throwable t, boolean root){
		JsonObjectBuilder obj = JsonBuilder.object()
				.addValue("type", t.getClass().getName())
				.addValue("message", t.getLocalizedMessage())
				.addValue("exception-obj", t)
				.addArray("stacktrace", arr -> {
					for(StackTraceElement st : t.getStackTrace()){
						StacktraceFilter.MethodMatch match = StacktraceFilter.highlights.matchMethod(st.getClassName(), st.getMethodName());
						String highlight = "none";
						if(match.methodMatch){
							highlight = match.methodHighlight.getHighlight().isEmpty() ? "method" : match.methodHighlight.getHighlight();
						}else if(match.classMatch){
							highlight = match.classHighlight.getHighlight().isEmpty() ? "class" : match.classHighlight.getHighlight();
						}else if(match.packageMatch){
							if(match.classMatchDepth > 0){
								highlight = match.classHighlight.getHighlight().isEmpty() ? "class" : match.classHighlight.getHighlight();
							}else{
								highlight = match.packageHighlight.getHighlight().isEmpty() ? "package" : match.packageHighlight.getHighlight();
							}
						}else if(match.packageMatchDepth > 0 && match.packageHighlight.isSticky()){
							highlight = match.packageHighlight.getHighlight().isEmpty() ? "package" : match.packageHighlight.getHighlight();
						}else if(match.packageMatchDepth >= 3){
							highlight = match.packageHighlight.getHighlight().isEmpty() ? "group" : match.packageHighlight.getHighlight() + "-grp";
						}
						final String highlightVal = highlight;
						arr.addObject(item -> item
								.addValue("hl-" + highlightVal, highlightVal)
								.addValue("class", st.getClassName())
								.addValue("method", st.getMethodName())
								.addValue("file", st.getFileName())
								.addValue("line", st.getLineNumber()));
					}
				});
		if(t.getCause() != null) {
			obj.addValue("cause", exceptionJson(t.getCause(), false));
		}
		return obj.toMap();
	}

	public void logEvent(Consumer<JsonObjectBuilder> jsonEvent){
		if(logPlaintext) {
			log(JsonLogConsoleOut.formatEvent(jsonEvent), null, plaintextLogger);
		}
		if(logHtml) {
			log(JsonLogHtmlOut.formatEvent(jsonEvent), null, htmlLogger);
		}
		if(logJson) {
			log(JsonBuilder.toJsonString(jsonEvent), null, jsonLogger);
		}

	}
	public void logException(Consumer<JsonObjectBuilder> jsonEvent, Throwable t){
		if(logHtml || logJson || logPlaintext) {
			Consumer<JsonObjectBuilder> combined = t == null ? jsonEvent : jsonEvent.andThen(obj -> obj.addValue("exception", exceptionJson(t)));
			if (logPlaintext) {
				log(JsonLogConsoleOut.formatEvent(combined), t, plaintextLogger);
			}
			if (logHtml) {
				log(JsonLogHtmlOut.formatEvent(combined), t, htmlLogger);
			}
			if (logJson) {
				log(JsonBuilder.toJsonString(combined), t, jsonLogger);
			}
		}
	}

	protected void log(String message, Throwable t, Logger logger){
		logger.info(message, t);
	}

	public void logEvent(String type, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent){
		logEvent(type, actionDisplay, jsonEvent, null);
	}

	public void logEvent(String type, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, List<Consumer<JsonObjectBuilder>> sourceEvents){
		logEvent(getEventJson(type, actionDisplay, jsonEvent, sourceEvents));
	}

	public void logException(String type, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, List<Consumer<JsonObjectBuilder>> sourceEvents, Throwable t){
		logException(getEventJson(type, actionDisplay, jsonEvent, sourceEvents), t);
	}

	private static void startLoggers(){
//		jsonLogger.info("[");
		if(logHtml) {
			htmlLogger.info(JsonLogHtmlOut.htmlHeader);
		}
	}

	abstract protected Boolean callTest(Callable<Boolean> test);

	public Quiet quiet(){
		if(Quiet.class.isAssignableFrom(this.getClass())){
			return (Quiet)this;
		}
		return new Quiet(this);
	}

	public String getActionDisplay() {
		return actionDisplay;
	}

	protected void setTestEventRef(String testType, String actionDisplay, Consumer<JsonObjectBuilder> eventParams) {
		this.testType = testType;
		this.actionDisplay = actionDisplay;
		this.eventParams = eventParams;
	}

	public Consumer<JsonObjectBuilder> getEventParams() {
		return eventParams;
	}

	public List<Consumer<JsonObjectBuilder>> getSourceEvents() {
		return sourceEvents;
	}

	public void clearSourceEvents(){
		sourceEvents.clear();
	}

	public void addSourceEvent(String testType, String actionDisplay, Consumer<JsonObjectBuilder> eventParams) {
		if(testType == null || actionDisplay == null || eventParams == null){
			// TODO: remove this when safe
			sourceEvents.clear();
			return;
		}
		this.sourceEvents.add(getEventJson(testType, actionDisplay, eventParams));
	}

	public void setSourceFindEvent(String actionDisplay, Consumer<JsonObjectBuilder> eventParams){
		addSourceEvent(TEST_FIND, actionDisplay, eventParams);
	}

	public Consumer<JsonObjectBuilder> getAssertEvent(String actionDisplay, Consumer<JsonObjectBuilder> eventParams, List<Consumer<JsonObjectBuilder>> sourceEventRefs){
		return getEventJson(TEST_ASSERT, actionDisplay, eventParams, sourceEventRefs);
	}

	public Consumer<JsonObjectBuilder> getAssertEvent(String actionDisplay, Consumer<JsonObjectBuilder> eventParams){
		return getEventJson(TEST_ASSERT, actionDisplay, eventParams);
	}

	public Consumer<JsonObjectBuilder> getExecuteEvent(String actionDisplay, Consumer<JsonObjectBuilder> eventParams){
		return getEventJson(TEST_EXECUTE, actionDisplay, eventParams);
	}

	public Consumer<JsonObjectBuilder> getEventJson(String type, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent) {
		return getEventJson(type, actionDisplay, jsonEvent, null);
	}

	public Consumer<JsonObjectBuilder> getEventJson(String type, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, List<Consumer<JsonObjectBuilder>> sourceEvents){
		return obj -> obj
				.addValue("type", type)
				.addObject("eval", getEvalTypeJson())
				.addObject("op", op -> op
						.addValue("action", actionDisplay)
						.doAdd(jsonEvent))
				.doAdd(op -> {
					if(sourceEvents != null){
						op.addArray("source", arr -> {
							for(Consumer<JsonObjectBuilder> src : sourceEvents){
								arr.addValue(JsonBuilder.toMapRec(src));
							}
						});
					}
				});
	}

	public Consumer<JsonObjectBuilder> getEvalTypeJson(){
		return eval -> eval.addValue("type", label);
	}

	public static class Now extends TestEvaluator {
		public Now() {
			this.label = "now";
		}

		@Override
		protected Boolean callTest(Callable<Boolean> test) {
			return ThrowingCallable.unchecked(test).call();
		}
	}

	public static class Quiet extends TestEvaluator {
		private TestEvaluator testEvaluator;

		public Quiet(TestEvaluator testEvaluator) {
			this.testEvaluator = testEvaluator;
		}

		@Override
		protected void log(String message, Throwable t, Logger logger){
			logger.debug(message, t);
		}

		public TestEvaluator getInnerEvaluator(){
			return testEvaluator;
		}

		@Override
		public Consumer<JsonObjectBuilder> getEventParams() {
			return testEvaluator.getEventParams();
		}

		@Override
		public List<Consumer<JsonObjectBuilder>> getSourceEvents() {
			return testEvaluator.getSourceEvents();
		}

		@Override
		public void clearSourceEvents(){
			testEvaluator.clearSourceEvents();
		}

		@Override
		protected void setTestEventRef(String testType, String actionDisplay, Consumer<JsonObjectBuilder> eventParams) {
			testEvaluator.setTestEventRef(testType, actionDisplay, eventParams);
		}

		@Override
		public void addSourceEvent(String testType, String actionDisplay, Consumer<JsonObjectBuilder> eventParams) {
			testEvaluator.addSourceEvent(testType, actionDisplay, eventParams);
		}

		@Override
		public Consumer<JsonObjectBuilder> getEventJson(String type, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, List<Consumer<JsonObjectBuilder>> sourceEvents) {
			return testEvaluator.getEventJson(type, actionDisplay, jsonEvent, sourceEvents);
		}

		@Override
		public Consumer<JsonObjectBuilder> getEvalTypeJson(){
			return testEvaluator.getEvalTypeJson();
		}

		@Override
		protected Boolean callTest(Callable<Boolean> test) {
			return testEvaluator.callTest(test);
		}
	}

	public static class NoException extends TestEvaluator {
		private TestEvaluator testEvaluator;
		private boolean testStatus = true;

		public NoException(TestEvaluator testEvaluator) {
			this.testEvaluator = testEvaluator;
		}

		@Override
		public <T> T doTest(String testType, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, Callable<Boolean> test, T returnObj, TestContext testContext) {
			try {
				if (!testStatus) {
					return returnObj;
				}
				setTestEventRef(testType, actionDisplay, jsonEvent);
				logEvent(TEST_ASSERT, actionDisplay, getEventParams(), getSourceEvents());
				try {
					if (callTest(test)) {
						return returnObj;
					}
				} catch (Throwable ex) {
				}
				testStatus = false;
				return returnObj;
			}finally {
				setSourceFindEvent(null, null);
			}
		}

		@Override
		protected void log(String message, Throwable t, Logger logger){
			logger.debug(message, t);
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
		public Consumer<JsonObjectBuilder> getEventParams() {
			return testEvaluator.getEventParams();
		}

		@Override
		public List<Consumer<JsonObjectBuilder>> getSourceEvents() {
			return testEvaluator.getSourceEvents();
		}

		@Override
		public void clearSourceEvents(){
			testEvaluator.clearSourceEvents();
		}

		@Override
		protected void setTestEventRef(String testType, String actionDisplay, Consumer<JsonObjectBuilder> eventParams) {
			testEvaluator.setTestEventRef(testType, actionDisplay, eventParams);
		}

		@Override
		public void addSourceEvent(String testType, String actionDisplay, Consumer<JsonObjectBuilder> eventParams) {
			testEvaluator.addSourceEvent(testType, actionDisplay, eventParams);
		}

		@Override
		public Consumer<JsonObjectBuilder> getEventJson(String type, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, List<Consumer<JsonObjectBuilder>> sourceEvents) {
			return testEvaluator.getEventJson(type, actionDisplay, jsonEvent, sourceEvents);
		}

		@Override
		public Consumer<JsonObjectBuilder> getEvalTypeJson(){
			return testEvaluator.getEvalTypeJson();
		}

		@Override
		protected Boolean callTest(Callable<Boolean> test) {
			return testEvaluator.callTest(test);
		}
	}

	public static class LogTests extends TestEvaluator {
		private TestEvaluator testEvaluator;
		protected List<Consumer<JsonObjectBuilder>> logMessages = new LinkedList<>();

		public LogTests(TestEvaluator testEvaluator) {
			this.testEvaluator = testEvaluator;
		}

		@Override
		protected <T> T doTest(String testType, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, Callable<Boolean> test, T returnObj, TestContext testContext) {
			setTestEventRef(testType, actionDisplay, jsonEvent);
			logMessages.add(getAssertEvent(actionDisplay, getEventParams(), getSourceEvents()));
			return returnObj;
		}

		public TestEvaluator getInnerEvaluator(){
			return testEvaluator;
		}

		public List<Consumer<JsonObjectBuilder>> getTestLog(){
			return logMessages;
		}

		@Override
		public Consumer<JsonObjectBuilder> getEventParams() {
			return testEvaluator.getEventParams();
		}

		@Override
		public List<Consumer<JsonObjectBuilder>> getSourceEvents() {
			return testEvaluator.getSourceEvents();
		}

		@Override
		public void clearSourceEvents(){
			testEvaluator.clearSourceEvents();
		}

		@Override
		protected void setTestEventRef(String testType, String actionDisplay, Consumer<JsonObjectBuilder> eventParams) {
			testEvaluator.setTestEventRef(testType, actionDisplay, eventParams);
		}

		@Override
		public void addSourceEvent(String testType, String actionDisplay, Consumer<JsonObjectBuilder> eventParams) {
			testEvaluator.addSourceEvent(testType, actionDisplay, eventParams);
		}

		@Override
		public Consumer<JsonObjectBuilder> getEventJson(String type, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, List<Consumer<JsonObjectBuilder>> sourceEvents) {
			return testEvaluator.getEventJson(type, actionDisplay, jsonEvent, sourceEvents);
		}

		@Override
		public Consumer<JsonObjectBuilder> getEvalTypeJson(){
			return testEvaluator.getEvalTypeJson();
		}

		@Override
		protected Boolean callTest(Callable<Boolean> test) {
			return testEvaluator.callTest(test);
		}
	}
}
