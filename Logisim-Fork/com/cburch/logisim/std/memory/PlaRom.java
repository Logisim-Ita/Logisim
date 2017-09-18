package com.cburch.logisim.std.memory;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstanceLogger;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.MenuExtender;
import com.cburch.logisim.util.GraphicsUtil;

public class PlaRom extends InstanceFactory {

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
		private JMenuItem edit;
		private Instance instance;
		private CircuitState circState;

		public PlaMenu(Instance instance) {
			this.instance = instance;
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			if (evt.getSource() == edit) {
				PlaRomData data = PlaRom.getPlaRomData(instance, circState);
				PlaRomEditWindow window = data.getWindow();
				window.setVisible(true);
			}
		}

		@Override
		public void configureMenu(JPopupMenu menu, Project proj) {
			this.circState = proj.getCircuitState();
			boolean enabled = circState != null;

			this.edit = createItem(enabled, Strings.get("ramEditMenuItem"));
			menu.addSeparator();
			menu.add(this.edit);
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
				PlaRomEditWindow window = data.getWindow();
				window.setVisible(true);
			}
			isPressed = false;
		}

		@Override
		public void paint(InstancePainter painter) {
			Graphics g = painter.getGraphics();
			Bounds bds = painter.getBounds();
			g.setFont(new Font("sans serif", Font.BOLD, 11));
			g.setColor((isPressed) ? Color.LIGHT_GRAY : Color.WHITE);
			g.fillRect(bds.getX() + 4, bds.getY() + bds.getHeight() - bds.getHeight() / 4 - 11, 72, 16);
			g.setColor(Color.BLACK);
			g.drawRect(bds.getX() + 4, bds.getY() + bds.getHeight() - bds.getHeight() / 4 - 12, 72, 16);
			GraphicsUtil.drawCenteredText(g, com.cburch.logisim.gui.menu.Strings.get("editMenu"),
					bds.getX() + bds.getWidth() / 2, bds.getY() + bds.getHeight() - bds.getHeight() / 4 - 6);
		}
	}

	private static final Attribute<Integer> ATTR_INPUTS = Attributes.forIntegerRange("inputs",
			Strings.getter("gateInputsAttr"), 2, 32);

	private static final Attribute<Integer> ATTR_AND = Attributes.forIntegerRange("and", Strings.getter("PlaANDAttr"),
			2, 32);

	private static final Attribute<Integer> ATTR_OUTPUTS = Attributes.forIntegerRange("outputs",
			Strings.getter("PlaOutputsAttr"), 2, 32);

	public static PlaRomData getPlaRomData(Instance instance, CircuitState state) {
		int inputs = instance.getAttributeValue(ATTR_INPUTS);
		int outputs = instance.getAttributeValue(ATTR_OUTPUTS);
		int and = instance.getAttributeValue(ATTR_AND);
		PlaRomData ret = (PlaRomData) instance.getData(state);
		if (ret == null) {
			ret = new PlaRomData(inputs, outputs, and, instance);
			instance.setData(state, ret);
		} else {
			ret.updateSize(inputs, outputs, and);
		}
		return ret;
	}

	public static PlaRomData getPlaRomData(InstanceState state) {
		int inputs = state.getAttributeValue(ATTR_INPUTS);
		int outputs = state.getAttributeValue(ATTR_OUTPUTS);
		int and = state.getAttributeValue(ATTR_AND);
		PlaRomData ret = (PlaRomData) state.getData();
		if (ret == null) {
			ret = new PlaRomData(inputs, outputs, and, state);
			state.setData(ret);
		} else {
			ret.updateSize(inputs, outputs, and);
		}
		return ret;
	}

	public PlaRom() {
		super("PlaRom", Strings.getter("PlaRomComponent"));
		setIconName("plarom.gif");
		setAttributes(new Attribute[] { ATTR_INPUTS, ATTR_AND, ATTR_OUTPUTS }, new Object[] { 4, 4, 4 });
		setOffsetBounds(Bounds.create(0, -40, 80, 80));
		setInstancePoker(Poker.class);
		setInstanceLogger(Logger.class);
	}

	@Override
	protected void configureNewInstance(Instance instance) {
		instance.addAttributeListener();
		updateports(instance);
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
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		PlaRomData data = getPlaRomData(painter);
		Graphics g = painter.getGraphics();
		painter.drawBounds();
		Bounds bds = painter.getBounds();
		g.setFont(new Font("sans serif", Font.BOLD, 11));
		GraphicsUtil.drawCenteredText(g, Strings.getter("PlaRomComponent").toString(), bds.getX() + bds.getWidth() / 2,
				bds.getY() + bds.getHeight() / 4);
		GraphicsUtil.drawCenteredText(g, data.getSizeString(), bds.getX() + bds.getWidth() / 2,
				bds.getY() + bds.getHeight() * 2 / 5);
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
		painter.drawPort(3, Strings.get("memEnableLabel"), Direction.NORTH);
	}

	@Override
	public void propagate(InstanceState state) {
		PlaRomData data = getPlaRomData(state);
		PlaRomEditWindow window = data.getWindow();
		Value clear = state.getPort(2);
		Value enable = state.getPort(3);
		if (clear == Value.TRUE)
			data.ClearMatrixValues();
		if (enable != Value.FALSE) {
			Value[] inputs = state.getPort(0).getAll();
			for (int i = 0; i < inputs.length / 2; i++) {// reverse array
				Value temp = inputs[i];
				inputs[i] = inputs[inputs.length - i - 1];
				inputs[inputs.length - i - 1] = temp;
			}
			data.setInputsValue(inputs);
		}
		if (window.getVisible())
			window.mp.repaint();
		if (enable != Value.FALSE || clear == Value.TRUE)
			// update the output port value if is enabled or if clear is true
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
		ps[3].setToolTip(Strings.getter("PlaRomEnabletip"));
		instance.setPorts(ps);
	}
}
