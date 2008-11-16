package jist.env;

import java.applet.Applet;
import java.io.IOException;

import jist.editor.SchemeEditor;
import jist.listener.CallListener;
import jist.listener.SchemeListener;
import sisc.interpreter.Context;
import sisc.interpreter.Interpreter;
import sisc.interpreter.SchemeException;

/**
 * Manage JIST applets and classes for lifetime of enclosing frame
 * 
 * $Id: JistStore.java,v 1.8 2004/10/20 23:07:02 turadg Exp $
 * 
 * @author Turadg Aleahmad
 */
public class JistStore extends Applet {

	static SchemeEditor editor;

	static SchemeListener listener;

	// to preserve while SchemeListener is revived
	static sisc.interpreter.Interpreter interpreter;

	public void init() {
		System.out.println("JIST package "
				+ getClass().getPackage().getSpecificationVersion());
		getEditor();
		getListener();
	}

	/**
	 * Kill the SchemeListener interface but keep the reference to the
	 * interpreter.
	 */
	static void killListener() {
		listener.kill();
		listener = null;
	}

	/**
	 * Start the SchemeListener over from scratch, creating a new fresh
	 * interpreter and setting it up as if it was just launched by the browser
	 */
	public static void reinitializeListener(String initExpression) {
		killListener();
		interpreter = null;
		CallListener.reinit();
	}

	/**
	 * NOTE: when the browser frameset is reloaded, start() is called for the
	 * new page before destroy() is called on the old one!
	 * 
	 * That prevents us from using start() to redisplay the listener
	 */
	public void destroy() {
		System.out.println("JistStore.destroy() killing listener");
		killListener();
	}

	public static synchronized SchemeEditor getEditor() {
		if (editor == null)
			editor = new SchemeEditor();
		return editor;
	}

	public static synchronized SchemeListener getListener() {
		if (listener == null) {
			if (interpreter == null)
				listener = new SchemeListener();
			else
				listener = new SchemeListener(interpreter);
		}
		interpreter = listener.getInterpreter();
		return listener;
	}

	/**
	 * @return true iff a reference is held to an existing interpreter
	 */
	public static boolean holdingInterpreter() {
		return (interpreter != null);
	}

	public static void evaluateInListener(String expr) {
		Interpreter r = getListener().getInterpreter();

		try {
			r = Context.enter(r);
			r.eval(expr);
		} catch (SchemeException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
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
