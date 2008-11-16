package jist.editor;

import java.applet.Applet;
import java.io.IOException;

import jist.env.JistStore;
import jist.io.WiserFile;
import jist.io.WiserFileSystem;

/**
 * Calls up the Editor using the UC-WISE filesystem
 * 
 * @see CallEditorLocal
 * @author Turadg
 * @version $Id: CallEditor.java,v 1.5 2004/10/20 23:07:02 turadg Exp $
 */
public class CallEditor extends Applet {

	public void init() {
		SchemeEditor editor = JistStore.getEditor();

		WiserFileSystem wfs = editor.getFileSystem();
		if (wfs == null) {
			try {
				// set up IO connection to server
				wfs = new WiserFileSystem(getCodeBase());
				editor.setRemoteFileSystem(wfs);
			} catch (Exception ex) {
				System.err.println("Error creating WiserFileSystem :: " + ex);
			}
		}

		// load specified file
		String path = getParameter("open");
		String newp = getParameter("new");

		boolean doOpen = (path != null) && (path.length() != 0);
		boolean doNew = (newp != null) && newp.equals("true");

		// new gets priority
		if (doNew) {
			boolean proceed = editor.checkKillBuffer();
			if (proceed) {
				editor.newFile(); // abort on cancel
				// if user cancels New File dialog, currentFile
				// stays as null and setVisible is deactivated
			}
		} else if (doOpen) {
			try {
				boolean proceed = editor.checkKillBuffer();
				if (proceed) {
					WiserFile wf = new WiserFile(wfs, path);
					editor.openFile(wf);
				}
			} catch (IOException ex) {
				System.err.println(ex);
			}
		} else {
			// make a new file if one isn't open
			if (editor.getCurrentFile() == null)
				editor.newFile();
		}

		// always make visible and request focus
		editor.setVisible(true);
		editor.getBuffer().requestFocus();
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
