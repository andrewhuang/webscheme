/** Event
    
    Java model of a WebScheme event

    @author Turadg
*/

package webscheme.events;

import java.io.*;
import java.util.*;
import sisc.compiler.*;
import sisc.data.*;
import sisc.io.*;

public class Event {
	String name;
	List assertions;
	Template template;

	public Event(String name, String assertionsCode, String template) {
		this(name, splitAssertions(assertionsCode), new Template(template));
	}

	public Event(String name, List assertions, Template template) {
		this.name = name;
		this.assertions = assertions;
		this.template = template;
	}

	public String getName() {
		return name;
	}

	/**
	   List is of sisc.data.Value s-expressions
	*/
	public List getAssertions() {
		return Collections.unmodifiableList(assertions);
	}

	public Template getTemplate() {
		return template;
	}

	static List splitAssertions(String code) {
		List assertions = new LinkedList();
		ReaderInputPort port =
			new ReaderInputPort(new BufferedReader(new StringReader(code)));
		Parser parser = new Parser(new Lexer()); // the lexer is stateful
		try {
			for (Value v = parser.nextExpression(port);
				v != Parser.EOF;
				v = parser.nextExpression(port)) {
				// 		System.out.println("adding assertion '"+v+"' ("+v.getClass()+")");
				assertions.add(v);
			}
		} catch (IOException iox) {
			// never happens since just reading a string
		}
		return assertions;
	}

	/**
	   for debugging
	*/
	public static void main(String args[]) {
		System.out.println(splitAssertions(args[0]));
	}

}
