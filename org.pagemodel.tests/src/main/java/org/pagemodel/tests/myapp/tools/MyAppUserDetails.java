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

package org.pagemodel.tests.myapp.tools;

import org.pagemodel.core.utils.Unique;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class MyAppUserDetails {

	private String username;
	private String password;
	private String email;
	private String userRole;

	public static MyAppUserDetails generate(String userRole) {
		String username = Unique.string("user_%s");
		String email = Unique.string("email_%s@example.com");
		return new MyAppUserDetails(username, "password", email, userRole);
	}

	public MyAppUserDetails(String username, String password, String email, String userRole) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.userRole = userRole;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}
}
