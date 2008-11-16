package jist.editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Reads in custom preferences storage format
 * 
 * @todo Switch to a .properties file or Properties class
 */
public class PreferencesReader {
    Vector preferences = new Vector();

    // initialize and read preferences from a file
    public PreferencesReader(InputStream is) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is));
            String line;

            while ((line = reader.readLine()) != null) {
                // read line by line
                StreamTokenizer tokens = new StreamTokenizer(new StringReader(
                        line));
                Vector pref = new Vector();

                while (tokens.nextToken() != StreamTokenizer.TT_EOF) {
                    // read token by token
                    if (tokens.ttype == StreamTokenizer.TT_WORD) {
                        pref.add(tokens.sval);
                    } else if (tokens.ttype == StreamTokenizer.TT_NUMBER) {
                        pref.add(new Double(tokens.nval));
                    } else if (pref.size() == 0 && tokens.ttype == ';') {
                        break;
                    }
                }
                if (pref.size() > 0)
                    preferences.add(pref);
            }
        } catch (IOException e) {
            System.out.println("PrefReader failed :: " + e);
        }
    }

    public Enumeration preferences() {
        return preferences.elements();
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
