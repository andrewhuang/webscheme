package jist.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;

import jist.env.JistStore;
import jist.io.WiserFile;
import jist.io.WiserFileSystem;
import jist.listener.SchemeConsoleFrame;
import jist.listener.SchemeListener;
import jist.listener.SchemeListenerPane;

/**
 * editor applet
 */
public class SchemeEditor extends JFrame {

	static final String WINDOW_TITLE = "Scheme Editor";

	static final String UNIX_LINE_SEPARATOR = "\r\n";

	/** IO connection to server */
	WiserFileSystem wfs;

	/** Current file being edited */
	File currentFile;

	/** prefix for untitled files */
	final static String defaultPrefix = "Untitled";

	/** The buffer */
	JSEditorPane buffer = new JSEditorPane();

	/** Find/Replace dialog */
	FindReplaceDialog findDialog;

	/** the status bar */
	final private JLabel statusBar;

	boolean saved;

	boolean autosaved;

	/** The Timer and associated fields used in AutoSaving */
	static final int AUTOSAVE_INTERVAL = 5000 * 60; // 5 minutes

	Timer timer;

	File autosaveFile;

	/** The font that is used by this buffer */
	Font bufferFont;

	int bufferFontSize = 14;

	int smallestFontSize = 8;

	int largestFontSize = 40;

	String[] fontTypes = { "Courier", "Ariel", "Serif", "SansSerif" };

	/** for sending text to listener */
	boolean VERBOSE = false;

	/** the file chooser */
	JFileChooser remoteFileChooser;

	int untitledNum = 0;

	/** Menu actions */
	Action actionNew;

	Action actionOpenRemote;

	Action actionOpenLocal;

	Action actionSave;

	Action actionSaveAs;

	Action actionClose;

	Action actionUndo;

	Action actionRedo;

	Action actionCopy;

	Action actionCut;

	Action actionPaste;

	Action actionSelectAll;

	Action actionFind;

	Action actionFindNext;

	Action actionReplace;

	Action actionIndent;

	Action actionSize; //select Font size actions

	Action actionFontType; //select font type

	Action actionEvalSexpresion;

	Action actionEvalSelection;

	Action actionEvalAll;

	Action actionSetVerbose;

	Action actionViewListener;

	Action actionViewConsole;

	public SchemeEditor() {
		super(WINDOW_TITLE);

		// handle close box
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				close();
			}
		});

		// handle text modifications
		buffer.getDocument().addDocumentListener(new DocumentTracker());

		setJMenuBar(createMenuBar());

		getContentPane().setLayout(new BorderLayout());

		statusBar = new JLabel(" ");
		getContentPane().add(statusBar, BorderLayout.SOUTH);

		try {
			// buffer
			bufferFont = new Font("Courier", Font.BOLD, 14);

			JScrollPane scroller = new JScrollPane();
			scroller.getViewport().add(buffer);

			getContentPane().add(scroller, BorderLayout.CENTER);

			setSize(600, 600);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public JSEditorPane getBuffer() {
		return buffer;
	}

	public void setVisible(boolean b) {
		if (currentFile == null)
			super.setVisible(false); // if there is no file, don't show editor
		else
			super.setVisible(b); // if there is, do as normal
	}

	protected void setStatus(String s) {
		statusBar.setText(s);
	}

	/**
	 * indicate that the current file has been modified
	 */
	protected void touch() {
		saved = false;
		autosaved = false;
	}

	public void setRemoteFileSystem(WiserFileSystem wfs) {
		this.wfs = wfs;

		saved = true;
		autosaved = true;

		try {
			// FIX constructing this JFileChooser causes an OutOfMemoryError
            //			FileSystemView fsv = new WiserFileSystemView(wfs);
			//            remoteFileChooser = new JFileChooser(fsv);
		} catch (java.security.AccessControlException ex) {
			System.err.println("No permission to use file chooser");
		}

		//initialize the timer
		initTimer();
	}

	public WiserFileSystem getFileSystem() {
		return wfs;
	}

	void initTimer() {
		timer = new Timer(AUTOSAVE_INTERVAL, new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (autosaved == false) {
					autosaved = true;
					String data = buffer.getText();
					autosaveFile = new WiserFile(wfs, ".#"
							+ currentFile.getName() + "#");
					try {
						wfs.put(autosaveFile, data.getBytes());
					} catch (IOException ex) {
						System.err.println("autosave caught: " + ex);
					}
					setStatus("Autosaved " + currentFile.getName());
				}
			}
		});
		timer.start();
	}

	/**
	 * Prompt a simple file input dialog.
	 * 
	 * @return the file name or null on cancel.
	 */
	String promptFileInputDialog() {
		String path;
		while (true) {
			path = JOptionPane.showInputDialog(SchemeEditor.this,
					"New file name: ", "New", JOptionPane.PLAIN_MESSAGE);
			if (path != null) {
				if (((path = path.trim())).equals(""))
					continue;
				else
					break;
			}

			path = null;
			break;
		}
		return path;
	}

	/** @return false if user cancels */
	public boolean newFile() {
		String newPath = promptFileInputDialog();

		if (newPath == null)
			return false; // no new file to make
		boolean writeOk = checkOverwrite(newPath);
		if (!writeOk)
			return false; // user chose not to overwrite that file

		deleteAutosaved(currentFile);
		buffer.setText("");
		saved = true;
		setCurrentFile(new WiserFile(wfs, newPath));
		return true;
	}

	void setCurrentFile(File file) {
		currentFile = file;
		if (currentFile == null)
			setTitle(WINDOW_TITLE);
		else
			setTitle(currentFile.getName() + " - " + WINDOW_TITLE);
	}

	public File getCurrentFile() {
		return currentFile;
	}

	public void openFile(File file) throws IOException {
		if (file instanceof WiserFile)
			openRemoteFile((WiserFile) file);
		else
			openLocalFile(file);
	}

	public void openLocalFile(File file) throws IOException {
		setStatus("Opening: " + file.getName());

		BufferedReader in = new BufferedReader(new FileReader(file));
		StringBuffer data = new StringBuffer();
		for (String line = in.readLine(); line != null; line = in.readLine()) {
			data.append(line);
			data.append(UNIX_LINE_SEPARATOR);
		}
		in.close();
		buffer.setText(data.toString());

		saved = true;
		setCurrentFile(file);
		System.out.println(" .. success");
		setStatus(null);
	}

	public void openRemoteFile(WiserFile mainFile) throws FileNotFoundException {
		System.out.println("Attempting to load '" + mainFile + "'");

		WiserFile toLoad = mainFile;

		// first, try to recover autosave version of file
		try {
			WiserFile autosaveFile = getAutosaveFile(mainFile);
			wfs.get(autosaveFile);
			// if we get to this point, that means wfs.get() didn't
			// throw FileNotFoundException and the file exists
			int choice = JOptionPane
					.showConfirmDialog(
							SchemeEditor.this,
							"A previous version of this file has been found. Do you want open the autosaved file?");
			if (choice == JOptionPane.YES_OPTION)
				toLoad = autosaveFile;
		} catch (FileNotFoundException ex) {
			// this is fine, toLoad stays as mainFile
			System.err.println("autosave not found: " + ex);
		}

		setStatus("Opening: " + toLoad.getName());
		String data = wfs.get(toLoad); // load the data from toLoad
		buffer.setText(data);
		saved = true;
		setCurrentFile(mainFile); // but call the file as passed in
		System.out.println(" .. success");
		setStatus(null);
	}

	void saveFile(File file) throws IOException {
		if (file instanceof WiserFile)
			saveRemoteFile((WiserFile) file);
		else
			saveLocalFile(file);
	}

	void saveLocalFile(File file) throws IOException {
		System.out.println("saving to file " + file);
		String data = buffer.getText();

		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		out.write(data, 0, data.length());
		out.close();

		setStatus("Saved " + file.toString());
		saved = true;
	}

	void saveRemoteFile(WiserFile file) throws IOException {
		System.out.println("saving to file " + file);
		String data = buffer.getText();

		wfs.put(file, data.getBytes());

		setStatus("Saved " + file.toString());
		saved = true;
	}

	void close() {
		boolean proceed = checkKillBuffer();
		if (proceed) {
			buffer.setText("");
			setCurrentFile(null); // really close file
			dispose();
		}
	}

	/**
	 * called by the applet's destroy() method unlike checkKillBuffer, doesn't
	 * give CANCEL option
	 */
	public void destroy() {
		if (currentFile == null)
			return; // no file to check on

		// close silently if changes are saved
		if (saved) {
			deleteAutosaved(currentFile);
			return;
		}

		// otherwise, offer to save
		int choice = JOptionPane.showConfirmDialog(this, "The text in "
				+ currentFile.getName()
				+ " has changed.\nDo you want to save the changes?",
				"Save changes?", JOptionPane.YES_NO_OPTION);
		switch (choice) {
		case JOptionPane.NO_OPTION:
			deleteAutosaved(currentFile);
			return;
		case JOptionPane.YES_OPTION:
			try {
				saveFile(currentFile);
				deleteAutosaved(currentFile);
			} catch (Exception ex) {
				alertSaveFailure(ex);
			}
			return;
		}

		return; // should never be reached
	}

	/**
	 * 
	 * @return false if user cancels
	 */
	public boolean checkKillBuffer() {
		if (currentFile == null)
			return true; // no file to check on

		// close silently if changes are saved
		if (saved) {
			deleteAutosaved(currentFile);
			return true;
		}

		// otherwise, offer to save
		int choice = JOptionPane.showConfirmDialog(this, "The text in "
				+ currentFile.getName()
				+ " has changed.\nDo you want to save the changes?",
				"Save changes?", JOptionPane.YES_NO_CANCEL_OPTION);
		switch (choice) {
		case JOptionPane.NO_OPTION:
			deleteAutosaved(currentFile);
			return true;
		case JOptionPane.YES_OPTION:
			try {
				saveFile(currentFile);
				deleteAutosaved(currentFile);
			} catch (Exception ex) {
				alertSaveFailure(ex);
			}
			return true;
		case JOptionPane.CANCEL_OPTION:
			return false;
		// tell calling procedure that the user chose to cancel
		}

		return false; // should never be reached
	}

	/**
	 * @param ex
	 */
	void alertSaveFailure(Exception ex) {
		System.err.println(ex);
		setStatus("Error saving file");
		JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Failed",
				JOptionPane.ERROR_MESSAGE);
	}

	void deleteAutosaved(File file) {
		if (file == null)
			return;

		try {
			WiserFile autosaveFile = getAutosaveFile(file);
			wfs.delete(autosaveFile);
		} catch (Exception ex) {
			System.err.println("Error deleting autosave file :: " + ex);
		}
	}

	WiserFile nextUntitledFile() {
		Vector filenames = wfs.listFiles(new WiserFile(wfs, "/"));

		if (filenames != null) {
			String fileName;

			for (int i = 0; i < filenames.size(); ++i) {
				// listFiles returns a Vector of *Strings* not Files
				// this was causing a ClassCastException
				//fileName = ((File) files.elementAt(i)).getName();

				fileName = (String) filenames.elementAt(i);

				if (fileName.startsWith(defaultPrefix)) {
					String fileNum = fileName.substring(defaultPrefix.length());
					try {
						int num = Integer.parseInt(fileNum);
						if (num > untitledNum)
							untitledNum = num;
					} catch (NumberFormatException e) {
						// do nothing
					}
				}
			}
			untitledNum++;
		}

		// set up current file
		return new WiserFile(wfs, defaultPrefix + untitledNum);
	}

	// returns true if overwrite is ok
	boolean checkOverwrite(String path) {
		return checkOverwrite(new WiserFile(wfs, path));
	}

	boolean checkOverwrite(File file) {
		if (file.exists()) {
			int choice = JOptionPane.showConfirmDialog(this,
					"Overwrite existing file: " + file.getName() + "?",
					"Confirm overwrite", JOptionPane.YES_NO_OPTION);
			if (choice == JOptionPane.NO_OPTION) {
				return false;
			}
		}
		return true; // ok to write since file doesn't exist yet
	}

	// Create the menu bar
	JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenuItem item;

		// File menu
		JMenu mFile = new JMenu("File");
		mFile.setMnemonic('f');

		// File:New
		actionNew = new AbstractAction("New") {

			public void actionPerformed(ActionEvent e) {
				boolean proceed = checkKillBuffer();
				if (proceed)
					newFile();
			}
		};

		item = mFile.add(actionNew);
		item.setMnemonic('n');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				KeyEvent.CTRL_MASK));

		// File:OpenRemote
		actionOpenRemote = new AbstractAction("Open Remote") {

			public void actionPerformed(ActionEvent e) {
				boolean proceed = checkKillBuffer();
				if (!proceed)
					return;

				File toOpen = null;

				try { // to use JFileChooser
					if (remoteFileChooser == null)
						throw new AccessControlException("No permissions");
					int returnVal = remoteFileChooser
							.showOpenDialog(SchemeEditor.this);
					if (returnVal == JFileChooser.APPROVE_OPTION)
						toOpen = remoteFileChooser.getSelectedFile();
				} catch (java.security.AccessControlException ex) {
					// use simple input box if necessary
					String filepath = JOptionPane.showInputDialog(
							SchemeEditor.this, "Open ", "Open File",
							JOptionPane.PLAIN_MESSAGE);
					if (filepath != null)
						toOpen = new WiserFile(wfs, filepath);
				}

				if (toOpen == null)
					return; // nothing chosen to open

				try { // to open the specified file
					openFile(toOpen);
				} catch (FileNotFoundException ex) {
					setStatus("File '" + toOpen + "' not found");
				} catch (Exception ex) {
					setStatus("Error reading file " + toOpen);
				}
			}
		};

		item = mFile.add(actionOpenRemote);
		item.setMnemonic('o');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				KeyEvent.CTRL_MASK));

		// File:OpenLocal
		actionOpenLocal = new AbstractAction("Open Local") {

			public void actionPerformed(ActionEvent e) {
				boolean proceed = checkKillBuffer();
				if (!proceed)
					return;

				File toOpen = null;

				try { // to use JFileChooser
					JFileChooser localFileChooser = new JFileChooser();
					int returnVal = localFileChooser
							.showOpenDialog(SchemeEditor.this);
					if (returnVal == JFileChooser.APPROVE_OPTION)
						toOpen = localFileChooser.getSelectedFile();
				} catch (java.security.AccessControlException ex) {
					System.err.println("No permissions");
				}

				if (toOpen == null)
					return; // nothing chosen to open

				try { // to open the specified file
					openFile(toOpen);
				} catch (FileNotFoundException ex) {
					setStatus("File '" + toOpen + "' not found");
				} catch (IOException ex) {
					setStatus("Error reading file " + toOpen);
				}
			}
		};

		item = mFile.add(actionOpenLocal);
		item.setMnemonic('l');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));
		// disable if no permission to use JFileChooser
		try {
			FileSystemView fsv = FileSystemView.getFileSystemView();
			new JFileChooser(fsv);
		} catch (RuntimeException ex) {
			System.err.println("No local file permission :: " + ex);
			item.setEnabled(false);
		}

		// File:Save
		actionSave = new AbstractAction("Save") {

			public void actionPerformed(ActionEvent e) {
				try {
					saveFile(currentFile);
					deleteAutosaved(currentFile);
				} catch (Exception ex) {
					alertSaveFailure(ex);
				}
			}
		};
		item = mFile.add(actionSave);
		item.setMnemonic('s');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				KeyEvent.CTRL_MASK));

		// File:Save As
		actionSaveAs = new AbstractAction("Save As") {

			public void actionPerformed(ActionEvent e) {
				File saveFile = null;

				try { // to use JFileChoooser
					if (remoteFileChooser == null)
						throw new AccessControlException("No Permissions");
					int returnVal = remoteFileChooser
							.showSaveDialog(SchemeEditor.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						saveFile = remoteFileChooser.getSelectedFile();
					}
				} catch (java.security.AccessControlException ex) {
					String fileName = promptFileInputDialog();
					if (fileName != null)
						saveFile = new WiserFile(wfs, fileName);
				}
				if (saveFile == null)
					return; // nothing to save because of
				// cancel

				// warn if saveFile already exists
				boolean proceed = checkOverwrite(saveFile);
				if (!proceed)
					return;

				// actually save the file
				try {
					saveFile(saveFile);
					// only delete autosaves if save succeeds
					deleteAutosaved(currentFile);
					// now reset current file
					setCurrentFile(saveFile);
				} catch (IOException ex) {
					alertSaveFailure(ex);
				}
			}
		};
		item = mFile.add(actionSaveAs);
		item.setMnemonic('a');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));

		// File:Close
		actionClose = new AbstractAction("Close") {

			public void actionPerformed(ActionEvent e) {
				close();
			}
		};

		item = mFile.add(actionClose);
		item.setMnemonic('c');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				KeyEvent.CTRL_MASK));

		// end File menu
		menuBar.add(mFile);

		// start Edit menu
		JMenu mEdit = new JMenu("Edit");
		mEdit.setMnemonic('e');

		actionUndo = new AbstractAction("Undo") {

			public void actionPerformed(ActionEvent e) {
				setStatus("Undo!");
				((SchemeDocument) buffer.getDocument()).undo();
			}
		};
		item = mEdit.add(actionUndo);
		item.setMnemonic('u');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				KeyEvent.CTRL_MASK));

		Action actionRedo = new AbstractAction("Redo") {

			public void actionPerformed(ActionEvent e) {
				setStatus("Redo!");
				((SchemeDocument) buffer.getDocument()).redo();
			}
		};
		item = mEdit.add(actionRedo);
		item.setMnemonic('e');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
				KeyEvent.CTRL_MASK));

		mEdit.addSeparator();

		actionIndent = new AbstractAction("Indent Selection") {

			public void actionPerformed(ActionEvent e) {
				setStatus("Indenting");
				buffer.indentSelection();
			}
		};

		item = mEdit.add(actionIndent);
		item.setMnemonic('i');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
				KeyEvent.CTRL_MASK));

		actionCopy = new AbstractAction("Copy") {

			public void actionPerformed(ActionEvent e) {
				setStatus("Text Copied");
				buffer.copy();
			}
		};
		item = mEdit.add(actionCopy);
		item.setMnemonic('c');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				KeyEvent.CTRL_MASK));

		actionCut = new AbstractAction("Cut") {

			public void actionPerformed(ActionEvent e) {
				setStatus("Text Cut");
				buffer.cut();
			}
		};
		item = mEdit.add(actionCut);
		item.setMnemonic('t');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				KeyEvent.CTRL_MASK));

		actionPaste = new AbstractAction("Paste") {

			public void actionPerformed(ActionEvent e) {
				try {
					java.awt.datatransfer.Clipboard clip = Toolkit
							.getDefaultToolkit().getSystemClipboard();
					System.out.println("Paste (clipboard) " + clip);
					System.out.println("contents: " + clip.getContents(this));
				} catch (AccessControlException ex) {
					System.err
							.println("No permission to use system clipboard.");
				}
				buffer.paste();
				setStatus("Text Pasted");
			}
		};

		item = mEdit.add(actionPaste);
		item.setMnemonic('p');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				KeyEvent.CTRL_MASK));

		mEdit.addSeparator();

		//FontSize Selection

		JMenu fontSizeSubMenu = new JMenu("Font Size");
		addSizeActions(fontSizeSubMenu, smallestFontSize, largestFontSize);
		mEdit.add(fontSizeSubMenu);

		//FontType Selection
		JMenu typeSubMenu = new JMenu("Font Type");
		addTypeActions(typeSubMenu);
		mEdit.add(typeSubMenu);

		mEdit.addSeparator();

		//Select All
		actionSelectAll = new AbstractAction("Select All") {

			public void actionPerformed(ActionEvent e) {
				setStatus("Select All");
				buffer.selectAll();
			}
		};
		item = mEdit.add(actionSelectAll);
		item.setMnemonic('a');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				KeyEvent.CTRL_MASK));

		mEdit.addSeparator();

		actionFind = new AbstractAction("Find...") {

			public void actionPerformed(ActionEvent e) {
				buffer.repaint();
				if (findDialog == null)
					findDialog = new FindReplaceDialog(buffer, 0);
				else
					findDialog.setSelectedIndex(0);

				Dimension d1 = findDialog.getSize();
				Dimension d2 = getSize();
				int x = Math.max((d2.width - d1.width) / 2, 0);
				int y = Math.max((d2.height - d1.height) / 2, 0);
				findDialog.setBounds(x + (int) getLocation().getX(), y
						+ (int) getLocation().getY(), d1.width, d1.height);

				findDialog.show();
			}
		};
		item = mEdit.add(actionFind);
		item.setMnemonic('f');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
				KeyEvent.CTRL_MASK));

		/* start find Next */
		actionFindNext = new AbstractAction("Find Again") {

			public void actionPerformed(ActionEvent e) {
				buffer.repaint();
				findDialog.setSelectedIndex(0);
				findDialog.show();
				findDialog.findNext(false, true);
			}
		};
		item = mEdit.add(actionFindNext);
		item.setMnemonic('g');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
				KeyEvent.CTRL_MASK));

		actionReplace = new AbstractAction("Replace...") {

			public void actionPerformed(ActionEvent e) {
				buffer.repaint();
				if (findDialog == null)
					findDialog = new FindReplaceDialog(buffer, 1);
				else
					findDialog.setSelectedIndex(1);

				Dimension d1 = findDialog.getSize();
				Dimension d2 = getSize();
				int x = Math.max((d2.width - d1.width) / 2, 0);
				int y = Math.max((d2.height - d1.height) / 2, 0);
				findDialog.setBounds(x + (int) getLocation().getX(), y
						+ (int) getLocation().getY(), d1.width, d1.height);

				findDialog.show();
			}
		};
		item = mEdit.add(actionReplace);
		item.setMnemonic('h');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
				KeyEvent.CTRL_MASK));

		menuBar.add(mEdit);

		// Scheme menu
		JMenu mScheme = new JMenu("Scheme");
		mScheme.setMnemonic('s');

		actionEvalSexpresion = new AbstractAction("Eval S-Expression") {

			public void actionPerformed(ActionEvent e) {
				String expr = buffer.getSExpression();
				sendToListener(expr, true);
			}
		};
		item = mScheme.add(actionEvalSexpresion);
		item.setMnemonic('s');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				KeyEvent.CTRL_MASK));

		actionEvalSelection = new AbstractAction("Eval Selection") {

			public void actionPerformed(ActionEvent e) {
				String selection = buffer.getSelection();
				sendToListener(selection, true);
			}
		};
		item = mScheme.add(actionEvalSelection);
		item.setMnemonic('r');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				KeyEvent.CTRL_MASK));

		actionEvalAll = new AbstractAction("Eval All") {

			public void actionPerformed(ActionEvent e) {
				String all = buffer.getText();
				sendToListener(all, true);
			}
		};
		item = mScheme.add(actionEvalAll);
		item.setMnemonic('l');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				KeyEvent.CTRL_MASK));

		mScheme.addSeparator();

		actionSetVerbose = new AbstractAction("Eval Verbose") {

			public void actionPerformed(ActionEvent e) {
				if (VERBOSE)
					VERBOSE = false;
				else
					VERBOSE = true;
			}
		};

		mScheme.add(new JCheckBoxMenuItem(actionSetVerbose));

		menuBar.add(mScheme);

		// Window menu
		JMenu mView = new JMenu("Window");
		mView.setMnemonic('w');

		actionViewListener = new AbstractAction("Listener") {

			public void actionPerformed(ActionEvent e) {
				// FIX assumes JistStore loads before SchemeEditor
				SchemeListener sl = JistStore.getListener();
				sl.setVisible(true);
				sl.toFront();
			}
		};
		item = mView.add(actionViewListener);
		item.setMnemonic('l');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				KeyEvent.CTRL_MASK));

		actionViewConsole = new AbstractAction("Console") {

			public void actionPerformed(ActionEvent e) {
				SchemeConsoleFrame scf = JistStore.getListener().getConsole();
				scf.setVisible(true);
				scf.toFront();
			}
		};
		item = mView.add(actionViewConsole);
		item.setMnemonic('l');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K,
				KeyEvent.CTRL_MASK));

		menuBar.add(mView);

		return menuBar;
	}

	void setBufferFontSize(int size) {
		bufferFontSize = size;
		bufferFont = bufferFont.deriveFont(Font.BOLD, size);
		buffer.resetBufferFont();
		buffer.stylizeFont(bufferFont.getName(), size);

	}

	void setBufferFontType(String type) {
		bufferFont = new Font(type, Font.BOLD, bufferFontSize);
		buffer.resetBufferFont();
		buffer.stylizeFont(bufferFont.getName(), bufferFontSize);
	}

	void addTypeActions(JMenu m) {

		for (int i = 0; i < fontTypes.length; i++) {
			final String font = fontTypes[i];

			actionFontType = new AbstractAction(font) {

				public void actionPerformed(ActionEvent e) {
					setBufferFontType(font);
				}
			};

			m.add(actionFontType);
		}
	}

	void addSizeActions(JMenu m, int smallestFontSize, int largestFontSize) {
		for (int size = smallestFontSize; size <= largestFontSize; size += 2) {
			final int s = size;
			actionSize = new AbstractAction((new Integer(size)).toString()) {

				public void actionPerformed(ActionEvent e) {
					setBufferFontSize(s);
				}
			};
			m.add(actionSize);
		}
	}

	void sendToListener(String str, boolean doEval) {
		if (!JistStore.holdingInterpreter()) {
			String errorMessage = "No listener is available.  You must first call up the Scheme Listener.  Then try again.";
			JOptionPane.showMessageDialog(null, errorMessage, "Error",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		SchemeListener listener = JistStore.getListener();

		if (listener == null)
			JOptionPane
					.showMessageDialog(
							null,
							"Cannot find the listener. At least one instance must be running.",
							"Error", JOptionPane.WARNING_MESSAGE);
		else if (str == null || str.equals(""))
			JOptionPane.showMessageDialog(null,
					"Expression to be evaluated/passed cannot be empty.",
					"Error", JOptionPane.WARNING_MESSAGE);
		else { // do it
			listener.setVisible(true);
			SchemeListenerPane sta = listener.getSchemeTextArea();
			if (doEval)
				sta.store(";; Eval expression from editor\n");
			if (VERBOSE)
				sta.store(str + "\n");
			if (doEval)
				sta.evaluate(str);
		}
	}

	public WiserFile getAutosaveFile(File file) {
		String newName = "#" + file.getName() + "#";
		return new WiserFile(wfs, file.getParentFile(), newName);
	}

	public static void main(String[] args) throws Exception {
		SchemeEditor se = new SchemeEditor();
		JFileChooser fc = new JFileChooser();
		File toOpen = null;
		int returnVal = fc.showOpenDialog(se);
		if (returnVal == JFileChooser.APPROVE_OPTION)
			toOpen = fc.getSelectedFile();
		if (toOpen != null)
			se.openFile(toOpen);
		se.setVisible(true);
		se.getBuffer().requestFocus();
	}

	// cool, but not used
	static class WindowTrackerMenuItem extends JCheckBoxMenuItem implements
			ItemListener, ComponentListener {

		JFrame tracked;

		public WindowTrackerMenuItem(JFrame tracked, String name) {
			super(name);
			this.tracked = tracked;
			addItemListener(this);
			tracked.addComponentListener(this);
		}

		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				tracked.setVisible(true);
			} else {
				tracked.setVisible(false);
			}
		}

		public void componentHidden(ComponentEvent e) {
			setSelected(false);
		}

		public void componentMoved(ComponentEvent e) {
		}

		public void componentResized(ComponentEvent e) {
		}

		public void componentShown(ComponentEvent e) {
			setSelected(true);
		}
	}

	class DocumentTracker implements DocumentListener {

		public void insertUpdate(DocumentEvent e) {
			update();
		}

		public void removeUpdate(DocumentEvent e) {
			update();
		}

		public void changedUpdate(DocumentEvent e) {
			update();
		}

		void update() {
			setStatus(" ");
			touch();
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
