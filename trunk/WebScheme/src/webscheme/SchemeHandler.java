/**
	SchemeHandler

	The hub of WebScheme.  Sits as an applet on the Web page,
	mediating communication between the Scheme and
	Javascript/DOM environments.  

	@author Turadg
*/

package webscheme;

import webscheme.dom.*;
import webscheme.events.*;
import webscheme.wise.*;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import javax.swing.*;

import sisc.*;
import sisc.data.*;
import sisc.interpreter.*;
import sisc.ser.*;
import sisc.util.*;

public class SchemeHandler extends JApplet {

	/** Prefix of the applet param enumerating URLs to load */
	static final String LOADURL_PREFIX = "loadurl-";
	/** Prefix of the applet param enumerating event names */
	static final String EVENT_NAME_PREFIX = "event-name-";
	/** Prefix of the applet param enumerating event assertions */
	static final String EVENT_ASSERTIONS_PREFIX = "event-assertions-";
	/** Prefix of the applet param enumerating event templates */
	static final String EVENT_TEMPLATE_PREFIX = "event-template-";

	// TODO refactor HeapAnchor as HeapLoader and put this filename there
	/** Filename of heap file for SISC */
	static final String SISC_HEAP = "sisc.shp";
	/** Name of SISC Scheme application context for WebScheme */
	static final String WEBSCHEME_CONTEXT = "webscheme";

	/** Map of event names to event definitions */
	final Map events = new HashMap();

	/** Scheme symbol for the WebScheme evaluation timeout */
	static final Symbol TIMEOUT_DELAY_SYM = Symbol.get("ws-timeout-delay");
	/** Scheme symbol for the message to show upon evaluation timeout */
	static final Symbol TIMEOUT_MESSAGE_SYM = Symbol.get("ws-timeout-message");

	/** for communication with Scheme enviroment */
	static Interpreter interpreter;
	/** for communication with web page environment */
	DataModel dataModel;
	/** for communication with persistent storage of page state */
	StateStore stateStore;

	public SchemeHandler() {
		getContentPane().add(new java.awt.Label("SchemeHandler"));
	}

	public void init() {
		// FIX create a reset-interpreter PARAM command
		if (interpreter != null)
			reuseInterpreter();
		else
			try {
				// TODO copy the SISC heap locally and then load it as a file
				URL heapURL = sisc.HeapAnchor.class.getResource(SISC_HEAP);
				if (heapURL == null)
					throw new MissingResourceException(
						"SISC heap file unavailable",
						SISC_HEAP,
						null);
				initInterpreter(heapURL);
			} catch (Exception ex) {
				ex.printStackTrace();
				return;
			}
		readEvents();
		initSchemeEnv();
		initWise();
		loadFiles();
		evaluateQuiet(getParameter("init-expr"));
		restoreDocumentState();
		System.out.println("SchemeHandler ready");
	}

	public StateStore getStateStore() {
		return stateStore;
	}

	void restoreDocumentState() {
		stateStore.restore();
	}

	void initInterpreter(URL heapURL)
		throws IOException, ClassNotFoundException {
		System.out.print("Loading heap from " + heapURL);
		SeekableInputStream heap =
			new MemoryRandomAccessInputStream(heapURL.openStream());
		// as of SISC 1.8, this must be done via -D
		// 	Defaults.permitinterrupts = "true"; // necessary for time-out interrupts
		AppContext ctx = new AppContext();
		Context.register(WEBSCHEME_CONTEXT, ctx);
		interpreter = Context.enter(WEBSCHEME_CONTEXT);
		REPL.initializeInterpreter(interpreter, new String[0], heap);
		System.out.println(" done.");
		// load ws-lib
		String baseURL = getCodeBase().toString();
		System.out.println("(current-url \"" + baseURL + "\")");
		evaluateQuiet("(current-url \"" + baseURL + "\")");
		evaluateQuiet("(import libraries)");

		// produce some debugging output
		evaluateQuiet("(with/fc (lambda (m e) (print-exception (make-exception m e)))   (lambda () (require-library \"webscheme/wslib\") ))");
		// 	evaluateQuiet("(require-library \"webscheme/wslib\")");
	}

	void reuseInterpreter() {
		interpreter = Context.enter(WEBSCHEME_CONTEXT);
	}

	void initSchemeEnv() {
		dataModel = new DataModel(this);
		Symbol dmSymb = Symbol.get("ws-data-model-obj");
		Value dmObj = new sisc.modules.s2j.JavaObject(dataModel);
		Symbol shSymb = Symbol.get("ws-scheme-handler-obj");
		Value shObj = new sisc.modules.s2j.JavaObject(this);
		try {
			interpreter.define(dmSymb, dmObj, Util.TOPLEVEL);
			interpreter.define(shSymb, shObj, Util.TOPLEVEL);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void initWise() {
		WiseContext wc = new WiseContext(this);

		try {
			stateStore = new StateStore(dataModel, wc);
			Symbol ssSymb = Symbol.get("ws-statestore-obj");
			Value ssObj = new sisc.modules.s2j.JavaObject(stateStore);
			interpreter.define(ssSymb, ssObj, Util.TOPLEVEL);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			Logger wsl = new Logger(wc);
			Symbol wslSymb = Symbol.get("ws-logger-obj");
			Value wslObj = new sisc.modules.s2j.JavaObject(wsl);
			interpreter.define(wslSymb, wslObj, Util.TOPLEVEL);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Load Scheme files specified in each loadurl-n applet param */
	void loadFiles() {
		final Symbol loadSymb = Symbol.get("load");

		// then load parameter urls
		for (int i = 0; true; i += 1) {
			String url = getParameter(LOADURL_PREFIX + i);
			if (url == null)
				break;
			System.out.println("[ loading " + url + " ]");

			try {
				interpreter.eval(
					(Procedure) interpreter.dynenv.ctx.toplevel_env.lookup(
						loadSymb),
					new Value[] { new SchemeString(url)});
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/** Evaluate the expression ignoring the returned value */
	public void evaluateQuiet(String sexpression) {
		if (interpreter == null) {
			System.out.println("not evaluating: " + sexpression);
			return;
		}

		try {
			// 	    printPermStatus("SchemeHandler.evaluateQuiet()");
			interpreter.eval(sexpression);
		} catch (Exception ex) {
			System.err.println(ex);
		}
	}

	static void printPermStatus(String source) {
		try {
			System.out.print(
				source + " has accessDeclaredMembers permission: ");
			java.security.Permission perm =
				new RuntimePermission("accessDeclaredMembers");
			java.security.AccessController.checkPermission(perm);
			System.out.println("true");
		} catch (AccessControlException ex) {
			System.out.println("false");
		}
	}

	/** Search web page for the requisite symbols and fill the template
	 * 	with the corresponding values
	 * 	@return the filled-out template 
	 */
	String fillTemplate(Template template) {
		/*
		System.out.println("fillTemplate() on:\n  " + template);
		System.out.println("  with symbols:\n" + template.symbols());
		*/
		Map inputMap = mapInputs(template.symbols());
		String full = template.fill(inputMap);
		/*
		System.out.println("fillTemplate() returning: " + full);
		*/
		return full;
	}

	/** Reads events out of the PARAM tags, starting at zero index
	 */
	void readEvents() {
		for (int i = 0; true; i += 1) {
			try {
				String name = getParameter(EVENT_NAME_PREFIX + i);
				if (name == null)
					break;
				String assertionsdef =
					getParameter(EVENT_ASSERTIONS_PREFIX + i);
				String templatedef = getParameter(EVENT_TEMPLATE_PREFIX + i);
				defineEvent(name, assertionsdef, templatedef);
			} catch (StringIndexOutOfBoundsException ex) {
				System.err.println("Error parsing event param :: " + ex);
			}
		}
	}

	/** Search web page for the requisite symbols and return
	 * a mapping of symbols to their values on the page
	 * @return Map of inputs to value 
	*/
	Map mapInputs(Collection inputs) {
		Map inputMap = new HashMap();
		for (Iterator iterator = inputs.iterator(); iterator.hasNext();) {
			String inputName = (String) iterator.next();
			String inputString = dataModel.getString(inputName);
			inputMap.put(inputName, inputString);
		}
		return inputMap;
	}

	/**
	   Parse and add event to those available
	*/
	public void defineEvent(
		String name,
		String assertionsdef,
		String templatedef) {
		/*
		System.out.println("[ start event " + name + " ]");
		System.out.print("  [assertions] ");
		System.out.println(assertionsdef);
		System.out.print("  [template] ");
		System.out.println(templatedef);
		System.out.println("[ end ]");
		*/
		webscheme.events.Event event =
			new webscheme.events.Event(name, assertionsdef, templatedef);
		events.put(name, event);
	}

	/**
	   Evaluates each assertion of the named event.
	   If each one returns #t, fills in the template and evaluates it
	*/
	public void runEvent(String eventName)
		throws NoSuchEventException, IOException {
		System.out.println("runEvent(" + eventName + "):");
		webscheme.events.Event event =
			(webscheme.events.Event) events.get(eventName);
		if (event == null)
			throw new NoSuchEventException(eventName);
		// try each assertion
		Iterator iterator = event.getAssertions().iterator();
		boolean alltrue = true;
		while (iterator.hasNext()) {
			try {
				Value exp = (Value) iterator.next();
				System.out.println("Evaluating assertion: " + exp);
				Value val = interpreter.eval(exp);
				System.out.println("  [" + val + "]");
				if (val != SchemeBoolean.TRUE) { // abort on failure
					alltrue = false;
					break;
				}
			} catch (SchemeException ex) {
				System.err.println("..assertion caused SchemeException: " + ex);
				alltrue = false;
				break;
			}
		}

		if (!alltrue) {
			// FIX decide whether to notify user automatically
			System.out.println("ALERT: one or more assertions failed");
			return;
		}

		// through the gauntlet
		String code = fillTemplate(event.getTemplate());
		System.out.println(code);
		evaluate(code);
	}

	/** experimental */
	public void setJSError(String s) {
		System.err.println("\n\nsetJSError( " + s + " )");
	}

	/** Evaluate the given s-expression
	 * 	
	 * @param sexp s-expression to evaluate
	 */
	void evaluate(String sexp) {
		int timeoutDelay;
		try {
			Value timeoutDelayVal = interpreter.eval(TIMEOUT_DELAY_SYM);
			timeoutDelay = ((Quantity) timeoutDelayVal).intValue();
		} catch (Exception ex) {
			System.err.println(TIMEOUT_DELAY_SYM + " undefined");
			timeoutDelay = 30;
		}
		EvaluationThread et =
			new EvaluationThread(interpreter, sexp, timeoutDelay);
		et.start();
	}

	/** Thread that evaluates the expression
	 * 	If evaluation time exceeds threshold, aborts and pops-up
	 * 	an error dialog
	 * 	@author Turadg
	 */
	class EvaluationThread extends Thread {
		final Interpreter interpreter;
		final javax.swing.Timer timer;
		final String sexpression; // to evaluate

		public EvaluationThread(Interpreter i, String s, int t) {
			super();
			interpreter = i;
			sexpression = s;

			ActionListener timeoutAction = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String timeoutMessage;
					try {
						Value timeoutMessageVal =
							interpreter.eval(TIMEOUT_MESSAGE_SYM);
						timeoutMessage =
							((SchemeString) timeoutMessageVal).asString();
					} catch (Exception ex) {
						System.err.println(TIMEOUT_MESSAGE_SYM + " undefined");
						timeoutMessage =
							"Scheme evaluation time exceeded limit of "
								+ timer.getInitialDelay() / 1000
								+ " seconds";
					}

					interpreter.tctx.interrupt = true;
					JOptionPane.showMessageDialog(
						null,
						timeoutMessage,
						"Evaluation aborted after "
							+ timer.getInitialDelay() / 1000
							+ "seconds",
						JOptionPane.ERROR_MESSAGE);
				}
			};

			timer = new javax.swing.Timer(t * 1000, timeoutAction);
			timer.setRepeats(false);

			// nice it
			setPriority(getPriority() - 2);
		}

		public void run() {
			System.out.println(
				"EvaluationThread to run for "
					+ (timer.getInitialDelay() / 1000)
					+ " seconds...");
			Interpreter r = Context.enter(interpreter);
			r.tctx.interrupt = false; // unstick break
			timer.restart();

			Value rv = null;
			try {
				rv = r.eval(sexpression);
				// do nothing with returned Value

				// FIX remove once debugging's done
				System.out.println(
					"ET returned " + rv + " (" + rv.getClass() + ")");
			} catch (SchemeException se) {
				System.err.println(se);
				String message = ((Pair) ((Pair) se.m).car).cdr.toString();
				JOptionPane.showMessageDialog(
					null,
					message,
					"Scheme Exception",
					JOptionPane.ERROR_MESSAGE);
			} catch (IOException iox) {
				System.err.println("IO: " + iox);
			}

			timer.stop();

			// see if the return value is an error
			if (rv instanceof Values) {
				Value[] vs = ((Values) rv).values;
				if (vs.length == 2 && vs[1] instanceof ApplyParentFrame) {
					// interpreter returned error continuation
					try {
						r.eval(vs[1]);
					} catch (Exception ex) {
						ex.printStackTrace(); // should never happen?
					}
					// vs[0] is has the error message
				}
			}
		}

	}

}