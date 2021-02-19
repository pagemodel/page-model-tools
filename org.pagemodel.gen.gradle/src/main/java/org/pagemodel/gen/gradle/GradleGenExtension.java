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

package org.pagemodel.gen.gradle;

public class GradleGenExtension {
	private String srcDir = "src/main/resources/pagemodels";
	private String genRootDir = "src/gen/java";

	public String getSrcDir() {
		return srcDir;
	}

	public void setSrcDir(String srcDir) {
		this.srcDir = srcDir;
	}

	public String getGenRootDir() {
		return genRootDir;
	}

	public void setGenRootDir(String genRootDir) {
//		if(genRootDir.trim().startsWith("src/main/java/") || genRootDir.trim().equals("src/main/java")){
//			throw new IllegalArgumentException("Error: genRootDir cannot be in 'src/main/java'");
//		}
		this.genRootDir = genRootDir;
	}
}
