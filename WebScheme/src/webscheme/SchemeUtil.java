/**
 * SchemeUtil
 * 
 * Some methods of widespread utility
 * 
 * @author Turadg
 */

package webscheme;

import java.util.Vector;

import sisc.data.Pair;
import sisc.data.Quantity;
import sisc.data.SchemeString;
import sisc.data.Value;
import sisc.util.Util;

public class SchemeUtil {

	public static Vector toVector(Pair p) {
		Value[] vals = Util.pairToValues(p);
		Vector vec = new Vector();
		for (int i = 0; i < vals.length; i += 1) {
			Value val = vals[i];
			Object o;
			if (val instanceof Quantity) {
				o = new Integer(((Quantity) val).intValue());
			} else if (val instanceof SchemeString) {
				o = ((SchemeString) val).asString();
			} else {
				System.err.println("Undefined Scheme conversion: " + val + " ("
						+ val.getClass() + ")");
				o = new String("UNKNOWN");
			}

			vec.addElement(o);
		}
		return vec;
	}

	public static Vector toVector(Object[] array) {
		Vector vec = new Vector();
		for (int i = 0; i < array.length; i += 1)
			vec.addElement(array[i]);
		return vec;
	}

}