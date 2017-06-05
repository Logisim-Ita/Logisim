/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.start;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.border.EmptyBorder;

import com.cburch.logisim.gui.generic.LFrame;

public class UpdateScreen extends JWindow {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5725103703896809899L;

	boolean inClose = false; // for avoiding mutual recursion

	long startTime = System.currentTimeMillis();

	JFrame f;
	JButton cancel = new JButton(Strings.get("startupCancelButton"));
	JProgressBar progress;
	JLabel label;
	int MAX = 0;

	public UpdateScreen() {

		ClassLoader loader = LFrame.class.getClassLoader();
		URL url = loader.getResource("resources/logisim/img/update-icon.png");
		ImageIcon icon = null;
		if (url != null)
			icon = new ImageIcon(url);

		f = new JFrame(Strings.get("Update"));
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setResizable(false);
		if (icon != null)
			f.setIconImage(icon.getImage());
	}

	public void Downloading(int n) {
		MAX = n;

		JPanel labelPanel = new JPanel(new FlowLayout());
		label = new JLabel("Downloading...");
		label.setFont(new Font("Sans Serif", Font.PLAIN, 14));
		label.setBorder(new EmptyBorder(8, 0, 8, 0));
		labelPanel.add(label);
		labelPanel.setBackground(Color.WHITE);

		JPanel progressPanel = new JPanel(new BorderLayout());
		progressPanel.setBorder(new EmptyBorder(0, 10, 8, 10));
		progress = new JProgressBar(0, MAX);
		progress.setStringPainted(true);
		progressPanel.add(progress);
		progressPanel.setBackground(Color.WHITE);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(cancel);
		buttonPanel.setBackground(Color.WHITE);

		JPanel updatePanel = new JPanel(new BorderLayout());
		updatePanel.add(labelPanel, BorderLayout.NORTH);
		updatePanel.add(progressPanel, BorderLayout.CENTER);
		updatePanel.add(buttonPanel, BorderLayout.SOUTH);

		f.getContentPane().add(updatePanel, BorderLayout.CENTER);
	}
	
	
	public void Message(String s) {
		label = new JLabel(s);
		label.setFont(new Font("Sans Serif", Font.PLAIN, 13));
		JPanel j = new JPanel(new GridBagLayout());
		j.add(label);
		j.setPreferredSize(new Dimension(305,105));
		f.getContentPane().add(j, BorderLayout.CENTER);
	}

	public void Clear() {
		f.invalidate();
		f.getContentPane().removeAll();
	}

	public void Repaint() {
		f.validate();
		f.getContentPane().repaint();
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
			f.pack();
			Dimension dim = getToolkit().getScreenSize();
			int x = (int) (dim.getWidth() - f.getWidth()) / 2;
			int y = (int) (dim.getHeight() - f.getHeight()) / 2;
			f.setLocation(x, y);
		}
		f.setVisible(value);
	}

	public void close() {
		if (inClose)
			return;
		inClose = true;
		f.setVisible(false);
		inClose = false;
	}

	public void addActionListener(ActionListener listener) {
		cancel.addActionListener(listener);
	}
}
