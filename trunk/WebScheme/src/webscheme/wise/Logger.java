/** FieldSpecifier

    @author Turadg
*/

package webscheme.wise;

import java.util.*;

import org.apache.xmlrpc.*;

import sisc.data.*;

public class Logger {

	XmlRpcClient client;
	Integer groupID;
	Integer pageID;

	public Logger(WiseContext wc) throws Exception {
		client = new XmlRpcClient(wc.getRpcUrl());
		groupID = wc.getGroupID();
		pageID = wc.getPageID();
	}

	/**
	   queueKey is relative to current WebScheme step
	*/
	public void push(SchemeString queueKey, SchemeString value)
		throws Exception {
		Vector params = new Vector();
		params.addElement(groupID);
		params.addElement(pageID);
		params.addElement(queueKey.asString());
		params.addElement(value.asString());
		client.executeAsync("pushValue", params, null);
	}

	/**
	   queueKey is relative to logKey
	   logKey is relative to projectID
	*/
	public void push(
		SchemeString logKey,
		SchemeString queueKey,
		SchemeString value)
		throws Exception {
		Vector params = new Vector();
		params.addElement(groupID);
		params.addElement(logKey.asString());
		params.addElement(queueKey.asString());
		params.addElement(value.asString());
		client.executeAsync("pushValue", params, null);
	}

	public SchemeVector getQueue(SchemeString logKey, SchemeString queueKey)
		throws Exception {
		Vector params = new Vector();
		params.addElement(groupID);
		params.addElement(logKey.asString());
		params.addElement(queueKey.asString());
		Vector queue = (Vector) client.execute("getQueueValues", params);
		for (int i = 0; i < queue.size(); i += 1) {
			Object el = queue.elementAt(i);
			System.out.println(
				"queue[" + i + "]: " + el + " (" + el.getClass() + ")");
		}
		return new SchemeVector(0);
	}

	/**
	   queueKey is relative to current WebScheme step
	*/
	public SchemeVector getQueue(SchemeString queueKey) throws Exception {
		Vector params = new Vector();
		params.addElement(groupID);
		params.addElement(pageID);
		params.addElement(queueKey.asString());
		Vector queue = (Vector) client.execute("getQueueValues", params);
		for (int i = 0; i < queue.size(); i += 1) {
			Object el = queue.elementAt(i);
			System.out.println(
				"queue[" + i + "]: " + el + " (" + el.getClass() + ")");
		}
		return new SchemeVector(0);
	}

}
