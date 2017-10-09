package com.cburch.logisim.std.ttl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

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

public abstract class AbstractTtlGate extends InstanceFactory {

	protected static final int pinwidth = 10, pinheight = 7, height = 60;
	protected final String Ttl7447portnames[] = { "B", "C", "LT", "BI", "RBI", "D", "A", "f", "g", "a", "b", "c", "d",
			"e" };
	protected final String Ttl7485portnames[] = { "B3", "A<B", "A=B", "A>B", "A<B", "A=B", "A>B", "A3", "B2", "A2",
			"A1", "B1", "A0", "B0" };
	protected final String Ttl74283portnames[] = { "∑2", "B2", "A2", "∑1", "A1", "B1", "CIN", "B3", "A3", "∑3", "A4",
			"B4", "∑4", "COUT" };
	private String name;
	protected int pinnumber;

	protected AbstractTtlGate(String name, int pins) {
		super(name);
		setIconName("ttl.gif");
		setAttributes(
				new Attribute[] { StdAttr.FACING, TTL.VCC_GND, TTL.DRAW_INTERNAL_STRUCTURE, StdAttr.LABEL,
						StdAttr.LABEL_FONT },
				new Object[] { Direction.EAST, false, false, "", StdAttr.DEFAULT_LABEL_FONT });
		setFacingAttribute(StdAttr.FACING);
		this.name = name;
		this.pinnumber = pins;
	}

	private void computeTextField(Instance instance) {
		Bounds bds = instance.getBounds();
		Direction dir = instance.getAttributeValue(StdAttr.FACING);
		if (dir == Direction.EAST || dir == Direction.WEST)
			instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT, bds.getX() + bds.getWidth() + 3,
					bds.getY() + bds.getHeight() / 2, GraphicsUtil.H_LEFT, GraphicsUtil.V_CENTER);
		else
			instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT, bds.getX() + bds.getWidth() / 2, bds.getY() - 3,
					GraphicsUtil.H_CENTER, GraphicsUtil.V_BASELINE);
	}

	@Override
	protected void configureNewInstance(Instance instance) {
		instance.addAttributeListener();
		updateports(instance);
		computeTextField(instance);
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		Direction dir = attrs.getValue(StdAttr.FACING);
		return Bounds.create(0, -30, this.pinnumber * 10, height).rotate(Direction.EAST, dir, 0, 0);
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == StdAttr.FACING) {
			instance.recomputeBounds();
			updateports(instance);
			computeTextField(instance);
		} else if (attr == TTL.VCC_GND) {
			updateports(instance);
		}
	}

	private void paintBase(InstancePainter painter, boolean drawname) {
		Direction dir = painter.getAttributeValue(StdAttr.FACING);
		Graphics2D g = (Graphics2D) painter.getGraphics();
		Bounds bds = painter.getBounds();
		int x = bds.getX();
		int y = bds.getY();
		int xp = x, yp = y;
		int width = bds.getWidth();
		int height = bds.getHeight();
		for (int i = 0; i < this.pinnumber; i++) {
			if (i < this.pinnumber / 2) {
				if (dir == Direction.WEST || dir == Direction.EAST)
					xp = i * 20 + (10 - pinwidth / 2) + x;
				else
					yp = i * 20 + (10 - pinwidth / 2) + y;
			} else {
				if (dir == Direction.WEST || dir == Direction.EAST) {
					xp = (i - this.pinnumber / 2) * 20 + (10 - pinwidth / 2) + x;
					yp = height + y - pinheight;
				} else {
					yp = (i - this.pinnumber / 2) * 20 + (10 - pinwidth / 2) + y;
					xp = width + x - pinheight;
				}
			}
			if (dir == Direction.WEST || dir == Direction.EAST)
				g.drawRect(xp, yp, pinwidth, pinheight);
			else
				g.drawRect(xp, yp, pinheight, pinwidth);
		}
		if (dir == Direction.SOUTH) {
			g.drawRect(x + pinheight, y, bds.getWidth() - pinheight * 2, bds.getHeight());
			g.drawArc(x + width / 2 - 7, y - 7, 14, 14, 180, 180);
		} else if (dir == Direction.WEST) {
			g.drawRect(x, y + pinheight, bds.getWidth(), bds.getHeight() - pinheight * 2);
			g.drawArc(x + width - 7, y + height / 2 - 7, 14, 14, 90, 180);
		} else if (dir == Direction.NORTH) {
			g.drawRect(x + pinheight, y, bds.getWidth() - pinheight * 2, bds.getHeight());
			g.drawArc(x + width / 2 - 7, y + height - 7, 14, 14, 0, 180);
		} else {// east
			g.drawRect(x, y + pinheight, bds.getWidth(), bds.getHeight() - pinheight * 2);
			g.drawArc(x - 7, y + height / 2 - 7, 14, 14, 270, 180);
		}
		g.rotate(Math.toRadians(-dir.toDegrees()), x + width / 2, y + height / 2);
		if (drawname) {
			g.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 14));
			GraphicsUtil.drawCenteredText(g, this.name, x + bds.getWidth() / 2, y + bds.getHeight() / 2 - 4);
		}
		if (dir == Direction.WEST || dir == Direction.EAST) {
			xp = x;
			yp = y;
		} else {
			xp = x + (width - height) / 2;
			yp = y + (height - width) / 2;
			width = bds.getHeight();
			height = bds.getWidth();
		}
		g.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 7));
		GraphicsUtil.drawCenteredText(g, "Vcc", xp + 10, yp + pinheight + 4);
		GraphicsUtil.drawCenteredText(g, "GND", xp + width - 10, yp + height - pinheight - 7);
	}

	@Override
	public void paintGhost(InstancePainter painter) {
		paintBase(painter, true);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		painter.drawPorts();
		painter.drawLabel();
		if (!painter.getAttributeValue(TTL.DRAW_INTERNAL_STRUCTURE)) {
			Direction dir = painter.getAttributeValue(StdAttr.FACING);
			Graphics2D g = (Graphics2D) painter.getGraphics();
			Bounds bds = painter.getBounds();
			int x = bds.getX();
			int y = bds.getY();
			int xp = x, yp = y;
			int width = bds.getWidth();
			int height = bds.getHeight();
			for (int i = 0; i < this.pinnumber; i++) {
				if (i == this.pinnumber / 2) {
					xp = x;
					yp = y;
					g.setColor(Color.DARK_GRAY);
					if (dir == Direction.WEST || dir == Direction.EAST) {
						g.fillRect(xp, yp + pinheight, width, height - pinheight * 2 - 2);
						g.setColor(Color.BLACK);
						g.drawRect(xp, yp + pinheight, width, height - pinheight * 2 - 2);
					} else {
						g.fillRect(xp + pinheight, yp, width - pinheight * 2, height - 4);
						g.setColor(Color.DARK_GRAY.darker());
						g.fillRect(xp + pinheight, yp + height - 4, width - pinheight * 2, 4);
						g.setColor(Color.BLACK);
						g.drawRect(xp + pinheight, yp, width - pinheight * 2, height - 4);
						g.drawRect(xp + pinheight, yp + height - 4, width - pinheight * 2, 4);
					}
					if (dir == Direction.WEST || dir == Direction.EAST) {
						g.setColor(Color.DARK_GRAY.darker());
						g.fillRect(xp, yp + height - pinheight - 2, width, 4);
						g.setColor(Color.BLACK);
						g.drawRect(xp, yp + height - pinheight - 2, width, 4);
					}
					if (dir == Direction.SOUTH)
						g.fillArc(xp + width / 2 - 7, yp - 7, 14, 14, 180, 180);
					else if (dir == Direction.WEST)
						g.fillArc(xp + width - 7, yp + height / 2 - 7, 14, 14, 90, 180);
					else if (dir == Direction.NORTH)
						g.fillArc(xp + width / 2 - 7, yp + height - 11, 14, 14, 0, 180);
					else // east
						g.fillArc(xp - 7, yp + height / 2 - 7, 14, 14, 270, 180);
				}
				if (i < this.pinnumber / 2) {
					if (dir == Direction.WEST || dir == Direction.EAST)
						xp = i * 20 + (10 - pinwidth / 2) + x;
					else
						yp = i * 20 + (10 - pinwidth / 2) + y;
				} else {
					if (dir == Direction.WEST || dir == Direction.EAST) {
						xp = (i - this.pinnumber / 2) * 20 + (10 - pinwidth / 2) + x;
						yp = height + y - pinheight;
					} else {
						yp = (i - this.pinnumber / 2) * 20 + (10 - pinwidth / 2) + y;
						xp = width + x - pinheight;
					}
				}
				if (dir == Direction.WEST || dir == Direction.EAST) {
					g.setColor(Color.LIGHT_GRAY);
					g.fillRect(xp, yp, pinwidth, pinheight);
					g.setColor(Color.BLACK);
					g.drawRect(xp, yp, pinwidth, pinheight);
				} else {
					g.setColor(Color.LIGHT_GRAY);
					g.fillRect(xp, yp, pinheight, pinwidth);
					g.setColor(Color.BLACK);
					g.drawRect(xp, yp, pinheight, pinwidth);
				}
			}

			g.setColor(Color.LIGHT_GRAY.brighter());
			g.rotate(Math.toRadians(-dir.toDegrees()), x + width / 2, y + height / 2);
			g.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 14));
			GraphicsUtil.drawCenteredText(g, this.name, x + width / 2, y + height / 2 - 4);
			g.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 7));
			if (dir == Direction.WEST || dir == Direction.EAST) {
				xp = x;
				yp = y;
			} else {
				xp = x + (width - height) / 2;
				yp = y + (height - width) / 2;
			}
			if (dir == Direction.SOUTH) {
				GraphicsUtil.drawCenteredText(g, "Vcc", xp + 10, yp + pinheight + 4);
				GraphicsUtil.drawCenteredText(g, "GND", xp + height - 14, yp + width - pinheight - 8);
			} else if (dir == Direction.WEST) {
				GraphicsUtil.drawCenteredText(g, "Vcc", xp + 10, yp + pinheight + 6);
				GraphicsUtil.drawCenteredText(g, "GND", xp + width - 10, yp + height - pinheight - 8);
			} else if (dir == Direction.NORTH) {
				GraphicsUtil.drawCenteredText(g, "Vcc", xp + 14, yp + pinheight + 4);
				GraphicsUtil.drawCenteredText(g, "GND", xp + height - 10, yp + width - pinheight - 8);
			} else { // east
				GraphicsUtil.drawCenteredText(g, "Vcc", xp + 10, yp + pinheight + 4);
				GraphicsUtil.drawCenteredText(g, "GND", xp + width - 10, yp + height - pinheight - 10);
			}
		} else
			paintInternalBase(painter);
	}

	abstract public void paintInternal(InstancePainter painter, int x, int y, int height, boolean up);

	private void paintInternalBase(InstancePainter painter) {
		Direction dir = painter.getAttributeValue(StdAttr.FACING);
		Bounds bds = painter.getBounds();
		int x = bds.getX();
		int y = bds.getY();
		int width = bds.getWidth();
		int height = bds.getHeight();
		if (dir == Direction.SOUTH || dir == Direction.NORTH) {
			x += (width - height) / 2;
			y += (height - width) / 2;
			width = bds.getHeight();
			height = bds.getWidth();
		}
		if (this.pinnumber == 14) {
			paintBase(painter, false);
			int c = this.name != "7404" ? 4 : 6;
			for (int i = 0; i < c; i++) {
				paintInternal(painter, x + (c != 6 ? i % 2 != 0 ? 60 : 0 : i % 3 * 40) + (i < c / 2 ? 0 : 20), y,
						height, !(i < c / 2));
			}
		} else {
			paintBase(painter, true);
			paintInternal(painter, x, y, height, false);
		}
	}

	@Override
	public void propagate(InstanceState state) {
		if (state.getAttributeValue(TTL.VCC_GND) && (state.getPort(this.pinnumber - 2) != Value.FALSE
				|| state.getPort(this.pinnumber - 1) != Value.TRUE))
			for (int i = 0; i < this.pinnumber; i++)
				state.setPort(i, Value.UNKNOWN, 1);
		else
			ttlpropagate(state);
	}

	abstract public void ttlpropagate(InstanceState state);

	private void updateports(Instance instance) {
		Direction dir = instance.getAttributeValue(StdAttr.FACING);
		Port[] ps = new Port[instance.getAttributeValue(TTL.VCC_GND) ? this.pinnumber : this.pinnumber - 2];
		int dx = 0, dy = 0, portnumber = 1;
		for (int i = 0; i < ps.length; i++) {// GND->12,Vcc->13
			if (i < this.pinnumber / 2 - 1 || i == this.pinnumber - 2) {
				if (i == this.pinnumber - 2)
					i = this.pinnumber / 2 - 1;
				if (dir == Direction.EAST) {
					dx = i * 20 + 10;
					dy = 30;
				} else if (dir == Direction.WEST) {
					dx = -10 - 20 * i;
					dy = -30;
				} else if (dir == Direction.NORTH) {
					dx = 30;
					dy = -10 - 20 * i;
				} else {// SOUTH
					dx = -30;
					dy = i * 20 + 10;
				}
				if (i == this.pinnumber / 2 - 1)
					i = this.pinnumber - 2;
				else
					portnumber = i + 1;
			} else if (i > this.pinnumber / 2 - 2) {
				if (i == this.pinnumber - 1)
					i = this.pinnumber / 2 - 2;
				if (dir == Direction.EAST) {
					dx = (i - (this.pinnumber / 2 - 1)) * 20 + 30;
					dy = -30;
				} else if (dir == Direction.WEST) {
					dx = -30 - (i - (this.pinnumber / 2 - 1)) * 20;
					dy = 30;
				} else if (dir == Direction.NORTH) {
					dx = -30;
					dy = -30 - (i - (this.pinnumber / 2 - 1)) * 20;
				} else {// SOUTH
					dx = 30;
					dy = (i - (this.pinnumber / 2 - 1)) * 20 + 30;
				}
				if (i == this.pinnumber / 2 - 2)
					i = this.pinnumber - 1;
				else
					portnumber = this.pinnumber - 1 - (i - (this.pinnumber / 2 - 1));
			}
			if (i > this.pinnumber - 3) {
				ps[i] = new Port(dx, dy, Port.INPUT, 1);
				if (i == this.pinnumber - 2)// GND
					ps[i].setToolTip(Strings.getter("GND: " + this.pinnumber / 2));
				else// Vcc
					ps[i].setToolTip(Strings.getter("Vcc: " + this.pinnumber));
			} else if (this.name == "7404") {// 1 inputs 1 output
				if (i % 2 == 0) {
					ps[i] = new Port(dx, dy, Port.INPUT, 1);
					ps[i].setToolTip(Strings.getter("multiplexerInTip", ": " + String.valueOf(portnumber)));
				} else {
					ps[i] = new Port(dx, dy, Port.OUTPUT, 1);
					ps[i].setToolTip(Strings.getter("demultiplexerOutTip", ": " + String.valueOf(portnumber)));
				}
			} else if (this.name == "7447") {
				if (i < this.pinnumber / 2 - 1) {
					ps[i] = new Port(dx, dy, Port.INPUT, 1);
					ps[i].setToolTip(Strings.getter("multiplexerInTip",
							String.valueOf(portnumber) + ": " + this.Ttl7447portnames[i]));
				} else {
					ps[i] = new Port(dx, dy, Port.OUTPUT, 1);
					ps[i].setToolTip(Strings.getter("demultiplexerOutTip",
							String.valueOf(portnumber) + ": " + this.Ttl7447portnames[i]));
				}

			} else if (this.name == "7485") {
				if (i < 7 && i > 3) {
					ps[i] = new Port(dx, dy, Port.OUTPUT, 1);
					ps[i].setToolTip(Strings.getter("demultiplexerOutTip",
							String.valueOf(portnumber) + ": " + this.Ttl7485portnames[i]));
				} else {
					ps[i] = new Port(dx, dy, Port.INPUT, 1);
					ps[i].setToolTip(Strings.getter("multiplexerInTip",
							String.valueOf(portnumber) + ": " + this.Ttl7485portnames[i]));
				}

			} else if (this.name == "74283") {
				if (i == 0 || i == 3 || i == 9 || i == 12 || i == 13) {
					ps[i] = new Port(dx, dy, Port.OUTPUT, 1);
					ps[i].setToolTip(Strings.getter("demultiplexerOutTip",
							String.valueOf(portnumber) + ": " + this.Ttl74283portnames[i]));
				} else {
					ps[i] = new Port(dx, dy, Port.INPUT, 1);
					ps[i].setToolTip(Strings.getter("multiplexerInTip",
							String.valueOf(portnumber) + ": " + this.Ttl74283portnames[i]));
				}
			} else {// 2 input 1 output
				if ((i + 1) % 3 != 0) {
					ps[i] = new Port(dx, dy, Port.INPUT, 1);
					ps[i].setToolTip(Strings.getter("multiplexerInTip", ": " + String.valueOf(portnumber)));
				} else {
					ps[i] = new Port(dx, dy, Port.OUTPUT, 1);
					ps[i].setToolTip(Strings.getter("demultiplexerOutTip", ": " + String.valueOf(portnumber)));
				}
			}

		}
		instance.setPorts(ps);
	}
}
