package jist.listener;

import sisc.data.Pair;
import sisc.data.Quantity;
import sisc.data.Value;
import sisc.interpreter.Interpreter;
import sisc.nativefun.IndexedLibraryAdapter;
import sisc.nativefun.IndexedProcedure;

/**
 * Turtle is a SISC module
 * 
 * <a href="http://sisc.sourceforge.net/manual/html/ch10.html">SISC module docs
 * </a>
 * 
 * @see sisc.util.Util
 * @see jist.listener.SchemeTurtleGraphics
 * 
 * @author Turadg
 * @version $Id: Turtle.java,v 1.6 2004/11/09 22:18:30 turadg Exp $
 */
public class Turtle extends IndexedProcedure {

    /**
     * object for state of graphics window
     * 
     * FIX rewrite this class 1) so it's internal 2) so the methods use Scheme
     * types directly 3) so we have the copyright on the new code
     */
    static SchemeTurtleGraphics tg = new SchemeTurtleGraphics();

    /**
     * IDs of module primitives
     */
    public static final int BACK = 1, CLEAN = 2, CLEARSCREEN = 3, SHOWNP = 41,
    // added out of sequence
            DISTANCETOXY = 4, DRAW = 5, FENCE = 6, FORWARD = 7,
            GETBACKGROUND = 8, GETPENCOLOR = 39,
            // added out of sequence
            HEADING = 9, HIDETURTLE = 10, HOME = 11, LABEL = 12, LEFT = 13,
            NODRAW = 14, PENDOWN = 15, PENERASE = 16, PENUP = 17, POS = 18,
            REFRESH = 19, REFRESHINTERVAL = 20, RIGHT = 21, SETBACKGROUND = 22,
            SETHEADING = 23, SETPENCOLOR = 24, SETX = 25, SETXY = 26,
            SETY = 27, SHOWTURTLE = 28, TOWARDSXY = 29, WINDOW = 30, WRAP = 31,
            XCOR = 32, XSIZE = 33, YCOR = 34, YSIZE = 35, SETPALETTE = 36,
            UNSETPALETTE = 37, PALETTE = 40,
            // added out of sequence
            PALETTEP = 38;

    public static class Index extends IndexedLibraryAdapter {

        public Value construct(int id) {
            return new Turtle(id);
        }

        public Index() {
            define("back", BACK);
            define("clean", CLEAN);
            define("clearscreen", CLEARSCREEN);
            define("shown?", SHOWNP);
            define("distancetoxy", DISTANCETOXY);
            define("draw", DRAW);
            define("fence", FENCE);
            define("forward", FORWARD);
            define("getbackground", GETBACKGROUND);
            define("getpencolor", GETPENCOLOR);
            define("heading", HEADING);
            define("hideturtle", HIDETURTLE);
            define("home", HOME);
            define("label", LABEL);
            define("left", LEFT);
            define("nodraw", NODRAW);
            define("pendown", PENDOWN);
            define("penerase", PENERASE);
            define("penup", PENUP);
            define("pos", POS);
            define("refresh", REFRESH);
            define("refreshinterval", REFRESHINTERVAL);
            define("right", RIGHT);
            define("setbackground", SETBACKGROUND);
            define("setheading", SETHEADING);
            define("setpencolor", SETPENCOLOR);
            define("setx", SETX);
            define("setxy", SETXY);
            define("sety", SETY);
            define("showturtle", SHOWTURTLE);
            define("towardsxy", TOWARDSXY);
            define("window", WINDOW);
            define("wrap", WRAP);
            define("xcor", XCOR);
            define("xsize", XSIZE);
            define("ycor", YCOR);
            define("ysize", YSIZE);
            define("setpalette", SETPALETTE);
            define("unsetpalette", UNSETPALETTE);
            define("palette", PALETTE);
            define("palette?", PALETTEP);

            // shortcuts
            define("bk", BACK);
            define("cs", CLEARSCREEN);
            define("fd", FORWARD);
            define("getbg", GETBACKGROUND);
            define("getpc", GETPENCOLOR);
            define("ht", HIDETURTLE);
            define("lt", LEFT);
            define("pd", PENDOWN);
            define("pe", PENERASE);
            define("rt", RIGHT);
            define("setbg", SETBACKGROUND);
            define("setpc", SETPENCOLOR);
            define("st", SHOWTURTLE);
        }
    }

    public Turtle(int id) {
        super(id);
    }

    public Turtle() {
    }

    public Value doApply(Interpreter r) {
        /*
         * procedure arguments are stored in r.vlr numArgs = r.vlr.length; first
         * arg = r.vlr[0]
         */

        switch (r.vlr.length) {
        // Zero argument functions
        case 0:
            switch (id) {
            // FIX move and copy functions to other arg lengths as appropriate
            case CLEAN:
                tg.pClean();
                return VOID;
            case CLEARSCREEN:
                tg.pClearscreen();
                return VOID;
            case SHOWNP:
                return truth(tg.pShown());
            case DRAW:
                // also a 2-arg
                tg.pDraw();
                return VOID;
            case FENCE:
                tg.pFence();
                return VOID;
            case GETBACKGROUND:
                return toSchemeList(tg.pGetbackground());
            case GETPENCOLOR:
                return toSchemeList(tg.pGetpencolor());
            case HEADING:
                return Quantity.valueOf(tg.pHeading());
            case HIDETURTLE:
                tg.pHideturtle();
                return VOID;
            case HOME:
                tg.pHome();
                return VOID;
            case NODRAW:
                tg.pNodraw();
                return VOID;
            case PENDOWN:
                tg.pPendown();
                return VOID;
            case PENERASE:
                tg.pPenerase();
                return VOID;
            case PENUP:
                tg.pPenup();
                return VOID;
            case POS:
                return toSchemeList(tg.pPos());
            case REFRESH:
                tg.pRefresh();
                return VOID;
            case SHOWTURTLE:
                tg.pShowturtle();
                return VOID;
            case WINDOW:
                tg.pWindow();
                return VOID;
            case WRAP:
                tg.pWrap();
                return VOID;
            case XCOR:
                return Quantity.valueOf(tg.pXcor());
            case XSIZE:
                return Quantity.valueOf(tg.pXsize());
            case YCOR:
                return Quantity.valueOf(tg.pYcor());
            case YSIZE:
                return Quantity.valueOf(tg.pYsize());
            default:
                break;
            }

        // One argument functions
        case 1:
            Value arg = r.vlr[0];
            switch (id) {
            case BACK:
                tg.pBack(num(arg).doubleValue());
                return VOID;
            case FORWARD:
                tg.pForward(num(arg).doubleValue());
                return VOID;
            case LABEL:
                tg.pLabel(string(arg));
                return VOID;
            case LEFT:
                tg.pLeft(num(arg).doubleValue());
                return VOID;
            case REFRESHINTERVAL:
                tg.pRefreshinterval(num(arg).intValue());
                return VOID;
            case RIGHT:
                tg.pRight(num(arg).doubleValue());
                return VOID;
            case SETBACKGROUND:
                tg.pSetbackground(string(arg));
                return VOID;
            case SETHEADING:
                tg.pSetheading(num(arg).doubleValue());
                return VOID;
            case SETPENCOLOR:
                tg.pSetpencolor(string(arg));
                return VOID;
            case SETX:
                tg.pSetx(num(arg).doubleValue());
                return VOID;
            case SETY:
                tg.pSety(num(arg).doubleValue());
                return VOID;
            case UNSETPALETTE:
                tg.pUnsetpalette(string(arg));
                return VOID;
            case PALETTE:
                return toSchemeList(tg.pPalette(string(arg)));
            case PALETTEP:
                return truth(tg.pPalettep(string(arg)));
            default:
                break;
            }

        // Two argument functions
        case 2:
            // all two-arg procs take two numbers
            // so make these handy conversions
            int i1 = num(r.vlr[0]).intValue();
            int i2 = num(r.vlr[1]).intValue();
            double d1 = num(r.vlr[0]).doubleValue();
            double d2 = num(r.vlr[1]).doubleValue();
            switch (id) {
            case DRAW:
                // also a 0-arg
                tg.pDraw(i1, i2);
                return VOID;
            case DISTANCETOXY:
                tg.pDistancetoxy(d1, d2);
                return VOID;
            case SETXY:
                tg.pSetxy(d1, d2);
                return VOID;
            case TOWARDSXY:
                return Quantity.valueOf(tg.pTowardsxy(d1, d2));
            default:
                break;
            }

        // Three argument functions (none)
        case 3:
            switch (id) {
            default:
                break;
            }

        // Four argument functions
        case 4:
            switch (id) {
            case SETPALETTE:
                String arg1 = string(r.vlr[0]);
                int arg2 = num(r.vlr[1]).intValue();
                int arg3 = num(r.vlr[2]).intValue();
                int arg4 = num(r.vlr[3]).intValue();
                tg.pSetpalette(arg1, arg2, arg3, arg4);
                return VOID;
            default:
                break;
            }
        }

        // no cases matched
        throw new RuntimeException("Invalid number of arguments to function "
                + r.acc);
    }

    // FIX rewrite SchemeTurtleGraphics as inner class that uses Scheme vals
    static Pair toSchemeList(double[] nums) {
        Value[] vals = new Value[nums.length];
        for (int i = 0; i < nums.length; i += 1)
            vals[i] = Quantity.valueOf(nums[i]);
        return valArrayToList(vals, 0, vals.length);
    }

    static Pair toSchemeList(int[] nums) {
        Value[] vals = new Value[nums.length];
        for (int i = 0; i < nums.length; i += 1)
            vals[i] = Quantity.valueOf(nums[i]);
        return valArrayToList(vals, 0, vals.length);
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
