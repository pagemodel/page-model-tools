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

public class Main {
	public static void main(String[] argv){
		if(argv.length < 2){
			System.out.println("Error: ProjectShortName and ProjectGroup required arguments.");
			System.out.println("Usage: java -jar org.pagemodel.gen.project.jar ProjectShortName ProjectGroup [path/to/outputProjectDir/]");
			System.out.println("Ex: java -jar org.pagemodel.gen.project.jar XYZ com.example projects/XYZTest/");
		}
		String projectName = argv[0];
		String projectGroup = argv[1];
		String projectDir = projectName;
		if(argv.length > 2) {
			projectDir = argv[2];
		}
		System.out.println("Generating org.pagemodel project:");
		System.out.println("ProjectName: " + projectName);
		System.out.println("ProjectGroup: " + projectGroup);
		System.out.println("ProjectPackage: " + projectGroup + "." + projectName.toLowerCase());
		System.out.println("ProjectDir: " + projectDir);
		FileFindReplace.generateProject(projectName, projectGroup, projectDir);
	}
}
