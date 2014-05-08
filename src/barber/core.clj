(ns barber.core
  (:use [hickory.core]
        [hickory.zip])
  (:require [clj-http.client :as chttp]
            [clojure.zip :as zip]))

(defn fetch-page [url]
  (-> (chttp/get url) :body parse as-hiccup))
;  (html/parse url))
;  (chttp/get url))
;  (html/html-resource (java.net.URL. url)))
;  (let [ret (chttp/get url)]
;    (Jsoup/parse (:body ret))))
;  (Jsoup/parse (chttp/get url)))

(defn foo
  "I don't do a whole lot."
  [x]
  (println (some #(when (= (first %) :html)
                    (some (fn [node] (when (= (first node) :body) node))
                          (drop 2 %)))
                 (fetch-page "http://www.baidu.com")))
  (println x "Hello, World!"))
