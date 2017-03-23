(ns clweb.core
  (:require [cljs.reader :as reader]
            [clweb.types :as t]))
(reader/register-tag-parser! "clweb.types.Greeting" t/map->Greeting)

(defonce state (atom {:list []}))

(enable-console-print!)
(def ws (js/WebSocket. "ws://localhost:3449/ws"))
(defn handle [ws-event]
  (let [greet (-> ws-event
                  (.-data)
                  (reader/read-string)
                  (t/greeting))]
    (swap! state update-in [:list] #(conj % greet))))
(aset ws "onmessage" handle)
(aset ws "onopen" (fn [] (.send ws (pr-str (t/Greeting. "I'm here")))))

(defn figwheel-reload []
  (println @state))
