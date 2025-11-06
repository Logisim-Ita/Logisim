/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitEvent;
import com.cburch.logisim.circuit.CircuitListener;
import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.ComponentUserEvent;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.gui.main.Canvas;
import com.cburch.logisim.gui.main.SelectionActions;
import com.cburch.logisim.proj.Action;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.base.Text;

public class TextTool extends Tool {
	private class MyListener implements CaretListener, CircuitListener {
		@Override
		public void circuitChanged(CircuitEvent event) {
			if (event.getCircuit() != caretCircuit) {
				event.getCircuit().removeCircuitListener(this);
				return;
			}
			int action = event.getAction();
			if (action == CircuitEvent.ACTION_REMOVE) {
				if (event.getData() == caretComponent) {
					caret.cancelEditing();
				}
			} else if (action == CircuitEvent.ACTION_CLEAR) {
				if (caretComponent != null) {
					caret.cancelEditing();
				}
			}
		}

		@Override
		public void editingCanceled(CaretEvent e) {
			if (e.getCaret() != caret) {
				e.getCaret().removeCaretListener(this);
				return;
			}
			caret.removeCaretListener(this);
			caretCircuit.removeCircuitListener(this);

			caretCircuit = null;
			caretComponent = null;
			caretCreatingText = false;
			caret = null;
			// reset edit tool after end edit
			if (Fromdoubleclick)
				resetEditTool();
		}

		@Override
		public void editingStopped(CaretEvent e) {
			if (e.getCaret() != caret) {
				e.getCaret().removeCaretListener(this);
				return;
			}
			caret.removeCaretListener(this);
			caretCircuit.removeCircuitListener(this);

			String val = caret.getText();
			boolean isEmpty = (val == null || val.equals(""));
			Action a;
			Project proj = caretCanvas.getProject();
			if (caretCreatingText) {
				if (!isEmpty) {
					CircuitMutation xn = new CircuitMutation(caretCircuit);
					xn.add(caretComponent);
					a = xn.toAction(Strings.getter("addComponentAction", Text.FACTORY.getDisplayGetter()));
				} else {
					a = null; // don't add the blank text field
				}
			} else {
				if (isEmpty && caretComponent.getFactory() instanceof Text) {
					CircuitMutation xn = new CircuitMutation(caretCircuit);
					xn.add(caretComponent);
					a = xn.toAction(Strings.getter("removeComponentAction", Text.FACTORY.getDisplayGetter()));
				} else {
					Object obj = caretComponent.getFeature(TextEditable.class);
					if (obj == null) { // should never happen
						a = null;
					} else {
						TextEditable editable = (TextEditable) obj;
						a = editable.getCommitAction(caretCircuit, e.getOldText(), e.getText());
					}
				}
			}

			caretCircuit = null;
			caretComponent = null;
			caretCreatingText = false;
			caret = null;

			if (a != null)
				proj.doAction(a);
			// reset edit tool after end edit
			if (Fromdoubleclick)
				resetEditTool();
		}

	}

	private static Cursor cursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);

	private MyListener listener = new MyListener();

	private AttributeSet attrs;
	private Caret caret = null;
	private boolean caretCreatingText = false, Fromdoubleclick = false;
	private Canvas caretCanvas = null;
	private Circuit caretCircuit = null;
	private Component caretComponent = null;

	public TextTool() {
		attrs = Text.FACTORY.createAttributeSet();
	}

	public void AddLabelforDoubleClick(Canvas canvas, Component comp, TextEditable editable, ComponentUserEvent event) {
		Project proj = canvas.getProject();
		Fromdoubleclick = true;
		caret = editable.getTextCaret(event, Fromdoubleclick);
		if (caret != null) {
			caretComponent = comp;
			caretCreatingText = false;
		}
		if (caret != null) {
			caretCanvas = canvas;
			caretCircuit = canvas.getCircuit();
			caret.addCaretListener(listener);
			caretCircuit.addCircuitListener(listener);
		}
		proj.repaintCanvas();
	}

	@Override
	public void deselect(Canvas canvas) {
		if (caret != null) {
			caret.stopEditing();
			caret = null;
		}
	}

	@Override
	public void draw(Canvas canvas, ComponentDrawContext context) {
		if (caret != null)
			caret.draw(context.getGraphics());
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof TextTool;
	}

	@Override
	public AttributeSet getAttributeSet() {
		return attrs;
	}

	@Override
	public Cursor getCursor() {
		return cursor;
	}

	@Override
	public String getDescription() {
		return Strings.get("textToolDesc");
	}

	@Override
	public String getDisplayName() {
		return Strings.get("textTool");
	}

	@Override
	public String getName() {
		return "Text Tool";
	}

	@Override
	public int hashCode() {
		return TextTool.class.hashCode();
	}

	@Override
	public void keyPressed(Canvas canvas, KeyEvent e) {
		if (caret != null) {
			caret.keyPressed(e);
			canvas.getProject().repaintCanvas();
		}
	}

	@Override
	public void keyReleased(Canvas canvas, KeyEvent e) {
		if (caret != null) {
			caret.keyReleased(e);
			canvas.getProject().repaintCanvas();
		}
	}

	@Override
	public void keyTyped(Canvas canvas, KeyEvent e) {
		if (caret != null) {
			caret.keyTyped(e);
			canvas.getProject().repaintCanvas();
		}
	}

	@Override
	public void mouseDragged(Canvas canvas, Graphics g, MouseEvent e) {
		if (caret != null && caret.getBounds(g).contains(e.getX(), e.getY())) {
			caret.mousePressed(e);
			canvas.getProject().repaintCanvas();
			return;
		}
	}

	@Override
	public void mousePressed(Canvas canvas, Graphics g, MouseEvent e) {
		Project proj = canvas.getProject();
		Circuit circ = canvas.getCircuit();
		Action act = SelectionActions.dropAll(canvas.getSelection());
		canvas.getProject().doAction(act);
		if (!proj.getLogisimFile().contains(circ)) {
			if (caret != null)
				caret.cancelEditing();
			canvas.setErrorMessage(Strings.getter("cannotModifyError"));
			return;
		}

		// Maybe user is clicking within the current caret.
		if (caret != null) {
			if (caret.getBounds(g).contains(e.getX(), e.getY())) { // Yes
				caret.mousePressed(e);
				proj.repaintCanvas();
				return;
			} else if (Fromdoubleclick) {// No. End the current caret
				caret.stopEditing();
				return;
			} else // No. End the current caret
				caret.stopEditing();
		}
		// caret will be null at this point

		// Otherwise search for a new caret.
		int x = e.getX();
		int y = e.getY();
		Location loc = Location.create(x, y);
		ComponentUserEvent event = new ComponentUserEvent(canvas, x, y);

		// First search in selection.
		for (Component comp : proj.getSelection().getComponentsContaining(loc, g)) {
			TextEditable editable = (TextEditable) comp.getFeature(TextEditable.class);
			if (editable != null) {
				caret = editable.getTextCaret(event, Fromdoubleclick);
				if (caret != null) {
					proj.getFrame().viewComponentAttributes(circ, comp);
					caretComponent = comp;
					caretCreatingText = false;
					break;
				}
			}
		}

		// Then search in circuit
		if (caret == null) {
			for (Component comp : circ.getAllContaining(loc, g)) {
				TextEditable editable = (TextEditable) comp.getFeature(TextEditable.class);
				if (editable != null) {
					caret = editable.getTextCaret(event, Fromdoubleclick);
					if (caret != null) {
						proj.getFrame().viewComponentAttributes(circ, comp);
						caretComponent = comp;
						caretCreatingText = false;
						break;
					}
				}
			}
		}

		// if nothing found, create a new label
		if (caret == null) {
			if (loc.getX() < 0 || loc.getY() < 0)
				return;
			AttributeSet copy = (AttributeSet) attrs.clone();
			caretComponent = Text.FACTORY.createComponent(loc, copy);
			caretCreatingText = true;
			TextEditable editable = (TextEditable) caretComponent.getFeature(TextEditable.class);
			if (editable != null) {
				caret = editable.getTextCaret(event, Fromdoubleclick);
				proj.getFrame().viewComponentAttributes(circ, caretComponent);
			}
		}

		if (caret != null) {
			caretCanvas = canvas;
			caretCircuit = canvas.getCircuit();
			caret.addCaretListener(listener);
			caretCircuit.addCircuitListener(listener);
		}
		proj.repaintCanvas();
	}

	@Override
	public void mouseReleased(Canvas canvas, Graphics g, MouseEvent e) {
		// TODO: enhance label editing
	}

	@Override
	public void paintIcon(ComponentDrawContext c, int x, int y) {
		Text.FACTORY.paintIcon(c, x, y, null);
	}

	private void resetEditTool() {
		Project proj = caretCanvas.getProject();
		Tool tool = Canvas.findTool(proj.getLogisimFile().getOptions().getToolbarData().getContents());
		if (tool == null) {
			for (Library lib : proj.getLogisimFile().getLibraries()) {
				tool = Canvas.findTool(lib.getTools());
				if (tool != null)
					break;
			}
			if (tool == null)
				tool = new TextTool();
		}
		proj.setTool(tool);
		Fromdoubleclick = false;
	}
}
