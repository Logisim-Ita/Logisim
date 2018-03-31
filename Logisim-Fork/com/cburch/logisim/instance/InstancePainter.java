/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.instance;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.WireSet;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.proj.Project;

public class InstancePainter implements InstanceState {
	private ComponentDrawContext context;
	private InstanceComponent comp;
	private InstanceFactory factory;
	private AttributeSet attrs;

	public InstancePainter(ComponentDrawContext context, InstanceComponent instance) {
		this.context = context;
		this.comp = instance;
	}

	//
	// helper methods for drawing common elements in components
	//
	public void drawBounds() {
		context.drawBounds(comp, null);
	}

	public void drawBounds(Bounds bds, Color color) {
		context.drawBounds(comp, bds, color);
	}

	public void drawBounds(Color color) {
		context.drawBounds(comp, color);
	}

	public void drawClock(int i, Direction dir) {
		context.drawClock(comp, i, dir);
	}

	public void drawDongle(int x, int y) {
		context.drawDongle(x, y);
	}

	public void drawHandle(int x, int y) {
		context.drawHandle(x, y);
	}

	public void drawHandle(Location loc) {
		context.drawHandle(loc);
	}

	public void drawHandles() {
		context.drawHandles(comp);
	}

	public void drawLabel() {
		if (comp != null) {
			comp.drawLabel(context);
		}
	}

	public void drawPort(int i) {
		context.drawPin(comp, i);
	}

	public void drawPort(int i, String label, Direction dir) {
		context.drawPin(comp, i, label, dir);
	}

	public void drawPorts() {
		context.drawPins(comp);
	}

	public void drawRectangle(Bounds bds, String label) {
		context.drawRoundRectangle(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight(), label, Color.WHITE);
	}

	public void drawRectangle(int x, int y, int width, int height, String label, Color color) {
		context.drawRoundRectangle(x, y, width, height, label, color);
	}

	public void drawRoundBounds(Bounds bds, Color color) {
		context.drawRoundBounds(comp, bds, color);
	}

	public void drawRoundBounds(Color color) {
		context.drawRoundBounds(comp, color);
	}

	public void drawRoundRectangle(Bounds bds, String label) {
		context.drawRoundRectangle(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight(), label, Color.WHITE);
	}

	public void drawRoundRectangle(int x, int y, int width, int height, String label, Color color) {
		context.drawRoundRectangle(x, y, width, height, label, color);
	}

	@Override
	public void fireInvalidated() {
		comp.fireInvalidated();
	}

	@Override
	public AttributeSet getAttributeSet() {
		InstanceComponent c = comp;
		return c == null ? attrs : c.getAttributeSet();
	}

	@Override
	public <E> E getAttributeValue(Attribute<E> attr) {
		InstanceComponent c = comp;
		AttributeSet as = c == null ? attrs : c.getAttributeSet();
		return as.getValue(attr);
	}

	public Bounds getBounds() {
		InstanceComponent c = comp;
		return c == null ? factory.getOffsetBounds(attrs) : c.getBounds();
	}

	public Circuit getCircuit() {
		return context.getCircuit();
	}

	@Override
	public InstanceData getData() {
		CircuitState circState = context.getCircuitState();
		if (circState == null || comp == null) {
			throw new UnsupportedOperationException("setData on InstancePainter");
		} else {
			return (InstanceData) circState.getData(comp);
		}
	}

	public java.awt.Component getDestination() {
		return context.getDestination();
	}

	@Override
	public InstanceFactory getFactory() {
		return comp == null ? factory : (InstanceFactory) comp.getFactory();
	}

	public Object getGateShape() {
		return context.getGateShape();
	}

	public Graphics getGraphics() {
		return context.getGraphics();
	}

	//
	// methods related to the context of the canvas
	//
	public WireSet getHighlightedWires() {
		return context.getHighlightedWires();
	}

	//
	// methods related to the instance
	//
	@Override
	public Instance getInstance() {
		InstanceComponent c = comp;
		return c == null ? null : c.getInstance();
	}

	public Location getLocation() {
		InstanceComponent c = comp;
		return c == null ? Location.create(0, 0) : c.getLocation();
	}

	public Bounds getOffsetBounds() {
		InstanceComponent c = comp;
		if (c == null) {
			return factory.getOffsetBounds(attrs);
		} else {
			Location loc = c.getLocation();
			return c.getBounds().translate(-loc.getX(), -loc.getY());
		}
	}

	@Override
	public Value getPort(int portIndex) {
		InstanceComponent c = comp;
		CircuitState s = context.getCircuitState();
		if (c != null && s != null) {
			return s.getValue(c.getEnd(portIndex).getLocation());
		} else {
			return Value.UNKNOWN;
		}
	}

	//
	// methods related to the circuit state
	//
	@Override
	public Project getProject() {
		return context.getCircuitState().getProject();
	}

	public boolean getShowState() {
		return context.getShowState();
	}

	@Override
	public long getTickCount() {
		return context.getCircuitState().getPropagator().getTickCount();
	}

	@Override
	public boolean isCircuitRoot() {
		return !context.getCircuitState().isSubstate();
	}

	@Override
	public boolean isPortConnected(int index) {
		Circuit circ = context.getCircuit();
		Location loc = comp.getEnd(index).getLocation();
		return circ.isConnected(loc, comp);
	}

	public boolean isPrintView() {
		return context.isPrintView();
	}

	@Override
	public void setData(InstanceData value) {
		CircuitState circState = context.getCircuitState();
		if (circState == null || comp == null) {
			throw new UnsupportedOperationException("setData on InstancePainter");
		} else {
			circState.setData(comp, value);
		}
	}

	void setFactory(InstanceFactory factory, AttributeSet attrs) {
		this.comp = null;
		this.factory = factory;
		this.attrs = attrs;
	}

	void setInstance(InstanceComponent value) {
		this.comp = value;
	}

	@Override
	public void setPort(int portIndex, Value value, int delay) {
		throw new UnsupportedOperationException("setValue on InstancePainter");
	}

	public boolean shouldDrawColor() {
		return context.shouldDrawColor();
	}
}
