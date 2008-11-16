/**
 * This class provides the functionality for the front-end of the Replacement
 * Modeler.
 * 
 * $Id: ModelerTextArea.java,v 1.15 2004/11/09 22:18:30 turadg Exp $
 * 
 * @author Jeff Wang
 */

package jist.listener;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.LinkedList;

import jist.editor.AbstractSchemeEditorPane;
import jist.editor.TokenScanner;
import sisc.data.SchemeVoid;
import sisc.data.Value;
import sisc.interpreter.Context;
import sisc.interpreter.Interpreter;
import sisc.interpreter.SchemeException;

public class ModelerTextArea extends AbstractSchemeEditorPane {

	/**
	 * The expression to evaluate
	 */
	String expr;

	/**
	 * Selected Text
	 */
	String selectedText = "";

	int selectionStart, selectionEnd = 0;

	/**
	 * History of expressions evaluated
	 */
	LinkedList history;

	/**
	 * Full Text of this modeler
	 */
	String modelerText;

	Interpreter interpreter;

	public ModelerTextArea(String expr, Interpreter interpreter) {
		super();
		setText(expr.toString());
		this.interpreter = interpreter;
		if (interpreter == null)
			throw new NullPointerException("interpreter was null");

		this.expr = expr;
		modelerText = expr;
		history = new LinkedList();
		history.addLast(expr);
		setEditable(false);
	}

	void reset() {
		setText("");
	}

	protected void processKeyEvent(KeyEvent k) {

		String eval;

		if (k.getID() == KeyEvent.KEY_PRESSED) {
			switch (k.getKeyCode()) {
			case KeyEvent.VK_ENTER:
				if (getSelection().equals("")) {

					//If ctrl is down then full eval else part-eval
					if (k.isControlDown())
						eval = "(full-eval '" + expr + ")";
					else
						eval = "(part-eval '" + expr + ")";

					evalExpression(eval);
				} else {
					//if there's a selection
					selectedText = getSelection();
					if (TokenScanner.balancedParens(selectedText)) {
						if (k.isControlDown())
							eval = "(full-eval '" + selectedText + ")";
						else
							eval = "(part-eval '" + selectedText + ")";
						selectionStart = getSelectionStart();
						selectionEnd = getSelectionEnd();
						//If newlines are in the selection, subtract from the
						// end
						int index = selectedText.indexOf("\n");
						while (index != -1) {
							selectionEnd--;
							index = selectedText.indexOf("\n", index + 1);
						}
						updateText(selectionStart);
						evalExpression(eval);
						selectedText = "";
					}
				}
				break;
			case KeyEvent.VK_BACK_SPACE:
				if (history.size() > 1) {
					history.removeLast();
					expr = (String) history.getLast();
					refresh(history);
				}
				break;

			}
		}
	}

	protected void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);

		if (e.getID() == MouseEvent.MOUSE_CLICKED) {
			int pos = getCaretPosition();
			int start = findSExpressionStart(pos - 1);
			int end = findSExpressionEnd(start + 1);
			setSelection(start, end, false);
		}
	}

	private void updateText(int from) {

		//update History
		int index = modelerText.indexOf("\n\n", from);
		while (index != -1) {
			history.removeLast();
			index = modelerText.indexOf("\n\n", index + 1);
		}

		//Update the modelerText
		int end;
		end = modelerText.indexOf("\n\n", from);
		if (end != -1)
			modelerText = modelerText.substring(0, end);
		//System.out.println("updated text is:" + modelerText);
		//System.out.println("history has :" + history.size());
	}

	/**
	 * Refreshes the text area with the given history of evaluated expressions
	 * 
	 * @param history
	 */
	private void refresh(LinkedList history) {
		modelerText = (String) history.get(0);

		for (int i = 1; i < history.size(); i++) {
			modelerText += "\n\n" + (String) history.get(i);
		}

		setText(modelerText);
	}

	protected void evalExpression(String e) {
		interpreter = Context.enter(interpreter);
		try {
			Value rv;

			rv = interpreter.eval(e);

			if (rv instanceof SchemeVoid) {
				System.out.println("Return value was void");
			} else {
				if (!selectedText.equals("")) {

					int exprStartPos;
					String oldExpr = (String) history.getLast();

					exprStartPos = modelerText.lastIndexOf("\n") + 1;

					//			System.out.println("selectionStart: " + selectionStart);
					//			System.out.println("selectionEnd:" + selectionEnd);
					//			System.out.println("exprStartPos: " + exprStartPos);
					//			System.out.println("expr:" + expr + " old: " + oldExpr);

					expr = oldExpr.substring(0, selectionStart - exprStartPos);
					expr += rv.toString();
					expr += oldExpr.substring(selectionEnd - exprStartPos);
					expr.trim();

				} else {
					expr = rv.toString();
				}

				history.addLast(expr);
				modelerText += "\n\n" + expr;
				setText(modelerText);
			}
		} catch (SchemeException ex) {
			System.out.println("Modeler Input Error: " + ex);
		} catch (IOException ex) {
			System.err.println("IO: " + ex);
		}

		Context.exit();
	}

	static final String MACRO_PREPEND = ""
			+ "(let-syntax ([old-define (syntax-rules ()"
			+ "                          [(_ . rest) (define . rest)])])"
			+ "  (letrec-syntax"
			+ "    ([define (syntax-rules (lambda)"
			+ "              [(_ name (lambda formals . body))"
			+ "                (old-define name (let ([p (lambda formals . body)])"
			+ "                                  (\\@procedure-properties::set-procedure-property!"
			+ "                                   p 'definition"
			+ "                                   '(lambda formals . body))"
			+ "                                  p))]"
			+ "              [(_ (name . formals) . body)"
			+ "               (define name (lambda formals . body))]"
			+ "              [(_ . rest) (old-define . rest)])))";

	// expression goes in between
	static final String MACRO_APPEND = "))";

	/**
	 * Wraps the expression in a macro that enables use of |procedure-body|
	 * 
	 * @author turadg
	 */
	static String wrapExpression(String expr) {
		// can't wrap an empty expr
		if (expr.trim().length() == 0)
			return "";

		return MACRO_PREPEND + expr + MACRO_APPEND;
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
