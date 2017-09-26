/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.hex;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

public class Test {
	private static class Model implements HexModel {
		private ArrayList<HexModelListener> listeners = new ArrayList<HexModelListener>();
		private int[] data = new int[924];

		@Override
		public void addHexModelListener(HexModelListener l) {
			listeners.add(l);
		}

		@Override
		public void fill(long start, long len, int value) {
			int[] oldValues = new int[(int) len];
			System.arraycopy(data, (int) (start - 11111), oldValues, 0, (int) len);
			Arrays.fill(data, (int) (start - 11111), (int) len, value);
			for (HexModelListener l : listeners) {
				l.bytesChanged(this, start, len, oldValues);
			}
		}

		@Override
		public int get(long address) {
			return data[(int) (address - 11111)];
		}

		@Override
		public long getFirstOffset() {
			return 11111;
		}

		@Override
		public long getLastOffset() {
			return data.length + 11110;
		}

		@Override
		public int getValueWidth() {
			return 9;
		}

		@Override
		public void removeHexModelListener(HexModelListener l) {
			listeners.remove(l);
		}

		@Override
		public void set(long address, int value) {
			int[] oldValues = new int[] { data[(int) (address - 11111)] };
			data[(int) (address - 11111)] = value & 0x1FF;
			for (HexModelListener l : listeners) {
				l.bytesChanged(this, address, 1, oldValues);
			}
		}

		@Override
		public void set(long start, int[] values) {
			int[] oldValues = new int[values.length];
			System.arraycopy(data, (int) (start - 11111), oldValues, 0, values.length);
			System.arraycopy(values, 0, data, (int) (start - 11111), values.length);
			for (HexModelListener l : listeners) {
				l.bytesChanged(this, start, values.length, oldValues);
			}
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		HexModel model = new Model();
		HexEditor editor = new HexEditor(model);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().add(new JScrollPane(editor));
		frame.pack();
		frame.setVisible(true);
	}
}
