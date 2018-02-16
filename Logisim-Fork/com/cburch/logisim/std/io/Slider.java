package com.cburch.logisim.std.io;

import java.awt.Color;
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

		@Override
		public void mousePressed(InstanceState state, MouseEvent e) {
			data = getValueState(state);
		}

		@Override
		public void mouseDragged(InstanceState state, MouseEvent e) {
			byte x = (byte) (e.getX() - state.getInstance().getBounds().getX() - 10);
			if (x < 0)
				x = 0;
			else if (x > 50)
				x = 50;
			System.out.println(x);
		}
	}

	public static class SliderValue implements InstanceData, Cloneable {
		public Value currentval = Value.UNKNOWN;
		public BitWidth currentWidth;

		public SliderValue(BitWidth b) {
			currentWidth = b;
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

	public Slider() {
		super("Slider", Strings.getter("SliderComponent"));
		setAttributes(
				new Attribute[] { StdAttr.FACING, StdAttr.WIDTH, Io.ATTR_COLOR, StdAttr.LABEL, Io.ATTR_LABEL_LOC,
						StdAttr.LABEL_FONT, StdAttr.ATTR_LABEL_COLOR },
				new Object[] { Direction.EAST, BitWidth.create(8), Color.WHITE, "", Direction.NORTH,
						StdAttr.DEFAULT_LABEL_FONT, Color.BLACK });
		setFacingAttribute(StdAttr.FACING);
		setIconName("slider.gif");
		setPorts(new Port[] { new Port(0, 0, Port.OUTPUT, 1) });
		setInstancePoker(Poker.class);
	}

	private void computeTextField(Instance instance) {
		Object labelLoc = instance.getAttributeValue(Io.ATTR_LABEL_LOC);
		Bounds bds = instance.getBounds();
		int x = bds.getX() + bds.getWidth() / 2;
		int y = bds.getY() + bds.getHeight() / 2;
		int halign = GraphicsUtil.H_CENTER;
		int valign = GraphicsUtil.V_CENTER;
		if (labelLoc == Direction.NORTH) {
			y = bds.getY() - 2;
			valign = GraphicsUtil.V_BOTTOM;
		} else if (labelLoc == Direction.SOUTH) {
			y = bds.getY() + bds.getHeight() + 2;
			valign = GraphicsUtil.V_TOP;
		} else if (labelLoc == Direction.EAST) {
			x = bds.getX() + bds.getWidth() + 2;
			halign = GraphicsUtil.H_LEFT;
		} else if (labelLoc == Direction.WEST) {
			x = bds.getX();
			y = bds.getY() - 2;
			valign = GraphicsUtil.V_BOTTOM;
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
		if (facing == Direction.EAST)
			return Bounds.create(-70, -20, 70, 40);
		else if (facing == Direction.WEST)
			return Bounds.create(0, -20, 70, 40);
		else if (facing == Direction.NORTH)
			return Bounds.create(-35, 0, 70, 40);
		else // Direction SUD
			return Bounds.create(-35, -40, 70, 40);
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == StdAttr.FACING) {
			instance.recomputeBounds();
			updateports(instance);
		} else if (attr == StdAttr.WIDTH)
			updateports(instance);
		else if (attr == Io.ATTR_LABEL_LOC)
			computeTextField(instance);
	}

	private static SliderValue getValueState(InstanceState state) {
		SliderValue ret = (SliderValue) state.getData();
		BitWidth b = state.getAttributeValue(StdAttr.WIDTH);
		if (ret == null) {
			ret = new SliderValue(b);
			state.setData(ret);
		} else {
			ret.currentWidth = b;
		}
		return ret;
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		painter.drawRoundBounds(painter.getAttributeValue(Io.ATTR_COLOR));
		painter.drawPorts();
		painter.drawLabel();
	}

	@Override
	public void propagate(InstanceState state) {
		SliderValue data = getValueState(state);
		state.setPort(0, data.currentval, 1);
	}

	private void updateports(Instance instance) {
		BitWidth b = instance.getAttributeValue(StdAttr.WIDTH);
		instance.setPorts(new Port[] { new Port(0, 0, Port.OUTPUT, b) });
	}

}
