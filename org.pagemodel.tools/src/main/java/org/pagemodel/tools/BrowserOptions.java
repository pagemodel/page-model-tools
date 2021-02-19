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

import java.util.*;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class BrowserOptions {
	private Map<String,List<String>> userOpts = new HashMap<>();
	private Map<String,List<String>> systemOpts = new HashMap<>();

	public BrowserOptions() {
		systemOpts.put("chrome", Arrays.asList(
				"--ignore-certificate-errors", "--disable-dev-shm-usage", "--silent", "--log-level=3"));

		systemOpts.put("headless", Arrays.asList(
				"--ignore-certificate-errors", "--disable-dev-shm-usage", "--silent", "--log-level=3",
				"--headless", "--disable-gpu", "--window-size=1920,1080"));
	}

	public void addUserBrowserOptions(String browser, String...browserArgs){
		if(!userOpts.containsKey(browser)){
			userOpts.put(browser, new ArrayList<>());
		}
		List<String> dest = userOpts.get(browser);
		for(String arg: browserArgs){
			if(!dest.contains(arg.trim())){
				dest.add(arg.trim());
			}
		}
	}

	public void removeBrowserArgument(String browser, String browserArg){
		if(userOpts.containsKey(browser)){
			userOpts.get(browser).remove(browserArg.trim());
		}
		if(systemOpts.containsKey(browser)){
			systemOpts.get(browser).remove(browserArg.trim());
		}
	}

	public void clearUserBrowserOptions(String browser){
		if(userOpts.containsKey(browser)) {
			userOpts.get(browser).clear();
		}
	}

	public List<String> getBrowserOptions(String browser, String...extraBrowserArgs){
		List<String> system = systemOpts.containsKey(browser) ? systemOpts.get(browser) : Collections.EMPTY_LIST;
		List<String> user = userOpts.containsKey(browser) ? userOpts.get(browser) : Collections.EMPTY_LIST;
		List<String> extras = Arrays.asList(extraBrowserArgs);
		List<String> args = new ArrayList<>(system);
		for(String arg : user){
			if(!args.contains(arg.trim())){
				args.add(arg.trim());
			}
		}
		for(String arg : extras){
			if(!args.contains(arg.trim())){
				args.add(arg.trim());
			}
		}
		return args;
	}
}
