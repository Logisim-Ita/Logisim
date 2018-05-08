/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.wiring;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.RadixOption;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.main.Canvas;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstanceLogger;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.MenuExtender;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.tools.key.DirectionConfigurator;
import com.cburch.logisim.tools.key.JoinedConfigurator;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.Icons;
import com.cburch.logisim.util.LocaleManager;

public class Pin extends InstanceFactory {
	public static class PinLogger extends InstanceLogger {
		@Override
		public String getLogName(InstanceState state, Object option) {
			PinAttributes attrs = (PinAttributes) state.getAttributeSet();
			String ret = attrs.label;
			if (ret == null || ret.equals("")) {
				String type = attrs.type == EndData.INPUT_ONLY ? Strings.get("pinInputName")
						: Strings.get("pinOutputName");
				return type + state.getInstance().getLocation();
			} else {
				return ret;
			}
		}

		@Override
		public Value getLogValue(InstanceState state, Object option) {
			PinState s = getState(state);
			return s.sending;
		}
	}

	private static class PinMenu implements ActionListener, MenuExtender {
		private JMenuItem edit;
		private Instance instance;
		private CircuitState circState;

		public PinMenu(Instance instance) {
			this.instance = instance;
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			if (evt.getSource() == edit) {
				PinState data = Pin.getState(instance, circState);
				int CurrentValue = data.sending.toIntValue();
				// pop up input dialog
				String input = (String) JOptionPane.showInputDialog(null, Strings.get("constantValueAttr"),
						Strings.get("constantValueAttr"), JOptionPane.PLAIN_MESSAGE, null, null,
						CurrentValue & 0xffffffffL);
				// something typed
				if (input != null && !input.equals("")) {
					byte CurrentWidth = (byte) data.sending.getWidth();
					double MaxValue = Math.pow(2, CurrentWidth);
					long TypedValue;
					// should avoid some problem
					if (input.length() > 10 && input.charAt(0) != '-') {
						// set max number
						TypedValue = (long) MaxValue - 1;
					} else if (input.length() > 11 && input.charAt(0) == '-') {
						// set minimum negative number
						TypedValue = 1 << CurrentWidth - 1;
					} else {
						try {
							// get value from input text
							TypedValue = Long.parseLong(input);
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(null,
									new LocaleManager("resources/logisim", "menu").get("FrequencyNumberNotAccepted"));
							return;
						}
					}
					// check if bigger or equal to than max - 1
					if (TypedValue >= MaxValue) {
						TypedValue = (long) MaxValue - 1;
					} // check if smaller than -max/2
					else if (TypedValue < -MaxValue / 2) {
						// minimum negative number
						TypedValue = 1 << CurrentWidth - 1;
					}
					// if different from old value
					if (TypedValue != (CurrentValue & 0xffffffffL)) {
						data.sending = Value.createKnown(BitWidth.create(CurrentWidth), (int) TypedValue);
						instance.fireInvalidated();
					}
				}
			}
		}

		@Override
		public void configureMenu(JPopupMenu menu, Project proj) {
			this.circState = proj.getCircuitState();
			// enabled if input
			boolean enabled = circState != null && !instance.getAttributeValue(Pin.ATTR_TYPE);
			this.edit = createItem(enabled, Strings.get("ramEditMenuItem"));
			menu.addSeparator();
			menu.add(this.edit);
		}

		private JMenuItem createItem(boolean enabled, String label) {
			JMenuItem ret = new JMenuItem(label);
			ret.setEnabled(enabled);
			ret.addActionListener(this);
			return ret;
		}
	}

	public static class PinPoker extends InstancePoker {
		int bitPressed = -1;

		private int getBit(InstanceState state, MouseEvent e) {
			BitWidth width = state.getAttributeValue(StdAttr.WIDTH);
			if (width.getWidth() == 1) {
				return 0;
			} else {
				// intentionally with no graphics object - we don't want label
				// included
				Direction facing = state.getAttributeValue(StdAttr.FACING);
				// intentionally with no graphics object - we don't want label included
				Bounds bds = state.getInstance().getBounds();
				int x = bds.getX();
				int y = bds.getY();
				int w = bds.getWidth();
				int h = bds.getHeight();
				if (facing == Direction.EAST || facing == Direction.WEST) {
					w -= 5;
					if (facing == Direction.WEST)
						x += 5;
				} else {
					h -= 5;
					if (facing == Direction.NORTH)
						y += 5;
				}
				bds = Bounds.create(x, y, w, h);
				int i = (bds.getX() + bds.getWidth() - e.getX()) / 10;
				int j = (bds.getY() + bds.getHeight() - e.getY()) / 20;
				int bit = 8 * j + i;
				if (bit < 0 || bit >= width.getWidth()) {
					return -1;
				} else {
					return bit;
				}
			}
		}

		private void handleBitPress(InstanceState state, int bit, MouseEvent e) {
			PinAttributes attrs = (PinAttributes) state.getAttributeSet();
			if (!attrs.isInput())
				return;

			java.awt.Component sourceComp = e.getComponent();
			if (sourceComp instanceof Canvas && !state.isCircuitRoot()) {
				Canvas canvas = (Canvas) e.getComponent();
				CircuitState circState = canvas.getCircuitState();
				java.awt.Component frame = SwingUtilities.getRoot(canvas);
				int choice = JOptionPane.showConfirmDialog(frame, Strings.get("pinFrozenQuestion"),
						Strings.get("pinFrozenTitle"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (choice == JOptionPane.OK_OPTION) {
					circState = circState.cloneState();
					canvas.getProject().setCircuitState(circState);
					state = circState.getInstanceState(state.getInstance());
				} else {
					return;
				}
			}

			PinState pinState = getState(state);
			Value val = pinState.sending.get(bit);
			if (val == Value.FALSE) {
				val = Value.TRUE;
			} else if (val == Value.TRUE) {
				val = attrs.threeState ? Value.UNKNOWN : Value.FALSE;
			} else {
				val = Value.FALSE;
			}
			pinState.sending = pinState.sending.set(bit, val);
			state.fireInvalidated();
		}

		@Override
		public void mousePressed(InstanceState state, MouseEvent e) {
			bitPressed = getBit(state, e);
		}

		@Override
		public void mouseReleased(InstanceState state, MouseEvent e) {
			int bit = getBit(state, e);
			if (bit == bitPressed && bit >= 0) {
				handleBitPress(state, bit, e);
			}
			bitPressed = -1;
		}
	}

	private static class PinState implements InstanceData, Cloneable {
		Value sending;
		Value receiving;

		public PinState(Value sending, Value receiving) {
			this.sending = sending;
			this.receiving = receiving;
		}

		@Override
		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	}

	public static final Attribute<Boolean> ATTR_TRISTATE = Attributes.forBoolean("tristate",
			Strings.getter("pinThreeStateAttr"));
	public static final Attribute<Boolean> ATTR_TYPE = Attributes.forBoolean("output", Strings.getter("pinOutputAttr"));
	public static final Attribute<Direction> ATTR_LABEL_LOC = Attributes.forDirection("labelloc",
			Strings.getter("pinLabelLocAttr"));
	public static final AttributeOption PULL_NONE = new AttributeOption("none", Strings.getter("pinPullNoneOption"));

	public static final AttributeOption PULL_UP = new AttributeOption("up", Strings.getter("pinPullUpOption"));

	public static final AttributeOption PULL_DOWN = new AttributeOption("down", Strings.getter("pinPullDownOption"));
	public static final Attribute<AttributeOption> ATTR_PULL = Attributes.forOption("pull",
			Strings.getter("pinPullAttr"), new AttributeOption[] { PULL_NONE, PULL_UP, PULL_DOWN });
	public static final Pin FACTORY = new Pin();

	private static final Icon ICON_IN = Icons.getIcon("pinInput.gif");

	private static final Icon ICON_OUT = Icons.getIcon("pinOutput.gif");

	private static final Icon ICON_IN_MULTI = Icons.getIcon("pinInputMulti.gif");

	private static final Icon ICON_OUT_MULTI = Icons.getIcon("pinOutputMulti.gif");

	private static final Font ICON_WIDTH_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 8);

	private static PinState getState(Instance state, CircuitState circ) {
		PinAttributes attrs = (PinAttributes) state.getAttributeSet();
		BitWidth width = attrs.width;
		PinState ret = (PinState) state.getData(circ);
		if (ret == null) {
			Value val = attrs.threeState ? Value.UNKNOWN : Value.FALSE;
			if (width.getWidth() > 1) {
				Value[] arr = new Value[width.getWidth()];
				java.util.Arrays.fill(arr, val);
				val = Value.create(arr);
			}
			ret = new PinState(val, val);
			state.setData(circ, ret);
		}
		if (ret.sending.getWidth() != width.getWidth()) {
			ret.sending = ret.sending.extendWidth(width.getWidth(), attrs.threeState ? Value.UNKNOWN : Value.FALSE);
		}
		if (ret.receiving.getWidth() != width.getWidth()) {
			ret.receiving = ret.receiving.extendWidth(width.getWidth(), Value.UNKNOWN);
		}
		return ret;
	}

	private static PinState getState(InstanceState state) {
		PinAttributes attrs = (PinAttributes) state.getAttributeSet();
		BitWidth width = attrs.width;
		PinState ret = (PinState) state.getData();
		if (ret == null) {
			Value val = attrs.threeState ? Value.UNKNOWN : Value.FALSE;
			if (width.getWidth() > 1) {
				Value[] arr = new Value[width.getWidth()];
				java.util.Arrays.fill(arr, val);
				val = Value.create(arr);
			}
			ret = new PinState(val, val);
			state.setData(ret);
		}
		if (ret.sending.getWidth() != width.getWidth()) {
			ret.sending = ret.sending.extendWidth(width.getWidth(), attrs.threeState ? Value.UNKNOWN : Value.FALSE);
		}
		if (ret.receiving.getWidth() != width.getWidth()) {
			ret.receiving = ret.receiving.extendWidth(width.getWidth(), Value.UNKNOWN);
		}
		return ret;
	}

	private static Value pull2(Value mod, BitWidth expectedWidth) {
		if (mod.getWidth() == expectedWidth.getWidth()) {
			Value[] vs = mod.getAll();
			for (int i = 0; i < vs.length; i++) {
				if (vs[i] == Value.UNKNOWN)
					vs[i] = Value.FALSE;
			}
			return Value.create(vs);
		} else {
			return Value.createKnown(expectedWidth, 0);
		}
	}

	public Pin() {
		super("Pin", Strings.getter("pinComponent"));
		setFacingAttribute(StdAttr.FACING);
		setKeyConfigurator(JoinedConfigurator.create(new BitWidthConfigurator(StdAttr.WIDTH),
				new DirectionConfigurator(ATTR_LABEL_LOC, InputEvent.ALT_DOWN_MASK)));
		setInstanceLogger(PinLogger.class);
		setInstancePoker(PinPoker.class);
	}

	//
	// methods for instances
	//
	@Override
	protected void configureNewInstance(Instance instance) {
		PinAttributes attrs = (PinAttributes) instance.getAttributeSet();
		instance.addAttributeListener();
		configurePorts(instance);
		Probe.configureLabel(instance, attrs.labelloc, attrs.facing);
	}

	private void configurePorts(Instance instance) {
		PinAttributes attrs = (PinAttributes) instance.getAttributeSet();
		String endType = attrs.isOutput() ? Port.INPUT : Port.OUTPUT;
		Port port = new Port(0, 0, endType, StdAttr.WIDTH);
		if (attrs.isOutput()) {
			port.setToolTip(Strings.getter("pinOutputToolTip"));
		} else {
			port.setToolTip(Strings.getter("pinInputToolTip"));
		}
		instance.setPorts(new Port[] { port });
	}

	@Override
	public AttributeSet createAttributeSet() {
		return new PinAttributes();
	}

	@Override
	protected Object getInstanceFeature(Instance instance, Object key) {
		if (key == MenuExtender.class) {
			return new PinMenu(instance);
		}
		return super.getInstanceFeature(instance, key);
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		Direction facing = attrs.getValue(StdAttr.FACING);
		BitWidth width = attrs.getValue(StdAttr.WIDTH);
		Bounds bds = Probe.getOffsetBounds(facing, width, RadixOption.RADIX_2);
		if (facing == Direction.EAST)
			return Bounds.create(bds.getX() - 5, bds.getY(), bds.getWidth() + 5, bds.getHeight());
		else if (facing == Direction.WEST)
			return Bounds.create(bds.getX(), bds.getY(), bds.getWidth() + 5, bds.getHeight());
		else if (facing == Direction.NORTH)
			return Bounds.create(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight() + 5);
		else // SOUTH
			return Bounds.create(bds.getX(), bds.getY() - 5, bds.getWidth(), bds.getHeight() + 5);
	}

	public int getType(Instance instance) {
		PinAttributes attrs = (PinAttributes) instance.getAttributeSet();
		return attrs.type;
	}

	//
	// state information methods
	//
	public Value getValue(InstanceState state) {
		return getState(state).sending;
	}

	//
	// basic information methods
	//
	public BitWidth getWidth(Instance instance) {
		PinAttributes attrs = (PinAttributes) instance.getAttributeSet();
		return attrs.width;
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == ATTR_TYPE) {
			configurePorts(instance);
		} else if (attr == StdAttr.WIDTH || attr == StdAttr.FACING || attr == Pin.ATTR_LABEL_LOC) {
			instance.recomputeBounds();
			PinAttributes attrs = (PinAttributes) instance.getAttributeSet();
			Probe.configureLabel(instance, attrs.labelloc, attrs.facing);
		} else if (attr == Pin.ATTR_TRISTATE || attr == Pin.ATTR_PULL) {
			instance.fireInvalidated();
		}
	}

	public boolean isInputPin(Instance instance) {
		PinAttributes attrs = (PinAttributes) instance.getAttributeSet();
		return attrs.type != EndData.OUTPUT_ONLY;
	}

	private void paintBase(InstancePainter painter, boolean isGhost) {
		PinAttributes attrs = (PinAttributes) painter.getAttributeSet();
		Graphics g = painter.getGraphics();
		Direction facing = attrs.getValue(StdAttr.FACING);
		// intentionally with no graphics object - we don't want label included
		Bounds bds;
		if (!isGhost)
			bds = painter.getInstance().getBounds();
		else
			bds = painter.getBounds();
		int x = bds.getX();
		int y = bds.getY();
		short width = (short) bds.getWidth();
		short height = (short) bds.getHeight();
		if (facing == Direction.EAST || facing == Direction.WEST) {
			width -= 5;
			if (facing == Direction.WEST)
				x += 5;
		} else {
			height -= 5;
			if (facing == Direction.NORTH)
				y += 5;
		}
		GraphicsUtil.switchToWidth(g, 2);
		PinState state = null;
		if (!isGhost)
			state = getState(painter);
		// outline coordinates
		int[] xPoints;
		int[] yPoints;
		if (facing == Direction.WEST) {
			xPoints = new int[] { x, bds.getX(), x, x + width, x + width };
			yPoints = new int[] { bds.getY(), bds.getY() + bds.getHeight() / 2, bds.getY() + bds.getHeight(),
					bds.getY() + bds.getHeight(), bds.getY() };
			if (attrs.type == EndData.OUTPUT_ONLY) {
				xPoints[4] -= 10;
				xPoints[3] -= 10;
			}
		} else if (facing == Direction.EAST) {
			xPoints = new int[] { x + width, x + bds.getWidth(), x + width, x, x };
			yPoints = new int[] { bds.getY(), bds.getY() + bds.getHeight() / 2, bds.getY() + bds.getHeight(),
					bds.getY() + bds.getHeight(), bds.getY() };
			if (attrs.type == EndData.OUTPUT_ONLY) {
				xPoints[4] += 10;
				xPoints[3] += 10;
			}
		} else if (facing == Direction.NORTH) {
			xPoints = new int[] { bds.getX(), bds.getX() + bds.getWidth() / 2, bds.getX() + bds.getWidth(),
					bds.getX() + bds.getWidth(), bds.getX() };
			yPoints = new int[] { y, bds.getY(), y, y + height, y + height };
			if (attrs.type == EndData.OUTPUT_ONLY) {
				yPoints[4] -= 10;
				yPoints[3] -= 10;
			}
		} else {// SOUTH
			xPoints = new int[] { bds.getX(), bds.getX() + bds.getWidth() / 2, bds.getX() + bds.getWidth(),
					bds.getX() + bds.getWidth(), bds.getX() };
			yPoints = new int[] { y + height, y + bds.getHeight(), y + height, y, y };
			if (attrs.type == EndData.OUTPUT_ONLY) {
				yPoints[4] += 10;
				yPoints[3] += 10;
			}
		}
		// draw shape
		Color bgColor = Color.WHITE;
		if (!isGhost) {
			if (attrs.width.getWidth() <= 1)
				bgColor = state.receiving.getColor();
			g.setColor(bgColor);
		}
		if (attrs.type == EndData.OUTPUT_ONLY) {
			if (!isGhost) {
				// output pin
				g.fillRoundRect(x, y, width, height, 20, 20);
				g.fillPolygon(new int[] { xPoints[4], xPoints[0], xPoints[1], xPoints[2], xPoints[3] },
						new int[] { yPoints[4], yPoints[0], yPoints[1], yPoints[2], yPoints[3] }, 5);
				g.setColor(Color.BLACK);
			}
			g.drawPolyline(new int[] { xPoints[4], xPoints[0], xPoints[1], xPoints[2], xPoints[3] },
					new int[] { yPoints[4], yPoints[0], yPoints[1], yPoints[2], yPoints[3] }, 5);
			if (facing == Direction.NORTH || facing == Direction.SOUTH) {
				if (width > 20)
					g.drawLine(xPoints[4] + 10, yPoints[4] + (facing == Direction.NORTH ? 10 : -10), xPoints[3] - 10,
							yPoints[3] + (facing == Direction.NORTH ? 10 : -10));
				yPoints[3] -= 10;
				yPoints[4] -= 10;
				xPoints[3] -= 20;
			} else {
				if (height > 20)
					g.drawLine(xPoints[4] + (facing == Direction.WEST ? 10 : -10), yPoints[4] + 10,
							xPoints[3] + (facing == Direction.WEST ? 10 : -10), yPoints[3] - 10);
				xPoints[3] -= 10;
				xPoints[4] -= 10;
				yPoints[3] -= 20;
			}
			g.drawArc(xPoints[4], yPoints[4], 20, 20, facing.toDegrees() - 180,
					(facing == Direction.NORTH || facing == Direction.EAST) ? -90 : 90);
			g.drawArc(xPoints[3], yPoints[3], 20, 20, facing.toDegrees() - 180,
					(facing == Direction.NORTH || facing == Direction.EAST) ? 90 : -90);
		} else {
			if (!isGhost) {
				// input pin
				g.fillPolygon(xPoints, yPoints, 5);
				g.setColor(Color.BLACK);
			}
			g.drawPolygon(xPoints, yPoints, 5);
		}
		if (!isGhost) {
			// print value
			if (attrs.width.getWidth() == 1) {
				g.setColor(Color.WHITE);
				GraphicsUtil.drawCenteredText(g, state.sending.toDisplayString(), x + 10, y + 8);
			} else
				Probe.paintValue(painter, Bounds.create(x, y, width, height), state.sending);
		}
	}

	@Override
	public void paintGhost(InstancePainter painter) {
		paintBase(painter, true);
	}

	//
	// graphics methods
	//
	@Override
	public void paintIcon(InstancePainter painter) {
		BitWidth w = painter.getAttributeValue(StdAttr.WIDTH);
		paintIconBase(painter, w);
	}

	private void paintIconBase(InstancePainter painter, BitWidth w) {
		PinAttributes attrs = (PinAttributes) painter.getAttributeSet();
		Direction dir = attrs.facing;
		boolean output = attrs.isOutput();
		boolean iconprinted = false;
		Graphics g = painter.getGraphics();
		g.setFont(ICON_WIDTH_FONT);
		if (output) {
			if (w.equals(BitWidth.ONE) && ICON_OUT != null) {
				Icons.paintRotated(g, 2, 2, dir, ICON_OUT, painter.getDestination());
				iconprinted = true;
			} else if (ICON_OUT_MULTI != null) {
				Icons.paintRotated(g, 2, 2, dir, ICON_OUT_MULTI, painter.getDestination());
				iconprinted = true;
			}
		} else {
			if (w.equals(BitWidth.ONE) && ICON_IN != null) {
				Icons.paintRotated(g, 2, 2, dir, ICON_IN, painter.getDestination());
				iconprinted = true;
			} else if (ICON_IN_MULTI != null) {
				Icons.paintRotated(g, 2, 2, dir, ICON_IN_MULTI, painter.getDestination());
				iconprinted = true;
			}
		}
		if (!iconprinted) {
			int pinx = 16;
			int piny = 9;
			if (dir == Direction.EAST) { // keep defaults
			} else if (dir == Direction.WEST) {
				pinx = 4;
			} else if (dir == Direction.NORTH) {
				pinx = 9;
				piny = 4;
			} else if (dir == Direction.SOUTH) {
				pinx = 9;
				piny = 16;
			}
			g.setColor(w.equals(BitWidth.ONE) ? Value.TRUE.getColor() : Color.WHITE);
			if (output) {
				g.fillOval(4, 4, 13, 13);
			} else {
				g.fillRect(4, 4, 13, 13);
			}
			g.fillOval(pinx, piny, 3, 3);

			g.setColor(Color.black);
			if (output) {
				g.drawOval(4, 4, 13, 13);
			} else {
				g.drawRect(4, 4, 13, 13);
			}
		}
		if (w.equals(BitWidth.ONE))
			g.setColor(Color.WHITE);
		else
			g.setColor(Color.BLACK);
		GraphicsUtil.drawCenteredText(g, "" + w.getWidth(), 10, 9);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		paintBase(painter, false);
		painter.drawPorts();
		// print label
		painter.getGraphics().setColor(painter.getAttributeValue(StdAttr.ATTR_LABEL_COLOR));
		painter.drawLabel();
	}

	@Override
	public void propagate(InstanceState state) {
		PinAttributes attrs = (PinAttributes) state.getAttributeSet();
		Value val = state.getPort(0);

		PinState q = getState(state);
		if (attrs.type == EndData.OUTPUT_ONLY) {
			q.sending = val;
			q.receiving = val;
			state.setPort(0, Value.createUnknown(attrs.width), 1);
		} else {
			if (!val.isFullyDefined() && !attrs.threeState && state.isCircuitRoot()) {
				q.sending = pull2(q.sending, attrs.width);
				q.receiving = pull2(val, attrs.width);
				state.setPort(0, q.sending, 1);
			} else {
				q.receiving = val;
				if (!val.equals(q.sending)) { // ignore if no change
					state.setPort(0, q.sending, 1);
				}
			}
		}
	}

	public void setValue(InstanceState state, Value value) {
		PinAttributes attrs = (PinAttributes) state.getAttributeSet();
		Object pull = attrs.pull;
		if (pull != PULL_NONE && pull != null && !value.isFullyDefined()) {
			Value[] bits = value.getAll();
			if (pull == PULL_UP) {
				for (int i = 0; i < bits.length; i++) {
					if (bits[i] != Value.FALSE)
						bits[i] = Value.TRUE;
				}
			} else if (pull == PULL_DOWN) {
				for (int i = 0; i < bits.length; i++) {
					if (bits[i] != Value.TRUE)
						bits[i] = Value.FALSE;
				}
			}
			value = Value.create(bits);
		}

		PinState myState = getState(state);
		if (value == Value.NIL) {
			myState.sending = Value.createUnknown(attrs.width);
		} else {
			myState.sending = value;
		}
	}
}