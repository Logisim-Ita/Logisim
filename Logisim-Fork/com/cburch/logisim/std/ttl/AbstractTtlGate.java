package com.cburch.logisim.std.ttl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.GraphicsUtil;

public abstract class AbstractTtlGate extends InstanceFactory {

	private static int pinwidth = 10, pinheight = 7;// the height can be odd
	private String name;

	protected AbstractTtlGate(String name) {
		super(name);
		setIconName("ttl.gif");
		setAttributes(new Attribute[] { StdAttr.FACING, StdAttr.LABEL, StdAttr.LABEL_FONT },
				new Object[] { Direction.EAST, "", StdAttr.DEFAULT_LABEL_FONT });
		setFacingAttribute(StdAttr.FACING);
		this.name = name;
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
		return Bounds.create(0, -30, 140, 60).rotate(Direction.EAST, dir, 0, 0);
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == StdAttr.FACING) {
			instance.recomputeBounds();
			updateports(instance);
			computeTextField(instance);
		}
	}

	@Override
	public void paintGhost(InstancePainter painter) {
		Direction dir = painter.getAttributeValue(StdAttr.FACING);
		Graphics2D g = (Graphics2D) painter.getGraphics();
		Bounds bds = painter.getBounds();
		int x = bds.getX();
		int y = bds.getY();
		int width = bds.getWidth();
		int height = bds.getHeight();
		for (int i = 0; i < 14; i++) {
			if (i < 7) {
				if (dir == Direction.WEST || dir == Direction.EAST)
					x = i * 20 + (10 - pinwidth / 2) + bds.getX();
				else
					y = i * 20 + (10 - pinwidth / 2) + bds.getY();
			} else {
				if (dir == Direction.WEST || dir == Direction.EAST) {
					x = (i - 7) * 20 + (10 - pinwidth / 2) + bds.getX();
					y = bds.getHeight() + bds.getY() - pinheight;
				} else {
					y = (i - 7) * 20 + (10 - pinwidth / 2) + bds.getY();
					x = bds.getWidth() + bds.getX() - pinheight;
				}
			}
			if (dir == Direction.WEST || dir == Direction.EAST)
				g.drawRect(x, y, pinwidth, pinheight);
			else
				g.drawRect(x, y, pinheight, pinwidth);
		}
		x = bds.getX();
		y = bds.getY();
		if (dir == Direction.WEST || dir == Direction.EAST)
			g.drawRect(x, y + pinheight, bds.getWidth(), bds.getHeight() - pinheight * 2);
		else
			g.drawRect(x + pinheight, y, bds.getWidth() - pinheight * 2, bds.getHeight());
		g.rotate(Math.toRadians(-dir.toDegrees()), x + width / 2, y + height / 2);
		g.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 14));
		GraphicsUtil.drawCenteredText(g, this.name, x + bds.getWidth() / 2, y + bds.getHeight() / 2 - 4);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		Direction dir = painter.getAttributeValue(StdAttr.FACING);
		painter.drawPorts();
		painter.drawLabel();
		Graphics2D g = (Graphics2D) painter.getGraphics();
		Bounds bds = painter.getBounds();
		int x = bds.getX();
		int y = bds.getY();
		int width = bds.getWidth();
		int height = bds.getHeight();
		for (int i = 0; i < 14; i++) {
			if (i == 7) {
				x = bds.getX();
				y = bds.getY();
				g.setColor(Color.DARK_GRAY);
				if (dir == Direction.WEST || dir == Direction.EAST) {
					g.fillRect(x, y + pinheight, width, height - pinheight * 2 - 2);
					g.setColor(Color.BLACK);
					g.drawRect(x, y + pinheight, width, height - pinheight * 2 - 2);
				} else {
					g.fillRect(x + pinheight, y, width - pinheight * 2, height - 4);
					g.setColor(Color.DARK_GRAY.darker());
					g.fillRect(x + pinheight, y + height - 4, width - pinheight * 2, 4);
					g.setColor(Color.BLACK);
					g.drawRect(x + pinheight, y, width - pinheight * 2, height - 4);
					g.drawRect(x + pinheight, y + height - 4, width - pinheight * 2, 4);
				}
				if (dir == Direction.WEST || dir == Direction.EAST) {
					g.setColor(Color.DARK_GRAY.darker());
					g.fillRect(x, y + height - pinheight - 2, width, 4);
					g.setColor(Color.BLACK);
					g.drawRect(x, y + height - pinheight - 2, width, 4);
				}
				if (dir == Direction.SOUTH)
					g.fillArc(x + width / 2 - 7, y - 7, 14, 14, 180, 180);
				else if (dir == Direction.WEST)
					g.fillArc(x + width - 7, y + height / 2 - 7, 14, 14, 90, 180);
				else if (dir == Direction.NORTH)
					g.fillArc(x + width / 2 - 7, y + height - 11, 14, 14, 0, 180);
				else // east
					g.fillArc(x - 7, y + height / 2 - 7, 14, 14, 270, 180);
			}
			if (i < 7) {
				if (dir == Direction.WEST || dir == Direction.EAST)
					x = i * 20 + (10 - pinwidth / 2) + bds.getX();
				else
					y = i * 20 + (10 - pinwidth / 2) + bds.getY();
			} else {
				if (dir == Direction.WEST || dir == Direction.EAST) {
					x = (i - 7) * 20 + (10 - pinwidth / 2) + bds.getX();
					y = height + bds.getY() - pinheight;
				} else {
					y = (i - 7) * 20 + (10 - pinwidth / 2) + bds.getY();
					x = width + bds.getX() - pinheight;
				}
			}
			if (dir == Direction.WEST || dir == Direction.EAST) {
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(x, y, pinwidth, pinheight);
				g.setColor(Color.BLACK);
				g.drawRect(x, y, pinwidth, pinheight);
			} else {
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(x, y, pinheight, pinwidth);
				g.setColor(Color.BLACK);
				g.drawRect(x, y, pinheight, pinwidth);
			}
		}
		x = bds.getX();
		y = bds.getY();

		g.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 7));
		g.setColor(Color.LIGHT_GRAY.brighter());
		g.rotate(Math.toRadians(-dir.toDegrees()), x + width / 2, y + height / 2);
		if (dir == Direction.SOUTH) {
			GraphicsUtil.drawCenteredText(g, "Vcc", x + (bds.getWidth() - bds.getHeight()) / 2 + 10,
					y + (bds.getHeight() - bds.getWidth()) / 2 + pinheight + 4);
			GraphicsUtil.drawCenteredText(g, "GND", x + (bds.getWidth() - bds.getHeight()) / 2 + height - 14,
					y + (bds.getHeight() - bds.getWidth()) / 2 + width - pinheight - 8);
		} else if (dir == Direction.WEST) {
			GraphicsUtil.drawCenteredText(g, "Vcc", x + 10, y + pinheight + 6);
			GraphicsUtil.drawCenteredText(g, "GND", x + width - 10, y + height - pinheight - 8);
		} else if (dir == Direction.NORTH) {
			GraphicsUtil.drawCenteredText(g, "Vcc", x + (bds.getWidth() - bds.getHeight()) / 2 + 14,
					y + (bds.getHeight() - bds.getWidth()) / 2 + pinheight + 4);
			GraphicsUtil.drawCenteredText(g, "GND", x + (bds.getWidth() - bds.getHeight()) / 2 + height - 10,
					y + (bds.getHeight() - bds.getWidth()) / 2 + width - pinheight - 8);
		} else { // east
			GraphicsUtil.drawCenteredText(g, "Vcc", x + 10, y + pinheight + 4);
			GraphicsUtil.drawCenteredText(g, "GND", x + width - 10, y + height - pinheight - 10);
		}
		g.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 14));
		GraphicsUtil.drawCenteredText(g, this.name, x + width / 2, y + height / 2 - 4);
	}

	private void updateports(Instance instance) {
		Direction dir = instance.getAttributeValue(StdAttr.FACING);
		Port[] ps = new Port[12];
		int dx, dy;
		for (int i = 0; i < ps.length; i++) {
			if (i < 6) {
				if (dir == Direction.EAST) {
					dx = i * 20 + 10;
					dy = 30;
				} else if (dir == Direction.WEST) {
					dx = (6 - i) * 20 - 130;
					dy = -30;
				} else if (dir == Direction.NORTH) {
					dx = 30;
					dy = (5 - i) * 20 - 110;
				} else {// SOUTH
					dx = -30;
					dy = i * 20 + 10;
				}
				if (this.name != "7404")// 2 inputs 1 output
					if ((i + 1) % 3 != 0) {
						ps[i] = new Port(dx, dy, Port.INPUT, 1);
						ps[i].setToolTip(Strings.getter("multiplexerInTip", ": " + String.valueOf(i + 1)));
					} else {
						ps[i] = new Port(dx, dy, Port.OUTPUT, 1);
						ps[i].setToolTip(Strings.getter("demultiplexerOutTip", ": " + String.valueOf(i + 1)));
					}
				else {// 1 input 1 output
					if (i % 2 == 0) {
						ps[i] = new Port(dx, dy, Port.INPUT, 1);
						ps[i].setToolTip(Strings.getter("multiplexerInTip", ": " + String.valueOf(i + 1)));
					} else {
						ps[i] = new Port(dx, dy, Port.OUTPUT, 1);
						ps[i].setToolTip(Strings.getter("demultiplexerOutTip", ": " + String.valueOf(i + 1)));
					}
				}
			} else if (i > 5) {
				if (dir == Direction.EAST) {
					dx = (i - 5) * 20 + 10;
					dy = -30;
				} else if (dir == Direction.WEST) {
					dx = (11 - i) * 20 - 130;
					dy = 30;
				} else if (dir == Direction.NORTH) {
					dx = -30;
					dy = (10 - i) * 20 - 110;
				} else {// SOUTH
					dx = 30;
					dy = (i - 5) * 20 + 10;
				}
				if (this.name != "7404")// 2 inputs 1 output
					if ((i + 1) % 3 != 0) {
						ps[i] = new Port(dx, dy, Port.INPUT, 1);
						ps[i].setToolTip(Strings.getter("multiplexerInTip", ": " + String.valueOf(13 - (i - 6))));
					} else {
						ps[i] = new Port(dx, dy, Port.OUTPUT, 1);
						ps[i].setToolTip(Strings.getter("demultiplexerOutTip", ": " + String.valueOf(13 - (i - 6))));
					}
				else {// 1 input 1 output
					if (i % 2 == 0) {
						ps[i] = new Port(dx, dy, Port.INPUT, 1);
						ps[i].setToolTip(Strings.getter("multiplexerInTip", ": " + String.valueOf(13 - (i - 6))));
					} else {
						ps[i] = new Port(dx, dy, Port.OUTPUT, 1);
						ps[i].setToolTip(Strings.getter("demultiplexerOutTip", ": " + String.valueOf(13 - (i - 6))));
					}
				}
			}
		}
		instance.setPorts(ps);
	}
}
