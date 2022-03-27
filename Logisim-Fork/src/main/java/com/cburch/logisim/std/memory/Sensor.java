/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.hex.HexFile;
import com.cburch.logisim.gui.hex.HexFrame;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstanceLogger;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.memory.Rom.ContentsCell;
import com.cburch.logisim.tools.MenuExtender;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringGetter;
import com.cburch.logisim.util.StringUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Sensor extends InstanceFactory {

	public static class Logger extends InstanceLogger {
		
		@Override
		public String getLogName(InstanceState state, Object option) {
			String ret = state.getAttributeValue(StdAttr.LABEL);
			return ret != null && !ret.equals("") ? ret : null;
		}

		@Override
		public Value getLogValue(InstanceState state, Object option) {
			BitWidth dataWidth = state.getAttributeValue(StdAttr.WIDTH);
			if (dataWidth == null)
				dataWidth = BitWidth.create(0);
			StateData data = (StateData) state.getData();
			if (data == null)
				return Value.createKnown(dataWidth, 0);
			return Value.createKnown(dataWidth, data.value);
		}
	}

	private static class StateData extends ClockState implements InstanceData {
	
		private int value;
		private int [] values;
		private int record;
		
		// Previous State
		
		BitWidth prevoiusDataWidth=null;
		
		

		public StateData(InstanceState state) {
			reset(state);
		}
		
		
		
		private int[] calculateValues(String str,int bitN) {
			
			int[] arrayValues;
			try {
				String [] strRecords=str.replaceAll("\\[|\\]", "").split(", ");
				Float[] records=new Float[strRecords.length];
		        for (int i =0; i < strRecords.length; i++)
		            records[i] = Float.parseFloat(strRecords[i]);
		        float gap=Collections.max(Arrays.asList(records))-Collections.min(Arrays.asList(records));
		        float jump=gap/(float)Math.pow(2, bitN);
		        arrayValues= new int[strRecords.length];
		        for (int i =0; i < strRecords.length; i++) {
		        	int value=(int)((records[i]-Collections.min(Arrays.asList(records)))/jump);
		        	if (value>=(int)Math.pow(2, bitN))
		        		value=(int)(Math.pow(2, bitN)-1);
		        	arrayValues[i] = value;
		        }
			} catch (Exception e) {
				arrayValues= new int[] {0};
				if (!str.isEmpty()) {
					JOptionPane.showMessageDialog(null, e.getMessage(), Strings.get("ramLoadErrorTitle"),
							JOptionPane.ERROR_MESSAGE);
				}
				
				
			}
			return arrayValues;
		}

		void reset(InstanceState state) {
			BitWidth dataWidth = state.getAttributeValue(StdAttr.WIDTH);
			values= calculateValues(state.getAttributeValue(ATTR_VALUES),Integer.parseInt(dataWidth.toString()));
			prevoiusDataWidth=dataWidth;
			
			record=0;
			value = values[0];
			record++;
		}
		
		void step() {
			if (record==values.length){
				record=0;
			}
			value=values[record];
			record++;
		}

	}
	private static final Attribute<String> ATTR_VALUES = Attributes.forString("values", Strings.getter("Values"));
	private static final int OUT = 0;
	private static final int CK = 1;
	private static final int RST = 2;
	File selectedFile=null;
	public Sensor() {
		super("Sensor", Strings.getter("sensorComponent"));
		setAttributes(
				new Attribute[] { StdAttr.WIDTH, StdAttr.EDGE_TRIGGER, StdAttr.LABEL, StdAttr.LABEL_FONT,
						StdAttr.ATTR_LABEL_COLOR,ATTR_VALUES},
				new Object[] { BitWidth.create(8), StdAttr.TRIG_RISING, "",
						StdAttr.DEFAULT_LABEL_FONT, Color.BLACK,""});
		setKeyConfigurator(new BitWidthConfigurator(StdAttr.WIDTH));
		setOffsetBounds(Bounds.create(-30, -20, 30, 40));
		setIconName("random.gif");
		setInstanceLogger(Logger.class);
		Port[] ps = new Port[3];
		ps[OUT] = new Port(0, 0, Port.OUTPUT, StdAttr.WIDTH);
		ps[CK] = new Port(-30, -10, Port.INPUT, 1);
		ps[RST] = new Port(-20, 20, Port.INPUT, 1);
		ps[OUT].setToolTip(Strings.getter("Output"));
		ps[CK].setToolTip(Strings.getter("Clock"));
		ps[RST].setToolTip(Strings.getter("Reset"));
		setPorts(ps);
	}

	protected void setValuesAttribute(InstanceState state,String values) {
		state.getAttributeSet().setValue(ATTR_VALUES, values);
		StateData data = (StateData) state.getData();
		data.reset(state);
		
	}
	
	protected String getValuesAttribute(InstanceState state) {
		return state.getAttributeSet().getValue(ATTR_VALUES);
		
	}
	
	@Override
	protected void configureNewInstance(Instance instance) {
		Bounds bds = instance.getBounds();
		instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT, StdAttr.ATTR_LABEL_COLOR,
				bds.getX() + bds.getWidth() / 2, bds.getY() - 3, GraphicsUtil.H_CENTER, GraphicsUtil.V_BOTTOM);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		Bounds bds = painter.getBounds();
		StateData state = (StateData) painter.getData();
		BitWidth widthVal = painter.getAttributeValue(StdAttr.WIDTH);
		int width = widthVal == null ? 8 : widthVal.getWidth();

		// draw boundary, label
		painter.drawRoundBounds(Color.WHITE);
		painter.drawLabel();

		// draw input and output ports
		painter.drawPort(OUT, "Q", Direction.WEST);
		painter.drawPort(RST);
		painter.drawClock(CK, Direction.EAST);

		// draw contents
		if (painter.getShowState()) {
			int val = state == null ? 0 : state.value;
			String str = StringUtil.toHexString(width, val);
			if (str.length() <= 4) {
				GraphicsUtil.drawText(g, str, bds.getX() + 15, bds.getY() + 4, GraphicsUtil.H_CENTER,
						GraphicsUtil.V_TOP);
			} else {
				int split = str.length() - 4;
				GraphicsUtil.drawText(g, str.substring(0, split), bds.getX() + 15, bds.getY() + 3,
						GraphicsUtil.H_CENTER, GraphicsUtil.V_TOP);
				GraphicsUtil.drawText(g, str.substring(split), bds.getX() + 15, bds.getY() + 15, GraphicsUtil.H_CENTER,
						GraphicsUtil.V_TOP);
			}
		}
	}
	@Override
	protected Object getInstanceFeature(Instance instance, Object key) {
		if (key == MenuExtender.class)
			return new SensorMenu(this,instance);
		return super.getInstanceFeature(instance, key);
	}

	@Override
	public void propagate(InstanceState state) {
		StateData data = (StateData) state.getData();
		BitWidth dataWidth = state.getAttributeValue(StdAttr.WIDTH);
		if (data == null) {
			data = new StateData(state);
			state.setData(data);
		}
		
		Object triggerType = state.getAttributeValue(StdAttr.EDGE_TRIGGER);
		boolean triggered = data.updateClock(state.getPort(CK), triggerType);
		// Action
		if (state.getPort(RST) == Value.TRUE) {
			//Reset
			data.reset(state);
		}else if (triggered) {
			if (data.prevoiusDataWidth!=dataWidth) {
				// Reset if dataWidth change
				data.reset(state);
			}else
				data.step();
		}
		state.setPort(OUT, Value.createKnown(dataWidth, data.value), 4);
	}
}