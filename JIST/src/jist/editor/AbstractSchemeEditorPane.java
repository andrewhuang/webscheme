package jist.editor;

import java.awt.Color;
import java.awt.Font;
import java.util.LinkedList;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

/**
 * Abstract superclass of all Scheme-coloring editor panes
 * 
 * @author Jeff Wang
 *  
 */
public class AbstractSchemeEditorPane extends JEditorPane {

    static final String STYLES_RESOURCE = "Styles.properties";

    static final int FLASH_DURATION = 100; // in milliseconds

    /** Stores the syntax coloring settings */
    protected SchemeContext styles;

    public AbstractSchemeEditorPane() {
        setStyle();
        stylizeAppearance();

        setBackground(Color.black);
        setForeground(Color.white);
        setCaretColor(new Color(255, 255, 255));
    }

    protected void setStyle() {
        SchemeEditorKit kit = new SchemeEditorKit(this);
        styles = kit.getStylePreferences();
        setEditorKitForContentType("text/scheme", kit);
        setContentType("text/scheme");
        setRequestFocusEnabled(true);
        setEditable(true);
    }

    /* SCHEME INDENTING FUNCTIONALITY */

    public int indentLine() {
        return indentLine(getCaretPosition());
    }

    public int indentLine(int pos) {
        String schemeText = getText();
        int schemeTextLength = schemeText.length();
        int endOfBeginning = 0;

        //Trying to find the position in this text right before the line
        //that is being indented. To do this, find the last occurance of a
        // newline
        //and if none exists, it means there is only 1 line in the document.

        if (pos > schemeTextLength)
            endOfBeginning = schemeText.lastIndexOf("\n");
        else
            endOfBeginning = (schemeText.substring(0, pos)).lastIndexOf("\n");

        if (endOfBeginning == -1) {
            setText(schemeText.trim() + "\n");
            setCaretPosition(0);
            return 0;
        }

        String preceding = schemeText.substring(0, endOfBeginning);
        int lineStart = endOfBeginning + 1;
        int lineEnd = schemeText.indexOf('\n', pos);
        if (lineEnd <= 0)
            lineEnd = schemeText.length();
        String line = schemeText.substring(lineStart, lineEnd);

        // count existing spaces at front of line
        int existingSpaces = 0;
        while (true) {
            if (existingSpaces >= line.length()
                    || line.charAt(existingSpaces) != ' ')
                break;
            existingSpaces += 1;
        }

        Vector res = findExpressionType(preceding.length());
        String name = Token.getRepresentation(((Integer) res.firstElement())
                .intValue());
        if (name == null)
            name = "";
        int fromMargin = SchemeIndenting.getFrom(name);
        int indentAmount = ((Integer) res.lastElement()).intValue()
                + SchemeIndenting.getSpaces(name)
                + ((fromMargin == 1) ? ((Integer) res.elementAt(1)).intValue()
                        : 0);
        // length of the arg

        int sizeDelta = indentAmount - existingSpaces;
        // 	System.out.println(" delta: " + sizeDelta);
        try {
            if (sizeDelta < 0) {
                getDocument().remove(lineStart, -sizeDelta);
            } else if (sizeDelta > 0) {
                StringBuffer toInsert = new StringBuffer();
                for (int i = 0; i < (indentAmount - existingSpaces); ++i)
                    toInsert.append(' ');

                getDocument()
                        .insertString(lineStart, toInsert.toString(), null);
            }
        } catch (BadLocationException ex) {
            System.err.println(ex);
        }
        setCaretPosition(lineStart + indentAmount);
        return sizeDelta;
    }

    public void indentSelection() {
        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();
        if (selectionStart >= selectionEnd)
            return;

        int linesDone = 0;
        int lineStart;
        do {
            lineStart = selectionStart;
            String text = getText(); // must reget because Document changed
            for (int i = 0; i < linesDone; ++i) {
                lineStart = text.indexOf('\n', lineStart + 1); // next newline
            }
            if (lineStart == -1)
                break;
            int sizeDelta = indentLine(lineStart);
            selectionEnd += sizeDelta;
            linesDone += 1;
            styles.view.forceUpdateScanner();
        } while (lineStart <= selectionEnd);
    }

    public void indentReturn() {
        int caretPos = getCaretPosition();
        Vector res = findExpressionType(caretPos);

        String name = Token.getRepresentation(((Integer) res.firstElement())
                .intValue());
        if (name == null)
            name = new String("");

        int from = SchemeIndenting.getFrom(name);

        int spaces = ((Integer) res.lastElement()).intValue() +
        //the number of spaces before the keyword
                SchemeIndenting.getSpaces(name) + //The number of spaces to
                // indent
                ((from == 1) ? ((Integer) res.elementAt(1)).intValue() : 0);
        // length of the keyword

        int newPos = 0; // so it's in scope for error output
        String str = "";
        try {
            str = getText(0, caretPos) + "\n"; // get text from 0 to caret Pos.
            newPos = caretPos + 1; // for newline
            String spacesToIndent = "";

            while (spaces-- > 0) { // Add the number of spaces to indent.
                spacesToIndent = spacesToIndent + " ";
                // The number of spaces to indent
                newPos += 1; // each new space
            }

            String textAfterCaret = getText(caretPos, getDocument().getLength()
                    - caretPos);
            LinkedList trimmedAndString = indentAndTrim(textAfterCaret,
                    spacesToIndent);
            str += (String) trimmedAndString.get(1);
            newPos -= ((Integer) (trimmedAndString.get(0))).intValue();

            // where does newPos get used?

            setText(str);
            setCaretPosition(caretPos + 1);

        } catch (BadLocationException ex) {
            ex.printStackTrace();
            System.err.println("getDocument().getLength(): "
                    + getDocument().getLength());
            System.err.println("  caretPos: " + caretPos + " , newPos: "
                    + newPos);
        }
    }

    /**
     * Return selected text
     */
    public String getSelection() {
        try {
            return getDocument().getText(getSelectionStart(),
                    getSelectionEnd() - getSelectionStart());
        } catch (BadLocationException e) {
            return new String("");
        }
    }

    /**
     * Return s-expression
     */
    public String getSExpression() {
        try {
            int pos = getCaretPosition();
            // If it's a ')' on the left, then move left 1 character, so we can
            // select the s-expr to the left
            int sv = styles.getScanner().getToken(pos - 1);
            if (sv == Token.T_12.getScanValue())
                pos--;

            int start = findSExpressionStart(pos - 1);
            int end = findSExpressionEnd(start + 1);

            flashText(start, end, 150);

            return getDocument().getText(start, end - start);
        } catch (BadLocationException e) {
            return new String("");
        }
    }

    public void setSelection(int xStart, int xFinish, boolean moveUp) {
        if (moveUp) {
            setCaretPosition(xFinish);
            moveCaretPosition(xStart);
        } else
            select(xStart, xFinish);
    }

    /**
     * Reset the token fonts cache so it can be restylized.
     */
    void resetBufferFont() {
        styles.resetTokenFont();
    }

    /**
     * Find the starting position of a s-expression
     */
    protected int findSExpressionStart(int end) {
        TokenScanner lexer = styles.getScanner();
        int i = end, level = 0;

        while (i >= 0) {
            int v = lexer.getToken(i);
            i = lexer.posStart - 1;

            if (v == Token.T_11.getScanValue()) {
                if (--level < 0)
                    return i + 1;
            } else if (v == Token.T_12.getScanValue()) {
                level++;
            }
        }
        return -1;
    }

    /**
     * Find the type of current level expression (first argument after '('),
     * length of it, and also the number of spaces before the argument (till a
     * newline). Return a vector containing the 2 numbers
     */
    protected Vector findExpressionType(int pos) {

        Vector res = new Vector();
        TokenScanner lexer = styles.getScanner();
        int token = -1, i = pos, start = 0, nl = 0, level = 0, len = 0;
        boolean addExtraSpace = false;

        while (i >= 0) {
            int v = lexer.getToken(i);
            i = lexer.posStart - 1;
            //System.out.println("In find Exp type with i : " + i);
            //System.out.println("v is : " + Token.getRepresentation(v));
            // remember the type of token
            if (level == 0 && v != Token.COMMENT.getScanValue()
                    && v != Token.WHITESPACE.getScanValue()
                    && v != Token.NEWLINE.getScanValue()
                    && v != Token.T_11.getScanValue()
                    && v != Token.T_12.getScanValue()) {

                token = v;
                //System.out.println("Setting token to : " + token);
                start = i + 1;

                //if found start opening position. Mark start as this
                // position.
            } else if (level == 1 && v == Token.T_11.getScanValue())
                start = i;

            //System.out.println("Token is : " + token);
            //Paren matching.
            if (v == Token.T_11.getScanValue()) {
                if (--level < 0 && token != -1)
                    break;
                else if (level < 0) {
                    //special case: if you didn't break when level < 0, reset
                    // it to 0; (ie: pushing return after "if ( "
                    level = 0;
                    addExtraSpace = true;
                }
            } else if (v == Token.T_12.getScanValue()) {
                level++;
            }
        }

        // type of expression (scan value)

        res.add(new Integer(token));
        if (Token.getRepresentation(token) != null
                && !Token.getRepresentation(token).equals("unknown")) {
            len = Token.getRepresentation(token).length();
            //the length of this token
        } else
            len = 0;

        res.add(new Integer(len));

        while (i >= 0) {
            int v = lexer.getToken(i);
            i = lexer.posStart - 1;
            nl = i + 2;
            if (v == Token.NEWLINE.getScanValue())
                break;
        }

        if (token != -1) {
            if (addExtraSpace) //special case for when user leaves only an
                // open bracket on the line.
                res.add(new Integer(start - nl + 1));
            else
                res.add(new Integer(start - nl));
        } else
            res.add(new Integer(0));

        //System.out.println("res is : " + res.toString());

        return res;
    }

    /**
     * Find the ending position of a s-expression
     */
    protected int findSExpressionEnd(int start) {
        TokenScanner lexer = styles.getScanner();
        int i = start, level = 0;

        while (i < getDocument().getLength()) {
            int v = lexer.getToken(i);
            i = lexer.posEnd;

            if (v == Token.T_12.getScanValue()) {
                if (--level < 0)
                    return i;
            } else if (v == Token.T_11.getScanValue()) {
                level++;
            }
        }
        return -1;
    }

    public static boolean inQuotes(String text, int pos) {
        int quoteStart = text.indexOf('"');
        int quoteEnd = text.lastIndexOf('"');

        if (quoteStart == quoteEnd && pos > quoteStart && quoteStart != -1) { // IN
            // QUOTES
            return true;
        } else if (pos > quoteStart && pos < quoteEnd) { //IN QUOTES
            return true;
        } else {
            return false;
        }
    }

    protected void parensFlash() {
        int start = findSExpressionStart(getCaretPosition() - 2);
        final int end = getCaretPosition() - 1;
        int textLength = end - start;
        String currentLineOfText = null;
        try {
            currentLineOfText = getDocument().getText(start, textLength);
        } catch (BadLocationException e) { // This actually catches mismatched
            // parens ie: Extra parens
            currentLineOfText = getText();
        }

        if (!inQuotes(currentLineOfText, end)) { // Not in quotes -> do parens
            // matching

            if (start >= 0) {
                flashText(start, end, 300);
            } else {
                for (int n = 0; n < 3; n++) {
                    flashText(start, end, 150);
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        ;
                    }
                }
            }
        }
    }

    /*
     * Remove all non-newline WhiteSpace Chars from the given string and then
     * Indent. Return a LinkedList where the first element is the number of
     * spaces trimmed and the second element is the resulting string.
     */
    public LinkedList indentAndTrim(String s, String indent) {
        //System.out.println("inside indentAndTrim");
        String result = "";
        int i, trimmed = 0;
        LinkedList returnVal = new LinkedList();
        boolean onFirstLine = true;

        for (i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') {
                onFirstLine = false;
                result += s.charAt(i);
            } else if (!Character.isWhitespace(s.charAt(i)))
                break;
            else if (onFirstLine)
                trimmed++;
        }

        result += (indent + s.substring(i, s.length()));
        returnVal.add(new Integer(trimmed));
        returnVal.add(result);

        return returnVal;
    }

    protected void flashText(int start, int end, int millisec) {
        int pos = getCaretPosition();

        setSelectionStart(start);
        setSelectionEnd(end);
        paintImmediately(0, 0, getWidth(), getHeight());
        try {
            Thread.sleep(millisec);
        } catch (Exception e) {
            ;
        }

        setSelectionStart(pos);
        setSelectionStart(pos);
        paintImmediately(0, 0, getWidth(), getHeight());
    }

    /**
     * Get stylization from properties file
     */
    protected int[] splitPrefs(Object s) {
        int[] result = new int[5];
        StringTokenizer st = new StringTokenizer(s.toString());
        for (int i = 0; i < 5; i++) {
            result[i] = Integer.parseInt(st.nextToken());
        }
        return result;
    }

    protected void stylizeAppearance() {
        Properties props = new Properties();

        try {
            java.net.URL url = AbstractSchemeEditorPane.class
                    .getResource(STYLES_RESOURCE);
            if (url == null)
                throw new MissingResourceException(
                        "Appearance resource missing", "Properties",
                        STYLES_RESOURCE);
            props.load(url.openStream());
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        StringTokenizer st;

        st = new StringTokenizer(props.get("FontFamily").toString());
        String temp = st.nextToken();
        stylizeFont(temp, Integer.parseInt(st.nextToken()));

        // Hmm function pointers would have been really handy here
        // Probably rewrite this again in the next iteration, but this
        // would involve digging into the Tokens code perhaps

        int[] p;
        p = splitPrefs(props.get("Default"));
        stylizeDefault(p[0], p[1], p[2], p[3] == 1, p[4] == 1);
        p = splitPrefs(props.get("BoolVal"));
        stylizeBoolVal(p[0], p[1], p[2], p[3] == 1, p[4] == 1);
        p = splitPrefs(props.get("CharVal"));
        stylizeCharVal(p[0], p[1], p[2], p[3] == 1, p[4] == 1);
        p = splitPrefs(props.get("NumVal"));
        stylizeNumVal(p[0], p[1], p[2], p[3] == 1, p[4] == 1);
        p = splitPrefs(props.get("StrVal"));
        stylizeStrVal(p[0], p[1], p[2], p[3] == 1, p[4] == 1);
        p = splitPrefs(props.get("Keyword"));
        stylizeKeyword(p[0], p[1], p[2], p[3] == 1, p[4] == 1);
        p = splitPrefs(props.get("Identifier"));
        stylizeIdentifier(p[0], p[1], p[2], p[3] == 1, p[4] == 1);

        /*
         * This was in the old code, but not in the new properties file. Why? p =
         * splitPrefs(props.get("Operator"));
         */

        p = splitPrefs(props.get("Comment"));
        stylizeComment(p[0], p[1], p[2], p[3] == 1, p[4] == 1);
    }

    /* BUFFER STYLIZATION FUNCTIONALITY */

    /**
     * Set font and size
     */
    public void stylizeFont(String font, int size) {

        for (int i = 0; i < Token.all.length; i++) {
            Style s = styles.getStyleForScanValue(Token.all[i].getScanValue());
            StyleConstants.setFontFamily(s, font);
            StyleConstants.setFontSize(s, size);
        }
        setFont(new Font(font, Font.BOLD, size));
    }

    /**
     * Set default text attributes for all Scheme texts
     */
    public void stylizeDefault(int r, int g, int b, boolean bold, boolean italic) {
        for (int i = 0; i < Token.all.length; i++) {
            setStyleConstants(styles.getStyleForScanValue(Token.all[i]
                    .getScanValue()), new Color(r, g, b), bold, italic);
        }
    }

    /**
     * Set text attributes for Scheme operators
     */
    public void stylizeOperator(int r, int g, int b, boolean bold,
            boolean italic) {
        for (int i = 0; i < Token.operators.length; i++) {
            setStyleConstants(styles.getStyleForScanValue(Token.operators[i]
                    .getScanValue()), new Color(r, g, b), bold, italic);
        }
    }

    /**
     * Set text attributes for Scheme keywords
     */
    public void stylizeKeyword(int r, int g, int b, boolean bold, boolean italic) {
        for (int i = 0; i < Token.keywords.length; i++) {
            setStyleConstants(styles.getStyleForScanValue(Token.keywords[i]
                    .getScanValue()), new Color(r, g, b), bold, italic);
        }
    }

    /**
     * Set text attributes for Scheme identifiers
     */
    public void stylizeIdentifier(int r, int g, int b, boolean bold,
            boolean italic) {
        setStyleConstants(styles.getStyleForScanValue(Token.IDENT
                .getScanValue()), new Color(r, g, b), bold, italic);
    }

    /**
     * Set text attributes for Scheme bool values
     */
    public void stylizeBoolVal(int r, int g, int b, boolean bold, boolean italic) {
        setStyleConstants(styles.getStyleForScanValue(Token.BOOL_VAL
                .getScanValue()), new Color(r, g, b), bold, italic);
    }

    /**
     * Set text attributes for Scheme char values
     */
    public void stylizeCharVal(int r, int g, int b, boolean bold, boolean italic) {
        setStyleConstants(styles.getStyleForScanValue(Token.CHAR_VAL
                .getScanValue()), new Color(r, g, b), bold, italic);
    }

    /**
     * Set text attributes for Scheme number values
     */
    public void stylizeNumVal(int r, int g, int b, boolean bold, boolean italic) {
        setStyleConstants(styles.getStyleForScanValue(Token.NUM_VAL
                .getScanValue()), new Color(r, g, b), bold, italic);
    }

    /**
     * Set text attributes for Scheme string values
     */
    public void stylizeStrVal(int r, int g, int b, boolean bold, boolean italic) {
        setStyleConstants(styles.getStyleForScanValue(Token.STR_VAL
                .getScanValue()), new Color(r, g, b), bold, italic);
    }

    /**
     * Set text attributes for Scheme comments
     */
    public void stylizeComment(int r, int g, int b, boolean bold, boolean italic) {
        setStyleConstants(styles.getStyleForScanValue(Token.COMMENT
                .getScanValue()), new Color(r, g, b), bold, italic);
    }

    /**
     * Set text attributes for other Scheme tokens
     */
    public void stylizeUnknown(int r, int g, int b, boolean bold, boolean italic) {
        setStyleConstants(styles.getStyleForScanValue(Token.UNKNOWN
                .getScanValue()), new Color(r, g, b), bold, italic);
    }

    protected void setStyleConstants(Style s, Color color, boolean bold,
            boolean italic) {
        StyleConstants.setForeground(s, color);
        StyleConstants.setBold(s, bold);
        StyleConstants.setItalic(s, italic);
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
