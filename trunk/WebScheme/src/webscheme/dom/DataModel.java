/**
    DataModel

	Re-representation of the web page document object model

  	@author Turadg
*/

package webscheme.dom;

import java.applet.*;
import java.util.*;
import netscape.javascript.*;

/**
 * @author Turadg
 *
 */
/**
 * @author Turadg
 *
 */
/**
 * @author Turadg
 *
 */
public class DataModel {

	// put a wrapper on this to translate JSExceptions to Java exceptions
	final JSObject win;
	final JSObject doc;
	final AppletContext appletContext;

	public DataModel(Applet applet) {
		win = JSObject.getWindow(applet);
		doc = (JSObject) win.eval("document");
		System.out.println("win: " + win);
		System.out.println("doc: " + doc);
		appletContext = applet.getAppletContext();
	}

	/** Find the Javascript object on the web page identified by <code>id</code>
	 * 
	 * @param id
	 * @return JSObject identified by <code>id</id>
	 * @throws NoSuchElementException
	 */
	JSObject getJSObjectById(String id) throws NoSuchElementException {
		//		System.out.print("getJSObjectById(" + id + ") ");
		JSObject element = null;
		try {
			Object[] args = { id };
			Object obj = doc.call("getElementById", args);
			//			System.out.println("got object by id: " + obj);
			element = (JSObject) obj;
		} catch (Exception ex) {
			System.err.println(
				"getJSObjectById(\"" + id + "\") second attempt");
			try {
				Object[] args = { id };
				Object obj = doc.call("getElementById", args);
				//				System.out.println("got object by id: " + obj);
				element = (JSObject) obj;
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
		}

		if (element == null) // still
			throw new NoSuchElementException(id);
		else
			return element;
	}

	public SmartJSO getSmartJsoById(String id) throws NoSuchElementException {
		return new SmartJSO(getJSObjectById(id));
	}

	public void setString(String id, String value)
		throws NoSuchElementException {
		SmartJSO sjso = getSmartJsoById(id);
		sjso.setString(value);
	}

	public String getString(String id) throws NoSuchElementException {
		SmartJSO sjso = getSmartJsoById(id);
		return sjso.getString();
	}

	// FIX bring table features into SmartJSO
	public String getCell(String tableID, Integer rownum, Integer cellnum) {
		return getCell(tableID, rownum.intValue(), cellnum.intValue());
	}

	public String getCell(String tableID, int rownum, int cellnum) {
		Object[] args = { tableID, new Integer(rownum), new Integer(cellnum)};
		Object cell = win.call("getCellElement", args);
		Object[] args2 = { cell };
		Object value = win.call("getCellValue", args);
		System.out.println("getCell returning " + value);
		return value.toString();
	}

	public String[] getRow(String tableID, int rownum)
		throws NoSuchElementException {
		System.out.println("getRow( " + tableID + ", " + rownum + ")");
		Object[] args = { tableID, new Integer(rownum)};
		String[] cells = (String[]) win.call("getRowElementAsJavaArray", args);
		System.out.println("getRow returning: " + cells);
		return cells;
	}

	public void addRow(String tableID, String[] vals)
		throws NoSuchElementException {
		System.out.println(
			"addRow( " + tableID + ", " + Arrays.asList(vals) + ")");
		Object[] args = { tableID, vals };
		Object result;
		try {
			result = win.call("addRowValues", args);
			System.out.println("addRowValues returned: " + result);
			if (result instanceof Exception) {
				throw (RuntimeException) result;
			}
		} catch (JSException ex) {
			System.err.println("addRow caught a JSException: " + ex);
			System.err.println("  ex.getMessage(): " + ex.getMessage());
			System.err.println("  ex.printStackTrace(): ");
			ex.printStackTrace();
		}
	}

	// for development, maybe ditch later
	public Object evalJavascript(String code) {
		System.out.println("DataModel.evalJavascript( " + code + " )");
		return doc.eval(code);
	}

}
