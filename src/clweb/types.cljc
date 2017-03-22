(ns clweb.types)

(defprotocol Greeter
  (greeting [this]))

(defrecord Greeting [msg]
  Greeter
  (greeting [_] (str "Server says: " msg)))
