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

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class Attachment {
	protected String filename;
	protected String contentType;
	protected byte[] byteContent;

	public Attachment(String filename, byte[] byteContent, String contentType) {
		this.filename = filename;
		this.contentType = contentType;
		this.byteContent = byteContent;
	}

	public Attachment(String filename, File contents, String contentType) {
		this(filename, fileBytes(contents), contentType);
	}

	public Attachment(File contents, String contentType) {
		this(contents.getName(), fileBytes(contents), contentType);
	}

	private static byte[] fileBytes(File file) {
		try {
			return Files.readAllBytes(file.toPath());
		}catch (Exception ex){
			throw new RuntimeException("Error: file byte copy failed for file [" + file.getAbsolutePath() + "]", ex);
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getByteContent() {
		return byteContent;
	}

	public void setByteContent(byte[] byteContent) {
		this.byteContent = byteContent;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Attachment))
			return false;
		Attachment that = (Attachment) o;
		return filename.equals(that.filename) &&
				contentType.equals(that.contentType) &&
				Arrays.equals(byteContent, that.byteContent);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(filename, contentType);
		result = 31 * result + Arrays.hashCode(byteContent);
		return result;
	}

	public static class TextAttachment extends Attachment {
		private Charset charset;

		public TextAttachment(String filename, String contents) {
			this(filename, contents, "text/plain");
		}

		public TextAttachment(String filename, String contents, String contentType) {
			super(filename, contents.getBytes(), contentType);
			this.charset = null;
		}

		public TextAttachment(String filename, String contents, Charset charset) {
			this(filename, contents, charset, "text/plain");
		}

		public TextAttachment(String filename, String contents, Charset charset, String contentType) {
			super(filename, contents.getBytes(charset), contentType);
			this.charset = charset;
		}

		public String getTextContent() {
			if (charset != null) {
				return new String(byteContent, charset);
			}
			return new String(byteContent);
		}

		public void setTextContent(String textContent) {
			this.charset = null;
			setByteContent(textContent.getBytes());
		}

		public void setTextContent(String textContent, Charset charset) {
			this.charset = charset;
			setByteContent(textContent.getBytes(charset));
		}
	}
}
