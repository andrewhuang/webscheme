;;;; WebScheme library functions

(define ws-lib-ver "030321.1600")
(display "Loading WebScheme library (")
(display ws-lib-ver)
(display ")\n")

(import old-generic-procedures)
(import old-s2j)
(import libraries)
(require-library "sisc/libs/srfi")
(import srfi-13) ; string functions for assertions
(import srfi-14)

;; vector-map
;; .parameter proc procedure to apply to vector elements
;; .parmeter vec vector of elements
(define (vector-map proc vec)
  (let* ((len (vector-length vec))
	 (result (make-vector len)))
    (do ((index 0 (+ index 1)))
          ((= index len) result)
        (vector-set! result index
		     (proc (vector-ref vec index))))))

; Define referenced classes
(define <SchemeHandler> (java-class "webscheme.SchemeHandler"))
(define <DataModel> (java-class "webscheme.dom.DataModel"))
(define <Logger> (java-class "webscheme.wise.Logger"))
(define <StateStore> (java-class "webscheme.wise.StateStore"))
(define <JOptionPane> (java-class "javax.swing.JOptionPane"))

; Discover referenced methods
; This ensures that Reflection is used during applet
; initialization while permission is available
(define-generic set-timeout-delay)
(applicable-methods set-timeout-delay (list <SchemeHandler>))
(define-generic set-timeout-message)
(applicable-methods set-timeout-message (list <SchemeHandler>))
(define-generic show-message-dialog)
(applicable-methods show-message-dialog (list <JOptionPane>))
(define-generic show-input-dialog)
(applicable-methods show-input-dialog (list <JOptionPane>))
(define-generic set-string)
(applicable-methods set-string (list <DataModel>))
(define-generic get-string)
(applicable-methods get-string (list <DataModel>))
(define-generic eval-javascript)
(applicable-methods eval-javascript (list <DataModel>))
(define-generic push)
(applicable-methods push (list <Logger>))
(define-generic include)
(applicable-methods include (list <StateStore>))
(define-generic get-queue)
(applicable-methods get-queue (list <Logger>))


; ; ws-xmlrpc-execute
;(define <XmlRpcClient> (java-class-of ws-xmlrpc-obj))
;(define-generic execute)
;(for-each
; (lambda (class)
;   (applicable-methods execute (list class)))
; (list <XmlRpcClient>))
;(define
;  (ws-xmlrpc-execute method-name values)
;  (execute ws-xmlrpc-obj method-name values))



;; Evaluate Javascript code
;; .parameter code Javascript code to evaluate
(define
  (ws-eval-js code)
  (eval-javascript ws-data-model-obj (->jstring code)))

;; Present a Swing info box to the user
;; .parameter message message to present
(define
  (ws-tell-user message)
  (show-message-dialog <JOptionPane> (java-null <JOptionPane>) (->jstring message)))

;; Present a Swing query box to the user
;; .parameter message message to present
;; .returns user's input into the box
(define
  (ws-ask-user message)
  (->string (show-input-dialog <JOptionPane> (java-null <JOptionPane>) (->jstring message))))


;; Set WebScheme timeout delay
;; .parameter seconds seconds until timeout
(define (ws-set-timeout-delay! seconds)
  (set-timeout-delay (->jint seconds))

;; Set WebScheme timeout message
;; .parameter message to deliver on timeout
(define (ws-set-timeout-message! str)
  (set-timeout-message (->jstring str))


;;; Fields

;; Set the value of a field
;; .parameter id id of field to address
;; .parameter value new value for field
(define
  (ws-set-string id value)
  (set-string ws-data-model-obj (->jstring id) (->jstring value)))

;; Get the value of a field
;; .parameter id id of field to address
;; .returns value of the field
(define
  (ws-get-string id)
  (->string (get-string ws-data-model-obj (->jstring id))))

;; Set the SRC of an IMG field
;; .parameter id id of field to address
;; .parameter src new src for img
(define (ws-set-src id src)
  (ws-eval-js
   (string-append "document.getElementById(\"" id "\").src=\"" src "\"")))

;; Set the DISABLED flag of a field
;; .parameter id id of field to address
;; .parameter disabled new boolean for disabled flag
(define (ws-set-disabled id disabled)
  (let ((dval (if disabled "true" "")))
    (ws-eval-js
     (string-append "document.getElementById(\"" id "\").disabled=\"" dval "\""))))

;; Set the status of a status field
;; .parameter id id of field to address
;; .parameter statuscode code of new status
(define (ws-set-status id statuscode)
  (ws-set-src id (string-append "ws-icons/status_" statuscode ".gif")))


;;; Assertions

; FIX sisc bug
; using it here helps somehow
(display "test string-length (3): ")(display (string-length "( ("))(display "\n")

;; Assert that the length of the field is exactly "length"
;; .returns #t if the specified field contains a string of length 'length'
(define (ws-assert-length id length)
  (= (string-length (ws-get-string id)) length))

;; Assert that the length of the field is at minumum "length"
;; .returns #t if the specified field contains a string no shorter than 'length'
(define (ws-assert-minlength id length)
  (>= (string-length (ws-get-string id)) length))

;; Assert that the length of the field is at maximum "length"
;; .returns #t if the specified field contains a string no longer than 'length'
(define (ws-assert-maxlength id length)
  (<= (string-length (ws-get-string id)) length))

;; Assert that the field contains a number
;; .returns #t if the specified field contains a number
(define (ws-assert-number id)
  (number? (string->number (ws-get-string id))))

; FIX sisc bug
; using it here helps somehow
(display "test string-count (2): ")
(display (string-count "( (" #\( ))
(display "\n")

(define (string-count-unclosed s)
  (- (string-count s #\( )
     (string-count s #\) )))

;; Assert that the field has "diff" more open than close parens
;; .returns #t if the specified field contains "diff" more open than close parens
(define (ws-assert-imbalanced id diff)
  (= diff (string-count-unclosed (ws-get-string id))))

;; Assert that the field has as many open as close parens
;; .returns #t if the specified field contains as many open as close parens
(define (ws-assert-balanced id)
  (ws-assert-imbalanced id 0))


;;; Logging

;; Push "value" onto "queueKey" queue in "logKey"
(define
  (ws-log-push logKey queueKey value)
  (push ws-logger-obj logKey queueKey value))

;; Push "value" onto "queueKey" queue within the current page
(define
  (ws-log-page-push queueKey value)
  (push ws-logger-obj queueKey value))

; pushes like |ws-log-push| unless it's already happened
; FIX this session (hashtable) vs.  ever (db hit)
(define (ws-log-push-once logKey queueKey value)
  #f)

;; Get a vector of logged values
;; .returns a vector of values for log,queueKey
(define
  (ws-log-get-queue logKey queueKey)
  (get-queue ws-logger-obj logKey queueKey))

;; Get a vector of logged values
;; .returns a vector of values for queueKey on the current page
(define
  (ws-log-page-get-queue queueKey)
  (get-queue ws-logger-obj queueKey))


;;; State

;; Sets attr of field to be stored in persistant state
;; string literals defined in webscheme.dom.SmartJSO
(define
  (ws-state-include-src fieldid)
  (include ws-statestore-obj fieldid "SRC"))

;; Sets attr of field to be stored in persistant state
;; string literals defined in webscheme.dom.SmartJSO
(define
  (ws-state-include-disabled fieldid)
  (include ws-statestore-obj fieldid "DISABLED"))

;; Sets attr of field to be stored in persistant state
;; string literals defined in webscheme.dom.SmartJSO
(define
  (ws-state-include-value fieldid)
  (include ws-statestore-obj fieldid "VALUE"))


;;; Tables

; ws-get-row
(define-generic get-row)
(for-each
 (lambda (class)
   (applicable-methods get-row (list class)))
 (list <DataModel>))
(define
  (ws-get-row table-name rownum)
  (let ((jstring-array (get-row ws-data-model-obj (->jstring table-name) (->jint rownum))))
    (vector-map ->string (->vector jstring-array))))


; ws-add-row
(define-generic add-row)
(for-each
 (lambda (class)
   (applicable-methods add-row (list class)))
 (list <DataModel>))
(define
  (ws-add-row table-name data-vector)
  (let* ((jstring-vector (vector-map ->jstring data-vector))
	 (jstring-array (->jarray jstring-vector <JString>))
	 (jstring-table-name (->jstring table-name)))
    (display "jstring-array: ")
    (display jstring-array)
    (display "\n")
    (add-row ws-data-model-obj jstring-table-name jstring-array)))


; ws-table-clear
(define
  (ws-table-clear table-id)
  (ws-eval-js
   "alert('FIX: implement |ws-table-clear|')")
)
