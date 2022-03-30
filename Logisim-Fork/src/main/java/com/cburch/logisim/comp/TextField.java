/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.comp;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.LinkedList;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.instance.InstanceTextField;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringGetter;

public class TextField {
	public static final int H_LEFT = GraphicsUtil.H_LEFT;
	public static final int H_CENTER = GraphicsUtil.H_CENTER;
	public static final int H_RIGHT = GraphicsUtil.H_RIGHT;
	public static final int V_TOP = GraphicsUtil.V_TOP;
	public static final int V_CENTER = GraphicsUtil.V_CENTER;
	public static final int V_CENTER_OVERALL = GraphicsUtil.V_CENTER_OVERALL;
	public static final int V_BASELINE = GraphicsUtil.V_BASELINE;
	public static final int V_BOTTOM = GraphicsUtil.V_BOTTOM;

	private int x;
	private int y;
	private int halign;
	private int valign;
	private Font font;
	private Color color;
	private String text = "";
	private int nLines = 1; 
	private int longestWidth = 0;
	private boolean singleLineEditMode = false;
	private LinkedList<TextFieldListener> listeners = new LinkedList<TextFieldListener>();

	public TextField(int x, int y, int halign, int valign) {
		this(x, y, halign, valign, null, null);
	}

	public TextField(int x, int y, int halign, int valign, Font font, Color color) {
		this.x = x;
		this.y = y;
		this.halign = halign;
		this.valign = valign;
		this.font = font;
		this.color = color;
	}

	//
	// listener methods
	//
	public void addTextFieldListener(TextFieldListener l) {
		listeners.add(l);
	}

	public void draw(Graphics g) {
		singleLineEditMode = true; 
		Font oldFont = g.getFont();
		Color oldColor = g.getColor();
		if (font != null)
			g.setFont(font);
		if (color != null)
			g.setColor(color);
		int x = this.x;
		int y = this.y;
		FontMetrics fm = g.getFontMetrics();
		requestLongestWidth(fm);
		int width = ( longestWidth == 0 && fm.stringWidth(text) > 0) ? longestWidth = fm.stringWidth(text) : longestWidth; //if longestWidth is zero but stringWidth(text) is > 0 requestLongestWidth() didn't find any TextFieldCaret object
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		switch (halign) {
		case TextField.H_CENTER:
			x -= width / 2;
			break;
		case TextField.H_RIGHT:
			x -= width;
			break;
		default:
			break;
		}
		switch (valign) {
		case TextField.V_TOP:
			y += ascent;
			break;
		case TextField.V_CENTER:
			y += ascent / 2;
			break;
		case TextField.V_CENTER_OVERALL:
			y += (ascent - descent) / 2;
			break;
		case TextField.V_BOTTOM:
			y -= descent;
			break;
		default:
			break;
		}
		g.drawString(text, x, y);
		g.setFont(oldFont);
		g.setColor(oldColor);
	}

	public void fireTextChanged(TextFieldEvent e) {
		for (TextFieldListener l : new ArrayList<TextFieldListener>(listeners)) {
			l.textChanged(e);
		}
	}

	public Bounds getBounds(Graphics g) {
		int x = this.x;
		int y = this.y;
		FontMetrics fm;
		if (font == null)
			fm = g.getFontMetrics();
		else
			fm = g.getFontMetrics(font);
		requestLongestWidth(fm);
		int width = longestWidth;
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int height = ascent*nLines + descent; 
		switch (halign) {
		case TextField.H_CENTER:
			x -= width / 2;
			break;
		case TextField.H_RIGHT:
			x -= width;
			break;
		default:
			break;
		}
		switch (valign) {
		case TextField.V_TOP:
			y += ascent;
			break;
		case TextField.V_CENTER:
			y += ascent / 2;
			break;
		case TextField.V_CENTER_OVERALL:
			y += (ascent - descent) / 2;
			break;
		case TextField.V_BOTTOM:
			y -= descent;
			break;
		default:
			break;
		}
		return Bounds.create(x, y - ascent, width, height);
	}

	public TextFieldCaret getCaret(Graphics g, int pos) {
		return new TextFieldCaret(this, g, pos);
	}

	
	private void requestLongestWidth(FontMetrics fm) {
		for (TextFieldListener l : new ArrayList<TextFieldListener>(listeners)) {
			if (l instanceof TextFieldCaret) 
				longestWidth = ( (TextFieldCaret) l).getLongestWidth(fm);
		}
	}
	
	public void infoMessage(StringGetter message) {
		for (TextFieldListener l : new ArrayList<TextFieldListener>(listeners)) {
			if (l instanceof InstanceTextField) 
				((InstanceTextField) l).setInfoMessage(message);
		}
	}
	
	//
	// graphics methods
	//
	public TextFieldCaret getCaret(Graphics g, int x, int y) {
		return new TextFieldCaret(this, g, x, y);
	}

	public Color getColor() {
		return color;
	}

	public Font getFont() {
		return font;
	}

	public int getHAlign() {
		return halign;
	}

	public String getText() {
		return text;
	}

	public int getVAlign() {
		return valign;
	}

	//
	// access methods
	//
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void removeTextFieldListener(TextFieldListener l) {
		listeners.remove(l);
	}

	public void setAlign(int halign, int valign) {
		this.halign = halign;
		this.valign = valign;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public void setHorzAlign(int halign) {
		this.halign = halign;
	}

	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setLocation(int x, int y, int halign, int valign) {
		this.x = x;
		this.y = y;
		this.halign = halign;
		this.valign = valign;
	}

	//
	// modification methods
	//
	public void setText(String text) {
		if (!text.equals(this.text)) {
			TextFieldEvent e = new TextFieldEvent(this, this.text, text);
			this.text = text;
			fireTextChanged(e);
		}
	}
	
	public void setLinesSize(int nLines) {
		if (nLines != this.nLines) {
			this.nLines = nLines;
		}
	}
	
	public boolean getEditMode() {
		return singleLineEditMode;
	}

	public void setVertAlign(int valign) {
		this.valign = valign;
	}

}
