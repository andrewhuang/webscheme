package jist.listener;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * SchemeConsoleFrame
 * 
 * Swing JFrame that contains the Scheme console and buttons
 * 
 * @author Turadg
 */
public class SchemeConsoleFrame extends JFrame {

	protected InputStream in;

	protected OutputStream out;

	ScfTextArea textArea;

	public SchemeConsoleFrame() {
		super("Scheme Console");

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 10, 5));

		JButton clearButton = new JButton("Clear");
		AbstractAction clearAction = new AbstractAction("Clear") {
			public void actionPerformed(ActionEvent e) {
				textArea.clear();
			}
		};
		clearButton.addActionListener(clearAction);
		topPanel.add(clearButton);

		JButton copyButton = new JButton("Copy");
		AbstractAction copyAction = new AbstractAction("Copy") {
			public void actionPerformed(ActionEvent e) {
				textArea.asJTextArea().copy();
			}
		};
		copyButton.addActionListener(copyAction);
		topPanel.add(copyButton);

		JButton closeButton = new JButton("Close");
		AbstractAction closeAction = new AbstractAction("Close") {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		};
		closeButton.addActionListener(closeAction);
		topPanel.add(closeButton);

		textArea = new ScfTextArea(this);
		textArea.asJTextArea().setEditable(false);
		in = textArea.in;
		out = textArea.out;

		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(textArea.asJTextArea(), BorderLayout.CENTER);
		setSize(600, 300);
	}

}

class ScfTextArea {
	SchemeConsoleFrame scf;

	JTextPane textArea;

	ProtectedDocument doc;

	InputStream in;

	OutputStream out;

	int readMarker;

	SimpleAttributeSet attrWritten, attrRead;

	public ScfTextArea(SchemeConsoleFrame frame) {
		scf = frame;

		textArea = new JTextPane();
		doc = new ProtectedDocument();
		textArea.setDocument(doc);

		readMarker = 0;
		attrRead = new SimpleAttributeSet();
		attrWritten = new SimpleAttributeSet();
		StyleConstants.setBold(attrWritten, true);

		in = new ScfInputStream();
		out = new ScfOutputStream();

		textArea.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					synchronized (textArea) {
						if (!textArea.isEditable())
							return;

						// Assume that the last line is the only that is ever
						// pending
						// Otherwise interleaved reads and writes can cause
						// problems

						// Therefore, enter never splits lines
						e.consume();
						try {
							doc.insertString(doc.getLength(), "\n", null);
						} catch (BadLocationException ex) {
						}
						// textArea.setCaretPosition(doc.getLength());

						// Need to do this here too in case the listener
						// thread is woken up instead of the console thread
						textArea.setEditable(false);

						textArea.notify();
					}
				}
			}

			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
			}
		});
	}

	public void clear() {
		readMarker = 0;
		textArea.setText("");
	}

	public JTextPane asJTextArea() {
		return textArea;
	}

	class ProtectedDocument extends DefaultStyledDocument {
		public void insertString(int offset, String s, AttributeSet attrSet)
				throws BadLocationException {
			if (offset < readMarker)
				return;
			super.insertString(offset, s, attrSet);
		}

		public void remove(int offset, int length) throws BadLocationException {
			if (offset < readMarker)
				return;
			super.remove(offset, length);
		}
	}

	class ScfInputStream extends InputStream {

		public int read() {
			byte[] buf = new byte[1];
			if (read(buf, 0, 1) == 0)
				return -1;
			return buf[0];
		}

		public int available() {
			synchronized (textArea) {
				return doc.getLength() - readMarker;
			}
		}

		public int read(byte[] buf, int offset, int count) {
			int bytesRead = 0;
			synchronized (textArea) {
				// Only do this stuff once if there is no data, we don't want to
				// keep
				// switching the user back to this window if the user selects
				// another
				// one.
				if (available() == 0) {
					if (!scf.isVisible())
						scf.setVisible(true);
					scf.requestFocus();
					textArea.requestFocus();
					textArea.setCaretPosition(doc.getLength());
				}
				while (available() == 0) {
					textArea.setEditable(true);
					try {
						textArea.wait();
					} catch (InterruptedException e) {
					}
					textArea.setEditable(false);
				}
				bytesRead = Math.min(count, available());
				String text = "";
				try {
					text = doc.getText(readMarker, bytesRead);
				} catch (BadLocationException e) {
				}
				doc.setCharacterAttributes(readMarker, bytesRead, attrRead,
						true);
				for (int i = 0; i < bytesRead; i++) {
					buf[offset + i] = (byte) text.charAt(i);
					readMarker++;
				}
			}
			return bytesRead;
		}

		public void close() {
			// Does nothing for now
		}
	}

	class ScfOutputStream extends OutputStream {

		public void write(int c) {
			byte[] buf = new byte[1];
			buf[0] = (byte) c;
			write(buf, 0, 1);
		}

		public void write(byte[] buf, int offset, int count) {
			if (!scf.isVisible())
				scf.setVisible(true);
			String s = new String(buf, offset, count);
			try {
				doc.insertString(doc.getLength(), s, null);
			} catch (BadLocationException ex) {
			}
			doc.setCharacterAttributes(readMarker, s.length(), attrWritten,
					true);
			readMarker += s.length();
			doc.setParagraphAttributes(doc.getLength(), 0, attrRead, true);
		}

		public void close() {
			// Does nothing for now
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
