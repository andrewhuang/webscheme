/**
   SmartJSO
  
   @author Turadg
*/

package webscheme.dom;

import java.util.*;
import netscape.javascript.*;

public class SmartJSO {

	static final String OBJECT_NAME = "OBJECT";
	static final String SPAN_NAME = "SPAN";
	static final String INPUT_NAME = "INPUT";
	static final String TEXTAREA_NAME = "TEXTAREA";

	static final String RADIO_INPUT = "radio";

	static final String VALUE_ATTR = "VALUE";
	static final String SRC_ATTR = "SRC";
	static final String DISABLED_ATTR = "DISABLED";

	static final String ERROR_VAL = "*error*";

	final JSObject jsObject;
	final String nodeName;

	public SmartJSO(JSObject jso) {
		jsObject = jso;
		nodeName = jsObject.getMember("nodeName").toString().intern();
	}

	public void setString(String newval) {
		JSObject firstChild = null; // used in SPAN

		try {
			if (nodeName == OBJECT_NAME) {
				Object[] args = { newval };
				jsObject.call("setString", args);
				return;
			}

			if (nodeName == SPAN_NAME) {
				Object[] args = { newval };
				firstChild = (JSObject) jsObject.getMember("firstChild");
				if (firstChild == null) {
					// FIX create the text child node
					System.err.println("FIX: no child for " + jsObject);
				}
				firstChild.setMember("nodeValue", newval);
				return;
			}

			if (nodeName == INPUT_NAME) {
				String inputType =
					jsObject.getMember("type").toString().intern();
				if (inputType.equals(RADIO_INPUT)) {
					JSObject radioGroup = groupOfRadioInput(jsObject);
					int groupLength =
						((Double) radioGroup.getMember("length")).intValue();
					for (int i = 0; i < groupLength; i += 1) {
						JSObject radioInGroup =
							(JSObject) radioGroup.getSlot(i);
						String radiosVal =
							(String) radioInGroup.getMember("value");
						if (radiosVal.equals(newval))
							radioInGroup.setMember("checked", Boolean.TRUE);
						else
							radioInGroup.removeMember("checked");
					}
					return;
				} else {
					// default INPUT control
					jsObject.setMember("value", newval);
					return;
				}
			}

			if (nodeName == TEXTAREA_NAME) {
				jsObject.setMember("value", newval);
				return;
			}

		} catch (Exception ex) {
			System.err.println("setString error on " + jsObject);
			ex.printStackTrace();
			return;
		}

		// no matches
		throw new IllegalStateException(
			"setString does not support nodeName " + nodeName);
	}

	public String getString() {
		JSObject firstChild = null; // used in SPAN

		try {
			if (nodeName == OBJECT_NAME) {
				Object[] args = {
				};
				return (String) jsObject.call("getString", args);
			}

			if (nodeName == SPAN_NAME) {
				firstChild = (JSObject) jsObject.getMember("firstChild");
				return firstChild.getMember("nodeValue").toString();
			}

			if (nodeName == INPUT_NAME) {
				String inputType =
					jsObject.getMember("type").toString().intern();
				if (inputType.equals(RADIO_INPUT)) {
					JSObject radioGroup = groupOfRadioInput(jsObject);
					int groupLength =
						((Double) radioGroup.getMember("length")).intValue();
					for (int i = 0; i < groupLength; i += 1) {
						JSObject radioInGroup =
							(JSObject) radioGroup.getSlot(i);
						Boolean checked =
							(Boolean) radioInGroup.getMember("checked");
						if (checked.booleanValue())
							return (String) radioInGroup.getMember("value");
					}
					return "*noselection*";
				} else {
					// default INPUT control
					return jsObject.getMember("value").toString();
				}
			}

			if (nodeName == TEXTAREA_NAME) {
				return jsObject.getMember("value").toString();
			}

		} catch (Exception ex) {
			System.err.println("getString error on " + jsObject);
			ex.printStackTrace();
			return ERROR_VAL;
		}

		// no matches
		throw new IllegalStateException(
			"getString does not support nodeName " + nodeName);
	}

	/**
	   Return Javascript code that can be called on this JSObject
	   to return the attribute to its present state
	*/
	public String getRestoreMethod(String attr) {
		attr = attr.intern();

		if (attr == SRC_ATTR) {
			return "src=\"" + jsObject.getMember("src") + "\"";
		}

		if (attr == VALUE_ATTR) {
			String value = getString();
			if (nodeName == OBJECT_NAME)
				return "setString('" + escapeNewlines(value) + "')";
			// FIX implement SPAN_NAME
			// FIX implement INPUT_NAME (including radio and checkbox)
			if (nodeName == TEXTAREA_NAME)
				return "value='" + value + "'";
		}

		if (attr == DISABLED_ATTR) {
			if (nodeName == OBJECT_NAME) {
				// FIX add setDisabled() method to EmbeddedEditorApplet
			}
			if (nodeName == SPAN_NAME) {
				return ""; // spans are never "enabled" anyway
			}
			if (nodeName == INPUT_NAME) {
				// FIX handle radio and checkboxes
				return "disabled=" + jsObject.getMember("disabled");
			}
			if (nodeName == TEXTAREA_NAME)
				return "disabled=" + jsObject.getMember("disabled");
		}

		// inert
		return "unimplemented=" + attr;
	}

	static String escapeNewlines(String s) {
		StringBuffer sb = new StringBuffer();
		StringTokenizer st = new StringTokenizer(s, "\n", true);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.equals("\n"))
				sb.append("\\n");
			else
				sb.append(token);
		}
		return sb.toString();
	}

	static JSObject groupOfRadioInput(JSObject anInput) {
		JSObject form = (JSObject) anInput.getMember("form");
		String groupName = (String) anInput.getMember("name");
		JSObject radioGroup = (JSObject) form.getMember(groupName);
		return radioGroup;
	}

}
