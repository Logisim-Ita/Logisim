package com.cburch.logisim.std.memory;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.util.LocaleManager;

public class PlaRomData implements InstanceData {
	private int inputs, outputs, and;
	private boolean clear = false;
	private boolean[][] InputAnd;
	private boolean[][] AndOutput;
	private Value[] InputValue;
	private Value[] AndValue;
	private Value[] OutputValue;
	private String[] options = new String[] { new LocaleManager("resources/logisim", "gui").get("saveOption"),
			Strings.get("ramClearMenuItem") };
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private JScrollPane panel;
	private PlaRomPanel drawing;

	public PlaRomData(int inputs, int outputs, int and) {
		this.inputs = inputs;
		this.outputs = outputs;
		this.and = and;
		InputAnd = new boolean[getAnd()][getInputs() * 2];
		AndOutput = new boolean[getAnd()][getOutputs()];
		InputValue = new Value[getInputs()];
		AndValue = new Value[getAnd()];
		OutputValue = new Value[getOutputs()];
		InitializeInputValue();
		setAndValue();
		setOutputValue();
	}

	public void ClearMatrixValues() {
		for (int i = 0; i < getAnd(); i++) {
			for (int j = 0; j < getOutputs(); j++) {
				setAndOutputValue(i, j, false);
			}
			for (int k = 0; k < getInputs() * 2; k++) {
				setInputAndValue(i, k, false);
			}
		}
	}

	@Override
	public PlaRomData clone() {
		try {
			PlaRomData ret = (PlaRomData) super.clone();
			return ret;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public int getAnd() {
		return this.and;
	}

	public boolean getAndOutputValue(int row, int column) {
		return this.AndOutput[row][column];
	}

	public void setClear(boolean b) {
		this.clear = b;
	}

	public boolean getClear() {
		return this.clear;
	}

	public Value getAndValue(int i) {
		return AndValue[i];
	}

	public boolean getInputAndValue(int row, int column) {
		return this.InputAnd[row][column];
	}

	public int getInputs() {
		return this.inputs;
	}

	public Value getInputValue(int i) {
		return this.InputValue[i];
	}

	public int getOutputs() {
		return this.outputs;
	}

	public Value getOutputValue(int i) {
		return OutputValue[i];
	}

	public Value[] getOutputValues() {
		Value[] OutputValuecopy = new Value[getOutputs()];
		for (int i = getOutputs() - 1; i >= 0; i--)// reverse array
			OutputValuecopy[i] = OutputValue[OutputValue.length - i - 1];
		return OutputValuecopy;
	}

	public String getSizeString() {
		return this.getInputs() + "x" + this.getAnd() + "x" + this.getOutputs();
	}

	public Integer editWindow() {
		this.drawing = new PlaRomPanel(this);
		panel = new JScrollPane(this.drawing, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.setBorder(null);
		if (this.drawing.getPreferredSize().getWidth() >= (int) (screenSize.width * 0.75))
			panel.setPreferredSize(
					new Dimension((int) (screenSize.width * 0.75), (int) panel.getPreferredSize().getHeight()));
		if (this.drawing.getPreferredSize().getHeight() >= (int) (screenSize.height * 0.75))
			panel.setPreferredSize(
					new Dimension((int) panel.getPreferredSize().getWidth(), (int) (screenSize.height * 0.75)));
		return JOptionPane.showOptionDialog(null, panel, Strings.getter("ProgrammableGeneratorComponent").get(),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, this.options, null);
	}

	private void InitializeInputValue() {
		for (int i = 0; i < getInputs(); i++)
			InputValue[i] = Value.UNKNOWN;
	}

	public void setAndOutputValue(int row, int column, boolean b) {
		this.AndOutput[row][column] = b;
		// update all values
		setAndValue();
		setOutputValue();
	}

	private void setAndValue() {
		boolean thereisadot = false;
		for (int i = 0; i < getAnd(); i++) {
			AndValue[i] = Value.TRUE;
			for (int j = 0; j < getInputs() * 2; j++) {
				if (getInputAndValue(i, j)) {
					thereisadot = true;
					if (j % 2 == 0) { // not
						if (!getInputValue(j / 2).isFullyDefined())
							AndValue[i] = Value.ERROR;
						else if (getInputValue(j / 2) == Value.TRUE) {
							AndValue[i] = Value.FALSE;
							break;
						}
					} else if (j % 2 == 1) {
						if (!getInputValue((j - 1) / 2).isFullyDefined())
							AndValue[i] = Value.ERROR;
						else if (getInputValue((j - 1) / 2) == Value.FALSE) {
							AndValue[i] = Value.FALSE;
							break;
						}
					}
				}
			}
			if (!thereisadot)
				AndValue[i] = Value.ERROR;
			thereisadot = false;
		}
	}

	public void setInputAndValue(int row, int column, boolean b) {
		this.InputAnd[row][column] = b;
		// update all values
		setAndValue();
		setOutputValue();
	}

	public void setInputsValue(Value[] inputs) {
		int mininputs = getInputs() < inputs.length ? getInputs() : inputs.length;
		for (int i = 0; i < mininputs; i++)
			this.InputValue[i + getInputs() - mininputs] = inputs[i + inputs.length - mininputs];
		setAndValue();
		setOutputValue();
	}

	private void setOutputValue() {
		boolean thereisadot = false;
		for (int i = 0; i < getOutputs(); i++) {
			OutputValue[i] = Value.FALSE;
			for (int j = 0; j < getAnd(); j++) {
				if (getAndOutputValue(j, i)) {
					OutputValue[i] = OutputValue[i].or(getAndValue(j));
					thereisadot = true;
				}
			}
			if (!thereisadot)
				OutputValue[i] = Value.ERROR;
			thereisadot = false;
		}
	}

	public void updateSize(int inputs, int outputs, int and) {
		if (this.inputs != inputs || this.outputs != outputs || this.and != and) {
			int mininputs = getInputs() < inputs ? getInputs() : inputs;
			int minoutputs = getOutputs() < outputs ? getOutputs() : outputs;
			int minand = getAnd() < and ? getAnd() : and;
			this.inputs = inputs;
			this.outputs = outputs;
			this.and = and;
			boolean oldInputAnd[][] = Arrays.copyOf(InputAnd, InputAnd.length);
			boolean oldAndOutput[][] = Arrays.copyOf(AndOutput, AndOutput.length);
			InputAnd = new boolean[getAnd()][getInputs() * 2];
			AndOutput = new boolean[getAnd()][getOutputs()];
			InputValue = new Value[getInputs()];
			AndValue = new Value[getAnd()];
			OutputValue = new Value[getOutputs()];
			for (int i = 0; i < minand; i++) {
				for (int j = 0; j < mininputs * 2; j++) {
					InputAnd[i][j] = oldInputAnd[i][j];
				}
				for (int k = 0; k < minoutputs; k++) {
					AndOutput[i][k] = oldAndOutput[i][k];
				}
			}
			InitializeInputValue();
			setAndValue();
			setOutputValue();
		}
	}
}
