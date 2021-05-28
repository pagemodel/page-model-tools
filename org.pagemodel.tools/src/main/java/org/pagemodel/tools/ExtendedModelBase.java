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
import org.pagemodel.core.utils.ThrowingFunction;
import org.pagemodel.core.utils.json.JsonObjectBuilder;
import org.pagemodel.mail.MailTester;
import org.pagemodel.ssh.SSHConnectionTester;
import org.pagemodel.web.PageModel;

import java.util.function.Consumer;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public interface ExtendedModelBase<P extends ExtendedModelBase<? super P>> extends PageModel<P> {

	public ExtendedTestContext getContext();

	default public MailTester<P> testMail() {
		return new MailTester<>(getContext(), (P)this, new TestEvaluator.Now());
	}

	default public SSHConnectionTester<P> testSSH() {
		return new SSHConnectionTester<>((P) this, getContext(), getEvaluator());
	}

	default public P testSSH(ThrowingFunction<? super SSHConnectionTester<P>, P, ?> sshAction) {
		return new SSHConnectionTester<>((P) this, getContext(), getEvaluator()).doAction(sshAction);
	}

	@Override
	ExtendedPageTester<P> testPage();

	@Experimental
	default public P log(String message){
		this.getEvaluator().logMessage(message);
		return (P)this;
	}

	@Experimental
	default public P log(String action, Consumer<JsonObjectBuilder> eventParams){
		this.getEvaluator().logEvent(TestEvaluator.TEST_LOG, action, eventParams);
		return (P)this;
	}
}
