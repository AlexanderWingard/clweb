(ns clweb.core
  (:require
   [cljs.reader :as reader]
   [cljs.test]
   [cljsjs.d3 :as d3]
   [cljsjs.semantic-ui]
   [clojure.string :as str]
   [clojure.walk :refer [prewalk-replace]]
   [clojure.reader :refer [register-tag-parser!]]
   [clweb.types :as t]
   [json-html.core :refer [edn->hiccup]]
   [reagent-forms.core :refer [bind-fields]]
   [reagent.core :as reagent :refer [atom]]
   ))
(defonce state (atom {}))
(defonce server-state (atom {}))
(enable-console-print!)
(register-tag-parser! "object" (fn [arg] (prn-str arg)))

(def ws-uri
  (let [location (-> js/window .-location)
        host (-> location .-host)
        protocol (-> location .-protocol (case "http:" "ws:" "https:" "wss:"))]
    (str protocol "//" host "/ws")))
(defonce ws (js/WebSocket. ws-uri))
(defn ws-on-message [ws-event]
  (let [message (reader/read-string (.-data  ws-event))]
    (case (:action message)
      "server-state" (reset! server-state (:state message)))))
(defn ws-open []
  ())
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
     [:i.lock.icon]]]
   [:button.ui.button {:on-click #(ws-send (assoc (:user @state) :action "login"))} "Login"]])

(defn render-clojure [atom]
  (prewalk-replace
   {:table.jh-type-object :table.jh-type-object.ui.celled.table}
   (json-html.core/edn->hiccup @atom)))

(defn app []
  [:div.ui.container
   [:h1.ui.header "Charlies Bank"]
   [:div.ui.segment
    [:div.ui.form
     [bind-fields form-template state]]]
   [:h1.ui.header "Client state"]
   (render-clojure state)
   [:h1.ui.header "Server state"]
   (render-clojure server-state)])

(reagent/render [app] (js/document.getElementById "app"))

(defn figwheel-reload [])
