package org.pagemodel.tests.myapp.tools;

import org.pagemodel.core.utils.Unique;

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
