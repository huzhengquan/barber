barber
======

barber是一位网页理发师，给一个URL，就会把网页中重点内容反馈给你。

## Installation

`barber` is available as a Maven artifact from
[Clojars](http://clojars.org/barber):

```clojure
[barber "0.1.7"]
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
