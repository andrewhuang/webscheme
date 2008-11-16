package jist.env;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.TextArea;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.StringTokenizer;

/**
 * Applet tests the Java runtime version versus required version
 * 
 * @todo Make "required" a full version number (eg. 1.3.1)
 * @todo Handle any error
 * @todo Talk to Javascript
 * @todo Add table of os.name -> Java source
 * 
 * $Id: CheckJavaVersion.java,v 1.5 2004/10/20 23:07:02 turadg Exp $
 * 
 * @author Turadg Aleahmad
 */
public class CheckJavaVersion extends Applet {
	static final String WINDOWS_UPDATE = "http://java.sun.com/getjava/download.html";

	String osName;

	String javaVersion;

	String javaVendor;

	int minorVersion;

	int requiredMinorVersion = 3;

	public void init() {
		setLayout(new BorderLayout());

		// read in the required minor version
		try {
			String str = getParameter("required");
			requiredMinorVersion = Integer.parseInt(str);
		} catch (Exception ex) {
			System.err.println("Error in 'required' parameter; defaulting to "
					+ requiredMinorVersion);
		}

		// Query Java version, vendor, and operating system
		javaVersion = System.getProperty("java.version");
		javaVendor = System.getProperty("java.vendor");
		osName = System.getProperty("os.name");

		// output to console
		System.out.print("CheckJavaVersion (");
		System.out.print("os.name = " + osName);
		System.out.print(", java.vendor = " + javaVendor);
		System.out.print(", java.version = " + javaVersion);
		System.out.println(")");

		// parse out second digit of version
		minorVersion = minorVersionOf(javaVersion);
	}

	public void start() {
		if (needsUpdating()) {
			Frame alert = new Frame("Update Java");
			WindowAdapter wl = new WindowAdapter() {
				// disappear when it loses focus
				public void windowDeactivated(WindowEvent e) {
					e.getWindow().hide();
				}
			};
			alert.addWindowListener(wl);
			TextArea info = new TextArea(
					"Your Java runtime is too old for UCWISE.  Please update it from Sun:\n"
							+ WINDOWS_UPDATE);
			info.setEditable(false);
			alert.add(info);
			alert.pack();
			alert.show();
		}
	}

	// just draw the platform info
	public void paint(Graphics g) {
		Font font = new Font("SansSerif", Font.PLAIN, 24);
		g.setFont(font);
		if (minorVersion >= requiredMinorVersion) {
			g.setColor(Color.black);
			g.drawString("Your Java runtime", 10, 20);
			g.drawString("is up to date.", 10, 50);
		} else {
			g.setColor(Color.red);
			g.drawString("Your Java runtime is old.", 10, 20);
			g.drawString("Please update it.", 10, 50);
		}
	}

	static int minorVersionOf(String version) {
		String num;
		StringTokenizer st = new StringTokenizer(version, ".");
		num = st.nextToken(); // major number
		num = st.nextToken(); // minor
		return Integer.parseInt(num);
	}

	public boolean needsUpdating() {
		return (minorVersion < requiredMinorVersion);
	}

	public String getUpdateURL() {
		StringTokenizer st = new StringTokenizer(osName);
		String platform = st.nextToken();
		if (platform.equals("Windows"))
			return WINDOWS_UPDATE;
		else
			return null;
		/*
		 * Mac OS X comes with JRE 1.3.1 which is Apple's latest Mac OS 9 comes
		 * with JRE 1.1.8 which Apple has no plans of updating
		 */
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
