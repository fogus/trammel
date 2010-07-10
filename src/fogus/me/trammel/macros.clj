;;; macros.clj -- Contracts programming library for Clojure

;; by Michael Fogus - http://fogus.me/fun/
;; July 9, 2010

;; Copyright (c) Michael Fogus, 2010. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file COPYING the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns fogus.me.trammel.macros)

;; ALL macros in this ns are taken directly from clojure.contrib.macro-utils
;;  by the eminent Konrad Hinsen
;; This inclusion is only temporary until contrib is properly modularized.

(def macro-fns {})
(def macro-symbols {})
(def protected-symbols #{})
(def special-forms (into #{} (keys clojure.lang.Compiler/specials)))

(declare expand-all)

(defn- reserved?
  [symbol]
  "Return true if symbol is a reserved symbol (starting or ending with a dot)."
  (let [s (str symbol)]
    (or (= "." (subs s 0 1))
	(= "." (subs s (dec (count s)))))))

(defn- expand-symbol
  "Expand symbol macros"
  [symbol]
  (cond (contains? protected-symbols symbol) symbol
	(reserved? symbol)                   symbol
	(contains? macro-symbols symbol)     (get macro-symbols symbol)
	:else (let [v (resolve symbol)
		    m (meta v)]
		(if (:symbol-macro m)
		  (var-get v)
		  symbol))))

(defn- expand-1
  "Perform a single non-recursive macro expansion of form."
  [form]
  (cond
    (seq? form)
      (let [f (first form)]
        (cond (contains? special-forms f) form
	      (contains? macro-fns f)     (apply (get macro-fns f) (rest form))
	      (symbol? f)                 (let [exp (expand-symbol f)]
					    (if (= exp f)
					      (clojure.core/macroexpand-1 form)
					      (cons exp (rest form))))
	      ; handle defmacro macros and Java method special forms
	      :else (clojure.core/macroexpand-1 form)))
    (symbol? form)
      (expand-symbol form)
     :else
       form))

(defn- expand
  "Perform repeated non-recursive macro expansion of form, until it no
   longer changes."
  [form]
  (let [ex (expand-1 form)]
    (if (identical? ex form)
      form
      (recur ex))))

(defn- expand-args
  "Recursively expand the arguments of form, leaving its first
   n elements unchanged."
  ([form]
   (expand-args form 1))
  ([form n]
   (doall (concat (take n form) (map expand-all (drop n form))))))

(defn- expand-bindings
  [bindings exprs]
  (if (empty? bindings)
    (list (doall (map expand-all exprs)))
    (let [[[s b] & bindings] bindings]
      (let [b (expand-all b)]
	(binding [protected-symbols (conj protected-symbols s)]
	  (doall (cons [s b] (expand-bindings bindings exprs))))))))

(defn- expand-with-bindings
  "Handle let* and loop* forms. The symbols defined in them are protected
   from symbol macro expansion, the definitions and the body expressions
   are expanded recursively."
  [form]
  (let [f        (first form)
	bindings (partition 2 (second form))
	exprs    (rest (rest form))
	expanded (expand-bindings bindings exprs)
	bindings (vec (apply concat (butlast expanded)))
	exprs    (last expanded)]
    (cons f (cons bindings exprs))))

(defn- expand-fn-body
  [[args & exprs]]
  (binding [protected-symbols (reduce conj protected-symbols
				     (filter #(not (= % '&)) args))]
    (cons args (doall (map expand-all exprs)))))

(defn- expand-fn
  "Handle fn* forms. The arguments are protected from symbol macro
   expansion, the bodies are expanded recursively."
  [form]
  (let [[f & bodies] form
	name         (when (symbol? (first bodies)) (first bodies))
	bodies       (if (symbol? (first bodies)) (rest bodies) bodies)
	bodies       (if (vector? (first bodies)) (list bodies) bodies)
	bodies       (doall (map expand-fn-body bodies))]
    (if (nil? name)
      (cons f bodies)
      (cons f (cons name bodies)))))

(defn- expand-method
  "Handle a method in a deftype* or reify* form."
  [m]
  (rest (expand-fn (cons 'fn* m))))

(defn- expand-deftype
  "Handle deftype* forms."
  [[symbol typename classname fields implements interfaces & methods]]
  (assert (= implements :implements))
  (let [expanded-methods (map expand-method methods)]
    (concat
     (list symbol typename classname fields implements interfaces)
     expanded-methods)))

(defn- expand-reify
  "Handle reify* forms."
  [[symbol interfaces & methods]]
  (let [expanded-methods (map expand-method methods)]
    (cons symbol (cons interfaces expanded-methods))))

(def special-form-handlers
  {'quote 	  identity
   'var   	  identity
   'def   	  #(expand-args % 2)
   'new           #(expand-args % 2)
   'let*          expand-with-bindings
   'loop*         expand-with-bindings
   'fn*           expand-fn
   'deftype*      expand-deftype
   'reify*        expand-reify})

(defn- expand-list
  "Recursively expand a form that is a list or a cons."
  [form]
  (let [f (first form)]
    (if (symbol? f)
      (if (contains? special-forms f)
	((get special-form-handlers f expand-args) form)
	(expand-args form))
      (doall (map expand-all form)))))

(defn- expand-all
  "Expand a form recursively."
  [form]
  (let [exp (expand form)]
    (cond (symbol? exp) exp
	  (seq? exp) (expand-list exp)
	  (vector? exp) (into [] (map expand-all exp))
	  (map? exp) (into {} (map expand-all (seq exp)))
	  :else exp)))

(defmacro macrolet
  "Define local macros that are used in the expansion of exprs. The
   syntax is the same as for letfn forms."
  [fn-bindings & exprs]
  (let [names      (map first fn-bindings)
	name-map   (into {} (map (fn [n] [(list 'quote n) n]) names))
	macro-map  (eval `(letfn ~fn-bindings ~name-map))]
    (binding [macro-fns     (merge macro-fns macro-map)
	      macro-symbols (apply dissoc macro-symbols names)]
      `(do ~@(doall (map expand-all exprs))))))