/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.file;

import java.util.Collections;
import java.util.List;

class XmlReaderException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8185235093673186621L;
	private List<String> messages;

	public XmlReaderException(List<String> messages) {
		this.messages = messages;
	}

	public XmlReaderException(String message) {
		this(Collections.singletonList(message));
	}

	@Override
	public String getMessage() {
		return messages.get(0);
	}

	public List<String> getMessages() {
		return messages;
	}
}
