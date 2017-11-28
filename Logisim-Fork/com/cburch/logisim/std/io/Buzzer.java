package com.cburch.logisim.std.io;

/* Copyright (c) 2014, PUC-Minas. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

import java.awt.Color;
import java.awt.Graphics;
import java.io.StringWriter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JOptionPane;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;

public class Buzzer extends InstanceFactory {
	// public static final Attribute<Integer> FREQ_ATTR =
	// Attributes.forIntegerRange("Frequency" , 100, 5000);

	/*
	 * protected void instanceAttributeChanged(Instance instance, Attribute<?> attr)
	 * { if (attr ==FREQ_ATTR) { //instance.recomputeBounds(); Data d=new
	 * Buzzer.Data(true); d.freq=
	 * (instance.getAttributeValue(FREQ_ATTR)).intValue(); d.sound_changed=true;
	 * d.StartThread(); } }
	 */
	private static class Data implements InstanceData {
		public volatile byte vol;

		public volatile boolean is_on;

		public volatile int freq;
		public volatile boolean sound_changed;
		public volatile boolean still_alive;
		public Thread thread;

		public Data(boolean b) {
			is_on = false;
			freq = 100;
			sound_changed = true;
			still_alive = true;
			is_on = b;
			StartThread();
		}

		@Override
		public Object clone() {
			return new Data(is_on);
		}

		public void StartThread() {
			thread = new Thread(new Runnable() {

				@Override
				public void run() {
					SourceDataLine line = null;
					AudioFormat format = new AudioFormat(11025F, 8, 1, true, false);
					DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

					try {
						line = (SourceDataLine) AudioSystem.getLine(info);
						line.open(format, 11025);
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						JOptionPane.showMessageDialog(null, sw.getBuffer().toString(),
								"ERROR (Buzzer): Could not initialise audio", 0);
						return;
					}
					line.start();
					byte audioData[] = new byte[1102];
					sound_changed = true;

					do {
						if (sound_changed) {
							sound_changed = false;
							double step = 11025D / freq;
							double n = step;
							byte val = vol;

							for (int k = 0; k < audioData.length; k++) {
								n--;
								if (n < 0.0D) {
									n += step;
									val = (byte) (-val);
								}
								audioData[k] = val;
							}
						}
						if (is_on) {
							line.write(audioData, 0, audioData.length);
						}
						try {
							Thread.sleep(99L);
						} catch (Exception e) {
							break;
						}
						if (is_on) {
							continue;
						}
						if (!still_alive) {
							break;
						}
						still_alive = false;
					} while (true);
					line.stop();
					line.close();
				}

			});
			thread.start();
		}

	}

	public Buzzer() {
		super("Buzzer");
		/*
		 * setPorts(new Port[] { new Port( 0, 0, Port.INPUT , 1) // ,new Port(40, 0,
		 * Port.OUTPUT, StdAttr.WIDTH) });
		 */
		Port[] p = new Port[3];
		p[0] = new Port(0, 0, "input", 1);
		p[1] = new Port(0, -10, "input", 16);
		p[2] = new Port(0, 10, "input", 7);

		// String foo = "multiplexerEnableTip";
		p[0].setToolTip(Strings.getter("enableSound"));
		p[1].setToolTip(Strings.getter("buzzerFrequecy"));
		p[2].setToolTip(Strings.getter("buzzerVolume"));

		setPorts(p);

		/*
		 * setAttributes ( new Attribute[] { FREQ_ATTR, }, new Object[] { new
		 * Integer(500) });
		 */

		setOffsetBounds(Bounds.create(0, -20, 40, 40));
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

		g.setColor(Color.BLACK);

		for (int k = 0; k <= 20; k += 5)
			g.drawOval((x + 20) - k, (y + 20) - k, k * 2, k * 2);

		painter.drawPort(0);
		painter.drawPort(1);
		painter.drawPort(2);

		Data d = (Data) painter.getData();
		if (d != null && d.is_on) {
			d.still_alive = true;
			if (!d.thread.isAlive())
				d.StartThread();
		}
	}

	@Override
	public void propagate(InstanceState state) {
		Data d = (Data) state.getData();

		if (d == null)
			state.setData(d = new Data(false));

		d.is_on = state.getPort(0) == Value.TRUE;
		if (d.is_on)
			d.still_alive = true;

		// int freq = (state.getAttributeValue(FREQ_ATTR)).intValue();
		int freq = state.getPort(1).toIntValue();
		int vol = state.getPort(2).toIntValue();

		if (freq != d.freq || vol != d.vol) {
			d.freq = freq;
			d.vol = (byte) vol;
			d.sound_changed = true;
		}

		if (!d.thread.isAlive() && d.is_on)
			d.StartThread();

		// Value out;
		// out = Value.createKnown ( BitWidth.create(32), len );
		// state.setPort(1, out, out.getWidth() + 1);
	}

}