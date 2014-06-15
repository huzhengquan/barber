(ns barber.means.oleola
  (:import [org.jsoup Jsoup]
           [org.jsoup.select Selector]
           [org.jsoup.nodes Document Element ]))

(def tags
  "常数"
  {:delete #{"script" "style" "textarea" "input" "noscript" "iframe" "frame"}
})

(defn pass
  "处理文本节点"
  [ele]
  (let [weight (if (= (.tagName ele) "img")
                 (/ (* (Integer/parseInt (.attr ele "width"))
                       (Integer/parseInt (.attr ele "height")))
                    2000)
                 (-> ele .ownText .getBytes count))
        block-parents (filter #(.isBlock %) (.parents ele))
        w-name "barber-weight"
        s-name "barber-score"
        get-old (fn [n e] (if (.hasAttr e n) (Float/parseFloat (.attr e n)) 0))]
        ;(if (= (.tagName ele) "img") (println (map #(.tagName %) block-parents)))
    (reduce
      #(let [[max-score node-weight] %1
             new-node-weight (+ node-weight (get-old w-name %2))
             new-max-score (max max-score (get-old s-name %2))]
          (.attr %2 w-name (str new-node-weight))
          (.attr %2 s-name (str new-max-score))
          [new-max-score new-node-weight])
      [weight (Math/sqrt weight)] block-parents)))


(defn score
  "评出节点的权重，包括父节点操作,返回最高分"
  [ele]
  (if
    (contains? (:delete tags) (.tagName ele))
      (do (.remove ele) 0)
    (apply max
      (cons (if (or (.hasText ele)
                    (and (= (.tagName ele) "img")
                         (.hasAttr ele "width")
                         (.hasAttr ele "height")))
                (first (pass ele)) 0)
        (map #(score %) (.children ele))))))

(defn doc->article
  ""
  [doc]
  {:title (.title doc)
   :html (if-let [max-score (str (score (.body doc)))]
            (->> (map #(list (Float/parseFloat (.attr % "barber-weight")) %)
                  (.getElementsByAttributeValue doc "barber-score" max-score))
              (sort-by first) first last .html))}
  )
