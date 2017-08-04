(ns clweb.core
  (:require
   [cljs.reader :as reader]
   [cljs.test]
   [cljsjs.d3 :as d3]
   [cljsjs.semantic-ui]
   [clojure.string :as str]
   [clojure.reader :refer [register-tag-parser!]]
   [reagent.core :as reagent :refer [atom]]
   [clweb.components :refer [fe-action]]
   [clweb.components.login-form :as login-form]
   [clweb.components.registration-form :as registration-form]
   [clweb.components.state-debug :as state-debug]
   ))
(enable-console-print!)

(defonce client-state (atom {}))
(register-tag-parser! "object" (fn [arg] (prn-str arg)))

(def ws-uri
  (let [location (-> js/window .-location)
        host (-> location .-host)
        protocol (-> location .-protocol (case "http:" "ws:" "https:" "wss:"))]
    (str protocol "//" host "/ws")))
(defonce channel (js/WebSocket. ws-uri))

(defn ws-handle-message [message]
  (fe-action channel message client-state))
(defn ws-on-message [ws-event]
  (ws-handle-message (reader/read-string (.-data  ws-event))))

(defn ws-open [] ())
(aset channel "onmessage" ws-on-message)
(aset channel "onopen" ws-open)

(aset js/window "onhashchange" (fn [] (println (apply hash-map (str/split (subs (.-hash (.-location js/window)) 1) #"/")))))

(defn app []
  [:div.ui.container
   [:h1.ui.header "Charlies Bank"]
   [login-form/form channel client-state]
   [registration-form/form channel client-state]
   [state-debug/form client-state]])

(reagent/render [app] (js/document.getElementById "app"))

(defn figwheel-reload [])
