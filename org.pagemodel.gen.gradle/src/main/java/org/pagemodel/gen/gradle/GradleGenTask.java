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

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GradleGenTask extends DefaultTask {
	@TaskAction
	public void pagegen() throws IOException {
		GradleGenExtension extension = getProject().getExtensions().findByType(GradleGenExtension.class);
		if (extension == null) {
			extension = new GradleGenExtension();
		}
		File modelDir = new File(this.getProject().getProjectDir(), extension.getSrcDir());
		File outDir = new File(this.getProject().getProjectDir(), extension.getGenRootDir());
		generatePageModelClasses(modelDir, outDir);
	}

	public static void generatePageModelClasses(final File inputDir, final File outputDir) throws IOException {
		if(!inputDir.exists()){
			throw new RuntimeException("Error: pagemodel directory does not exist [" + inputDir.getPath() + "]");
		}
		for(File pagemodel : inputDir.listFiles(file -> file.getName().endsWith(".pagemodel"))){
			generatePageModelClass(pagemodel, outputDir);
		}
		for(File modelDir : inputDir.listFiles(file -> file.isDirectory())){
			generatePageModelClasses(modelDir, outputDir);
		}
	}

	public static void generatePageModelClass(final File inputFile, final File outputDir) throws IOException {
		try {
			String path = inputFile.getAbsolutePath();
			System.out.println("Generating: " + path);
			PageModelConfig config = new PageModelReader().readPageModel(path);
			String javaContent = new PageModelJavaWriter().generatePageModelJavaFile(config);

			String javaFile = inputFile.getName().replaceAll("\\.pagemodel$", ".java");
			String javaDir = outputDir.getAbsolutePath() + "/" + config.modelPackage.replaceAll("\\.", "/") + "/";
			String javaPath = javaDir + javaFile;

			new File(javaDir).mkdirs();
			FileWriter writer = new FileWriter(javaPath);
			writer.write(javaContent);
			writer.flush();
			writer.close();
		}catch (Throwable ex){
			System.out.println();
		}
	}
}