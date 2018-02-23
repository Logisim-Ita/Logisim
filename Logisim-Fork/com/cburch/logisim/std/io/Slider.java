package com.cburch.logisim.std.io;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import com.cburch.logisim.circuit.RadixOption;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.GraphicsUtil;

public class Slider extends InstanceFactory {
	public static class Poker extends InstancePoker {
		private SliderValue data;
		private boolean dragging = false;

		@Override
		public void mouseDragged(InstanceState state, MouseEvent e) {
			if (dragging) {
				data.setCurrentX(e.getX() - state.getInstance().getBounds().getX() - 10);
				state.fireInvalidated();
			}
		}

		@Override
		public void mousePressed(InstanceState state, MouseEvent e) {
			data = getValueState(state);
			Bounds bds = state.getInstance().getBounds();
			Rectangle slider = new Rectangle(bds.getX() + data.getCurrentX() + 5, bds.getY() + bds.getHeight() - 16, 12,
					12);
			// check if clicking slider rectangle
			if (slider.contains(e.getX(), e.getY()))
				this.dragging = true;
		}

		@Override
		public void mouseReleased(InstanceState state, MouseEvent e) {
			this.dragging = false;
		}
	}

	public static class SliderValue implements InstanceData, Cloneable {
		private int currentx = 0;

		@Override
		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}

		public int getCurrentX() {
			return this.currentx;
		}

		public void setCurrentX(int x) {
			if (x < 0)
				x = 0;
			else if (x > 100)
				x = 100;
			this.currentx = x;
		}
	}

	private static SliderValue getValueState(InstanceState state) {
		SliderValue ret = (SliderValue) state.getData();
		if (ret == null) {
			ret = new SliderValue();
			state.setData(ret);
		}
		return ret;
	}

	public Slider() {
		super("Slider", Strings.getter("Slider"));
		setAttributes(
				new Attribute[] { StdAttr.FACING, StdAttr.WIDTH, RadixOption.ATTRIBUTE, Io.ATTR_COLOR, StdAttr.LABEL,
						StdAttr.LABEL_FONT, StdAttr.ATTR_LABEL_COLOR },
				new Object[] { Direction.EAST, BitWidth.create(8), RadixOption.RADIX_2, Color.WHITE, "",
						StdAttr.DEFAULT_LABEL_FONT, Color.BLACK });
		setFacingAttribute(StdAttr.FACING);
		setIconName("slider.gif");
		setPorts(new Port[] { new Port(0, 0, Port.OUTPUT, 1) });
		setInstancePoker(Poker.class);
	}

	private void computeTextField(Instance instance) {
		Object d = instance.getAttributeValue(StdAttr.FACING);
		Bounds bds = instance.getBounds();
		int x = bds.getX() - 3;
		int y = bds.getY() + bds.getHeight() / 2 - 1;
		int halign = GraphicsUtil.H_RIGHT;
		int valign = GraphicsUtil.V_CENTER_OVERALL;
		if (d == Direction.WEST) {
			y = bds.getY();
			valign = GraphicsUtil.V_BASELINE;
		}
		instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT, StdAttr.ATTR_LABEL_COLOR, x, y, halign, valign);
	}

	@Override
	protected void configureNewInstance(Instance instance) {
		instance.addAttributeListener();
		computeTextField(instance);
		updateports(instance);
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		Direction facing = attrs.getValue(StdAttr.FACING);
		int width = 120, height = 30;
		if (facing == Direction.EAST)
			return Bounds.create(-width, -height / 2, width, height);
		else if (facing == Direction.WEST)
			return Bounds.create(0, -height / 2, width, height);
		else if (facing == Direction.NORTH)
			return Bounds.create(-width / 2, 0, width, height);
		else // Direction SUD
			return Bounds.create(-width / 2, -height, width, height);
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == StdAttr.FACING) {
			instance.recomputeBounds();
			updateports(instance);
			computeTextField(instance);
		} else if (attr == StdAttr.WIDTH)
			updateports(instance);

	}

	@Override
	public void paintInstance(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		Bounds bds = painter.getBounds();
		SliderValue data = getValueState(painter);
		int x = bds.getX(), y = bds.getY();
		painter.drawRoundBounds(painter.getAttributeValue(Io.ATTR_COLOR));
		GraphicsUtil.switchToWidth(g, 2);
		// slider line
		g.drawLine(x + 10, y + bds.getHeight() - 10, x + bds.getWidth() - 10, y + bds.getHeight() - 10);
		g.setColor(Color.DARK_GRAY);
		// slider
		g.fillRoundRect(x + data.getCurrentX() + 5, y + bds.getHeight() - 15, 10, 10, 4, 4);
		g.setColor(Color.BLACK);
		g.drawRoundRect(x + data.getCurrentX() + 5, y + bds.getHeight() - 15, 10, 10, 4, 4);
		painter.drawPorts();
		painter.drawLabel();
		// paint current value
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
		Value v = painter.getPort(0);
		FontMetrics fm = g.getFontMetrics();
		RadixOption radix = painter.getAttributeValue(RadixOption.ATTRIBUTE);
		String vStr = radix.toString(v);
		// if the string is too long, reduce its dimension
		if (fm.stringWidth(vStr) > bds.getWidth() - 10)
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 5));
		GraphicsUtil.drawCenteredText(g, vStr, x + bds.getWidth() / 2, y + 6);
	}

	@Override
	public void propagate(InstanceState state) {
		SliderValue data = getValueState(state);
		BitWidth b = state.getAttributeValue(StdAttr.WIDTH);
		Bounds bds = state.getInstance().getBounds();
		// 100(slider width-20):2^b-1 = currentx:value(dec)
		state.setPort(0,
				Value.createKnown(b,
						(int) Math.round(data.getCurrentX() * (Math.pow(2, b.getWidth()) - 1) / (bds.getWidth() - 20))),
				1);
	}

	private void updateports(Instance instance) {
		BitWidth b = instance.getAttributeValue(StdAttr.WIDTH);
		instance.setPorts(new Port[] { new Port(0, 0, Port.OUTPUT, b) });
	}

}
