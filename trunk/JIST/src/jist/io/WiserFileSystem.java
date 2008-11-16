package jist.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import java.util.zip.CRC32;

import org.apache.xmlrpc.Base64;
import org.apache.xmlrpc.XmlRpcClient;

/**
 * Abstraction of UC-WISE file system
 * 
 * @author Turadg
 */
public class WiserFileSystem {
	public static final char wiserSeparatorChar = '/';

	public static final String wiserSeparator = "" + wiserSeparatorChar;

	XmlRpcClient xmlrpc;

	public WiserFileSystem(URL codeBase) throws Exception {
		// Create XML RPC Client object, bind it to Wiser endpoint
		URL rpcUrl = new URL(codeBase, "/fm/xmlrpc.php");
		System.out.println("rpcUrl= " + rpcUrl);
		xmlrpc = new XmlRpcClient(rpcUrl);
	}

	public void put(File file, String value) throws IOException {
		System.out.println("put string ( " + file + " ) aka '"
				+ file.getAbsolutePath() + "'");

		CRC32 checksum = new CRC32();
		checksum.update(value.getBytes());
		long crc = checksum.getValue();

		Vector params = new Vector();
		try {
			params.addElement(file.getAbsolutePath());
		} catch (Exception ex) {
			System.err.println("couldn't add param for : " + file);
		}

		// add data to payload
		params.addElement(value);

		Object result = null;
		try {
			// writeString should return an Integer
			result = xmlrpc.execute("writeString", params);
		} catch (Exception ex) {
			System.err.println("writeString failed :: " + ex);
			ex.printStackTrace();
			System.err.println();
		}

		int replyCRC = 0;
		try {
			replyCRC = ((Integer) result).intValue();
		} catch (Exception ex) {
			System.out.println("replyCRC: " + ex);
		}

		// PHP's CRC function returns a signed integer
		// so we need to convert Java's long to compare
		boolean success = (replyCRC == (int) crc);

		if (!success)
			throw new IOException("unmatched CRCs (" + replyCRC + "!=" + crc
					+ ") for " + result + "(" + result.getClass() + ")");
	}

	public void put(File file, byte[] data) throws IOException {
		System.out.println("put binary ( " + file + " ) aka '"
				+ file.getAbsolutePath() + "'");

		CRC32 checksum = new CRC32();
		checksum.update(data);
		long crc = checksum.getValue();

		Vector params = new Vector();
		try {
			params.addElement(file.getAbsolutePath());
		} catch (Exception ex) {
			System.err.println("couldn't add param for : " + file);
		}

		// add data to payload as base64
		params.addElement(Base64.encode(data));

		Object result = null;
		try {
			// writeBinary should return an Integer
			result = xmlrpc.execute("writeBinary", params);
		} catch (Exception ex) {
			System.err.println("writeBinary failed :: " + ex);
			ex.printStackTrace();
			System.err.println();
		}
		int replyCRC = 0;
		try {
			replyCRC = ((Integer) result).intValue();
		} catch (Exception ex) {
			System.out.println("replyCRC failed: " + ex);
		}

		// PHP's CRC function returns a signed integer
		// so we need to convert Java's long to compare
		boolean success = (replyCRC == (int) crc);

		if (!success)
			throw new IOException(
					"unmatched CRCs ("
							+ replyCRC
							+ "!="
							+ crc
							+ ") for "
							+ result
							+ "("
							+ ((result == null) ? "null" : result.getClass()
									.toString()) + ")");
	}

	public String get(File file) throws FileNotFoundException {
		System.out.println("get( " + file + " )");
		System.out.println("   .getName(): " + file.getName());
		System.out.println("   .getAbsolutePath(): " + file.getAbsolutePath());
		Vector params = new Vector();
		params.addElement(file.getAbsolutePath());
		String result = "failed";
		try {
			Object tmp = xmlrpc.execute("readString", params);
			System.out.println("readString returned " + tmp);
			result = (String) tmp;
		} catch (Exception ex) {
			System.err.println("readString failed :: " + ex);
			throw new FileNotFoundException("error getting " + file + "::" + ex);
		}

		if (result == null)
			throw new FileNotFoundException(file + " does not exist");
		else
			return result;
	}

	public Vector listDirs(File file) {
		System.out.println("listDirs( " + file + " ) aka '"
				+ file.getAbsolutePath() + "'");
		Vector params = new Vector();
		params.addElement(file.getAbsolutePath());
		Vector result = null;
		try {
			result = (Vector) xmlrpc.execute("listDirs", params);
		} catch (Exception ex) {
			System.err.println("listDirs failed :: " + ex);
		}

		System.out.println("listDirs returning: " + result);

		System.out
				.println("*OVERRIDE: return empty list until subdir saving works");
		if (result != null)
			return new Vector();

		return result;
	}

	public Vector listFiles(WiserFile file) {
		System.out.println("listFiles( " + file + " ) aka '"
				+ file.getAbsolutePath() + "'");
		Vector params = new Vector();
		params.addElement(file.getAbsolutePath());
		Vector result = null;
		try {
			result = (Vector) xmlrpc.execute("listFiles", params);
		} catch (Exception ex) {
			System.err.println("listFiles failed :: " + ex);
		}

		System.out.println("listFiles returning: " + result);
		return result;
	}

	public boolean delete(WiserFile file) {
		System.out.println("delete( " + file + " ) aka '"
				+ file.getAbsolutePath() + "'");
		Vector params = new Vector();
		params.addElement(file.getAbsolutePath());
		Boolean success = new Boolean(false);
		try {
			success = (Boolean) xmlrpc.execute("delete", params);
		} catch (Exception ex) {
			System.err.println("delete failed :: " + ex);
		}
		return success.booleanValue();
	}

	public boolean exists(WiserFile file) {
		Vector params = new Vector();
		params.addElement(file.getAbsolutePath());
		Boolean success = new Boolean(false);
		try {
			success = (Boolean) xmlrpc.execute("exists", params);
		} catch (Exception ex) {
			System.err.println("exists failed :: " + ex);
		}
		System.out.println("exists( " + file + " ) is " + success);
		return success.booleanValue();
	}

	public static void main(String args[]) throws Exception {
		WiserFileSystem fs = new WiserFileSystem(new URL(
				"http://turadg.ucdev.org/"));

		String data = "This_is_a_longish_short_string";
		String result;
		boolean success;

		WiserFile file = new WiserFile(fs, "test");

		System.out.println("putting bytes...");
		fs.put(file, data.getBytes());
		result = fs.get(file);
		System.out.println("OUT: " + data);
		System.out.println("IN:  " + result);

		System.out.println("putting string...");
		fs.put(file, data);
		result = fs.get(file);
		System.out.println("OUT: " + data);
		System.out.println("IN:  " + result);

		System.out.println("checking existance...");
		success = fs.exists(file);
		System.out.println("OUT: " + data);
		System.out.println("IN:  " + success);

		System.out.println("deleting...");
		success = fs.delete(file);
		System.out.println("OUT: " + data);
		System.out.println("IN:  " + success);

		System.out.println("checking existance again...");
		success = fs.exists(file);
		System.out.println("OUT: " + data);
		System.out.println("IN:  " + success);
	}

	/* -- Normalization and construction -- */

	/**
	 * Return the local filesystem's name-separator character.
	 */
	public char getSeparator() {
		return '/';
	}

	/**
	 * Return the local filesystem's path-separator character.
	 */
	public char getPathSeparator() {
		return ':';
	}

	/**
	 * Convert the given pathname string to normal form. If the string is
	 * already in normal form then it is simply returned.
	 */
	public String normalize(String path) {
		// FIX
		return path;
	}

	/**
	 * Compute the length of this pathname string's prefix. The pathname string
	 * must be in normal form.
	 */
	public int prefixLength(String path) {
		// FIX ??
		return path.lastIndexOf(wiserSeparatorChar) + 1;
	}

	/**
	 * Resolve the child pathname string against the parent. Both strings must
	 * be in normal form, and the result will be in normal form.
	 */
	public String resolve(String parent, String child) {
		// FIX handle .. and such
		return parent + getSeparator() + child;
	}

	/**
	 * Return the parent pathname string to be used when the parent-directory
	 * argument in one of the two-argument File constructors is the empty
	 * pathname.
	 */
	public String getDefaultParent() {
		// FIX
		return "" + getSeparator();
	}

	/**
	 * Post-process the given URI path string if necessary. This is used on
	 * win32, e.g., to transform "/c:/foo" into "c:/foo". The path string still
	 * has slash separators; code in the File class will translate them after
	 * this method returns.
	 */
	public String fromURIPath(String path) {
		return path;
	}

	/* -- Path operations -- */

	/**
	 * Tell whether or not the given abstract pathname is absolute.
	 */
	public boolean isAbsolute(WiserFile f) {
		return f.getPath().charAt(0) == '/';
	}

	/**
	 * Resolve the given abstract pathname into absolute form. Invoked by the
	 * getAbsolutePath and getCanonicalPath methods in the File class.
	 */
	public String resolve(WiserFile f) {
		// FIX
		if (isAbsolute(f))
			return f.getPath();
		else
			return "/" + f.getPath();
	}

	public String canonicalize(String path) {
		// TODO implement
		return path;
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
