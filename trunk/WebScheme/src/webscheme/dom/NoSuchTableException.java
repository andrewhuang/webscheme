/**
   NoSuchTableException
  
   @author Turadg
*/

package webscheme.dom;

public class NoSuchTableException extends RuntimeException {

	/**
	 * Constructs a <code>NoSuchTableException</code>, saving the  
	 * name of the template <tt>name</tt> for later retrieval by the 
	 * <tt>getMessage</tt> method.
	 *
	 * @param   name   the named template
	 */
	public NoSuchTableException(String name) {
		super(name);
	}

}
