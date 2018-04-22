package com.cburch.logisim.std.io;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import java.awt.Color;
import java.awt.Graphics;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JOptionPane;

public class Buzzer extends InstanceFactory {
	public static final byte FREQ = 0;
	public static final byte ENABLE = 1;
	public static final byte VOL = 2;

	public Buzzer() {
		super("Buzzer", Strings.getter("buzzerComponent"));

		Port[] p = new Port[3];
		p[FREQ] = new Port(0, -10, Port.INPUT, 16);
		p[ENABLE] = new Port(0, 0, Port.INPUT, 1);
		p[VOL] = new Port(0, 10, Port.INPUT, 7);
		p[FREQ].setToolTip(Strings.getter("buzzerFrequecy"));
		p[ENABLE].setToolTip(Strings.getter("enableSound"));
		p[VOL].setToolTip(Strings.getter("buzzerVolume"));
		setPorts(p);

		setOffsetBounds(Bounds.create(0, -20, 40, 40));
	}

	private static class Data implements InstanceData {
		public volatile boolean is_on = false;
		public volatile int freq = 500;
		public volatile byte vol = 16;
		public volatile boolean sound_changed = true;

		public volatile boolean still_alive = true;
		public Thread thread;

		public void StartThread() {
			thread = new Thread(new Runnable() {
				public void run() {
					SourceDataLine line = null;

					AudioFormat format = new AudioFormat(11025.0F, 8, 1, true, false);
					DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

					try {
						line = (SourceDataLine) AudioSystem.getLine(info);
						line.open(format, 11025);
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						JOptionPane.showMessageDialog(null, sw.getBuffer().toString(), "Could not initialise audio", 0);
						return;
					}

					line.start();

					byte[] audioData = new byte['ю'];
					sound_changed = true;
					int i = 0;
					for (;;) {
						if (sound_changed) {
							sound_changed = false;
							double step = 11025.0D / freq;
							double n = step;
							byte val = vol;
							for (int k = 0; k < audioData.length; k++) {
								n -= 1.0D;
								if (n < 0.0D) {
									n += step;
									val = (byte) -val;
								}
								audioData[k] = val;
							}
						}
						if (is_on)
							line.write(audioData, 0, audioData.length);
						try {
							Thread.sleep(99L);
						} catch (Exception e) {
							break;
						}
						i++;
						if (i == 10) {
							if (!still_alive)
								break;
							still_alive = false;
							i = 0;
						}
					}
					line.stop();
					line.close();
				}
			});
			thread.start();
		}

		public Data(boolean b) {
			is_on = b;

			StartThread();
		}

		public Object clone() {
			return new Data(is_on);
		}
	}

	public void propagate(InstanceState state) {
		Data d = (Data) state.getData();
		if (d == null) {
			state.setData(d = new Data(false));
		}
		d.is_on = (state.getPort(ENABLE) == Value.TRUE);
		if (d.is_on)
			d.still_alive = true;
		int freq = state.getPort(FREQ).toIntValue();
		int vol = state.getPort(VOL).toIntValue();
		if ((freq != d.freq) || (vol != d.vol)) {
			d.freq = freq;
			d.vol = ((byte) vol);
			d.sound_changed = true;
		}
		if ((!d.thread.isAlive()) && (d.is_on))
			d.StartThread();
	}

	public void paintInstance(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		Bounds b = painter.getBounds();
		int x = b.getX();
		int y = b.getY();
		g.setColor(Color.BLACK);
		for (int k = 0; k <= 20; k += 5) {
			g.drawOval(x + 20 - k, y + 20 - k, k * 2, k * 2);
		}
		painter.drawPorts();
		Data d = (Data) painter.getData();
		if (d != null) {
			d.still_alive = true;
			if (!d.thread.isAlive()) {
				d.StartThread();
			}
		}
	}

	public void paintGhost(InstancePainter painter) {
		Bounds b = painter.getBounds();
		Graphics g = painter.getGraphics();
		g.setColor(Color.GRAY);
		g.drawOval(b.getX(), b.getY(), 40, 40);
	}
}
