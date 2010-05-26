Trammel
=======

I was planning on making a grand announcement about the launch of my Clojure contracts programming library [Trammel](http://github.com/fogus/trammel), but got totally upstaged by something called [Clojure/core](http://clojure.com). ^_^

While researching for [The Joy of Clojure](http://joyofclojure.com) I eventually came by a few books about the [Eiffel Programming Language](http://archive.eiffel.com/eiffel/nutshell.html) and was blown away by its notion of [design by contract](http://en.wikipedia.org/wiki/Eiffel_(programming_language)#Design_by_Contract)&copy;.  I've posted before about Clojure's [pre- and post-conditions](http://blog.fogus.me/2009/12/21/clojures-pre-and-post/) but didn't take it to the next step until chapter 7 of JoC -- which forms the basis for Trammel.  At the moment I have only the base form `contract` returning a higher-order function that can then be partially applied to an existing function to "apply" a contract:

    (def cheese-contract
      (contract cheese
        [x]
        (requires 
          (= x :cheese))
        (ensures
          (string? %)
          (= % "cheese"))
        
        [x y]
        (requires
          (every? #(= :cheese %) [x y]))
        (ensures 
          (string? %))))
    
    (def do-something 
      (partial
        cheese-contract
        (fn 
          ([x] (name x))
          ([x y] (str x y)))))
    
    (do-something :cheese)
    ;=> "cheese"
    
    (do-something :foo)
    ; java.lang.AssertionError: Assert failed: (= x :cheese)
    
    (do-something :cheese :cheese)
    ;=> ":cheese:cheese"
    
    (do-something :cheese :ham)
    ; java.lang.AssertionError: Assert failed: 
    ;    (every? (fn* [p1__6079#] (= :cheese p1__6079#)) [x y])

Anyway, Trammel is in its infancy but I think that I have a nice springboard for experimentation and expansion, including:

  1. Abstracting out the use of `partial`  (in progress)
  2. Better error messages
  3. Distinct pre and post exceptions
  4. An all-in-one defn/contract           (in progress)
  5. Study the heck out of everything Bertrand Meyer ever wrote (in progress)
  6. Choose better names than `requires` and `ensures`

If you have any ideas or interesting references then I would be happy to discuss.

:f
