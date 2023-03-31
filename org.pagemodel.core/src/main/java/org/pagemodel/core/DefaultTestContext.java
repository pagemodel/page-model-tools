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

package org.pagemodel.core;

import org.pagemodel.core.testers.TestEvaluator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * Default implementation of the {@link TestContext} interface.
 * Stores objects in a {@link Map} and uses a {@link TestEvaluator} to evaluate tests and conditions.
 */
public class DefaultTestContext implements TestContext {

	protected Map<String, Object> storedObjects = new HashMap<>();
	protected TestEvaluator testEvaluator;

	/**
	 * Constructs a new DefaultTestContext with a default TestEvaluator.
	 */
	public DefaultTestContext() {
		this.testEvaluator = new TestEvaluator.Now();
	}

	/**
	 * Returns the TestEvaluator used by this context.
	 *
	 * @return the TestEvaluator used by this context
	 */
	@Override
	public TestEvaluator getEvaluator() {
		return testEvaluator;
	}

	/**
	 * Sets the TestEvaluator used by this context.
	 *
	 * @param testEvaluator the TestEvaluator to set
	 */
	@Override
	public void setEvaluator(TestEvaluator testEvaluator) {
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Stores a value with the given key in the context.
	 *
	 * @param key the key to store the value under
	 * @param value the value to store
	 * @throws NullPointerException if the key or value is null
	 * @throws IllegalArgumentException if the key is already in use
	 */
	@Override
	public <T> void store(String key, T value) {
		getEvaluator().testRun(TestEvaluator.TEST_EXECUTE,
				"store", op -> op.addValue("key", key).addValue("value", value),
				() -> {
					if (key == null) {
						throw new NullPointerException("Attempting to store value with null key");
					}
					if (value == null) {
						throw new NullPointerException("Attempting to store null value for key [" + key + "]");
					}
					if (storedObjects.containsKey(key)) {
						throw new IllegalArgumentException("Unable to store [" + key + " -> " + value + "] key already in use [" + key + " -> " + storedObjects.get(key) + "]");
					}
					storedObjects.put(key, value);
				}, this, this);
	}

	/**
	 * Loads a value with the given key from the context and casts it to the specified class.
	 *
	 * @param clazz the class to cast the value to
	 * @param key the key to load the value from
	 * @return the value with the given key, cast to the specified class
	 * @throws NullPointerException if the key is null
	 * @throws IllegalArgumentException if no value is stored with the given key or if the stored value cannot be cast to the specified class
	 */
	@Override
	public <T> T load(Class<T> clazz, String key) {
		return getEvaluator().quiet().testCondition(
				"load", op -> op.addValue("value", key).addValue("actual",storedObjects.get(key)).addValue("class",clazz.getSimpleName()),
				() -> {
					if (key == null) {
						throw new NullPointerException("Attempting to load value with null key");
					}
					if (!storedObjects.containsKey(key)) {
						throw new IllegalArgumentException("No value stored with key [" + key + "]");
					}
					Object obj = storedObjects.get(key);
					if(obj == null || !clazz.isAssignableFrom(obj.getClass())){
						throw new ClassCastException("Error: Unable to cast from [" + obj == null ? null : obj.getClass().getSimpleName() + "] to [" + clazz.getSimpleName() + "] for value:[" + obj + "] with key:[" + key + "]");
					}
					return true;
				}, (T)storedObjects.get(key), this);
	}

	/**
	 * Loads a value with the given key from the context.
	 *
	 * @param key the key to load the value from
	 * @return the value with the given key
	 * @throws NullPointerException if the key is null
	 * @throws IllegalArgumentException if no value is stored with the given key
	 */
	@Override
	public <T> T load(String key) {
		return getEvaluator().quiet().testCondition(
				"load", op -> op.addValue("value", key).addValue("actual",storedObjects.get(key)),
				() -> {
					if (key == null) {
						throw new NullPointerException("Attempting to load value with null key");
					}
					if (!storedObjects.containsKey(key)) {
						throw new IllegalArgumentException("No value stored with key [" + key + "]");
					}
					return true;
				}, (T)storedObjects.get(key), this);
	}

	/**
	 * Removes the value with the given key from the context.
	 *
	 * @param key the key to remove the value from
	 * @return this DefaultTestContext
	 * @throws NullPointerException if the key is null
	 * @throws IllegalArgumentException if no value is stored with the given key
	 */
	@Override
	public DefaultTestContext removeStored(String key) {
		return getEvaluator().testCondition(
				"remove stored", op -> op.addValue("value", key).addValue("actual",storedObjects.get(key)),
				() -> {
					if (key == null) {
						throw new NullPointerException("Attempting to remove value with null key");
					}
					if (!storedObjects.containsKey(key)) {
						throw new IllegalArgumentException("No value stored with key [" + key + "]");
					}
					storedObjects.remove(key);
					return true;
				}, this, this);
	}

	/**
	 * Creates a new RuntimeException for this context with the given message and cause.
	 *
	 * @param message the message for the exception
	 * @param cause the cause of the exception
	 * @return a new RuntimeException with the given message and cause
	 */
	@Override
	public <E extends RuntimeException> E createException(String message, Throwable cause) {
		return (E)new RuntimeException(message, cause);
	}

}