/**
 * ObjectElement
 * 
 * Methods that an OBJECT must implement to be used by DataHandler and SmartJSO
 * 
 * @author Turadg
 */

package webscheme.dom;

public interface ObjectElement {

	/**
	 * @param attr
	 * @return
	 */
	public String getRestoreMethod(String attr);

	public String getString();

	public void setString(String s);

}