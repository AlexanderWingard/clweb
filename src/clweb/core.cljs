(ns clweb.core
  (:require [cljs.reader :as reader]
            [clweb.types :as t]
            [clojure.string :as str]
            [cljsjs.d3 :as d3]
            [cljsjs.semantic-ui]
            [cljs.test]
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
(def in (.getElementById js/document "in"))
(defn render [parent term]
  (if (coll? term)
    (let [select (.. parent
                     (selectAll (fn []  (.-childNodes (.node parent))))
                     (data (apply array term)))
          enter  (.. select
                     (enter)
                     (append "div"))
          exit   (.. select
                     (exit)
                     (remove))
          update (.. select
                     (merge enter)
                     (style "background-color" "rgba(1, 1, 1, 0.2)")
                     (style "margin-left" "20px")
                     (each (fn [d] (this-as t (render (.select js/d3 t) d)))))])
    (.text parent term)))

(defn parse-in []
  (let [term (try
               (reader/read-string (.-value in))
               (catch js/Error e '(nil)))
        top (.select js/d3 "#out")]
    (render top term))
  )
(aset in "onkeyup" parse-in)
(parse-in)

(defn the-truth [] true)

(defn figwheel-reload []
  (println @state))
