package jist.editor;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * An interface class that help to set attributes for the Scheme context
 */
public class JSEditorPane extends AbstractSchemeEditorPane {

	/**
	 * A listener that handles ')' key event
	 */
	protected void processKeyEvent(KeyEvent ke) {
		// on tab, indent
		if (ke.getID() == KeyEvent.KEY_PRESSED
				&& ke.getKeyCode() == KeyEvent.VK_TAB) {
			indentLine();
			ke.consume();
		}

		// on enter, newline and indent
		if (ke.getID() == KeyEvent.KEY_PRESSED
				&& ke.getKeyCode() == KeyEvent.VK_ENTER) {
			indentReturn();
			ke.consume();
		}

		super.processKeyEvent(ke);

		if (ke.getID() == KeyEvent.KEY_RELEASED && ke.getKeyChar() == ')') {
			parensFlash();
		}
	}

	/*
	 * A listener that mouse double-clicking event
	 */
	protected void processMouseEvent(MouseEvent me) {
		super.processMouseEvent(me);

		if (me.getID() == MouseEvent.MOUSE_CLICKED && me.getClickCount() == 2) {
			int pos = getCaretPosition();
			// If it's a ')' on the left, then move left 1 character, so we can
			// select the s-expr to the left
			if (styles.getScanner().getToken(pos - 2) == Token.T_12
					.getScanValue())
				pos--;

			int start = findSExpressionStart(pos - 2);
			int end = findSExpressionEnd(start + 1);

			if (start >= 0)
				setSelectionStart(start);
			else
				setSelectionStart(0);
			if (end >= 0)
				setSelectionEnd(end);
			else
				setSelectionEnd(getDocument().getLength() - 1);
			paintImmediately(0, 0, getWidth(), getHeight());
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
