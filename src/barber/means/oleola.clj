(ns barber.means.oleola
  (:import [org.jsoup Jsoup]
           [org.jsoup.select Selector]
           [org.jsoup.safety Cleaner Whitelist]
           [org.jsoup.nodes Document Element ]))

; a, b, blockquote, br, caption, cite, code, col, colgroup, dd, dl, dt, em, h1, h2, h3, h4, h5, h6, i, img, li, ol, p, pre, q, small, strike, strong, sub, sup, table, tbody, td, tfoot, th, thead, tr, u, ul
(def tags
  "常数"
  {:whitelist (into-array ["article" "section" "del" "header" "title"]) 
   :html5-block #{"article" "section"}
   :delete #{"iframe" "frame" "textarea" "noscript" "form"}})

(defn- pass
  "处理文本节点"
  [ele]
  (let [weight (if (= (.tagName ele) "img")
                 (quot (try (* (Integer/parseInt (.attr ele "width"))
                               (Integer/parseInt (.attr ele "height")))
                          (catch Exception e 200))
                    2000.0)
                 (-> ele .ownText .getBytes count float))
        block-parents (filter
                        #(or (.isBlock %)
                             (contains? (:html5-block tags) (.tagName %)))
                        (.parents ele))
        w-name "barber-weight"
        s-name "barber-score"
        get-old (fn [n e] (if (.hasAttr e n) (Float/parseFloat (.attr e n)) 0.0))
        next-weight #(if (< % 6) (- % 4) (Math/sqrt %))]
    (reduce
      #(let [[max-score node-weight] %1
             new-node-weight (+ node-weight (get-old w-name %2))
             new-max-score (max max-score (get-old s-name %2))]
          (.attr %2 w-name (format "%.2f" new-node-weight))
          (.attr %2 s-name (format "%.2f" new-max-score))
          [new-max-score (next-weight node-weight)])
      [weight (next-weight weight)] block-parents)))


(defn- score
  "评出节点的权重，包括父节点操作,返回最高分"
  [ele]
    (apply max
      (cons (if (or (.hasText ele)
                    (and (= (.tagName ele) "img")
                         (.hasAttr ele "width")
                         (.hasAttr ele "height")))
                (first (pass ele)) 0.0)
        (map #(score %) (.children ele)))))

(defn- rm-attr
  "删除多余的属性"
  [attrs ele]
  (doseq [attr attrs]
    (if-let [find-ele (.getElementsByAttribute ele attr)]
      (.removeAttr find-ele attr)))
  ele)

(defn- rm-tags
  "删除多余的tag"
  [doc tags]
  (doseq [tag tags]
    (.remove (.getElementsByTag doc tag)))
  doc)

(defn doc->article
  ""
  [dirtyDoc]
  {:title (.title dirtyDoc)
   :html (let [doc (. (Cleaner.
                        (.addTags
                          (. Whitelist relaxed)
                          (:whitelist tags)))
                      clean (rm-tags dirtyDoc (:delete tags)))
               max-score (format "%.2f" (score (.body doc)))]
            (->> (map #(list (Float/parseFloat (.attr % "barber-weight")) %)
                  (.getElementsByAttributeValue doc "barber-score" max-score))
              (sort-by first) last last
              (rm-attr ["barber-weight" "barber-score"])
              .html))})
