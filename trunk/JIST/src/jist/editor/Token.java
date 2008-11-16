package jist.editor;

import java.io.Serializable;

/**
 * Simple class to represent a lexical token in Scheme. This includes the
 * Symbols used by jsint.Symbol as well as punctuations, etc to provide a
 * convenient class that can be stored as a attribute value.
 */
public class Token implements Serializable {

	Token(String representation, int scanValue) {
		this.representation = representation;
		this.scanValue = scanValue;
	}

	/**
	 * A human presentable form of the token, useful for things like lists,
	 * debugging, etc.
	 */
	public String toString() {
		return representation;
	}

	/**
	 * Numeric value of this token. This is the value returned by the scanner
	 * and is the tie between the lexical scanner and the tokens.
	 */
	public int getScanValue() {
		return scanValue;
	}

	/**
	 * Specifies the category of the token as a string that can be used as a
	 * label.
	 */
	public String getCategory() {
		return getClass().getName();
	}

	/**
	 * Look for representation by scan value
	 */
	public static String getRepresentation(int scanValue) {
		for (int i = 0; i < all.length; i++)
			if (all[i].getScanValue() == scanValue)
				return all[i].toString();
		return null;
	}

	/**
	 * Returns a hashcode for this set of attributes.
	 */
	public final int hashCode() {
		return scanValue;
	}

	/**
	 * Compares this object to the specifed object. The result is true if and
	 * only if the argument is not null and is a Font object with the same name,
	 * style, and point size as this font.
	 */
	public final boolean equals(Object obj) {
		if (obj instanceof Token)
			return (scanValue == ((Token) obj).scanValue);
		else
			return false;
	}

	/**
	 * Key to be used in AttributeSet's holding a value of Token.
	 */
	public static final Object TokenAttribute = new AttributeKey();

	protected String representation;

	protected int scanValue;

	private static class AttributeKey {
		private AttributeKey() {
		}

		public String toString() {
			return "token";
		}
	}

	public static class Operator extends Token {
		Operator(String representation, int scanValue) {
			super(representation, scanValue);
		}
	}

	public static class Value extends Token {
		Value(String representation, int scanValue) {
			super(representation, scanValue);
		}
	}

	public static class Keyword extends Token {
		Keyword(String representation, int scanValue) {
			super(representation, scanValue);
		}
	}

	public static class ExpressionKeyword extends Token {
		ExpressionKeyword(String representation, int scanValue) {
			super(representation, scanValue);
		}
	}

	public static class Special extends Token {
		Special(String representation, int scanValue) {
			super(representation, scanValue);
		}
	}

	public static final int BASEINSTANCE = 0;

	public static final int MAXINSTANCE = BASEINSTANCE + 74;

	public static final Token NONE = new Token("", BASEINSTANCE);

	/*
	 * Operators
	 */
	public static final Token T_01 = new Operator("~", BASEINSTANCE + 1);

	public static final Token T_02 = new Operator("`", BASEINSTANCE + 2);

	public static final Token T_03 = new Operator("!", BASEINSTANCE + 3);

	public static final Token T_04 = new Operator("@", BASEINSTANCE + 4);

	public static final Token T_05 = new Operator("#", BASEINSTANCE + 5);

	public static final Token T_06 = new Operator("$", BASEINSTANCE + 6);

	public static final Token T_07 = new Operator("%", BASEINSTANCE + 7);

	public static final Token T_08 = new Operator("^", BASEINSTANCE + 8);

	public static final Token T_09 = new Operator("&", BASEINSTANCE + 9);

	public static final Token T_10 = new Operator("*", BASEINSTANCE + 10);

	public static final Token T_11 = new Operator("(", BASEINSTANCE + 11);

	public static final Token T_12 = new Operator(")", BASEINSTANCE + 12);

	public static final Token T_13 = new Operator("_", BASEINSTANCE + 13);

	public static final Token T_14 = new Operator("-", BASEINSTANCE + 14);

	public static final Token T_15 = new Operator("+", BASEINSTANCE + 15);

	public static final Token T_16 = new Operator("=", BASEINSTANCE + 16);

	public static final Token T_17 = new Operator("\\", BASEINSTANCE + 17);

	public static final Token T_18 = new Operator(":", BASEINSTANCE + 18);

	public static final Token T_19 = new Operator(";", BASEINSTANCE + 19);

	public static final Token T_20 = new Operator("\"", BASEINSTANCE + 20);

	public static final Token T_21 = new Operator("'", BASEINSTANCE + 21);

	public static final Token T_22 = new Operator("<", BASEINSTANCE + 22);

	public static final Token T_23 = new Operator(">", BASEINSTANCE + 23);

	public static final Token T_24 = new Operator(",", BASEINSTANCE + 24);

	public static final Token T_25 = new Operator(".", BASEINSTANCE + 25);

	public static final Token T_26 = new Operator("?", BASEINSTANCE + 26);

	public static final Token T_27 = new Operator("/", BASEINSTANCE + 27);

	/*
	 * Value tokens
	 */
	public static final Token IDENT = new Value("identifier", BASEINSTANCE + 31);

	public static final Token BOOL_VAL = new Value("boolean", BASEINSTANCE + 32);

	public static final Token CHAR_VAL = new Value("character",
			BASEINSTANCE + 33);

	public static final Token NUM_VAL = new Value("number", BASEINSTANCE + 34);

	public static final Token STR_VAL = new Value("string", BASEINSTANCE + 35);

	public static final Token OTHER_VAL = new Value("other", BASEINSTANCE + 36);

	/*
	 * Keywords
	 */
	public static final Token KEY_01 = new Keyword("and", BASEINSTANCE + 41);

	public static final Token KEY_02 = new Keyword("begin", BASEINSTANCE + 42);

	public static final Token KEY_03 = new Keyword("case", BASEINSTANCE + 43);

	public static final Token KEY_04 = new Keyword("cond", BASEINSTANCE + 44);

	public static final Token KEY_05 = new Keyword("define", BASEINSTANCE + 45);

	public static final Token KEY_06 = new Keyword("delay", BASEINSTANCE + 46);

	public static final Token KEY_07 = new Keyword("do", BASEINSTANCE + 47);

	public static final Token KEY_08 = new Keyword("else", BASEINSTANCE + 48);

	public static final Token KEY_09 = new Keyword("if", BASEINSTANCE + 49);

	public static final Token KEY_10 = new Keyword("lambda", BASEINSTANCE + 50);

	public static final Token KEY_11 = new Keyword("let", BASEINSTANCE + 51);

	public static final Token KEY_12 = new Keyword("let*", BASEINSTANCE + 52);

	public static final Token KEY_13 = new Keyword("letrec", BASEINSTANCE + 53);

	public static final Token KEY_14 = new Keyword("or", BASEINSTANCE + 54);

	public static final Token KEY_15 = new Keyword("quasiquote",
			BASEINSTANCE + 55);

	public static final Token KEY_16 = new Keyword("quote", BASEINSTANCE + 56);

	public static final Token KEY_17 = new Keyword("set!", BASEINSTANCE + 57);

	public static final Token KEY_18 = new Keyword("unquote", BASEINSTANCE + 58);

	public static final Token KEY_19 = new Keyword("unquote-splicing",
			BASEINSTANCE + 59);

	/*
	 * Special tokens
	 */
	public static final Token COMMENT = new Special("comment",
			BASEINSTANCE + 71);

	public static final Token WHITESPACE = new Special("whitespace",
			BASEINSTANCE + 72);

	public static final Token NEWLINE = new Special("newline",
			BASEINSTANCE + 73);

	public static final Token UNKNOWN = new Special("unknown",
			BASEINSTANCE + 74);

	public static Token[] operators = { T_01, T_02, T_03, T_04, T_05, T_06,
			T_07, T_08, T_09, T_10, T_11, T_12, T_13, T_14, T_15, T_16, T_17,
			T_18, T_19, T_20, T_21, T_22, T_23, T_24, T_25, T_26, T_27 };

	public static Token[] values = { IDENT, BOOL_VAL, CHAR_VAL, NUM_VAL,
			STR_VAL, OTHER_VAL };

	public static Token[] keywords = { KEY_01, KEY_02, KEY_03, KEY_04, KEY_05,
			KEY_06, KEY_07, KEY_08, KEY_09, KEY_10, KEY_11, KEY_12, KEY_13,
			KEY_14, KEY_15, KEY_16, KEY_17, KEY_18, KEY_19 };

	public static Token[] all = { T_01, T_02, T_03, T_04, T_05, T_06, T_07,
			T_08, T_09, T_10, T_11, T_12, T_13, T_14, T_15, T_16, T_17, T_18,
			T_19, T_20, T_21, T_22, T_23, T_24, T_25, T_26, T_27, IDENT,
			BOOL_VAL, CHAR_VAL, NUM_VAL, STR_VAL, OTHER_VAL, KEY_01, KEY_02,
			KEY_03, KEY_04, KEY_05, KEY_06, KEY_07, KEY_08, KEY_09, KEY_10,
			KEY_11, KEY_12, KEY_13, KEY_14, KEY_15, KEY_16, KEY_17, KEY_18,
			KEY_19, COMMENT, WHITESPACE, NEWLINE, UNKNOWN };

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
