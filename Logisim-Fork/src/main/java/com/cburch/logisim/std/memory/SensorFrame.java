package com.cburch.logisim.std.memory;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultTreeCellEditor.EditorContainer;
import javax.swing.event.ChangeEvent;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.gui.generic.LFrame;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.proj.Project;

public class SensorFrame extends LFrame {
	Sensor factory;
	CircuitState circState;
	Instance instance;
	String ss;
	private class MyListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			Object src = event.getSource();
			if (src == deleteRow) {
				int[] selectedRows = table.getSelectedRows();
				for (int i = selectedRows.length-1; i >=0; i--) {
					int row = selectedRows[i];
					((DefaultTableModel)table.getModel()).removeRow(row);
				}
			}
			 else if (src == addRow) {
				 Vector<Vector<Object>> records = new Vector<Vector<Object>>();
				 Vector<Object> single = new Vector<Object>();
				 single.add(table.getRowCount()+1);
				 single.add(Float.parseFloat("0.0"));
				 
				 records.add(single);
				 ((DefaultTableModel)table.getModel()).insertRow(table.getSelectedRow()+1,single);
			}
		}
	}
	private MyListener myListener = new MyListener();
	

	JButton deleteRow = new JButton(Strings.get("deleteRow"));
	JButton addRow = new JButton(Strings.get("addRow"));
	JButton save = new JButton(Strings.get("save"));
	
	JTable table;
	
	private Vector<Vector<Object>> getFloats(String str) {
		Vector<Vector<Object>> records = new Vector<Vector<Object>>();
	 	Vector<Object> single = null;
		try {
			String [] strRecords=str.replaceAll("\\[|\\]", "").split(", ");
	        for (int i =0; i < strRecords.length; i++) {
	        	single=new Vector<Object>();
                single.add(i+1);
                single.add(Float.parseFloat(strRecords[i]));
                records.add(single);
            }
		} catch (Exception e) {
			single=new Vector<Object>();
			single.add(1);
            single.add(Float.parseFloat("5.0"));
            records.add(single);
			if (!str.isEmpty()) {
				JOptionPane.showMessageDialog(null, e.getMessage(), Strings.get("ramLoadErrorTitle"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
		return records;
	}
	
	
	public SensorFrame(Project proj, Sensor factory, CircuitState circState, Instance instance) {
		deleteRow.addActionListener(myListener);
		addRow.addActionListener(myListener);
		
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		LogisimMenuBar menubar = new LogisimMenuBar(this, proj);
		setJMenuBar(menubar);
		
		JPanel panel = new JPanel();
	    Vector<String> columnNames = new Vector<String>();
	    columnNames.addElement("ID");
	    columnNames.addElement("Value");
	    
	    DefaultTableModel tableModel = new DefaultTableModel(getFloats(factory.getValuesAttribute(circState.getInstanceState(instance))),columnNames) {
	    	
	    	@Override
	    	public Class<?> getColumnClass(int columnIndex) { 
			    return columnIndex == 1 ? Float.class : super.getColumnClass(columnIndex);
			}
	    	
	    	@Override
	    	public boolean isCellEditable(int rowIndex, int columnIndex)
	    	{
	    	    return columnIndex==0 ? false : true ;
	    	}
	    };
	    
	    
		table=new JTable(tableModel);
		table.getModel().addTableModelListener(new TableModelListener() {
	      public void tableChanged(TableModelEvent e) {
	    	  DefaultTableModel tableEvent = (DefaultTableModel) e.getSource();
	    	  //Vector<Vector> tableEventResult = tableEvent.getDataVector();
	    	  Float[] list=new Float[table.getRowCount()];
	    	  	for (int i=0; i<table.getRowCount();i++) {
	    	  		Object tableValue = table.getValueAt(i, 1);
			    	if (tableValue instanceof String)
			    		list[i]= Float.parseFloat((String) table.getValueAt(i, 1));
			    	else
			    		list[i]= (Float) table.getValueAt(i, 1);
				}
			    factory.setValuesAttribute(circState.getInstanceState(instance), Arrays.toString(list));

	      }
	    });
		JScrollPane scrollPane=new JScrollPane(table);
	    Dimension d = new Dimension(400,200);
	    scrollPane.setPreferredSize(d);
	    panel.add(scrollPane);
	    panel.add(deleteRow);
	    panel.add(addRow);
		add(panel);
		setSize(500,300); 
		setLocationRelativeTo(null);
		
		
	}
}
