/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.wiring;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.RadixOption;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstanceLogger;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.MenuExtender;
import com.cburch.logisim.util.GraphicsUtil;

public class ProgrammableGenerator extends InstanceFactory {
	public static class ClockLogger extends InstanceLogger {
		@Override
		public String getLogName(InstanceState state, Object option) {
			return state.getAttributeValue(StdAttr.LABEL);
		}

		@Override
		public Value getLogValue(InstanceState state, Object option) {
			ProgrammableGeneratorState s = getState(state);
			return s.sending;
		}
	}

	public static class ClockPoker extends InstancePoker {
		boolean isPressed = true;

		private boolean isInside(InstanceState state, MouseEvent e) {
			Bounds bds = state.getInstance().getBounds();
			return bds.contains(e.getX(), e.getY());
		}

		@Override
		public void mousePressed(InstanceState state, MouseEvent e) {
			isPressed = isInside(state, e);
		}

		@Override
		public void mouseReleased(InstanceState state, MouseEvent e) {
			if (isPressed && isInside(state, e)) {
				ProgrammableGeneratorState myState = (ProgrammableGeneratorState) state.getData();
				myState.editWindow();
			}
			isPressed = false;
		}
	}

	private static class ProgrammableGeneratorMenu implements ActionListener, MenuExtender {
		private JMenuItem edit;
		private JMenuItem reset;
		private Instance instance;
		private CircuitState circState;

		public ProgrammableGeneratorMenu(Instance instance) {
			this.instance = instance;
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			ProgrammableGeneratorState state = ProgrammableGenerator.getState(instance, circState);
			if (evt.getSource() == edit)
				state.editWindow();
			else if (evt.getSource() == reset)
				state.clearValues();
		}

		@Override
		public void configureMenu(JPopupMenu menu, Project proj) {
			this.circState = proj.getCircuitState();
			boolean enabled = circState != null;

			this.edit = createItem(enabled, Strings.get("ramEditMenuItem"));
			this.reset = createItem(enabled, Strings.get("ramClearMenuItem"));
			menu.addSeparator();
			menu.add(this.edit);
			menu.add(this.reset);
		}

		private JMenuItem createItem(boolean enabled, String label) {
			JMenuItem ret = new JMenuItem(label);
			ret.setEnabled(enabled);
			ret.addActionListener(this);
			return ret;
		}
	}

	private static final Attribute<Integer> ATTR_NSTATE = Attributes.forIntegerRange("nState",
			Strings.getter("NStateAttr"), 1, 32);

	public static final ProgrammableGenerator FACTORY = new ProgrammableGenerator();

	private static ProgrammableGeneratorState getState(Component comp, CircuitState circ) {
		ProgrammableGeneratorState ret = (ProgrammableGeneratorState) circ.getData(comp);
		int nstate = comp.getAttributeSet().getValue(ATTR_NSTATE);
		if (ret == null) {
			ret = new ProgrammableGeneratorState(nstate);
			circ.setData(comp, ret);
		} else {
			ret.updateSize(nstate);
		}
		return ret;
	}

	private static ProgrammableGeneratorState getState(Instance state, CircuitState circ) {
		ProgrammableGeneratorState ret = (ProgrammableGeneratorState) state.getData(circ);
		int nstate = state.getAttributeValue(ATTR_NSTATE);
		if (ret == null) {
			ret = new ProgrammableGeneratorState(nstate);
			state.setData(circ, ret);
		} else {
			ret.updateSize(nstate);
		}
		return ret;
	}

	private static ProgrammableGeneratorState getState(InstanceState state) {
		ProgrammableGeneratorState ret = (ProgrammableGeneratorState) state.getData();
		int nstate = state.getAttributeValue(ATTR_NSTATE);
		if (ret == null) {
			ret = new ProgrammableGeneratorState(nstate);
			state.setData(ret);
		} else {
			ret.updateSize(nstate);
		}
		return ret;
	}

	//
	// package methods
	//
	public static boolean tick(CircuitState circState, int ticks, Component comp) {
		ProgrammableGeneratorState state = getState(comp, circState);
		state.incrementTicks();
		int durationHigh = state.getdurationHighValue();
		int statetick = state.getStateTick();
		Value desired = (statetick - 1 < durationHigh ? Value.TRUE : Value.FALSE);
		if (!state.sending.equals(desired)) {
			state.sending = desired;
			Instance.getInstanceFor(comp).fireInvalidated();
			return true;
		} else {
			return false;
		}
	}

	public ProgrammableGenerator() {
		super("ProgrammableGenerator", Strings.getter("ProgrammableGeneratorComponent"));
		setAttributes(
				new Attribute[] { StdAttr.FACING, ATTR_NSTATE, StdAttr.LABEL, Pin.ATTR_LABEL_LOC, StdAttr.LABEL_FONT,StdAttr.ATTR_LABEL_COLOR },
				new Object[] { Direction.EAST, Integer.valueOf(4), "", Direction.WEST, StdAttr.DEFAULT_LABEL_FONT,Color.BLACK });
		setFacingAttribute(StdAttr.FACING);
		setInstanceLogger(ClockLogger.class);
		setInstancePoker(ClockPoker.class);
		setIconName("programmablegenerator.gif");
	}

	//
	// private methods
	//
	private void configureLabel(Instance instance) {
		Direction facing = instance.getAttributeValue(StdAttr.FACING);
		Direction labelLoc = instance.getAttributeValue(Pin.ATTR_LABEL_LOC);
		Probe.configureLabel(instance, labelLoc, facing);
	}

	//
	// methods for instances
	//
	@Override
	protected void configureNewInstance(Instance instance) {
		instance.addAttributeListener();
		instance.setPorts(new Port[] { new Port(0, 0, Port.OUTPUT, BitWidth.ONE) });
		configureLabel(instance);
	}

	@Override
	protected Object getInstanceFeature(Instance instance, Object key) {
		if (key == MenuExtender.class) {
			return new ProgrammableGeneratorMenu(instance);
		}
		return super.getInstanceFeature(instance, key);
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		return Probe.getOffsetBounds(attrs.getValue(StdAttr.FACING), BitWidth.ONE, RadixOption.RADIX_2);
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == Pin.ATTR_LABEL_LOC) {
			configureLabel(instance);
		} else if (attr == StdAttr.FACING) {
			instance.recomputeBounds();
			configureLabel(instance);
		}
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		Bounds bds = painter.getInstance().getBounds();
		int x = bds.getX();
		int y = bds.getY();
		g.setColor(painter.getAttributeValue(StdAttr.ATTR_LABEL_COLOR));
		painter.drawLabel();
		g.setColor(Color.BLACK);
		boolean drawUp;
		if (painter.getShowState()) {
			ProgrammableGeneratorState state = getState(painter);
			painter.drawBounds(state.sending.getColor());
			drawUp = state.sending == Value.TRUE;
		} else {
			painter.drawBounds(Color.BLACK);
			drawUp = true;
		}
		g.setColor(Color.WHITE);
		x += 10;
		y += 10;
		int[] xs = { x + 1, x + 1, x + 4, x + 4, x + 7, x + 7 };
		int[] ys;
		if (drawUp) {
			ys = new int[] { y + 5, y + 3, y + 3, y + 7, y + 7, y + 5 };
		} else {
			ys = new int[] { y + 5, y + 7, y + 7, y + 3, y + 3, y + 5 };
		}
		g.drawPolyline(xs, ys, xs.length);
		GraphicsUtil.switchToWidth(g, 2);
		xs = new int[] { x - 6, x - 6, x + 1, x + 1, x - 5 };
		ys = new int[] { y + 6, y - 6, y - 6, y, y };
		g.drawPolyline(xs, ys, xs.length);
		painter.drawPorts();
	}

	@Override
	public void propagate(InstanceState state) {
		Value val = state.getPort(0);
		ProgrammableGeneratorState q = getState(state);
		if (!val.equals(q.sending)) { // ignore if no change
			state.setPort(0, q.sending, 1);
		}
	}
}
