package jist.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Vector;

/**
 * WiserFile provides a java.io.File representation of a file on the UC-WISE
 * server
 * 
 * @author Turadg
 */
public class WiserFile extends File {
	// to parallel the File class
	private WiserFileSystem fs;

	private transient int prefixLength;

	final static String PATHDELIMS = "/\\";

	private boolean checked;

	private boolean isdirectory;

	private boolean isfile;

	private String path;

	private long length;

	private long modified;

	private Vector filenames;

	private Vector dirnames;

	public WiserFile(WiserFileSystem fs, String pathname) {
		super(pathname);
		if (fs == null) {
			System.err.println("WiserFile constructor received null fs");
			throw new NullPointerException();
		}
		if (pathname == null) {
			throw new NullPointerException();
		}
		this.fs = fs;
		this.path = fs.normalize(pathname);
		this.prefixLength = fs.prefixLength(this.path);

		// 	System.out.println("new WiserFile ( "+fs+" , "+pathname+" )");
	}

	public WiserFile(WiserFileSystem fs, String parent, String child) {
		super(parent, child);
		System.out.println("new WiserFile ( " + fs + " , " + parent + " , "
				+ child + " )");
		this.fs = fs;
		if (fs == null) {
			System.err.println("WiserFile constructor received null fs");
			throw new NullPointerException();
		}
		if (child == null) {
			throw new NullPointerException();
		}
		if (parent != null) {
			if (parent.equals("")) {
				this.path = fs.resolve(fs.getDefaultParent(), fs
						.normalize(child));
			} else {
				this.path = fs.resolve(fs.normalize(parent), fs
						.normalize(child));
			}
		} else {
			this.path = fs.normalize(child);
		}
		this.prefixLength = fs.prefixLength(this.path);
	}

	public WiserFile(WiserFileSystem fs, File parent, String child) {
		super(parent, child);
		System.out.println("new WiserFile ( " + fs + ", " + parent + ", "
				+ child + " )");
		this.fs = fs;
		if (fs == null) {
			System.err.println("WiserFile constructor received null fs");
			throw new NullPointerException();
		}
		if (child == null) {
			throw new NullPointerException();
		}
		if (parent != null) {
			if (parent.getPath().equals("")) {
				this.path = fs.resolve(fs.getDefaultParent(), fs
						.normalize(child));
			} else {
				this.path = fs.resolve(parent.getPath(), fs.normalize(child));
			}
		} else {
			this.path = fs.normalize(child);
		}
		this.prefixLength = fs.prefixLength(this.path);
	}

	static String lastOfPath(String path) {
		int lastSlash = path.indexOf('/');
		String last = path.substring(lastSlash + 1);
		System.out.println("lastOfPath returning '" + last + "'");
		return last;
	}

	private void update() {
		System.out.println("update() for path: '" + path + "'");

		try {
			dirnames = fs.listDirs(this);
			filenames = fs.listFiles(this);
			isdirectory = (dirnames != null);
		} catch (Exception ex) {
			// if there's an error listing, means not a directory
			isdirectory = false;
		}

		isfile = !isdirectory;

		if (isdirectory && filenames == null)
			System.err
					.println("INCONSISTENT: dirnames has FAILURE but not filenames");

		checked = true;
	}

	public boolean delete() {
		return fs.delete(this);
	}

	public boolean equals(Object obj) {
		if (!checked)
			update();

		return path.equals(obj.toString());
	}

	public boolean isDirectory() {
		if (!checked)
			update();

		return isdirectory;
	}

	public boolean isFile() {
		if (!checked)
			update();

		return isfile;
	}

	public boolean isAbsolute() {
		if (!checked)
			update();

		return path.length() > 0
				&& path.charAt(0) == WiserFileSystem.wiserSeparatorChar;
	}

	public boolean isHidden() {
		String name = getName();
		return (path.length() > 0 && name.charAt(0) == '.');
	}

	public long lastModified() {
		return modified;
	}

	public long length() {
		return length;
	}

	public String[] list() {
		return list(null);
	}

	public String[] list(FilenameFilter filter) {
		System.out.println("WiserFile.list( " + filter + " )");

		update();

		if (!isdirectory)
			return null;
		// FIX list filenames and dirnames
		Vector all = new Vector();
		all.add(filenames);
		all.add(dirnames);
		return (String[]) (all.toArray());
	}

	public File[] listFiles() {
		return this.listFiles((FileFilter) null); // no file filter
	}

	public File[] listFiles(FileFilter filter) {
		System.out.println("WiserFile.listFiles( " + filter + " )");
		Vector files = new Vector();

		Vector dirnames = fs.listDirs(this);

		for (int i = 0; i < dirnames.size(); ++i) {
			String name = (String) dirnames.elementAt(i);
			WiserFile file = new WiserFile(fs, name);
			file.isdirectory = true;
			files.addElement(file);
		}
		Vector filenames = fs.listFiles(this);
		for (int i = 0; i < filenames.size(); ++i) {
			String name = (String) filenames.elementAt(i);
			files.addElement(new WiserFile(fs, name));
		}

		File[] result = new File[files.size()];
		files.copyInto(result);
		return result;
	}

	public String getPath() {
		return path;
	}

	public String getAbsolutePath() {
		return fs.resolve(this);
	}

	public File getAbsoluteFile() {
		return new WiserFile(fs, getAbsolutePath());
	}

	public String getCanonicalPath() {
		return fs.canonicalize(fs.resolve(this));
	}

	public File getCanonicalFile() {
		return new WiserFile(fs, getCanonicalPath());
	}

	public String getName() {
		int index = path.lastIndexOf(WiserFileSystem.wiserSeparatorChar);
		if (index < prefixLength)
			return path.substring(prefixLength);
		return path.substring(index + 1);
	}

	public String getParent() {
		int index = path.lastIndexOf(WiserFileSystem.wiserSeparatorChar);
		// 	System.out.println("getParent() index:"+index+"
		// prefixLength:"+prefixLength);
		if (index < prefixLength) {
			if ((prefixLength > 0) && (path.length() > prefixLength))
				return path.substring(0, prefixLength);
			return null;
		}
		return path.substring(0, index);
	}

	public File getParentFile() {
		String p = this.getParent();
		// 	if (p == null) return null;
		if (p == null)
			return new WiserFile(fs, "/");
		return new WiserFile(fs, p);
	}

	public boolean exists() {
		return fs.exists(this);
	}

	public boolean canRead() {
		// FIX
		return true;
	}

	public boolean canWrite() {
		// FIX
		return true;
	}

	public int hashCode() {
		return path.hashCode() ^ 1234321;
	}

	public boolean mkdir() {
		// FIX no dir making yet
		return false;
	}

	public boolean renameTo(File dest) {
		// FIX no renaming yet
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
