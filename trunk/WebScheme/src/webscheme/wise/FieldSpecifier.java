/**
 * FieldSpecifier
 * 
 * @author Turadg
 */

package webscheme.wise;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import webscheme.dom.DataModel;
import webscheme.dom.ObjectElement;

class FieldSpecifier {

	String id;

	Set attrs;

	protected FieldSpecifier(String fieldId) {
		id = fieldId;
		attrs = new HashSet();
	}

	protected String getId() {
		return id;
	}

	protected void includeAttribute(String name) {
		attrs.add(name);
	}

	protected String getRestoreMethods(DataModel dm) {
		StringBuffer script = new StringBuffer();
		Iterator i = attrs.iterator();
		while (i.hasNext()) {
			String attr = (String) i.next();
			ObjectElement oe = dm.getObjectElementById(id);
			String restoreMethod = oe.getRestoreMethod(attr);

			script.append(restoreMethod);
			script.append(";\n");
		}
		return script.toString();
	}

}