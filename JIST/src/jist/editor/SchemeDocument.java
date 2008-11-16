package jist.editor;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.GapContent;
import javax.swing.text.PlainDocument;
import javax.swing.text.Segment;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * A document to represent text in the form of the java programming language.
 * This is quite primitive in that it simply provides support for lexically
 * analyzing the text.
 */
public class SchemeDocument extends PlainDocument {

	// The Undo/Redo manager
	protected UndoManager undoManager = new UndoManager();

	public SchemeDocument(JComponent pane) {
		super(new GapContent(1024));
		this.pane = pane;

		addUndoableEditListener(new UndoHandler());
		addDocumentListener(new DocumentHandler());
	}

	/*
	 * Fully repainted when text document gets changed
	 */
	protected JComponent pane;

	/**
	 * Create a lexical analyzer for this document
	 */
	public TokenScanner createScanner() {
		return new TokenScanner(new DocumentInputStream(0, getLength()));
	}

	// Undo
	public void undo() {
		try {
			undoManager.undo();
		} catch (CannotUndoException cue) {
		}
	}

	// Redo
	public void redo() {
		try {
			undoManager.redo();
		} catch (CannotUndoException cue) {
			cue.printStackTrace();
		}
	}

	/*
	 * Handle undo/redo events
	 */
	protected class UndoHandler implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent e) {
			undoManager.addEdit(e.getEdit());
		}
	}

	/*
	 * Handle insertion/deletion events
	 */
	protected class DocumentHandler implements DocumentListener {
		/*
		 * Handle changes to the text field
		 */
		public void changedUpdate(DocumentEvent event) {
			pane.repaint();
		}

		/*
		 * Handle insertion to the text field
		 */
		public void insertUpdate(DocumentEvent event) {
			pane.repaint();
		}

		/*
		 * Handle removal to the text field
		 */
		public void removeUpdate(DocumentEvent event) {
			pane.repaint();
		}
	}

	/**
	 * Class to provide InputStream functionality from a portion of a Document.
	 * This really should be a Reader, but not enough things use it yet.
	 */
	protected class DocumentInputStream extends InputStream {

		public DocumentInputStream(int p0, int p1) {
			this.segment = new Segment();
			this.p0 = p0;
			this.p1 = Math.min(getLength(), p1);
			this.pos = p0;
			try {
				loadSegment();
			} catch (IOException ioe) {
				throw new Error("Unexpected: " + ioe);
			}
		}

		/**
		 * Reads the next byte of data from this input stream. The value byte is
		 * returned as an int in the range 0 to 255. If no byte is available
		 * because the end of the stream has been reached, the value -1 is
		 * returned. This method blocks until input data is available, the end
		 * of the stream is detected, or an exception is thrown.
		 */
		public int read() throws IOException {
			if (index >= segment.offset + segment.count) {
				if (pos >= p1)
					return -1; // no more data
				loadSegment();
			}
			return segment.array[index++];
		}

		protected void loadSegment() throws IOException {
			try {
				int n = Math.min(1024, p1 - pos);
				getText(pos, n, segment);
				pos += n;
				index = segment.offset;
			} catch (BadLocationException e) {
				throw new IOException("Bad location");
			}
		}

		protected Segment segment;

		protected int index; // index into array of the segment

		protected int p0; // start position

		protected int p1; // end position

		public int pos; // pos in document
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
