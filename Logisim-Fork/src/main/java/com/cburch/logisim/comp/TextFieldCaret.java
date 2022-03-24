/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.comp;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.tools.Caret;
import com.cburch.logisim.tools.CaretEvent;
import com.cburch.logisim.tools.CaretListener;
import com.cburch.logisim.comp.Strings;

class TextFieldCaret implements Caret, TextFieldListener {
	private LinkedList<CaretListener> listeners = new LinkedList<CaretListener>();
	private TextField field;
	private Graphics g;
	private String oldText;
	private String curText;
	private int posx;
	private int posy = 0;
	private static final String LINE_SEPARATOR = "$";
	private List<String> lines = new ArrayList<>();
	
	public TextFieldCaret(TextField field, Graphics g, int posx) {
		this.field = field;
		this.g = g;
		this.oldText = field.getText();
		this.curText = field.getText();
		this.posx = posx;

		field.addTextFieldListener(this);
		refreshLines(); //If there is already the TextField (i.e. double click editing) sync lines list
		
		if (!field.getEditMode()) {
			field.infoMessage(Strings.getter("MultilineTip"));
		}
	}

	public TextFieldCaret(TextField field, Graphics g, int x, int y) {
		this(field, g, 0);
		moveCaret(x, y);
	}

	@Override
	public void addCaretListener(CaretListener l) {
		listeners.add(l);
	}
	
	public int getLongestWidth(FontMetrics fm) {
		int longestWidth = 0;
		for (String i : lines) {
			if (fm.stringWidth(i) > longestWidth) {
				longestWidth = fm.stringWidth(i); 
			}
		}
		return longestWidth;
	}
	
	private int getAbsolutePosition(List<String> arr, int posx, int posy) { 
		int pos = 0;
		
		for (int i=0; i<posy; i++) {
			pos+=(arr.get(i).length()+1); 
		}
		pos+=posx;
		return pos;
	}
	

	private void refreshLines() {
		if (curText.contains(LINE_SEPARATOR) && !field.getEditMode()) {
			lines = Arrays.asList(curText.split("\\"+LINE_SEPARATOR, -1)); //-1 is to include also empty tokens
			field.setLinesSize(lines.size()); //Sync with TextField class
		}
		else {		
			lines = new ArrayList<>(); //Empty the list
			lines.add(curText);
		}
	}

	@Override
	public void cancelEditing() {
		CaretEvent e = new CaretEvent(this, oldText, oldText);
		curText = oldText;
		posx = curText.length();
		for (CaretListener l : new ArrayList<CaretListener>(listeners)) {
			l.editingCanceled(e);
		}
		field.removeTextFieldListener(this);
	}

	@Override
	public void commitText(String text) {
		curText = text;
		posx = curText.length();
		field.setText(text);
	}

	@Override
	public void draw(Graphics g) {
		if (field.getFont() != null)
			g.setFont(field.getFont());
		// draw boundary
		Bounds bds = getBounds(g);
		g.setColor(new Color(255, 255, 255, 128));
		g.fillRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
		g.setColor(Color.black);
		g.drawRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());

		if (field.getColor() != null)
			g.setColor(field.getColor());
		// draw text
		int x = field.getX();
		int y = field.getY();
		FontMetrics fm = g.getFontMetrics();
		int width = getLongestWidth(fm);
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		
		switch (field.getHAlign()) {
		case TextField.H_CENTER:
			x -= width / 2;
			break;
		case TextField.H_RIGHT:
			x -= width;
			break;
		default:
			break;
		}
		switch (field.getVAlign()) {
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
		refreshLines();
		for (int i=0; i<lines.size(); i++) {
			g.drawString(lines.get(i), x, y+(i*ascent));
		}
		g.setColor(Color.BLACK);
		// draw cursor
		if (posx > 0)
			x += fm.stringWidth(lines.get(posy).substring(0, posx));
		y += (posy*ascent);
		g.drawLine(x, y + descent, x, y - ascent);
	
	}

	@Override
	public Bounds getBounds(Graphics g) {
		int x = field.getX();
		int y = field.getY();
		Font font = field.getFont();
		FontMetrics fm;
		if (font == null)
			fm = g.getFontMetrics();
		else
			fm = g.getFontMetrics(font);
		int width = getLongestWidth(fm);
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int height = ascent*lines.size() + descent; 
		switch (field.getHAlign()) {
		case TextField.H_CENTER:
			x -= width / 2;
			break;
		case TextField.H_RIGHT:
			x -= width;
			break;
		default:
			break;
		}
		switch (field.getVAlign()) {
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
		return Bounds.create(x, y - ascent, width, height).add(field.getBounds(g)).expand(3);
	}

	@Override
	public String getText() {
		return curText;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int absPos = getAbsolutePosition(lines, posx, posy);
		int ign = InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK;
		if ((e.getModifiersEx() & ign) != 0)
			return;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_KP_LEFT:
			if (posx > 0)
				--posx;
			else
				if (posy-1 >= 0) {
					--posy;
					posx=lines.get(posy).length();
				}
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_KP_RIGHT:
			if (posx < lines.get(posy).length())
				++posx;
			else
				if (posy+1 < lines.size()) {
					++posy;
					posx=0;
				}
			break;
		case KeyEvent.VK_UP:
		case KeyEvent.VK_KP_UP:
			if (posy-1 >= 0) {
				--posy;
				if (lines.get(posy).length() < posx)
					posx = lines.get(posy).length();
			}
			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_KP_DOWN:
			if (posy+1 < lines.size()) {
				++posy;
				if (lines.get(posy).length() < posx)
					posx = lines.get(posy).length();
			}
			break;
		case KeyEvent.VK_HOME:
			posx = 0;
			break;
		case KeyEvent.VK_END:
			posx = lines.get(posy).length();
			break;
		case KeyEvent.VK_ESCAPE:
		case KeyEvent.VK_CANCEL:
			cancelEditing();
			break;
		case KeyEvent.VK_CLEAR:
			curText = "";
			posx = 0;
			posy = 0;
			break;
		case KeyEvent.VK_ENTER:
			if (e.isShiftDown() && !field.getEditMode())
			{
				if (absPos < curText.length()) {
					curText = curText.substring(0, absPos) + LINE_SEPARATOR + curText.substring(absPos);
				} else {				
					curText += LINE_SEPARATOR;
				}
				++posy;
				posx=0;
				field.setText(curText);
			}
			else
			{
				stopEditing(); 	
			}
			break;
		case KeyEvent.VK_BACK_SPACE:
			if (absPos > 0) {
				curText = curText.substring(0, absPos-1) + curText.substring(absPos);
				field.setText(curText);
				
				if (posx > 0) {
					--posx;
				}
				else {
					if (posy-1 >= 0) {
						--posy;
						posx=lines.get(posy).length();
					}
				}
			}
			
			break;
		case KeyEvent.VK_DELETE:	
			if (absPos < curText.length()) {
				curText = curText.substring(0, absPos) + curText.substring(absPos + 1);
				field.setText(curText);
			}
			break;
		case KeyEvent.VK_INSERT:
		case KeyEvent.VK_COPY:
		case KeyEvent.VK_CUT:
		case KeyEvent.VK_PASTE:
			// TODO: enhance label editing
			break;
		default:
			; // ignore
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
		int absPos = getAbsolutePosition(lines, posx, posy);
		int ign = InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK;
		if ((e.getModifiersEx() & ign) != 0)
			return;

		char c = e.getKeyChar();
		if (c == '\n' || c == LINE_SEPARATOR.charAt(0)) {
			; //Avoid \n character concatenation to the string, see the KeyPressed() method above for the newline handling
		} else if (c != KeyEvent.CHAR_UNDEFINED && !Character.isISOControl(c)) {
			if (absPos < curText.length()) {
				curText = curText.substring(0, absPos) + c + curText.substring(absPos);
			} else {
				curText += c;
			}
			++posx;
			field.setText(curText);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO: enhance label editing
		moveCaret(e.getX(), e.getY());
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO: enhance label editing
		moveCaret(e.getX(), e.getY());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO: enhance label editing
		moveCaret(e.getX(), e.getY());
	}

	private void moveCaret(int x, int y) {
		Bounds bds = field.getBounds(g);
		Font font = field.getFont();
		FontMetrics fm;
		if (font == null)
			fm = g.getFontMetrics();
		else
			fm = g.getFontMetrics(font);
		x -= bds.getX();
		int last = 0;
		for (int i = 0; i < curText.length(); i++) {
			int cur = fm.stringWidth(curText.substring(0, i + 1)) - 3;
			if (x <= (cur + last) / 2) {
				posx = i;
				return;
			}
			last = cur;
		}
		posx = curText.length();
	}

	@Override
	public void removeCaretListener(CaretListener l) {
		listeners.remove(l);
	}

	@Override
	public void stopEditing() {
		CaretEvent e = new CaretEvent(this, oldText, curText);
		field.setText(curText);
		for (CaretListener l : new ArrayList<CaretListener>(listeners)) {
			l.editingStopped(e);
		}
		field.infoMessage(null);
		field.removeTextFieldListener(this);
	}

	@Override
	public void textChanged(TextFieldEvent e) {
		curText = field.getText();
		oldText = curText;
		if (Math.abs(e.getText().length() - e.getOldText().length()) > 1)
			posx = curText.length();
	}
}
