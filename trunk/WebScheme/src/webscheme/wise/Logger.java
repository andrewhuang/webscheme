/**
 *  
 * @author Turadg
 * @version $Id$
 */

package webscheme.wise;

import java.net.URL;
import java.util.*;
import org.apache.xmlrpc.*;
import sisc.data.*;

public class Logger {

    XmlRpcClient client;

    Integer groupID;

    Integer pageID;

    AsyncCallback acb = new AsyncCallback() {

        public void handleResult(Object result, URL url, String method) {
            // TODO Auto-generated method stub
            System.out.println(method + " returned " + result);
        }

        public void handleError(Exception exception, URL url, String method) {
            // TODO Auto-generated method stub
            exception.printStackTrace();
        }

    };

    public Logger(WiseContext wc) throws Exception {
        client = new XmlRpcClient(wc.getRpcUrl());
        groupID = wc.getGroupID();
        pageID = wc.getPageID();

        // initialize the XmlRpc client during startup,
        // while we have permission
        Object obj = client.execute("noop", new Vector(0));
        System.out.println("synchronous xmlrpc returned " + obj);
        client.executeAsync("noop", new Vector(0), acb);
    }

    /**
     * queueKey is relative to current WebScheme step
     */
    public void push(String queueKey, String value) {
        try {
            Vector params = new Vector(4);
            params.addElement(groupID);
            params.addElement(pageID);
            params.addElement(queueKey);
            params.addElement(value);
            System.out.print("about to executeAsync pushValue");
            System.out.println("  " + params);
            client.executeAsync("pushValue", params, acb);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * queueKey is relative to logKey logKey is relative to projectID
     */
    public void push(String logKey, String queueKey, String value)
            throws Exception {
        Vector params = new Vector(4);
        params.addElement(groupID);
        params.addElement(logKey);
        params.addElement(queueKey);
        params.addElement(value);
        System.out.print("about to executeAsync pushValue");
        System.out.println("  " + params);
        client.executeAsync("pushValue", params, acb);
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
            System.out.println("queue[" + i + "]: " + el + " (" + el.getClass()
                    + ")");
        }
        return new SchemeVector(0);
    }

    /**
     * queueKey is relative to current WebScheme step
     */
    public SchemeVector getQueue(SchemeString queueKey) throws Exception {
        Vector params = new Vector();
        params.addElement(groupID);
        params.addElement(pageID);
        params.addElement(queueKey.asString());
        Vector queue = (Vector) client.execute("getQueueValues", params);
        for (int i = 0; i < queue.size(); i += 1) {
            Object el = queue.elementAt(i);
            System.out.println("queue[" + i + "]: " + el + " (" + el.getClass()
                    + ")");
        }
        return new SchemeVector(0);
    }

}