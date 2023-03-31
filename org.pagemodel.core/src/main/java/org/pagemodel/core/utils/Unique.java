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

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * The Unique class provides methods for generating unique identifiers.
 */
public class Unique {

	/**
	 * Generates a short alpha-numeric string.
	 * @return a 12-character alpha-numeric string
	 */
	public static String shortString(){
		//TODO: find better algorithm for generating random alpha-numeric string
		UUID uuid = UUID.randomUUID();
		long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
		String ret = Long.toString(l, Character.MAX_RADIX);
		// length is always 12 or 13, if 13 the first char is always '1', for consistency always return 12 char string.
		return ret.length() > 12 ? ret.substring(1, 13) : ret;
	}

	/**
	 * Generates a string with a unique identifier inserted at each occurrence of "%s".
	 * If the input string does not contain "%s", a unique identifier is appended to the end of the string.
	 * @param string the input string
	 * @return a string with unique identifiers inserted
	 */
	public static String string(String string){
		if(!string.contains("%s")){
			return string + " " + shortString();
		}
		String[] parts = ("|" + string + "|" ).split("%s");
		StringBuilder sb = new StringBuilder(parts[0]);
		for(int i=1; i<parts.length; i++){
			sb.append(shortString());
			sb.append(parts[i]);
		}
		String ret = sb.toString();
		return ret.substring(1,ret.length()-1);
	}

	/**
	 * Generates a long alpha-numeric string.
	 * @return a 36-character alpha-numeric string
	 */
	public static String longString(){
		return UUID.randomUUID().toString();
	}

	/**
	 * Generates a random UUID.
	 * @return a random UUID
	 */
	public static UUID uuid(){
		return UUID.randomUUID();
	}

	/**
	 * Generates a long integer number.
	 * @return a long integer number
	 */
	public static long number(){
		UUID uuid = UUID.randomUUID();
		return ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
	}
}