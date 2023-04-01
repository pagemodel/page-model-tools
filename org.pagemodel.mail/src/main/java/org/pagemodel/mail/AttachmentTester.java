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
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * The AttachmentTester class provides methods for testing properties of an email attachment.
 * It is used to test whether an attachment exists, its filename, content type, and byte content.
 *
 * @param <R> the return type of the test method
 *
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class AttachmentTester<R> {

	/**
	 * The return object of the test method.
	 */
	protected R returnObj;

	/**
	 * The callable reference to the attachment being tested.
	 */
	protected final Callable<Attachment> ref;

	/**
	 * The test context for the test.
	 */
	protected final TestContext testContext;

	/**
	 * The test evaluator for the test.
	 */
	private TestEvaluator testEvaluator;

	/**
	 * Constructs an AttachmentTester object with the given parameters.
	 *
	 * @param ref the callable reference to the attachment being tested
	 * @param returnObj the return object of the test method
	 * @param testContext the test context for the test
	 * @param testEvaluator the test evaluator for the test
	 */
	public AttachmentTester(Callable<Attachment> ref, R returnObj, TestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	/**
	 * Calls the callable reference to the attachment being tested.
	 *
	 * @return the attachment being tested, or null if an exception is thrown
	 */
	protected Attachment callRef(){
		try{
			return ref.call();
		}catch (Throwable t){
			return null;
		}
	}

	/**
	 * Gets the test evaluator for the test.
	 *
	 * @return the test evaluator for the test
	 */
	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	/**
	 * Tests whether the attachment exists.
	 *
	 * @return the return object of the test method
	 */
	public R exists() {
		return getEvaluator().testCondition(
				"exists", op -> op
						.addValue("attachment", getAttachmentJson()),
				() -> callRef() != null, returnObj, testContext);
	}

	/**
	 * Tests whether the attachment does not exist.
	 *
	 * @return the return object of the test method
	 */
	public R notExists() {
		return getEvaluator().testCondition(
				"not exists", op -> op
						.addValue("attachment", getAttachmentJson()),
				() -> callRef() == null, returnObj, testContext);
	}

	/**
	 * Tests the filename of the attachment.
	 *
	 * @return a StringTester object for testing the filename
	 */
	public StringTester<R> filename() {
		getEvaluator().setSourceFindEvent("filename", op -> op
				.addValue("attachment", getAttachmentJson()));
		return new StringTester<>(() -> callRef().getFilename(), returnObj, testContext, getEvaluator());
	}

	/**
	 * Tests the content type of the attachment.
	 *
	 * @return a StringTester object for testing the content type
	 */
	public StringTester<R> contentType() {
		getEvaluator().setSourceFindEvent("content type", op -> op
				.addValue("attachment", getAttachmentJson()));
		return new StringTester<>(() -> callRef().getContentType(), returnObj, testContext, getEvaluator());
	}

	/**
	 * Tests the text content of the attachment.
	 *
	 * @return a StringTester object for testing the text content
	 */
	public StringTester<R> textContent() {
		getEvaluator().setSourceFindEvent("text content", op -> op
				.addValue("attachment", getAttachmentJson()));
		return new StringTester<>(() -> new String(callRef().getByteContent()), returnObj, testContext, getEvaluator());
	}

	/**
	 * Tests the byte content of the attachment with the given byteTest function.
	 *
	 * @param byteTest the function to test the byte content
	 * @return the return object of the test method
	 */
	public R byteContent(ThrowingFunction<byte[],Boolean,?> byteTest) {
		return getEvaluator().testCondition(
				"byte content", op -> op
						.addValue("attachment", getAttachmentJson()),
				() -> ThrowingFunction.unchecked(byteTest).apply(callRef().getByteContent()), returnObj, testContext);
	}

	/**
	 * Gets a JSON representation of the attachment being tested.
	 *
	 * @return a Map object representing the attachment
	 */
	protected Map<String,Object> getAttachmentJson(){
		Attachment attachment = callRef();
		if(attachment == null){
			return null;
		}
		return JsonBuilder.object().addValue("filename", attachment.getFilename()).toMap();
	}
}