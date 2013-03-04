;; trammel.clj -- Contracts programming library for Clojure

;; by Michael Fogus - <http://fogus.me/fun/trammel>

; Copyright (c) Michael Fogus, 2010-2012. All rights reserved.  The use
; and distribution terms for this software are covered by the Eclipse
; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file COPYING the root of this
; distribution.  By using this software in any fashion, you are
; agreeing to be bound by the terms of this license.  You must not
; remove this notice, or any other, from this software.

(ns trammel.core
  "The core contracts programming functions and macros for Trammel."
  (:use [trammel.funcify :only (funcify)]
        [trammel.factors]
        [trammel.utils]
        [clojure.core.contracts :only (contract with-constraints)]))


(defmacro defcontract
  "Convenience macro for defining a named contract.  Equivalent to `(def fc (contract ...))`"
  [name docstring & forms]
  `(def ~name
     (contract ~(symbol (str name "-impl")) ~docstring ~@forms)))

(defmacro defconstrainedfn
  "Defines a function using the `contract` vector appearing after the arguments.

       (defconstrainedfn sqr
         [n] [number? (not= 0 n) => pos? number?]
         (* n n))

   Like the `contract` macro, multiple arity functions can be defined where each argument vector 
   is immediately followed by the relevent arity expectations.  This macro will also detect
   if a map is in that constraints position and use that instead under the assumption that
   Clojure's `:pre`/`:post` map is used instead.
  "
  [name & body]
  (let [mdata (if (string? (first body))
                {:doc (first body)}
                {})
        body  (if (:doc mdata)
                (next body)
                body)
        body  (if (vector? (first body))
                (list body)
                body)
        body  (for [[args cnstr & bd] body]
                (list* args
                       (if (vector? cnstr)
                         (second (#'clojure.core.contracts.impl.transformers/build-constraints-description args cnstr (:doc mdata)))
                         cnstr)
                       bd))]
    `(defn ~name
       ~(str (:doc mdata))
       ~@body)))

; clojure/core_deftype.clj
(defn- build-positional-factory
  "Used to build a positional factory for a given type/record.  Because of the
  limitation of 20 arguments to Clojure functions, this factory needs to be
  constructed to deal with more arguments.  It does this by building a straight
  forward type/record ctor call in the <=20 case, and a call to the same
  ctor pulling the extra args out of the & overage parameter.  Finally, the
  arity is constrained to the number of expected fields and an ArityException
  will be thrown at runtime if the actual arg count does not match."
  [nom classname fields invariants chk]
  (let [fn-name (symbol (str '-> nom))
        [field-args over] (split-at 20 fields)
        field-count (count fields)
        arg-count (count field-args)
        over-count (count over)]
    `(defconstrainedfn ~fn-name
       [~@field-args ~@(if (seq over) '[& overage] [])]
       ~invariants
       (println (str "build-a " '~field-args))
       (with-meta
         ~(if (seq over)
            `(if (= (count ~'overage) ~over-count)
               (new ~nom
                    ~@field-args
                    ~@(for [i (range 0 (count over))]
                        (list `nth 'overage i)))
               (throw (clojure.lang.ArityException. (+ ~arg-count (count ~'overage)) (name '~fn-name))))
            `(new ~nom ~@field-args))
         {:contract ~chk}))))

(comment

  (macroexpand '(defconstrainedrecord HasF [g]
                  "Has a field called f"
                  [(number? g)]))

  (macroexpand '(trammel.core/defconstrainedfn ->HasF [g]
    [(number? g)]
    (println (str "build-a " (quote (g))))
    (with-meta (new HasF g)
      {:contract (clojure.core.contracts/contract chk-HasF
                                                  "Has a field called f"
                                                  [{:as m__1123__auto__, :keys [g]}]
                                                  [(number? g)])})))


  (macroexpand '(clojure.core.contracts/contract chk-HasF
                                                       "Has a field called f"
                                                       [{:as m__1123__auto__, :keys [g]}]
                                                       [(number? g)]))

  (defconstrainedrecord HasG [g]
  "Has a field called g"
  [(number? g)])

  (->HasG 1)
)

(defmacro defconstrainedrecord
  [name slots inv-description invariants & etc]
  (let [fields (vec slots)
        ns-part (namespace-munge *ns*)
        classname (symbol (str ns-part "." name))
        ctor-name (symbol (str name \.))
        positional-factory-name (symbol (str "->" name))
        map-arrow-factory-name (symbol (str "map->" name))
        chk `(contract ~(symbol (str "chk-" name))
                       ~inv-description
                       [{:keys ~fields :as m#}]
                       ~invariants)]
    `(do
       (let [t# (defrecord ~name ~fields ~@etc)]
         (defn ~(symbol (str name \?)) [r#]
           (= t# (type r#))))
      
       ~(build-positional-factory name classname fields invariants chk)

       (defconstrainedfn ~map-arrow-factory-name
         ([{:keys ~fields :as m#}]
          ~invariants
          (println (str "building " m#))  
          (with-meta
            (merge (new ~name ~@(for [e fields] nil)) m#)
            {:contract ~chk})))
       ~name)))

(defn- apply-contract
  [f]
  (if (:hooked (meta f))
    f
    (with-meta
      (fn [& [m & args]]
        (if-let [contract (and m (-> m meta :contract))]
          ((partial contract identity) (apply f m args))
          (apply f m args)))
      {:hooked true})))

(when *assert*
  (alter-var-root (var assoc) apply-contract)
  (alter-var-root (var dissoc) apply-contract)
  (alter-var-root (var merge) apply-contract)
  (alter-var-root (var merge-with) (fn [f] (let [mw (apply-contract f)] (fn [f & maps] (apply mw f maps)))))
  (alter-var-root (var into) apply-contract)
  (alter-var-root (var conj) apply-contract)
  (alter-var-root (var assoc-in) apply-contract)
  (alter-var-root (var update-in) apply-contract))

(defmacro defconstrainedtype
  [name slots inv-description invariants & etc]
  (check-args! name slots inv-description invariants)
  (let [fields       (vec slots)
        ctor-name    (symbol (str name \.))
        factory-name (symbol (str "->" name))]
    `(do
       (let [t# (deftype ~name ~fields ~@etc)]
         (defn ~(symbol (str name \?)) [r#]
           (= t# (type r#))))
       
       (let [chk# (contract ~(symbol (str "chk-" name))
                            ~inv-description
                            [{:keys ~fields :as m#}] ~invariants)]
         (defconstrainedfn ~factory-name
           (~fields ~invariants
              (~ctor-name ~@fields))))
       ~name)))

(defmacro defconstrainedvar
  [name init inv-description invariants]
  `(do
     (def ~name ~init)
     (set-validator! (var ~name) (partial (contract ~(symbol (str "chk-" name))
                                            ~inv-description
                                            [~name]
                                            ~invariants)
                                          (fn [x#] true)))))

(defmacro constrained-atom
  [init inv-description invariants]
  `(do
     (let [r# (atom ~init)]
       (set-validator! r# (partial (contract ~(symbol (str "chk-atom" ))
                                     ~inv-description
                                     [the-atom#]
                                     ~invariants)
                                   (fn [x#] true)))
       r#)))

(defmacro constrained-ref
  [init inv-description invariants]
  `(do
     (let [r# (ref ~init)]
       (set-validator! r# (partial (contract ~(symbol (str "chk-ref" ))
                                     ~inv-description
                                     [the-ref#]
                                     ~invariants)
                                   (fn [x#] true)))
       r#)))


(defmacro constrained-agent
  [init inv-description invariants]
  `(do
     (let [r# (agent ~init)]
       (set-validator! r# (partial (contract ~(symbol (str "chk-agent" ))
                                     ~inv-description
                                     [the-agent#]
                                     ~invariants)
                                   (fn [x#] true)))
       r#)))

(comment
  ;; HOF support

  (defrecord HOC [args argspec ctx])

  (defmacro _ [args & argspec]
    `(HOC. '~args (vec '~argspec) nil))
  
  (_ [n] even? number? => number?)

  (let [hoc (_ [n] even? number? => number?)]
    (build-contract 'hof (build-constraints-map (:args hoc) (:argspec hoc))))

  (macroexpand '(contract my-map "mymap" [fun sq] [(_ [n] number? => number?) (seq sq) => seq]))

  (contract my-map "mymap" [fun sq] [(_ fun [n] number? => number?) (seq sq) => seq])

  (def cnstr '[(_ fun [n] number? => number?) (seq sq) => seq])
)

