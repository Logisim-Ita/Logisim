/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IteratorUtil {
	private static class ArrayIterator<E> implements Iterator<E> {
		private E[] data;
		private int i = -1;

		private ArrayIterator(E[] data) {
			this.data = data;
		}

		@Override
		public boolean hasNext() {
			return i + 1 < data.length;
		}

		@Override
		public E next() {
			if (!hasNext())
				throw new NoSuchElementException();
			i++;
			return data[i];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("ArrayIterator.remove");
		}
	}

	private static class EmptyIterator<E> implements Iterator<E> {
		private EmptyIterator() {
		}

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public E next() {
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("EmptyIterator.remove");
		}
	}

	private static class IteratorUnion<E> implements Iterator<E> {
		Iterator<? extends E> cur;
		Iterator<? extends E> next;

		private IteratorUnion(Iterator<? extends E> cur, Iterator<? extends E> next) {
			this.cur = cur;
			this.next = next;
		}

		@Override
		public boolean hasNext() {
			return cur.hasNext() || (next != null && next.hasNext());
		}

		@Override
		public E next() {
			if (!cur.hasNext()) {
				if (next == null)
					throw new NoSuchElementException();
				cur = next;
				if (!cur.hasNext())
					throw new NoSuchElementException();
			}
			return cur.next();
		}

		@Override
		public void remove() {
			cur.remove();
		}
	}

	private static class UnitIterator<E> implements Iterator<E> {
		private E data;
		private boolean taken = false;

		private UnitIterator(E data) {
			this.data = data;
		}

		@Override
		public boolean hasNext() {
			return !taken;
		}

		@Override
		public E next() {
			if (taken)
				throw new NoSuchElementException();
			taken = true;
			return data;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("UnitIterator.remove");
		}
	}

	public static Iterator<?> EMPTY_ITERATOR = new EmptyIterator<Object>();

	public static <E> Iterator<E> createArrayIterator(E[] data) {
		return new ArrayIterator<E>(data);
	}

	public static <E> Iterator<E> createJoinedIterator(Iterator<? extends E> i0, Iterator<? extends E> i1) {
		if (!i0.hasNext()) {
			Iterator<E> ret = (Iterator<E>) i1;
			return ret;
		} else if (!i1.hasNext()) {
			Iterator<E> ret = (Iterator<E>) i0;
			return ret;
		} else {
			return new IteratorUnion<E>(i0, i1);
		}
	}

	public static <E> Iterator<E> createUnitIterator(E data) {
		return new UnitIterator<E>(data);
	}

	public static <E> Iterator<E> emptyIterator() {
		return new EmptyIterator<E>();
	}

}
