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

import java.util.Date;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public interface TestContext {
	TestEvaluator getEvaluator();

	void setEvaluator(TestEvaluator testEvaluator);

	<T> void store(String key, T value);

	<T> T load(Class<T> clazz, String key);

	<T> T load(String key);

	default public String loadString(String key) {
		return load(key);
	}

	default public Integer loadInteger(String key) {
		return load(key);
	}

	default public Date loadDate(String key) {
		return load(key);
	}

	TestContext removeStored(String key);

	<E extends RuntimeException> E createException(String message, Throwable cause);

	default <E extends RuntimeException> E createException(String message) {
		return createException(message, null);
	}
}
