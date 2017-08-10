(ns clweb.components.login-front-view
  (:require [clweb.components.login-form :as login-form]))

(defn dom [channel state component]
  [:div
   [:div.big-logo
    [:h1"Bank of Charlie"]
    [:div "Est. 2017"]]
   [:div {:style {:max-width "400px" :margin "auto"}} component]])
