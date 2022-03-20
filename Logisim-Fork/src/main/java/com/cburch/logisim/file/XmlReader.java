/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.cburch.draw.model.AbstractCanvasObject;
import com.cburch.logisim.LogisimVersion;
import com.cburch.logisim.Main;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.appear.AppearanceSvgReader;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeDefaultProvider;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.std.wiring.Pin;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.InputEventUtil;
import com.cburch.logisim.util.StringUtil;

class XmlReader {
	static class CircuitData {
		Element circuitElement;
		Circuit circuit;
		Map<Element, Component> knownComponents;
		List<AbstractCanvasObject> appearance;

		public CircuitData(Element circuitElement, Circuit circuit) {
			this.circuitElement = circuitElement;
			this.circuit = circuit;
		}
	}

	class ReadContext {
		LogisimFile file;
		HashMap<String, Library> libs = new HashMap<String, Library>();
		private ArrayList<String> messages;

		ReadContext(LogisimFile file) {
			this.file = file;
			this.messages = new ArrayList<String>();
		}

		void addError(String message, String context) {
			messages.add(message + " [" + context + "]");
		}

		void addErrors(XmlReaderException exception, String context) {
			for (String msg : exception.getMessages()) {
				messages.add(msg + " [" + context + "]");
			}
		}

		Library findLibrary(String lib_name) throws XmlReaderException {
			if (lib_name == null || lib_name.equals("")) {
				return file;
			}

			Library ret = libs.get(lib_name);
			if (ret == null) {
				throw new XmlReaderException(StringUtil.format(Strings.get("libMissingError"), lib_name));
			} else {
				return ret;
			}
		}

		void initAttributeSet(Element parentElt, AttributeSet attrs, AttributeDefaultProvider defaults)
				throws XmlReaderException {
			ArrayList<String> messages = null;

			HashMap<String, String> attrsDefined = new HashMap<String, String>();
			for (Element attrElt : XmlIterator.forChildElements(parentElt, "a")) {
				if (!attrElt.hasAttribute("name")) {
					if (messages == null)
						messages = new ArrayList<String>();
					messages.add(Strings.get("attrNameMissingError"));
				} else {
					String attrName = attrElt.getAttribute("name");
					String attrVal;
					if (attrElt.hasAttribute("val")) {
						attrVal = attrElt.getAttribute("val");
					} else {
						attrVal = attrElt.getTextContent();
					}
					attrsDefined.put(attrName, attrVal);
				}
			}

			if (attrs == null)
				return;

			LogisimVersion ver = Main.FILE_VERSION;
			boolean setDefaults = defaults != null && !defaults.isAllDefaultValues(attrs, ver);
			// We need to process this in order, and we have to refetch the
			// attribute list each time because it may change as we iterate
			// (as it will for a splitter).
			for (int i = 0; true; i++) {
				List<Attribute<?>> attrList = attrs.getAttributes();
				if (i >= attrList.size())
					break;
				Attribute<Object> attr = (Attribute<Object>) attrList.get(i);
				String attrName = attr.getName();
				String attrVal = attrsDefined.get(attrName);
				if (attrVal == null) {
					if (setDefaults) {
						Object val = defaults.getDefaultAttributeValue(attr, ver);
						if (val != null) {
							attrs.setValue(attr, val);
						}
					}
				} else {
					try {
						Object val = attr.parse(attrVal);
						attrs.setValue(attr, val);
					} catch (NumberFormatException e) {
						if (messages == null)
							messages = new ArrayList<String>();
						messages.add(StringUtil.format(Strings.get("attrValueInvalidError"), attrVal, attrName));
					}
				}
			}
			if (messages != null) {
				throw new XmlReaderException(messages);
			}
		}

		private void initMouseMappings(Element elt) {
			MouseMappings map = file.getOptions().getMouseMappings();
			for (Element sub_elt : XmlIterator.forChildElements(elt, "tool")) {
				Tool tool;
				try {
					tool = toTool(sub_elt);
				} catch (XmlReaderException e) {
					addErrors(e, "mapping");
					continue;
				}

				String mods_str = sub_elt.getAttribute("map");
				if (mods_str == null || mods_str.equals("")) {
					loader.showError(Strings.get("mappingMissingError"));
					continue;
				}
				int mods;
				try {
					mods = InputEventUtil.fromString(mods_str);
				} catch (NumberFormatException e) {
					loader.showError(StringUtil.format(Strings.get("mappingBadError"), mods_str));
					continue;
				}

				tool = tool.cloneTool();
				try {
					initAttributeSet(sub_elt, tool.getAttributeSet(), tool);
				} catch (XmlReaderException e) {
					addErrors(e, "mapping." + tool.getName());
				}

				map.setToolFor(mods, tool);
			}
		}

		private void initToolbarData(Element elt) {
			ToolbarData toolbar = file.getOptions().getToolbarData();
			for (Element sub_elt : XmlIterator.forChildElements(elt)) {
				if (sub_elt.getTagName().equals("sep")) {
					toolbar.addSeparator();
				} else if (sub_elt.getTagName().equals("tool")) {
					Tool tool;
					try {
						tool = toTool(sub_elt);
					} catch (XmlReaderException e) {
						addErrors(e, "toolbar");
						continue;
					}
					if (tool != null) {
						tool = tool.cloneTool();
						try {
							initAttributeSet(sub_elt, tool.getAttributeSet(), tool);
						} catch (XmlReaderException e) {
							addErrors(e, "toolbar." + tool.getName());
						}
						toolbar.addTool(tool);
					}
				}
			}
		}

		private void loadAppearance(Element appearElt, CircuitData circData, String context) {
			Map<Location, Instance> pins = new HashMap<Location, Instance>();
			for (Component comp : circData.knownComponents.values()) {
				if (comp.getFactory() == Pin.FACTORY) {
					Instance instance = Instance.getInstanceFor(comp);
					pins.put(comp.getLocation(), instance);
				}
			}

			List<AbstractCanvasObject> shapes = new ArrayList<AbstractCanvasObject>();
			for (Element sub : XmlIterator.forChildElements(appearElt)) {
				try {
					AbstractCanvasObject m = AppearanceSvgReader.createShape(sub, pins);
					if (m == null) {
						addError(Strings.get("fileAppearanceNotFound", sub.getTagName()),
								context + "." + sub.getTagName());
					} else {
						shapes.add(m);
					}
				} catch (RuntimeException e) {
					addError(Strings.get("fileAppearanceError", sub.getTagName()), context + "." + sub.getTagName());
				}
			}
			if (!shapes.isEmpty()) {
				if (circData.appearance == null) {
					circData.appearance = shapes;
				} else {
					circData.appearance.addAll(shapes);
				}
			}
		}

		private Map<Element, Component> loadKnownComponents(Element elt) {
			Map<Element, Component> known = new HashMap<Element, Component>();
			for (Element sub : XmlIterator.forChildElements(elt, "comp")) {
				try {
					Component comp = XmlCircuitReader.getComponent(sub, this);
					known.put(sub, comp);
				} catch (XmlReaderException e) {
				}
			}
			return known;
		}

		private Library toLibrary(Element elt) {
			if (!elt.hasAttribute("name")) {
				loader.showError(Strings.get("libNameMissingError"));
				return null;
			}
			if (!elt.hasAttribute("desc")) {
				loader.showError(Strings.get("libDescMissingError"));
				return null;
			}
			String name = elt.getAttribute("name");
			String desc = elt.getAttribute("desc");
			Library ret = loader.loadLibrary(desc);
			if (ret == null)
				return null;
			libs.put(name, ret);
			for (Element sub_elt : XmlIterator.forChildElements(elt, "tool")) {
				if (!sub_elt.hasAttribute("name")) {
					loader.showError(Strings.get("toolNameMissingError"));
				} else {
					String tool_str = sub_elt.getAttribute("name");
					Tool tool = ret.getTool(tool_str);
					if (tool != null) {
						try {
							initAttributeSet(sub_elt, tool.getAttributeSet(), tool);
						} catch (XmlReaderException e) {
							addErrors(e, "lib." + name + "." + tool_str);
						}
					}
				}
			}
			return ret;
		}

		private void toLogisimFile(Element elt) {
			// determine the version producing this file
			String versionString = elt.getAttribute("source");
			if (versionString.equals("")) {
				Main.FILE_VERSION = Main.VERSION;
			} else {
				Main.FILE_VERSION = LogisimVersion.parse(versionString);
			}

			// first, load the sublibraries
			for (Element o : XmlIterator.forChildElements(elt, "lib")) {
				Library lib = toLibrary(o);
				if (lib != null)
					file.addLibrary(lib);
			}

			// second, create the circuits - empty for now
			List<CircuitData> circuitsData = new ArrayList<CircuitData>();
			for (Element circElt : XmlIterator.forChildElements(elt, "circuit")) {
				String name = circElt.getAttribute("name");
				if (name == null || name.equals("")) {
					addError(Strings.get("circNameMissingError"), "C??");
				}
				CircuitData circData = new CircuitData(circElt, new Circuit(name));
				file.addCircuit(circData.circuit);
				circData.knownComponents = loadKnownComponents(circElt);
				for (Element appearElt : XmlIterator.forChildElements(circElt, "appear")) {
					loadAppearance(appearElt, circData, name + ".appear");
				}
				circuitsData.add(circData);
			}

			// third, process the other child elements
			for (Element sub_elt : XmlIterator.forChildElements(elt)) {
				String name = sub_elt.getTagName();
				if (name.equals("circuit") || name.equals("lib")) {
					; // Nothing to do: Done earlier.
				} else if (name.equals("options")) {
					try {
						initAttributeSet(sub_elt, file.getOptions().getAttributeSet(), null);
					} catch (XmlReaderException e) {
						addErrors(e, "options");
					}
				} else if (name.equals("mappings")) {
					initMouseMappings(sub_elt);
				} else if (name.equals("toolbar")) {
					initToolbarData(sub_elt);
				} else if (name.equals("main")) {
					String main = sub_elt.getAttribute("name");
					Circuit circ = file.getCircuit(main);
					if (circ != null) {
						file.setMainCircuit(circ);
					}
				} else if (name.equals("message")) {
					file.addMessage(sub_elt.getAttribute("value"));
				}
			}

			// fourth, execute a transaction that initializes all the circuits
			XmlCircuitReader builder;
			builder = new XmlCircuitReader(this, circuitsData);
			builder.execute(null);
		}

		Tool toTool(Element elt) throws XmlReaderException {
			Library lib = findLibrary(elt.getAttribute("lib"));
			String name = elt.getAttribute("name");
			if (name == null || name.equals("")) {
				throw new XmlReaderException(Strings.get("toolNameMissing"));
			}
			Tool tool = lib.getTool(name);
			if (tool == null) {
				throw new XmlReaderException(Strings.get("toolNotFound"));
			}
			return tool;
		}
	}

	private static void findLibraryUses(ArrayList<Element> dest, String label, Iterable<Element> candidates) {
		for (Element elt : candidates) {
			String lib = elt.getAttribute("lib");
			if (lib.equals(label)) {
				dest.add(elt);
			}
		}
	}

	private LibraryLoader loader;

	XmlReader(Loader loader) {
		this.loader = loader;
	}

	private void addToLabelMap(HashMap<String, String> labelMap, String srcLabel, String dstLabel, String toolNames) {
		if (srcLabel != null && dstLabel != null) {
			for (String tool : toolNames.split(";")) {
				labelMap.put(srcLabel + ":" + tool, dstLabel);
			}
		}
	}

	private void considerRepairs(Document doc, Element root) {
		LogisimVersion version = LogisimVersion.parse(root.getAttribute("source"));
		// so you won't lose moving whith mouse wheel click if you open old files
		if (version.compareTo(LogisimVersion.get(2, 12, 0, 0)) < 0) {
			for (Element mappings : XmlIterator.forChildElements(root, "mappings")) {
				for (Element elt : XmlIterator.forChildElements(mappings, "tool")) {
					if (elt.getAttribute("map").equals("Button2"))
						elt.setAttribute("name", "Poke Tool");
				}
			}
		}
		if (version.compareTo(LogisimVersion.get(2, 3, 0)) < 0) {
			// This file was saved before an Edit tool existed. Most likely
			// we should replace the Select and Wiring tools in the toolbar
			// with the Edit tool instead.
			for (Element toolbar : XmlIterator.forChildElements(root, "toolbar")) {
				Element wiring = null;
				Element select = null;
				Element edit = null;
				for (Element elt : XmlIterator.forChildElements(toolbar, "tool")) {
					String eltName = elt.getAttribute("name");
					if (eltName != null && !eltName.equals("")) {
						if (eltName.equals("Select Tool"))
							select = elt;
						if (eltName.equals("Wiring Tool"))
							wiring = elt;
						if (eltName.equals("Edit Tool"))
							edit = elt;
					}
				}
				if (select != null && wiring != null && edit == null) {
					select.setAttribute("name", "Edit Tool");
					toolbar.removeChild(wiring);
				}
			}
		}
		for (Element circElt : XmlIterator.forChildElements(root, "circuit")) {
			if (version.compareTo(LogisimVersion.get(2, 6, 3)) < 0) {
				for (Element attrElt : XmlIterator.forChildElements(circElt, "a")) {
					String name = attrElt.getAttribute("name");
					if (name != null && name.startsWith("label")) {
						attrElt.setAttribute("name", "c" + name);
					}
				}

				repairForWiringLibrary(doc, root);
				repairForLegacyLibrary(doc, root);
			}
			// compatibility for new gates attributes
			if (version.compareTo(LogisimVersion.get(2, 7, 2, 255)) <= 0) {
				for (Element compElt : XmlIterator.forChildElements(circElt, "comp")) {
					if (compElt.getAttribute("name") != null) {
						if (compElt.getAttribute("name").equals("AND Gate")
								|| compElt.getAttribute("name").equals("NAND Gate")
								|| compElt.getAttribute("name").equals("OR Gate")
								|| compElt.getAttribute("name").equals("NOR Gate")
								|| compElt.getAttribute("name").equals("XOR Gate")
								|| compElt.getAttribute("name").equals("XNOR Gate")) {
							boolean defaultsizeattribute = true;
							boolean defaultinputsattribute = true;
							// check if the attribute is already defined
							for (Element attrElt : XmlIterator.forChildElements(compElt, "a")) {
								if (attrElt.getAttribute("name").equals("size"))
									defaultsizeattribute = false;
								if (attrElt.getAttribute("name").equals("inputs"))
									defaultinputsattribute = false;
							}
							// if these attributes are not defined, set them to match with old default value
							if (defaultsizeattribute) {
								Element sizeattribute = doc.createElement("a");
								sizeattribute.setAttribute("name", "size");
								sizeattribute.setAttribute("val", "50");
								compElt.appendChild(sizeattribute);
							}
							if (defaultinputsattribute) {
								Element inputsattribute = doc.createElement("a");
								inputsattribute.setAttribute("name", "inputs");
								inputsattribute.setAttribute("val", "5");
								compElt.appendChild(inputsattribute);
							}
						} else if (compElt.getAttribute("name").equals("NOT Gate")) { // the not hasn't the inputs
																						// attribute
							boolean defaultsizeattribute = true;
							for (Element attrElt : XmlIterator.forChildElements(compElt, "a")) {
								if (attrElt.getAttribute("name").startsWith("size"))
									defaultsizeattribute = false;
							}
							if (defaultsizeattribute) {
								Element sizeattribute = doc.createElement("a");
								sizeattribute.setAttribute("name", "size");
								sizeattribute.setAttribute("val", "30");
								compElt.appendChild(sizeattribute);
							}
						}
					}
				}
			}
			// compatibility for new RAM and ROM layout
			if (version.compareTo(LogisimVersion.get(2, 16, 1, 4)) < 0) {
				for (Element compElt : XmlIterator.forChildElements(circElt, "comp")) {
					if (compElt.getAttribute("name") != null) {
						if (compElt.getAttribute("name").equals("RAM")) {
							boolean simpleMode = true;
							// check if the attribute is already defined
							for (Element attrElt : XmlIterator.forChildElements(compElt, "a")) {
								if (attrElt.getAttribute("name").equals("simpleMode"))
									simpleMode = false;
							}
							// if these attributes are not defined, set them to match with old default value
							if (simpleMode) {
								Element sizeattribute = doc.createElement("a");
								sizeattribute.setAttribute("name", "simpleMode");
								sizeattribute.setAttribute("val", "false");
								compElt.appendChild(sizeattribute);
							}
						}
					}
				}
			}
			// compatibility for new ff layout
			if (version.compareTo(LogisimVersion.get(2, 10, 0, 0)) < 0) {
				for (Element compElt : XmlIterator.forChildElements(circElt, "comp")) {
					if (compElt.getAttribute("name") != null && compElt.getAttribute("name").endsWith("Flip-Flop")) {
						// add the new attributes and set them to look like old flip-flops
						Element prclrpos = doc.createElement("a");
						prclrpos.setAttribute("name", "Pre/Clr Positions");
						prclrpos.setAttribute("val", "LEGACY");
						compElt.appendChild(prclrpos);
						Element enable = doc.createElement("a");
						enable.setAttribute("name", "enable");
						enable.setAttribute("val", "true");
						compElt.appendChild(enable);
						Element newlayout = doc.createElement("a");
						newlayout.setAttribute("name", "NewFFLayout");
						newlayout.setAttribute("val", "false");
						compElt.appendChild(newlayout);
					}
				}
			}
			// compatibility for splitter new attributes value
			if (version.compareTo(LogisimVersion.get(2, 11, 0, 0)) < 0) {
				for (Element compElt : XmlIterator.forChildElements(circElt, "comp")) {
					if (compElt.getAttribute("name") != null && compElt.getAttribute("name").equals("Splitter")) {
						boolean defaultfanoutattribute = true;
						boolean defaultincomingattribute = true;
						// check if the attribute is already defined
						for (Element attrElt : XmlIterator.forChildElements(compElt, "a")) {
							if (attrElt.getAttribute("name").equals("fanout"))
								defaultfanoutattribute = false;
							if (attrElt.getAttribute("name").equals("incoming"))
								defaultincomingattribute = false;
						}
						if (defaultfanoutattribute) {
							Element sizeattribute = doc.createElement("a");
							sizeattribute.setAttribute("name", "fanout");
							sizeattribute.setAttribute("val", "2");
							compElt.appendChild(sizeattribute);
						}
						if (defaultincomingattribute) {
							Element inputsattribute = doc.createElement("a");
							inputsattribute.setAttribute("name", "incoming");
							inputsattribute.setAttribute("val", "2");
							compElt.appendChild(inputsattribute);
						}
					}

				}
			}
			// compatibility for tunnel new direction value
			if (version.compareTo(LogisimVersion.get(2, 11, 1, 1)) < 0) {
				for (Element compElt : XmlIterator.forChildElements(circElt, "comp")) {
					if (compElt.getAttribute("name") != null && compElt.getAttribute("name").equals("Tunnel")) {
						boolean defaultfacingattribute = true;
						// check if the attribute is already defined
						for (Element attrElt : XmlIterator.forChildElements(compElt, "a")) {
							if (attrElt.getAttribute("name").equals("facing"))
								defaultfacingattribute = false;
						}
						if (defaultfacingattribute) {
							Element sizeattribute = doc.createElement("a");
							sizeattribute.setAttribute("name", "facing");
							sizeattribute.setAttribute("val", "west");
							compElt.appendChild(sizeattribute);
						}
					}

				}
			}
			// compatibility message for gate inputs moved (only wide 4 inputs)
			if (version.compareTo(LogisimVersion.get(2, 11, 1, 2)) < 0) {
				for (Element compElt : XmlIterator.forChildElements(circElt, "comp")) {
					if (compElt.getAttribute("name").equals("AND Gate")
							|| compElt.getAttribute("name").equals("NAND Gate")
							|| compElt.getAttribute("name").equals("OR Gate")
							|| compElt.getAttribute("name").equals("NOR Gate")
							|| compElt.getAttribute("name").equals("XOR Gate")
							|| compElt.getAttribute("name").equals("XNOR Gate")) {
						boolean hasfourinputs = false;
						boolean iswide = false;
						// check if is wide and has 4 inputs
						for (Element attrElt : XmlIterator.forChildElements(compElt, "a")) {
							if (attrElt.getAttribute("name").equals("size") && attrElt.getAttribute("val").equals("70"))
								hasfourinputs = true;
							else if (attrElt.getAttribute("name").equals("inputs")
									&& attrElt.getAttribute("val").equals("4"))
								iswide = true;
						}
						if (hasfourinputs && iswide) {
							JOptionPane.showMessageDialog(null,
									"You could have to edit the position of all the gates that have 4 inputs and wide attribute\ndue to a bug in the input positions of the original Logisim",
									"Compatibility problem", JOptionPane.WARNING_MESSAGE);
						}
					}

				}
			}
			if (version.compareTo(LogisimVersion.get(2, 11, 5, 0)) < 0) {
				for (Element compElt : XmlIterator.forChildElements(circElt, "comp")) {
					// switch has changed its behaviour in this version, so we'll replace it with a
					// dipswitch with 1 switch
					if (compElt.getAttribute("name").equals("Switch")) {
						String facing = "east";
						String[] coords = compElt.getAttribute("loc")
								.substring(1, compElt.getAttribute("loc").length() - 1).split(",");
						int locx = Integer.valueOf(coords[0]), locy = Integer.valueOf(coords[1]);
						for (Element attrElt : XmlIterator.forChildElements(compElt, "a")) {
							if (attrElt.getAttribute("name").equals("facing"))
								facing = attrElt.getAttribute("val");
						}
						// set the new coordinates to palce dipswitch port in the same place
						if (facing.equals("east") || facing.equals("west"))
							locy -= 10;
						else
							locx -= 10;
						compElt.setAttribute("loc", "(" + locx + "," + locy + ")");

						compElt.removeAttribute("name");
						compElt.setAttribute("name", "DipSwitch");
						Element nswitches = doc.createElement("a");
						nswitches.setAttribute("name", "NSwitches");
						nswitches.setAttribute("val", "1");
						compElt.appendChild(nswitches);
					}
					// set sel active on high level as it was before
					else if (compElt.getAttribute("name").equals("RAM") || compElt.getAttribute("name").equals("ROM")
							|| compElt.getAttribute("name").equals("PlaRom")) {
						Element SelectAttribute = doc.createElement("a");
						SelectAttribute.setAttribute("name", "Select");
						SelectAttribute.setAttribute("val", "high");
						compElt.appendChild(SelectAttribute);
					}
				}

			}
			// switch attribute cause a bug
			if (version.compareTo(LogisimVersion.get(2, 14, 0, 0)) < 0) {
				for (Element compElt : XmlIterator.forChildElements(circElt, "comp")) {
					if (compElt.getAttribute("name") != null && compElt.getAttribute("name").endsWith("Flip-Flop")) {
						for (Element attrElt : XmlIterator.forChildElements(compElt, "a")) {
							if (attrElt.getAttribute("name").equals("NegatePresetClear")) {
								if (attrElt.getAttribute("val").equals("true"))
									attrElt.setAttribute("val", "false");
								else
									attrElt.setAttribute("val", "true");
							}
						}
					}
				}
			}
			// new counter behavior
			if (version.compareTo(LogisimVersion.get(2, 14, 3, 0)) < 0) {
				for (Element compElt : XmlIterator.forChildElements(circElt, "comp")) {
					if (compElt.getAttribute("name") != null && compElt.getAttribute("name").equals("Counter")) {
						Element BehavorAttribute = doc.createElement("a");
						BehavorAttribute.setAttribute("name", "behavior");
						BehavorAttribute.setAttribute("val", "old");
						compElt.appendChild(BehavorAttribute);
					}
				}
			}
		}
	}

	private Document loadXmlFrom(InputStream is) throws SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
		}
		return builder.parse(is);
	}

	LogisimFile readLibrary(InputStream is) throws IOException, SAXException {
		Document doc = loadXmlFrom(is);
		Element elt = doc.getDocumentElement();
		considerRepairs(doc, elt);
		LogisimFile file = new LogisimFile((Loader) loader);
		ReadContext context = new ReadContext(file);
		context.toLogisimFile(elt);
		if (file.getCircuitCount() == 0) {
			file.addCircuit(new Circuit("main"));
		}
		if (context.messages.size() > 0) {
			StringBuilder all = new StringBuilder();
			for (String msg : context.messages) {
				all.append(msg);
				all.append("\n");
			}
			loader.showError(all.substring(0, all.length() - 1));
		}
		return file;
	}

	private void relocateTools(Element src, Element dest, HashMap<String, String> labelMap) {
		if (src == null || src == dest)
			return;
		String srcLabel = src.getAttribute("name");
		if (srcLabel == null)
			return;

		ArrayList<Element> toRemove = new ArrayList<Element>();
		for (Element elt : XmlIterator.forChildElements(src, "tool")) {
			String name = elt.getAttribute("name");
			if (name != null && labelMap.containsKey(srcLabel + ":" + name)) {
				toRemove.add(elt);
			}
		}
		for (Element elt : toRemove) {
			src.removeChild(elt);
			if (dest != null) {
				dest.appendChild(elt);
			}
		}
	}

	private void repairForLegacyLibrary(Document doc, Element root) {
		Element legacyElt = null;
		String legacyLabel = null;
		for (Element libElt : XmlIterator.forChildElements(root, "lib")) {
			String desc = libElt.getAttribute("desc");
			String label = libElt.getAttribute("name");
			if (desc != null && desc.equals("#Legacy")) {
				legacyElt = libElt;
				legacyLabel = label;
			}
		}

		if (legacyElt != null) {
			root.removeChild(legacyElt);

			ArrayList<Element> toRemove = new ArrayList<Element>();
			findLibraryUses(toRemove, legacyLabel, XmlIterator.forDescendantElements(root, "comp"));
			boolean componentsRemoved = !toRemove.isEmpty();
			findLibraryUses(toRemove, legacyLabel, XmlIterator.forDescendantElements(root, "tool"));
			for (Element elt : toRemove) {
				elt.getParentNode().removeChild(elt);
			}
			if (componentsRemoved) {
				String error = "Some components have been deleted;" + " the Legacy library is no longer supported.";
				Element elt = doc.createElement("message");
				elt.setAttribute("value", error);
				root.appendChild(elt);
			}
		}
	}

	private void repairForWiringLibrary(Document doc, Element root) {
		Element oldBaseElt = null;
		String oldBaseLabel = null;
		Element gatesElt = null;
		String gatesLabel = null;
		int maxLabel = -1;
		Element firstLibElt = null;
		Element lastLibElt = null;
		for (Element libElt : XmlIterator.forChildElements(root, "lib")) {
			String desc = libElt.getAttribute("desc");
			String label = libElt.getAttribute("name");
			if (desc == null) {
				// skip these tests
			} else if (desc.equals("#Base")) {
				oldBaseElt = libElt;
				oldBaseLabel = label;
			} else if (desc.equals("#Wiring")) {
				// Wiring library already in file. This shouldn't happen, but if
				// somehow it does, we don't want to add it again.
				return;
			} else if (desc.equals("#Gates")) {
				gatesElt = libElt;
				gatesLabel = label;
			}

			if (firstLibElt == null)
				firstLibElt = libElt;
			lastLibElt = libElt;
			try {
				if (label != null) {
					int thisLabel = Integer.parseInt(label);
					if (thisLabel > maxLabel)
						maxLabel = thisLabel;
				}
			} catch (NumberFormatException e) {
			}
		}

		Element wiringElt;
		String wiringLabel;
		Element newBaseElt = null;
		String newBaseLabel = null;
		if (oldBaseElt != null) {
			wiringLabel = oldBaseLabel;
			wiringElt = oldBaseElt;
			wiringElt.setAttribute("desc", "#Wiring");

			newBaseLabel = "" + (maxLabel + 1);
			newBaseElt = doc.createElement("lib");
			newBaseElt.setAttribute("desc", "#Base");
			newBaseElt.setAttribute("name", newBaseLabel);
			root.insertBefore(newBaseElt, lastLibElt.getNextSibling());
		} else {
			wiringLabel = "" + (maxLabel + 1);
			wiringElt = doc.createElement("lib");
			wiringElt.setAttribute("desc", "#Wiring");
			wiringElt.setAttribute("name", wiringLabel);
			if (lastLibElt != null)
				root.insertBefore(wiringElt, lastLibElt.getNextSibling());
		}

		HashMap<String, String> labelMap = new HashMap<String, String>();
		addToLabelMap(labelMap, oldBaseLabel, newBaseLabel,
				"Poke Tool;" + "Edit Tool;Select Tool;Wiring Tool;Text Tool;Menu Tool;Text");
		addToLabelMap(labelMap, oldBaseLabel, wiringLabel,
				"Splitter;Pin;" + "Probe;Tunnel;Clock;Pull Resistor;Bit Extender");
		addToLabelMap(labelMap, gatesLabel, wiringLabel, "Constant");
		relocateTools(oldBaseElt, newBaseElt, labelMap);
		relocateTools(oldBaseElt, wiringElt, labelMap);
		relocateTools(gatesElt, wiringElt, labelMap);
		updateFromLabelMap(XmlIterator.forDescendantElements(root, "comp"), labelMap);
		updateFromLabelMap(XmlIterator.forDescendantElements(root, "tool"), labelMap);
	}

	private void updateFromLabelMap(Iterable<Element> elts, HashMap<String, String> labelMap) {
		for (Element elt : elts) {
			String oldLib = elt.getAttribute("lib");
			String name = elt.getAttribute("name");
			if (oldLib != null && name != null) {
				String newLib = labelMap.get(oldLib + ":" + name);
				if (newLib != null) {
					elt.setAttribute("lib", newLib);
				}
			}
		}
	}
}
