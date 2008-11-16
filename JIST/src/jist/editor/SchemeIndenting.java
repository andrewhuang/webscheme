package jist.editor;

import java.util.HashMap;
import java.util.Map;

/**
 * a data structure of indenting settings
 */
public class SchemeIndenting {

	static final int SPACES = 0;

	static final int FROM = 1;

	// indenting settings
	static int[] deft = { 1, 1 };

	static Map words = new HashMap();
	static {
		words.put("ant", new int[] { 1, 1 });
		words.put("and", new int[] { 1, 1 });
		words.put("begin", new int[] { 2, 0 });
		words.put("case", new int[] { 1, 1 });
		words.put("cond", new int[] { 1, 1 });
		words.put("define", new int[] { 2, 0 });
		words.put("delay", new int[] { 1, 1 });
		words.put("do", new int[] { 1, 1 });
		words.put("else", new int[] { 1, 1 });
		words.put("if", new int[] { 1, 1 });
		words.put("lambda", new int[] { 2, 0 });
		words.put("let", new int[] { 2, 0 });
		words.put("let*", new int[] { 2, 0 });
		words.put("letrec", new int[] { 2, 0 });
		words.put("", new int[] { 0, 0 }); // previously called "none"
		words.put("or", new int[] { 1, 1 });
		words.put("quasiquote", new int[] { 1, 1 });
		words.put("quote", new int[] { 1, 1 });
		words.put("set!", new int[] { 1, 1 });
		words.put("unknown", new int[] { 0, 0 });
		words.put("unquote", new int[] { 1, 1 });
		words.put("unquote-splicing", new int[] { 1, 1 });
	}

	// return number of spaces for a given name
	protected static int getSpaces(String name) {
		int[] word = (int[]) words.get(name);
		if (word == null)
			return deft[SPACES];
		else
			return word[SPACES];
	}

	// return from-type for a given name
	protected static int getFrom(String name) {
		int[] word = (int[]) words.get(name);
		if (word == null)
			return deft[FROM];
		else
			return word[FROM];
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
