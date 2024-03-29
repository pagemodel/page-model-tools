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

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
@FunctionalInterface
public interface ThrowingRunnable<E extends Exception> {
	static <E extends Exception> Runnable unchecked(ThrowingRunnable<E> runnable) {
		return () -> {
			try {
				runnable.run();
			} catch (Exception e) {
				if(RuntimeException.class.isAssignableFrom(e.getClass())){
					throw (RuntimeException) e;
				}
				throw new RuntimeException(e);
			}
		};
	}

	void run() throws E;
}
