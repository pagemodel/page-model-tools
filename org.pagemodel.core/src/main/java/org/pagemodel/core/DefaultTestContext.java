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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class DefaultTestContext implements TestContext {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected Map<String, Object> storedObjects = new HashMap<>();
	protected TestEvaluator testEvaluator;

	public DefaultTestContext() {
		this.testEvaluator = new TestEvaluator.Now();
	}

	@Override
	public TestEvaluator getEvaluator() {
		return testEvaluator;
	}

	@Override
	public void setEvaluator(TestEvaluator testEvaluator) {
		this.testEvaluator = testEvaluator;
	}

	@Override
	public <T> void store(String key, T value) {
		log.info("Storing: key:[" + key + "], value:[" + value + "]");
		if (value == null) {
			throw new NullPointerException("Attempting to store null value for key [" + key + "]");
		}
		if (storedObjects.containsKey(key)) {
			throw new IllegalArgumentException("Unable to store [" + key + " -> " + value + "] key already in use [" + key + " -> " + storedObjects.get(key) + "]");
		}
		storedObjects.put(key, value);
	}

	@Override
	public <T> T load(Class<T> clazz, String key) {
		return getEvaluator().testCondition(() -> "Loading: key:[" + key + "], value:[" + storedObjects.get(key) + "]", () -> {
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

	@Override
	public <T> T load(String key) {
		return getEvaluator().testCondition(() -> "Loading: key:[" + key + "], value:[" + storedObjects.get(key) + "]", () -> {
			if (key == null) {
				throw new NullPointerException("Attempting to load value with null key");
			}
			if (!storedObjects.containsKey(key)) {
				throw new IllegalArgumentException("No value stored with key [" + key + "]");
			}
			return true;
		}, (T)storedObjects.get(key), this);
	}

	@Override
	public DefaultTestContext removeStored(String key) {
		return getEvaluator().testCondition(() -> "Removing: key:[" + key + "], value:[" + storedObjects.get(key) + "]", () -> {
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

	@Override
	public <E extends RuntimeException> E createException(String message, Throwable cause) {
		return (E)new RuntimeException(message, cause);
	}
}
