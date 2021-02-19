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

package org.pagemodel.core.utils;

import java.util.function.Consumer;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> {
	static <T, E extends Throwable> Consumer<T> unchecked(ThrowingConsumer<T, E> consumer) {
		return t -> {
			try {
				consumer.accept(t);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
	}

	void accept(T t) throws E;
}
