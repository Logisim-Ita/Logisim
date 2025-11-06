/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractSpinnerModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.prefs.AppPreferences;

public class ZoomControl extends JPanel {
	private class GridIcon extends JComponent implements MouseListener, PropertyChangeListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3780961019640263926L;
		boolean state = true;

		public GridIcon() {
			addMouseListener(this);
			setPreferredSize(new Dimension(15, 15));
			setToolTipText("Show grid");
			setFocusable(true);
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			return Strings.get("zoomShowGrid");
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			model.setShowGrid(!state);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		protected void paintComponent(Graphics g) {
			if (AppPreferences.ANTI_ALIASING.getBoolean()) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}
			int width = getWidth();
			int height = getHeight();
			g.setColor(state ? Color.black : getBackground().darker());
			int dim = (Math.min(width, height) - 4) / 3 * 3 + 1;
			int xoff = (width - dim) / 2;
			int yoff = (height - dim) / 2;
			for (int x = 0; x < dim; x += 3) {
				for (int y = 0; y < dim; y += 3) {
					g.drawLine(x + xoff, y + yoff, x + xoff, y + yoff);
				}
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			update();
		}

		private void update() {
			boolean grid = model.getShowGrid();
			if (grid != state) {
				state = grid;
				repaint();
			}
		}
	}

	public class SpinnerModel extends AbstractSpinnerModel implements PropertyChangeListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2283318939208459104L;

		private double checkZoom(double zoom) {
			if (zoom < Frame.MIN_ZOOM)
				return Frame.MIN_ZOOM;
			else if (zoom > Frame.MAX_ZOOM)
				return Frame.MAX_ZOOM;
			return zoom;
		}

		@Override
		public Object getNextValue() {
			double zoom = Math.round((model.getZoomFactor() * 100.0 + Frame.STEP_ZOOM) / Frame.STEP_ZOOM)
					* Frame.STEP_ZOOM;
			return toString(checkZoom(zoom));
		}

		@Override
		public Object getPreviousValue() {
			double zoom = Math.round((model.getZoomFactor() * 100.0 - Frame.STEP_ZOOM) / Frame.STEP_ZOOM)
					* Frame.STEP_ZOOM;
			return toString(checkZoom(zoom));
		}

		@Override
		public Object getValue() {
			double zoom = model.getZoomFactor();
			return toString(zoom * 100.0);
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			fireStateChanged();
		}

		@Override
		public void setValue(Object value) {
			if (value instanceof String) {
				String s = (String) value;
				if (s.endsWith("%"))
					s = s.substring(6, s.length() - 1);
				s = s.trim();
				try {
					double zoom = Double.parseDouble(s);
					model.setZoomFactor(checkZoom(zoom) / 100.0);
				} catch (NumberFormatException e) {
				}
			}
		}

		private String toString(double factor) {
			if (factor > 10) {
				return "Zoom: " + (int) (factor + 0.5) + "%";
			} else if (factor > 0.1) {
				return "Zoom: " + (int) (factor * 100 + 0.5) / 100.0 + "%";
			} else {
				return "Zoom: " + factor + "%";
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7823904346559070108L;

	public SpinnerModel spinnerModel;
	private ZoomModel model;
	private JSpinner spinner;
	private GridIcon grid;

	public ZoomControl(ZoomModel model) {
		super(new BorderLayout());
		this.model = model;

		spinnerModel = new SpinnerModel();
		spinner = new JSpinner();
		spinner.setModel(spinnerModel);
		this.add(spinner, BorderLayout.CENTER);

		grid = new GridIcon();
		this.add(grid, BorderLayout.EAST);
		grid.update();

		model.addPropertyChangeListener(ZoomModel.SHOW_GRID, grid);
		model.addPropertyChangeListener(ZoomModel.ZOOM, spinnerModel);
	}

	public void setZoomModel(ZoomModel value) {
		ZoomModel oldModel = model;
		if (oldModel != value) {
			if (oldModel != null) {
				oldModel.removePropertyChangeListener(ZoomModel.SHOW_GRID, grid);
				oldModel.removePropertyChangeListener(ZoomModel.ZOOM, spinnerModel);
			}
			model = value;
			spinnerModel = new SpinnerModel();
			spinner.setModel(spinnerModel);
			grid.update();
			if (value != null) {
				value.addPropertyChangeListener(ZoomModel.SHOW_GRID, grid);
				value.addPropertyChangeListener(ZoomModel.ZOOM, spinnerModel);
			}
		}
	}
}
