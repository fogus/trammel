Trammel
=======

[Contracts programming](http://c2.com/cgi/wiki?DesignByContract) for Clojure.

- [Official documentation and usage scenarios](http://fogus.me/fun/trammel/)
- [Original announcement](http://blog.fogus.me/2010/05/25/trammel-contracts-programming-for-clojure/) (*syntax has evolved since then*)

Example
-------

    (use '[fogus.me.trammel :only [provide-contracts]])
    
    (defn sqr [n] (* n n))
    
    (sqr 10)
    ;=> 100
    (sqr 0)
    ;=> 0
    
    (provide-contracts 
      [sqr "Constraints for squaring" 
        [x] [number? (not= 0 x) => number? pos?]])
    
    (sqr 10)
    ;=> 100
    (sqr 0)
    ; java.lang.AssertionError: Assert failed: (not= 0 x)

Getting
-------

### Leiningen

Modify your [Leiningen](http://github.com/technomancy/leiningen) dependencies to include Trammel:

    :dependencies [[trammel "0.3.0"] ...]    

### Maven

Add the following to your `pom.xml` file:

    <dependency>
      <groupId>trammel</groupId>
      <artifactId>trammel</artifactId>
      <version>0.3.0</version>
    </dependency>

Notes
-----

Trammel is in its infancy but I think that I have a nice springboard for experimentation and expansion, including:

  1. Abstracting out the use of `partial`  [done](http://github.com/fogus/trammel/commit/2f03a992d00b97c1f7e354fff32174b4c1edd1d8)
  2. Better error messages
  3. Distinct pre and post exceptions
  4. An all-in-one defn/contract           [done](http://is.gd/cCsvF)
  5. Study the heck out of everything Bertrand Meyer ever wrote (in progress)
  6. Choose better names than `:requires` and `:ensures` [done](http://github.com/fogus/trammel/commit/7427fac79f813dd2877fdb4c23e012f76aa9fb8e)
  7. Type invariants (in progress)
  8. `defconstraint` -- with ability to relax requires and tighten ensures
  9. Implicit arguments for isolated function. (in progress)
 10. Study the heck out of Racket Scheme (in progress)
 11. Modify macros to also allow regular Clojure constraint maps
 12. Reference contracts
 13. `or`
 14. 

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
