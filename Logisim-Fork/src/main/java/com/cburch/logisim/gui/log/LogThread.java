/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.cburch.logisim.data.Value;

class LogThread extends Thread implements ModelListener {
	// file will be flushed with at least this frequency
	private static final int FLUSH_FREQUENCY = 500;

	// file will be closed after waiting this many milliseconds between writes
	private static final int IDLE_UNTIL_CLOSE = 10000;

	private Model model;
	private boolean canceled = false;
	private Object lock = new Object();
	private PrintWriter writer = null;
	private boolean headerDirty = true;
	private long lastWrite = 0;

	public LogThread(Model model) {
		this.model = model;
		model.addModelListener(this);
	}

	// Should hold lock and have verified that isFileEnabled() before
	// entering this method.
	private void addEntry(Value[] values) {
		if (writer == null) {
			if (model.getFile().length() == 0)
				headerDirty = true;
			try {
				writer = new PrintWriter(new FileWriter(model.getFile(), true));
			} catch (IOException e) {
				model.setFile(null);
				return;
			}
		}
		Selection sel = model.getSelection();
		if (headerDirty) {
			if (model.getFileHeader()) {
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < sel.size(); i++) {
					if (i > 0) {
						buf.append("  ");
						int l = (sel.get(i - 1).toString() + "  ").length()
								- values[i - 1].toDisplayString(sel.get(i - 1).getRadix()).length();
						if (l < 2)
							for (int j = 0; j < (-l + 2); j++)
								buf.append(" ");
					}
					buf.append(sel.get(i).toString());
				}
				writer.println(buf.toString());
			}
			headerDirty = false;
		}
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			if (i > 0) {
				int l = (sel.get(i - 1).toString() + "  ").length()
						- values[i - 1].toDisplayString(sel.get(i - 1).getRadix()).length();
				if (l > 2)
					for (int j = 0; j < l; j++)
						buf.append(" ");
				else
					buf.append("  ");
			}
			if (values[i] != null) {
				int radix = sel.get(i).getRadix();
				buf.append(values[i].toDisplayString(radix));
			}
		}
		writer.println(buf.toString());
		lastWrite = System.currentTimeMillis();
	}

	public void cancel() {
		synchronized (lock) {
			canceled = true;
			if (writer != null) {
				writer.close();
				writer = null;
			}
		}
	}

	@Override
	public void entryAdded(ModelEvent event, Value[] values) {
		synchronized (lock) {
			if (isFileEnabled())
				addEntry(values);
		}
	}

	public void fileChanged() {
		if (writer != null) {
			writer.close();
			writer = null;
		}
	}

	@Override
	public void filePropertyChanged(ModelEvent event) {
		synchronized (lock) {
			if (isFileEnabled()) {
				if (writer == null) {
					Selection sel = model.getSelection();
					Value[] values = new Value[sel.size()];
					boolean found = false;
					for (int i = 0; i < values.length; i++) {
						values[i] = model.getValueLog(sel.get(i)).getLast();
						if (values[i] != null)
							found = true;
					}
					if (found)
						addEntry(values);
				}
			} else {
				if (writer != null) {
					writer.close();
					writer = null;
				}
			}
		}
	}

	private boolean isFileEnabled() {
		return !canceled && model.isSelected() && model.isFileEnabled() && model.getFile() != null;
	}

	@Override
	public void run() {
		while (!canceled) {
			synchronized (lock) {
				if (writer != null) {
					if (System.currentTimeMillis() - lastWrite > IDLE_UNTIL_CLOSE) {
						writer.close();
						writer = null;
					} else {
						writer.flush();
					}
				}
			}
			try {
				Thread.sleep(FLUSH_FREQUENCY);
			} catch (InterruptedException e) {
			}
		}
		synchronized (lock) {
			if (writer != null) {
				writer.close();
				writer = null;
			}
		}
	}

	@Override
	public void selectionChanged(ModelEvent event) {
		headerDirty = true;
	}
}
