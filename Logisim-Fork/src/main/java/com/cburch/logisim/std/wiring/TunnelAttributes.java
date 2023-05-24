/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.wiring;


import java.awt.Font;
import java.util.Arrays;
import java.util.List;

import com.cburch.logisim.circuit.Strings;
import com.cburch.logisim.comp.TextField;
import com.cburch.logisim.data.*;
import com.cburch.logisim.instance.StdAttr;

class TunnelAttributes extends AbstractAttributeSet {
	// Creazione proprità "modalità micro"

	public static final AttributeOption ATTR_NORMAL = new AttributeOption("NormalMode",
			Strings.getter("tunnelnormal"));

	public static final AttributeOption ATTR_MICRO = new AttributeOption("MicroMode",
			Strings.getter("tunnelmicro"));

	public static final Attribute<AttributeOption> ATTR_MODE = Attributes.forOption("selectedMode",
			com.cburch.logisim.circuit.Strings.getter("tunnelmode"),
			new AttributeOption[] { ATTR_NORMAL, ATTR_MICRO});

	private static final List<Attribute<?>> ATTRIBUTES = Arrays
			.asList(new Attribute<?>[] { StdAttr.FACING, StdAttr.WIDTH, StdAttr.LABEL, StdAttr.LABEL_FONT, ATTR_MODE});

	private Direction facing;
	private BitWidth width;
	private String label;
	private Font labelFont;
	private Bounds offsetBounds;
	private int labelX;
	private int labelY;
	private int labelHAlign;
	private int labelVAlign;
	private boolean mode;	// True normale || False micro

	public TunnelAttributes() {
		facing = Direction.EAST;
		width = BitWidth.ONE;
		label = "tunnel";
		labelFont = StdAttr.DEFAULT_LABEL_FONT;
		offsetBounds = null;
		mode = true;
		configureLabel();
	}

	private void configureLabel() {
		Direction facing = this.facing;
		int x;
		int y;
		int halign;
		int valign;
		int margin = Tunnel.ARROW_MARGIN;
		if (facing == Direction.NORTH) {
			x = 0;
			y = margin;
			halign = TextField.H_CENTER;
			valign = TextField.V_TOP;
		} else if (facing == Direction.SOUTH) {
			x = 0;
			y = -margin;
			halign = TextField.H_CENTER;
			valign = TextField.V_BOTTOM;
		} else if (facing == Direction.EAST) {
			x = -margin;
			y = 0;
			halign = TextField.H_RIGHT;
			valign = TextField.V_CENTER_OVERALL;
		} else {
			x = margin;
			y = 0;
			halign = TextField.H_LEFT;
			valign = TextField.V_CENTER_OVERALL;
		}
		labelX = x;
		labelY = y;
		labelHAlign = halign;
		labelVAlign = valign;
	}

	@Override
	protected void copyInto(AbstractAttributeSet destObj) {
		; // nothing to do
	}

	@Override
	public List<Attribute<?>> getAttributes() {
		return ATTRIBUTES;
	}

	Direction getFacing() {
		return facing;
	}

	Font getFont() {
		return labelFont;
	}

	String getLabel() {
		return label;
	}

	int getLabelHAlign() {
		return labelHAlign;
	}

	int getLabelVAlign() {
		return labelVAlign;
	}

	int getLabelX() {
		return labelX;
	}

	int getLabelY() {
		return labelY;
	}

	Bounds getOffsetBounds() {
		return offsetBounds;
	}

	@Override
	public <V> V getValue(Attribute<V> attr) {
		if (attr == StdAttr.FACING)
			return (V) facing;
		if (attr == StdAttr.WIDTH)
			return (V) width;
		if (attr == StdAttr.LABEL)
			return (V) label;
		if (attr == StdAttr.LABEL_FONT)
			return (V) labelFont;
		if (attr == ATTR_MODE) {
			if(mode) {
				return (V) Strings.getter("tunnelnormal");
			} else {
				return (V) Strings.getter("tunnelmicro");
			}
		}
		return null;
	}

	boolean setOffsetBounds(Bounds value) {
		Bounds old = offsetBounds;
		boolean same = old == null ? value == null : old.equals(value);
		if (!same) {
			offsetBounds = value;
		}
		return !same;
	}

	@Override
	public <V> void setValue(Attribute<V> attr, V value) {
		if (attr == StdAttr.FACING) {
			facing = (Direction) value;
			configureLabel();
		} else if (attr == StdAttr.WIDTH) {
			width = (BitWidth) value;
		} else if (attr == StdAttr.LABEL) {
			label = (String) value;
		} else if (attr == StdAttr.LABEL_FONT) {
			labelFont = (Font) value;
		} else if(attr == ATTR_MODE){
			if(value == ATTR_NORMAL) {
				mode = true;
			} else {
				mode = false;
			}
		} else {
			throw new IllegalArgumentException("unknown attribute");
		}
		offsetBounds = null;
		fireAttributeValueChanged(attr, value);
	}
}
