/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.comp;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Location;

public class EndData {
	public static final byte INPUT_ONLY = 1;
	public static final byte OUTPUT_ONLY = 2;
	public static final byte INPUT_OUTPUT = 3;

	private Location loc;
	private BitWidth width;
	private byte i_o;
	private boolean exclusive;

	public EndData(Location loc, BitWidth width, byte type) {
		this(loc, width, type, type == OUTPUT_ONLY);
	}

	public EndData(Location loc, BitWidth width, byte type, boolean exclusive) {
		this.loc = loc;
		this.width = width;
		this.i_o = type;
		this.exclusive = exclusive;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof EndData))
			return false;
		if (other == this)
			return true;
		EndData o = (EndData) other;
		return o.loc.equals(this.loc) && o.width.equals(this.width) && o.i_o == this.i_o
				&& o.exclusive == this.exclusive;
	}

	public Location getLocation() {
		return loc;
	}

	public byte getType() {
		return i_o;
	}

	public BitWidth getWidth() {
		return width;
	}

	public boolean isExclusive() {
		return exclusive;
	}

	public boolean isInput() {
		return (i_o & INPUT_ONLY) != 0;
	}

	public boolean isOutput() {
		return (i_o & OUTPUT_ONLY) != 0;
	}
}
