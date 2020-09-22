(ns rolling-rules.util)

(defmacro unless
  "Inverted 'if'"
  [test & branches]
  (conj (reverse branches) test 'if))

(defmacro for* [seq-exprs body-expr]
  (let [i (gensym)]
    `(let [~i (volatile! 0)]
       (doall (for ~seq-exprs 
                (with-meta
                  (do (vswap! ~i inc)
                      ~body-expr)
                  {:key @~i}))))))

(defmacro for-idx [idx seq-exprs body-expr]
  `(let [~idx (volatile! 0)]
     (doall (for ~seq-exprs
              (with-meta
                (do (vswap! ~idx inc)
                    ~body-expr)
                {:key @~idx})))))