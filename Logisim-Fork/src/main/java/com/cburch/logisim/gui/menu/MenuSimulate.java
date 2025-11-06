/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.cburch.logisim.Main;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitEvent;
import com.cburch.logisim.circuit.CircuitListener;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.Simulator;
import com.cburch.logisim.circuit.SimulatorEvent;
import com.cburch.logisim.circuit.SimulatorListener;
import com.cburch.logisim.gui.log.LogFrame;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.util.StringUtil;

class MenuSimulate extends Menu {
	private class CircuitStateMenuItem extends JMenuItem implements CircuitListener, ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -9015014443997361478L;
		private CircuitState circuitState;

		public CircuitStateMenuItem(CircuitState circuitState) {
			this.circuitState = circuitState;

			Circuit circuit = circuitState.getCircuit();
			circuit.addCircuitListener(this);
			this.setText(circuit.getName());
			addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			menubar.fireStateChanged(currentSim, circuitState);
		}

		@Override
		public void circuitChanged(CircuitEvent event) {
			if (event.getAction() == CircuitEvent.ACTION_SET_NAME) {
				this.setText(circuitState.getCircuit().getName());
			}
		}

		void unregister() {
			Circuit circuit = circuitState.getCircuit();
			circuit.removeCircuitListener(this);
		}
	}

	private class MyListener implements ActionListener, SimulatorListener, ChangeListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			Project proj = menubar.getProject();
			Simulator sim = proj == null ? null : proj.getSimulator();
			if (src == run || src == LogisimMenuBar.SIMULATE_ENABLE) {
				if (sim != null) {
					sim.setIsRunning(!sim.isRunning());
					proj.repaintCanvas();
				}
			} else if (src == reset) {
				if (sim != null)
					sim.requestReset();
			} else if (src == step || src == LogisimMenuBar.SIMULATE_STEP) {
				if (sim != null)
					sim.step();
			} else if (src == tickOnce || src == LogisimMenuBar.TICK_STEP) {
				if (sim != null)
					sim.tick();
			} else if (src == ticksEnabled || src == LogisimMenuBar.TICK_ENABLE) {
				if (sim != null)
					sim.setIsTicking(!sim.isTicking());
			} else if (src == log) {
				LogFrame frame = menubar.getProject().getLogFrame(true);
				frame.setVisible(true);
			}
		}

		@Override
		public void propagationCompleted(SimulatorEvent e) {
		}

		@Override
		public void simulatorStateChanged(SimulatorEvent e) {
			Simulator sim = e.getSource();
			if (sim != currentSim)
				return;
			computeEnabled();
			run.setSelected(sim.isRunning());
			ticksEnabled.setSelected(sim.isTicking());
			double freq = sim.getTickFrequency();
			for (int i = 0; i < tickFreqs.length; i++) {
				TickFrequencyChoice item = tickFreqs[i];
				item.setSelected(freq == item.freq);
			}
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			step.setEnabled(run.isEnabled() && !run.isSelected());
		}

		@Override
		public void tickCompleted(SimulatorEvent e) {
		}
	}

	private class TickFrequencyChoice extends JRadioButtonMenuItem implements ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4765228670969063276L;
		private double freq = -1;
		private boolean isChoice;

		public TickFrequencyChoice(double value, boolean ischoice) {
			isChoice = ischoice;
			if (!isChoice)
				freq = value;
			addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (currentSim != null && !isChoice)
				currentSim.setTickFrequency(freq);
			if (isChoice) {
				String Freq = (String) JOptionPane.showInputDialog(null,
						Strings.get("EnterTickFrequency") + " (0.25 - 4096 Hz)", Strings.get("CustomFrequency"),
						JOptionPane.PLAIN_MESSAGE, null, null, (this.freq != -1 ? this.freq : ""));

				if (Freq != null) {// not cancelled
					try {
						double convert = Math.round(Double.parseDouble(Freq) * 1000.0) / 1000.0;
						// save only if it's in the range
						if (convert >= 0.25 && convert <= 4096) {
							this.freq = convert;
							if (currentSim != null)
								currentSim.setTickFrequency(freq);
							localeChanged();
						} else {
							// error message
							JOptionPane.showMessageDialog(null, Strings.get("FrequencyNumberNotAccepted"));
						}
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, Strings.get("FrequencyNumberNotAccepted"));
					}
				}
			}
		}

		public void localeChanged() {
			double f = freq;
			if (f < 1000 && f > 0) {
				String hzStr;
				if (Math.abs(f - Math.round(f)) < 0.0001) {
					hzStr = "" + (int) Math.round(f);
				} else {
					hzStr = "" + f;
				}
				if (!isChoice)
					setText(StringUtil.format(Strings.get("simulateTickFreqItem"), hzStr));
				else
					setText(Strings.get("Custom") + ": "
							+ StringUtil.format(Strings.get("simulateTickFreqItem"), hzStr));
			} else if (f >= 1000) {
				String kHzStr;
				double kf = (!isChoice) ? Math.round(f / 100) / 10.0 : Math.round(f * 1.0) / 1000.0;
				if (kf == Math.round(kf)) {
					kHzStr = "" + (int) kf;
				} else {
					kHzStr = "" + kf;
				}
				if (!isChoice)
					setText(StringUtil.format(Strings.get("simulateTickKFreqItem"), kHzStr));
				else
					setText(Strings.get("Custom") + ": "
							+ StringUtil.format(Strings.get("simulateTickKFreqItem"), kHzStr));
			} else // is choice and invalid number
				setText(Strings.get("Custom"));
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8099735759353154681L;

	private LogisimMenuBar menubar;
	private MyListener myListener = new MyListener();
	private CircuitState currentState = null;
	private CircuitState bottomState = null;
	private Simulator currentSim = null;

	private MenuItemCheckImpl run;
	private JMenuItem reset = new JMenuItem();
	private MenuItemImpl step;
	private MenuItemCheckImpl ticksEnabled;
	private MenuItemImpl tickOnce;
	private JMenu tickFreq = new JMenu();
	private TickFrequencyChoice[] tickFreqs = { new TickFrequencyChoice(4096, false),
			new TickFrequencyChoice(2048, false), new TickFrequencyChoice(1024, false),
			new TickFrequencyChoice(512, false), new TickFrequencyChoice(256, false),
			new TickFrequencyChoice(128, false), new TickFrequencyChoice(64, false), new TickFrequencyChoice(32, false),
			new TickFrequencyChoice(16, false), new TickFrequencyChoice(8, false), new TickFrequencyChoice(4, false),
			new TickFrequencyChoice(2, false), new TickFrequencyChoice(1, false), new TickFrequencyChoice(0.5, false),
			new TickFrequencyChoice(0.25, false), new TickFrequencyChoice(0, true) };
	private JMenu downStateMenu = new JMenu();
	private ArrayList<CircuitStateMenuItem> downStateItems = new ArrayList<CircuitStateMenuItem>();
	private JMenu upStateMenu = new JMenu();
	private ArrayList<CircuitStateMenuItem> upStateItems = new ArrayList<CircuitStateMenuItem>();
	private JMenuItem log = new JMenuItem();

	public MenuSimulate(LogisimMenuBar menubar) {
		this.menubar = menubar;

		run = new MenuItemCheckImpl(this, LogisimMenuBar.SIMULATE_ENABLE);
		step = new MenuItemImpl(this, LogisimMenuBar.SIMULATE_STEP);
		ticksEnabled = new MenuItemCheckImpl(this, LogisimMenuBar.TICK_ENABLE);
		tickOnce = new MenuItemImpl(this, LogisimMenuBar.TICK_STEP);

		menubar.registerItem(LogisimMenuBar.SIMULATE_ENABLE, run);
		menubar.registerItem(LogisimMenuBar.SIMULATE_STEP, step);
		menubar.registerItem(LogisimMenuBar.TICK_ENABLE, ticksEnabled);
		menubar.registerItem(LogisimMenuBar.TICK_STEP, tickOnce);

		int menuMask = (Main.JAVA_VERSION < 10.0) ? 128 : getToolkit().getMenuShortcutKeyMaskEx();
		run.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, menuMask));
		reset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, menuMask));
		step.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, menuMask));
		tickOnce.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, menuMask));
		ticksEnabled.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, menuMask));

		ButtonGroup bgroup = new ButtonGroup();
		for (int i = 0; i < tickFreqs.length; i++) {
			bgroup.add(tickFreqs[i]);
			tickFreq.add(tickFreqs[i]);
		}

		add(run);
		add(reset);
		add(step);
		addSeparator();
		add(upStateMenu);
		add(downStateMenu);
		addSeparator();
		add(tickOnce);
		add(ticksEnabled);
		add(tickFreq);
		addSeparator();
		add(log);

		setEnabled(false);
		run.setEnabled(false);
		reset.setEnabled(false);
		step.setEnabled(false);
		upStateMenu.setEnabled(false);
		downStateMenu.setEnabled(false);
		tickOnce.setEnabled(false);
		ticksEnabled.setEnabled(false);
		tickFreq.setEnabled(false);

		run.addChangeListener(myListener);
		menubar.addActionListener(LogisimMenuBar.SIMULATE_ENABLE, myListener);
		menubar.addActionListener(LogisimMenuBar.SIMULATE_STEP, myListener);
		menubar.addActionListener(LogisimMenuBar.TICK_ENABLE, myListener);
		menubar.addActionListener(LogisimMenuBar.TICK_STEP, myListener);
		// run.addActionListener(myListener);
		reset.addActionListener(myListener);
		// step.addActionListener(myListener);
		// tickOnce.addActionListener(myListener);
		// ticksEnabled.addActionListener(myListener);
		log.addActionListener(myListener);

		computeEnabled();
	}

	private void clearItems(ArrayList<CircuitStateMenuItem> items) {
		for (CircuitStateMenuItem item : items) {
			item.unregister();
		}
		items.clear();
	}

	@Override
	void computeEnabled() {
		boolean present = currentState != null;
		Simulator sim = this.currentSim;
		boolean simRunning = sim != null && sim.isRunning();
		setEnabled(present);
		run.setEnabled(present);
		reset.setEnabled(present);
		step.setEnabled(present && !simRunning);
		upStateMenu.setEnabled(present);
		downStateMenu.setEnabled(present);
		tickOnce.setEnabled(present);
		ticksEnabled.setEnabled(present && simRunning);
		tickFreq.setEnabled(present);
		menubar.fireEnableChanged();
	}

	public void localeChanged() {
		this.setText(Strings.get("simulateMenu"));
		run.setText(Strings.get("simulateRunItem"));
		reset.setText(Strings.get("simulateResetItem"));
		step.setText(Strings.get("simulateStepItem"));
		tickOnce.setText(Strings.get("simulateTickOnceItem"));
		ticksEnabled.setText(Strings.get("simulateTickItem"));
		tickFreq.setText(Strings.get("simulateTickFreqMenu"));
		for (int i = 0; i < tickFreqs.length; i++) {
			tickFreqs[i].localeChanged();
		}
		downStateMenu.setText(Strings.get("simulateDownStateMenu"));
		upStateMenu.setText(Strings.get("simulateUpStateMenu"));
		log.setText(Strings.get("simulateLogItem"));
	}

	private void recreateStateMenu(JMenu menu, ArrayList<CircuitStateMenuItem> items, int code) {
		menu.removeAll();
		menu.setEnabled(items.size() > 0);
		boolean first = true;
		int mask = (Main.JAVA_VERSION < 10.0) ? 128 : getToolkit().getMenuShortcutKeyMaskEx();
		for (int i = items.size() - 1; i >= 0; i--) {
			JMenuItem item = items.get(i);
			menu.add(item);
			if (first) {
				item.setAccelerator(KeyStroke.getKeyStroke(code, mask));
				first = false;
			} else {
				item.setAccelerator(null);
			}
		}
	}

	private void recreateStateMenus() {
		recreateStateMenu(downStateMenu, downStateItems, KeyEvent.VK_RIGHT);
		recreateStateMenu(upStateMenu, upStateItems, KeyEvent.VK_LEFT);
	}

	public void setCurrentState(Simulator sim, CircuitState value) {
		if (currentState == value)
			return;
		Simulator oldSim = currentSim;
		CircuitState oldState = currentState;
		currentSim = sim;
		currentState = value;
		if (bottomState == null) {
			bottomState = currentState;
		} else if (currentState == null) {
			bottomState = null;
		} else {
			CircuitState cur = bottomState;
			while (cur != null && cur != currentState) {
				cur = cur.getParentState();
			}
			if (cur == null)
				bottomState = currentState;
		}

		boolean oldPresent = oldState != null;
		boolean present = currentState != null;
		if (oldPresent != present) {
			computeEnabled();
		}

		if (currentSim != oldSim) {
			double freq = currentSim == null ? 1.0 : currentSim.getTickFrequency();
			for (int i = 0; i < tickFreqs.length; i++) {
				tickFreqs[i].setSelected(Math.abs(tickFreqs[i].freq - freq) < 0.001);
			}

			if (oldSim != null)
				oldSim.removeSimulatorListener(myListener);
			if (currentSim != null)
				currentSim.addSimulatorListener(myListener);
			myListener.simulatorStateChanged(new SimulatorEvent(sim));
		}

		clearItems(downStateItems);
		CircuitState cur = bottomState;
		while (cur != null && cur != currentState) {
			downStateItems.add(new CircuitStateMenuItem(cur));
			cur = cur.getParentState();
		}
		if (cur != null)
			cur = cur.getParentState();
		clearItems(upStateItems);
		while (cur != null) {
			upStateItems.add(0, new CircuitStateMenuItem(cur));
			cur = cur.getParentState();
		}
		recreateStateMenus();
	}
}
