package jist.editor;

import javax.swing.JComponent;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.ViewFactory;

/**
 * This kit supports a fairly minimal handling of editing Scheme text content.
 * It supports syntax highlighting and produces the lexical structure of the
 * document as best it can.
 */
public class SchemeEditorKit extends DefaultEditorKit {

	public SchemeEditorKit(JComponent painter) {
		super();
		this.painter = painter;
	}

	public SchemeContext getStylePreferences() {
		if (preferences == null)
			preferences = new SchemeContext();
		return preferences;
	}

	public void setStylePreferences(SchemeContext prefs) {
		preferences = prefs;
	}

	/**
	 * Creates an uninitialized text storage model that is appropriate for this
	 * type of editor.
	 */
	public Document createDefaultDocument() {
		return new SchemeDocument(painter);
	}

	/**
	 * Get the MIME type of the data that this kit represents support for. This
	 * kit supports the type text/scheme.
	 */
	public String getContentType() {
		return "text/scheme";
	}

	/**
	 * Create a copy of the editor kit. This allows an implementation to serve
	 * as a prototype for others, so that they can be quickly created.
	 */
	public Object clone() {
		SchemeEditorKit kit = new SchemeEditorKit(painter);
		kit.preferences = preferences;
		return kit;
	}

	/**
	 * Fetches a factory that is suitable for producing views of any models that
	 * are produced by this kit. The default is to have the UI produce the
	 * factory, so this method has no implementation.
	 */
	public final ViewFactory getViewFactory() {
		return getStylePreferences();
	}

	protected SchemeContext preferences;

	protected JComponent painter;
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
