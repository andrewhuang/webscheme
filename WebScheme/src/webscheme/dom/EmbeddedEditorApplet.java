/**
   EmbeddedEditorApplet
  
   @author Turadg
*/

package webscheme.dom;

import java.awt.*;
import javax.swing.*;

import jist.editor.*;

public class EmbeddedEditorApplet extends JApplet implements ObjectElement {

	JSEditorPane buffer = new JSEditorPane();

	public void init() {
		getContentPane().add(buffer, BorderLayout.CENTER);
		String startValue = getParameter("startValue");
		if (startValue != null)
			buffer.setText(startValue);
	}

	public void setString(String s) {
		buffer.setText(s);
	}

	public String getString() {
		return buffer.getText();
	}

}
