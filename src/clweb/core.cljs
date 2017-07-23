(ns clweb.core
  (:require
   [cljs.reader :as reader]
   [cljs.test]
   [cljsjs.d3 :as d3]
   [cljsjs.semantic-ui]
   [clojure.string :as str]
   [clweb.types :as t]
   [reagent-forms.core :refer [bind-fields]]
   [reagent.core :as reagent]
   ))
(defonce state (atom {}))
(enable-console-print!)

(def ws-uri
  (let [location (-> js/window .-location)
        host (-> location .-host)
        protocol (-> location .-protocol (case "http:" "ws:" "https:" "wss:"))]
    (str protocol "//" host "/ws")))
(def ws (js/WebSocket. ws-uri))
(defn ws-message [ws-event]
  ())
(defn ws-open []
  ())
(aset ws "onmessage" ws-message)
(aset ws "onopen" ws-open)
(aset js/window "onhashchange" (fn [] (println (apply hash-map (str/split (subs (.-hash (.-location js/window)) 1) #"/")))))

(defn input [label type id]
  [:input.form-control {:field type :id id :placeholder label}])

(def form-template
  [:div.ui.segment
   [:div.ui.form
    [:div.ui.field
     [:label "Username:"]
     [:div.ui.icon.input
      (input "Username" :text :user.name)
      [:i.user.icon]]]
    [:div.ui.field
     [:label "Password:"]
     [:div.ui.icon.input
      (input "Password" :password :user.password)
      [:i.lock.icon]]]
    [:button.ui.button "Login"]]])

(defn app []
  (let [s (reagent/atom {})]
    (fn []
      [:div.ui.container
       [bind-fields form-template s]
       [:div (str @s)]])))

(reagent/render [app] (js/document.getElementById "app"))

(defn figwheel-reload [])
