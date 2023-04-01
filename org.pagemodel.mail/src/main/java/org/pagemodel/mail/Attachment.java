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

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
/**
 * The Attachment class represents an email attachment.
 * It contains the filename, content type, and byte content of the attachment.
 */
public class Attachment {
	protected String filename;
	protected String contentType;
	protected byte[] byteContent;

	/**
	 * Constructs an Attachment object with the given filename, byte content, and content type.
	 * @param filename the name of the attachment file
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
	 * @param filename the name of the attachment file
	 * @param contents the file contents of the attachment
	 * @param contentType the content type of the attachment
	 */
	public Attachment(String filename, File contents, String contentType) {
		this(filename, fileBytes(contents), contentType);
	}

	/**
	 * Constructs an Attachment object with the given file contents and content type.
	 * The filename is set to the name of the file.
	 * @param contents the file contents of the attachment
	 * @param contentType the content type of the attachment
	 */
	public Attachment(File contents, String contentType) {
		this(contents.getName(), fileBytes(contents), contentType);
	}

	/**
	 * Reads the byte content of the given file.
	 * @param file the file to read
	 * @return the byte content of the file
	 * @throws RuntimeException if the file byte copy fails
	 */
	private static byte[] fileBytes(File file) {
		try {
			return Files.readAllBytes(file.toPath());
		}catch (Exception ex){
			throw new RuntimeException("Error: file byte copy failed for file [" + file.getAbsolutePath() + "]", ex);
		}
	}
	// Improve memory efficiency for larger files, needs testing
//	private static byte[] fileBytes(File file) {
//		try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			byte[] buffer = new byte[4096];
//			int bytesRead;
//			while ((bytesRead = in.read(buffer)) != -1) {
//				out.write(buffer, 0, bytesRead);
//			}
//			return out.toByteArray();
//		} catch (IOException ex) {
//			throw new RuntimeException("Error: file byte copy failed for file [" + file.getAbsolutePath() + "]", ex);
//		}
//	}

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
	 * Checks if this Attachment object is equal to the given object.
	 * Two Attachment objects are equal if they have the same filename, content type, and byte content.
	 * @param o the object to compare to this Attachment object
	 * @return true if the objects are equal, false otherwise
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
	 * Returns the hash code of this Attachment object.
	 * The hash code is based on the filename, content type, and byte content.
	 * @return the hash code of this Attachment object
	 */
	@Override
	public int hashCode() {
		int result = Objects.hash(filename, contentType);
		result = 31 * result + Arrays.hashCode(byteContent);
		return result;
	}

	/**
	 * The TextAttachment class extends the Attachment class and represents a text attachment.
	 * It contains a charset field in addition to the filename, content type, and byte content fields.
	 */
	public static class TextAttachment extends Attachment {
		private Charset charset;

		/**
		 * Constructs a TextAttachment object with the given filename and text contents.
		 * The content type is set to "text/plain".
		 * @param filename the name of the attachment file
		 * @param contents the text contents of the attachment
		 */
		public TextAttachment(String filename, String contents) {
			this(filename, contents, "text/plain");
		}

		/**
		 * Constructs a TextAttachment object with the given filename, text contents, and content type.
		 * @param filename the name of the attachment file
		 * @param contents the text contents of the attachment
		 * @param contentType the content type of the attachment
		 */
		public TextAttachment(String filename, String contents, String contentType) {
			super(filename, contents.getBytes(), contentType);
			this.charset = null;
		}

		/**
		 * Constructs a TextAttachment object with the given filename, text contents, charset, and content type.
		 * @param filename the name of the attachment file
		 * @param contents the text contents of the attachment
		 * @param charset the charset of the text contents
		 * @param contentType the content type of the attachment
		 */
		public TextAttachment(String filename, String contents, Charset charset, String contentType) {
			super(filename, contents.getBytes(charset), contentType);
			this.charset = charset;
		}

		/**
		 * Returns the text content of the attachment.
		 * If the charset is not null, the byte content is decoded using the charset.
		 * @return the text content of the attachment
		 */
		public String getTextContent() {
			if (charset != null) {
				return new String(byteContent, charset);
			}
			return new String(byteContent);
		}

		/**
		 * Sets the text content of the attachment.
		 * The charset is set to null.
		 * @param textContent the new text content of the attachment
		 */
		public void setTextContent(String textContent) {
			this.charset = null;
			setByteContent(textContent.getBytes());
		}

		/**
		 * Sets the text content of the attachment with the given charset.
		 * @param textContent the new text content of the attachment
		 * @param charset the charset of the text content
		 */
		public void setTextContent(String textContent, Charset charset) {
			this.charset = charset;
			setByteContent(textContent.getBytes(charset));
		}
	}
}
