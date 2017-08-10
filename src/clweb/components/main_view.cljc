(ns clweb.components.main-view
  (:require [clweb.fe-state :as fes]
            [clweb.components.login-form :as login-form]))

(defn dom [channel state]
  [:div
   [:div.top-bar
    [:h1 "Bank of Charlie"]]
   [:div.ui.container {:style {:padding-right "319px" :position "relative" }}
    [:div.ui.right.internal.rail {:style {:width "319px" :padding "0px" :margin "0px"}}
     [:div.ui.segment "Logged in as " [:strong (fes/logged-in? state)]
      [:div [:a {:style {:cursor "pointer"}
                 :on-click #(login-form/on-click-logout channel state)}
             "Logout"]]]]
    [:div.ui.segment (fes/location state)]]])

