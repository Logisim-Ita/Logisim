/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.shapes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.cburch.draw.model.AbstractCanvasObject;
import com.cburch.draw.model.CanvasObject;
import com.cburch.draw.model.Handle;
import com.cburch.draw.model.HandleGesture;
import com.cburch.draw.util.EditableLabel;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.util.UnmodifiableList;

public class Text extends AbstractCanvasObject {
	private EditableLabel label;

	private Text(int x, int y, int halign, int valign, String text, Font font, Color color) {
		label = new EditableLabel(x, y, text, font, color);
		label.setColor(color);
		label.setHorizontalAlignment(halign);
		label.setVerticalAlignment(valign);
	}

	public Text(int x, int y, String text) {
		this(x, y, EditableLabel.LEFT, EditableLabel.BASELINE, text, DrawAttr.DEFAULT_FONT, Color.BLACK);
	}

	@Override
	public Text clone() {
		Text ret = (Text) super.clone();
		ret.label = this.label.clone();
		return ret;
	}

	@Override
	public boolean contains(Location loc, boolean assumeFilled) {
		return label.contains(loc.getX(), loc.getY());
	}

	@Override
	public List<Attribute<?>> getAttributes() {
		return DrawAttr.ATTRS_TEXT;
	}

	@Override
	public Bounds getBounds() {
		return label.getBounds();
	}

	@Override
	public String getDisplayName() {
		return Strings.get("shapeText");
	}

	public List<Handle> getHandles() {
		Bounds bds = label.getBounds();
		int x = bds.getX();
		int y = bds.getY();
		int w = bds.getWidth();
		int h = bds.getHeight();
		return UnmodifiableList.create(new Handle[] { new Handle(this, x, y), new Handle(this, x + w, y),
				new Handle(this, x + w, y + h), new Handle(this, x, y + h) });
	}

	@Override
	public List<Handle> getHandles(HandleGesture gesture) {
		return getHandles();
	}

	public EditableLabel getLabel() {
		return label;
	}

	public Location getLocation() {
		return Location.create(label.getX(), label.getY());
	}

	public String getText() {
		return label.getText();
	}

	@Override
	public <V> V getValue(Attribute<V> attr) {
		if (attr == DrawAttr.FONT) {
			return (V) label.getFont();
		} else if (attr == DrawAttr.FILL_COLOR) {
			return (V) label.getColor();
		} else if (attr == DrawAttr.ALIGNMENT) {
			int halign = label.getHorizontalAlignment();
			AttributeOption h;
			if (halign == EditableLabel.LEFT) {
				h = DrawAttr.ALIGN_LEFT;
			} else if (halign == EditableLabel.RIGHT) {
				h = DrawAttr.ALIGN_RIGHT;
			} else {
				h = DrawAttr.ALIGN_CENTER;
			}
			return (V) h;
		} else {
			return null;
		}
	}

	@Override
	public boolean matches(CanvasObject other) {
		if (other instanceof Text) {
			Text that = (Text) other;
			return this.label.equals(that.label);
		} else {
			return false;
		}
	}

	@Override
	public int matchesHashCode() {
		return label.hashCode();
	}

	@Override
	public void paint(Graphics g, HandleGesture gesture) {
		label.paint(g);
	}

	public void setText(String value) {
		label.setText(value);
	}

	@Override
	public Element toSvgElement(Document doc) {
		return SvgCreator.createText(doc, this);
	}

	@Override
	public void translate(int dx, int dy) {
		label.setLocation(label.getX() + dx, label.getY() + dy);
	}

	@Override
	public void updateValue(Attribute<?> attr, Object value) {
		if (attr == DrawAttr.FONT) {
			label.setFont((Font) value);
		} else if (attr == DrawAttr.FILL_COLOR) {
			label.setColor((Color) value);
		} else if (attr == DrawAttr.ALIGNMENT) {
			Integer intVal = (Integer) ((AttributeOption) value).getValue();
			label.setHorizontalAlignment(intVal.intValue());
		}
	}
}
