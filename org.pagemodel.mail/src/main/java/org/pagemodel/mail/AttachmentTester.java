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

package org.pagemodel.mail;

import org.pagemodel.core.TestContext;
import org.pagemodel.core.testers.StringTester;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.ThrowingFunction;
import org.pagemodel.core.utils.json.JsonBuilder;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class AttachmentTester<R> {
	protected R returnObj;
	protected final Callable<Attachment> ref;
	protected final TestContext testContext;
	private TestEvaluator testEvaluator;

	public AttachmentTester(Callable<Attachment> ref, R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	protected Attachment callRef(){
		try{
			return ref.call();
		}catch (Throwable t){
			return null;
		}
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	public R exists() {
		return getEvaluator().testCondition(
				"exists", op -> op
						.addValue("attachment", getAttachmentJson()),
				() -> callRef() != null, returnObj, testContext);
	}

	public R notExists() {
		return getEvaluator().testCondition(
				"not exists", op -> op
						.addValue("attachment", getAttachmentJson()),
				() -> callRef() == null, returnObj, testContext);
	}

	public StringTester<R> filename() {
		getEvaluator().setSourceFindEvent("filename", op -> op
				.addValue("attachment", getAttachmentJson()));
		return new StringTester<>(() -> callRef().getFilename(), returnObj, testContext, getEvaluator());
	}

	public StringTester<R> contentType() {
		getEvaluator().setSourceFindEvent("content type", op -> op
				.addValue("attachment", getAttachmentJson()));
		return new StringTester<>(() -> callRef().getContentType(), returnObj, testContext, getEvaluator());
	}

	public StringTester<R> textContent() {
		getEvaluator().setSourceFindEvent("text content", op -> op
				.addValue("attachment", getAttachmentJson()));
		return new StringTester<>(() -> new String(callRef().getByteContent()), returnObj, testContext, getEvaluator());
	}

	public R byteContent(ThrowingFunction<byte[],Boolean,?> byteTest) {
		return getEvaluator().testCondition(
				"byte content", op -> op
						.addValue("attachment", getAttachmentJson()),
				() -> ThrowingFunction.unchecked(byteTest).apply(callRef().getByteContent()), returnObj, testContext);
	}

	protected Map<String,Object> getAttachmentJson(){
		Attachment attachment = callRef();
		if(attachment == null){
			return null;
		}
		return JsonBuilder.object().addValue("filename", attachment.getFilename()).toMap();
	}
}
