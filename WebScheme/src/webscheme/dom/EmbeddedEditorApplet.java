/**
 * EmbeddedEditorApplet
 * 
 * @author Turadg
 */

package webscheme.dom;

import java.awt.BorderLayout;

import javax.swing.JApplet;

import jist.editor.JSEditorPane;

public class EmbeddedEditorApplet extends JApplet implements ObjectElement {

	JSEditorPane buffer = new JSEditorPane();

	/*
	 * (non-Javadoc)
	 * 
	 * @see webscheme.dom.ObjectElement#getRestoreMethod(java.lang.String)
	 */
	public String getRestoreMethod(String attr) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getString() {
		System.err.println("EmbeddedEditorApplet.getString() called");
		return buffer.getText();
	}

	public void init() {
		getContentPane().add(buffer, BorderLayout.CENTER);
		String startValue = getParameter("startValue");
		if (startValue != null)
			buffer.setText(startValue);
	}

	public void setString(String s) {
		buffer.setText(s);
	}

}