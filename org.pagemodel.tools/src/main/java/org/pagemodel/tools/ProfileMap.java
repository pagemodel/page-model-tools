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
import java.util.HashMap;
import java.util.Map;

public class ProfileMap<T> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static Map<Class<?>, Map<String, ?>> profileDefaults = new HashMap<>();

	public static <T> void addDefaults(Class<T> clazz, Map<String,T> defaults){
		if(defaults == null){
			profileDefaults.remove(clazz);
		}
		if(!profileDefaults.containsKey(clazz)){
			profileDefaults.put(clazz, defaults);
		}else{
			((Map<String,T>)profileDefaults.get(clazz)).putAll(defaults);
		}
	}

	protected Class<T> type;
	protected Map<String, T> profileMap;

	public static <T> ProfileMap<T> loadFile(Class<T> type, String filePath) {
		try {
			ProfileMap<T> profileMap = new ProfileMap<>(type);
			profileMap.profileMap.putAll(profileMap.loadConfigMapFile(filePath));
			return profileMap;
		}catch(IOException ex){
			throw new RuntimeException("Error: unable to load profiles.", ex);
		}
	}

	public static <T> ProfileMap<T> loadStream(Class<T> type, InputStream input) {
		ProfileMap<T> profileMap = new ProfileMap<>(type);
		profileMap.profileMap.putAll(profileMap.loadConfigMapStream(input));
		return profileMap;
	}

	private static <T> void registerDefaultProfiles(Class<T> type){
		if(profileDefaults.containsKey(type)){
			return;
		}
		try {
			type.getMethod("registerDefaultProfiles").invoke(type);
		}catch(NoSuchMethodException ex){
			profileDefaults.put(type, new HashMap<>());
		}catch(Exception ex){
			profileDefaults.put(type, new HashMap<>());
			log.info("Exception registering default profiles for type [" + type.getName() + "]", ex);
		}
	}

	public static <T> ProfileMap<T> loadUrl(Class<T> type, String url) {
		try {
			ProfileMap<T> profileMap = new ProfileMap<>(type);
			profileMap.profileMap.putAll(profileMap.loadConfigMapUrl(url));
			return profileMap;
		}catch(IOException ex){
			throw new RuntimeException("Error: unable to load profiles.", ex);
		}
	}

	protected ProfileMap(Class<T> type) {
		this.type = type;
		profileMap = new HashMap<>();
		registerDefaultProfiles(type);
		if(profileDefaults.containsKey(type)){
			profileMap.putAll((Map<String,T>)profileDefaults.get(type));
		}
	}

	public T getProfile(String profile) {
		return profileMap.get(profile);
	}

	public Map<String, T> getProfileMap() {
		return profileMap;
	}

	protected T loadJsonFile(String configPath) throws FileNotFoundException {
		Reader reader = new FileReader(configPath);
		return new Gson().fromJson(reader, type);
	}

	protected T loadJsonURL(String urlStr) throws IOException {
		InputStreamReader reader = new InputStreamReader(new URL(urlStr).openStream());
		return new Gson().fromJson(reader, type);
	}

	protected Map<String, T> loadConfigMapFile(String configPath) throws FileNotFoundException {
		return loadConfigMapFromReader(new FileReader(configPath));
	}

	protected Map<String, T> loadConfigMapStream(InputStream input) {
		return loadConfigMapFromReader(new InputStreamReader(input));
	}

	protected Map<String, T> loadConfigMapUrl(String urlStr) throws IOException {
		return loadConfigMapFromReader(new InputStreamReader(new URL(urlStr).openStream()));
	}

	private Map<String, T> loadConfigMapFromReader(Reader reader) {
		Type typeOfHashMap = TypeToken.getParameterized(Map.class, String.class, type).getType();
		return new Gson().fromJson(reader,typeOfHashMap);
	}
}
