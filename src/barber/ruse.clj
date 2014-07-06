(ns barber.ruse)

;

(def site-map
  (atom {}))

(defn empty-map []
  (reset! site-map {}))

(defn get-selector
  "取出适用某个URL的规则"
  [url]
  (let [[scheme _ domain path] (clojure.string/split url #"/" 4)]
    (if (contains? @site-map domain)
      (some #(if (re-matches (re-pattern (first %)) path) (last %))
            (get @site-map domain)))))

(defn put
  "为某URL增加规则"
  [domain rematch ruse-map]
  (swap! site-map assoc domain
    (merge
      (get @site-map domain {})
      {rematch (into {}
                  (for [[k [css & windup]] ruse-map]
                    {k (cons css
                        (for [wu windup]
                          (cond (fn? wu) wu
                            (symbol? wu)
                              (eval (list 'fn ['x] (list wu 'x)))
                            (instance? clojure.lang.Cons wu)
                              (eval wu))))}))})))
