/** 
 * @author Turadg
 * @version $Id$
 */

package webscheme.wise;

import java.applet.*;
import java.net.*;

import netscape.javascript.*;

public class WiseContext {

    final static String XMLRPC_PATH = "/modules/webscheme/xmlrpc.php";

    final Integer groupID;

    final Integer pageID;

    URL rpcUrl; // can't be final b/c try{} may not succeed

    public WiseContext(Applet applet) {
        JSObject win = JSObject.getWindow(applet);
        groupID = intOf("wise_groupID", win);
        System.out.println("WiseContext.groupID:  " + groupID);
        pageID = intOf("wise_pageID", win);
        System.out.println("WiseContext.pageID:  " + pageID);
        try {
            rpcUrl = new URL(applet.getCodeBase(), XMLRPC_PATH);
            System.out.println("WiseContext.rpcUrl:  " + rpcUrl);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }

    Integer intOf(String element, JSObject win) {
        Number num = (Number) win.eval(element);
        return (num == null) ? null : new Integer(num.intValue());
    }

    public URL getRpcUrl() {
        return rpcUrl;
    }

    public Integer getGroupID() {
        return groupID;
    }

    public Integer getPageID() {
        return pageID;
    }

}