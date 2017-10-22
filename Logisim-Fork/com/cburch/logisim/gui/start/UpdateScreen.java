/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.start;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.generic.LFrame;
import com.cburch.logisim.util.TableLayout;

public class UpdateScreen extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5725103703896809899L;

	private JButton cancel = new JButton(Strings.get("startupCancelButton"));
	private JProgressBar progress;
	private int MAX;

	public UpdateScreen() {
		URL url = LFrame.class.getClassLoader().getResource("resources/logisim/img/update-icon.png");
		ImageIcon icon = null;
		if (url != null)
			icon = new ImageIcon(url);
		getContentPane().setLayout(new BorderLayout());
		setTitle(Strings.get("Update"));
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);
		setSize(new Dimension(350, 140));
		setLocationRelativeTo(null);
		if (icon != null)
			setIconImage(icon.getImage());
	}

	public void addActionListener(ActionListener listener) {
		cancel.addActionListener(listener);
	}

	public void Clear() {
		getContentPane().invalidate();
		getContentPane().removeAll();
	}

	public void Downloading(int n) {
		MAX = n;

		JPanel labelPanel = new JPanel(new FlowLayout());
		JLabel label = new JLabel("Downloading...");
		label.setFont(new Font("Sans Serif", Font.PLAIN, 15));
		labelPanel.add(label);
		labelPanel.setBorder(new EmptyBorder(4, 0, 4, 0));
		labelPanel.setBackground(Color.WHITE);

		JPanel progressPanel = new JPanel(new BorderLayout());
		progress = new JProgressBar(0, MAX);
		progress.setStringPainted(true);
		progress.setForeground(Value.TRUE_COLOR);
		progressPanel.add(progress);
		progressPanel.setBorder(new EmptyBorder(0, 10, 4, 10));
		progressPanel.setBackground(Color.WHITE);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(cancel);
		buttonPanel.setBackground(Color.WHITE);

		JPanel updatePanel = new JPanel(new BorderLayout());
		updatePanel.add(labelPanel, BorderLayout.NORTH);
		updatePanel.add(progressPanel, BorderLayout.CENTER);
		updatePanel.add(buttonPanel, BorderLayout.SOUTH);

		this.getContentPane().add(updatePanel, BorderLayout.CENTER);
	}

	public void Message(String s) {
		JLabel label = new JLabel(s);
		label.setFont(new Font("Sans Serif", Font.PLAIN, 15));
		JPanel labelPanel = new JPanel(new TableLayout(1));
		labelPanel.setBackground(Color.WHITE);
		labelPanel.add(label);
		this.getContentPane().add(labelPanel, BorderLayout.CENTER);
	}

	public void Repaint() {
		this.getContentPane().revalidate();
	}

	public void setProgress(int n) {
		double nToMB = n / 1048576.0;
		double MAXToMB = MAX / 1048576.0;
		progress.setString(String.format("%.2fMB / %.2fMB", nToMB, MAXToMB));
		progress.setValue(n);
	}
}