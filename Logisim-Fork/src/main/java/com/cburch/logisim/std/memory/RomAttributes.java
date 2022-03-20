/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

import com.cburch.logisim.data.AbstractAttributeSet;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.gui.hex.HexFrame;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;

class RomAttributes extends AbstractAttributeSet {

	private static List<Attribute<?>> ATTRIBUTES = Arrays.asList(new Attribute<?>[] { Mem.ADDR_ATTR, Mem.DATA_ATTR,
			StdAttr.LABEL, StdAttr.LABEL_FONT, StdAttr.ATTR_LABEL_COLOR, Rom.CONTENTS_ATTR, Mem.ATTR_SELECTION, Mem.SIMPLE_MODE });

	private static WeakHashMap<MemContents, RomContentsListener> listenerRegistry = new WeakHashMap<MemContents, RomContentsListener>();
	private static WeakHashMap<MemContents, HexFrame> windowRegistry = new WeakHashMap<MemContents, HexFrame>();

	static HexFrame getHexFrame(MemContents value, Project proj) {
		synchronized (windowRegistry) {
			HexFrame ret = windowRegistry.get(value);
			if (ret == null) {
				ret = new HexFrame(proj, value);
				windowRegistry.put(value, ret);
			}
			return ret;
		}
	}

	static void register(MemContents value, Project proj) {
		if (proj == null || listenerRegistry.containsKey(value))
			return;
		RomContentsListener l = new RomContentsListener(proj);
		value.addHexModelListener(l);
		listenerRegistry.put(value, l);
	}

	private BitWidth addrBits = BitWidth.create(8);
	private BitWidth dataBits = BitWidth.create(8);
	private MemContents contents;
	private String label = "";
	private Font labelfont = StdAttr.DEFAULT_LABEL_FONT;
	private Color labelcolor = Color.BLACK;
	AttributeOption sel = Mem.SEL_LOW;
	private Boolean isSimple = Boolean.TRUE;
	
	RomAttributes() {
		contents = MemContents.create(addrBits.getWidth(), dataBits.getWidth());
	}

	@Override
	protected void copyInto(AbstractAttributeSet dest) {
		RomAttributes d = (RomAttributes) dest;
		d.addrBits = addrBits;
		d.dataBits = dataBits;
		d.label = label;
		d.labelfont = labelfont;
		d.labelcolor = labelcolor;
		d.contents = contents.clone();
		d.sel = sel;
		d.isSimple = isSimple;
	}

	@Override
	public List<Attribute<?>> getAttributes() {
		return ATTRIBUTES;
	}

	@Override
	public <V> V getValue(Attribute<V> attr) {
		if (attr == Mem.ADDR_ATTR)
			return (V) addrBits;
		if (attr == Mem.DATA_ATTR)
			return (V) dataBits;
		if (attr == StdAttr.LABEL)
			return (V) label;
		if (attr == StdAttr.LABEL_FONT)
			return (V) labelfont;
		if (attr == StdAttr.ATTR_LABEL_COLOR)
			return (V) labelcolor;
		if (attr == Rom.CONTENTS_ATTR)
			return (V) contents;
		if (attr == Mem.ATTR_SELECTION)
			return (V) sel;
		if (attr == Mem.SIMPLE_MODE)
			return (V) isSimple;
		return null;
	}

	void setProject(Project proj) {
		register(contents, proj);
	}

	@Override
	public <V> void setValue(Attribute<V> attr, V value) {
		if (attr == Mem.ADDR_ATTR) {
			addrBits = (BitWidth) value;
			contents.setDimensions(addrBits.getWidth(), dataBits.getWidth());
		} else if (attr == Mem.DATA_ATTR) {
			dataBits = (BitWidth) value;
			contents.setDimensions(addrBits.getWidth(), dataBits.getWidth());
		} else if (attr == StdAttr.LABEL) {
			label = (String) value;
		} else if (attr == StdAttr.LABEL_FONT) {
			labelfont = (Font) value;
		} else if (attr == StdAttr.ATTR_LABEL_COLOR) {
			labelcolor = (Color) value;
		} else if (attr == Rom.CONTENTS_ATTR) {
			contents = (MemContents) value;
		} else if (attr == Mem.ATTR_SELECTION) {
			sel = (AttributeOption) value;
		} else if (attr == Mem.SIMPLE_MODE) {
			isSimple = (Boolean) value;
		}
		fireAttributeValueChanged(attr, value);
	}
}
