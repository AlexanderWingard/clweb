(ns clweb.core
  (:require
   [cljs.reader :as reader]
   [cljs.test]
   [cljsjs.d3 :as d3]
   [cljsjs.semantic-ui]
   [clweb.components :as component]
   [clojure.string :as str]
   [clojure.walk :refer [prewalk-replace]]
   [clojure.reader :refer [register-tag-parser!]]
   [clweb.types :as t]
   [json-html.core :refer [edn->hiccup]]
   [reagent-forms.core :refer [bind-fields]]
   [reagent.core :as reagent :refer [atom]]
   [clweb.io :refer [ws-send]]
   [clweb.components :refer [fe-action]]
   [clweb.components.login-form :as login-form]
   [clweb.components.registration-form :as registration-form]
   ))
(defonce client-state (atom {}))
(defonce server-state (atom nil))
(defonce full-server-state (atom {}))
(enable-console-print!)
(register-tag-parser! "object" (fn [arg] (prn-str arg)))

(def ws-uri
  (let [location (-> js/window .-location)
        host (-> location .-host)
        protocol (-> location .-protocol (case "http:" "ws:" "https:" "wss:"))]
    (str protocol "//" host "/ws")))
(defonce channel (js/WebSocket. ws-uri))

(defn ws-handle-message [message]
  (case (:action message)
    "full-server-state" (reset! full-server-state (:state message))
    "your-state" (do (reset! server-state (:state message))
                     (swap! client-state dissoc :login-failed))
    (fe-action channel message client-state)))
(defn ws-on-message [ws-event]
  (ws-handle-message (reader/read-string (.-data  ws-event))))

(defn ws-open [] ())
(aset channel "onmessage" ws-on-message)
(aset channel "onopen" ws-open)

(aset js/window "onhashchange" (fn [] (println (apply hash-map (str/split (subs (.-hash (.-location js/window)) 1) #"/")))))

(defn input [label type id]
  [:input.form-control {:field type :id id :placeholder label}])

(defn login-button-component []
  [:div.inline.field
   [:button.ui.button
    {:on-click #(ws-send channel (assoc (:user @client-state) :action "login"))}
    "Login"]
   ])

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
   [:div.ui.divider]
   [login-button-component]
   ])

(defn render-clojure [atom]
  (prewalk-replace
   {:table.jh-type-object :table.jh-type-object.ui.celled.table}
   (json-html.core/edn->hiccup @atom)))


(defn login []
  [:div.ui.segment
   [:div.ui.form
    [bind-fields form-template client-state]]])

(defn logout []
  [:button.ui.button {:on-click #(ws-send channel {:action "logout"})} "Logout"])

(defn state-debug-component []
  [:div
   [:h1.ui.header "Client state"]
   (render-clojure client-state)
   [:h1.ui.header "Server state"]
   (render-clojure server-state)
   [:h1.ui.header "Full Server state"]
   (render-clojure full-server-state)])

(defn app []
  [:div.ui.container
   [:h1.ui.header "Charlies Bank"]
   [login-form/form channel client-state]
   [registration-form/form channel client-state]
   [state-debug-component]])

(reagent/render [app] (js/document.getElementById "app"))

(defn figwheel-reload [])
