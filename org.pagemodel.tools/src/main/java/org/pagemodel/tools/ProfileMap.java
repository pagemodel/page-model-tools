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

package org.pagemodel.tools;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Map;

public class ProfileMap<T> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected Class<T> type;
	protected Map<String, T> profileMap;


	public static <T> ProfileMap<T> loadFile(Class<T> type, String filePath) {
		try {
			ProfileMap<T> profileMap = new ProfileMap<>(type);
			profileMap.profileMap = profileMap.loadConfigMapFile(filePath);
			return profileMap;
		}catch(IOException ex){
			throw new RuntimeException("Error: unable to load profiles.", ex);
		}
	}

	public static <T> ProfileMap<T> loadUrl(Class<T> type, String url) {
		try {
			ProfileMap<T> profileMap = new ProfileMap<>(type);
			profileMap.profileMap = profileMap.loadConfigMapUrl(url);
			return profileMap;
		}catch(IOException ex){
			throw new RuntimeException("Error: unable to load profiles.", ex);
		}
	}

	protected ProfileMap(Class<T> type) {
		this.type = type;
	}

	public T getProfile(String profile) {
		return profileMap.get(profile);
	}

	public Map<String, T> getProfileMap() {
		return profileMap;
	}

	public T loadJsonFile(String configPath) throws FileNotFoundException {
		Reader reader = new FileReader(configPath);
		return new Gson().fromJson(reader, type);
	}

	public T loadJsonURL(String urlStr) throws IOException {
		InputStreamReader reader = new InputStreamReader(new URL(urlStr).openStream());
		return new Gson().fromJson(reader, type);
	}

	public Map<String, T> loadConfigMapFile(String configPath) throws FileNotFoundException {
		Reader reader = new FileReader(configPath);
		Type typeOfHashMap = TypeToken.getParameterized(Map.class, String.class, type).getType();
		return new Gson().fromJson(reader,typeOfHashMap);
	}

	public Map<String, T> loadConfigMapUrl(String urlStr) throws IOException {
		InputStreamReader reader = new InputStreamReader(new URL(urlStr).openStream());
		Type typeOfHashMap = TypeToken.getParameterized(Map.class, String.class, type).getType();
		return new Gson().fromJson(reader,typeOfHashMap);
	}
}
