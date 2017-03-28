(ns clweb.core
  (:require [cljs.reader :as reader]
            [clweb.types :as t]
            [clojure.string :as str]
            [cljsjs.d3]
            [cljsjs.semantic-ui]
            ))
(reader/register-tag-parser! "clweb.types.Greeting" t/map->Greeting)

(defonce state (atom {:list []}))

(enable-console-print!)
(def ws-uri
  (let [location (-> js/window .-location)
        host (-> location .-host)
        protocol (-> location .-protocol (case "http:" "ws:" "https:" "wss:"))]
    (str protocol "//" host "/ws")))
(def ws (js/WebSocket. ws-uri))
(defn handle [ws-event]
  (let [greet (-> ws-event
                  (.-data)
                  (reader/read-string)
                  (t/greeting))]
    (swap! state update-in [:list] #(conj % greet))))
(aset ws "onmessage" handle)
(aset ws "onopen" (fn [] (.send ws (pr-str (t/Greeting. "I'm here")))))
(aset js/window "onhashchange" (fn [] (println (apply hash-map (str/split (subs (.-hash (.-location js/window)) 1) #"/")))))

(.. js/d3
    (select "body")
    (text (str @state)))

(defn figwheel-reload []
  (println @state))
