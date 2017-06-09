/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import com.cburch.logisim.util.StringUtil;

public class AnalyzeException extends Exception {
	public static class CannotHandle extends AnalyzeException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1601707682203573221L;

		public CannotHandle(String reason) {
			super(StringUtil.format(Strings.get("analyzeCannotHandleError"), reason));
		}
	}

	public static class Circular extends AnalyzeException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1592662892984310557L;

		public Circular() {
			super(Strings.get("analyzeCircularError"));
		}
	}

	public static class Conflict extends AnalyzeException {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5219702902313237585L;

		public Conflict() {
			super(Strings.get("analyzeConflictError"));
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1053839875326704219L;

	public AnalyzeException() {
	}

	public AnalyzeException(String message) {
		super(message);
	}
}
