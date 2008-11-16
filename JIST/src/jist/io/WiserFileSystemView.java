package jist.io;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

/**
 * Provides a java.io.FileSystemView representation of filesystem on the UC-WISE
 * server
 * 
 * @author Turadg
 */
public class WiserFileSystemView extends FileSystemView {
	WiserFileSystem wfs;

	public WiserFileSystemView(WiserFileSystem wfs) {
		this.wfs = wfs;
	}

	public boolean isRoot(File f) {
		if (f == null || !f.isAbsolute()) {
			return false;
		}

		File[] roots = getRoots();
		for (int i = 0; i < roots.length; i++) {
			if (roots[i].equals(f)) {
				return true;
			}
		}
		return false;
	}

	public String getSystemDisplayName(File f) {
		return f.getName();
	}

	public String getSystemTypeDescription(File f) {
		String extension = getExtension(f);
		String type = null;

		if (extension != null) {
			extension = extension.toUpperCase();
			if (extension.equals("SCM"))
				type = "Scheme source code";
			else if (extension.equals("TXT"))
				type = "Text";
			else
				type = extension + " File";
		}

		return type;
	}

	public Icon getSystemIcon(File f) {
		return UIManager.getIcon(f.isDirectory() ? "FileView.directoryIcon"
				: "FileView.fileIcon");
	}

	/*
	 * Get the extension of a file.
	 */
	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	/**
	 * Creates a new folder with a default folder name.
	 */
	public File createNewFolder(File containingDir) throws IOException {
		if (containingDir == null)
			throw new IOException("Containing directory is null");

		if (!(containingDir instanceof WiserFile))
			containingDir = null;

		File newFolder = null;
		newFolder = createFileObject(containingDir, "new folder");
		int i = 1;
		while (newFolder.exists() && (i < 100)) {
			newFolder = createFileObject(containingDir, "new folder." + i);
			i++;
		}

		if (newFolder.exists()) {
			throw new IOException("Directory already exists:"
					+ newFolder.getAbsolutePath());
		} else {
			newFolder.mkdirs();
		}

		return newFolder;
	}

	public boolean isHiddenFile(File f) {
		return f.isHidden();
	}

	public boolean isFileSystemRoot(File dir) {
		return (dir != null && dir.getAbsolutePath().equals("/"));
	}

	public boolean isComputerNode(File dir) {
		if (dir != null) {
			String parent = dir.getParent();
			if (parent != null && parent.equals("/net")) {
				return true;
			}
		}
		return false;
	}

	public File[] getRoots() {
		return new File[] { new WiserFile(wfs, ""
				+ WiserFileSystem.wiserSeparator) };
	}

	public File getHomeDirectory() {
		// FIX use System.getProperty("wiser.home") or something
		return createFileObject("" + WiserFileSystem.wiserSeparator);
	}

	public File getDefaultDirectory() {
		return getHomeDirectory();
	}

	public File createFileObject(File dir, String filename) {
		if (dir == null) {
			return new WiserFile(wfs, filename);
		} else {
			return new WiserFile(wfs, dir, filename);
		}
	}

	public File createFileObject(String path) {
		WiserFile f = new WiserFile(wfs, path);
		if (isFileSystemRoot(f)) {
			f = (WiserFile) createFileSystemRoot(f);
		}
		return f;
	}

	public File[] getFiles(File dir, boolean useFileHiding) {
		Vector files = new Vector();

		if (dir instanceof WiserFile) {
			// add all files in dir
			File[] names = dir.listFiles();

			File f;

			int nameCount = (names == null) ? 0 : names.length;
			for (int i = 0; i < nameCount; i++) {
				if (Thread.currentThread().isInterrupted()) {
					break;
				}
				f = names[i];
				if (isFileSystemRoot(f)) {
					f = createFileSystemRoot(f);
				}

				if (!useFileHiding || !isHiddenFile(f)) {
					files.addElement(f);
				}
			}
		} else {
			System.err.println("WiserFileSystem got passed other File type");
		}

		return (File[]) files.toArray(new File[files.size()]);

		/*
		 * if (dir instanceof WiserFile) return ((WiserFile)dir).listFiles();
		 * else return new WiserFile(wfs, dir.getPath()).listFiles();
		 */
	}

	public File getParentDirectory(File dir) {
		if (dir != null && dir.exists()) {
			String p = dir.getParent();
			if (p != null)
				return new WiserFile(wfs, p);
		}

		return null;
	}

	protected File createFileSystemRoot(File f) {
		return new WiserFileSystemRoot(f);
	}

	class WiserFileSystemRoot extends WiserFile {

		public WiserFileSystemRoot(File f) {
			super(wfs, f, "");
		}

		public WiserFileSystemRoot(String s) {
			super(wfs, s);
		}

		public boolean isDirectory() {
			return true;
		}

		public String getName() {
			return getPath();
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
