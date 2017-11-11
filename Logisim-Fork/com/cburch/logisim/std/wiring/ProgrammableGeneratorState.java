package com.cburch.logisim.std.wiring;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.util.LocaleManager;

public class ProgrammableGeneratorState implements InstanceData, Cloneable {
	Value sending = Value.FALSE;
	private int[] durationHigh;
	private int[] durationLow;
	// number of clock ticks performed in the current state
	private int ticks, currentstate;
	private JTextField[] inputs;

	public ProgrammableGeneratorState(int i) {
		durationHigh = new int[i];
		durationLow = new int[i];
		clearValues();
	}

	public void clearValues() {
		this.ticks = 0;
		this.currentstate = 0;
		// set all the values to 1
		for (int i = 0; i < durationHigh.length; i++) {
			durationHigh[i] = 1;
			durationLow[i] = 1;
		}
	}

	@Override
	public ProgrammableGeneratorState clone() {
		try {
			return (ProgrammableGeneratorState) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public void editWindow() {
		// array of jtextfields, here will be saved the new values
		inputs = new JTextField[this.durationHigh.length + this.durationLow.length];
		String[] options = new String[] { new LocaleManager("resources/logisim", "gui").get("saveOption"),
				Strings.get("ramClearMenuItem") };
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbs = new GridBagConstraints();
		// to remove insets
		Insets empty = new Insets(0, 0, 0, 0);
		// insets between states
		Insets newstate = new Insets(5, 0, 10, 0);
		// arrow font
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
		// state number font
		Font state = new Font(Font.SANS_SERIF, Font.BOLD, 18);
		JLabel up, down, statenumber;
		for (int i = 0; i < inputs.length; i += 2) {
			statenumber = new JLabel(String.valueOf(i / 2 + 1));
			statenumber.setFont(state);
			statenumber.setForeground(Color.DARK_GRAY);
			gbs.gridx = 0;
			gbs.gridy = i;
			// 2 rows height
			gbs.gridheight = 2;
			// x padding
			gbs.ipadx = 10;
			gbs.insets = empty;
			gbs.fill = GridBagConstraints.VERTICAL;
			panel.add(statenumber, gbs);
			up = new JLabel("↑ ");
			up.setFont(font);
			gbs.gridx = 1;
			gbs.gridy = i;
			gbs.gridheight = 1;
			gbs.ipadx = 0;
			panel.add(up, gbs);
			inputs[i] = new JTextField(String.valueOf(getdurationHigh(i / 2)), 20);
			gbs.gridx = 2;
			gbs.gridy = i;
			gbs.gridheight = 1;
			panel.add(inputs[i], gbs);
			down = new JLabel("↓ ");
			down.setFont(font);
			gbs.gridx = 1;
			gbs.gridy = i + 1;
			gbs.gridheight = 1;
			panel.add(down, gbs);
			inputs[i + 1] = new JTextField(String.valueOf(getdurationLow(i / 2)), 20);
			gbs.gridx = 2;
			gbs.gridy = i + 1;
			gbs.gridheight = 1;
			gbs.insets = newstate;
			panel.add(inputs[i + 1], gbs);
		}
		JScrollPane scrollable = new JScrollPane(panel);
		scrollable.setPreferredSize(new Dimension(175, 300));
		scrollable.setBorder(null);

		int option = JOptionPane.showOptionDialog(null, scrollable,
				Strings.getter("ProgrammableGeneratorComponent").get(), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, null);
		if (option == 0)
			SaveValues(inputs);
		else if (option == 1)
			clearValues();
	}

	private int getdurationHigh(int i) {
		return this.durationHigh[i];
	}

	public int getdurationHighValue() {
		return this.durationHigh[this.currentstate];
	}

	private int getdurationLow(int i) {
		return this.durationLow[i];
	}

	public int getdurationLowValue() {
		return this.durationLow[this.currentstate];
	}

	public int getStateTick() {
		return this.ticks;
	}

	public void incrementCurrentState() {
		this.ticks = 1;
		this.currentstate++;
		if (this.currentstate >= this.durationHigh.length)
			this.currentstate = 0;
	}

	public void incrementTicks() {
		this.ticks++;
		if (this.ticks > getdurationHighValue() + getdurationLowValue())
			incrementCurrentState();
	}

	private void SaveValues(JTextField[] inputs) {
		String onlynumber;
		int value;
		for (int i = 0; i < inputs.length; i++) {
			onlynumber = "";
			value = 0;
			// create a string composed by the digits of the text field
			for (int j = 0; j < inputs[i].getText().length(); j++) {
				if (Character.isDigit(inputs[i].getText().charAt(j)))
					onlynumber += inputs[i].getText().charAt(j);
			}
			// if there are no digits the value is 0 and it isn't saved
			if (onlynumber != "")
				value = Integer.parseInt(onlynumber);
			if (value >= 1) {
				if (i % 2 == 0)
					setdurationHigh(i / 2, value);
				else
					setdurationLow(i / 2, value);
			}
		}
	}

	public void setdurationHigh(int i, int value) {
		if (value != getdurationHigh(i))
			this.durationHigh[i] = value;
	}

	public void setdurationLow(int i, int value) {
		if (value != getdurationLow(i))
			this.durationLow[i] = value;
	}

	public void updateSize(int newsize) {
		if (newsize != this.durationHigh.length) {
			// update arrays size maintaining values
			int[] oldDurationHigh = Arrays.copyOf(durationHigh, durationHigh.length);
			int[] oldDurationLow = Arrays.copyOf(durationLow, durationLow.length);
			durationHigh = new int[newsize];
			durationLow = new int[newsize];
			clearValues();
			int lowerlength = (oldDurationHigh.length < newsize) ? oldDurationHigh.length : newsize;
			for (int i = 0; i < lowerlength; i++) {
				durationHigh[i] = oldDurationHigh[i];
				durationLow[i] = oldDurationLow[i];
			}
		}
	}
}
