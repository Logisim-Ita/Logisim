/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.plugin;



import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.cburch.logisim.plugin.Json;
import com.cburch.logisim.plugin.PluginUtils;




class StoreOptions extends OptionsPanel {
	private static final long serialVersionUID = -6484141255871285744L;
	
	
	private class MyListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			Object src = event.getSource();
			if (src == pluginDownloadButton) {
				int[] selectedRows = table.getSelectedRows();
				for (int i = selectedRows.length-1; i >=0; i--) {
					int row = selectedRows[i];
					String pluginName =table.getValueAt(row, 0).toString()+"-"+table.getValueAt(row, 1)+"."+table.getValueAt(row, 2);
					PluginUtils.downloadPlugin(Json.searchJson(pluginName)[1]);
					PluginUtils.openLink(table.getValueAt(row, 0).toString());
					((DefaultTableModel)table.getModel()).removeRow(row);
				}
				PluginUtils.requestRestart();
			}
		}
	}

	private MyListener myListener = new MyListener();
	
	private JButton pluginDownloadButton = new JButton(Strings.get("downloadPlugIn"));
	
	private JTable table;
	
	public StoreOptions(PluginFrame frame) {
		super(frame);
		pluginDownloadButton.addActionListener(myListener);
		
		
		Vector<Vector<String>> rec = PluginUtils.getStoreTable();
		Vector<String> columnNames = new Vector<String>();
	    columnNames.addElement(Strings.get("tableName"));
	    columnNames.addElement(Strings.get("tableVersion"));
	    columnNames.addElement(Strings.get("tableType"));
	      table = new JTable(rec, columnNames){
	    	  private static final long serialVersionUID = -9199865726557261219L;
	    	  public boolean isCellEditable(int row, int column) {
	    		  return false; 
	    	  }
	      };
	      
	      
	      
	      JPanel panel = new JPanel();
	      
	      if(table.getRowCount()==0) {
	    	  pluginDownloadButton.setEnabled(false);
	      }
	      panel.add(pluginDownloadButton);
	      table.setFont(new Font("Arial", Font.CENTER_BASELINE, 15));
	      table.setRowHeight(25);
          JScrollPane x=new JScrollPane(table);
          Dimension d = new Dimension(400,200);
          x.setPreferredSize(d);
          table.getColumnModel().getColumn(0).setPreferredWidth(300);
          add(panel);
          add(x);
          
          
	      
	      
	}

	
	@Override
	public String getHelpText() {
		return null;
	}

	@Override
	public String getTitle() {
		return Strings.get("storeTitle");
	}
	
}
