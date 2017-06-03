/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.start;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.border.EmptyBorder;

public class UpdateScreen extends JWindow {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5725103703896809899L;

	boolean inClose = false; // for avoiding mutual recursion

	long startTime = System.currentTimeMillis();

	JButton cancel = new JButton(Strings.get("startupCancelButton"));
	JProgressBar progress;
	JLabel label;
	int MAX = 0;

	public UpdateScreen(int n) {
		MAX = n;

		JPanel labelPanel = new JPanel(new FlowLayout());
		label = new JLabel("Download...");
		label.setFont(new Font("Sans Serif", Font.PLAIN, 12));
		label.setBorder(new EmptyBorder(8, 0, 8, 0));
		labelPanel.add(label);

		JPanel progressPanel = new JPanel(new BorderLayout());
		progressPanel.setPreferredSize(new Dimension(300, 30));
		progressPanel.setBorder(new EmptyBorder(0, 10, 8, 10));
		progress = new JProgressBar(0, MAX);
		progress.setStringPainted(true);
		progressPanel.add(progress);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(cancel);

		JPanel updatePanel = new JPanel(new BorderLayout());
		updatePanel.add(labelPanel, BorderLayout.NORTH);
		updatePanel.add(progressPanel, BorderLayout.CENTER);
		updatePanel.add(buttonPanel, BorderLayout.SOUTH);
		updatePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

		labelPanel.setBackground(Color.WHITE);
		progressPanel.setBackground(Color.WHITE);
		buttonPanel.setBackground(Color.WHITE);
		setBackground(Color.WHITE);
		setContentPane(updatePanel);
	}

	public void setProgress(int n) {
		double nToMB = n / 1048576.0;
		double MAXToMB = MAX / 1048576.0;
		progress.setString(String.format("%.2fMB / %.2fMB", nToMB, MAXToMB));
		progress.setValue(n);
	}

	@Override
	public void setVisible(boolean value) {
		if (value) {
			pack();
			Dimension dim = getToolkit().getScreenSize();
			int x = (int) (dim.getWidth() - getWidth()) / 2;
			int y = (int) (dim.getHeight() - getHeight()) / 2;
			setLocation(x, y);
		}
		super.setVisible(value);
	}

	public void close() {
		if (inClose)
			return;
		inClose = true;
		setVisible(false);
		inClose = false;
	}

	public void addActionListener(ActionListener listener) {
		cancel.addActionListener(listener);
	}
}
