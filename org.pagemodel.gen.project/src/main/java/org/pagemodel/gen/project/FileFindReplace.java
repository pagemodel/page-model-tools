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

package org.pagemodel.gen.project;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class FileFindReplace {

	public static FileMove[] getMoves(String projectName, String projectGroup){
		String projectPackage = projectGroup + "." + projectName.toLowerCase();
		String pkgPath = projectPackage.replaceAll("\\.", "/");
		return new FileMove[]{
				mv("project_template/gradle/gradlew", "gradlew").exec(),
				mv("project_template/gradle/gradlew.bat", "gradlew.bat"),
				mv("project_template/gradle/gradle-wrapper.jar", "gradle/wrapper/gradle-wrapper.jar").unmodified(),
				mv("project_template/gradle/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.properties"),

				mv("project_template/docker/myapp-headless-chrome.dockerfile", "scripts/docker/" + projectName.toLowerCase() + "-headless-chrome.dockerfile"),
				mv("project_template/docker/build-docker.sh", "scripts/docker/build-docker.sh").exec(),
				mv("project_template/docker/dtest", "scripts/dtest").exec(),

				mv("project_template/html/style.css", "example_html/style.css"),
				mv("project_template/html/home.html", "example_html/home.html"),
				mv("project_template/html/login.html", "example_html/login.html"),
				mv("project_template/html/manage_users.html", "example_html/manage_users.html"),

				mv("project_template/.editorconfig", ".editorconfig"),
				mv("project_template/gitignore", ".gitignore"),
				mv("project_template/build.gradle", "build.gradle"),
				mv("project_template/settings.gradle", "settings.gradle"),

				mv("project_template/pagemodels/HomePage.pagemodel", projectName + "PageModels/src/main/resources/pagemodels/HomePage.pagemodel"),
				mv("project_template/pagemodels/LoginPage.pagemodel", projectName + "PageModels/src/main/resources/pagemodels/LoginPage.pagemodel"),
				mv("project_template/pagemodels/ManageUsersPage.pagemodel", projectName + "PageModels/src/main/resources/pagemodels/ManageUsersPage.pagemodel"),
				mv("project_template/pagemodels/MyAppInternalPage.pagemodel", projectName + "PageModels/src/main/resources/pagemodels/" + projectName + "InternalPage.pagemodel"),

				mv("project_template/test/build.gradle", projectName + "TestSanity/build.gradle"),
				mv("project_template/test/MyAppTestBase.java", projectName + "TestSanity/src/test/java/" + pkgPath + "/test/sanity/" + projectName + "TestBase.java"),
				mv("project_template/test/PageTests.java", projectName + "TestSanity/src/test/java/" + pkgPath + "/test/sanity/PageTests.java"),
				mv("project_template/test/profiles.myapp.json", projectName + "TestSanity/src/test/resources/profiles." + projectName.toLowerCase() + ".json"),
				mv("project_template/test/profiles.mail.json", projectName + "TestSanity/src/test/resources/profiles.mail.json"),

				mv("project_template/tools/build.gradle", projectName + "PageModels/build.gradle"),
				mv("project_template/tools/MyAppConfig.java", projectName + "PageModels/src/main/java/" + pkgPath + "/tools/" + projectName + "Config.java"),
				mv("project_template/tools/MyAppTestContext.java", projectName + "PageModels/src/main/java/" + pkgPath + "/tools/" + projectName + "TestContext.java"),
				mv("project_template/tools/MyAppUser.java", projectName + "PageModels/src/main/java/" + pkgPath + "/tools/" + projectName + "User.java"),
				mv("project_template/tools/MyAppUserDetails.java", projectName + "PageModels/src/main/java/" + pkgPath + "/tools/" + projectName + "UserDetails.java")
		};
	}

	public static Replace[] getReplaces(String projectName, String projectGroup){
		return new Replace[]{rep("MyApp", projectName),
				rep("org.pagemodel.tests.myapp", projectGroup + "." + projectName.toLowerCase()),
				rep("org.pagemodel.tests", projectGroup),
				rep("myapp", projectName.toLowerCase()),
				rep("myApp", projectName.toLowerCase())};
	}

	public static void generateProject(String projectName, String projectGroup, String outDir) {
		FileMove[] moves = getMoves(projectName, projectGroup);
		Replace[] replaces = getReplaces(projectName, projectGroup);
		for(FileMove move : moves){
			String dest = outDir + "/" + move.dest;
			try {
				byte[] contents = move.unmodified ? extractResource(move.resource) : findReplaceFile(move.resource, replaces);
				File destFile = new File(dest);
				destFile.getParentFile().mkdirs();
				Files.write(destFile.toPath(), contents);
				if(move.exec){
					destFile.setExecutable(true);
				}
			}catch (Throwable t){
				System.out.println(move.resource + " -> " + dest);
				t.printStackTrace();
			}
		}
	}

	public static byte[] findReplaceFile(String resource, Replace...replaces) throws IOException {
		String content = new String(extractResource(resource));
		for(Replace replace : replaces) {
			content = content.replaceAll(replace.regex, replace.replacement);
		}
		return content.getBytes();
	}

	public static byte[] extractResource(String resource) throws IOException {
		InputStream inputStream = FileFindReplace.class.getClassLoader().getResourceAsStream(resource);
		final int bufLen = 4 * 0x400; // 4KB
		byte[] buf = new byte[bufLen];
		int readLen;
		IOException exception = null;

		try {
			try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
				while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
					outputStream.write(buf, 0, readLen);

				return outputStream.toByteArray();
			}
		} catch (IOException e) {
			exception = e;
			throw e;
		} finally {
			if (exception == null) inputStream.close();
			else try {
				inputStream.close();
			} catch (IOException e) {
				exception.addSuppressed(e);
			}
		}
	}

	public static Replace rep(String find, String replace){
		Replace rep = new Replace();
		rep.regex = find;
		rep.replacement = replace;
		return rep;
	}

	public static class Replace{
		public String regex;
		public String replacement;
	}

	public static FileMove mv(String resource, String dest){
		FileMove mv = new FileMove();
		mv.resource = resource;
		mv.dest = dest;
		return mv;
	}

	public static class FileMove {
		public String resource;
		public String dest;
		public boolean exec = false;
		public boolean unmodified = false;

		public FileMove exec(){
			this.exec = true;
			return this;
		}
		public FileMove unmodified(){
			this.unmodified = true;
			return this;
		}
	}

}
