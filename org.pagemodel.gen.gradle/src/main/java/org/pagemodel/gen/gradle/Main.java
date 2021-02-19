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

import java.io.IOException;

public class Main {
	public static void main(String[] argv){
		try {
			String path = "../org.pagemodel.tests/src/main/resources/pagemodels/LoginPage.pagemodel";
			PageModelConfig config = new PageModelReader().readPageModel(path);
			String javaContent = new PageModelJavaWriter().generatePageModelJavaFile(config);
			System.out.println(javaContent);
		}catch (IOException ex){
			ex.printStackTrace();
		}
	}
}
