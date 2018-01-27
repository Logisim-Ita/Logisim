package com.cburch.logisim.std.memory;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstanceLogger;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.MenuExtender;
import com.cburch.logisim.util.GraphicsUtil;

public class PlaRom extends InstanceFactory {
	static class ContentsAttribute extends Attribute<String> {
		private InstanceState state = null;
		private Instance instance = null;
		private CircuitState circ = null;

		public ContentsAttribute() {
			super("Contents", Strings.getter("romContentsAttr"));
		}

		@Override
		public java.awt.Component getCellEditor(Window source, String value) {
			Project proj = null;
			if (source instanceof Frame)
				proj = ((Frame) source).getProject();
			PlaRomData data = null;
			if (this.state != null)
				data = getPlaRomData(state);
			else if (this.instance != null && this.circ != null) {
				data = getPlaRomData(instance, circ);
			}
			ContentsCell ret = new ContentsCell(data);
			// call mouse click function and open edit window
			ret.mouseClicked(null);
			// if something changed
			if (this.state != null && !data.getSavedData().equals(state.getAttributeValue(CONTENTS_ATTR))) {
				state.fireInvalidated();
				state.getAttributeSet().setValue(CONTENTS_ATTR, data.getSavedData());
				if (proj != null)
					proj.getLogisimFile().setDirty(true);
			} else if (this.instance != null
					&& !data.getSavedData().equals(instance.getAttributeValue(CONTENTS_ATTR))) {
				instance.fireInvalidated();
				instance.getAttributeSet().setValue(CONTENTS_ATTR, data.getSavedData());
				if (proj != null)
					proj.getLogisimFile().setDirty(true);
			}
			return ret;
		}

		@Override
		public String parse(String value) {
			return value;
		}

		public void setData(Instance instance, CircuitState circ) {
			if (this.instance == null)
				this.instance = instance;
			if (this.circ == null)
				this.circ = circ;
		}

		// i don't know other ways to do this, I'll try to change this when I'll know
		// better Burch's code
		public void setData(InstanceState state) {
			if (this.state == null)
				this.state = state;
		}

		@Override
		public String toDisplayString(String value) {
			return Strings.get("romContentsValue");
		}

	}

	public static class ContentsCell extends JLabel implements MouseListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -53754819096800664L;
		private PlaRomData data;

		ContentsCell(PlaRomData data) {
			super(Strings.get("romContentsValue"));
			this.data = data;
			addMouseListener(this);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (data == null)
				return;
			if (data.editWindow() == 1)
				data.ClearMatrixValues();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}
	}

	public static class Logger extends InstanceLogger {

		@Override
		public String getLogName(InstanceState state, Object option) {
			return null;
		}

		@Override
		public Value getLogValue(InstanceState state, Object option) {
			return state.getPort(1);
		}
	}

	private static class PlaMenu implements ActionListener, MenuExtender {
		private JMenuItem edit, clear;
		private Instance instance;
		private CircuitState circState;

		public PlaMenu(Instance instance) {
			this.instance = instance;
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			PlaRomData data = PlaRom.getPlaRomData(instance, circState);
			if (evt.getSource() == edit) {
				if (data.editWindow() == 1)
					data.ClearMatrixValues();
			} else if (evt.getSource() == clear)
				data.ClearMatrixValues();
			// if something changed
			if (!data.getSavedData().equals(instance.getAttributeValue(CONTENTS_ATTR))) {
				instance.fireInvalidated();
				instance.getAttributeSet().setValue(CONTENTS_ATTR, data.getSavedData());
				circState.getProject().getLogisimFile().setDirty(true);
			}
		}

		@Override
		public void configureMenu(JPopupMenu menu, Project proj) {
			this.circState = proj.getCircuitState();
			boolean enabled = circState != null;

			this.edit = createItem(enabled, Strings.get("ramEditMenuItem"));
			this.clear = createItem(enabled, Strings.get("ramClearMenuItem"));
			menu.addSeparator();
			menu.add(this.edit);
			menu.add(this.clear);
		}

		private JMenuItem createItem(boolean enabled, String label) {
			JMenuItem ret = new JMenuItem(label);
			ret.setEnabled(enabled);
			ret.addActionListener(this);
			return ret;
		}
	}

	public static class Poker extends InstancePoker {
		private boolean isPressed = true;

		private boolean isInside(InstanceState state, MouseEvent e) {
			Location loc = state.getInstance().getLocation();
			int dx = e.getX() - loc.getX();
			int dy = e.getY() - loc.getY();
			return (dx >= 4 && dx <= 76 && dy >= 7 && dy <= 24);
		}

		@Override
		public void mousePressed(InstanceState state, MouseEvent e) {
			isPressed = isInside(state, e);
		}

		@Override
		public void mouseReleased(InstanceState state, MouseEvent e) {
			if (isPressed && isInside(state, e)) {
				PlaRomData data = PlaRom.getPlaRomData(state);
				if (data.editWindow() == 1)
					data.ClearMatrixValues();
				// if something changed
				if (!data.getSavedData().equals(state.getAttributeValue(CONTENTS_ATTR))) {
					state.fireInvalidated();
					state.getAttributeSet().setValue(CONTENTS_ATTR, data.getSavedData());
					state.getProject().getLogisimFile().setDirty(true);
				}
			}
			isPressed = false;
		}

		@Override
		public void paint(InstancePainter painter) {
			Graphics g = painter.getGraphics();
			Bounds bds = painter.getBounds();
			g.setFont(new Font("sans serif", Font.BOLD, 11));
			g.setColor((isPressed) ? Color.LIGHT_GRAY : Color.WHITE);
			g.fillRect(bds.getX() + 4, bds.getY() + bds.getHeight() - bds.getHeight() / 4 - 12, 72, 16);
			g.setColor(Color.BLACK);
			g.drawRect(bds.getX() + 4, bds.getY() + bds.getHeight() - bds.getHeight() / 4 - 12, 72, 16);
			GraphicsUtil.drawCenteredText(g, com.cburch.logisim.gui.menu.Strings.get("editMenu"),
					bds.getX() + bds.getWidth() / 2, bds.getY() + bds.getHeight() - bds.getHeight() / 4 - 6);
		}
	}

	private static final Attribute<Integer> ATTR_INPUTS = Attributes.forIntegerRange("inputs",
			Strings.getter("gateInputsAttr"), 1, 32);

	private static final Attribute<Integer> ATTR_AND = Attributes.forIntegerRange("and", Strings.getter("PlaANDAttr"),
			1, 32);

	private static final Attribute<Integer> ATTR_OUTPUTS = Attributes.forIntegerRange("outputs",
			Strings.getter("PlaOutputsAttr"), 1, 32);

	private static final ContentsAttribute CONTENTS_ATTR = new ContentsAttribute();

	public static PlaRomData getPlaRomData(Instance instance, CircuitState state) {
		int inputs = instance.getAttributeValue(ATTR_INPUTS);
		int outputs = instance.getAttributeValue(ATTR_OUTPUTS);
		int and = instance.getAttributeValue(ATTR_AND);
		PlaRomData ret = (PlaRomData) instance.getData(state);
		if (ret == null) {
			ret = new PlaRomData(inputs, outputs, and);
			// if new, fill the content with the saved data
			ret.decodeSavedData(instance.getAttributeValue(CONTENTS_ATTR));
			instance.setData(state, ret);
		} else if (ret.updateSize(inputs, outputs, and)) {
			// if size updated, update the content attribute, written here because can't
			// access PlaRomData object from instanceAttributeChanged method
			instance.getAttributeSet().setValue(CONTENTS_ATTR, ret.getSavedData());
		}
		CONTENTS_ATTR.setData(instance, state);
		return ret;
	}

	public static PlaRomData getPlaRomData(InstanceState state) {
		int inputs = state.getAttributeValue(ATTR_INPUTS);
		int outputs = state.getAttributeValue(ATTR_OUTPUTS);
		int and = state.getAttributeValue(ATTR_AND);
		PlaRomData ret = (PlaRomData) state.getData();
		if (ret == null) {
			ret = new PlaRomData(inputs, outputs, and);
			// if new, fill the content with the saved data
			ret.decodeSavedData(state.getAttributeValue(CONTENTS_ATTR));
			state.setData(ret);
		} else if (ret.updateSize(inputs, outputs, and)) {
			// if size updated, update the content attribute, written here because can't
			// access PlaRomData object from instanceAttributeChanged method
			state.getAttributeSet().setValue(CONTENTS_ATTR, ret.getSavedData());
		}
		CONTENTS_ATTR.setData(state);
		return ret;
	}

	public PlaRom() {
		super("PlaRom", Strings.getter("PlaRomComponent"));
		setIconName("plarom.gif");
		setAttributes(
				new Attribute[] { ATTR_INPUTS, ATTR_AND, ATTR_OUTPUTS, Mem.ATTR_SELECTION, StdAttr.LABEL,
						StdAttr.LABEL_FONT, StdAttr.ATTR_LABEL_COLOR, CONTENTS_ATTR },
				new Object[] { 4, 4, 4, Mem.SEL_LOW, "", StdAttr.DEFAULT_LABEL_FONT, Color.BLACK, "" });
		setOffsetBounds(Bounds.create(0, -40, 80, 80));
		setInstancePoker(Poker.class);
		setInstanceLogger(Logger.class);
	}

	@Override
	protected void configureNewInstance(Instance instance) {
		Bounds bds = instance.getBounds();
		instance.addAttributeListener();
		updateports(instance);
		instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT, StdAttr.ATTR_LABEL_COLOR,
				bds.getX() + bds.getWidth() / 2, bds.getY() + bds.getHeight() / 4, GraphicsUtil.H_CENTER,
				GraphicsUtil.V_CENTER);
	}

	@Override
	protected Object getInstanceFeature(Instance instance, Object key) {
		if (key == MenuExtender.class) {
			return new PlaMenu(instance);
		}
		return super.getInstanceFeature(instance, key);
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == ATTR_INPUTS || attr == ATTR_OUTPUTS)
			updateports(instance);
		else
			instance.fireInvalidated();
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		PlaRomData data = getPlaRomData(painter);
		Graphics g = painter.getGraphics();
		painter.drawRoundBounds(Color.WHITE);
		Bounds bds = painter.getBounds();
		g.setFont(new Font("sans serif", Font.BOLD, 11));
		Object label = painter.getAttributeValue(StdAttr.LABEL);
		if (label == null || label.equals(""))
			GraphicsUtil.drawCenteredText(g, Strings.getter("PlaRomComponent").toString(),
					bds.getX() + bds.getWidth() / 2, bds.getY() + bds.getHeight() / 4);
		GraphicsUtil.drawCenteredText(g, data.getSizeString(), bds.getX() + bds.getWidth() / 2,
				bds.getY() + bds.getHeight() / 2 - 3);
		g.setColor(Color.WHITE);
		g.fillRect(bds.getX() + 4, bds.getY() + bds.getHeight() - bds.getHeight() / 4 - 11, 72, 16);
		g.setColor(Color.BLACK);
		g.drawRect(bds.getX() + 4, bds.getY() + bds.getHeight() - bds.getHeight() / 4 - 12, 72, 16);
		GraphicsUtil.drawCenteredText(g, com.cburch.logisim.gui.menu.Strings.get("editMenu"),
				bds.getX() + bds.getWidth() / 2, bds.getY() + bds.getHeight() - bds.getHeight() / 4 - 6);
		painter.drawPort(0);
		painter.drawPort(1);
		g.setColor(Color.GRAY);
		painter.drawPort(2, Strings.get("ramClrLabel"), Direction.SOUTH);
		painter.drawPort(3, Strings.get("ramCSLabel"), Direction.NORTH);
		painter.drawLabel();
	}

	@Override
	public void propagate(InstanceState state) {
		PlaRomData data = getPlaRomData(state);
		Value clear = state.getPort(2);
		Value cs = state.getPort(3);
		boolean selection = state.getAttributeValue(Mem.ATTR_SELECTION) == Mem.SEL_HIGH;
		boolean ComponentActive = !(cs == Value.FALSE && selection || cs == Value.TRUE && !selection);
		if (!ComponentActive) {
			state.setPort(1, Value.createUnknown(BitWidth.create(data.getOutputs())), Mem.DELAY);
			return;
		}
		if (clear == Value.TRUE) {
			data.setClear(true);
			data.ClearMatrixValues();
			if (state.getAttributeValue(CONTENTS_ATTR) != "" && data.getClear())
				state.getAttributeSet().setValue(CONTENTS_ATTR, data.getSavedData());
		} else
			data.setClear(false);

		Value[] inputs = state.getPort(0).getAll();
		for (int i = 0; i < inputs.length / 2; i++) {// reverse array
			Value temp = inputs[i];
			inputs[i] = inputs[inputs.length - i - 1];
			inputs[inputs.length - i - 1] = temp;
		}
		data.setInputsValue(inputs);
		state.setPort(1, Value.create(data.getOutputValues()), Mem.DELAY);

	}

	private void updateports(Instance instance) {
		int inputbitwidth = instance.getAttributeValue(ATTR_INPUTS);
		int outputbitwidth = instance.getAttributeValue(ATTR_OUTPUTS);
		Port[] ps = new Port[4];
		ps[0] = new Port(0, 0, Port.INPUT, inputbitwidth);
		ps[1] = new Port(80, 0, Port.OUTPUT, outputbitwidth);
		ps[2] = new Port(40, 40, Port.INPUT, 1); // clear
		ps[3] = new Port(40, -40, Port.INPUT, 1); // enable
		ps[0].setToolTip(Strings.getter("demultiplexerInTip"));
		ps[1].setToolTip(Strings.getter("multiplexerOutTip"));
		ps[2].setToolTip(Strings.getter("PlaRomCleartip"));
		if (instance.getAttributeValue(Mem.ATTR_SELECTION) == Mem.SEL_HIGH)
			ps[3].setToolTip(Strings.getter("memCSTip", "0"));
		else
			ps[3].setToolTip(Strings.getter("memCSTip", "1"));
		instance.setPorts(ps);
	}
}
