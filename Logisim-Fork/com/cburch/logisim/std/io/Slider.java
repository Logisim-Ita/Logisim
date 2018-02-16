package com.cburch.logisim.std.io;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

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
		SliderValue data;
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
			Rectangle slider = new Rectangle(bds.getX() + data.getCurrentX() + 5, bds.getY() + bds.getHeight() / 2 - 2,
					10, 10);
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
		super("Slider", Strings.getter("SliderComponent"));
		setAttributes(
				new Attribute[] { StdAttr.FACING, StdAttr.WIDTH, Io.ATTR_COLOR, StdAttr.LABEL, StdAttr.LABEL_FONT,
						StdAttr.ATTR_LABEL_COLOR },
				new Object[] { Direction.EAST, BitWidth.create(8), Color.WHITE, "", StdAttr.DEFAULT_LABEL_FONT,
						Color.BLACK });
		setFacingAttribute(StdAttr.FACING);
		setIconName("slider.gif");
		setPorts(new Port[] { new Port(0, 0, Port.OUTPUT, 1) });
		setInstancePoker(Poker.class);
	}

	private void computeTextField(Instance instance) {
		Object d = instance.getAttributeValue(StdAttr.FACING);
		Bounds bds = instance.getBounds();
		int x = bds.getX() + bds.getWidth() / 2;
		int y = bds.getY() - 3;
		int halign = GraphicsUtil.H_CENTER;
		int valign = GraphicsUtil.V_BOTTOM;
		if (d == Direction.NORTH)
			x = bds.getX();
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
		int width = 120, height = 20;
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
		g.drawLine(x + 10, y + bds.getHeight() / 2, x + bds.getWidth() - 10, y + bds.getHeight() / 2);
		g.setColor(Color.DARK_GRAY);
		g.fillRoundRect(x + data.getCurrentX() + 5, y + bds.getHeight() / 2 - 5, 10, 10, 4, 4);
		g.setColor(Color.BLACK);
		g.drawRoundRect(x + data.getCurrentX() + 5, y + bds.getHeight() / 2 - 5, 10, 10, 4, 4);
		painter.drawPorts();
		painter.drawLabel();
	}

	@Override
	public void propagate(InstanceState state) {
		SliderValue data = getValueState(state);
		BitWidth b = state.getAttributeValue(StdAttr.WIDTH);
		Bounds bds = state.getInstance().getBounds();
		// 100(slider width):2^b-1 = currentx:value(dec)
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
