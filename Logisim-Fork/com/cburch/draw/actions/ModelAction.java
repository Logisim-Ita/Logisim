/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.actions;

import java.util.Collection;
import java.util.Collections;

import com.cburch.draw.model.CanvasModel;
import com.cburch.draw.model.CanvasObject;
import com.cburch.draw.undo.Action;

public abstract class ModelAction extends Action {
	static String getShapesName(Collection<CanvasObject> coll) {
		if (coll.size() != 1) {
			return Strings.get("shapeMultiple");
		} else {
			CanvasObject shape = coll.iterator().next();
			return shape.getDisplayName();
		}
	}

	private CanvasModel model;

	public ModelAction(CanvasModel model) {
		this.model = model;
	}

	@Override
	public final void doIt() {
		doSub(model);
	}

	abstract void doSub(CanvasModel model);

	public CanvasModel getModel() {
		return model;
	}

	@Override
	public abstract String getName();

	public Collection<CanvasObject> getObjects() {
		return Collections.emptySet();
	}

	@Override
	public final void undo() {
		undoSub(model);
	}

	abstract void undoSub(CanvasModel model);
}
