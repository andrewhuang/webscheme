/** FieldSpecifier

    @author Turadg
*/

package webscheme.wise;

import java.util.*;

import webscheme.dom.*;

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
			SmartJSO sjso = dm.getSmartJsoById(id);
			String restoreMethod = sjso.getRestoreMethod(attr);

			script.append(restoreMethod);
			script.append(";\n");
		}
		return script.toString();
	}

}
