    (defcontracts
      [sqr [n] number? (not= 0) => pos? number?]
    
      [doubler 
        [x]   number? => number? (= n (* 2 x))
        [x y] (every? number? [x y]) => number? (= n (* 2 (+ y x)))])

**and**

    (cnstrfn 
      "This function squares a number"
      [n]
      [number? (not= 0) => pos? number?]
      (* n n))

**and**

    (defcnstrfn sqr 
      "This function squares a number"
      [n]
      [number? (not= 0) => pos? number?]
      (* n n))

**and**

    (contract
      "A contract for squaring"
      [n] number? (not= 0) => pos? number?)
