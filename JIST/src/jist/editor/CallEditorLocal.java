package jist.editor;

import java.applet.Applet;
import java.io.File;
import java.io.IOException;

import jist.env.JistStore;

/**
 * Calls up the Editor using the local filesystem
 * 
 * @see CallEditor
 * @author Turadg
 */
public class CallEditorLocal extends Applet {

	public void init() {
		SchemeEditor editor = JistStore.getEditor();

		// load specified file
		String path = getParameter("open");

		boolean doOpen = (path != null) && (path.length() != 0);

		if (doOpen) {
			try {
				String realpath = path;
				System.out.println("Attempting to load '" + realpath + "'");
				File file = new File(realpath);
				editor.openFile(file);
			} catch (IOException ex) {
				System.err.println(ex);
			}
		} else {
			System.err.println("invalid 'open' parameter");
		}

		// always make visible and request focus
		editor.setVisible(true);
		editor.buffer.requestFocus();
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
