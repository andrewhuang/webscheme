package jist.listener;

import java.awt.IllegalComponentStateException;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import javax.swing.JFrame;

import jist.editor.AbstractSchemeEditorPane;
import jist.editor.TokenScanner;
import sisc.data.Pair;
import sisc.data.SchemeVoid;
import sisc.data.Value;
import sisc.env.DynamicEnvironment;
import sisc.env.SymbolicEnvironment;
import sisc.interpreter.AppContext;
import sisc.interpreter.Context;
import sisc.interpreter.Interpreter;
import sisc.interpreter.SchemeException;

/**
 * Swing TextPane in which the user types Scheme
 * 
 * $Id: SchemeListenerPane.java,v 1.19 2004/11/09 22:18:30 turadg Exp $
 * 
 * @author Turadg
 */
public class SchemeListenerPane extends AbstractSchemeEditorPane {

	final static int state_TopLevel = 0;

	final static int state_InExpr = 1;

	final static int state_TooManyParens = 2;

	final static String prompt = "> ";

	final static String START_STRING = ";; SISC Scheme Interpreter\n";

	//    final static int BREAK_INTERVAL = 10 * 1000; // 10 seconds
	int state = state_TopLevel;

	boolean netscapeBug = false;

	int fakeLength = 0;

	boolean matchParens = true;

	private int currentExprStart;

	Interpreter interpreter;

	EvaluationThread currentEvaluationThread;

	SchemeConsoleFrame scf;

	private String initExpression = "'no-init-expr";

	public SchemeListenerPane(Interpreter r) {
		super();
		// create the Scheme console
		scf = new SchemeConsoleFrame();

		// set the interpreter to use it for IO
		r.dynenv = new DynamicEnvironment(r.dynenv.ctx, scf.in, scf.out);
		interpreter = r;

		currentExprStart = START_STRING.length() + prompt.length();
		setEditable(true);
		//	startPrompt();

	}

	public void startPrompt() {
		setText(START_STRING);
		showPrompt();
	}

	protected synchronized void showPrompt(String text) {

		if (!text.equals("")) {
			state = state_InExpr;
		}
		append(prompt);
		storeCurrentExprBuffer();
		append(text);
		setCaretPosition(currentExprStart + text.length());
		requestFocus();
	}

	protected synchronized void showPrompt() {
		showPrompt("");
	}

	public synchronized void append(String res) {
		int offset = getDocument().getLength();
		try {
			getDocument().insertString(offset, res, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized String getCurrentExpression() {
		String schemeText = getText();
		return (state != state_InExpr) ? schemeText.substring(currentExprStart,
				schemeText.length()) : "";
	}

	public void storeCurrentExprBuffer() {
		currentExprStart = getText().length();
		try {
			setCaretPosition(currentExprStart);
		} catch (IllegalComponentStateException ex) {
			System.err.println("WTF? " + ex);
		}
	}

	public synchronized boolean isEmpty() {
		return getCurrentExpression().equals("");
	}

	public boolean inReservedArea() {
		return (getCaretPosition() < currentExprStart);
	}

	public boolean nearReservedArea() {
		return (getCaretPosition() <= currentExprStart);
	}

	public synchronized void store(String res) {
		append(res);
	}

	public synchronized void processMouseEvent(MouseEvent me) {
		// we don't want users to use cut and paste in reserved text area
		// however, this will not 100% prevent user actions on cut and paste
		if (me.isPopupTrigger())
			me.consume();
		super.processMouseEvent(me);
	}

	public void processKeyEvent(KeyEvent ke) {
		// since copying does not mutate the buffer, allow it at any time
		if (ke.isControlDown() && ke.getKeyCode() == KeyEvent.VK_C) {
			super.processKeyEvent(ke);
			return;
		}

		//	no keyboard input while evaluating
		if (currentEvaluationThread != null
				&& currentEvaluationThread.isAlive())
			return;

		if (inReservedArea()) {
			// RESERVED AREA Allow only ENTER key event and DIRECTION key events
			if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
				//Copy and paste text from the reserved area
				handleEnterCopy(getCaretPosition());
				ke.consume();
			} else if (ke.isActionKey()) { // Allow direction key strokes
				super.processKeyEvent(ke);
			} else
				ke.consume();
		} else if (nearReservedArea()) {
			// NEAR RESERVED AREA Do not allow BACK_SPACE
			if (ke.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
				ke.consume();
			} else if (ke.getKeyChar() == KeyEvent.VK_ENTER
					&& ke.getID() == KeyEvent.KEY_PRESSED) {
				handleEnterEval();
				ke.consume();
			} else
				super.processKeyEvent(ke);
		} else if (!inReservedArea()) {
			/*
			 * EVERYWHERE ELSE Allow all key evets with special regards to TAB
			 * and ENTER
			 */
			if (ke.getKeyChar() == KeyEvent.VK_ENTER
					&& ke.getID() == KeyEvent.KEY_PRESSED) {

				if (state != state_InExpr) {
					handleEnterEval();
					ke.consume();
				} else {
					indentReturn(); //go to next line
					ke.consume();
				}
			} else if (ke.getKeyChar() == KeyEvent.VK_TAB
					&& ke.getID() == KeyEvent.KEY_PRESSED) { //handle tabs
				if (nearReservedArea()) {
					ke.consume();

				} else {
					indentLine();
					ke.consume();
				}
			} else
				super.processKeyEvent(ke);
		}

		if (ke.getID() == KeyEvent.KEY_RELEASED) { //parens matching
			stateUpdate(currentExprStart, getCaretPosition());
			if (matchParens && ke.getKeyChar() == ')')
				super.parensFlash();
		}
	}

	protected boolean handleEnterCopy(int pos) {
		String selected = getSelectedText();

		// if forced, the cursor will start from the character after the
		// current one, because
		// mouse clicking is handled previously by the system
		if (selected == null)
			selected = selectSExpression(pos);

		// now simply bring down the selected expression
		if (selected != null) {
			append(selected);
			/*
			 * TODO output status String.valueOf(selected.length()) + "
			 * characters have been copied."
			 */
			setCaretPosition(getText().length());
		}
		return true;
	}

	boolean handleEnterEval() {
		String expr = getCurrentExpression();
		interpreter.tctx.interrupt = false; // unstick break
		evaluate(expr);
		store("\n");
		return true; // FIX
	}

	public static Vector parseExpression(String expr) {
		Vector result = new Vector();
		int startPos = 0;
		int endPos = 0;

		while ((endPos = expr.indexOf(')', endPos)) != -1
				&& !AbstractSchemeEditorPane.inQuotes(expr, endPos)) {
			String parsed = expr.substring(startPos, endPos + 1);

			if (TokenScanner.balancedParens(parsed)) {
				// parse the expression and store it into the vector
				if (expr.length() > 0)
					result.add(parsed);
				else
					System.err.println("parseExpression parsed empty string");
				expr = expr.substring(endPos + 1);
				endPos = 0;
			} else {
				endPos = endPos + 1;
				continue;
			}
		}

		if (expr.length() > 0)
			result.add(expr);
		return result;
	}

	protected void stateUpdate(int start, int end) {
		String schemeText = getText();
		char[] chars = schemeText.toCharArray();

		// first we go through the area removing all comments and quotes
		int j = start;
		while (j < end) {
			if (chars[j] == ';') {
				// in comments
				start = j;
				do {
					if ((chars[j] == '(') || (chars[j] == ')'))
						chars[j] = '|';
					j++;
				} while ((j < end) && (chars[j] != '\n'));

				if (j == end)
					return;
			} else if (chars[j] == '"') {
				// in quotes
				start = j;
				do {
					if ((chars[j] == '(') || (chars[j] == ')'))
						chars[j] = '|';
					if ((chars[j] == '\\') && (j < end)
							&& (chars[j + 1] == '"'))
						chars[j + 1] = '_';
					if ((chars[j] == '\\') && (j < end)
							&& (chars[j + 1] == '\\'))
						chars[j + 1] = '_';
					j++;
				} while ((j < end) && (chars[j] != '"'));

				if (j == end) {
					state = state_InExpr;
					return;
				} else {
					j++;
				}
			} else
				j++;
		}

		// find first paren of current expression
		int parenCount = 0;
		int parenBias = (end > 0) ? ((chars[end - 1] == ')') ? 1 : 0) : 0;
		int i = end - 1;
		// Count to see if parens are balanced. Works backwards, ie: searches
		// from the end first
		if (i >= start)
			do {
				if (chars[i] == ')')
					parenCount++;
				else if (chars[i] == '(')
					parenCount--;
				i--;
			} while ((i >= start) && (parenCount >= parenBias));

//			parenMatchStart = i;
//			parenMatchEnd = end;

		if (i == start - 1) {
			if (parenCount < 0
					|| getCaretPosition() < schemeText.trim().length())
				// Still in the expression. Not enough parens
				state = state_InExpr;
			else if (parenCount > 0) // Too many parens
				state = state_TooManyParens;
			else
				state = state_TopLevel;
		}
	}

	protected String selectSExpression(int pos) {
		String schemeText = getText();
		char[] chars = schemeText.toCharArray();
		int s, e;

		// find the beginning of the line where cursor lies in
		s = schemeText.substring(0, pos).lastIndexOf("\n");
		// adjust for the prompt
		s = (s == -1) ? prompt.length() : ((schemeText.substring(s + 1, s + 3)
				.equals(prompt)) ? s + 3 : s + 1);
		// find the end of the line
		e = schemeText.substring(pos).indexOf("\n") + pos;
		// set the end of the text area if not found
		e = (e == -1) ? schemeText.length() : e;

		// find the starting of an expression
		if (chars[pos - 1] == ')')
			pos--;
		for (int i = pos - 1, level = 0; i >= 0; i--) {
			if (chars[i] == ')')
				level++;
			else if (chars[i] == '(' && level != 0)
				level--;
			else if (chars[i] == '(' && level == 0) {
				s = i;
				break;
			}
		}
		// find the ending of an expression
		for (int i = s + 1, level = 0; i < currentExprStart; i++) {
			if (chars[i] == '(')
				level++;
			else if (chars[i] == ')' && level != 0)
				level--;
			else if (chars[i] == ')' && level == 0) {
				e = i + 1;
				break;
			}
		}
		// select the expression
		try {
			return schemeText.substring(s, e);
		} catch (StringIndexOutOfBoundsException ex) {
			return new String("");
		}
	}

	public void evaluate(String expr) {
		Vector parsed = parseExpression(expr);
		currentEvaluationThread = new EvaluationThread(interpreter);
		currentEvaluationThread.setParsedExpression(parsed);
		currentEvaluationThread.start();
	}

	public void interruptEvaluation() {
		currentEvaluationThread.interruptEvaluation();
	}

	public void reset() {
		interruptEvaluation(); // first kill whatever's running
		jist.env.JistStore.reinitializeListener(initExpression);
	}

	class EvaluationThread extends Thread {

		Interpreter interpreter;

		//For storing parsed multi-expression lines.
		Vector parsedExpressions;

		public EvaluationThread(Interpreter i) {
			super();
			interpreter = i;
			// nice it up
			setPriority(getPriority() - 1);
		}

		public void setParsedExpression(Vector v) {
			parsedExpressions = v;
		}

		public void run() {
			if (interpreter == null) {
				System.err.println("interpreter was null");
				return;
			}

			interpreter = Context.enter(interpreter);

			// try this way of binding the Turtle module
			SymbolicEnvironment se = interpreter
					.getContextEnv(Context.TOPLEVEL);
			new Turtle.Index().bindAll(interpreter, se);

			// since we're hopping over SISC's REPL, we don't get
			// some of it's handy functionality (eg. catching 'define define')
			// to use that, we'll need to meld InputStream and OutputStream
			// onto SchemeTextArea

			String incompleteExpr = "";
			for (int i = 0; i < parsedExpressions.size(); i++) {
				try {
					String sexpression = (String) parsedExpressions.get(i);

					if (!TokenScanner.balancedParens(sexpression) && i != 0) {
						incompleteExpr = sexpression;
					}

					if (sexpression.trim().length() == 0)
						continue;

					String enhancedExpr = ModelerTextArea
							.wrapExpression(sexpression);

					Value rv = interpreter.eval(enhancedExpr);

					// if it's the last expression, maybe output it
					if (i == parsedExpressions.size() - 1) {
						if (!(rv instanceof SchemeVoid))
							store(rv.toString() + "\n");
					}

				} catch (SchemeException ex) {
					//					store("\n" + ex.getFriendlyMessage() + "\n");
					// the above is preferred, but won't be available in SISC
					// until 1.8.8 at the earliest (assuming they accept my
					// modification)
					store("\n" + friendlyMessage(ex) + "\n");
				} catch (IOException ex) {
					System.err.println("IO: " + ex);
				}
			}

			Context.exit();

			showPrompt(incompleteExpr);
		}

		void interruptEvaluation() {
			interpreter.tctx.interrupt = true;
		}
	}

	public static String friendlyMessage(SchemeException ex) {
		Pair messagePair = (Pair) ex.m.car;
		Pair locationPair = (Pair) ((Pair) ex.m.cdr).car;

		return locationPair.cdr.toString() + ": " + messagePair.cdr.toString();
	}

	public OutputStream getOutputStream() {
		return new STAOutputStream();
	}

	class STAOutputStream extends OutputStream {

		public synchronized void write(int b) {
			// recall that the int should really just be a byte
			b &= 0x000000FF;

			// must convert byte to a char in order to append it
			char c = (char) b;
			append(String.valueOf(c));
		}

		public synchronized void write(byte[] b) {
			append(new String(b));
		}

		public synchronized void write(byte[] b, int offset, int length) {
			append(new String(b, offset, length));
		}
	}

	public static void main(String args[]) {
		JFrame jframe = new JFrame();
		AppContext ctx = new AppContext();
		Context.register("listener", ctx);
		Interpreter r = Context.enter("listener");
		SchemeListenerPane sta = new SchemeListenerPane(r);

		jframe.getContentPane().add(sta);
		jframe.setVisible(true);
	}

	/**
	 * @param expr
	 *            Scheme expression to evaluate on load or reset
	 */
	public void setInitExpression(String expr) {
		initExpression = expr;
	}

}

/*
 * Copyright (c) 2004 Regents of the University of California (Regents). Created
 * by Graduate School of Education, University of California at Berkeley.
 * 
 * This software is distributed under the GNU General Public License, v2.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 * 
 * REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE SOFTWAREAND ACCOMPANYING DOCUMENTATION, IF ANY, PROVIDED
 * HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * 
 * IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
