/**
 * Event
 * 
 * Java model of a WebScheme event
 * 
 * @author Turadg
 */

package webscheme.events;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import sisc.data.Value;
import sisc.io.ReaderInputPort;
import sisc.reader.Lexer;
import sisc.reader.Parser;

public class Event {

	static List splitAssertions(String code) {
		List assertions = new LinkedList();
		ReaderInputPort port = new ReaderInputPort(new BufferedReader(
				new StringReader(code)));
		Parser parser = new Parser(new Lexer()); // the lexer is stateful
		try {
			for (Value v = parser.nextExpression(port); v != Parser.EOF; v = parser
					.nextExpression(port)) {
				assertions.add(v);
			}
		} catch (IOException iox) {
			// never happens since just reading a string
		}
		return assertions;
	}

	List assertions;
	String name;

	Template template;

	public Event(String name, List assertions, Template template) {
		this.name = name;
		this.assertions = assertions;
		this.template = template;
	}

	public Event(String name, String assertionsCode, String template) {
		this(name, splitAssertions(assertionsCode), new Template(template));
	}

	/**
	 * List is of sisc.data.Value s-expressions
	 */
	public List getAssertions() {
		return Collections.unmodifiableList(assertions);
	}

	public String getName() {
		return name;
	}

	public Template getTemplate() {
		return template;
	}

}