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
 * @author Matt Stevenson [matt@pagemodel.org]
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

	/**
	 * Tests a condition and logs the result as an assertion event. The test is performed by calling the provided Callable<Boolean> object.
	 * If the test returns true, the method returns the provided returnObj. If the test returns false, a TestRuntimeException is thrown.
	 * If an exception is thrown during the test, a TestRuntimeException is thrown with the exception as the cause.
	 * The event is logged using the TEST_ASSERT type, the provided actionDisplay string, and the provided jsonEvent Consumer.
	 * The eventParams and sourceEvents fields are set to the provided values.
	 *
	 * @param actionDisplay a string describing the action being tested
	 * @param jsonEvent a Consumer that adds additional parameters to the event
	 * @param test a Callable<Boolean> object that performs the test
	 * @param returnObj the object to return if the test passes
	 * @param testContext the TestContext object for the current test
	 * @param <T> the type of the return object
	 * @return the provided returnObj if the test passes
	 * @throws TestRuntimeException if the test fails or an exception is thrown during the test
	 */
	public <T> T testCondition(String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, Callable<Boolean> test, T returnObj, TestContext testContext) {
		return doTest(TEST_ASSERT, actionDisplay, jsonEvent, test, returnObj, testContext);
	}

	/**
	 * Executes a test and logs the result as an "Execute" event. The test is represented by a {@link ThrowingRunnable} object,
	 * which is a functional interface that can throw an exception. The test is executed by calling its {@code run()} method.
	 * The result of the test is determined by whether or not an exception is thrown. If an exception is thrown, the test is
	 * considered to have failed. If no exception is thrown, the test is considered to have passed.
	 *
	 * @param actionDisplay a string that describes the action being tested
	 * @param jsonEvent a {@link Consumer} that adds additional information to the event log
	 * @param test a {@link ThrowingRunnable} that represents the test to be executed
	 * @param returnObj the object to be returned if the test passes
	 * @param testContext the {@link TestContext} object that provides context for the test
	 * @param <T> the type of the object to be returned if the test passes
	 * @return the object to be returned if the test passes
	 */
	public <T> T testExecute(String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, ThrowingRunnable<?> test, T returnObj, TestContext testContext) {
		return testRun(TEST_EXECUTE, actionDisplay, jsonEvent, test, returnObj, testContext);
	}

	/**
	 * Runs a test of the specified type with the given action display and JSON event, using the provided test and test context.
	 * The test is executed by calling the provided ThrowingRunnable, and if it returns true, the returnObj is returned.
	 * If the test throws a TestRuntimeException, the event is logged as a failed test and the exception is re-thrown.
	 * If the test throws any other Throwable, an exception is created using the test context and the event is logged as a failed test.
	 * If the test does not return true, an exception is created using the test context and the event is logged as a failed test.
	 * The event is logged using the current test type, action display, and event parameters, along with any source events that have been added.
	 * Finally, the source find event is cleared.
	 *
	 * @param testType the type of test to run
	 * @param actionDisplay the display name of the action being tested
	 * @param jsonEvent the JSON event to log with the test
	 * @param test the test to execute
	 * @param returnObj the object to return if the test passes
	 * @param testContext the context in which the test is being run
	 * @param <T> the type of the return object
	 * @return the return object if the test passes
	 * @throws TestRuntimeException if the test throws a TestRuntimeException
	 * @throws Throwable if the test throws any other Throwable
	 * @throws TestException if the test does not return true
	 */
	public <T> T testRun(String testType, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, ThrowingRunnable<?> test, T returnObj, TestContext testContext) {
		return doTest(testType, actionDisplay, jsonEvent,
				() -> {
					test.run();
					return true;
				},
				returnObj, testContext);
	}

	/**
	 * The doTest method is responsible for executing a test and logging the results. It takes in the test type, action display,
	 * a JSON event, a Callable<Boolean> test, a return object, and a TestContext. It sets the test event reference, logs the event,
	 * and calls the test. If the test passes, it returns the return object. If the test fails, it logs the failure and throws a TestRuntimeException.
	 * If an exception is thrown during the test, it creates an exception event and throws a TestException.
	 * @param testType the type of test being performed
	 * @param actionDisplay a display name for the action being tested
	 * @param jsonEvent a JSON event to be logged
	 * @param test a Callable<Boolean> representing the test to be performed
	 * @param returnObj the object to be returned if the test passes
	 * @param testContext the TestContext in which the test is being performed
	 * @return the return object if the test passes
	 * @throws TestRuntimeException if the test fails
	 * @throws TestException if an exception is thrown during the test
	 */
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

	/**
	 * Logs a message to the test log. The message will be included in the "Log" event type in the test report.
	 * This function can be used to provide additional information about the test execution, such as the current state of the system or any relevant details that may help in debugging.
	 * The message will be logged in all enabled log formats (plaintext, HTML, and JSON) if logging is enabled.
	 *
	 * @param message the message to be logged
	 */
	public void logMessage(String message){
		logEvent("Log", "log", op -> op.addValue("message", message));
	}

	/**
	 * Logs an exception with a custom error message.
	 * This method is useful when an exception is caught and needs to be logged with a custom error message.
	 * The error message and exception are logged to the configured loggers (plaintext, JSON, and/or HTML).
	 *
	 * @param message the error message to log
	 * @param t the exception to log
	 *
	 * @see #logException(Consumer, Throwable)
	 */
	public void logException(String message, Throwable t){
		logEvent(TEST_ERROR, "exception", op -> op
				.addValue("error", message)
				.merge(exceptionJson(t)));
	}
	/**
	 * Logs an exception with a default message and the given Throwable object.
	 * The exception is logged to the configured loggers (plaintext, JSON, and/or HTML).
	 *
	 * @param t the exception to log
	 */
	public void logException(Throwable t){
		logEvent(TEST_ERROR, "exception", op -> op
				.merge(exceptionJson(t)));
	}

	/**
	 * Returns a map containing information about the given Throwable object, including its type, message, stack trace, and cause (if any).
	 * If the 'root' parameter is true, the entire stack trace will be included; otherwise, only the stack trace elements that are not caused by another exception will be included.
	 * The stack trace elements are filtered based on the configured StacktraceFilter, which can highlight certain packages, classes, or methods.
	 * The resulting map can be used to create a JSON object for logging purposes.
	 * @param t the Throwable object to extract information from
	 * @param root whether to include the entire stack trace or only the top-level elements
	 * @return a map containing information about the given Throwable object
	 */
	private Map<String,Object> exceptionJson(Throwable t){
		return exceptionJson(t, true);
	}

	/**
	 * Returns a map containing information about the given Throwable object and its stack trace.
	 * If the 'root' parameter is true, the map will also include information about the Throwable's cause.
	 * The map includes the type and message of the Throwable, the Throwable object itself, and an array of
	 * stack trace elements. Each stack trace element includes the class name, method name, file name, and line number,
	 * as well as a highlight value indicating whether the element matches a certain pattern.
	 * If the Throwable has a cause and 'root' is true, the map will also include a nested map containing information
	 * about the cause.
	 * @param t the Throwable object to generate the map for
	 * @param root whether to include information about the Throwable's cause
	 * @return a map containing information about the Throwable and its stack trace
	 */
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

	/**
	 * Logs an event with the given JSON object builder. The event is logged to all enabled loggers (plaintext, JSON, and HTML).
	 * If logging is not enabled for any of the loggers, the event is not logged.
	 *
	 * @param jsonEvent the JSON object builder representing the event to be logged
	 */
	public void logEvent(Consumer<JsonObjectBuilder> jsonEvent){
		if(logHtml || logJson || logPlaintext) {
			Map<String,Object> event = JsonBuilder.toMap(jsonEvent);
			if (logPlaintext) {
				log(JsonLogConsoleOut.formatEvent(event), null, plaintextLogger);
			}
			if (logHtml) {
				log(JsonLogHtmlOut.formatEvent(event), null, htmlLogger);
			}
			if (logJson) {
				log(JsonBuilder.toJsonString(event), null, jsonLogger);
			}
		}
	}
	/**
	 * Logs an exception event with the given JSON event and throwable.
	 * If logging is enabled for plaintext, HTML, or JSON, the event will be formatted and logged to the appropriate logger(s).
	 * The JSON event can be customized using a Consumer that accepts a JsonObjectBuilder.
	 * If a throwable is provided, its details will be included in the logged event.
	 *
	 * @param jsonEvent a Consumer that accepts a JsonObjectBuilder to customize the JSON event
	 * @param t the throwable to include in the logged event, or null if no throwable should be included
	 */
	public void logException(Consumer<JsonObjectBuilder> jsonEvent, Throwable t){
		if(logHtml || logJson || logPlaintext) {
			Consumer<JsonObjectBuilder> combined = t == null ? jsonEvent : jsonEvent.andThen(obj -> obj.addValue("exception", exceptionJson(t)));
			Map<String,Object> event = JsonBuilder.toMap(combined);
			if (logPlaintext) {
				log(JsonLogConsoleOut.formatEvent(event), t, plaintextLogger);
			}
			if (logHtml) {
				log(JsonLogHtmlOut.formatEvent(event), t, htmlLogger);
			}
			if (logJson) {
				log(JsonBuilder.toJsonString(event), t, jsonLogger);
			}
		}
	}

	/**
	 * Logs a message and an optional Throwable to the specified Logger.
	 *
	 * @param message the message to log
	 * @param t the Throwable to log (can be null)
	 * @param logger the Logger to use for logging
	 */
	protected void log(String message, Throwable t, Logger logger){
		logger.info(message, t);
	}
	/**
	 * Logs an event with the given type, action display, and JSON event.
	 * If logging is enabled for plaintext, HTML, or JSON, the event will be logged in those formats.
	 * @param type the type of the event
	 * @param actionDisplay a display name for the action being logged
	 * @param jsonEvent a JSON object builder containing additional information about the event
	 */
	public void logEvent(String type, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent){
		logEvent(type, actionDisplay, jsonEvent, null);
	}
	/**
	 * Logs an event with the given type, action display, JSON event, and list of source events.
	 * The JSON event and source events are represented as Consumer<JsonObjectBuilder> objects.
	 * If logging is enabled for plaintext, HTML, or JSON, the event will be logged in the appropriate format.
	 * @param type the type of the event
	 * @param actionDisplay a display name for the action being performed
	 * @param jsonEvent a Consumer<JsonObjectBuilder> representing the JSON event to be logged
	 * @param sourceEvents a list of Consumer<JsonObjectBuilder> objects representing the source events for the current event
	 */
	public void logEvent(String type, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, List<Consumer<JsonObjectBuilder>> sourceEvents){
		logEvent(getEventJson(type, actionDisplay, jsonEvent, sourceEvents));
	}

	/**
	 * Logs an event with the given type, action display, JSON event, list of source events, and exception.
	 * The JSON event and source events are represented as Consumer<JsonObjectBuilder> objects.
	 * If logging is enabled for plaintext, HTML, or JSON, the event will be logged in the appropriate format.
	 * @param type the type of the event
	 * @param actionDisplay a display name for the action being performed
	 * @param jsonEvent a Consumer<JsonObjectBuilder> representing the JSON event to be logged
	 * @param sourceEvents a list of Consumer<JsonObjectBuilder> objects representing the source events for the current event
	 * @param t the exception to log
	 */
	public void logException(String type, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, List<Consumer<JsonObjectBuilder>> sourceEvents, Throwable t){
		logException(getEventJson(type, actionDisplay, jsonEvent, sourceEvents), t);
	}

	/**
	 * Initializes the HTML logger and writes the HTML header to the log file if logging in HTML format is enabled.
	 */
	private static void startLoggers(){
//		jsonLogger.info("[");
		if(logHtml) {
			htmlLogger.info(JsonLogHtmlOut.htmlHeader);
		}
	}
	/**
	 * This method is called by the public test functions to execute the actual test logic.
	 * It takes a Callable<Boolean> as input, which represents the test logic to be executed.
	 * The method returns a Boolean value indicating whether the test passed or failed.
	 * Typically, any exceptions thrown during the test execution are caught and re-thrown as TestRuntimeExceptions.  The
	 * NoException subclass will catch and ignore all exceptions, setting a failed flag to true.
	 *
	 * @param test A Callable<Boolean> representing the test logic to be executed.
	 * @return A Boolean value indicating whether the test passed or failed.
	 * @throws TestRuntimeException If an exception is thrown during the test execution.
	 */
	abstract protected Boolean callTest(Callable<Boolean> test);

	public Quiet quiet(){
		if(Quiet.class.isAssignableFrom(this.getClass())){
			return (Quiet)this;
		}
		return new Quiet(this);
	}
	/**
	 * Returns the display name of the action being tested.
	 * @return the display name of the action being tested
	 */
	public String getActionDisplay() {
		return actionDisplay;
	}
	/**
	 * Sets the test event reference with the given test type, action display, and event parameters.
	 *
	 * @param testType the type of the test, which can be one of the following:
	 * {@link #TEST_ASSERT}, {@link #TEST_EXECUTE}, {@link #TEST_STORE}, {@link #TEST_LOAD},
	 * {@link #TEST_BUILD}, {@link #TEST_FIND}, {@link #TEST_ERROR}, or {@link #TEST_LOG}
	 * @param actionDisplay the display name of the action being tested
	 * @param eventParams the event parameters to be included in the test event
	 */
	protected void setTestEventRef(String testType, String actionDisplay, Consumer<JsonObjectBuilder> eventParams) {
		this.testType = testType;
		this.actionDisplay = actionDisplay;
		this.eventParams = eventParams;
	}
	/**
	 * Returns the event parameters as a Consumer of JsonObjectBuilder.
	 * These parameters are used to build the JSON representation of the event.
	 * @return a Consumer of JsonObjectBuilder representing the event parameters.
	 */
	public Consumer<JsonObjectBuilder> getEventParams() {
		return eventParams;
	}
	/**
	 * Returns a list of source events that were recorded during the test execution.
	 * Each source event is represented as a Consumer of JsonObjectBuilder.
	 * These events are used to provide context for the main test event.
	 * If no source events were recorded, an empty list is returned.
	 */
	public List<Consumer<JsonObjectBuilder>> getSourceEvents() {
		return sourceEvents;
	}
	/**
	 * Clears the list of source events.
	 */
	public void clearSourceEvents(){
		sourceEvents.clear();
	}
	/**
	 * Adds a source event to the list of source events for the current test. A source event is an event that led to the current test being executed.
	 *
	 * @param testType The type of the source event.
	 * @param actionDisplay A display name for the source event.
	 * @param eventParams A consumer that adds parameters to the source event.
	 */
	public void addSourceEvent(String testType, String actionDisplay, Consumer<JsonObjectBuilder> eventParams) {
		if(testType == null || actionDisplay == null || eventParams == null){
			// TODO: remove this when safe
			sourceEvents.clear();
			return;
		}
		this.sourceEvents.add(getEventJson(testType, actionDisplay, eventParams));
	}
	/**
	 * Adds a source event with type "Find" and the given action display and event parameters.
	 * This is typically used to indicate the location where a value was found during a test.
	 * @param actionDisplay a string describing the action being performed
	 * @param eventParams a consumer function that adds parameters to the event JSON object
	 */
	public void setSourceFindEvent(String actionDisplay, Consumer<JsonObjectBuilder> eventParams){
		addSourceEvent(TEST_FIND, actionDisplay, eventParams);
	}
	/**
	 * Returns a Consumer that represents an Assert event with the given action display and event parameters,
	 * along with a list of source event references.
	 *
	 * @param actionDisplay the display name of the action being asserted
	 * @param eventParams the event parameters to include in the Assert event
	 * @param sourceEventRefs a list of source event references to include in the Assert event
	 * @return a Consumer that represents an Assert event with the given parameters and source event references
	 */
	public Consumer<JsonObjectBuilder> getAssertEvent(String actionDisplay, Consumer<JsonObjectBuilder> eventParams, List<Consumer<JsonObjectBuilder>> sourceEventRefs){
		return getEventJson(TEST_ASSERT, actionDisplay, eventParams, sourceEventRefs);
	}
	/**
	 * Returns a Consumer that represents an Assert event with the given action display and event parameters.
	 *
	 * @param actionDisplay the display name of the action being asserted
	 * @param eventParams the event parameters to include in the event
	 * @return a Consumer that represents the Assert event
	 */
	public Consumer<JsonObjectBuilder> getAssertEvent(String actionDisplay, Consumer<JsonObjectBuilder> eventParams){
		return getEventJson(TEST_ASSERT, actionDisplay, eventParams);
	}
	/**
	 * Returns a Consumer that represents an Execute event with the given action display and event parameters.
	 *
	 * @param actionDisplay the display name of the action being executed
	 * @param eventParams the event parameters to include in the event
	 * @return a Consumer that represents the Execute event
	 */
	public Consumer<JsonObjectBuilder> getExecuteEvent(String actionDisplay, Consumer<JsonObjectBuilder> eventParams){
		return getEventJson(TEST_EXECUTE, actionDisplay, eventParams);
	}
	/**
	 * Returns a Consumer that represents an event with the giventype,  action display and event parameters.
	 *
	 * @param type the event type
	 * @param actionDisplay the display name of the event
	 * @param eventParams the event parameters to include in the event
	 * @return a Consumer that represents the event
	 */
	public Consumer<JsonObjectBuilder> getEventJson(String type, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent) {
		return getEventJson(type, actionDisplay, jsonEvent, null);
	}
	/**
	 * Returns a Consumer that represents an event with the giventype,  action display and event parameters.
	 *
	 * @param type the type of the event
	 * @param actionDisplay the display name of the action
	 * @param jsonEvent the JSON event to include in the event object
	 * @param sourceEvents a list of source events to include in the event object, or null if there are no source events
	 * @return a Consumer that builds a JSON object representing an event
	 */
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

	/**
	 * Returns a JSON object representing the evaluation type of this TestEvaluator instance.
	 * The returned object contains a "type" field with the value of the "label" field of this instance.
	 * This method is intended to be overridden by subclasses to provide additional information about the evaluation type.
	 * @return a JSON object representing the evaluation type of this TestEvaluator instance
	 */
	public Consumer<JsonObjectBuilder> getEvalTypeJson(){
		return eval -> eval.addValue("type", label);
	}
	/**
	 * A subclass of TestEvaluator that immediately evaluates a test when it is called.
	 * The test is executed using the ThrowingCallable utility class, which allows for the test to throw checked exceptions.
	 */
	public static class Now extends TestEvaluator {
		public Now() {
			this.label = "now";
		}
		/**
		 * Executes the given test and returns the result.
		 *
		 * @param test the test to execute
		 * @return the result of the test
		 * @throws TestRuntimeException if the test throws an exception
		 */
		@Override
		protected Boolean callTest(Callable<Boolean> test) {
			return ThrowingCallable.unchecked(test).call();
		}
	}

	/**
	 * A subclass of TestEvaluator that wraps a TestEvaluator and logs at the Debug level instead of Info level.
	 * By default, the Debug level is ignored by the loggers.
	 * This class provides a way to perform tests quietly without generating excessive log output.
	 */
	public static class Quiet extends TestEvaluator {
		private TestEvaluator testEvaluator;

		public Quiet(TestEvaluator testEvaluator) {
			this.testEvaluator = testEvaluator;
		}
		/**
		 * Logs the event to the appropriate logger based on the format specified.
		 * Overrides the base implementation to log events at the Debug level instead of Info level.
		 * By default, the Debug level is ignored by the loggers.
		 *
		 * @param message the message to log
		 * @param t the throwable to log
		 * @param logger the logger to use for logging
		 */
		@Override
		protected void log(String message, Throwable t, Logger logger){
			logger.debug(message, t);
		}
		/**
		 * Returns the inner evaluator object that this NoException object wraps.
		 *
		 * @return the inner evaluator object
		 */
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
		/**
		 * Performs the test and logs the event. Ignores any exceptions and continues.  If a previous test has failed, the current test is skipped.
		 *
		 * @param testType the type of test being performed
		 * @param actionDisplay a string that describes the action being performed
		 * @param jsonEvent a Consumer that takes in a JsonObjectBuilder and adds event parameters to it
		 * @param test a Callable that performs the test
		 * @param returnObj an object that is returned if the test passes
		 * @param testContext a TestContext object that provides context for the test
		 * @param <T>           the type of the return object
		 * @return the returnObj
		 */
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
		/**
		 * Logs the event to the appropriate logger based on the format specified.
		 * Overrides the base implementation to log events at the Debug level instead of Info level.
		 * By default, the Debug level is ignored by the loggers.
		 *
		 * @param message the message to log
		 * @param t the throwable to log
		 * @param logger the logger to use for logging
		 */
		@Override
		protected void log(String message, Throwable t, Logger logger){
			logger.debug(message, t);
		}
		/**
		 * Returns the inner evaluator object that this NoException object wraps.
		 *
		 * @return the inner evaluator object
		 */
		public TestEvaluator getInnerEvaluator(){
			return testEvaluator;
		}
		/**
		 * Returns the current status of the test. If the test has not been executed or has passed, the status is true.
		 * If the test has failed, the status is false.
		 *
		 * @return the current status of the test
		 */
		public boolean getTestStatus() {
			return testStatus;
		}
		/**
		 * Sets the status of the test.
		 *
		 * @param testStatus a boolean value indicating whether the test has passed or failed
		 */
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
	/**
	 * A TestEvaluator wrapper that logs events for tests but does not actually evaluate them.
	 */
	public static class LogTests extends TestEvaluator {
		private TestEvaluator testEvaluator;
		protected List<Consumer<JsonObjectBuilder>> logMessages = new LinkedList<>();

		public LogTests(TestEvaluator testEvaluator) {
			this.testEvaluator = testEvaluator;
		}
		/**
		 * This method logs a test event and returns the return object without actually evaluating the test.
		 * The event is added to the list of log messages.
		 * This implementation does not evaluate the test, it will always pass and the specified return object will always be returned.
		 *
		 * @param testType the type of test being performed
		 * @param actionDisplay a string describing the action being performed
		 * @param jsonEvent a consumer that adds event parameters to a JSON object builder
		 * @param test the test to be performed (not actually evaluated in this implementation)
		 * @param returnObj the object to be returned
		 * @param testContext the context for the test
		 * @param <T> the type of the return object
		 * @return the specified return object
		 */
		@Override
		protected <T> T doTest(String testType, String actionDisplay, Consumer<JsonObjectBuilder> jsonEvent, Callable<Boolean> test, T returnObj, TestContext testContext) {
			setTestEventRef(testType, actionDisplay, jsonEvent);
			logMessages.add(getAssertEvent(actionDisplay, getEventParams(), getSourceEvents()));
			return returnObj;
		}
		/**
		 * Returns the inner evaluator object that this NoException object wraps.
		 *
		 * @return the inner evaluator object
		 */
		public TestEvaluator getInnerEvaluator(){
			return testEvaluator;
		}
		/**
		 * Returns a list of all the logged events in the form of a list of {@code Consumer<JsonObjectBuilder>} objects.
		 * Each object represents an event and can be used to build a JSON object using a {@code JsonObjectBuilder}.
		 * The list contains events logged by all the tests performed by this {@code LogTests} object.
		 * @return a list of all the logged events
		 */
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
