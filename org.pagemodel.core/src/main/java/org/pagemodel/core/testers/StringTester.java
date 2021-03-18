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
import org.pagemodel.core.utils.ThrowingFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class StringTester<R> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected R returnObj;
	protected final Callable<String> ref;
	protected final TestContext testContext;
	private TestEvaluator testEvaluator;

	public StringTester(Callable<String> ref, R returnObj, TestContext testContext) {
		this(ref, returnObj, testContext, new TestEvaluator.Now());
	}

	public StringTester(Callable<String> ref, R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	protected String callRef() {
		try {
			return ref.call();
		} catch (Exception ex) {
			return "";
		}
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	public R contains(String string) {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] contains [" + string + "]",
				() -> callRef().contains(string), returnObj, testContext);
	}

	public R notContains(String string) {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] not contains [" + string + "]",
				() -> !callRef().contains(string), returnObj, testContext);
	}

	public R equals(String string) {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] equals [" + string + "]",
				() -> callRef().equals(string), returnObj, testContext);
	}

	public R notEquals(String string) {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] not equals [" + string + "]",
				() -> !callRef().equals(string), returnObj, testContext);
	}

	public R matches(String regex) {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] matches regex [" + regex + "]",
				() -> callRef().matches(regex), returnObj, testContext);
	}

	public R notMatches(String regex) {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] not matches regex [" + regex + "]",
				() -> !callRef().matches(regex), returnObj, testContext);
	}

	public R startsWith(String string) {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] starts with [" + string + "]",
				() -> callRef().startsWith(string), returnObj, testContext);
	}

	public R notStartsWith(String string) {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] not starts with [" + string + "]",
				() -> !callRef().startsWith(string), returnObj, testContext);
	}

	public R endsWith(String string) {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] ends with [" + string + "]",
				() -> callRef().endsWith(string), returnObj, testContext);
	}

	public R notEndsWith(String string) {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] not ends with [" + string + "]",
				() -> !callRef().endsWith(string), returnObj, testContext);
	}

	public R isEmpty() {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] is empty",
				() -> (callRef() == null || callRef().isEmpty()), returnObj, testContext);
	}

	public R notEmpty() {
		return getEvaluator().testCondition(() -> "[" + callRef() + "] not empty",
				() -> (callRef() != null && !callRef().isEmpty()), returnObj, testContext);
	}

	public R storeValue(String key) {
		testContext.store(key, callRef());
		return returnObj;
	}

	public R storeMatch(String key, String pattern) {
		return storeMatch(key, pattern, 1);
	}

	public R storeMatch(String key, String pattern, int group) {
		log.info("Storing pattern match pattern:[" + pattern + "], string:[" + callRef() + "], match group:[" + group + "]");
		Matcher matcher = Pattern.compile(pattern).matcher(callRef());
		if (!matcher.find()) {
			throw testContext.createException("Error: Unable to store match.  String [" + callRef() + "] does not match regex [" + pattern + "]");
		}
		testContext.store(key, matcher.group(group));
		return returnObj;
	}

	public StringTester<R> testMatch(String pattern) {
		return testMatch(pattern, 1);
	}

	public StringTester<R> testMatch(String pattern, int group) {
		return new StringTester<>(() -> {
			Matcher matcher = Pattern.compile(pattern).matcher(callRef());
			matcher.find();
			return matcher.group(group);
		}, returnObj, testContext, getEvaluator());
	}

	public StringTester<R> transform(ThrowingFunction<String,String,?> transform) {
		return new StringTester<>(() -> ThrowingFunction.unchecked(transform).apply(callRef()), returnObj, testContext, getEvaluator());
	}

	public <T extends Comparable<T>> ComparableTester<T, R> transformCompare(ThrowingFunction<String,T,?> transform) {
		return new ComparableTester<>(() -> ThrowingFunction.unchecked(transform).apply(callRef()), returnObj, testContext, getEvaluator());
	}

	public ComparableTester<Integer, R> length() {
		return new ComparableTester<>(() -> {
			String val = callRef();
			return val == null ? null : val.length();
		}, returnObj, testContext, getEvaluator());
	}

	public ComparableTester<Integer,R> asInteger() {
		return new ComparableTester<>(() -> {
			String val = callRef();
			try {
				if (val == null || val.isEmpty()) {
					return null;
				}
				return Integer.parseInt(callRef());
			} catch (Exception ex) {
				throw testContext.createException("Error: Unable to parse integer from string [" + val + "]");
			}
		}, returnObj, testContext, getEvaluator());
	}

	public ComparableTester<Date,R> asDate() {
		return new ComparableTester<>(() -> {
			String val = callRef();
			try {
				if (val == null || val.isEmpty()) {
					return null;
				}
				return new Date(callRef());
			} catch (Exception ex) {
				throw testContext.createException("Error: Unable to parse date from string [" + val + "]");
			}
		}, returnObj, testContext, getEvaluator());
	}
}
