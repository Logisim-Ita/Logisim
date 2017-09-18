/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.plexers;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.GraphicsUtil;

public class DisplayDecoder extends InstanceFactory {

	private static final Attribute<Boolean> MULTI_BIT = Attributes.forBoolean("multibit", Strings.getter("ioMultiBit"));

	public DisplayDecoder() {
		super("DisplayDecoder", Strings.getter("DisplayDecoderComponent"));
		setAttributes(new Attribute[] { StdAttr.FACING, MULTI_BIT }, new Object[] { Direction.EAST, Boolean.FALSE });
		setIconName("displaydecoder.gif");
	}

	@Override
	protected void configureNewInstance(Instance instance) {
		instance.addAttributeListener();
		updatePorts(instance);
	}

	private int getdecval(InstanceState state) {
		int decval = -1;
		boolean multibit = state.getAttributeValue(MULTI_BIT) == Boolean.TRUE;
		if (!multibit && state.getPort(8) != Value.UNKNOWN && state.getPort(9) != Value.UNKNOWN
				&& state.getPort(10) != Value.UNKNOWN && state.getPort(11) != Value.UNKNOWN) {
			for (int i = 8; i < 12; i++) {// 0, 1, 2, 3
				if (state.getPort(i) == Value.TRUE) {// if true input
					// for example 1101 --> 8+4+1= 13(decimal)
					decval += (int) Math.pow(2, (i - 8));
				}
			}
			decval++;
		} else if (multibit & state.getPort(8) != Value.UNKNOWN)
			decval = state.getPort(8).toIntValue();
		return decval;
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		Direction dir = attrs.getValue(StdAttr.FACING);
		int output = 7;
		int len = 10 * output + 10; // lenght
		int offs = -len / 2; // to get 0 in middle lenght
		if (dir == Direction.NORTH) {
			return Bounds.create(offs, 0, len, 40);
		} else if (dir == Direction.SOUTH) {
			return Bounds.create(offs, -40, len, 40);
		} else if (dir == Direction.WEST) {
			return Bounds.create(0, offs, 40, len);
		} else { // dir == Direction.EAST
			return Bounds.create(-40, offs, 40, len);
		}
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		instance.recomputeBounds();
		updatePorts(instance);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		Direction dir = painter.getAttributeValue(StdAttr.FACING);
		Graphics g = painter.getGraphics();
		painter.drawBounds();
		Bounds bds = painter.getBounds();
		GraphicsUtil.drawCenteredText(g,
				painter.getPort(7) == Value.FALSE ? "!" + Strings.get("memEnableLabel")
						: (getdecval(painter) != -1) ? Integer.toString(getdecval(painter)) : "-",
				bds.getX() + bds.getWidth() / 2, bds.getY() + bds.getHeight() / 2);
		for (int i = 0; i < 8 + (painter.getAttributeValue(MULTI_BIT) ? 1 : 4); i++) {
			if (i != 7)
				painter.drawPort(i);
		}
		g.setColor(Color.GRAY);
		painter.drawPort(7, Strings.get("memEnableLabel"),
				(dir == Direction.NORTH || dir == Direction.SOUTH) ? Direction.EAST : Direction.NORTH);
	}

	@Override
	public void propagate(InstanceState state) {
		if (state.getPort(7) != Value.FALSE) {// enabled
			// Ports: 0->a 1->b 2->c 3->d 4->e 5->f 6->g 7->enable
			switch (getdecval(state)) {
			case 0:
				state.setPort(0, Value.TRUE, Plexers.DELAY);
				state.setPort(1, Value.TRUE, Plexers.DELAY);
				state.setPort(2, Value.TRUE, Plexers.DELAY);
				state.setPort(3, Value.TRUE, Plexers.DELAY);
				state.setPort(4, Value.TRUE, Plexers.DELAY);
				state.setPort(5, Value.TRUE, Plexers.DELAY);
				state.setPort(6, Value.FALSE, Plexers.DELAY);
				break;
			case 1:
				state.setPort(0, Value.FALSE, Plexers.DELAY);
				state.setPort(1, Value.TRUE, Plexers.DELAY);
				state.setPort(2, Value.TRUE, Plexers.DELAY);
				state.setPort(3, Value.FALSE, Plexers.DELAY);
				state.setPort(4, Value.FALSE, Plexers.DELAY);
				state.setPort(5, Value.FALSE, Plexers.DELAY);
				state.setPort(6, Value.FALSE, Plexers.DELAY);
				break;
			case 2:
				state.setPort(0, Value.TRUE, Plexers.DELAY);
				state.setPort(1, Value.TRUE, Plexers.DELAY);
				state.setPort(2, Value.FALSE, Plexers.DELAY);
				state.setPort(3, Value.TRUE, Plexers.DELAY);
				state.setPort(4, Value.TRUE, Plexers.DELAY);
				state.setPort(5, Value.FALSE, Plexers.DELAY);
				state.setPort(6, Value.TRUE, Plexers.DELAY);
				break;
			case 3:
				state.setPort(0, Value.TRUE, Plexers.DELAY);
				state.setPort(1, Value.TRUE, Plexers.DELAY);
				state.setPort(2, Value.TRUE, Plexers.DELAY);
				state.setPort(3, Value.TRUE, Plexers.DELAY);
				state.setPort(4, Value.FALSE, Plexers.DELAY);
				state.setPort(5, Value.FALSE, Plexers.DELAY);
				state.setPort(6, Value.TRUE, Plexers.DELAY);
				break;
			case 4:
				state.setPort(0, Value.FALSE, Plexers.DELAY);
				state.setPort(1, Value.TRUE, Plexers.DELAY);
				state.setPort(2, Value.TRUE, Plexers.DELAY);
				state.setPort(3, Value.FALSE, Plexers.DELAY);
				state.setPort(4, Value.FALSE, Plexers.DELAY);
				state.setPort(5, Value.TRUE, Plexers.DELAY);
				state.setPort(6, Value.TRUE, Plexers.DELAY);
				break;
			case 5:
				state.setPort(0, Value.TRUE, Plexers.DELAY);
				state.setPort(1, Value.FALSE, Plexers.DELAY);
				state.setPort(2, Value.TRUE, Plexers.DELAY);
				state.setPort(3, Value.TRUE, Plexers.DELAY);
				state.setPort(4, Value.FALSE, Plexers.DELAY);
				state.setPort(5, Value.TRUE, Plexers.DELAY);
				state.setPort(6, Value.TRUE, Plexers.DELAY);
				break;
			case 6:
				state.setPort(0, Value.TRUE, Plexers.DELAY);
				state.setPort(1, Value.FALSE, Plexers.DELAY);
				state.setPort(2, Value.TRUE, Plexers.DELAY);
				state.setPort(3, Value.TRUE, Plexers.DELAY);
				state.setPort(4, Value.TRUE, Plexers.DELAY);
				state.setPort(5, Value.TRUE, Plexers.DELAY);
				state.setPort(6, Value.TRUE, Plexers.DELAY);
				break;
			case 7:
				state.setPort(0, Value.TRUE, Plexers.DELAY);
				state.setPort(1, Value.TRUE, Plexers.DELAY);
				state.setPort(2, Value.TRUE, Plexers.DELAY);
				state.setPort(3, Value.FALSE, Plexers.DELAY);
				state.setPort(4, Value.FALSE, Plexers.DELAY);
				state.setPort(5, Value.FALSE, Plexers.DELAY);
				state.setPort(6, Value.FALSE, Plexers.DELAY);
				break;
			case 8:
				state.setPort(0, Value.TRUE, Plexers.DELAY);
				state.setPort(1, Value.TRUE, Plexers.DELAY);
				state.setPort(2, Value.TRUE, Plexers.DELAY);
				state.setPort(3, Value.TRUE, Plexers.DELAY);
				state.setPort(4, Value.TRUE, Plexers.DELAY);
				state.setPort(5, Value.TRUE, Plexers.DELAY);
				state.setPort(6, Value.TRUE, Plexers.DELAY);
				break;
			case 9:
				state.setPort(0, Value.TRUE, Plexers.DELAY);
				state.setPort(1, Value.TRUE, Plexers.DELAY);
				state.setPort(2, Value.TRUE, Plexers.DELAY);
				state.setPort(3, Value.FALSE, Plexers.DELAY);
				state.setPort(4, Value.FALSE, Plexers.DELAY);
				state.setPort(5, Value.TRUE, Plexers.DELAY);
				state.setPort(6, Value.TRUE, Plexers.DELAY);
				break;
			case 10:
				state.setPort(0, Value.TRUE, Plexers.DELAY);
				state.setPort(1, Value.TRUE, Plexers.DELAY);
				state.setPort(2, Value.TRUE, Plexers.DELAY);
				state.setPort(3, Value.FALSE, Plexers.DELAY);
				state.setPort(4, Value.TRUE, Plexers.DELAY);
				state.setPort(5, Value.TRUE, Plexers.DELAY);
				state.setPort(6, Value.TRUE, Plexers.DELAY);
				break;
			case 11:
				state.setPort(0, Value.FALSE, Plexers.DELAY);
				state.setPort(1, Value.FALSE, Plexers.DELAY);
				state.setPort(2, Value.TRUE, Plexers.DELAY);
				state.setPort(3, Value.TRUE, Plexers.DELAY);
				state.setPort(4, Value.TRUE, Plexers.DELAY);
				state.setPort(5, Value.TRUE, Plexers.DELAY);
				state.setPort(6, Value.TRUE, Plexers.DELAY);
				break;
			case 12:
				state.setPort(0, Value.TRUE, Plexers.DELAY);
				state.setPort(1, Value.FALSE, Plexers.DELAY);
				state.setPort(2, Value.FALSE, Plexers.DELAY);
				state.setPort(3, Value.TRUE, Plexers.DELAY);
				state.setPort(4, Value.TRUE, Plexers.DELAY);
				state.setPort(5, Value.TRUE, Plexers.DELAY);
				state.setPort(6, Value.FALSE, Plexers.DELAY);
				break;
			case 13:
				state.setPort(0, Value.FALSE, Plexers.DELAY);
				state.setPort(1, Value.TRUE, Plexers.DELAY);
				state.setPort(2, Value.TRUE, Plexers.DELAY);
				state.setPort(3, Value.TRUE, Plexers.DELAY);
				state.setPort(4, Value.TRUE, Plexers.DELAY);
				state.setPort(5, Value.FALSE, Plexers.DELAY);
				state.setPort(6, Value.TRUE, Plexers.DELAY);
				break;
			case 14:
				state.setPort(0, Value.TRUE, Plexers.DELAY);
				state.setPort(1, Value.FALSE, Plexers.DELAY);
				state.setPort(2, Value.FALSE, Plexers.DELAY);
				state.setPort(3, Value.TRUE, Plexers.DELAY);
				state.setPort(4, Value.TRUE, Plexers.DELAY);
				state.setPort(5, Value.TRUE, Plexers.DELAY);
				state.setPort(6, Value.TRUE, Plexers.DELAY);
				break;
			case 15:
				state.setPort(0, Value.TRUE, Plexers.DELAY);
				state.setPort(1, Value.FALSE, Plexers.DELAY);
				state.setPort(2, Value.FALSE, Plexers.DELAY);
				state.setPort(3, Value.FALSE, Plexers.DELAY);
				state.setPort(4, Value.TRUE, Plexers.DELAY);
				state.setPort(5, Value.TRUE, Plexers.DELAY);
				state.setPort(6, Value.TRUE, Plexers.DELAY);
				break;
			default:
				state.setPort(0, Value.UNKNOWN, Plexers.DELAY);
				state.setPort(1, Value.UNKNOWN, Plexers.DELAY);
				state.setPort(2, Value.UNKNOWN, Plexers.DELAY);
				state.setPort(3, Value.UNKNOWN, Plexers.DELAY);
				state.setPort(4, Value.UNKNOWN, Plexers.DELAY);
				state.setPort(5, Value.UNKNOWN, Plexers.DELAY);
				state.setPort(6, Value.UNKNOWN, Plexers.DELAY);
				break;
			}
		}
	}

	private void updatePorts(Instance instance) {
		Direction dir = instance.getAttributeValue(StdAttr.FACING);
		boolean multibit = instance.getAttributeValue(MULTI_BIT) == Boolean.TRUE;
		int in = multibit ? 1 : 4;// number of input ports
		int out = 7;// number of output ports
		char cin = 65;// Letter A (to D in for)
		char cout = 97;// Letter a (to g in for)
		Port[] ps = new Port[in + out + 1];
		if (dir == Direction.NORTH || dir == Direction.SOUTH) {// horizzontal
			int y = dir == Direction.NORTH ? 40 : -40;
			if (!multibit) {
				for (int i = 8; i < in + 8; i++) {// inputs
					// total lenght should be 80(10-A-20-B-20-C-20-D-10)
					ps[i] = new Port(20 * (i - 8) - 30, y, Port.INPUT, 1);
					ps[i].setToolTip(Strings.getter("DisplayDecoderInTip", "" + cin));
					cin++;
				}
			} else {
				ps[8] = new Port(0, y, Port.INPUT, 4);
				ps[8].setToolTip(Strings.getter("DisplayDecoderInTip", "" + cin));
			}
			for (int i = 0; i < out; i++) {// outputs
				// total lenght should be 80(10-A-20-B-20-C-20-D-10)
				ps[i] = new Port(10 * i - 30, 0, Port.OUTPUT, 1);
				ps[i].setToolTip(Strings.getter("DisplayDecoderOutTip", "" + cout));
				cout++;
			}
			ps[out] = new Port(-40, y / 2, Port.INPUT, 1); // enable input
		} else {// vertical
			int x = dir == Direction.EAST ? -40 : 40;
			if (!multibit) {
				for (int i = 8; i < in + 8; i++) {// inputs
					ps[i] = new Port(x, 20 * (i - 8) - 30, Port.INPUT, 1);
					ps[i].setToolTip(Strings.getter("DisplayDecoderInTip", "" + cin));
					cin++;
				}
			} else {
				ps[8] = new Port(x, 0, Port.INPUT, 4);
				ps[8].setToolTip(Strings.getter("DisplayDecoderInTip", "" + cin));
			}
			for (int i = 0; i < out; i++) {// outputs
				ps[i] = new Port(0, 10 * i - 30, Port.OUTPUT, 1);
				ps[i].setToolTip(Strings.getter("DisplayDecoderOutTip", "" + cout));
				cout++;
			}
			ps[out] = new Port(x / 2, -40, Port.INPUT, 1); // enable input
		}
		ps[out].setToolTip(Strings.getter("priorityEncoderEnableInTip"));
		instance.setPorts(ps);
	}
}
