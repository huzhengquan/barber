(ns barber.core
  (:require [barber.ruse :as ruse]
            [barber.means.oleola :as bef])
  (:import [org.jsoup Jsoup]
           [org.jsoup.select Selector]
           [org.jsoup.nodes Document Element]))

(defn- url->document
  [url]
  (. (. Jsoup connect url) get))

(defn- select-article
  "{:title  [nil .title]
    :author [\"div.show-author\" '.text]
    :html [\"div.show-content\" '.html]}"
  [doc query & args]
  (into {} (for [[k [css & fns]] query]
      (let [eles (if (string? css) (Selector/select css doc)
                    doc)]
        {k (reduce (fn [ele tfn] (tfn ele))
                   eles fns)}))))
(defn put-ruse
  "设置一个选择器规则"
  [domain rematch ruse-map]
  (ruse/put domain rematch ruse-map))

(defn url->article
  [url]
  (if-let [doc (url->document url)]
    (merge  
      (if-let [query (ruse/get-selector (.baseUri doc))]
        (select-article doc query)
        (bef/doc->article doc))
      {:uri (.baseUri doc)})))

(defn- foo
  "I don't do a whole lot."
  [x]
#_(put-ruse "jianshu.io"
            "^p/[a-z0-9]+$"
            {:html ["div.show-content" '.html]
             :author ["div.container>div.people>a.author" 'first '.ownText]
             :title [nil '.title #(first (clojure.string/split % #"[\s\|]+"))]})
#_(println (ruse/get-selector "http://jianshu.io/p/6d010dab2c2a"))
(let [url (last [
  "http://www.techweb.com.cn/it/2014-05-12/2034768.shtml"
  "http://www.techweb.com.cn/internet/2014-05-12/2034741.shtml"
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
  "http://jianshu.io/p/6d010dab2c2a"
  "http://net.chinabyte.com/101/12948101.shtml"
  "http://www.infzm.com/content/99939"
  "http://finance.cnr.cn/gs/201405/t20140512_515491821.shtml"
  "http://bj.people.com.cn/n/2014/0512/c82847-21183630.html"
  "http://t.cn/RvNKyxO"
  ])]

  (println (url->article url))
  (println url)
))


