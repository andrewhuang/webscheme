package jist.editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A Scanner for Scheme tokens.
 * 
 * The scanner keeps track of the current token, the value of the current token
 * (if any), and the start position of the current token.
 * 
 * The compiler treats either "\n", "\r" or "\r\n" as the end of a line.
 */
public class TokenScanner {
    /**
     * Input reader
     */
    protected InputStreamReader inStreamReader;

    protected BufferedReader in;

    /**
     * The position and length of the current token
     */
    protected int pos;

    /*
     * Indicate the type of the first character of the token
     */

    public int first = -1;

    /**
     * Current token
     */
    protected Token token;

    /**
     * The position of the starting and ending positions of the current token
     */
    public int posStart;

    public int posEnd;

    /**
     * Hashtable to store token types corresponding to a position
     */
    protected TokenTable tokens = new TokenTable();

    /**
     * Create a scanner to scan an input stream.
     */
    public TokenScanner(InputStream in) {
        useInputStream(in);
    }

    /**
     * Setup input from the given input stream, and scan the first token from
     * it.
     */
    public TokenScanner useInputStream(InputStream in) {
        this.inStreamReader = new InputStreamReader(in);
        this.in = new BufferedReader(inStreamReader);
        pos = 0;
        return this;
    }

    /**
     * The scan value, starting and ending positions of the current token
     */
    public int getScanValue() {
        return token.getScanValue();
    }

    public int getStartOffset() {
        return pos - posStart;
    }

    public int getEndOffset() {
        return posEnd - pos;
    }

    /**
     * Scan the whole document and put token information to hashtable
     */
    public void scan() {
        try {
            TokenValue v = new TokenValue(in);
            while (v.valid) {
                tokens.put(new Integer(pos), new TokenInformation(v.token,
                        v.length));
                pos += v.length;
                v = new TokenValue(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Find the token in the specified position
     */

    public int getToken(int pos) {
        TokenInformation content = tokens.getTokenInformation(new Integer(pos));

        this.pos = pos;
        if (content != null) {
            token = content.token;
            posEnd = posStart + content.length;
        } else {
            token = Token.UNKNOWN;
            posStart = 0;
            posEnd = 1;
        }
        return token.getScanValue();
    }

    /**
     * Methods to manipulate token table for text changes
     */

    public void clear() {
        tokens.clear();
        pos = 0;
        token = Token.UNKNOWN;
        first = -1;
        posStart = 0;
        posEnd = 1;
    }

    public void dump() {
        for (Enumeration k = tokens.keys(); k.hasMoreElements();) {
            Object i = k.nextElement();
            TokenInformation e = (TokenInformation) tokens.get(i);
            System.out.println(i + ": " + e.token.toString() + "(" + e.length
                    + ")");
        }
    }

    /**
     * @return true if the number of left and right parens are equal, ignores
     *         paranthesis in quotes.
     */
    public static boolean balancedParens(String text) {
        int rightParens = 0;
        int leftParens = 0;
        int searchPos = 0;

        //Remove the quoted part of the string
        if (text.indexOf('"') != -1) {
            text = text.substring(0, text.indexOf('"'))
                    + text.substring(text.lastIndexOf('"'));
        }

        while ((searchPos = text.indexOf('(', searchPos) + 1) != 0)
            rightParens++;

        searchPos = 0;

        while ((searchPos = text.indexOf(')', searchPos) + 1) != 0)
            leftParens++;

        return (rightParens == leftParens);
    }

    /**
     * Hash object: (token, length) pairs
     */
    protected class TokenInformation {
        public Token token;

        public int length;

        public TokenInformation(Token t, int l) {
            token = t;
            length = l;
        }
    }

    /**
     * Hashtable for (position, (token, length)) pairs
     */
    protected class TokenTable extends Hashtable {
        public TokenInformation getTokenInformation(Integer key) {
            TokenInformation content;
            do {
                if ((content = (TokenInformation) get(key)) == null) {
                    if (key.intValue() > 0)
                        key = new Integer(key.intValue() - 1);
                    else
                        break;
                } else {
                    posStart = key.intValue();
                    break;
                }
            } while (key.intValue() >= 0);
            return content;
        }
    }

    /**
     * Class to represent a token in string
     */
    protected class TokenValue {
        public TokenValue(BufferedReader in) throws IOException {
            if (first == -1)
                parseNextChar(in, null);
            else
                length++;
            parseNextToken(in);
        }

        /**
         * Parse the input stream for the next token
         */
        public void parseNextToken(BufferedReader in) throws IOException {
            switch (first) {
            // no more data
            case -1:
                valid = false;
                break;

            // operators
            case '~':
                token = Token.T_01;
                first = -1;
                valid = true;
                break;
            case '`':
                token = Token.T_02;
                first = -1;
                valid = true;
                break;
            case '!':
                token = Token.T_03;
                first = -1;
                valid = true;
                break;
            case '@':
                token = Token.T_04;
                first = -1;
                valid = true;
                break;
            case '$':
                token = Token.T_06;
                first = -1;
                valid = true;
                break;
            case '%':
                token = Token.T_07;
                first = -1;
                valid = true;
                break;
            case '^':
                token = Token.T_08;
                first = -1;
                valid = true;
                break;
            case '&':
                token = Token.T_09;
                first = -1;
                valid = true;
                break;
            case '*':
                token = Token.T_10;
                first = -1;
                valid = true;
                break;
            case '(':
                token = Token.T_11;
                first = -1;
                valid = true;
                break;
            case ')':
                token = Token.T_12;
                first = -1;
                valid = true;
                break;
            case '_':
                token = Token.T_13;
                first = -1;
                valid = true;
                break;
            case '-':
                token = Token.T_14;
                first = -1;
                valid = true;
                break;
            case '+':
                token = Token.T_15;
                first = -1;
                valid = true;
                break;
            case '=':
                token = Token.T_16;
                first = -1;
                valid = true;
                break;
            case '\\':
                token = Token.T_17;
                first = -1;
                valid = true;
                break;
            case ':':
                token = Token.T_18;
                first = -1;
                valid = true;
                break;
            case '<':
                token = Token.T_22;
                first = -1;
                valid = true;
                break;
            case '>':
                token = Token.T_23;
                first = -1;
                valid = true;
                break;
            case ',':
                token = Token.T_24;
                first = -1;
                valid = true;
                break;
            case '.':
                token = Token.T_25;
                first = -1;
                valid = true;
                break;
            case '?':
                token = Token.T_26;
                first = -1;
                valid = true;
                break;
            case '/':
                token = Token.T_27;
                first = -1;
                valid = true;
                break;

            // boolean
            case '#':
                String bool = readUntilDelimiter();
                token = Token.UNKNOWN;
                // compare boolean
                if (bool.toLowerCase().equals("#t")
                        || bool.toLowerCase().equals("#f"))
                    token = Token.BOOL_VAL;
                valid = true;
                break;

            // strings
            case '"':
                token = Token.STR_VAL;
                readUntil('"', true);
                first = -1;
                valid = true;
                break;
            case '\'':
                token = Token.STR_VAL;
                readUntilWhitespace();
                valid = true;
                break;

            // comments
            case ';':
                token = Token.COMMENT;
                readUntilNewline();
                valid = true;
                break;

            // whitespaces
            case ' ':
            case '\t':
            case '\r':
                token = Token.WHITESPACE;
                first = -1;
                valid = true;
                break;
            case '\n':
                token = Token.NEWLINE;
                first = -1;
                valid = true;
                break;

            // others
            default:
                if (first >= '0' && first <= '9') {
                    readUntilDelimiter();
                    token = Token.NUM_VAL;
                } else {
                    String str = readUntilDelimiter();
                    token = Token.UNKNOWN;

                    // compare keywords
                    for (int i = 0; i < Token.keywords.length; i++) {
                        if (Token.keywords[i].toString().equals(str)) {
                            token = Token.keywords[i];
                            break;
                        }
                    }

                    if (token == Token.KEY_05) // is "define"?
                        defining = true;
                    else if (token == Token.UNKNOWN && defining) { // is after
                        // "define"?
                        token = Token.IDENT;
                        defining = false;
                    }
                }
                valid = true;
                break;
            }
        }

        /*
         * Grab the string until the given delimiter, and whether escaped
         * characters are interpreted
         */
        protected String readUntil(int del, boolean escaped) throws IOException {
            String str = "" + (char) first;
            do {
                parseNextChar(in, str);
                // An escaped character
                if (escaped && first == '\\') {
                    parseNextChar(in, str);
                    parseNextChar(in, str);
                }
            } while (first != del && first != -1);
            return str;
        }

        /*
         * Grab the string until a newline, and whether escaped characters are
         * interpreted
         */
        protected String readUntilNewline() throws IOException {
            String str = "" + (char) first;
            do {
                first = in.read();
                if (first != '\n' && first != -1) {
                    str = str + (char) first;
                    length++;
                } else
                    break;
            } while (true);
            return str;
        }

        /*
         * Grab the string until a whitespace, and whether escaped characters
         * are interpreted
         */
        protected String readUntilWhitespace() throws IOException {

            String str = "" + (char) first;
            boolean openParensFound = false;
            //true if found an open parens following the quote...implies
            // quoting a list.

            do {
                first = in.read();

                //If this is the first open Parens
                if (first == '(' && !openParensFound) {
                    openParensFound = true;
                }

                if ((first != ' ' || openParensFound)
                        && (first != '\t' || openParensFound)
                        && (first != '\n' || openParensFound) && first != -1) {

                    if ((first == ')' || first == ' ' || first == '\t' || first == '\n')
                            && openParensFound
                            && TokenScanner.balancedParens(str))
                        break;

                    if ((first == ')') && !openParensFound)
                        break;

                    str = str + (char) first;
                    length++;

                } else
                    break;
            } while (true);

            return str;
        }

        /*
         * Grab the string until a delimiter, and whether escaped characters are
         * interpreted
         */
        protected String readUntilDelimiter() throws IOException {
            String str = "" + (char) first;
            do {
                first = in.read();
                if (first != ' ' && first != '\t' && first != '\n'
                        && first != '(' && first != ')' && first != '"'
                        && first != ';' && first != -1) {
                    str = str + (char) first;
                    length++;
                } else
                    break;
            } while (true);
            return str;
        }

        /*
         * parse the next character
         */
        protected String parseNextChar(BufferedReader in, String str)
                throws IOException {
            first = in.read();
            if (first != -1) {
                length++;
                if (str != null)
                    return str + (char) first;
                else
                    return null;
            } else
                return null;
        }

        /*
         * Indecate the type of the token
         */
        public Token token;

        /*
         * Indecate the length of the token
         */
        public int length = 0;

        /*
         * Indecate whether the token stored is valid
         */
        public boolean valid = false;
    }

    // remember if it is a definition, so we can paint identifiers
    protected boolean defining = false;
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
