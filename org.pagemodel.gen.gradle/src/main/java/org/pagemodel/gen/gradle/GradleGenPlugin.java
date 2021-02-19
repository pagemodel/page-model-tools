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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

public class GradleGenPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		GradleGenExtension ext = project.getExtensions().create("pagegenSettings", GradleGenExtension.class);
		project.getTasks().create("pagegen", GradleGenTask.class);

		project.getGradle().projectsEvaluated(cl ->
				project.getTasks().getByName("compileJava").dependsOn(project.getTasks().getByName("pagegen")));

		project.getTasks().getByName("clean").doFirst(cl ->
				cl.getProject().delete(cl.getProject().file(ext.getGenRootDir())));

		((SourceSetContainer) project.getProperties().get("sourceSets"))
				.getByName("main").getJava().srcDir(ext.getGenRootDir());
	}
}