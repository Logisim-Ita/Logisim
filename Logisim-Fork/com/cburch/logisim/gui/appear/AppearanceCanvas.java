/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.appear;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.JViewport;

import com.cburch.draw.actions.ModelAddAction;
import com.cburch.draw.actions.ModelReorderAction;
import com.cburch.draw.canvas.ActionDispatcher;
import com.cburch.draw.canvas.Canvas;
import com.cburch.draw.canvas.CanvasTool;
import com.cburch.draw.model.CanvasModel;
import com.cburch.draw.model.CanvasModelEvent;
import com.cburch.draw.model.CanvasModelListener;
import com.cburch.draw.model.CanvasObject;
import com.cburch.draw.model.ReorderRequest;
import com.cburch.draw.undo.Action;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.appear.AppearanceElement;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.gui.generic.CanvasPane;
import com.cburch.logisim.gui.generic.CanvasPaneContents;
import com.cburch.logisim.gui.generic.GridPainter;
import com.cburch.logisim.gui.generic.ZoomControl;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringGetter;

public class AppearanceCanvas extends Canvas implements CanvasPaneContents, ActionDispatcher {
	private class Listener implements CanvasModelListener, PropertyChangeListener, KeyListener, MouseListener,
			MouseMotionListener, MouseWheelListener {
		private final Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		private final Cursor move = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		private int x = 0, y = 0, ScrollBarX = 0, ScrollBarY = 0;
		private boolean mooving = false;

		@Override
		public void keyPressed(KeyEvent arg0) {
			if ((arg0.getKeyCode() == KeyEvent.VK_0 || arg0.getKeyCode() == KeyEvent.VK_NUMPAD0)
					&& arg0.isControlDown()) {
				CircuitState circState = circuitState;
				Bounds bounds;
				if (circState == null)
					bounds = Bounds.create(0, 0, 0, 0);
				else
					bounds = circState.getCircuit().getAppearance().getAbsoluteBounds();
				if (bounds.getHeight() == 0 || bounds.getWidth() == 0) {
					setScrollBar(0, 0);
					ZoomControl.spinnerModel.setValue(String.valueOf(100.0));
					return;
				}
				// the white space around
				int padding = 50;
				// set autozoom
				double height = (bounds.getHeight() + 2 * padding) * getZoomFactor();
				double width = (bounds.getWidth() + 2 * padding) * getZoomFactor();
				double autozoom = getZoomFactor();
				if (canvasPane.getViewport().getSize().getWidth()
						/ width < canvasPane.getViewport().getSize().getHeight() / height) {
					autozoom *= canvasPane.getViewport().getSize().getWidth() / width;
				} else
					autozoom *= canvasPane.getViewport().getSize().getHeight() / height;
				if (Math.abs(autozoom - getZoomFactor()) >= 0.01)
					ZoomControl.spinnerModel.setValue(String.valueOf(autozoom * 100.0));
				// align at the center
				setScrollBar((int) (Math.round(bounds.getX() * getZoomFactor()
						- (canvasPane.getViewport().getSize().getWidth() - bounds.getWidth() * getZoomFactor()) / 2)),
						(int) (Math
								.round(bounds.getY() * getZoomFactor() - (canvasPane.getViewport().getSize().getHeight()
										- bounds.getHeight() * getZoomFactor()) / 2)));
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
		}

		@Override
		public void modelChanged(CanvasModelEvent event) {
			computeSize(false);
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
		}

		@Override
		public void mouseDragged(MouseEvent arg0) {
			if (mooving) {
				setCursor(move);
				int x = Math.round(this.ScrollBarX + ((this.x * (float) getZoomFactor() - this.ScrollBarX)
						- (arg0.getX() * (float) getZoomFactor() - getHorizzontalScrollBar())));
				int y = Math.round(this.ScrollBarY + ((this.y * (float) getZoomFactor() - this.ScrollBarY)
						- (arg0.getY() * (float) getZoomFactor() - getVerticalScrollBar())));
				setScrollBar(x, y);
				setArrows();
			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			if (arg0.getButton() == MouseEvent.BUTTON2) {
				this.x = arg0.getX();
				this.y = arg0.getY();
				this.ScrollBarX = getHorizzontalScrollBar();
				this.ScrollBarY = getVerticalScrollBar();
				this.mooving = true;
			}

		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			if (arg0.getButton() == MouseEvent.BUTTON2) {
				setCursor(cursor);
				this.mooving = false;
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent arg0) {// zoom mouse wheel
			if (arg0.getPreciseWheelRotation() < 0) {
				ZoomControl.spinnerModel.setValue(ZoomControl.spinnerModel.getNextValue());
			} else if (arg0.getPreciseWheelRotation() > 0) {
				ZoomControl.spinnerModel.setValue(ZoomControl.spinnerModel.getPreviousValue());
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String prop = evt.getPropertyName();
			if (prop.equals(GridPainter.ZOOM_PROPERTY)) {
				CanvasTool t = getTool();
				if (t != null) {
					t.zoomFactorChanged(AppearanceCanvas.this);
				}
			}
		}
	}

	public class MyViewport extends JViewport {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7658870263931307849L;
		StringGetter errorMessage = null;
		Color errorColor = com.cburch.logisim.gui.main.Canvas.DEFAULT_ERROR_COLOR;
		boolean isNorth = false;
		boolean isSouth = false;
		boolean isWest = false;
		boolean isEast = false;
		boolean isNortheast = false;
		boolean isNorthwest = false;
		boolean isSoutheast = false;
		boolean isSouthwest = false;

		public MyViewport() {
		}

		void clearArrows() {
			isNorth = false;
			isSouth = false;
			isWest = false;
			isEast = false;
			isNortheast = false;
			isNorthwest = false;
			isSoutheast = false;
			isSouthwest = false;
		}

		@Override
		public Color getBackground() {
			return null;
		}

		@Override
		public void paintChildren(Graphics g) {
			super.paintChildren(g);
			paintContents(g);
		}

		void paintContents(Graphics g) {
			/*
			 * TODO this is for the SimulatorPrototype class int speed =
			 * proj.getSimulator().getSimulationSpeed(); String speedStr; if (speed >=
			 * 10000000) { speedStr = (speed / 1000000) + " MHz"; } else if (speed >=
			 * 1000000) { speedStr = (speed / 100000) / 10.0 + " MHz"; } else if (speed >=
			 * 10000) { speedStr = (speed / 1000) + " KHz"; } else if (speed >= 10000) {
			 * speedStr = (speed / 100) / 10.0 + " KHz"; } else { speedStr = speed + " Hz";
			 * } FontMetrics fm = g.getFontMetrics(); g.drawString(speedStr, getWidth() - 10
			 * - fm.stringWidth(speedStr), getHeight() - 10);
			 */
			if (AppPreferences.ANTI_ALIASING.getBoolean()) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}
			Dimension sz = getSize();

			g.setColor(com.cburch.logisim.gui.main.Canvas.TICK_RATE_COLOR);
			if (isNorth)
				GraphicsUtil.drawArrow2(g, sz.width / 2 - 20, 15, sz.width / 2, 5, sz.width / 2 + 20, 15);
			if (isSouth)
				GraphicsUtil.drawArrow2(g, sz.width / 2 - 20, sz.height - 15, sz.width / 2, sz.height - 5,
						sz.width / 2 + 20, sz.height - 15);
			if (isEast)
				GraphicsUtil.drawArrow2(g, sz.width - 15, sz.height / 2 + 20, sz.width - 5, sz.height / 2,
						sz.width - 15, sz.height / 2 - 20);
			if (isWest)
				GraphicsUtil.drawArrow2(g, 15, sz.height / 2 + 20, 5, sz.height / 2, 15, sz.height / 2 + -20);
			if (isNortheast)
				GraphicsUtil.drawArrow2(g, sz.width - 30, 5, sz.width - 5, 5, sz.width - 5, 30);
			if (isNorthwest)
				GraphicsUtil.drawArrow2(g, 30, 5, 5, 5, 5, 30);
			if (isSoutheast)
				GraphicsUtil.drawArrow2(g, sz.width - 30, sz.height - 5, sz.width - 5, sz.height - 5, sz.width - 5,
						sz.height - 30);
			if (isSouthwest)
				GraphicsUtil.drawArrow2(g, 30, sz.height - 5, 5, sz.height - 5, 5, sz.height - 30);

			g.setColor(Color.BLACK);

		}

		void setEast(boolean value) {
			isEast = value;
		}

		void setNorth(boolean value) {
			isNorth = value;
		}

		void setNortheast(boolean value) {
			isNortheast = value;
		}

		void setNorthwest(boolean value) {
			isNorthwest = value;
		}

		void setSouth(boolean value) {
			isSouth = value;
		}

		void setSoutheast(boolean value) {
			isSoutheast = value;
		}

		void setSouthwest(boolean value) {
			isSouthwest = value;
		}

		void setWest(boolean value) {
			isWest = value;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2997341273503780021L;
	private static final int BOUNDS_BUFFER = 70;

	// pixels shown in canvas beyond outermost boundaries
	private static final int THRESH_SIZE_UPDATE = 10;
	// don't bother to update the size if it hasn't changed more than this

	static int getMaxIndex(CanvasModel model) {
		List<CanvasObject> objects = model.getObjectsFromBottom();
		for (int i = objects.size() - 1; i >= 0; i--) {
			if (!(objects.get(i) instanceof AppearanceElement)) {
				return i;
			}
		}
		return -1;
	}

	private MyViewport viewport = new MyViewport();
	private CanvasTool selectTool;
	private Project proj;
	private CircuitState circuitState;
	private Listener listener;
	private GridPainter grid;
	private CanvasPane canvasPane;
	private Bounds oldPreferredSize;

	private LayoutPopupManager popupManager;

	public AppearanceCanvas(CanvasTool selectTool) {
		this.selectTool = selectTool;
		this.grid = new GridPainter(this);
		this.listener = new Listener();
		this.oldPreferredSize = null;
		addKeyListener(listener);
		addMouseListener(listener);
		addMouseMotionListener(listener);
		addMouseWheelListener(listener);
		setPreferredSize(new Dimension(Integer.MAX_VALUE / 100, Integer.MAX_VALUE / 100));
		setSelection(new AppearanceSelection());
		setTool(selectTool);

		CanvasModel model = super.getModel();
		if (model != null)
			model.addCanvasModelListener(listener);
		grid.addPropertyChangeListener(GridPainter.ZOOM_PROPERTY, listener);
	}

	private void computeSize(boolean immediate) {
		hidePopup();
		Bounds bounds;
		CircuitState circState = circuitState;
		if (circState == null) {
			bounds = Bounds.create(0, 0, 50, 50);
		} else {
			bounds = circState.getCircuit().getAppearance().getAbsoluteBounds();
		}
		int width = bounds.getX() + bounds.getWidth() + BOUNDS_BUFFER;
		int height = bounds.getY() + bounds.getHeight() + BOUNDS_BUFFER;
		Dimension dim;
		if (canvasPane == null) {
			dim = new Dimension(width, height);
		} else {
			dim = canvasPane.supportPreferredSize(width, height);
		}
		if (!immediate) {
			Bounds old = oldPreferredSize;
			if (old != null && Math.abs(old.getWidth() - dim.width) < THRESH_SIZE_UPDATE
					&& Math.abs(old.getHeight() - dim.height) < THRESH_SIZE_UPDATE) {
				return;
			}
		}
		setArrows();
		oldPreferredSize = Bounds.create(0, 0, dim.width, dim.height);
		setPreferredSize(dim);
		revalidate();
	}

	@Override
	public void doAction(Action canvasAction) {
		Circuit circuit = circuitState.getCircuit();
		if (!proj.getLogisimFile().contains(circuit)) {
			return;
		}

		if (canvasAction instanceof ModelReorderAction) {
			int max = getMaxIndex(getModel());
			ModelReorderAction reorder = (ModelReorderAction) canvasAction;
			List<ReorderRequest> rs = reorder.getReorderRequests();
			List<ReorderRequest> mod = new ArrayList<ReorderRequest>(rs.size());
			boolean changed = false;
			boolean movedToMax = false;
			for (ReorderRequest r : rs) {
				CanvasObject o = r.getObject();
				if (o instanceof AppearanceElement) {
					changed = true;
				} else {
					if (r.getToIndex() > max) {
						int from = r.getFromIndex();
						changed = true;
						movedToMax = true;
						if (from == max && !movedToMax) {
							; // this change is ineffective - don't add it
						} else {
							mod.add(new ReorderRequest(o, from, max));
						}
					} else {
						if (r.getToIndex() == max)
							movedToMax = true;
						mod.add(r);
					}
				}
			}
			if (changed) {
				if (mod.isEmpty()) {
					return;
				}
				canvasAction = new ModelReorderAction(getModel(), mod);
			}
		}

		if (canvasAction instanceof ModelAddAction) {
			ModelAddAction addAction = (ModelAddAction) canvasAction;
			int cur = addAction.getDestinationIndex();
			int max = getMaxIndex(getModel());
			if (cur > max) {
				canvasAction = new ModelAddAction(getModel(), addAction.getObjects(), max + 1);
			}
		}

		proj.doAction(new CanvasActionAdapter(circuit, canvasAction));
	}

	Circuit getCircuit() {
		return circuitState.getCircuit();
	}

	CircuitState getCircuitState() {
		return circuitState;
	}

	GridPainter getGridPainter() {
		return grid;
	}

	public int getHorizzontalScrollBar() {
		return canvasPane.getHorizontalScrollBar().getValue();
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	Project getProject() {
		return proj;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return canvasPane.supportScrollableBlockIncrement(visibleRect, orientation, direction);
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return canvasPane.supportScrollableUnitIncrement(visibleRect, orientation, direction);
	}

	public int getVerticalScrollBar() {
		return canvasPane.getVerticalScrollBar().getValue();
	}

	@Override
	public double getZoomFactor() {
		return grid.getZoomFactor();
	}

	private void hidePopup() {
		LayoutPopupManager man = popupManager;
		if (man != null) {
			man.hideCurrentPopup();
		}
	}

	@Override
	protected void paintBackground(Graphics g) {
		super.paintBackground(g);
		grid.paintGrid(g);
	}

	@Override
	protected void paintForeground(Graphics g) {
		double zoom = grid.getZoomFactor();
		Graphics gScaled = g.create();
		if (zoom != 1.0 && zoom != 0.0 && gScaled instanceof Graphics2D) {
			((Graphics2D) gScaled).scale(zoom, zoom);
		}
		super.paintForeground(gScaled);
		gScaled.dispose();
	}

	@Override
	protected void processMouseEvent(MouseEvent e) {
		repairEvent(e, grid.getZoomFactor());
		super.processMouseEvent(e);
	}

	@Override
	protected void processMouseMotionEvent(MouseEvent e) {
		repairEvent(e, grid.getZoomFactor());
		super.processMouseMotionEvent(e);
	}

	@Override
	public void recomputeSize() {
		computeSize(true);
		repaint();
	}

	@Override
	public void repaintCanvasCoords(int x, int y, int width, int height) {
		double zoom = grid.getZoomFactor();
		if (zoom != 1.0) {
			x = (int) (x * zoom - 1);
			y = (int) (y * zoom - 1);
			width = (int) (width * zoom + 4);
			height = (int) (height * zoom + 4);
		}
		super.repaintCanvasCoords(x, y, width, height);
	}

	private void repairEvent(MouseEvent e, double zoom) {
		if (zoom != 1.0) {
			int oldx = e.getX();
			int oldy = e.getY();
			int newx = (int) Math.round(e.getX() / zoom);
			int newy = (int) Math.round(e.getY() / zoom);
			e.translatePoint(newx - oldx, newy - oldy);
		}
	}

	public void setArrows() {
		viewport.clearArrows();
		// no circuit
		Bounds bounds;
		if (circuitState == null)
			bounds = Bounds.create(0, 0, 0, 0);
		else
			bounds = circuitState.getCircuit().getAppearance().getAbsoluteBounds();
		if (bounds.getHeight() == 0 || bounds.getWidth() == 0)
			return;
		// get circuit coords if null

		int x0 = bounds.getX();
		int x1 = bounds.getX() + bounds.getWidth();
		int y0 = bounds.getY();
		int y1 = bounds.getY() + bounds.getHeight();
		Rectangle viewableBase;
		Rectangle viewable;
		if (canvasPane != null) {
			viewableBase = canvasPane.getViewport().getViewRect();
		} else {
			viewableBase = new Rectangle(0, 0, bounds.getWidth(), bounds.getHeight());
		}
		double zoom = getZoomFactor();
		if (zoom == 1.0) {
			viewable = viewableBase;
		} else {
			viewable = new Rectangle((int) (viewableBase.x / zoom), (int) (viewableBase.y / zoom),
					(int) (viewableBase.width / zoom), (int) (viewableBase.height / zoom));
		}
		boolean isWest = x0 < viewable.x;
		boolean isEast = x1 >= viewable.x + viewable.width;
		boolean isNorth = y0 < viewable.y;
		boolean isSouth = y1 >= viewable.y + viewable.height;

		if (isNorth) {
			if (isEast)
				viewport.setNortheast(true);
			if (isWest)
				viewport.setNorthwest(true);
			if (!isWest && !isEast)
				viewport.setNorth(true);
		}
		if (isSouth) {
			if (isEast)
				viewport.setSoutheast(true);
			if (isWest)
				viewport.setSouthwest(true);
			if (!isWest && !isEast)
				viewport.setSouth(true);
		}
		if (isEast && !viewport.isSoutheast && !viewport.isNortheast)
			viewport.setEast(true);
		if (isWest && !viewport.isSouthwest && !viewport.isNorthwest)
			viewport.setWest(true);
	}

	//
	// CanvasPaneContents methods
	//
	@Override
	public void setCanvasPane(CanvasPane value) {
		canvasPane = value;
		canvasPane.setViewport(viewport);
		viewport.setView(this);
		computeSize(true);
		popupManager = new LayoutPopupManager(value, this);
	}

	public void setCircuit(Project proj, CircuitState circuitState) {
		this.proj = proj;
		this.circuitState = circuitState;
		Circuit circuit = circuitState.getCircuit();
		setModel(circuit.getAppearance(), this);
	}

	@Override
	public void setModel(CanvasModel value, ActionDispatcher dispatcher) {
		CanvasModel oldModel = super.getModel();
		if (oldModel != null) {
			oldModel.removeCanvasModelListener(listener);
		}
		super.setModel(value, dispatcher);
		if (value != null) {
			value.addCanvasModelListener(listener);
		}
	}

	public void setScrollBar(int X, int Y) {
		canvasPane.getHorizontalScrollBar().setValue(X);
		canvasPane.getVerticalScrollBar().setValue(Y);
	}

	@Override
	public void setTool(CanvasTool value) {
		hidePopup();
		super.setTool(value);
	}

	@Override
	public JPopupMenu showPopupMenu(MouseEvent e, CanvasObject clicked) {
		double zoom = grid.getZoomFactor();
		int x = (int) Math.round(e.getX() * zoom);
		int y = (int) Math.round(e.getY() * zoom);
		if (clicked != null && getSelection().isSelected(clicked)) {
			AppearanceEditPopup popup = new AppearanceEditPopup(this);
			popup.show(this, x, y);
			return popup;
		}
		return null;
	}

	@Override
	public void toolGestureComplete(CanvasTool tool, CanvasObject created) {
		if (tool == getTool() && tool != selectTool) {
			setTool(selectTool);
			if (created != null) {
				getSelection().clearSelected();
				getSelection().setSelected(created, true);
			}
		}
	}
}
