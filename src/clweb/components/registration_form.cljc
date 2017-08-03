(ns clweb.components.registration-form
  (:require [clweb.io :refer [ws-send]]
            [clweb.components :refer [fe-action
                                      be-action
                                      assign-error
                                      field
                                      clear-errors]]))

(def action "register")
(def state-key :registration-form)

(defn validate [channel state]
  (-> state
      (clear-errors state-key)
      (assign-error [state-key :password-1] "bad password")))

(defn on-click [channel state]
  (ws-send channel (assoc @state :action action)))

(defn form [channel state]
  [:div.ui.segment
   [:div.ui.form
    (field :text "Username" state [state-key :username])
    [:div.two.fields
     (field :password "Password" state [state-key :password-1])
     (field :password "Repeat" state [state-key :password-2])]
    [:button.ui.button {:on-click #(on-click channel state)} "Register"]]])

(defmethod be-action action [channel message]
  (ws-send channel (validate channel message)))

(defmethod fe-action action [channel message state]
  (swap! state assoc state-key (state-key message)))
