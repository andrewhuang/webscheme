/**
 * NoSuchTemplateException
 * 
 * @author Turadg
 */

package webscheme.events;

public class NoSuchTemplateException extends RuntimeException {

	/**
	 * Constructs a <code>NoSuchTemplateException</code>, saving the name of
	 * the template <tt>name</tt> for later retrieval by the
	 * <tt>getMessage</tt> method.
	 * 
	 * @param name
	 *            the named template
	 */
	public NoSuchTemplateException(String name) {
		super(name);
	}

}