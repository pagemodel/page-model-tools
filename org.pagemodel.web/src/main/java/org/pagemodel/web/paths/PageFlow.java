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
/**
 * A PageFlow represents a set of possible page sequences that lead from one specific page to another specific page.
 * This class can be used to handle extra security steps during a signup or login, or to handle extra dialogs that may or may not be present when navigating from one page to another.
 *
 * @param <N> the type of the final page in the flow
 */
public class PageFlow<N extends PageModel<? super N>> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected WebTestContext testContext;
	protected Class<? extends PageModel<?>> currentPageClass;
	private Set<Class<? extends PageModel<?>>> pageTypes = new LinkedHashSet<>();
	private Map<Class<? extends PageModel<?>>, Function<? extends PageModel<?>, N>> pagePaths = new HashMap<>();

	/**
	 * Constructs a new PageFlow with the given WebTestContext and current page class.
	 *
	 * @param testContext      the WebTestContext to use for testing
	 * @param currentPageClass the class of the current page
	 */
	public <P extends PageModel<? super P>> PageFlow(WebTestContext testContext, Class<P> currentPageClass) {
		this.testContext = testContext;
		this.currentPageClass = currentPageClass;
	}

	/**
	 * Tests all possible page paths in the flow with the default load timeout.
	 *
	 * @param <T> the type of the current page
	 * @return the final page in the flow
	 * @throws RuntimeException if the current page does not match any expected page types
	 */
	public <T extends PageModel<? super T>> N testPaths() {
		return testPaths(PageResolver.DEFAULT_LOAD_TIMEOUT_SEC);
	}

	/**
	 * Tests all possible page paths in the flow with the given load timeout.
	 *
	 * @param timeoutSeconds the load timeout in seconds
	 * @param <T>            the type of the current page
	 * @return the final page in the flow
	 * @throws RuntimeException if the current page does not match any expected page types
	 */
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

	/**
	 * Adds a new page path to the flow.
	 *
	 * @param pageClass      the class of the page to navigate to
	 * @param pageNavigation the function that navigates to the page
	 * @param <T>            the type of the page to navigate to
	 * @return this PageFlow instance
	 * @throws IllegalArgumentException if a page path already exists for the given page class
	 */
	public <T extends PageModel<? super T>> PageFlow<N> addPath(Class<T> pageClass, Function<T, N> pageNavigation) {
		if (pageTypes.contains(pageClass)) {
			throw new IllegalArgumentException("Page path already exists for class [" + pageClass.getSimpleName() + "]");
		}
		pageTypes.add(pageClass);
		pagePaths.put(pageClass, pageNavigation);
		return this;
	}
}