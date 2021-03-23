/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import java.awt.Graphics;
import java.util.List;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;

public interface CanvasObject {
	public abstract Handle canDeleteHandle(Location desired);

	public abstract Handle canInsertHandle(Location desired);

	public abstract boolean canMoveHandle(Handle handle);

	public abstract boolean canRemove();

	public abstract CanvasObject clone();

	public abstract boolean contains(Location loc, boolean assumeFilled);

	public Handle deleteHandle(Handle handle);

	public abstract AttributeSet getAttributeSet();

	public abstract Bounds getBounds();

	public abstract String getDisplayName();

	public abstract List<Handle> getHandles(HandleGesture gesture);

	public abstract <V> V getValue(Attribute<V> attr);

	public void insertHandle(Handle desired, Handle previous);

	public abstract boolean matches(CanvasObject other);

	public abstract int matchesHashCode();

	public Handle moveHandle(HandleGesture gesture);

	public abstract boolean overlaps(CanvasObject other);

	public abstract void paint(Graphics g, HandleGesture gesture);

	public <V> void setValue(Attribute<V> attr, V value);

	public void translate(int dx, int dy);
}
