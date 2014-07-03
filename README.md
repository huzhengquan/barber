barber
======

barber是一位网页理发师，给一个URL，就会把网页中正文内容反馈给你。

## Installation

`barber` is available as a Maven artifact from
[Clojars](http://clojars.org/barber):

```clojure
[huzhengquan/barber "0.1.9"]
```

## Usage

Require it in the REPL:

```clojure
(require '[barber.core :as barber])
```

Require it in your application:

```clojure
(ns my-app.core
  (:require [barber.core :as barber]))
```

通过URL获取文章内容
```clojure
(barber/url->article "http://...")
=> {:title title
    :html <div>text...</div>}
```

```clojure
(barber/url->article
  "http://..."
  [[:userAgent "Mozilla/5.0..."]
   [:data {"name" "test"}]])
=> {:title title
    :html <div>text...</div>}
```

通过CSS语法选择正文内容
```clojure
(ns my-app.core
  (:require [barber.core :as barber]
            [barber.ruse :as ruse]))

(ruse/put-ruse
  "www.xxx001.com"
  "^p/[a-z0-9]+$"
  {:html ["div.show-content" '.html]
   :author ["div.container>div.people>a.author" 'first '.ownText]
   :title [nil '.title #(first (clojure.string/split % #"[\s\|]+"))]})

(url->article "http://www.xxx001.com/p/test09")
```
