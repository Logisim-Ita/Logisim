/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.plexers;

import java.awt.Color;
import java.awt.Graphics;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
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

	public DisplayDecoder() {
		super("DisplayDecoder", Strings.getter("DisplayDecoderComponent"));
		setAttributes(new Attribute[] { StdAttr.FACING }, new Object[] { Direction.EAST });
		setIconName("displaydecoder.gif");
		setFacingAttribute(StdAttr.FACING);
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
	protected void configureNewInstance(Instance instance) {
		instance.addAttributeListener();
		updatePorts(instance);
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		instance.recomputeBounds();
		updatePorts(instance);
	}

	private void updatePorts(Instance instance) {
		Object dir = instance.getAttributeValue(StdAttr.FACING);
		int in = 4;
		int out = 7;
		char cin = 65;// Letter A (to D in for)
		char cout = 97;// Letter a (to g in for)
		Port[] ps = new Port[in + out + 1];
		if (dir == Direction.NORTH || dir == Direction.SOUTH) {// horizzontal
			int y = dir == Direction.NORTH ? 40 : -40;
			for (int i = 0; i < in; i++) {// inputs
				ps[i] = new Port(20 * i - 30, y, Port.INPUT, 1); // total lenght
																	// should be
																	// 80(10-A-20-B-20-C-20-D-10)
				ps[i].setToolTip(Strings.getter("DisplayDecoderInTip", "" + cin));
				cin++;
			}
			int j = in;
			for (int i = 0; i < out; i++) {// outputs
				ps[j] = new Port(10 * i - 30, 0, Port.OUTPUT, 1); // total
																	// lenght
																	// should be
																	// 80(10-a-10-b-10-c-10-d-10-e-10-f-10-g-10)
				ps[j].setToolTip(Strings.getter("DisplayDecoderOutTip", "" + cout));
				cout++;
				j++;
			}
			ps[in + out] = new Port(-40, y / 2, Port.INPUT, 1); // enable input
		} else {// vertical
			int x = dir == Direction.EAST ? -40 : 40;
			for (int i = 0; i < in; i++) {// inputs
				ps[i] = new Port(x, 20 * i - 30, Port.INPUT, 1);
				ps[i].setToolTip(Strings.getter("DisplayDecoderInTip", "" + cin));
				cin++;
			}
			int j = in;
			for (int i = 0; i < out; i++) {// outputs
				ps[j] = new Port(0, 10 * i - 30, Port.OUTPUT, 1);
				ps[j].setToolTip(Strings.getter("DisplayDecoderOutTip", "" + cout));
				cout++;
				j++;
			}
			ps[in + out] = new Port(x / 2, -40, Port.INPUT, 1); // enable input
		}
		ps[in + out].setToolTip(Strings.getter("priorityEncoderEnableInTip"));
		instance.setPorts(ps);
	}

	public int decval;// decimal output's value

	@Override
	public void propagate(InstanceState state) {
		/*
		 * 0->A 1->C 2->A 3->D 4->a 5->b 6->c 7->d 8->e 9->f 10->g
		 */
		int input = 4;
		decval = -1;
		boolean enabled = (state.getPort(11) != Value.FALSE);
		if (enabled) {
			if (state.getPort(0) != Value.UNKNOWN && state.getPort(1) != Value.UNKNOWN
					&& state.getPort(2) != Value.UNKNOWN && state.getPort(3) != Value.UNKNOWN) {
				for (int i = 0; i < input; i++) {// 0, 1, 2, 3
					if (state.getPort(i) == Value.TRUE) {// array's cell
						decval += (int) Math.pow(2, i);// for example 1101 -->
														// 8+4+1= 13(decimal)
					}
				}
				decval++;
			}
		}
		switch (decval) {
		case 0:
			state.setPort(4, Value.TRUE, Plexers.DELAY);
			state.setPort(5, Value.TRUE, Plexers.DELAY);
			state.setPort(6, Value.TRUE, Plexers.DELAY);
			state.setPort(7, Value.TRUE, Plexers.DELAY);
			state.setPort(8, Value.TRUE, Plexers.DELAY);
			state.setPort(9, Value.TRUE, Plexers.DELAY);
			state.setPort(10, Value.FALSE, Plexers.DELAY);
			break;
		case 1:
			state.setPort(4, Value.FALSE, Plexers.DELAY);
			state.setPort(5, Value.TRUE, Plexers.DELAY);
			state.setPort(6, Value.TRUE, Plexers.DELAY);
			state.setPort(7, Value.FALSE, Plexers.DELAY);
			state.setPort(8, Value.FALSE, Plexers.DELAY);
			state.setPort(9, Value.FALSE, Plexers.DELAY);
			state.setPort(10, Value.FALSE, Plexers.DELAY);
			break;
		case 2:
			state.setPort(4, Value.TRUE, Plexers.DELAY);
			state.setPort(5, Value.TRUE, Plexers.DELAY);
			state.setPort(6, Value.FALSE, Plexers.DELAY);
			state.setPort(7, Value.TRUE, Plexers.DELAY);
			state.setPort(8, Value.TRUE, Plexers.DELAY);
			state.setPort(9, Value.FALSE, Plexers.DELAY);
			state.setPort(10, Value.TRUE, Plexers.DELAY);
			break;
		case 3:
			state.setPort(4, Value.TRUE, Plexers.DELAY);
			state.setPort(5, Value.TRUE, Plexers.DELAY);
			state.setPort(6, Value.TRUE, Plexers.DELAY);
			state.setPort(7, Value.TRUE, Plexers.DELAY);
			state.setPort(8, Value.FALSE, Plexers.DELAY);
			state.setPort(9, Value.FALSE, Plexers.DELAY);
			state.setPort(10, Value.TRUE, Plexers.DELAY);
			break;
		case 4:
			state.setPort(4, Value.FALSE, Plexers.DELAY);
			state.setPort(5, Value.TRUE, Plexers.DELAY);
			state.setPort(6, Value.TRUE, Plexers.DELAY);
			state.setPort(7, Value.FALSE, Plexers.DELAY);
			state.setPort(8, Value.FALSE, Plexers.DELAY);
			state.setPort(9, Value.TRUE, Plexers.DELAY);
			state.setPort(10, Value.TRUE, Plexers.DELAY);
			break;
		case 5:
			state.setPort(4, Value.TRUE, Plexers.DELAY);
			state.setPort(5, Value.FALSE, Plexers.DELAY);
			state.setPort(6, Value.TRUE, Plexers.DELAY);
			state.setPort(7, Value.TRUE, Plexers.DELAY);
			state.setPort(8, Value.FALSE, Plexers.DELAY);
			state.setPort(9, Value.TRUE, Plexers.DELAY);
			state.setPort(10, Value.TRUE, Plexers.DELAY);
			break;
		case 6:
			state.setPort(4, Value.TRUE, Plexers.DELAY);
			state.setPort(5, Value.FALSE, Plexers.DELAY);
			state.setPort(6, Value.TRUE, Plexers.DELAY);
			state.setPort(7, Value.TRUE, Plexers.DELAY);
			state.setPort(8, Value.TRUE, Plexers.DELAY);
			state.setPort(9, Value.TRUE, Plexers.DELAY);
			state.setPort(10, Value.TRUE, Plexers.DELAY);
			break;
		case 7:
			state.setPort(4, Value.TRUE, Plexers.DELAY);
			state.setPort(5, Value.TRUE, Plexers.DELAY);
			state.setPort(6, Value.TRUE, Plexers.DELAY);
			state.setPort(7, Value.FALSE, Plexers.DELAY);
			state.setPort(8, Value.FALSE, Plexers.DELAY);
			state.setPort(9, Value.FALSE, Plexers.DELAY);
			state.setPort(10, Value.FALSE, Plexers.DELAY);
			break;
		case 8:
			state.setPort(4, Value.TRUE, Plexers.DELAY);
			state.setPort(5, Value.TRUE, Plexers.DELAY);
			state.setPort(6, Value.TRUE, Plexers.DELAY);
			state.setPort(7, Value.TRUE, Plexers.DELAY);
			state.setPort(8, Value.TRUE, Plexers.DELAY);
			state.setPort(9, Value.TRUE, Plexers.DELAY);
			state.setPort(10, Value.TRUE, Plexers.DELAY);
			break;
		case 9:
			state.setPort(4, Value.TRUE, Plexers.DELAY);
			state.setPort(5, Value.TRUE, Plexers.DELAY);
			state.setPort(6, Value.TRUE, Plexers.DELAY);
			state.setPort(7, Value.FALSE, Plexers.DELAY);
			state.setPort(8, Value.FALSE, Plexers.DELAY);
			state.setPort(9, Value.TRUE, Plexers.DELAY);
			state.setPort(10, Value.TRUE, Plexers.DELAY);
			break;
		case 10:
			state.setPort(4, Value.TRUE, Plexers.DELAY);
			state.setPort(5, Value.TRUE, Plexers.DELAY);
			state.setPort(6, Value.TRUE, Plexers.DELAY);
			state.setPort(7, Value.FALSE, Plexers.DELAY);
			state.setPort(8, Value.TRUE, Plexers.DELAY);
			state.setPort(9, Value.TRUE, Plexers.DELAY);
			state.setPort(10, Value.TRUE, Plexers.DELAY);
			break;
		case 11:
			state.setPort(4, Value.FALSE, Plexers.DELAY);
			state.setPort(5, Value.FALSE, Plexers.DELAY);
			state.setPort(6, Value.TRUE, Plexers.DELAY);
			state.setPort(7, Value.TRUE, Plexers.DELAY);
			state.setPort(8, Value.TRUE, Plexers.DELAY);
			state.setPort(9, Value.TRUE, Plexers.DELAY);
			state.setPort(10, Value.TRUE, Plexers.DELAY);
			break;
		case 12:
			state.setPort(4, Value.TRUE, Plexers.DELAY);
			state.setPort(5, Value.FALSE, Plexers.DELAY);
			state.setPort(6, Value.FALSE, Plexers.DELAY);
			state.setPort(7, Value.TRUE, Plexers.DELAY);
			state.setPort(8, Value.TRUE, Plexers.DELAY);
			state.setPort(9, Value.TRUE, Plexers.DELAY);
			state.setPort(10, Value.FALSE, Plexers.DELAY);
			break;
		case 13:
			state.setPort(4, Value.FALSE, Plexers.DELAY);
			state.setPort(5, Value.TRUE, Plexers.DELAY);
			state.setPort(6, Value.TRUE, Plexers.DELAY);
			state.setPort(7, Value.TRUE, Plexers.DELAY);
			state.setPort(8, Value.TRUE, Plexers.DELAY);
			state.setPort(9, Value.FALSE, Plexers.DELAY);
			state.setPort(10, Value.TRUE, Plexers.DELAY);
			break;
		case 14:
			state.setPort(4, Value.TRUE, Plexers.DELAY);
			state.setPort(5, Value.FALSE, Plexers.DELAY);
			state.setPort(6, Value.FALSE, Plexers.DELAY);
			state.setPort(7, Value.TRUE, Plexers.DELAY);
			state.setPort(8, Value.TRUE, Plexers.DELAY);
			state.setPort(9, Value.TRUE, Plexers.DELAY);
			state.setPort(10, Value.TRUE, Plexers.DELAY);
			break;
		case 15:
			state.setPort(4, Value.TRUE, Plexers.DELAY);
			state.setPort(5, Value.FALSE, Plexers.DELAY);
			state.setPort(6, Value.FALSE, Plexers.DELAY);
			state.setPort(7, Value.FALSE, Plexers.DELAY);
			state.setPort(8, Value.TRUE, Plexers.DELAY);
			state.setPort(9, Value.TRUE, Plexers.DELAY);
			state.setPort(10, Value.TRUE, Plexers.DELAY);
			break;
		default:
			state.setPort(4, Value.UNKNOWN, Plexers.DELAY);
			state.setPort(5, Value.UNKNOWN, Plexers.DELAY);
			state.setPort(6, Value.UNKNOWN, Plexers.DELAY);
			state.setPort(7, Value.UNKNOWN, Plexers.DELAY);
			state.setPort(8, Value.UNKNOWN, Plexers.DELAY);
			state.setPort(9, Value.UNKNOWN, Plexers.DELAY);
			state.setPort(10, Value.UNKNOWN, Plexers.DELAY);
			break;
		}
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		painter.drawBounds();
		Bounds bds = painter.getBounds();
		g.setColor(Color.BLACK);
		painter.drawPorts();
		if (decval != -1) {// draw the dec number on the component
			GraphicsUtil.drawCenteredText(g, Integer.toString(decval), bds.getX() + bds.getWidth() / 2,
					bds.getY() + bds.getHeight() / 2);
		} else {// unknown --> draw '-'
			GraphicsUtil.drawCenteredText(g, "-", bds.getX() + bds.getWidth() / 2, bds.getY() + bds.getHeight() / 2);
		}
	}
}
