package jist.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * The Find/Replace dialog
 */
public class FindReplaceDialog extends JDialog {
	protected JSEditorPane owner;

	protected JTabbedPane tabs;

	protected JTextField findField;

	protected JTextField replaceField;

	protected Document findDoc;

	protected Document replaceDoc;

	protected ButtonModel modelWord;

	protected ButtonModel modelCase;

	protected ButtonModel modelUp;

	protected ButtonModel modelDown;

	protected int searchIndex = -1;

	protected boolean searchUp = false;

	protected String searchData;

	protected final static int REPLACED_TEXT = 2;

	// used for counting the number of replacements.
	protected final static int REPLACED_TEXT_AND_CANCEL = 3;

	// if text was replaced and user cancelled wrap around

	public FindReplaceDialog(JSEditorPane owner, int index) {
		super((Frame) null, "Find and Replace", false);
		this.owner = owner;

		tabs = new JTabbedPane();

		// Find panel
		JPanel p1 = new JPanel(new BorderLayout());
		JPanel pc1 = new JPanel(new BorderLayout());

		JPanel findWaht = new JPanel();
		findWaht.setLayout(new DialogLayout(20, 5));
		findWaht.setBorder(new EmptyBorder(8, 8, 8, 0));

		findWaht.add(new JLabel("Find:"));

		findField = new JTextField();
		findDoc = findField.getDocument();
		findWaht.add(findField);

		pc1.add(findWaht, BorderLayout.CENTER);

		JPanel options = new JPanel(new GridLayout(2, 2, 8, 2));
		options.setBorder(new TitledBorder(new EtchedBorder(), "Options"));

		JCheckBox chkWord = new JCheckBox("Whole words only");
		chkWord.setMnemonic('w');
		modelWord = chkWord.getModel();
		options.add(chkWord);

		ButtonGroup bg = new ButtonGroup();
		JRadioButton rdUp = new JRadioButton("Search up");
		rdUp.setMnemonic('u');
		modelUp = rdUp.getModel();
		bg.add(rdUp);
		options.add(rdUp);

		JCheckBox chkCase = new JCheckBox("Match case");
		chkCase.setMnemonic('c');
		modelCase = chkCase.getModel();
		options.add(chkCase);

		JRadioButton rdDown = new JRadioButton("Search down", true);
		rdDown.setMnemonic('d');
		modelDown = rdDown.getModel();
		bg.add(rdDown);
		options.add(rdDown);

		pc1.add(options, BorderLayout.SOUTH);
		p1.add(pc1, BorderLayout.CENTER);

		JPanel buttons1 = new JPanel(new FlowLayout());
		JPanel p = new JPanel(new GridLayout(2, 1, 2, 8));

		ActionListener findAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findNext(false, true);
			}
		};
		JButton btFind = new JButton(" Find Next  ");
		btFind.addActionListener(findAction);
		btFind.setMnemonic('f');
		p.add(btFind);

		ActionListener closeAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		};
		JButton btClose = new JButton("Close");
		btClose.addActionListener(closeAction);
		btClose.setDefaultCapable(true);
		p.add(btClose);

		buttons1.add(p);
		p1.add(buttons1, BorderLayout.EAST);

		tabs.addTab("Find", p1);

		// "Replace" panel
		JPanel p2 = new JPanel(new BorderLayout());
		JPanel pc2 = new JPanel(new BorderLayout());

		JPanel replaceWhat = new JPanel();
		replaceWhat.setLayout(new DialogLayout(20, 5));
		replaceWhat.setBorder(new EmptyBorder(8, 8, 8, 0));

		replaceWhat.add(new JLabel("Find:"));
		replaceField = new JTextField();
		replaceField.setDocument(findDoc);
		replaceWhat.add(replaceField);

		replaceWhat.add(new JLabel("Replace:"));
		JTextField txtReplace = new JTextField();
		replaceDoc = txtReplace.getDocument();
		replaceWhat.add(txtReplace);

		pc2.add(replaceWhat, BorderLayout.CENTER);

		options = new JPanel(new GridLayout(2, 2, 8, 2));
		options.setBorder(new TitledBorder(new EtchedBorder(), "Options"));

		chkWord = new JCheckBox("Whole words only");
		chkWord.setMnemonic('w');
		chkWord.setModel(modelWord);
		options.add(chkWord);

		bg = new ButtonGroup();
		rdUp = new JRadioButton("Search up");
		rdUp.setMnemonic('u');
		rdUp.setModel(modelUp);
		bg.add(rdUp);
		options.add(rdUp);

		chkCase = new JCheckBox("Match case");
		chkCase.setMnemonic('c');
		chkCase.setModel(modelCase);
		options.add(chkCase);

		rdDown = new JRadioButton("Search down", true);
		rdDown.setMnemonic('d');
		rdDown.setModel(modelDown);
		bg.add(rdDown);
		options.add(rdDown);

		pc2.add(options, BorderLayout.SOUTH);
		p2.add(pc2, BorderLayout.CENTER);

		JPanel buttons2 = new JPanel(new FlowLayout());
		p = new JPanel(new GridLayout(3, 1, 2, 8));

		ActionListener replaceAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findNext(true, true);
			}
		};
		JButton btReplace = new JButton("Replace");
		btReplace.addActionListener(replaceAction);
		btReplace.setMnemonic('r');
		p.add(btReplace);

		ActionListener replaceAllAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int counter = 0;
				while (true) {
					int result = findNext(true, false);
					if (result < 0)
						return; // error
					else if (result == REPLACED_TEXT_AND_CANCEL) {
						counter++;
						break;
					} else if (result == 0)
						break; // no more
					else if (result == REPLACED_TEXT)
						counter++;
				}
				info(counter + " replacement(s) have been done");
			}
		};
		JButton btReplaceAll = new JButton("Replace All");
		btReplaceAll.addActionListener(replaceAllAction);
		btReplaceAll.setMnemonic('a');
		p.add(btReplaceAll);

		btClose = new JButton("Close");
		btClose.addActionListener(closeAction);
		btClose.setDefaultCapable(true);
		p.add(btClose);
		buttons2.add(p);
		p2.add(buttons2, BorderLayout.EAST);

		// Make button columns the same size
		buttons1.setPreferredSize(buttons2.getPreferredSize());

		tabs.addTab("Replace", p2);
		tabs.setSelectedIndex(index);

		getContentPane().add(tabs, BorderLayout.CENTER);

		WindowListener flst = new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				searchIndex = -1;
				if (tabs.getSelectedIndex() == 0)
					findField.grabFocus();
				else
					replaceField.grabFocus();
			}

			public void windowDeactivated(WindowEvent e) {
				searchData = null;
			}
		};
		addWindowListener(flst);

		pack();
		setResizable(false);
	}

	public void setSelectedIndex(int index) {
		tabs.setSelectedIndex(index);
		setVisible(true);
		searchIndex = -1;
	}

	public int findNext(boolean doReplace, boolean showWarnings) {
		int pos = owner.getCaretPosition();
		int docLen = -1; //The length of the document.
		boolean wrappingText = false; //If the text is wrapping.
		boolean replaced = false; //If text was replaced, then true else false

		//if searchUp button is selected, set searchUp boolean to true
		if (modelUp.isSelected() != searchUp) {
			searchUp = modelUp.isSelected();
		}
		searchIndex = -1;

		if (searchIndex == -1) {
			try {
				Document doc = owner.getDocument();
				docLen = doc.getLength();
				if (searchUp)
					searchData = doc.getText(0, pos);
				else
					searchData = doc.getText(pos, doc.getLength() - pos);
				searchIndex = pos;
			} catch (BadLocationException ex) {
				warning(ex.toString());
				return -1;
			}
		}

		String key = "";
		try {
			key = findDoc.getText(0, findDoc.getLength());
		} catch (BadLocationException ex) {
		}
		if (key.length() == 0) {
			warning("Please enter the target to search");
			return -1;
		}

		//Handle matchCase option
		if (!modelCase.isSelected()) {
			searchData = searchData.toLowerCase();
			key = key.toLowerCase();
		}

		//handle wholeWordsOnly Option
		if (modelWord.isSelected()) {
			for (int k = 0; k < TextUtils.WORD_SEPARATORS.length; k++) {
				if (key.indexOf(TextUtils.WORD_SEPARATORS[k]) >= 0) {
					warning("The text target contains an illegal "
							+ "character \'" + TextUtils.WORD_SEPARATORS[k]
							+ "\'");
					return -1;
				}
			}
		}

		String replacement = "";
		if (doReplace) {
			try {
				replacement = replaceDoc.getText(0, replaceDoc.getLength());
			} catch (BadLocationException ex) {
			}
		}

		int xStart = -1;
		int xFinish = -1;

		while (true) {

			if (searchUp)
				xStart = searchData.lastIndexOf(key, pos - 1);
			else
				xStart = searchData.indexOf(key, pos - searchIndex);

			//WrapAround or Text not found
			if (xStart < 0) {
				wrappingText = true;
				if (searchData.length() == docLen) {
					if (showWarnings)
						warning("Text not found");
					return 0;
				}

			}

			xFinish = xStart + key.length();

			if (modelWord.isSelected()) {
				boolean s1 = xStart > 0;
				boolean b1 = s1
						&& !TextUtils
								.isSeparator(searchData.charAt(xStart - 1));
				boolean s2 = xFinish < searchData.length();
				boolean b2 = s2
						&& !TextUtils.isSeparator(searchData.charAt(xFinish));

				if (b1 || b2) { // Not a whole word
					if (searchUp && s1) { // Can continue up
						pos = xStart;
						continue;
					}
					if (!searchUp && s2) { // Can continue down
						pos = xFinish;
						continue;
					}
					// Found, but not a whole word, and we cannot continue
					if (showWarnings)
						warning("Text not found");
					return 0;
				}
			}
			break;
		}

		if (!searchUp) {
			xStart += searchIndex;
			xFinish += searchIndex;
		}

		if (doReplace) {

			String selectedText = owner.getSelectedText();

			//If some text was found and this text is equal to the key then
			// replace it.
			if (selectedText != null && selectedText.equals(key)) {
				owner.replaceSelection(replacement); //replace text

				replaced = true;
				if (!searchUp)
					xStart += replacement.length() - key.length();
			}
			if (!wrappingText)
				owner.setSelection(xStart, xStart + key.length(), searchUp);
			//Select the next field to replace

		} else if (!wrappingText)
			owner.setSelection(xStart, xFinish, searchUp);

		//if need to wrapText, then set the caret position at the start or end
		// of Document.
		if (wrappingText) {
			int userOption = JOptionPane.showConfirmDialog(null,
					"End of Text Reached Wrapping Around. Click OK. ",
					"Warning", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (userOption == JOptionPane.CANCEL_OPTION) {
				//if user cancels, then exit.
				if (replaced == true)
					return REPLACED_TEXT_AND_CANCEL;
				else
					return 0;
			} else if (searchUp)
				owner.setCaretPosition(owner.getDocument().getLength());
			else if (!searchUp)
				owner.setCaretPosition(0);
		}
		return replaced ? REPLACED_TEXT : 1;
	}

	protected void info(String message) {
		JOptionPane.showMessageDialog(null, message, "Info",
				JOptionPane.INFORMATION_MESSAGE);
	}

	protected void warningAndChoice(JOptionPane pane, String message) {
		JOptionPane.showConfirmDialog(null, message, "Warning",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	}

	protected void warning(String message) {
		JOptionPane.showMessageDialog(null, message, "Warning",
				JOptionPane.INFORMATION_MESSAGE);
	}
}

class DialogLayout implements LayoutManager {
	protected int m_divider = -1;

	protected int m_hGap = 10;

	protected int m_vGap = 5;

	public DialogLayout() {
	}

	public DialogLayout(int hGap, int vGap) {
		m_hGap = hGap;
		m_vGap = vGap;
	}

	public void addLayoutComponent(String name, Component comp) {
	}

	public void removeLayoutComponent(Component comp) {
	}

	public Dimension preferredLayoutSize(Container parent) {
		int divider = getDivider(parent);

		int w = 0;
		int h = 0;
		for (int k = 1; k < parent.getComponentCount(); k += 2) {
			Component comp = parent.getComponent(k);
			Dimension d = comp.getPreferredSize();
			w = Math.max(w, d.width);
			h += d.height + m_vGap;
		}
		h -= m_vGap;

		Insets insets = parent.getInsets();
		return new Dimension(divider + w + insets.left + insets.right, h
				+ insets.top + insets.bottom);
	}

	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}

	public void layoutContainer(Container parent) {
		int divider = getDivider(parent);

		Insets insets = parent.getInsets();
		int w = parent.getWidth() - insets.left - insets.right - divider;
		int x = insets.left;
		int y = insets.top;

		for (int k = 1; k < parent.getComponentCount(); k += 2) {
			Component comp1 = parent.getComponent(k - 1);
			Component comp2 = parent.getComponent(k);
			Dimension d = comp2.getPreferredSize();

			comp1.setBounds(x, y, divider, d.height);
			comp2.setBounds(x + divider, y, w, d.height);
			y += d.height + m_vGap;
		}
	}

	public int getHGap() {
		return m_hGap;
	}

	public int getVGap() {
		return m_vGap;
	}

	public void setDivider(int divider) {
		if (divider > 0)
			m_divider = divider;
	}

	public int getDivider() {
		return m_divider;
	}

	protected int getDivider(Container parent) {
		if (m_divider > 0)
			return m_divider;

		int divider = 0;
		for (int k = 0; k < parent.getComponentCount(); k += 2) {
			Component comp = parent.getComponent(k);
			Dimension d = comp.getPreferredSize();
			divider = Math.max(divider, d.width);
		}
		divider += m_hGap;
		return divider;
	}

	public String toString() {
		return getClass().getName() + "[hgap=" + m_hGap + ",vgap=" + m_vGap
				+ ",divider=" + m_divider + "]";
	}
}

class TextUtils {
	public static final char[] WORD_SEPARATORS = { ' ', '\t', '\n', '\r', '\f',
			'.', ',', ':', '-', '(', ')', '[', ']', '{', '}', '<', '>', '/',
			'|', '\\', '\'', '\"' };

	public static boolean isSeparator(char ch) {
		for (int k = 0; k < WORD_SEPARATORS.length; k++)
			if (ch == WORD_SEPARATORS[k])
				return true;
		return false;
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
