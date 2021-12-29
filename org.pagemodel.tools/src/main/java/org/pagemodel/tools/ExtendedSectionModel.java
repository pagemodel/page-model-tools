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

package org.pagemodel.tools;

import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.web.SectionModel;
import org.pagemodel.web.testers.ClickAction;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public abstract class ExtendedSectionModel<S extends ExtendedSectionModel<? super S, P, R>, P extends ExtendedModelBase<? super P>, R extends ExtendedModelBase<? super R>> extends SectionModel<S, P, R> implements ExtendedModelBase<S> {
	public ExtendedSectionModel(ClickAction<P, ?> clickAction, TestEvaluator testEvaluator) {
		super(clickAction, testEvaluator);
	}

	@Override
	public ExtendedTestContext getContext() {
		return (ExtendedTestContext)super.getContext();
	}

	@Override
	public ExtendedPageTester<S> testPage() {
		return new ExtendedPageTester<>((S)this, getContext(), getEvaluator());
	}
}
