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

import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public interface ThrowingCallable {
	static <T, E extends Throwable> UncheckedCallable<T> unchecked(Callable<T> callable) {
		return () -> {
			try {
				return callable.call();
			} catch (Throwable e) {
				if(RuntimeException.class.isAssignableFrom(e.getClass())){
					throw (RuntimeException) e;
				}
				throw new RuntimeException(e);
			}
		};
	}

	static <T, E extends Throwable> UncheckedCallable<T> defaultOnError(Callable<T> callable, T defaultValue) {
		return () -> {
			try {
				return callable.call();
			} catch (Throwable e) {
				return defaultValue;
			}
		};
	}

	static <T, E extends Throwable> UncheckedCallable<T> nullOnError(Callable<T> callable) {
		return defaultOnError(callable, null);
	}

	static <E extends Throwable> ThrowingRunnable<Exception> asThrowingRunnable(Callable<?> callable) {
		return () -> {
			callable.call();
		};
	}

	static Runnable asUncheckedRunnable(Callable<?> callable) {
		return ThrowingRunnable.unchecked(() -> {
			callable.call();
		});
	}
}
