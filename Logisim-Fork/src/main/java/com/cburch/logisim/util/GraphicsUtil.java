/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GraphicsUtil {
	public static final int H_LEFT = -1;
	public static final int H_CENTER = 0;
	public static final int H_RIGHT = 1;
	public static final int V_TOP = -1;
	public static final int V_CENTER = 0;
	public static final int V_BASELINE = 1;
	public static final int V_BOTTOM = 2;
	public static final int V_CENTER_OVERALL = 3;
	private static final String LINE_SEPARATOR = "$";
	private static List<String> lines = new ArrayList<>();

	private static int getLongestWidth(List<String> arr, FontMetrics fm) {
		int longestWidth = 0;

		for (String i : arr) {
			if (fm.stringWidth(i) > longestWidth) {
				longestWidth = fm.stringWidth(i); 
			}
		}
		return longestWidth;
	}
	
	static public void drawArrow(Graphics g, int x0, int y0, int x1, int y1, int headLength, int headAngle) {
		double offs = headAngle * Math.PI / 180.0;
		double angle = Math.atan2(y0 - y1, x0 - x1);
		int[] xs = { x1 + (int) (headLength * Math.cos(angle + offs)), x1,
				x1 + (int) (headLength * Math.cos(angle - offs)) };
		int[] ys = { y1 + (int) (headLength * Math.sin(angle + offs)), y1,
				y1 + (int) (headLength * Math.sin(angle - offs)) };
		g.drawLine(x0, y0, x1, y1);
		g.drawPolyline(xs, ys, 3);
	}

	static public void drawArrow2(Graphics g, int x0, int y0, int x1, int y1, int x2, int y2) {
		int[] xs = { x0, x1, x2 };
		int[] ys = { y0, y1, y2 };
		GraphicsUtil.switchToWidth(g, 7);
		g.drawPolyline(xs, ys, 3);
		Color oldColor = g.getColor();
		g.setColor(Color.WHITE);
		GraphicsUtil.switchToWidth(g, 3);
		g.drawPolyline(xs, ys, 3);
		g.setColor(oldColor);
		GraphicsUtil.switchToWidth(g, 1);
	}

	static public void drawCenteredArc(Graphics g, int x, int y, int r, int start, int dist) {
		g.drawArc(x - r, y - r, 2 * r, 2 * r, start, dist);
	}

	static public void drawCenteredText(Graphics g, String text, int x, int y) {
		drawText(g, text, x, y, H_CENTER, V_CENTER);
	}

	static public void drawText(Graphics g, Font font, String text, int x, int y, int halign, int valign) {
		Font oldfont = g.getFont();
		if (font != null)
			g.setFont(font);
		drawText(g, text, x, y, halign, valign);
		if (font != null)
			g.setFont(oldfont);
	}

	static public void drawText(Graphics g, String text, int x, int y, int halign, int valign) {
		if (text.length() == 0)
			return;
		Rectangle bd = getTextBounds(g, text, x, y, halign, valign);
		for (int i=1; i<=lines.size(); i++) {
			g.drawString(lines.get(i-1), bd.x, bd.y + (i*g.getFontMetrics().getAscent()));
		}
	}

	static public Rectangle getTextBounds(Graphics g, Font font, String text, int x, int y, int halign, int valign) {
		if (g == null)
			return new Rectangle(x, y, 0, 0);
		Font oldfont = g.getFont();
		if (font != null)
			g.setFont(font);
		Rectangle ret = getTextBounds(g, text, x, y, halign, valign);
		if (font != null)
			g.setFont(oldfont);
		return ret;
	}

	static public Rectangle getTextBounds(Graphics g, String text, int x, int y, int halign, int valign) {
		if (g == null)
			return new Rectangle(x, y, 0, 0);
		if (text.contains(LINE_SEPARATOR)) {
			lines = Arrays.asList(text.split("\\"+LINE_SEPARATOR, -1));
		} 
		else {
			lines = new ArrayList<>();
			lines.add(text);
		}
		FontMetrics mets = g.getFontMetrics();
		int width = getLongestWidth(lines, mets);
		int ascent = mets.getAscent();
		int descent = mets.getDescent();
		int height = ascent*lines.size() + descent;

		Rectangle ret = new Rectangle(x, y, width, height);
		switch (halign) {
		case H_CENTER:
			ret.translate(-(width / 2), 0);
			break;
		case H_RIGHT:
			ret.translate(-width, 0);
			break;
		default:
			;
		}
		switch (valign) {
		case V_TOP:
			break;
		case V_CENTER:
			ret.translate(0, -(ascent / 2));
			break;
		case V_CENTER_OVERALL:
			ret.translate(0, -(height / 2));
			break;
		case V_BASELINE:
			ret.translate(0, -ascent);
			break;
		case V_BOTTOM:
			ret.translate(0, -height);
			break;
		default:
			;
		}
		return ret;
	}

	static public void switchToWidth(Graphics g, float width) {
		if (g instanceof Graphics2D) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		}
	}

	static public void switchToWidth(Graphics g, int width) {
		switchToWidth(g, (float) width);
	}
}
