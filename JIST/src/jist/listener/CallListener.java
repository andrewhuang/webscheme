package jist.listener;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JApplet;
import javax.swing.JButton;

import jist.env.JistStore;

/**
 * <pre>
 * 
 *   &lt;applet code=&quot;jist.listener.CallListener.class&quot; codebase=&quot;http://www.ucwise.org/jist/&quot; archive=&quot;ucwise.jar,xmlrpc-1.1-applet.jar&quot; width=&quot;1&quot; height=&quot;1&quot;&gt; &lt;/applet&gt;
 *     
 * </pre>
 * 
 * 
 * load-urls : space-delimited list of URLs to |load|
 * 
 * @author Turadg
 */
public class CallListener extends JApplet {

	private static CallListener currentCallListener;

	public CallListener() {
		JButton show = new JButton("Show");

		AbstractAction actionHide = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JistStore.getListener().setVisible(true);
			}
		};
		show.addActionListener(actionHide);

		getContentPane().setLayout(new FlowLayout());
		getContentPane().add(show);
	}

	/**
	 * Sets up the Listener, sets the current-url, runs the init-expr if it
	 * hasn't been already, runs the eval-silent param regardless
	 */
	public void init() {
		currentCallListener = this;

		// set current-url
		String baseURL = getCodeBase().toString();
		System.out.println("(current-url \"" + baseURL + "\")");
		JistStore.evaluateInListener("(current-url \"" + baseURL + "\")");

		initParam();
		evalParam();

		JistStore.getListener().setVisible(true);
	}

	/**
	 * Load it up over again as if the applet were called again by browser
	 *  
	 */
	public static void reinit() {
		currentCallListener.init();
	}

	/**
	 * Check the Applet param init-expr and set it in the Listener
	 */
	void initParam() {
		String expr = getParameter("init-expr");
		if (expr == null)
			return;
		System.out.println("init-expr: " + expr);
		try {
			JistStore.getListener().setInitExpression(expr);
			JistStore.evaluateInListener(expr);
		} catch (IllegalStateException ex) {
			System.err.println("init-expr set before; ignored");
		}
	}

	/**
	 * Run the expression contained in eval-silent param
	 */
	void evalParam() {
		String expr = getParameter("eval-silent");
		System.out.println("eval-silent: " + expr);
		if (expr != null)
			JistStore.evaluateInListener(expr);
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
