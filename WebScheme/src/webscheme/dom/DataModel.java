package webscheme.dom;

import java.applet.Applet;
import java.applet.AppletContext;
import java.util.Arrays;
import java.util.NoSuchElementException;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

/**
 * DataModel
 * 
 * Re-representation of the web page document object model
 * 
 * @author Turadg
 */
public class DataModel {

	final AppletContext appletContext;

	// FIX maybe I should get a new Window reference on each call
	// http://forums.devshed.com/t130973/s.html
	final JSObject doc;

	final JSObject win;

	public DataModel(Applet applet) {
		win = JSObject.getWindow(applet);
		if (win == null)
			throw new RuntimeException(
					"LiveConnect provided null Window element");

		doc = (JSObject) win.eval("document");
		if (doc == null)
			throw new RuntimeException(
					"LiveConnect provided null Document element");

		appletContext = applet.getAppletContext();
	}

	public void addRow(String tableID, String[] vals)
			throws NoSuchElementException {
		System.out.println("addRow( " + tableID + ", " + Arrays.asList(vals)
				+ ")");
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

	public String getCell(String tableID, int rownum, int cellnum) {
		Object[] args = { tableID, new Integer(rownum), new Integer(cellnum) };
		Object cell = win.call("getCellElement", args);
		Object[] args2 = { cell };
		Object value = win.call("getCellValue", args);
		System.out.println("getCell returning " + value);
		return value.toString();
	}

	// FIX bring table features into SmartJSO
	public String getCell(String tableID, Integer rownum, Integer cellnum) {
		return getCell(tableID, rownum.intValue(), cellnum.intValue());
	}

	/**
	 * Find the Javascript object on the web page identified by <code>id</code>
	 * 
	 * @param id
	 * @return JSObject identified by <code>id</id>
	 * @throws NoSuchElementException
	 */
	JSObject getJSObjectById(String id) throws NoSuchElementException {
		JSObject element = null;

		try {
			Object[] args = { id };
			// FIX the following fails in Firefix 0.8
			Object obj = doc.call("getElementById", args);
			element = (JSObject) obj;
		} catch (Exception ex) {
			System.err
					.println("getJSObjectById(\"" + id + "\") second attempt");
			try {
				Object[] args = { id };
				Object obj = doc.call("getElementById", args);
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

	public ObjectElement getObjectElementById(String id)
			throws NoSuchElementException {
		// if it's an applet that implements ObjectElement, we can talk to it
		// directly
		Applet applet = appletContext.getApplet(id);
		if (applet != null && applet instanceof ObjectElement)
			return (ObjectElement) applet;

		// otherwise fall back on Javascript
		return new SmartJSO(getJSObjectById(id));
	}

	public String[] getRow(String tableID, int rownum)
			throws NoSuchElementException {
		System.out.println("getRow( " + tableID + ", " + rownum + ")");
		Object[] args = { tableID, new Integer(rownum) };
		String[] cells = (String[]) win.call("getRowElementAsJavaArray", args);
		System.out.println("getRow returning: " + cells);
		return cells;
	}

	public String getString(String id) throws NoSuchElementException {
		System.out.println("DataModel.getString( " + id + ")");
		ObjectElement oe = getObjectElementById(id);
		System.out.println("..SmartJSO: " + oe);
		return oe.getString();
	}

	public void setString(String id, String value)
			throws NoSuchElementException {
		ObjectElement oe = getObjectElementById(id);
		oe.setString(value);
	}

}