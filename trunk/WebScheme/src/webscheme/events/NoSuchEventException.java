/** NoSuchEventException

    @author Turadg
*/

package webscheme.events;

public class NoSuchEventException extends RuntimeException {

	/**
	 * Constructs a <code>NoSuchEventException</code>, saving the  
	 * name of the event <tt>name</tt> for later retrieval by the 
	 * <tt>getMessage</tt> method.
	 *
	 * @param   name   the named event
	 */
	public NoSuchEventException(String name) {
		super(name);
	}

}
