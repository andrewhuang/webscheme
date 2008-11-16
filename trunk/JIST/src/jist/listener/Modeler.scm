;; This is the back-end for the Replacement
;; Modeler based on Brian Gaeke's Replacement
;; Modeler.

;; This file also ties Modeler.java (the front-end)
;; to SISC.  

;; $Id: Modeler.scm,v 1.10 2004/03/03 00:26:10 turadg Exp $

; @author Jeff Wang


;; for SISC >1.8.1
(import s2j)
(import generic-procedures)


;Align removed
;Random Removed

;==========================================================
;Interface between the replacement modeler and the java gui
;==========================================================
(define-java-class <Modeler> |jist.listener.Modeler|)

(define-generic-java-methods
  model
  init)
    
;;Make a SISC Syntax Macro
(define-syntax model
  (syntax-rules ()
    ((model expr)
      (init (java-new <Modeler>  (java-wrap (quote expr)))))))
 

;================================================================
; Re-define functions inherent to STk that is used in the backend
;================================================================

(define (procedure-body proc)
  (\@procedure-properties::procedure-property proc 'definition))

(define (bound? symbol)
  (not
   (with-failure-continuation (lambda (error-record error-k) #t)
                              (lambda () (not (eval symbol))))))


;==============================================================================
; Interface between the replacement modeler and the STk interpreter
;==============================================================================

;; get-fn for stk:
; This version of the replacement modeler uses STk's "procedure-body" function
; to get at the innards of user-defined procedures.

(define (get-fn name)
  (let ((value (eval name)))
    (if (book-primitive? name value)
        value
        (procedure-body value))))


; Errors are added to the list named *the-errors*, which is checked by the
; front-end after each expression is evaluated.
(define *the-errors* '())

(define (eval-error . args)
  (let ((errtext (cons "*** Error: " args)))
    (if (not (member errtext *the-errors*))
      (set! *the-errors* (cons errtext *the-errors*)))))


;==========================================================
;Backend end replacement evaluation code
;==========================================================

;; (8/16/94 added map, reduce, filter, apply, and let* as "special forms")
;; This file requires GET-FN to be defined, and requires BOUND? and EVAL.
;; This file "exports" PART-EVAL, which takes an expression as its
;; argument and returns the same expression, evaluated by one step.
;; Also, it "exports" FULL-EVAL, which takes an expression as its
;; argument and returns the same expression, evaluated "all the way".
;; FULL-eval is different from Scheme's EVAL because, for example,
;; it returns a variable naming a procedure or a lambda expression
;; instead of a procedure.

;; Global variables:
;
; This is in support of CS3.
;
(define *they-know-lambda* #f)

;; STk always loads berkeley.scm.
(define (using-berkeley-scm?) #t)
(define *harvey+wright* (using-berkeley-scm?))
(define *grillmeyer* (not (using-berkeley-scm?)))

;; The evaluator

; This is one of the main entry points to the replacement modeler.
; It is used for doing a partial evaluation of a function.
; This must handle begin functions specially because SISC
; does not treat begin as a normal procedures/subroutine.
(define *the-expression-that-failed* #f)
(define (part-eval exp)
    (let ((result (cond ((basic? exp) exp)
    					((begin? exp) (handle-begin exp))
                        ((symbol? exp) (symeval exp))
                        ((special-form? exp) (eval-spec-form exp))
                        ((application? exp)
                         (eval-application exp))
                        (else (eval-error "Unrecognized expession type:" exp)))))
      result))

(define (begin? exp)
	(if (equal? (car exp) 'begin)
		#t
		#f))

(define (handle-begin exp)
   (quote-if-necessary (eval exp)))

;; Variables
;; Note that all local variables get substituted before the evaluator
;; gets to them, so any variable that you actually try to evaluate is
;; global and you can just eval it.
;; Maybe add a hack for global variables whose value is a function, e.g.,
;; (define foo +)...

; Used for finding the values of variables (symbols).
(define (symeval var)
  (cond ((member var *the-real-special-forms*)
         (eval-error "Can't evaluate the name of a special form: " var))
        ((not (bound? var)) (eval-error "Variable not bound: " var))
        (else (let ((val (eval var)))
                (if (procedure? val)
                    var
                    (quote-if-necessary val))))))

;; Special Forms
;; Note that quote and lambda expressions are treated as basic, so they
;; don't really count as special forms.  Also, some of the higher-order
;; functions we provide are treated as special forms, even though they're
;; really not special forms.

(define (eval-accumulate exp)
  (let ((fn (cadr exp))
        (stuff (map quote-if-necessary
                    ((if *grillmeyer* map every)
                     (lambda (x) x) (eval (caddr exp)))) ))
    (if (null? stuff)
        (list fn)
        (accum/reduce-expand fn stuff)) ))

(define (accum/reduce-expand fn stuff)
  (if (null? (cdr stuff))
      (car stuff)
      (if *grillmeyer*          
          ; left-to-right accumulation
          (accum/reduce-expand 
            fn 
            (cons (list fn (car stuff) (cadr stuff))
                  (cddr stuff)))
          ; Harvey+Wright right-to-left accumulation
          (list fn (car stuff)  
                (accum/reduce-expand fn (cdr stuff)) ) ) ) )

(define *not-basic* (cons 'not-basic '()))

(define (eval-and exp)
  (define (and-value exp)
    (cond ((not (basic? (car exp)))
           *not-basic*)
          ((null? (cdr exp)) (car exp))
          ((car exp) (and-value (cdr exp)))
          (else #f) ))
  (if (null? (cdr exp))
      #t
      (let ((ans (and-value (cdr exp))))
        (if (eq? ans *not-basic*)
            (cons 'and (make-next-basic (cdr exp)))
            ans)) ))

(define (eval-apply exp)
  (let* ((fn-exp (cadr exp))
         (lst-exp (caddr exp))
         (lst-elements (map quasiquote-if-necessary (cadr lst-exp))))
    (cons fn-exp lst-elements)))

  (define (eval-cond-try-next-clause clauses)
    (if (basic? (car (car clauses)))
        (cons (car clauses)
              (eval-cond-try-next-clause (cdr clauses)))
        (cons (cons (full-eval (car (car clauses)))
                    (cdr (car clauses)))
              (cdr clauses) )))
  (define (eval-cond-valid-shapes? clauses)
    (if (null? clauses)
        #t
        (let ((c (car clauses)))
          (and (list? c)
               (not (null? c))
               (<= (length c) 2)
               (if (eq? (car c) 'else)
                   (and (= (length c) 2) (null? (cdr clauses)))
                   (eval-cond-valid-shapes? (cdr clauses)) )))))
(define (eval-cond exp)
  (define (eval-cond-helper clauses)
    (cond ((null? clauses) 
           (eval-error "No true clauses or else clause in COND"))
          ((eq? (car (car clauses)) #f)
           (eval-cond-helper (cdr clauses)))
          ((eq? (car (car clauses)) 'else)
           (cadr (car clauses)) )
          ((basic? (car (car clauses)))
           (if (null? (cdr (car clauses)))
               (car (car clauses))
               (cadr (car clauses))))
          (else (cons 'cond (eval-cond-try-next-clause (cdr exp)))) ))
  (if (eval-cond-valid-shapes? (cdr exp))
      (eval-cond-helper (cdr exp))
      (eval-error "Bad COND syntax:" exp) ))

; Grillmeyer only
(define (eval-count-if exp)
  (let* ((fn-exp (cadr exp))
         (datum (cadr (caddr exp)))     ; Get rid of quote or quasiquote
         (list-elts (map quasiquote-if-necessary datum)))
    (cons '+
          (map (lambda (elt) `(if (,fn-exp ,elt) 1 0))
               list-elts)) ))

; Grillmeyer only
(define (eval-count-if-not exp)
  (let* ((fn-exp (cadr exp))
         (datum (cadr (caddr exp)))     ; Get rid of quote or quasiquote
         (list-elts (map quasiquote-if-necessary datum)))
    (cons '+
          (map (lambda (elt) `(if (,fn-exp ,elt) 0 1))
               list-elts))))

(define (eval-define exp)
  (eval-error "You can't use this evaluator for DEFINE") )

; Grillmeyer and Harvey+Wright have different versions.
(define (eval-every exp)
  (if *grillmeyer*         
      ; Grillmeyer version, returns #t or #f
      (let* ((fn-exp (cadr exp))
             (datum (cadr (caddr exp)))     ; Get rid of quote or quasiquote
             (list-elts (map quasiquote-if-necessary datum)))
        (append
          (cons 'and
              (map (lambda (elt) (list fn-exp elt))
                   list-elts))
          '(#t)) )
      
      ; Harvey+Wright word/sent version of map
      (let* ((fn-exp (cadr exp))
             (datum (eval (caddr exp)))
             (exploded (map quote-if-necessary (every (lambda (x) x) datum))))
        ;; EXPLODED has two purposes:
        ;; 1 -- Turn a word into a list of letters.
        ;; 2 -- Generate an error if datum isn't a word or sentence.
        (cons 'se
              (map (lambda (elt) (list fn-exp elt))
                   exploded)) )))

; Used by Harvey & Wright to evaluate their filter, and used by Grillmeyer
; to evaluate his keep-if.
(define (eval-filter exp)
  (let* ((fn-exp (cadr exp))
         (datum (cadr (caddr exp)))     ; Get rid of quote or quasiquote
         (list-elts (map quasiquote-if-necessary datum)))
    (cons 'append
          (map (lambda (elt) `(if (,fn-exp ,elt) (list ,elt) '()))
               list-elts)) ))

(define (eval-find-if exp) #t)

(define (eval-find-if-not exp) #t)

(define (eval-if exp)
  (if (cadr exp)
      (caddr exp)
      (cadddr exp) ))

;R4RS optional 1+ and -1+
(define (eval-1+ exp)
  (1+ (cadr exp)))

(define (eval--1+ exp)
  (-1+ (cadr exp)))


; Harvey & Wright only
(define (eval-keep exp)
  (let* ((fn (cadr exp))
         (datum (eval (caddr exp)))
         (stuff (map quote-if-necessary (every (lambda (x) x) datum)))
         (combiner (if (word? datum) 'word 'se))
         (ident (if (word? datum) "" ''())) )
    (cons combiner
          (map (lambda (elt) `(if (,fn ,elt) ,elt ,ident))
               stuff)) ))

(define (eval-let exp)
  (let* ((bindings (cadr exp))
         (foo (if (not (correct-let-binding-syntax? bindings))
                  (eval-error "Illegal LET syntax: " exp)
                  'foo))
         (names (map car bindings))
         (values (map cadr bindings))
         (body (caddr exp)) )
    `((lambda ,names ,body) ,@values) ))

(define (eval-let* exp)
  (define (translate bindings body)
    (if (null? bindings)
        body
        (list 'let
              (list (car bindings))
              (translate (cdr bindings) body))))
  (let ((bindings (cadr exp))
        (body (caddr exp)))
    (if (not (correct-let-binding-syntax? (cadr exp)))
        (eval-error "Illegal LET* syntax: " exp)
        (translate bindings body))))

(define (correct-let-binding-syntax? bindings)
  (if (null? bindings)
      #t
      (and (list? (car bindings))
           (= (length (car bindings)) 2)
           (correct-let-binding-syntax? (cdr bindings)))))

(define (eval-map exp)
  (let* ((fn-exp (cadr exp))
         (datum (cadr (caddr exp)))     ; Get rid of quote or quasiquote
         (list-elts (map quasiquote-if-necessary datum)))
    (cons 'list 
          (map (lambda (elt) (list fn-exp elt))
               list-elts))))

(define (eval-or exp)
  (define (or-value exp)
    (cond ((null? exp) #f)
          ((not (basic? (car exp)))
           *not-basic*)
          ((car exp))   ; Change to #t to make OR sensible
          (else (or-value (cdr exp))) ))
  (let ((ans (or-value (cdr exp))))
    (if (eq? ans *not-basic*)
        (cons 'or (make-next-basic (cdr exp)))
        ans)) )

; Harvey & Wright only
(define (eval-reduce exp)
  (let* ((fn (cadr exp))
         (datum (cadr (caddr exp)))     ; Get rid of quote or quasiquote
         (list-elts (map quasiquote-if-necessary datum)))
    (if (null? list-elts)
        (list fn)
        (accum/reduce-expand fn list-elts)) ))

; Grillmeyer only
(define (eval-remove-if exp)
  (let* ((fn-exp (cadr exp))
         (datum (cadr (caddr exp)))     ; Get rid of quote or quasiquote
         (list-elts (map quasiquote-if-necessary datum)))
    (cons 'append
          (map (lambda (elt) `(if (,fn-exp ,elt) '() (list ,elt)))
               list-elts)) ))

; Harvey & Wright only
(define (eval-repeated exp)
  (let ((fn (cadr exp))
        (num (caddr exp)) )
    (if (not (and (integer? num) (>= num 0)))
        (eval-error "Second argument to REPEATED must be a positive integer")
        `(lambda (x) ,(nest-function-calls fn num 'x)) )))

; Grillmeyer only
(define (eval-some exp)
  (let* ((fn-exp (cadr exp))
         (datum (cadr (caddr exp)))     ; Get rid of quote or quasiquote
         (list-elts (map quasiquote-if-necessary datum)))
    (cons 'or
          (map (lambda (elt) (list fn-exp elt))
               list-elts)) ))

(define (nest-function-calls fn n base-case)
  (if (= n 0)
      base-case
      (list fn (nest-function-calls fn (- n 1) base-case)) ))


;; Each entry in this table has the form:
;; 1) Keyword
;; 2) Modeller procedure to translate the special form
;; 3) Which arguments are evaluated before the special form does its
;;    translation.  (E.g., IF evaluates its first argument only, and
;;    REPEATED evaluated both of its args.)
;; 4) The required length of the special form, i.e., one more than the number
;;    of arguments.  0 if it can be any length.

(define *the-special-forms*
  (if *grillmeyer*
      ; Grillmeyer forms to be evaluated specially
      `((1+ ,eval-1+ (1) 2)
        (1- ,eval--1+ (1) 2)
        (-1+ ,eval--1+ (1) 2)
        (accumulate ,eval-reduce (1 2) 3)  ; Grillmeyer version
        (and ,eval-and () 0)
        (apply ,eval-apply (1 2) 3)
        (cond ,eval-cond () 0)
        (count-if ,eval-count-if (1 2) 3)  ; Grillmeyer only
        (count-if-not ,eval-count-if-not (1 2) 3)  ; Grillmeyer only
        (define ,eval-define () 0)
        (every ,eval-every (1 2) 3)  ; Grillmeyer and Harvey & Wright differ
        ; Modelling find-if(-not) as a recursive function is ok.
        ;   (find-if ,eval-find-if (1 2) 3)  ; Grillmeyer only
        ;   (find-if-not ,eval-find-if-not (1 2) 3)  ; Grillmeyer only
        (if ,eval-if (1) 4)
        (keep-if ,eval-filter (1 2) 3)  ; Grillmeyer only
        (let ,eval-let () 3)
        (let* ,eval-let* () 3)
        (letrec ,eval-define () 0)
        (map ,eval-map (1 2) 3)
        (or ,eval-or () 0)
        (remove-if ,eval-remove-if (1 2) 3)  ; Grillmeyer only
        (some ,eval-some (1 2) 3) )  ; Grillmeyer only
      
      ; Harvey+Wright forms to be evaluated specially
      `((1+ ,eval-1+ (1) 2)
        (1- ,eval--1+ (1) 2)
        (-1+ ,eval--1+ (1) 2)
        (accumulate ,eval-accumulate (1 2) 3) ; Harvey & Wright version
        (and ,eval-and () 0)
        (apply ,eval-apply (1 2) 3)
        (cond ,eval-cond () 0)
        (define ,eval-define () 0)
        (every ,eval-every (1 2) 3)  ; Grillmeyer and Harvey & Wright differ
        (filter ,eval-filter (1 2) 3)  ; Harvey & Wright only
        (if ,eval-if (1) 4)
        (keep ,eval-keep (1 2) 3)  ; Harvey & Wright only
        (let ,eval-let () 3)
        (let* ,eval-let* () 3)
        (letrec ,eval-define () 0)
        (map ,eval-map (1 2) 3)
        (or ,eval-or () 0)
        (reduce ,eval-reduce (1 2) 3)  ; Harvey & Wright only
        (repeated ,eval-repeated (2) 3)  ; Harvey & Wright only
        ) ) )

;; A list of the actual special forms, i.e., symbols we should try not
;; to EVAL.
(define *the-real-special-forms*
  '(lambda quote and cond define if let or))

;; The actual evaluation of special forms

(define (special-form? exp)
  (and (list? exp)
       (assoc (car exp) *the-special-forms*)))

(define (eval-spec-form exp)
  (let* ((record (assoc (car exp) *the-special-forms*))
         (num-subexprs (cadddr record))
         (which-to-eval (caddr record))
         (eval-fn (cadr record)))
    (cond ((and (> num-subexprs 0)
                (not (= num-subexprs (length exp))))
           (eval-error "Wrong number of arguments to special form: " exp))
          ((these-basic? which-to-eval exp)
           (eval-fn exp))
          (else (make-these-basic which-to-eval exp)) )))

;; For special forms that evaluate some of their arguments:
(define (these-basic? which-ones exp)
  (cond ((null? which-ones) #t)
        ((basic? (list-ref exp (car which-ones)))
         (these-basic? (cdr which-ones) exp))
        (else #f) ))

(define (make-these-basic which-ones exp)
  (define (helper this which-ones rest-exp)
    (cond ((null? rest-exp) '())
          ((null? which-ones) rest-exp)
          ((= this (car which-ones))
           (cons (full-eval (car rest-exp))
                 (helper (+ this 1) (cdr which-ones) (cdr rest-exp)) ))
          (else (cons (car rest-exp)
                      (helper (+ this 1) which-ones (cdr rest-exp)) ))))
  (helper 0 which-ones exp) )

(define (make-next-basic exps)
  (if (basic? (car exps))
      (cons (car exps)
            (make-next-basic (cdr exps)) )
      (cons (full-eval (car exps))
            (cdr exps))))


;; Applications:

(define application? list?)

(define (eval-application exp)
  (cond ((all-basic? exp) (part-apply (car exp) (cdr exp)))
        ((all-but-one-basic? exp) (map part-eval exp))
        (else (map full-eval exp))))

(define (part-apply fn args)
  (cond ((function-name? fn)
         (let ((lambda-expr-or-procedure (get-fn fn)))
           (if (procedure? lambda-expr-or-procedure)
               (apply-primitive lambda-expr-or-procedure args)
               (user-apply lambda-expr-or-procedure args))))
        ((legit-lambda-expr? fn)
         (user-apply (fn-record-from-lambda fn) args))
        ((call-to-repeated? fn)
         (handle-repeated-as-primitive fn args))
        (else (eval-error "Not a function: " fn)) ))

(define (legit-lambda-expr? expr)
  (and (pair? expr)
       (eq? (car expr) 'lambda)
       (pair? (cdr expr))
       (ok-formals? (cadr expr))
       (pair? (cddr expr))
       (null? (cdddr expr))))

(define (ok-formals? formals)
  (cond ((null? formals) #t)
        ((symbol? formals) #t)
        ((pair? formals)
         (and (symbol? (car formals))
              (ok-formals? (cdr formals))))
        (else #f)))

(define (call-to-repeated? fn)
  (and (pair? fn)
       (eq? 'repeated (car fn))
       (pair? (cdr fn))
       (pair? (cddr fn))
       (integer? (caddr fn))
       (>= (caddr fn) 0)
       (null? (cdddr fn))
       (procedural-basic-value? (cadr fn))))

(define (procedural-basic-value? thingo)
  (or (function-name? thingo) 
      (legit-lambda-expr? thingo)
      (call-to-repeated? thingo)))

(define (handle-repeated-as-primitive repeated-call args)
  (let ((fn (cadr repeated-call))
        (num (caddr repeated-call)))
    (if (= 1 (length args))
        (nest-function-calls fn num (car args))
        (eval-error "Wrong number of arguments to function returned by REPEATED."))))

(define (apply-primitive fn args)
;; ARGS is a list of basic expressions
  (thunky-val->basic-expr
    (apply fn (map basic-expr->thunky-val args))))

(define (basic-expr->thunky-val expr)
  (cond ((procedural-basic-value? expr)
         (lambda () expr))
        ((quasiquoted? expr)
         (deep-comma->thunk (cadr expr)))
        (else (eval expr))))

(define (thunky-val->basic-expr tv)
  (cond ((procedure? tv) (tv))
        ((list-with-any-thunks? tv)
         (list 'quasiquote (deep-thunk->comma tv)))
        ((or (symbol? tv) (list? tv))
         (list 'quote tv))
        (else tv)))

(define (quasiquoted? expr)
  (and (pair? expr) (eq? (car expr) 'quasiquote)))

(define (deep-comma->thunk expr)
  (cond ((unquoted? expr) (lambda () (cadr expr)))
        ((quoted? expr) expr)
        ((quasiquoted? expr) expr)      ;; we think this can't happen
        ((pair? expr)
         (cons (deep-comma->thunk (car expr))
               (deep-comma->thunk (cdr expr))))
        (else expr)))

(define (quoted? expr)
  (and (pair? expr) (eq? (car expr) 'quote)))

(define (list-with-any-thunks? tree)
  (cond ((procedure? tree) #t)
        ((pair? tree) (or (list-with-any-thunks? (car tree))
                          (list-with-any-thunks? (cdr tree))))
        (else #f)))

(define (deep-thunk->comma tree)
  (cond ((procedure? tree) (list 'unquote (tree)))
        ((pair? tree) (cons (deep-thunk->comma (car tree))
                            (deep-thunk->comma (cdr tree))))
        (else tree)))

(define (any-procedures? tree)
  (cond ((procedure? tree) #t)
        ((pair? tree)
         (or (any-procedures (car tree))
             (any-procedures (cdr tree))))
        (else #f)))

;; The only primitive functions that return functions are lambda and repeated,
;; and these are treated specially by other parts of the program.  So the 
;; only thing we have to be careful about is quoting the result if it's a
;; symbol or list.
(define (quote-if-necessary value)
  (cond ((and (pair? value) (eq? (car value) 'unquote)) (cadr value))
        ((or (symbol? value) (list? value)) (list 'quote value))
        (else value)))

(define (quasiquote-if-necessary value)
  (cond ((and (pair? value) (eq? (car value) 'unquote)) (cadr value))
        ((any-unquoted? value) (list 'quasiquote value))
        ((or (symbol? value) (list? value)) (list 'quote value))
        (else value)))


;; Full-eval: evaluate an expression all the way to a basic value, but not
;; truly all the way.
;; This is a real kludge.  I can't just call eval and then quote-if-necessary
;; the output, because what if the value is a procedure and I really
;; want the name of the procedure as a symbol?
;; So I just repeatedly part-eval the expression until I get the same
;; answer I got last time.  This is *totally* slow, but I don't think
;; there's any way to do it, short of writing a metacircular evaluator.
;; At least it's tail recursive.

(define (full-eval exp)
  (let ((next (part-eval exp)))
    (if (equal? exp next)
        exp
        (full-eval next) )))

;; user-apply: apply a compound function to some (basic) arguments
;; Basically just substitution.

(define (substitute alist tree)
  (cond ((not (pair? tree))
         (let ((record (assq tree alist)))
           (if record (cdr record) tree) ) )
        ((eq? 'lambda (car tree))
         (substitute-lambda alist tree))
        ((eq? 'quote (car tree)) tree)
        (else (cons (substitute alist (car tree))
                    (substitute alist (cdr tree)) ))))

(define (user-apply fn-record args)
  (let ((substitute substitute))
    (substitute (make-bindings (formals fn-record) args)
                (body fn-record) ) ) )

(define (make-bindings names values)
  (cond ((null? names) 
         (if (null? values)
             '()
             (eval-error "Wrong number of arguments to function")))
        ((symbol? names) (list (cons names values)))
        ((null? values) (eval-error "Wrong number of arguments to function"))
        (else (cons (cons (car names) (car values))
                    (make-bindings (cdr names) (cdr values)) ))))

;; Lambda is hard.  If you have (define (f x) (lambda (x) (+ x 4)) )
;; then you have to be careful to leave the x as x in (+ x 4)

(define (substitute-lambda alist lam-exp)
  (let ((substitute substitute))
    (if (not (= (length lam-exp) 3))
        (eval-error "Incorrect use of LAMBDA: " lam-exp)
        (list (car lam-exp)
              (cadr lam-exp)
              (substitute (remove-bindings alist (cadr lam-exp))
                          (caddr lam-exp) )))) )

(define (remove-bindings alist names)
  (cond ((null? alist) '())
        ((member (car (car alist)) names)
         (remove-bindings (cdr alist) names))
        (else (cons (car alist) (remove-bindings (cdr alist) names))) ))


;; Basicness

;; Something is "basic" if it has been evaluated as far as it can be
;; evaluated.  If a compound expression is composed of nothing but
;; basic subexpressions, then evaluating it causes the apply to happen.
;; Otherwise, it causes the subexpressions to be evaluated.
;; The following data types are basic:
;; - Anything self-evaluating, e.g., any atom besides a symbol
;; - Names of functions and lambda expressions
;; - Any quoted data object
;; - Calls to "primitive" functions that return functions, if they don't
;;   know lambda yet.
;; Note that anything basic can be evaluated by the regular EVAL to produce
;; the value that it represents.
;; Also note that checking if a symbol is basic involves evaluating it;
;; so it will generate unbound variable errors.

(define (all-basic? exps)
  (if (null? exps)
      #t
      (and (basic? (car exps))
           (all-basic? (cdr exps)) )))

(define (all-but-one-basic? exps)
  (cond ((null? exps) #f)
        ((not (basic? (car exps))) (all-basic? (cdr exps)))
        (else (all-but-one-basic? (cdr exps)) )))

(define (basic? exp)
  (cond ((number? exp) #t)
        ((symbol? exp) (function-name? exp))
        ((null? exp) #t)
        ((not (pair? exp)) #t)
        ((eq? 'quote (car exp)) #t)
        ((eq? 'quasiquote (car exp)) #t)
        ((eq? 'lambda (car exp)) #t)
        ((eq? (car exp) 'repeated)
         (and (not *they-know-lambda*)
              (basic? (cadr exp))
              (basic? (caddr exp)) ))
        (else #f) ))


;; Compound functions
;;
;; GET-FN, which is implementation-dependent, takes a symbol as its
;; argument.  If the symbol names a compound procedure, GET-FN
;; returns a lambda expression.  If the symbol names a primitive
;; procedure, GET-FN returns the primitive procedure named.
;; GET-FN is implementation-dependent.

(define (function-name? exp)
  (and (symbol? exp)
       (not (member exp *the-real-special-forms*))
       (bound? exp)
       (procedure? (eval exp))) )


;; A lambda expression already looks like a function record.
(define (fn-record-from-lambda l-exp)
  (if (not (= (length l-exp) 3))
      (eval-error "Invalid use of LAMBDA:" l-exp)
      l-exp))

(define formals cadr)

(define (body exp)
  (if (null? (cdr exp))
      (car exp)
      (body (cdr exp))))

(define (deep-dequasiquote tree)
  (cond ((not (pair? tree)) tree)
        ((eq? (car tree) 'quasiquote) (de-quasiquote (cadr tree)))
        (else (cons (deep-dequasiquote (car tree))
                    (deep-dequasiquote (cdr tree))))))

(define (de-quasiquote tree)
  (cond ((unquoted? tree) (cadr tree))
        ((not (any-unquoted? tree)) (quote-if-necessary tree))
        ((and (pair? tree)
              (or (not (list? tree))
                  (and (= (length tree) 3)
                       (eq? (cadr tree) 'unquote))))
         (list 'cons
               (de-quasiquote (car tree))
               (de-quasiquote (cdr tree))))
        ((list? tree)
         (cons 'list (map de-quasiquote tree)))
        (else (error "This can't happen."))))

(define (unquoted? tree)
  (and (pair? tree)
       (eq? 'unquote (car tree))))

(define (any-unquoted? tree)
  (cond ((unquoted? tree) #t)
        ((pair? tree)
         (or (any-unquoted? (car tree))
             (any-unquoted? (cdr tree))))
        (else #f)))

(define book-primitive?
  (if *grillmeyer*       
      ; Grillmeyer version
      (let* 
        ((names '(* + - / < <= = > >= abs acos append
                    asin assoc atan boolean?
                    car cdr caar cadr cdar cddr
                    caaar caadr cadar caddr cdaar cdadr cddar cdddr
                    caaaar caaadr caadar caaddr cadaar cadadr caddar cadddr
                    cdaaar cdaadr cdadar cdaddr cddaar cddadr cdddar cddddr
                    ceiling cons cos count equal? even?
                    exit exp expt floor gcd integer? lcm
                    length list list-ref list? log max member
                    min modulo not null? number?
                    odd? procedure? quotient random remainder round
                    sin sqrt tan truncate
                    first second third fourth fifth rest atom?
                    list-head subseq position remove count rassoc
                    intersection union set-difference subset? adjoin))
         (procs (map eval names)))
        (lambda (name value)
          (define (helper names procs)
            (cond ((null? names) #f)
                  ((eq? (car names) name)
                   ;; Make sure it hasn't been redefined
                   (eq? (car procs) value))
                  (else (helper (cdr names) (cdr procs)))))
          (helper names procs)))
      
      ; Harvey+Wright version
      (let*
        ((names '(* + - / < <= = > >= abs  acos appearances append
                    asin assoc atan before? bf bl boolean? butfirst
                    butlast car cdr caar cadr cdar cddr
                    caaar caadr cadar caddr cdaar cdadr cddar cdddr
                    caaaar caaadr caadar caaddr cadaar cadadr caddar cadddr
                    cdaaar cdaadr cdadar cdaddr cddaar cddadr cdddar cddddr
                    ceiling children cons cos count datum empty? equal? even?
                    exit exp expt first floor gcd integer? item last lcm
                    length list list-ref list? log make-node max member
                    member? min modulo not null? number?
                    odd? procedure? quotient remainder round se
                    sentence sentence? sin sqrt tan truncate word word?))
         (procs (map eval names)))
        (lambda (name value)
          (define (helper names procs)
            (cond ((null? names) #f)
                  ((eq? (car names) name)
                   ;; Make sure it hasn't been redefined
                   (eq? (car procs) value))
                  (else (helper (cdr names) (cdr procs)))))
          (helper names procs)))
      ) )


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
