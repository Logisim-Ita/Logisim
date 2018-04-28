package com.cburch.logisim.std.io;

import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.GraphicsUtil;

public class Buzzer extends InstanceFactory {
	private static class Data implements InstanceData {
		public AtomicBoolean is_on = new AtomicBoolean(false);
		private final int SAMPLE_RATE = 44100;
		public int hz = 500;
		public byte vol = 1;
		public Thread thread;

		public Data() {
			StartThread();
		}

		@Override
		public Object clone() {
			return new Data();
		}

		public void StartThread() {
			thread = new Thread(new Runnable() {
				@Override
				public void run() {
					SourceDataLine line = null;
					AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);

					try {
						line = AudioSystem.getSourceDataLine(format);
						line.open(format, 2200);
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("Could not initialise audio");
						return;
					}
					line.start();
					byte[] audioData = new byte[1];
					while (is_on.get()) {
						for (int i = 0; is_on.get() && i < 44100; i++) {
							double angle = 2 * Math.PI * i * hz / SAMPLE_RATE;
							audioData[0] = (byte) Math.round(Math.sin(angle) * vol);
							line.write(audioData, 0, 1);
						}
					}
					line.drain();
					line.stop();
					line.close();
				}
			});
			thread.setName("Sound Thread");
			thread.start();
		}
	}

	public static final byte FREQ = 0;
	public static final byte ENABLE = 1;
	public static final byte VOL = 2;

	public Buzzer() {
		super("Buzzer", Strings.getter("buzzerComponent"));
		setAttributes(new Attribute[] { StdAttr.FACING, StdAttr.LABEL, StdAttr.LABEL_FONT, StdAttr.ATTR_LABEL_COLOR },
				new Object[] { Direction.WEST, "", StdAttr.DEFAULT_LABEL_FONT, Color.BLACK });
		setFacingAttribute(StdAttr.FACING);
		setIconName("buzzer.gif");
	}

	@Override
	protected void configureNewInstance(Instance instance) {
		Bounds b = instance.getBounds();
		updateports(instance);
		instance.addAttributeListener();
		instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT, StdAttr.ATTR_LABEL_COLOR, b.getX() + b.getWidth() / 2,
				b.getY() - 3, GraphicsUtil.H_CENTER, GraphicsUtil.V_BOTTOM);
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		Direction dir = attrs.getValue(StdAttr.FACING);
		if (dir == Direction.EAST || dir == Direction.WEST)
			return Bounds.create(-40, -20, 40, 40).rotate(Direction.EAST, dir, 0, 0);
		else
			return Bounds.create(-20, 0, 40, 40).rotate(Direction.NORTH, dir, 0, 0);

	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == StdAttr.FACING) {
			instance.recomputeBounds();
			updateports(instance);
		}
	}

	@Override
	public void paintGhost(InstancePainter painter) {
		Bounds b = painter.getBounds();
		Graphics g = painter.getGraphics();
		g.setColor(Color.GRAY);
		g.drawOval(b.getX(), b.getY(), 40, 40);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		Bounds b = painter.getBounds();
		int x = b.getX();
		int y = b.getY();
		byte height = (byte) b.getHeight();
		byte width = (byte) b.getWidth();
		g.setColor(Color.DARK_GRAY);
		g.fillOval(x, y, 40, 40);
		g.setColor(Color.GRAY);
		GraphicsUtil.switchToWidth(g, 1.5f);
		for (byte k = 8; k <= 16; k += 4) {
			g.drawOval(x + 20 - k, y + 20 - k, k * 2, k * 2);
		}
		GraphicsUtil.switchToWidth(g, 2);
		g.setColor(Color.DARK_GRAY);
		g.drawLine(x + 4, y + height / 2, x + 36, y + height / 2);
		g.drawLine(x + width / 2, y + 4, x + width / 2, y + 36);
		g.setColor(Color.BLACK);
		g.fillOval(x + 15, y + 15, 10, 10);
		g.drawOval(x, y, 40, 40);
		painter.drawPorts();
		painter.drawLabel();
	}

	@Override
	public void propagate(InstanceState state) {
		Data d = (Data) state.getData();
		if (d == null) {
			state.setData(d = new Data());
		}
		d.is_on.set(state.getPort(ENABLE) == Value.TRUE);
		int freq = state.getPort(FREQ).toIntValue();
		byte vol = (byte) state.getPort(VOL).toIntValue();
		if (freq >= 0)
			d.hz = freq;
		if (vol >= 0)
			d.vol = vol;
		if ((!d.thread.isAlive()) && (d.is_on.get()))
			d.StartThread();
	}

	public void stopSound(CircuitState circuitState, Component comp) {
		Data d = (Data) circuitState.getData(comp);
		if (d != null && d.is_on.get()) {
			d.is_on.set(false);
		}
	}

	private void updateports(Instance instance) {
		Direction dir = instance.getAttributeValue(StdAttr.FACING);
		Port[] p = new Port[3];
		if (dir == Direction.EAST || dir == Direction.WEST) {
			p[FREQ] = new Port(0, -10, Port.INPUT, 12);
			p[VOL] = new Port(0, 10, Port.INPUT, 7);
		} else {
			p[FREQ] = new Port(-10, 0, Port.INPUT, 12);
			p[VOL] = new Port(10, 0, Port.INPUT, 7);
		}
		p[ENABLE] = new Port(0, 0, Port.INPUT, 1);
		p[FREQ].setToolTip(Strings.getter("buzzerFrequecy"));
		p[ENABLE].setToolTip(Strings.getter("enableSound"));
		p[VOL].setToolTip(Strings.getter("buzzerVolume"));
		instance.setPorts(p);

	}
}
