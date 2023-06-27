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
/**
 * Represents an attachment with a filename, content type, and byte content.
 */
public class Attachment {
	protected String filename;
	protected String contentType;
	protected byte[] byteContent;

	/**
	 * Constructs an Attachment object with the given filename, byte content, and content type.
	 * @param filename the filename of the attachment
	 * @param byteContent the byte content of the attachment
	 * @param contentType the content type of the attachment
	 */
	public Attachment(String filename, byte[] byteContent, String contentType) {
		this.filename = filename;
		this.contentType = contentType;
		this.byteContent = byteContent;
	}

	/**
	 * Constructs an Attachment object with the given filename, file contents, and content type.
	 * @param filename the filename of the attachment
	 * @param contents the file contents of the attachment
	 * @param contentType the content type of the attachment
	 */
	public Attachment(String filename, File contents, String contentType) {
		this(filename, fileBytes(contents), contentType);
	}

	/**
	 * Constructs an Attachment object with the given file contents and content type.
	 * @param contents the file contents of the attachment
	 * @param contentType the content type of the attachment
	 */
	public Attachment(File contents, String contentType) {
		this(contents.getName(), fileBytes(contents), contentType);
	}

	/**
	 * Reads all bytes from the given file and returns them as a byte array.
	 * @param file the file to read from
	 * @return a byte array containing the contents of the file
	 * @throws RuntimeException if an error occurs while reading the file
	 */
	private static byte[] fileBytes(File file) {
		try {
			return Files.readAllBytes(file.toPath());
		}catch (Exception ex){
			throw new RuntimeException("Error: file byte copy failed for file [" + file.getAbsolutePath() + "]", ex);
		}
	}

	/**
	 * Returns the filename of the attachment.
	 * @return the filename of the attachment
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Sets the filename of the attachment.
	 * @param filename the new filename of the attachment
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * Returns the content type of the attachment.
	 * @return the content type of the attachment
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Sets the content type of the attachment.
	 * @param contentType the new content type of the attachment
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Returns the byte content of the attachment.
	 * @return the byte content of the attachment
	 */
	public byte[] getByteContent() {
		return byteContent;
	}

	/**
	 * Sets the byte content of the attachment.
	 * @param byteContent the new byte content of the attachment
	 */
	public void setByteContent(byte[] byteContent) {
		this.byteContent = byteContent;
	}

	/**
	 * Returns true if the given object is equal to this attachment.
	 * @param o the object to compare to this attachment
	 * @return true if the given object is equal to this attachment, false otherwise
	 */
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

	/**
	 * Returns the hash code of this attachment.
	 * @return the hash code of this attachment
	 */
	@Override
	public int hashCode() {
		int result = Objects.hash(filename, contentType);
		result = 31 * result + Arrays.hashCode(byteContent);
		return result;
	}

	/**
	 * Represents a text attachment with a filename, content type, and byte content.
	 */
	public static class TextAttachment extends Attachment {
		private Charset charset;

		/**
		 * Constructs a TextAttachment object with the given filename and contents.
		 * The content type is set to "text/plain".
		 * @param filename the filename of the text attachment
		 * @param contents the contents of the text attachment
		 */
		public TextAttachment(String filename, String contents) {
			this(filename, contents, "text/plain");
		}

		/**
		 * Constructs a TextAttachment object with the given filename, contents, and content type.
		 * 		 * @param filename the filename of the text attachment
		 * 		 * @param contents the contents of the text attachment
		 * 		 * @param contentType the content type of the text attachment
		 */
		public TextAttachment(String filename, String contents, String contentType) {
			super(filename, contents.getBytes(), contentType);
			this.charset = null;
		}

		/**
		 * Constructs a TextAttachment object with the given filename, contents, and charset.
		 * @param filename the filename of the text attachment
		 * @param contents the contents of the text attachment
		 * @param charset the charset of the text attachment
		 */
		public TextAttachment(String filename, String contents, Charset charset) {
			this(filename, contents, charset, "text/plain");
		}

		/**
		 * Constructs a TextAttachment object with the given filename, contents, charset, and content type.
		 * @param filename the filename of the text attachment
		 * @param contents the contents of the text attachment
		 * @param charset the charset of the text attachment
		 * @param contentType the content type of the text attachment
		 */
		public TextAttachment(String filename, String contents, Charset charset, String contentType) {
			super(filename, contents.getBytes(charset), contentType);
			this.charset = charset;
		}

		/**
		 * Returns the text content of the attachment as a string.
		 * @return the text content of the attachment as a string
		 */
		public String getTextContent() {
			if (charset != null) {
				return new String(byteContent, charset);
			}
			return new String(byteContent);
		}

		/**
		 * Sets the text content of the attachment as a string.
		 * The charset is set to null.
		 * @param textContent the new text content of the attachment
		 */
		public void setTextContent(String textContent) {
			this.charset = null;
			setByteContent(textContent.getBytes());
		}

		/**
		 * Sets the text content of the attachment as a string with the given charset.
		 * @param textContent the new text content of the attachment
		 * @param charset the charset of the text content
		 */
		public void setTextContent(String textContent, Charset charset) {
			this.charset = charset;
			setByteContent(textContent.getBytes(charset));
		}
	}
}
