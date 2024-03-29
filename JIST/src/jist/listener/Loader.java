package jist.listener;

import java.util.StringTokenizer;

import sisc.data.Procedure;
import sisc.data.SchemeString;
import sisc.data.Symbol;
import sisc.data.Value;
import sisc.interpreter.Interpreter;

/**
 * Loader
 * 
 * Loads a list of urls with the given interpreter
 * 
 * @author Turadg
 */
public class Loader {

	final static Symbol loadSymb = Symbol.get("load");

	public static void load(Interpreter r, String url) {
		try {
			r.eval((Procedure) r.dynenv.ctx.toplevel_env.lookup(loadSymb),
					new Value[] { new SchemeString(url) });
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @param urlList
	 *            is , delimited
	 */
	public static void loadList(Interpreter r, String urlList) {
		StringTokenizer st = new StringTokenizer(urlList, " ", false);

		while (st.hasMoreTokens())
			load(r, st.nextToken());
	}

	/**
	 * @param urlList
	 *            is array of url strings
	 */
	public static void loadList(Interpreter r, String[] urlList) {
		for (int i = 0; i < urlList.length; i += 1)
			load(r, urlList[i]);
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
