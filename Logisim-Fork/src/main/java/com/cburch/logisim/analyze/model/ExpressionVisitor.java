/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.model;

public interface ExpressionVisitor<T> {
	public T visitAnd(Expression a, Expression b);

	public T visitConstant(int value);

	public T visitNot(Expression a);

	public T visitOr(Expression a, Expression b);

	public T visitVariable(String name);

	public T visitXor(Expression a, Expression b);
}
