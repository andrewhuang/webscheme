/** StateStore

    @author Turadg
*/

package webscheme.wise;

import java.util.*;

import webscheme.*;
import webscheme.dom.*;

import org.apache.xmlrpc.*;

import sisc.data.*;

public class StateStore {

	XmlRpcClient client;
	Integer groupID;
	Integer pageID;
	DataModel dataModel;
	Map toSave;

	boolean inWise = false;

	public StateStore(DataModel dm, WiseContext wc) throws Exception {
		dataModel = dm;
		client = new XmlRpcClient(wc.getRpcUrl());
		groupID = wc.getGroupID();
		pageID = wc.getPageID();
		toSave = new HashMap();

		// FIX maybe use exception since this is an exceptional case?
		if (groupID != null && pageID != null)
			inWise = true;
	}

	public void include(SchemeString fieldId, SchemeString attr) {
		include(fieldId.asString(), attr.asString());
	}

	public void include(SchemeString fieldId, Pair attrs) {
		include(fieldId.asString(), SchemeUtil.toVector(attrs));
	}

	public void include(String fieldId, Vector attrs) {
		for (int i = 0; i < attrs.size(); i += 1) {
			Object a = attrs.elementAt(i);
			include(fieldId, a);
		}
	}

	public void include(String fieldId, Object attr) {
		FieldSpecifier fs;
		if ((fs = (FieldSpecifier) toSave.get(fieldId)) == null) {
			fs = new FieldSpecifier(fieldId);
			toSave.put(fieldId, fs);
		}

		String attrStr = null;
		if (attr instanceof String) {
			attrStr = (String) attr;
		} else if (attr instanceof SchemeString) {
			attrStr = ((SchemeString) attr).asString();
		} else {
			System.err.println(
				"unrecognized type " + attr + " (" + attr.getClass() + ")");
		}
		fs.includeAttribute(attrStr);
	}

	public void save() {
		if (!inWise)
			return;

		// hashtable is what XmlRpcClient expects
		Hashtable stateTable = new Hashtable();

		Iterator i = toSave.values().iterator();
		while (i.hasNext()) {
			FieldSpecifier fs = (FieldSpecifier) i.next();
			stateTable.put(fs.getId(), fs.getRestoreMethods(dataModel));
		}

		System.out.println("\n[state table]");
		System.out.println(stateTable);
		try {
			saveToServer(stateTable);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void saveToServer(Hashtable table) throws Exception {
		Vector params = new Vector();
		params.addElement(groupID);
		params.addElement(pageID);
		params.addElement(table);
		client.executeAsync("setStateData", params, null);
	}

	public void restore() {
		if (!inWise)
			return;

		try {
			Hashtable stateTable = loadFromServer();
			Iterator i = stateTable.keySet().iterator();
			while (i.hasNext()) {
				Object id = i.next();
				String restoreMethods = (String) stateTable.get(id);
				System.out.println("[" + id + " restore]");
				String restoreScript =
					"with (document.getElementById(\""
						+ id
						+ "\")) {\n"
						+ restoreMethods
						+ "}\n";
				dataModel.evalJavascript(restoreScript);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	Hashtable loadFromServer() throws Exception {
		Vector params = new Vector();
		params.addElement(groupID);
		params.addElement(pageID);
		Object reply = client.execute("getStateData", params);
		if (reply instanceof Hashtable)
			return (Hashtable) reply;
		else {
			// FIX investigate Apache XML-RPC's fault codes
			System.err.println("xmlrpc.getStateData returned bad data:");
			System.err.println(reply);
			return new Hashtable();
		}
	}

}
