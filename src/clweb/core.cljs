(ns clweb.core
  (:require
   [cljs.reader :as reader]
   [cljs.test]
   [cljsjs.d3 :as d3]
   [cljsjs.semantic-ui]
   [clojure.string :as str]
   [clojure.walk :refer [prewalk-replace]]
   [clweb.types :as t]
   [json-html.core :refer [edn->hiccup]]
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
(defonce ws (js/WebSocket. ws-uri))
(defn ws-on-message [ws-event]
  (let [message (reader/read-string (.-data  ws-event))]
    (case (:action message)
      "get-uuid" (swap! state assoc :uuid (:uuid message)))))
(defn ws-open []
  (.send ws (pr-str {:action "get-uuid"})))
(defn ws-send [data]
  (.send ws (pr-str data)))
(aset ws "onmessage" ws-on-message)
(aset ws "onopen" ws-open)

(aset js/window "onhashchange" (fn [] (println (apply hash-map (str/split (subs (.-hash (.-location js/window)) 1) #"/")))))

(defn input [label type id]
  [:input.form-control {:field type :id id :placeholder label}])

(def form-template
  [:div
   [:div.ui.field
    [:label "Username:"]
    [:div.ui.icon.input
     (input "Username" :text :user.name)
     [:i.user.icon]]]
   [:div.ui.field
    [:label "Password:"]
    [:div.ui.icon.input
     (input "Password" :password :user.password)
     [:i.lock.icon]]]])

(defn app []
  (let [s (reagent/atom {})]
    (fn []
      [:div.ui.container
       [:div.ui.segment
        [:div.ui.form
         [bind-fields form-template s]
         [:button.ui.button {:on-click #(ws-send (merge @state (assoc @s :action "login")))} "Login"]]]
       (prewalk-replace {:table.jh-type-object :table.jh-type-object.ui.celled.table} (json-html.core/edn->hiccup @s))])))

(reagent/render [app] (js/document.getElementById "app"))

(defn figwheel-reload [])
