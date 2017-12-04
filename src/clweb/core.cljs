(ns clweb.core
  (:require
   [cljs.reader :as reader]
   [cljs.test]
   [cljsjs.d3 :as d3]
   [cljsjs.semantic-ui]
   [clojure.string :as str]
   [clojure.reader :refer [register-tag-parser!]]
   [reagent.core :as reagent :refer [atom]]
   [clweb.fe-state :as fes]
   [clweb.components :refer [fe-action]]
   [clweb.components.login-front-view :as login-front-view]
   [clweb.components.main-view :as main-view]
   [clweb.components.login-form :as login-form]
   [clweb.components.registration-form :as registration-form]
   [clweb.components.state-debug :as state-debug]))
(enable-console-print!)

(def css-transition-group
  (reagent/adapt-react-class js/React.addons.CSSTransitionGroup))

(defonce fe-state (atom {}))
(register-tag-parser! "object" (fn [arg] (prn-str arg)))

(def ws-uri
  (let [location (-> js/window .-location)
        host (-> location .-host)
        protocol (-> location .-protocol (case "http:" "ws:" "https:" "wss:"))]
    (str protocol "//" host "/ws")))
(defonce channel (js/WebSocket. ws-uri))

(defn ws-handle-message [message]
  (fe-action channel message fe-state))
(defn ws-on-message [ws-event]
  (ws-handle-message (reader/read-string (.-data  ws-event))))

(defn ws-open [] ())
(aset channel "onmessage" ws-on-message)
(aset channel "onopen" ws-open)

(defn hash-change []
  (fes/location fe-state (.-hash (.-location js/window))))
(aset js/window "onhashchange" hash-change)
(hash-change)

(defn app []
  [:div {:style {:position "relative"}}
   [css-transition-group {:transition-name "foo"
                          :transition-enter-timeout 1000
                          :transition-leave-timeout 1000}
    ^{:key (fes/location fe-state)}
    [:div {:style {:position "absolute" :width "100%"}}
     (cond
       (fes/location-is fe-state "#register")
       [login-front-view/dom channel
        fe-state
        [registration-form/form channel fe-state]]

       (not (fes/logged-in? fe-state))
       [login-front-view/dom channel
        fe-state
        [login-form/form channel fe-state]]

       :else [main-view/dom channel fe-state])
     ;; [state-debug/form fe-state]
]]])

(reagent/render [app] (js/document.getElementById "app"))

(defn figwheel-reload [])
