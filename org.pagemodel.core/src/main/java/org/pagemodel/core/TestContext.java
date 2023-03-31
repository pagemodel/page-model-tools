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
import org.pagemodel.core.utils.json.JsonLogConsoleOut;

import java.util.Date;
import java.util.Map;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * Interface for a test context, which provides methods for storing and retrieving data, as well as creating exceptions.
 */
public interface TestContext {

	/**
	 * Returns the test evaluator associated with this context.
	 * @return the test evaluator
	 */
	TestEvaluator getEvaluator();

	/**
	 * Sets the test evaluator for this context.
	 * @param testEvaluator the test evaluator to set
	 */
	void setEvaluator(TestEvaluator testEvaluator);

	/**
	 * Stores a value with the given key in this context.
	 * @param key the key to store the value under
	 * @param value the value to store
	 * @param <T> the type of the value being stored
	 */
	<T> void store(String key, T value);

	/**
	 * Loads a value with the given key and type from this context.
	 * @param clazz the class of the value being loaded
	 * @param key the key to load the value from
	 * @param <T> the type of the value being loaded
	 * @return the loaded value
	 */
	<T> T load(Class<T> clazz, String key);

	/**
	 * Loads a value with the given key from this context.
	 * @param key the key to load the value from
	 * @param <T> the type of the value being loaded
	 * @return the loaded value
	 */
	<T> T load(String key);

	/**
	 * Loads a string value with the given key from this context.
	 * @param key the key to load the value from
	 * @return the loaded string value
	 */
	default public String loadString(String key) {
		return load(key);
	}

	/**
	 * Loads an integer value with the given key from this context.
	 * @param key the key to load the value from
	 * @return the loaded integer value
	 */
	default public Integer loadInteger(String key) {
		return load(key);
	}

	/**
	 * Loads a date value with the given key from this context.
	 * @param key the key to load the value from
	 * @return the loaded date value
	 */
	default public Date loadDate(String key) {
		return load(key);
	}

	/**
	 * Removes a stored value with the given key from this context.
	 * @param key the key of the value to remove
	 * @return this context
	 */
	TestContext removeStored(String key);

	/**
	 * Creates a new runtime exception with the given message and cause.
	 * @param message the message for the exception
	 * @param cause the cause of the exception
	 * @param <E> the type of the exception being created
	 * @return the created exception
	 */
	<E extends RuntimeException> E createException(String message, Throwable cause);

	/**
	 * Creates a new runtime exception with the given message.
	 * @param message the message for the exception
	 * @param <E> the type of the exception being created
	 * @return the created exception
	 */
	default <E extends RuntimeException> E createException(String message) {
		return createException(message, null);
	}

	/**
	 * Creates a new runtime exception with the given event and cause.
	 * @param event the event to format the message from
	 * @param cause the cause of the exception
	 * @param <E> the type of the exception being created
	 * @return the created exception
	 */
	default <E extends RuntimeException> E createException(Map<String,Object> event, Throwable cause) {
		return createException(JsonLogConsoleOut.formatEvent(event), cause);
	}

	/**
	 * Creates a new runtime exception with the given event.
	 * @param event the event to format the message from
	 * @param <E> the type of the exception being created
	 * @return the created exception
	 */
	default <E extends RuntimeException> E createException(Map<String,Object> event) {
		return createException(JsonLogConsoleOut.formatEvent(event));
	}
}