(ns barber.core-test
  (:require [clojure.test :refer :all]
            [barber.core :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))

#_(put-ruse "js.io"
            "^p/[a-z0-9]+$"
            {:html ["div.show-content" '.html]
             :author ["div.container>div.people>a.author" 'first '.ownText]
             :title [nil '.title #(first (clojure.string/split % #"[\s\|]+"))]})
  #_(put-ruse "www.vn.com"
            "^[a-z0-9\\-/]+$"
            [[nil ["div.post_source" '.remove]]
             [:html ["div.entry-content" '.html]]
             [:title ["h1.entry-title" '.text]]])
#_(println (ruse/get-selector "http://js.io/p/6d010dab2c2a"))

#_(ruse/put "js.com"
            "^p/[a-z0-9]+$"
            (read-string
            "{:html [\"div.show-content\" .html]
             :author [\"div.container>div.people>a.author\" first .ownText]
             :title [nil .title #(first (clojure.string/split % #\"[\\s\\|]+\"))]}"))
