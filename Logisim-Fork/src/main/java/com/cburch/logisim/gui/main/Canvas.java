/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.event.MouseInputListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.cburch.logisim.Main;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitEvent;
import com.cburch.logisim.circuit.CircuitListener;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.Propagator;
import com.cburch.logisim.circuit.SimulatorEvent;
import com.cburch.logisim.circuit.SimulatorListener;
import com.cburch.logisim.circuit.SubcircuitFactory;
import com.cburch.logisim.circuit.WidthIncompatibilityData;
import com.cburch.logisim.circuit.WireSet;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentUserEvent;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.file.LibraryEvent;
import com.cburch.logisim.file.LibraryListener;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.file.MouseMappings;
import com.cburch.logisim.file.Options;
import com.cburch.logisim.gui.generic.CanvasPane;
import com.cburch.logisim.gui.generic.CanvasPaneContents;
import com.cburch.logisim.gui.generic.GridPainter;
import com.cburch.logisim.gui.start.Startup;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;
import com.cburch.logisim.std.io.Buzzer;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.EditTool;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.TextTool;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.tools.ToolTipMaker;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.StringGetter;

public class Canvas extends JPanel implements LocaleListener, CanvasPaneContents {
	private class MyListener
			implements MouseInputListener, KeyListener, PopupMenuListener, PropertyChangeListener, MouseWheelListener {
		boolean menu_on = false;

		private Tool getToolFor(MouseEvent e) {
			if (menu_on)
				return null;

			Tool ret = mappings.getToolFor(e);
			if (ret == null)
				return proj.getTool();
			else
				return ret;
		}

		//
		// KeyListener methods
		//
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_F5) {
				proj.getSimulator().requestReset();
				proj.getSimulator().setIsRunning(true);
			} else if ((e.getKeyCode() == KeyEvent.VK_0 || e.getKeyCode() == KeyEvent.VK_NUMPAD0)
					&& e.isControlDown()) {
				autoZoomCenter();
			} else {
				Tool tool = proj.getTool();
				if (tool != null)
					tool.keyPressed(Canvas.this, e);
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			Tool tool = proj.getTool();
			if (tool != null)
				tool.keyReleased(Canvas.this, e);
		}

		@Override
		public void keyTyped(KeyEvent e) {
			Tool tool = proj.getTool();
			if (tool != null)
				tool.keyTyped(Canvas.this, e);
		}

		//
		// MouseListener methods
		//
		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (drag_tool != null) {
				drag_tool.mouseDragged(Canvas.this, getGraphics(), e);
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			if (drag_tool != null) {
				drag_tool.mouseEntered(Canvas.this, getGraphics(), e);
			} else {
				Tool tool = getToolFor(e);
				if (tool != null) {
					tool.mouseEntered(Canvas.this, getGraphics(), e);
				}
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (drag_tool != null) {
				drag_tool.mouseExited(Canvas.this, getGraphics(), e);
			} else {
				Tool tool = getToolFor(e);
				if (tool != null) {
					tool.mouseExited(Canvas.this, getGraphics(), e);
				}
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if ((e.getModifiersEx() & BUTTONS_MASK) != 0) {
				// If the control key is down while the mouse is being
				// dragged, mouseMoved is called instead. This may well be
				// an issue specific to the MacOS Java implementation,
				// but it exists there in the 1.4 and 5.0 versions.
				mouseDragged(e);
				return;
			}

			Tool tool = getToolFor(e);
			if (tool != null) {
				tool.mouseMoved(Canvas.this, getGraphics(), e);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			viewport.setErrorMessage(null, null);
			viewport.setInfoMessage(null, null);
			if (proj.isStartupScreen()) {
				Graphics g = getGraphics();
				Bounds bounds;
				if (g != null)
					bounds = proj.getCurrentCircuit().getBounds(getGraphics());
				else
					bounds = proj.getCurrentCircuit().getBounds();
				// set the project as dirty only if it contains something
				if (bounds.getHeight() != 0 || bounds.getWidth() != 0)
					proj.setStartupScreen(false);
			}
			if (e.getButton() == MouseEvent.BUTTON1 && viewport.zoomButtonVisible
					&& AutoZoomButtonClicked(viewport.getSize(), e.getX() * getZoomFactor() - getHorizzontalScrollBar(),
							e.getY() * getZoomFactor() - getVerticalScrollBar())) {
				viewport.zoomButtonColor = defaultzoomButtonColor.darker();
				viewport.repaint();
			} else {
				Canvas.this.requestFocus();
				drag_tool = getToolFor(e);
				if (drag_tool != null) {
					drag_tool.mousePressed(Canvas.this, getGraphics(), e);
					if (e.getButton() != MouseEvent.BUTTON1) {
						temp_tool = proj.getTool();
						proj.setTool(drag_tool);
					}
				}
				completeAction();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if ((e.getButton() == MouseEvent.BUTTON1 && viewport.zoomButtonVisible
					&& AutoZoomButtonClicked(viewport.getSize(), e.getX() * getZoomFactor() - getHorizzontalScrollBar(),
							e.getY() * getZoomFactor() - getVerticalScrollBar())
					&& viewport.zoomButtonColor != defaultzoomButtonColor)
					|| e.getButton() == MouseEvent.BUTTON2 && e.getClickCount() == 2) {
				autoZoomCenter();
				setCursor(proj.getTool().getCursor());
			}
			if (drag_tool != null) {
				drag_tool.mouseReleased(Canvas.this, getGraphics(), e);
				drag_tool = null;
			}
			if (temp_tool != null) {
				proj.setTool(temp_tool);
				temp_tool = null;
			}
			Tool tool = proj.getTool();
			if (tool != null && !(tool instanceof EditTool)) {
				tool.mouseMoved(Canvas.this, getGraphics(), e);
				setCursor(tool.getCursor());
			}
			completeAction();

			viewport.zoomButtonColor = defaultzoomButtonColor;
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {// zoom mouse wheel
			if (e.getPreciseWheelRotation() < 0) {
				proj.getFrame().getZoomControl().spinnerModel
						.setValue(proj.getFrame().getZoomControl().spinnerModel.getNextValue());
			} else if (e.getPreciseWheelRotation() > 0) {
				proj.getFrame().getZoomControl().spinnerModel
						.setValue(proj.getFrame().getZoomControl().spinnerModel.getPreviousValue());
			}
		}

		//
		// PopupMenuListener methods
		//
		@Override
		public void popupMenuCanceled(PopupMenuEvent e) {
			menu_on = false;
		}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			menu_on = false;
		}

		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		}

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (AppPreferences.GATE_SHAPE.isSource(event) || AppPreferences.SHOW_TICK_RATE.isSource(event)
					|| AppPreferences.ANTI_ALIASING.isSource(event)
					|| AppPreferences.FILL_COMPONENT_BACKGROUND.isSource(event)) {
				paintThread.requestRepaint();
			} else if (AppPreferences.REFRESH_RATE.isSource(event)) {
				paintThread.setRefreshRate(Integer.parseInt(AppPreferences.REFRESH_RATE.get()));
			} else if (AppPreferences.COMPONENT_TIPS.isSource(event)) {
				boolean showTips = AppPreferences.COMPONENT_TIPS.getBoolean();
				setToolTipText(showTips ? "" : null);
			} else if (AppPreferences.LOOK_AND_FEEL.isSource(event)
					|| AppPreferences.GRAPHICS_ACCELERATION.isSource(event)) {
				int answer = JOptionPane.showConfirmDialog(null,
						new LocaleManager("resources/logisim", "prefs").getter("accelRestartLabel"),
						Strings.get("RestartRequired"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
				if (answer != 0)
					// User refused to update
					return;
				Startup.AsktoSave(proj.getFrame());
				Startup.restart(Main.OpenedFiles.toArray(new String[0]));
			} /*
				 * else if (AppPreferences.SEND_DATA.isSource(event)) { if
				 * (AppPreferences.SEND_DATA.getBoolean()) Startup.runRemotePhpCode(
				 * "http://logisim.altervista.org/LogisimData/OnlineUsers/online.php?val=1");
				 * else Startup.runRemotePhpCode(
				 * "http://logisim.altervista.org/LogisimData/OnlineUsers/online.php?val=0"); }
				 */
		}
	}

	private class MyProjectListener implements ProjectListener, LibraryListener, CircuitListener, AttributeListener,
			SimulatorListener, Selection.Listener {
		@Override
		public void attributeListChanged(AttributeEvent e) {
		}

		@Override
		public void attributeValueChanged(AttributeEvent e) {
			Attribute<?> attr = e.getAttribute();
			if (attr == Options.ATTR_GATE_UNDEFINED) {
				CircuitState circState = getCircuitState();
				circState.markComponentsDirty(getCircuit().getNonWires());
				// TODO actually, we'd want to mark all components in
				// subcircuits as dirty as well
			}
		}

		@Override
		public void circuitChanged(CircuitEvent event) {
			int act = event.getAction();
			if (act == CircuitEvent.ACTION_REMOVE) {
				Component c = (Component) event.getData();
				if (c == painter.getHaloedComponent()) {
					proj.getFrame().viewComponentAttributes(null, null);
				}
			} else if (act == CircuitEvent.ACTION_CLEAR) {
				if (painter.getHaloedComponent() != null) {
					proj.getFrame().viewComponentAttributes(null, null);
				}
			} else if (act == CircuitEvent.ACTION_INVALIDATE) {
				completeAction();
			}
		}

		@Override
		public void libraryChanged(LibraryEvent event) {
			if (event.getAction() == LibraryEvent.REMOVE_TOOL) {
				Object t = event.getData();
				Circuit circ = null;
				if (t instanceof AddTool) {
					t = ((AddTool) t).getFactory();
					if (t instanceof SubcircuitFactory) {
						circ = ((SubcircuitFactory) t).getSubcircuit();
					}
				}

				if (t == proj.getCurrentCircuit() && t != null) {
					proj.setCurrentCircuit(proj.getLogisimFile().getMainCircuit());
				}

				if (proj.getTool() == event.getData()) {
					Tool next = findTool(proj.getLogisimFile().getOptions().getToolbarData().getContents());
					if (next == null) {
						for (Library lib : proj.getLogisimFile().getLibraries()) {
							next = findTool(lib.getTools());
							if (next != null)
								break;
						}
					}
					proj.setTool(next);
				}

				if (circ != null) {
					CircuitState state = getCircuitState();
					CircuitState last = state;
					while (state != null && state.getCircuit() != circ) {
						last = state;
						state = state.getParentState();
					}
					if (state != null) {
						getProject().setCircuitState(last.cloneState());
					}
				}
				autoZoomCenter();
			}
		}

		@Override
		public void projectChanged(ProjectEvent event) {
			int act = event.getAction();
			if (act == ProjectEvent.ACTION_SET_CURRENT) {
				viewport.setErrorMessage(null, null);
				viewport.setInfoMessage(null, null);
				if (painter.getHaloedComponent() != null) {
					proj.getFrame().viewComponentAttributes(null, null);
				}
			} else if (act == ProjectEvent.ACTION_SET_FILE) {
				LogisimFile old = (LogisimFile) event.getOldData();
				if (old != null)
					old.getOptions().getAttributeSet().removeAttributeListener(this);
				LogisimFile file = (LogisimFile) event.getData();
				if (file != null) {
					AttributeSet attrs = file.getOptions().getAttributeSet();
					attrs.addAttributeListener(this);
					loadOptions(attrs);
					mappings = file.getOptions().getMouseMappings();
				}
			} else if (act == ProjectEvent.ACTION_SET_TOOL) {
				viewport.setErrorMessage(null, null);
				viewport.setInfoMessage(null, null);

				Tool t = event.getTool();
				if (t == null)
					setCursor(Cursor.getDefaultCursor());
				else
					setCursor(t.getCursor());
			} else if (act == ProjectEvent.ACTION_SET_STATE) {
				CircuitState oldState = (CircuitState) event.getOldData();
				CircuitState newState = (CircuitState) event.getData();
				if (oldState != null && newState != null) {
					Propagator oldProp = oldState.getPropagator();
					Propagator newProp = newState.getPropagator();
					if (oldProp != newProp) {
						tickCounter.clear();
					}
				}
			}

			if (act != ProjectEvent.ACTION_SELECTION && act != ProjectEvent.ACTION_START
					&& act != ProjectEvent.UNDO_START) {
				completeAction();
			}
		}

		@Override
		public void propagationCompleted(SimulatorEvent e) {
			/*
			 * This was a good idea for a while... but it leads to problems when a repaint
			 * is done just before a user action takes place. // repaint - but only if it's
			 * been a while since the last one long now = System.currentTimeMillis(); if
			 * (now > lastRepaint + repaintDuration) { lastRepaint = now; // (ensure that
			 * multiple requests aren't made repaintDuration = 15 + (int) (20 *
			 * Math.random()); // repaintDuration is for jittering the repaints to // reduce
			 * aliasing effects repaint(); }
			 */
			paintThread.requestRepaint();
		}

		@Override
		public void selectionChanged(Selection.Event event) {
			repaint();
		}

		@Override
		public void simulatorStateChanged(SimulatorEvent e) {
		}

		@Override
		public void tickCompleted(SimulatorEvent e) {
			waitForRepaintDone();
		}
	}

	private class MyViewport extends JViewport {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7658870263931307849L;
		StringGetter errorMessage = null;
		StringGetter infoMessage = null;
		Color errorColor = DEFAULT_ERROR_COLOR;
		Color infoColor = DEFAULT_INFO_COLOR;
		String widthMessage = null;
		boolean isNorth = false;
		boolean isSouth = false;
		boolean isWest = false;
		boolean isEast = false;
		boolean isNortheast = false;
		boolean isNorthwest = false;
		boolean isSoutheast = false;
		boolean isSouthwest = false;
		boolean zoomButtonVisible = false;
		Color zoomButtonColor = defaultzoomButtonColor;

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
			return getView() == null ? super.getBackground() : getView().getBackground();
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

			StringGetter message = errorMessage;
			
			if (message != null) {
				g.setColor(errorColor);
				paintString(g, message.get());
				return;
			}
			
			message = infoMessage;
			if (message != null) {
				g.setColor(infoColor);
				paintString(g, message.get());
				return;
			}

			if (proj.getSimulator().isOscillating()) {
				g.setColor(DEFAULT_ERROR_COLOR);
				paintString(g, Strings.get("canvasOscillationError"));
				return;
			}

			if (proj.getSimulator().isExceptionEncountered()) {
				g.setColor(DEFAULT_ERROR_COLOR);
				paintString(g, Strings.get("canvasExceptionError"));
				return;
			}

			computeViewportContents();
			Dimension sz = getSize();

			if (widthMessage != null) {
				g.setColor(Value.WIDTH_ERROR_COLOR);
				paintString(g, widthMessage);
			} else
				g.setColor(TICK_RATE_COLOR);
			if (isNorth || isSouth || isEast || isWest || isNortheast || isNorthwest || isSoutheast || isSouthwest) {
				zoomButtonVisible = true;
				paintAutoZoomButton(g, getSize(), zoomButtonColor);
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
			} else
				zoomButtonVisible = false;
			if (AppPreferences.SHOW_TICK_RATE.getBoolean()) {
				String hz = tickCounter.getTickRate();
				if (hz != null && !hz.equals("")) {
					g.setFont(TICK_RATE_FONT);
					FontMetrics fm = g.getFontMetrics();
					int x = getWidth() - fm.stringWidth(hz) - 10;
					int y = fm.getAscent() + 5;
					g.drawString(hz, x, y);
				}
			}
			g.setColor(Color.BLACK);

		}

		private void paintString(Graphics g, String msg) {
			Font old = g.getFont();
			g.setFont(old.deriveFont(Font.BOLD).deriveFont(18.0f));
			FontMetrics fm = g.getFontMetrics();
			int x = (getWidth() - fm.stringWidth(msg)) / 2;
			if (x < 0)
				x = 0;
			g.drawString(msg, x, getHeight() - 23);
			g.setFont(old);
			return;
		}

		void setEast(boolean value) {
			isEast = value;
		}

		void setErrorMessage(StringGetter msg, Color color) {
			if (infoMessage != null) //Error has priority
				setInfoMessage(null, null);
			if (errorMessage != msg) {
				errorMessage = msg;
				errorColor = color == null ? DEFAULT_ERROR_COLOR : color;
				paintThread.requestRepaint();
			}
		}

		void setInfoMessage(StringGetter msg, Color color) {
			if (errorMessage == null) { //Error has priority
				if (infoMessage != msg) {
					infoMessage = msg;
					infoColor = color == null ? DEFAULT_INFO_COLOR : color;
					paintThread.requestRepaint();
				}
			}
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

		void setWidthMessage(String msg) {
			widthMessage = msg;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4680658517189289645L;

	static final Color HALO_COLOR = new Color(200, 250, 255);

	// pixels shown in canvas beyond outermost boundaries
	private static final byte THRESH_SIZE_UPDATE = 10;
	// don't bother to update the size if it hasn't changed more than this
	static final double SQRT_2 = Math.sqrt(2.0);
	private static final int BUTTONS_MASK = InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON2_DOWN_MASK
			| InputEvent.BUTTON3_DOWN_MASK;

	public static final Color DEFAULT_ERROR_COLOR = new Color(192, 0, 0);
	public static final Color DEFAULT_INFO_COLOR = new Color(0, 0, 0);
	public static final Color TICK_RATE_COLOR = Color.GRAY;

	private static final Font TICK_RATE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);

	public static final byte zoomButtonSize = 52, zoomButtonMargin = 30;

	public static Color defaultzoomButtonColor = Color.WHITE;

	public static boolean AutoZoomButtonClicked(Dimension sz, double x, double y) {
		return Point2D.distance(x, y, sz.width - zoomButtonSize / 2 - zoomButtonMargin,
				sz.height - zoomButtonMargin - zoomButtonSize / 2) <= zoomButtonSize / 2;
	}

	public static Tool findTextTool(List<? extends Tool> opts) {
		Tool ret = null;
		for (Tool o : opts) {
			if (o instanceof TextTool) {
				ret = o;
				break;
			}
		}
		return ret;
	}

	public static Tool findTool(List<? extends Tool> opts) {
		Tool ret = null;
		for (Tool o : opts) {
			if (ret == null && o != null)
				ret = o;
			else if (o instanceof EditTool)
				ret = o;
		}
		return ret;
	}

	public static void paintAutoZoomButton(Graphics g, Dimension sz, Color zoomButtonColor) {
		Color oldcolor = g.getColor();
		g.setColor(TICK_RATE_COLOR);
		g.fillOval(sz.width - zoomButtonSize - 33, sz.height - zoomButtonSize - 33, zoomButtonSize + 6,
				zoomButtonSize + 6);
		g.setColor(zoomButtonColor);
		g.fillOval(sz.width - zoomButtonSize - 30, sz.height - zoomButtonSize - 30, zoomButtonSize, zoomButtonSize);
		g.setColor(Value.UNKNOWN_COLOR);
		GraphicsUtil.switchToWidth(g, 3);
		int width = sz.width - zoomButtonMargin;
		int height = sz.height - zoomButtonMargin;
		g.drawOval(width - zoomButtonSize * 3 / 4, height - zoomButtonSize * 3 / 4, zoomButtonSize / 2,
				zoomButtonSize / 2);
		g.drawLine(width - zoomButtonSize / 4 + 4, height - zoomButtonSize / 2, width - zoomButtonSize * 3 / 4 - 4,
				height - zoomButtonSize / 2);
		g.drawLine(width - zoomButtonSize / 2, height - zoomButtonSize / 4 + 4, width - zoomButtonSize / 2,
				height - zoomButtonSize * 3 / 4 - 4);
		g.setColor(oldcolor);
	}

	public static void snapToGrid(MouseEvent e) {
		int old_x = e.getX();
		int old_y = e.getY();
		int new_x = snapXToGrid(old_x);
		int new_y = snapYToGrid(old_y);
		e.translatePoint(new_x - old_x, new_y - old_y);
	}

	//
	// static methods
	//
	public static int snapXToGrid(int x) {
		if (x < 0) {
			return -((-x + 5) / 10) * 10;
		} else {
			return ((x + 5) / 10) * 10;
		}
	}

	public static int snapYToGrid(int y) {
		if (y < 0) {
			return -((-y + 5) / 10) * 10;
		} else {
			return ((y + 5) / 10) * 10;
		}
	}

	private Project proj;
	private Tool drag_tool, temp_tool;
	private Selection selection;
	private MouseMappings mappings;
	private CanvasPane canvasPane;
	private Bounds oldPreferredSize;

	private MyListener myListener = new MyListener();
	private MyViewport viewport = new MyViewport();
	private MyProjectListener myProjectListener = new MyProjectListener();
	private TickCounter tickCounter;
	private CanvasPaintThread paintThread;

	private CanvasPainter painter;

	private boolean paintDirty = false; // only for within paintComponent

	private boolean inPaint = false; // only for within paintComponent

	private Object repaintLock = new Object(); // for waitForRepaintDone

	public Canvas(Project proj) {
		this.proj = proj;
		this.selection = new Selection(proj, this);
		this.painter = new CanvasPainter(this);
		this.oldPreferredSize = null;
		this.paintThread = new CanvasPaintThread(this);
		this.mappings = proj.getOptions().getMouseMappings();
		this.canvasPane = null;
		this.tickCounter = new TickCounter();

		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		addMouseListener(myListener);
		addMouseMotionListener(myListener);
		addMouseWheelListener(myListener);
		addKeyListener(myListener);

		proj.addProjectListener(myProjectListener);
		proj.addLibraryListener(myProjectListener);
		proj.addCircuitListener(myProjectListener);
		proj.getSimulator().addSimulatorListener(tickCounter);
		selection.addListener(myProjectListener);
		LocaleManager.addLocaleListener(this);

		AttributeSet options = proj.getOptions().getAttributeSet();
		options.addAttributeListener(myProjectListener);
		AppPreferences.COMPONENT_TIPS.addPropertyChangeListener(myListener);
		AppPreferences.GATE_SHAPE.addPropertyChangeListener(myListener);
		AppPreferences.ANTI_ALIASING.addPropertyChangeListener(myListener);
		AppPreferences.FILL_COMPONENT_BACKGROUND.addPropertyChangeListener(myListener);
		// AppPreferences.SEND_DATA.addPropertyChangeListener(myListener);
		AppPreferences.SHOW_TICK_RATE.addPropertyChangeListener(myListener);
		AppPreferences.REFRESH_RATE.addPropertyChangeListener(myListener);
		AppPreferences.LOOK_AND_FEEL.addPropertyChangeListener(myListener);
		AppPreferences.GRAPHICS_ACCELERATION.addPropertyChangeListener(myListener);
		loadOptions(options);
		paintThread.start();
	}

	public void autoZoomCenter() {
		Graphics g = getGraphics();
		Bounds bounds;
		if (g != null)
			bounds = proj.getCurrentCircuit().getBounds(getGraphics());
		else
			bounds = proj.getCurrentCircuit().getBounds();
		if (bounds.getHeight() == 0 || bounds.getWidth() == 0) {
			setScrollBar(0, 0);
			return;
		}
		// the white space around
		byte padding = 50;
		// set autozoom
		double height = (bounds.getHeight() + 2 * padding) * getZoomFactor();
		double width = (bounds.getWidth() + 2 * padding) * getZoomFactor();
		double autozoom = getZoomFactor();
		if (canvasPane.getViewport().getSize().getWidth() / width < canvasPane.getViewport().getSize().getHeight()
				/ height) {
			autozoom *= canvasPane.getViewport().getSize().getWidth() / width;
		} else
			autozoom *= canvasPane.getViewport().getSize().getHeight() / height;
		if (Math.abs(autozoom - getZoomFactor()) >= 0.01)
			proj.getFrame().getZoomControl().spinnerModel.setValue(String.valueOf(autozoom * 100.0));
		// align at the center
		setScrollBar(
				(int) (Math.round(bounds.getX() * getZoomFactor()
						- (canvasPane.getViewport().getSize().getWidth() - bounds.getWidth() * getZoomFactor()) / 2)),
				(int) (Math.round(bounds.getY() * getZoomFactor()
						- (canvasPane.getViewport().getSize().getHeight() - bounds.getHeight() * getZoomFactor())
								/ 2)));
		setArrows();
	}

	public void closeCanvas() {
		for (Component comp : proj.getCurrentCircuit().getComponents())
			Buzzer.StopBuzzerSound(comp, proj.getCircuitState());
		paintThread.requestStop();
	}

	private void completeAction() {
		computeSize(false);
		// TODO for SimulatorPrototype: proj.getSimulator().releaseUserEvents();
		proj.getSimulator().requestPropagate();
		// repaint will occur after propagation completes
	}

	public void computeSize(boolean immediate) {
		Graphics g = getGraphics();
		Bounds bounds;
		if (g != null)
			bounds = proj.getCurrentCircuit().getBounds(getGraphics());
		else
			bounds = proj.getCurrentCircuit().getBounds();
		int height = 0, width = 0;
		if (bounds != null && viewport != null) {
			width = bounds.getX() + bounds.getWidth() + viewport.getWidth();
			height = bounds.getY() + bounds.getHeight() + viewport.getHeight();
		}
		Dimension dim;
		if (canvasPane == null) {
			dim = new Dimension(width, height);
		} else {
			dim = canvasPane.supportPreferredSize(width, height);
		}
		setArrows();
		if (!immediate) {
			Bounds old = oldPreferredSize;
			if (old != null && Math.abs(old.getWidth() - dim.width) < THRESH_SIZE_UPDATE
					&& Math.abs(old.getHeight() - dim.height) < THRESH_SIZE_UPDATE) {
				return;
			}
		}
		oldPreferredSize = Bounds.create(0, 0, dim.width, dim.height);
		setPreferredSize(dim);
		revalidate();
	}

	private void computeViewportContents() {
		Set<WidthIncompatibilityData> exceptions = proj.getCurrentCircuit().getWidthIncompatibilityData();
		if (exceptions == null || exceptions.size() == 0) {
			viewport.setWidthMessage(null);
			return;
		}
		viewport.setWidthMessage(
				Strings.get("canvasWidthError") + (exceptions.size() == 1 ? "" : " (" + exceptions.size() + ")"));
		for (WidthIncompatibilityData ex : exceptions) {
			Location p = ex.getPoint(0);
			setArrows(p.getX(), p.getY(), p.getX(), p.getY());
		}
	}

	//
	// access methods
	//
	public Circuit getCircuit() {
		return proj.getCurrentCircuit();
	}

	public CircuitState getCircuitState() {
		return proj.getCircuitState();
	}

	Tool getDragTool() {
		return drag_tool;
	}

	public StringGetter getErrorMessage() {
		return viewport.errorMessage;
	}
	
	public StringGetter getInfoMessage() {
		return viewport.infoMessage;
	}

	GridPainter getGridPainter() {
		return painter.getGridPainter();
	}

	Component getHaloedComponent() {
		return painter.getHaloedComponent();
	}

	public int getHorizzontalScrollBar() {
		return canvasPane.getHorizontalScrollBar().getValue();
	}

	@Override
	public Point getMousePosition() {
		return canvasPane.getMousePosition();
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	public Project getProject() {
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

	public Selection getSelection() {
		return selection;
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		boolean showTips = AppPreferences.COMPONENT_TIPS.getBoolean();
		if (showTips) {
			Canvas.snapToGrid(event);
			Location loc = Location.create(event.getX(), event.getY());
			ComponentUserEvent e = null;
			for (Component comp : getCircuit().getAllContaining(loc)) {
				Object makerObj = comp.getFeature(ToolTipMaker.class);
				if (makerObj != null && makerObj instanceof ToolTipMaker) {
					ToolTipMaker maker = (ToolTipMaker) makerObj;
					if (e == null) {
						e = new ComponentUserEvent(this, loc.getX(), loc.getY());
					}
					String ret = maker.getToolTip(e);
					if (ret != null) {
						unrepairMouseEvent(event);
						return ret;
					}
				}
			}
		}
		return null;
	}

	public int getVerticalScrollBar() {
		return canvasPane.getVerticalScrollBar().getValue();
	}

	public Rectangle getViewableRectangle() {
		Rectangle r = canvasPane.getViewport().getViewRect();
		double zoom = getZoomFactor();
		return new Rectangle((int) Math.round(r.x / zoom), (int) Math.round(r.y / zoom),
				(int) Math.round(r.width / zoom), (int) Math.round(r.height / zoom));
	}

	//
	// graphics methods
	//
	public double getZoomFactor() {
		CanvasPane pane = canvasPane;
		return pane == null ? 1.0 : pane.getZoomFactor();
	}

	boolean ifPaintDirtyReset() {
		if (paintDirty) {
			paintDirty = false;
			return false;
		} else {
			return true;
		}
	}

	boolean isPopupMenuUp() {
		return myListener.menu_on;
	}

	private void loadOptions(AttributeSet options) {
		boolean showTips = AppPreferences.COMPONENT_TIPS.getBoolean();
		setToolTipText(showTips ? "" : null);

		proj.getSimulator().removeSimulatorListener(myProjectListener);
		proj.getSimulator().addSimulatorListener(myProjectListener);
	}

	@Override
	public void localeChanged() {
		paintThread.requestRepaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		if (AppPreferences.ANTI_ALIASING.getBoolean()) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		inPaint = true;
		try {
			super.paintComponent(g);
			do {
				painter.paintContents(g, proj);
			} while (paintDirty);
			if (canvasPane == null)
				viewport.paintContents(g);
		} finally {
			inPaint = false;
			synchronized (repaintLock) {
				repaintLock.notifyAll();
			}
		}
	}

	@Override
	protected void processMouseEvent(MouseEvent e) {
		repairMouseEvent(e);
		super.processMouseEvent(e);
	}

	@Override
	protected void processMouseMotionEvent(MouseEvent e) {
		repairMouseEvent(e);
		super.processMouseMotionEvent(e);
	}

	@Override
	public void recomputeSize() {
		computeSize(true);
	}

	@Override
	public void repaint() {
		if (inPaint)
			paintDirty = true;
		else
			super.repaint();
	}

	@Override
	public void repaint(int x, int y, int width, int height) {
		double zoom = getZoomFactor();
		if (zoom < 1.0) {
			int newX = (int) Math.floor(x * zoom);
			int newY = (int) Math.floor(y * zoom);
			width += x - newX;
			height += y - newY;
			x = newX;
			y = newY;
		} else if (zoom > 1.0) {
			int x1 = (int) Math.ceil((x + width) * zoom);
			int y1 = (int) Math.ceil((y + height) * zoom);
			width = x1 - x;
			height = y1 - y;
		}
		super.repaint(x, y, width, height);
	}

	@Override
	public void repaint(Rectangle r) {
		double zoom = getZoomFactor();
		if (zoom == 1.0) {
			super.repaint(r);
		} else {
			this.repaint(r.x, r.y, r.width, r.height);
		}
	}

	private void repairMouseEvent(MouseEvent e) {
		double zoom = getZoomFactor();
		if (zoom != 1.0)
			zoomEvent(e, zoom);
	}

	public void setArrows() {
		Graphics g = getGraphics();
		Bounds circBds;
		if (g != null)
			circBds = proj.getCurrentCircuit().getBounds(getGraphics());
		else
			circBds = proj.getCurrentCircuit().getBounds();
		// no circuit
		if (circBds == null || circBds.getHeight() == 0 || circBds.getWidth() == 0)
			return;
		int x = circBds.getX();
		int y = circBds.getY();
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		setArrows(x, y, x + circBds.getWidth(), y + circBds.getHeight());
	}

	public void setArrows(int x0, int y0, int x1, int y1) {
		viewport.clearArrows();
		Rectangle viewableBase, viewable;
		if (canvasPane != null) {
			viewableBase = canvasPane.getViewport().getViewRect();
		} else {
			Graphics g = getGraphics();
			Bounds bds;
			if (g != null)
				bds = proj.getCurrentCircuit().getBounds(getGraphics());
			else
				bds = proj.getCurrentCircuit().getBounds();
			viewableBase = new Rectangle(0, 0, bds.getWidth(), bds.getHeight());
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
		setOpaque(false);
		computeSize(true);
	}

	public void setErrorMessage(StringGetter message) {
		viewport.setErrorMessage(message, null);
	}

	public void setErrorMessage(StringGetter message, Color color) {
		viewport.setErrorMessage(message, color);
	}

	public void setInfoMessage(StringGetter message) {
		viewport.setInfoMessage(message, null);
	}	
	
	public void setInfoMessage(StringGetter message, Color color) {
		viewport.setInfoMessage(message, color);
	}	
	
	void setHaloedComponent(Circuit circ, Component comp) {
		painter.setHaloedComponent(circ, comp);
	}

	public void setHighlightedWires(WireSet value) {
		painter.setHighlightedWires(value);
	}

	public void setHorizontalScrollBar(int X) {
		canvasPane.getHorizontalScrollBar().setValue(X);
	}

	public void setScrollBar(int X, int Y) {
		setHorizontalScrollBar(X);
		setVerticalScrollBar(Y);
	}

	public void setVerticalScrollBar(int Y) {
		canvasPane.getVerticalScrollBar().setValue(Y);
	}

	public void showPopupMenu(JPopupMenu menu, int x, int y) {
		double zoom = getZoomFactor();
		if (zoom != 1.0) {
			x = (int) Math.round(x * zoom);
			y = (int) Math.round(y * zoom);
		}
		myListener.menu_on = true;
		menu.addPopupMenuListener(myListener);
		menu.show(this, x, y);
	}

	private void unrepairMouseEvent(MouseEvent e) {
		double zoom = getZoomFactor();
		if (zoom != 1.0)
			zoomEvent(e, 1.0 / zoom);
	}

	private void waitForRepaintDone() {
		synchronized (repaintLock) {
			try {
				while (inPaint) {
					repaintLock.wait();
				}
			} catch (InterruptedException e) {
			}
		}
	}

	private void zoomEvent(MouseEvent e, double zoom) {
		int oldx = e.getX();
		int oldy = e.getY();
		int newx = (int) Math.round(e.getX() / zoom);
		int newy = (int) Math.round(e.getY() / zoom);
		e.translatePoint(newx - oldx, newy - oldy);
	}
}
