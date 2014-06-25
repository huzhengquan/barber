(ns barber.core
  (:require [barber.ruse :as ruse]
            [barber.means.oleola :as bef])
  (:import [org.jsoup Jsoup]
           [org.jsoup.select Selector]
           [org.jsoup.nodes Document Element]))

(defn- url->document [url args]
  "args:
    [[:userAgent \"Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)\" ]
     [:data [\"name\" \"test\"]]] "
  (.get
    (reduce
      (fn [conn [x y]]
        (case x
          :userAgent
            (.userAgent conn y)
          :data
            (.data conn (first y) (get y 2))
          :cookie
            (.cookie conn y)
          conn))
      (. Jsoup connect url) args)))

(defn- select-article
  "{:title  [nil .title]
    :author [\"div.show-author\" '.text]
    :html [\"div.show-content\" '.html]}"
  [doc query & args]
  (into {} (for [[k [css & fns]] query]
      (let [eles (if (string? css) (Selector/select css doc) doc)
            ret (reduce (fn [ele tfn] (tfn ele)) eles fns)]
        (if k {k ret})))))

(defn put-ruse
  "设置一个选择器规则"
  [domain rematch ruse-map]
  (ruse/put domain rematch ruse-map))

(defn url->article
  [url & args]
  (if-let [doc (url->document url (first args))]
    (merge  
      (if-let [query (ruse/get-selector (.baseUri doc))]
        (select-article doc query)
        (do
          (print "doc->article:" url)
          (time (bef/doc->article doc))))
      {:uri (.baseUri doc)})))

(defn- foo
  "I don't do a whole lot."
  [x]
  (let [url (last [
            "http://www.techweb.com.cn/it/2014-05-12/2034768.shtml"
            "http://tech.sina.com.cn/i/2014-05-12/16159373918.shtml"
            ])]
    (println (url->article url))
))


