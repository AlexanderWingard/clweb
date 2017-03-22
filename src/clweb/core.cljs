(ns clweb.core
  (:require [cljs.reader :as reader]
            [clweb.types :as t]))
(reader/register-tag-parser! "clweb.types.Greeting" t/map->Greeting)

(enable-console-print!)
(def ws (js/WebSocket. "ws://localhost:3449/ws"))
(defn handle [ws-event]
  (-> ws-event
      (.-data)
      (reader/read-string)
      (println)))
(aset ws "onmessage" handle)
(aset ws "onopen" (fn [] (.send ws "hello")))
