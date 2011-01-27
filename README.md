Trammel
=======

[Contracts programming](http://c2.com/cgi/wiki?DesignByContract) for Clojure.

- [Official documentation and usage scenarios](http://fogus.me/fun/trammel/)
- [Original announcement](http://blog.fogus.me/2010/05/25/trammel-contracts-programming-for-clojure/) (*syntax has evolved since then*)

Example
-------

### Function Contracts

    (require 'trammel.provide)
    
    (defn sqr [n] (* n n))
    
    (sqr 10)
    ;=> 100
    (sqr 0)
    ;=> 0
    
    (provide/contracts 
      [sqr "Constraints for squaring" 
        [x] [number? (not= 0 x) => number? pos?]])
    
    (sqr 10)
    ;=> 100
    (sqr 0)
    ; java.lang.AssertionError: Assert failed: (not= 0 x)

### Record Invariants

    (use '[trammel.core :only (defconstrainedrecord)])
    
    (defconstrainedrecord Foo [a 1 b 2]
      [(every? number? [a b])]
      Object
      (toString [this] (str "record Foo has " a " and " b)))
    
    ;; default ctor with default values
    (new-Foo)
    ;=> #:user.Foo{:a 1, :b 2}
    
    ;; kwarg ctor
    (new-Foo :a 42)
    ;=> #:user.Foo{:a 42, :b 2}
    
    ;; use like any other map/record
    (assoc (new-Foo) :a 88 :c "foo")
    ;=> #:user.Foo{:a 88, :b 2, :c "foo"}
    
    ;; invariants on records checked at runtime    
    (assoc (new-Foo) :a "foo")
    ; Assert failed: (every? number? [a b])

### Type Invariants

    (use '[trammel.core :only (defconstrainedtype)])
    
    (defconstrainedtype Foo [a 1 b 2]
      [(every? number? [a b])])
    
    (new-Foo)
    #<Foo user.Foo@73683>
    
    ;; invariants on types checked at constructions time
    (new-Foo 1 :b)
    ; Assert failed: (every? number? [a b])

Getting
-------

### Leiningen

Modify your [Leiningen](http://github.com/technomancy/leiningen) dependencies to include Trammel:

    :dependencies [[trammel "0.5.0"] ...]    

### Maven

Add the following to your `pom.xml` file:

    <dependency>
      <groupId>trammel</groupId>
      <artifactId>trammel</artifactId>
      <version>0.5.0</version>
    </dependency>

Notes
-----

Trammel is in its infancy but I think that I have a nice springboard for experimentation and expansion, including:

  - Better error messages
  - Distinct pre and post exceptions
  - Study the heck out of everything Bertrand Meyer ever wrote (in progress)
  - `defconstraint` -- with ability to relax requires and tighten ensures
  - Study the heck out of Racket Scheme (in progress)
  - Modify macros to also allow regular Clojure constraint maps
  - Reference contracts
  - Make the `anything` constraint cheap (elimination)
  - Allow other stand-alones: true/false, numbers, characters, regexes
  - Make `provide-contracts` more amenable to REPL use
  - Generate a Foo? function  (in progress) 

If you have any ideas or interesting references then I would be happy to discuss.

References
----------

- *Object-oriented Software Construction* by Bertrand Meyer
- *Eiffel: The Language* by Bertrand Meyer
- [D](http://www.digitalmars.com/d/2.0/dbc.html)
- *The Fortress Language Specification* by Guy L. Steele Jr., et al.
- [System.Diagnostics.Contracts](http://msdn.microsoft.com/en-us/library/system.diagnostics.contracts.aspx)
- *Contracts for Higher-order Functions* by Robert Bruce Findler and Matthias Felleisen
- [Design by Contract and Unit Testing](http://onestepback.org/index.cgi/Tech/Programming/DbcAndTesting.html)
- [Design by contract for Ruby](http://split-s.blogspot.com/2006/02/design-by-contract-for-ruby.html)
- [Contracts in Racket (A Scheme Descendent)](http://pre.plt-scheme.org/docs/html/guide/contracts.html)
- [A Proof Engine for Eiffel](http://tecomp.sourceforge.net/index.php?file=doc/papers/proof/engine)
- *How to Deign Programs* by Matthias Felleisen, Robert Bruce Findler, Matthew Flatt, and Shriram Krishnamurthi [here](http://www.htdp.org/2003-09-26/Book/)
- *An Axiomatic Basis for Computer Programming* by C.A.R Hoare

Emacs
-----

Add the following to your .emacs file for better Trammel formatting:

    (eval-after-load 'clojure-mode
      '(define-clojure-indent
         (contract 'defun)
         (defconstrainedfn 'defun)
         (defcontract 'defun)
         (provide 'defun)))


Example REPL Session
--------------------

Type the following into a REPL session to see how Trammel might be used.

    (defconstrainedtype Bar 
      [a 4 b 8] 
      [(every? pos? [a b])])
    
    (Bar? (new-Bar))
    
    (defn sqr [n] (* n n))
    
    (provide-contracts
      [sqr "the constraining of sqr" 
        [n] [number? (not= 0 n) => pos? number?]])
    
    (sqr 0)

    (positive-nums -1)

    (type (new-Bar))
    
    (.a (new-Bar  42 77))
    (.b (new-Bar  42 77))
    (.a (new-Bar -42 77))
    (.b (new-Bar  42 -77))

    (defconstrainedfn sqrt
      [x] [(>= x 0) => (>= % 0)]
      (Math/sqrt x))
    
    (defn- bigger-than-zero? [n] (>= n 0))
    
    (defconstrainedfn sqrt
      [x] [bigger-than-zero? => bigger-than-zero?]
      (Math/sqrt x))
    
    (sqrt 10)
    (sqrt -19)
    
    (defconstrainedfn sqrt
      [x] [bigger-than-zero? => bigger-than-zero? (<= (Math/abs (- x (* % %))) 0.01)]
      (Math/sqrt x))
    
    (* (sqrt 30) (sqrt 30))
