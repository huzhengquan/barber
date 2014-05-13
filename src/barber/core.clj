(ns barber.core
  (:use [hickory.core]
        [hickory.render])
  (:require [clj-http.client :as client]))


(def thred {
  :text-tags #{:span :a :b :strong :u :pre :code :text :abbr :del :em :font :i :q :sub :sup :summary :title :p}
  :ignore-tags #{:link :meta :script :style :head :title :footer :header :textarea :input :iframe :frame :noscript}
  :tag-weight {:p 2 :section 5 :a 0.5 :li 0.2 :h1 1.5 :h2 2 :h3 3 :h4 3 :h5 3 :pre 3 :title 3 :code 3 :img #(+ 10 %) :article 6 :nav 0.5 :form 0.3}
  })

(defn query
  "查询hiccup节点"
  [path nodes]
  (some #(when (= (first %) (first path))
               (if (= 1 (count path))
                  %
                  (query (rest path) (drop 2 %))))
        nodes))

(defn fortune-telling
  ""
  [subnodes max-strlen]
  (let [merge-nodes (reduce (fn [m v]
                               (if (and (contains? (:text-tags thred) (get v 1))
                                        (= (count v) 2)
                                        (contains? (:text-tags thred) (get (last m) 1)))
                                  (update-in m [(- (count m) 1) 0] + (first v))
                                  (conj m v)))
                             [] subnodes)
        numbers (map first merge-nodes)
        bv (Math/sqrt (/ (+ (apply + numbers) max-strlen) (inc (count numbers))))]
     (apply + (map #(if (> % bv) % (- % bv)) numbers))))

(defn find-max-strlen
  "找到最大的文字长度"
  [node max-count]
  (cond
    (contains? (:ignore-tags thred) (first node))
      max-count
    (string? node)
      (max (count node) max-count)
    (> (count node) 2)
      (apply max (map #(find-max-strlen % max-count) (drop 2 node)))
    :else
      max-count))

(defn stat-node
  "列出所有节点的值和路径"
  [node path]
  (conj
    (if (and (not (contains? (:text-tags thred) (nth node 1)))
             (> (count node) 2))
      (apply concat
        (for [i (range 2 (count node))]
          (stat-node (nth node i) (conj path (- i 2))))))
    [(first node) path]))

(defn scoring
  "给出各节点的分数"
  [node max-strlen]
  (cond
    (string? node)
      [(count (clojure.string/trim node)) :text]
    (contains? (:ignore-tags thred) (first node))
      [0 (first node)]
    (< (count node) 3)
      [0 (first node)]
    :else
      (let [subnodes (map #(scoring % max-strlen) (drop 2 node))
            tag-weight (get (:tag-weight thred) (first node) 1)
            plus (if (number? tag-weight )
                    #(if (> % 0) (* % tag-weight) (/ % tag-weight))
                    tag-weight)
            fen (plus (fortune-telling subnodes max-strlen))]
        (concat [fen (first node)] subnodes))))

(defn clean-attr
  "清理html标签的属性"
  [node]
  (cond
    (string? node)
      node
    (contains? (:ignore-tags thred) (first node))
      [(first node) {}]
    :else
      (vec
        (concat [(first node) (select-keys (get node 1) [:width :height :src :href])]
                (map clean-attr (drop 2 node))))))

(defn get-charset
  "取出"
  [content-type]
  (if (string? content-type)
      (get (into {} (map #(let [tmp (clojure.string/split % #"[=]")] {(first tmp) (get tmp 1)})
                          (clojure.string/split content-type #"[;\s]+")))
          "charset")))
(defn filter-tag
  "过滤不必要的标签"
  [html]
  (clojure.string/replace html #"(?s)<!--.*?-->" ""))

(defn get-html-charset
  "取出html中的charset"
  [hiccup-body]
  (get-charset
    (some #(when (and (= (first %) :meta)
                      (contains? (get % 1) :http-equiv)
                      (= (clojure.string/lower-case (get (get % 1) :http-equiv)) "content-type"))
                 (get (get % 1) :content))
     (drop 2 (query [:html :head] hiccup-body)))))


(defn url->hiccup
  "通过url抓取网页"
  [url]
  (println url)
  (let [content (client/get url {:as :byte-array})
        head-charset (get-charset (:Content-Type (:headers content)))
        hiccup-body (-> (new String (content :body) (or head-charset "utf-8")) filter-tag parse as-hiccup)
        charset (or head-charset
                    (get-html-charset hiccup-body)
                    "utf-8")]
     (if (or head-charset
             (and (string? charset)
                  (contains? ["utf-8" "utf8"] (clojure.string/lower-case charset))))
        hiccup-body
        (-> (new String (:body content) charset) filter-tag parse as-hiccup))))

(defn hiccup->article
  [hiccup-body]
  (let [doc (query [:html] hiccup-body)
        title (get (query [:html :head :title] hiccup-body) 2 "无标题")
        max-strlen (find-max-strlen doc 0)
        scor (scoring doc max-strlen)
        [fen node-path ] (reduce #(if (> (first %2) (first %1)) %2 %1)
                                  (filter #(> (first %) 0) (stat-node scor [])))]
    (if (> fen 0)
      {:title title,
       :weight fen
       :html (hiccup-to-html (list (clean-attr
                (reduce #(get %1 (+ %2 2)) doc node-path))))})))

(defn html->article
  [html]
  (-> html filter-tag parse as-hiccup hiccup->article))

(defn bytes->article
  [html-bytes & args]
  (html->article (new String html-bytes (or (first args) "utf-8"))))

(defn url->article
  [url]
  (-> url url->hiccup hiccup->article))

        ;(-> url chttp/get :body parse as-hiccup)))

#_(def test-html
  "<!DOCTYPE HTML>
  <html>
    <head>
      <title>hello</title>
    </head>
    <body>
    <h1>title</h1>
    <p style=\"color:red\">测试</p>
    <p>那天的天空真郑艳丽</p>
    <p>那天的天空真郑艳丽</p>
    <div>测试，我在北京天安门碰到一个二羽田夕夏子，他说他嘴不能说话了。。。这叫什么事儿呀！</div>
    </body>
  </html>")

#_(def url (last [
;  "http://www.techweb.com.cn/it/2014-05-12/2034768.shtml"
;  "http://www.techweb.com.cn/internet/2014-05-12/2034741.shtml"
  "http://auto.sina.com.cn/car/2014-05-12/07201293130.shtml"
  "http://blog.kurrunk.com/post/10006.html"
  "http://culture.gmw.cn/2014-05/12/content_11283226.htm"
  "http://china.haiwainet.cn/n/2014/0512/c345646-20622210.html"
  "http://finance.people.com.cn/n/2014/0512/c66323-25002623.html"
  "http://news.qq.com/a/20140512/001848.htm"
  "http://news.xinhuanet.com/world/2014-05/12/c_126487218.htm"
  "http://news.xinhuanet.com/politics/2014-05/10/c_1110629193.htm"
  "http://www.chinanews.com/ty/2014/05-12/6161497.shtml"
  "http://www.huxiu.com/article/33542/1.html"
  "http://www.pingwest.com/demo/tisiwi-demo-day-2014/"
  "http://tech.sina.com.cn/i/2014-05-12/16589373965.shtml"
  "http://tech.sina.com.cn/i/2014-05-12/16159373918.shtml"
  "http://www.techweb.com.cn/world/2014-05-12/2035038.shtml"
  "http://finance.cnr.cn/gs/201405/t20140512_515491821.shtml"
  "http://net.chinabyte.com/101/12948101.shtml"
  "http://www.infzm.com/content/99939"
  "http://bj.people.com.cn/n/2014/0512/c82847-21183630.html"
  ]))

#_(defn foo
  "I don't do a whole lot."
  [x]
  (println (bytes->article (:body (client/get url {:as :byte-array})) "gb2312")))

