/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.appear;

import java.util.Collection;

import com.cburch.draw.canvas.Canvas;
import com.cburch.draw.canvas.Selection;
import com.cburch.draw.model.CanvasObject;
import com.cburch.logisim.circuit.appear.AppearanceElement;

public class AppearanceSelection extends Selection {
	public static boolean shouldSnap(Collection<? extends CanvasObject> shapes) {
		for (CanvasObject o : shapes) {
			if (o instanceof AppearanceElement) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setMovingDelta(int dx, int dy) {
		if (shouldSnap(getSelected())) {
			dx = Canvas.Snap(dx);
			dy = Canvas.Snap(dy);
		}
		super.setMovingDelta(dx, dy);
	}

	@Override
	public void setMovingShapes(Collection<? extends CanvasObject> shapes, int dx, int dy) {
		if (shouldSnap(shapes)) {
			dx = Canvas.Snap(dx);
			dy = Canvas.Snap(dy);
		}
		super.setMovingShapes(shapes, dx, dy);
	}
}
