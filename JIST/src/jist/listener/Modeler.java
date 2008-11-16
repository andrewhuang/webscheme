package jist.listener;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import sisc.data.Expression;
import sisc.interpreter.Interpreter;

/**
 * This is the front-end for the Replacement Modeler.
 * 
 * $Id: Modeler.java,v 1.10 2004/10/20 23:07:02 turadg Exp $
 * 
 * @author Jeff Wang
 */
public class Modeler extends JFrame {

	//The text Area
	ModelerTextArea mta;

	//Scroll Pane
	JScrollPane pane;

	//The Menu Bar
	JMenuBar menu;

	//Menu Actions
	Action actionHelp;

	Action actionAbout;

	//This the message that Help prints
	String helpString = "Enter : Partial Evaluate\n"
			+ "You may also select specific expressions to evaluate by using the mouse to select an expression and then pressing Enter. \n\n"
			+ "CTRL + ENTER : Full Evaluate\n" + "BackSpace : Undo last step\n";

	String aboutString = "This is the Replacement Modeler V2.0 for STk 4.0.1.\n"
			+ "This modeler is based on Brian Gaeke's modeler and the Harvey/Wright modeler V1.5.\n"
			+ "Ported to SISC by Jeff Wang (July 2003)";

	static final String WINDOW_TITLE = "Replacement Modeler";

	//The SISC interpreter
	Interpreter interpreter;

	/**
	 * Current expression to eval
	 */
	String expr;

	public Modeler(String expr) {
		super(WINDOW_TITLE);
		interpreter = jist.env.JistStore.getListener().getInterpreter();
		this.expr = expr;
	}

	public Modeler(Expression e) {
		this(e.toString());
	}

	/* The entry point to the modeler */
	public void init() {

		setJMenuBar(createMenu());
		mta = new ModelerTextArea(expr, interpreter);
		pane = new JScrollPane(mta);
		pane.setMinimumSize(new java.awt.Dimension(400, 500));
		getContentPane().add(pane);
		setSize(600, 400);

		//this.pack();
		this.setVisible(true);

		this.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				//mta.reset();
				mta.setVisible(false);

			}
		});

	}

	public JMenuBar createMenu() {
		menu = new JMenuBar();

		// Help menu
		JMenu mHelp = new JMenu("Help");
		mHelp.setMnemonic('h');

		// Help:Help
		actionHelp = new AbstractAction("Help") {

			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, helpString);
			}
		};

		mHelp.add(actionHelp);

		//Help:About
		actionAbout = new AbstractAction("About") {

			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, aboutString);
			}
		};

		mHelp.add(actionAbout);

		//Add help menu to the menuBar
		menu.add(mHelp);

		return menu;

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
