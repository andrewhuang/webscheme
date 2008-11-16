package jist.listener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.security.AccessControlException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import sisc.REPL;
import sisc.interpreter.AppContext;
import sisc.interpreter.Context;
import sisc.interpreter.Interpreter;
import sisc.ser.MemoryRandomAccessInputStream;
import sisc.ser.SeekableInputStream;

/**
 * Swing JFrame that contains the Scheme listener and buttons
 * 
 * @author Turadg
 */
public class SchemeListener extends JFrame {

	Interpreter interpreter;

	final SchemeListenerPane slp;

	static {
		enableInterrupts();
	}

	static void enableInterrupts() {
		try {
			// enabling interrupts
			System.setProperty("sisc.permitInterrupts", "true");
			System.err.println("interrupts enabled");
		} catch (AccessControlException ex) {
			System.err.println("WARNING: could not enable interrupts");
		}
	}

	/**
	 * Construct a SchemeListener that uses the passed interpreter
	 * 
	 * @param interpreter
	 */
	public SchemeListener(Interpreter interpreter) {
		super("Scheme Interpreter");
		this.interpreter = interpreter;
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 10, 5));
		JButton breakButton = new JButton("Break");
		// for this to work, sisc.permitInterrupts system property
		// must be set to "true" before the sisc.util.Util class loads,
		// so do it when this class loads (see static block)
		breakButton.setEnabled(sisc.util.Util.permitInterrupts);
		AbstractAction breakAction = new AbstractAction("Break") {

			public void actionPerformed(ActionEvent e) {
				slp.interruptEvaluation();
			}
		};
		breakButton.addActionListener(breakAction);
		topPanel.add(breakButton);
		JButton resetButton = new JButton("Reset");
		AbstractAction resetAction = new AbstractAction("Reset") {

			public void actionPerformed(ActionEvent e) {
				int choice = JOptionPane.showConfirmDialog(SchemeListener.this,
						"Are you sure you want to reset the interpreter?",
						"Reset?", JOptionPane.YES_NO_OPTION);
				if (choice == JOptionPane.YES_OPTION)
					slp.reset();
				slp.requestFocus();
			}
		};
		resetButton.addActionListener(resetAction);
		topPanel.add(resetButton);
		// FIX would be nice to allow Control-T for this
		JButton hideButton = new JButton("Hide");
		AbstractAction hideAction = new AbstractAction("Hide") {

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		};
		hideButton.addActionListener(hideAction);
		topPanel.add(hideButton);
		slp = new SchemeListenerPane(interpreter);
		slp.setBackground(Color.black);
		//added this
		slp.setForeground(Color.white);
		slp.setFont(new Font("Courier", Font.BOLD, 14));
		slp.setCaretColor(new Color(255, 255, 255));
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(slp, BorderLayout.CENTER);
		getContentPane().setSize(300, 400);
		//getContentPane().setLayout(new BorderLayout());
		JScrollPane scroller = new JScrollPane();
		scroller.getViewport().add(slp);
		getContentPane().add(scroller, BorderLayout.CENTER);
		setSize(400, 400);
		slp.requestFocus();
		slp.startPrompt();
	}

	/**
	 * Construct a new SchemeListener using a fresh interpreter
	 */
	public SchemeListener() {
		this(freshInterpreter());
		System.out.println("SchemeListener constructed with fresh interpreter");
	}

	/**
	 * Necessary to close down cleanly so that a new instance can load later in
	 * this same JVM
	 *  
	 */
	public void kill() {
		interpreter = null;
		try {
			slp.removeAll();
			slp.removeNotify();
			removeNotify();
			super.finalize();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public SchemeConsoleFrame getConsole() {
		return slp.scf;
	}

	public Interpreter getInterpreter() {
		return interpreter;
	}

	public SchemeListenerPane getSchemeTextArea() {
		return slp;
	}

	static Interpreter freshInterpreter() {
		// FIX this needs to be set somewhere
		// 	Defaults.DEFAULT_PERMIT_INTERRUPTS = true; // necessary for time-out
		// interrupts
		AppContext ctx = new AppContext();
		Context.register("listener", ctx);
		Interpreter p = Context.enter(ctx);
		// load SISC heap
		try {
			URL heapURL = sisc.boot.HeapAnchor.class.getResource("sisc.shp");
			System.out.println("Loading heap from " + heapURL);
			SeekableInputStream heap = new MemoryRandomAccessInputStream(
					heapURL.openStream());
			// to get around the "No disk in drive" dialog,
			// disable the SecurityManager temporarily
			try {
				SecurityManager sm = System.getSecurityManager();
				System.setSecurityManager(null);
				REPL.loadHeap(p, heap);
				System.setSecurityManager(sm); // replace it
			} catch (AccessControlException ex) {
				// no SecurityManager anyway so don't bother
				System.out.println("Abandoning floppy workaround :: " + ex);
				REPL.loadHeap(p, heap);
			}
		} catch (Exception ex) {
			System.err.println("Error loading heap file :: " + ex);
		}
		System.out.println("heap loaded");
		Context.exit();
		return p;
	}

	/**
	 * @param expr
	 *            Scheme expression to evaluate on start or reset
	 */
	public void setInitExpression(String expr) throws IllegalStateException {
		slp.setInitExpression(expr);
	}

	public static void main(String[] args) {
		SchemeListener listener = jist.env.JistStore.getListener();
		listener.setVisible(true);
		listener.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
