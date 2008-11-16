(import s2j)
(import generic-procedures)

;;Create SISC Java classes
(define <intArray> (java-class "int[]"))
(define <doubleArray> (java-class "double[]"))

(define <SchemeTurtleGraphics> (java-class "jist.scheme.SchemeTurtleGraphics"))
(define graphics (make <SchemeTurtleGraphics>))


;;Generic Functions defined and then scheme function wrappers to hide S2J calls.

(define-generic p-back)
(define (back x) (output (p-back graphics (->jdouble x))))
(define (bk x)   (output (p-back graphics (->jdouble x))))

(define-generic p-clean)
(define (clean) (output (p-clean graphics)))

(define-generic p-clearscreen)
(define (clearscreen) (output (p-clearscreen graphics)))
(define (cs) (output (p-clearscreen graphics)))

(define-generic p-shown)
(define (shown?) (output(p-shown graphics)))

(define-generic p-distancetoxy)
(define (distancetoxy x y) (output(p-distancetoxy graphics (->jdouble x) (->jdouble y))))

(define-generic p-draw)
(define-generic draw)
(define-method (draw) (output(p-draw graphics)))
(define-method (draw (<number> x) (<number> y)) (output (p-draw graphics (->jint x) (->jint y))))

(define-generic p-fence)
(define (fence) (output (p-fence graphics)))

(define-generic p-forward)
(define (forward x) (output (p-forward graphics (->jdouble x))))
(define (fd x) (output (p-forward graphics (->jdouble x))))

(define-generic p-getbackground)
(define (getbackground) (output (p-getbackground graphics)))
(define (getbg) (output (p-getbackground graphics)))

(define-generic p-getpencolor)
(define (getpencolor) (output (p-getpencolor graphics)))
(define (getpc) (output (p-getpencolor graphics)))

(define-generic p-heading)
(define (heading) (output (p-heading graphics)))

(define-generic p-hideturtle)
(define (hideturtle)(output (p-hideturtle graphics)))
(define (ht)(output (p-hideturtle graphics)))

(define-generic p-home)
(define (home) (output (p-home graphics)))

(define-generic p-label)
(define (label str) (output (p-label graphics (->jstring str))))

(define-generic p-left)
(define (left turn) (output (p-left graphics (->jdouble turn))))
(define (lt turn) (output (p-left graphics (->jdouble turn))))

(define-generic p-nodraw)
(define (nodraw) (output (p-nodraw graphics)))

(define-generic p-pendown)
(define (pendown) (output (p-pendown graphics)))
(define (pd) (output (p-pendown graphics)))

(define-generic p-penerase)
(define (penerase) (output (p-penerase graphics)))
(define (pe)  (output (p-penerase graphics)))

(define-generic p-penup)
(define (penup) (output (p-penup graphics)))
(define (pu)  (output (p-penup graphics)))

(define-generic p-pos)
(define (pos) (output(p-pos graphics)))

(define-generic p-refresh)
(define (prefresh) (output (p-refresh graphics)))

(define-generic p-refresh-interval)
(define (prefreshinterval x) (output (p-refresh-interval graphics (->jint x))))

(define-generic p-right)
(define (right turn) (output (p-right graphics (->jdouble turn))))
(define (rt turn) (output (p-right graphics (->jdouble turn))))

(define-generic p-setbackground)
(define (setbackground color) (output (p-setbackground graphics (->jstring color))))
(define (setbg color) (output (p-setbackground graphics (->jstring color))))

(define-generic p-setheading)
(define (setheading newh) (output (p-setheading graphics (->jdouble newh))))

(define-generic p-setpencolor)
(define (setpencolor color) (output (p-setbackground graphics (->jstring color))))
(define (setpc color) (output (p-setbackground graphics (->jstring color))))

(define-generic p-setx)
(define (setx newx) (output (p-setx graphics (->jdouble newx))))

(define-generic p-setxy)
(define (setxy newx newy) (output (p-setxy graphics (->jdouble newx) (->jdouble newy))))

(define-generic p-sety)
(define (sety newy) (output (p-sety graphics (->jdouble newy))))

(define-generic p-showturtle)
(define (showturtle) (output (p-showturtle graphics)))
(define (st) (output (p-showturtle graphics)))

(define-generic p-towardsxy)
(define (towardsxy newx newy) (output(p-towardsxy graphics (->jdouble newx) (->jdouble newy))))

(define-generic p-window)
(define (window) (output (p-window graphics)))

(define-generic p-wrap)
(define (wrap) (output (p-wrap graphics)))

(define-generic p-xcor)
(define (xcor) (output (p-xcor graphics)))

(define-generic p-xsize)
(define (xsize) (output (p-xsize graphics)))

(define-generic p-ycor)
(define (ycor) (output (p-ycor graphics)))

(define-generic p-ysize)
(define (ysize) (output(p-ysize graphics)))

(define-generic p-setpalette)
(define (setpalette color r g b) (output (p-setpalette graphics (->jstring color) (->jint r) (->jint g) (->jint b))))

(define-generic p-unsetpalette)
(define (unsetpalette color) (output (p-unsetpalette graphics (->jstring color))))

(define-generic p-palette)
(define (palette color) (output (p-palette graphics (->jstring color)))) 

(define-generic p-palettep)
(define (palettep color) (output(p-palettep (graphics (->jstring color)))))



;;Generic procedures for outputing results
(define-generic output)
(define-method (output (<jvoid> out)) <void> )
(define-method (output (<jdouble> out)) (->number out))
(define-method (output (<jint> out)) (->number out))
(define-method (output (<jboolean> out)) (->boolean out))
(define-method (output (<intArray> out)) 
 (output-num-helper (->list out)))
(define-method (output (<doubleArray> out))
 (output-num-helper (->list out)))

 
(define (output-num-helper list)
	(if (null? list)
		'()
	(cons (->number (car list)) (output-num-helper (cdr list)))))
	


;; Copyright (c) 2004 Regents of the University of California (Regents).
;; Created by Graduate School of Education, University of California at Berkeley.
;; 
;; This software is distributed under the GNU General Public License, v2.
;;  
;; Permission is hereby granted, without written agreement and without
;; license or royalty fees, to use, copy, modify, and distribute this
;; software and its documentation for any purpose, provided that the
;; above copyright notice and the following two paragraphs appear in
;; all copies of this software.
;; 
;; REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
;; LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
;; FOR A PARTICULAR PURPOSE. THE SOFTWAREAND ACCOMPANYING
;; DOCUMENTATION, IF ANY, PROVIDED HEREUNDER IS PROVIDED "AS IS".
;; REGENTS HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
;; ENHANCEMENTS, OR MODIFICATIONS.
;; 
;; IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
;; SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST
;; PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
;; DOCUMENTATION, EVEN IF REGENTS HAS BEEN ADVISED OF THE POSSIBILITY
;; OF SUCH DAMAGE.
