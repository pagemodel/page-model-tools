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
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class Unique {
	public static String shortString(){
		UUID uuid = UUID.randomUUID();
		long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
		return Long.toString(l, Character.MAX_RADIX);
	}

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

	public static String longString(){
		return UUID.randomUUID().toString();
	}

	public static UUID uuid(){
		return UUID.randomUUID();
	}

	public static long number(){
		UUID uuid = UUID.randomUUID();
		return ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
	}
}
