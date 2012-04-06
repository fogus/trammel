Trammel v0.7.0 Release Notes
============================

Trammel is a [Clojure](http://clojure.org) providing contracts programming (sometimes called "[Design by Contract](http://en.wikipedia.org/wiki/Design_by_Contract)" or DbC) capabilities.  Features of Trammel currently include:

* Contracts on functions
* Composable contracts
* Post-definition function contract application
* defrecord and deftype invariants
* Reference type invariants (i.e. Agents, Atoms, Refs, and Vars)
* Ability to turn-off checks in a production setting
* Support for Clojure versions 1.3 and 1.4

Trammel is inspired by [Eiffel](http://www.eiffel.com/) and [Racket Scheme](http://docs.racket-lang.org/reference/contracts.html).

Absorb
------

You can use core.cache in your [Leiningen](https://github.com/technomancy/leiningen) and [Cake](https://github.com/flatland/cake) projects with the following `:dependencies` directive in your `project.clj` file:

        [trammel "0.7.0]

For Maven-driven projects, use the following slice of XML in your `pom.xml`'s `<dependencies>` section:

    <dependency>
	  <groupId>org.clojure</groupId>
	  <artifactId>trammel</artifactId>
	  <version>0.7.0</version>
	</dependency>

Enjoy!


Places
------

* [Source code](https://github.com/fogus/trammel)
* [Ticket system](https://github.com/fogus/trammel/issues)
* [Examples and documentation](http://fogus.github.com/trammel/) -- in progress
* [Dream Date](http://github.com/fogus/dream-date) - an example project using Trammel -- in progress

Changes from version 0.6.0
--------------------------

* Better assertion error reporting
* Ability to add constraint descriptions used in assertion reporting
* Constrained defrecords no longer support default field values
* Constrained defrecords no longer generate keyword args ctor function
* Reference type invariants

Reference type invariants work in the following ways.

### Atoms

Atoms are created directly with their invariants:

```clojure

    (def a (constrained-atom 0
             "only numbers allowed"
             [number?]))
    
    @a 
	;=> 0

```

And checked on change:

```clojure

    (swap! a inc)
	;=> 1

```

Invariant violations are reported right away:

```clojure

    (swap! a str)
	; Pre-condition failure: only numbers allowed 
    
	(compare-and-set! a 0 "a")
	; Pre-condition failure: only numbers allowed 
	
```

### Refs

Refs are created directly with their invariants:

```clojure

    (def r (constrained-ref 0
             "only numbers allowed"
             [number?]))

```

And also checked on change, within a transaction:

```clojure

    (dosync (alter r inc))
	;=> 1
    
    (dosync (alter r str))
	; Pre-condition failure: only numbers allowed 

```

### Vars

Vars are created directly with their invariants:

```clojure

    (defconstrainedvar ^:dynamic foo 0
      "only numbers allowed in Var foo"
      [number?])

```

Var invariants are checked on rebinding, such as with `binding`:

```clojure

    (binding [foo :a] [foo])
	; Pre-condition failure: only numbers allowed 

```

### Agents

Agents are created directly with their invariants:

```clojure

    (def ag (constrained-agent 0
             "only numbers allowed"
             [number?]))

```

And are checked on `send` and `send-off`

    (send ag str)
    (agent-error ag)

However, the invariant violations are reported consistently with the agent setup. In this case, the errors are accessible via the `agent-error` function.


Plans
-----

The following capabilities are under design, development, or consideration for future versions of Trammel:

  - Contracts for higher-order functions
  - Better error messages
  - `defconstraint` -- with ability to relax requires and tighten ensures
  - Modify macros to also allow regular Clojure constraint maps
  - Allow other stand-alones: true/false, numbers, characters, regexes
  - Marrying test.generative with Trammel
  - More documentation and examples

More planning is needed around capabilities not listed nor thought of.