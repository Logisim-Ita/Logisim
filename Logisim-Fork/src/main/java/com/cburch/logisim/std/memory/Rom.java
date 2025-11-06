/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

import javax.swing.JLabel;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.hex.HexFile;
import com.cburch.logisim.gui.hex.HexFrame;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;

public class Rom extends Mem {
	private static class ContentsAttribute extends Attribute<MemContents> {
		private ContentsAttribute() {
			super("contents", Strings.getter("romContentsAttr"));
		}

		@Override
		public java.awt.Component getCellEditor(Window source, MemContents value) {
			if (source instanceof Frame) {
				Project proj = ((Frame) source).getProject();
				RomAttributes.register(value, proj);
			}
			ContentsCell ret = new ContentsCell(source, value);
			ret.mouseClicked(null);
			return ret;
		}

		@Override
		public MemContents parse(String value) {
			int lineBreak = value.indexOf('\n');
			String first = lineBreak < 0 ? value : value.substring(0, lineBreak);
			String rest = lineBreak < 0 ? "" : value.substring(lineBreak + 1);
			StringTokenizer toks = new StringTokenizer(first);
			try {
				String header = toks.nextToken();
				if (!header.equals("addr/data:"))
					return null;
				int addr = Integer.parseInt(toks.nextToken());
				int data = Integer.parseInt(toks.nextToken());
				MemContents ret = MemContents.create(addr, data);
				HexFile.open(ret, new StringReader(rest));
				return ret;
			} catch (IOException e) {
				return null;
			} catch (NumberFormatException e) {
				return null;
			} catch (NoSuchElementException e) {
				return null;
			}
		}

		@Override
		public String toDisplayString(MemContents value) {
			return Strings.get("romContentsValue");
		}

		@Override
		public String toStandardString(MemContents state) {
			int addr = state.getLogLength();
			int data = state.getWidth();
			StringWriter ret = new StringWriter();
			ret.write("addr/data: " + addr + " " + data + "\n");
			try {
				HexFile.save(ret, state);
			} catch (IOException e) {
			}
			return ret.toString();
		}
	}

	public static class ContentsCell extends JLabel implements MouseListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -53754819096800664L;
		Window source;
		MemContents contents;

		ContentsCell(Window source, MemContents contents) {
			super(Strings.get("romContentsValue"));
			this.source = source;
			this.contents = contents;
			addMouseListener(this);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (contents == null)
				return;
			Project proj = source instanceof Frame ? ((Frame) source).getProject() : null;
			HexFrame frame = RomAttributes.getHexFrame(contents, proj);
			frame.setVisible(true);
			frame.toFront();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}
	}

	public static Attribute<MemContents> CONTENTS_ATTR = new ContentsAttribute();

	// The following is so that instance's MemListeners aren't freed by the
	// garbage collector until the instance itself is ready to be freed.
	private WeakHashMap<Instance, MemListener> memListeners;

	public Rom() {
		super("ROM", Strings.getter("romComponent"), 0);
		setIconName("rom.gif");
		memListeners = new WeakHashMap<Instance, MemListener>();
	}

	@Override
	protected void configureNewInstance(Instance instance) {
		super.configureNewInstance(instance);
		instance.addAttributeListener();
		MemContents contents = getMemContents(instance);
		MemListener listener = new MemListener(instance);
		memListeners.put(instance, listener);
		contents.addHexModelListener(listener);
	}

	@Override
	void configurePorts(Instance instance) {
		Port[] ps = new Port[MEM_INPUTS];
		configureStandardPorts(instance, ps);
		if (instance.getAttributeValue(Mem.ATTR_SELECTION) == Mem.SEL_HIGH)
			ps[CS].setToolTip(Strings.getter("memCSTip", "0"));
		else
			ps[CS].setToolTip(Strings.getter("memCSTip", "1"));
		instance.setPorts(ps);

	}

	@Override
	public AttributeSet createAttributeSet() {
		return new RomAttributes();
	}

	@Override
	HexFrame getHexFrame(Project proj, Instance instance, CircuitState state) {
		return RomAttributes.getHexFrame(getMemContents(instance), proj);
	}

	// TODO - maybe delete this method?
	MemContents getMemContents(Instance instance) {
		return instance.getAttributeValue(CONTENTS_ATTR);
	}

	@Override
	MemState getState(Instance instance, CircuitState state) {
		MemState ret = (MemState) instance.getData(state);
		if (ret == null) {
			MemContents contents = getMemContents(instance);
			ret = new MemState(contents);
			instance.setData(state, ret);
		}
		return ret;
	}

	@Override
	MemState getState(InstanceState state) {
		MemState ret = (MemState) state.getData();
		if (ret == null) {
			MemContents contents = getMemContents(state.getInstance());
			ret = new MemState(contents);
			state.setData(ret);
		}
		return ret;
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		super.instanceAttributeChanged(instance, attr);
		if (attr == StdAttr.LABEL || attr == StdAttr.LABEL_FONT || attr == StdAttr.ATTR_LABEL_COLOR)
			return;
		configurePorts(instance);
		instance.fireInvalidated();
	}

	@Override
	public void propagate(InstanceState state) {
		MemState myState = getState(state);
		BitWidth dataBits = state.getAttributeValue(DATA_ATTR);

		Value addrValue = state.getPort(ADDR);
		boolean selection = state.getAttributeValue(ATTR_SELECTION) == SEL_HIGH;
		boolean chipSelect = !(state.getPort(CS) == Value.FALSE && selection
				|| state.getPort(CS) == Value.TRUE && !selection);

		if (!chipSelect) {
			myState.setCurrent(-1);
			state.setPort(DATA, Value.createUnknown(dataBits), DELAY);
			return;
		}

		int addr = addrValue.toIntValue();
		if (!addrValue.isFullyDefined() || addr < 0)
			return;
		if (addr != myState.getCurrent()) {
			myState.setCurrent(addr);
			myState.scrollToShow(addr);
		}

		int val = myState.getContents().get(addr);
		state.setPort(DATA, Value.createKnown(dataBits, val), DELAY);
	}
}
