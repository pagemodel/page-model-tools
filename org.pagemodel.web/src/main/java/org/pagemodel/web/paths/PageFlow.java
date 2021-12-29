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

package org.pagemodel.web.paths;

import org.pagemodel.web.PageModel;
import org.pagemodel.web.WebTestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.Function;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class PageFlow<N extends PageModel<? super N>> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected WebTestContext testContext;
	protected Class<? extends PageModel<?>> currentPageClass;
	private Set<Class<? extends PageModel<?>>> pageTypes = new LinkedHashSet<>();
	private Map<Class<? extends PageModel<?>>, Function<? extends PageModel<?>, N>> pagePaths = new HashMap<>();

	public PageFlow(WebTestContext testContext, Class<? extends PageModel<?>> currentPageClass) {
		this.testContext = testContext;
		this.currentPageClass = currentPageClass;
	}

	public <T extends PageModel<? super T>> N testPaths() {
		return testPaths(PageResolver.DEFAULT_LOAD_TIMEOUT_SEC);
	}

	public <T extends PageModel<? super T>> N testPaths(int timeoutSeconds) {
		log.info("Testing current page type. " +
				"CurrentPage(title:[" + testContext.getDriver().getTitle() + "], url:[" + testContext.getDriver().getCurrentUrl() + "]), " +
				"Expected types:" + Arrays.toString(pageTypes.stream().map(clazz -> clazz.getSimpleName()).toArray()) + ".");
		//Push current page to end of list to prevent it from matching right away
		List<Class<? extends PageModel<?>>> reorderedPageTypes = new ArrayList<>(pageTypes);
		if (reorderedPageTypes.contains(currentPageClass)) {
			reorderedPageTypes.remove(currentPageClass);
			reorderedPageTypes.add(currentPageClass);
		}
		T curPage = (T) new PageResolver().tryPageTypes(testContext, timeoutSeconds, pageTypes.toArray(new Class[0]));
		if (curPage == null) {
			throw new RuntimeException("Current page does not match expected page types. " +
					"CurrentPage(title:[" + testContext.getDriver().getTitle() + "], url:[" + testContext.getDriver().getCurrentUrl() + "]), " +
					"Expected types:" + Arrays.toString(pageTypes.stream().map(clazz -> clazz.getSimpleName()).toArray()) + ".");
		}
		log.info("Current page type resolved to: " + curPage.getClass().getSimpleName());
		Function<T, N> path = (Function<T, N>) pagePaths.get(curPage.getClass());
		return path.apply(curPage);
	}

	public <T extends PageModel<? super T>> PageFlow<N> addPath(Class<T> pageClass, Function<T, N> pageNavigation) {
		if (pageTypes.contains(pageClass)) {
			throw new IllegalArgumentException("Page path already exists for class [" + pageClass.getSimpleName() + "]");
		}
		pageTypes.add(pageClass);
		pagePaths.put(pageClass, pageNavigation);
		return this;
	}
}
