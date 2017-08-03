(ns clweb.components.login-form
  (:require [clweb.io :refer [ws-send]]
            [clweb.components :refer [fe-action
                                      be-action
                                      assign-error
                                      field
                                      clear-errors]]))

(def action "login")
(def state-key :login)
(def username-path [state-key :username])
(def password-path [state-key :password])

(defn validate [state]
  (-> state
      (clear-errors state-key)
      (assign-error username-path (str (get-in state (conj username-path :value)) " is a bad name"))))

(defn on-click [channel state]
  (ws-send channel (assoc @state :action action)))

(defn form [channel state]
  [:div.ui.segment
   [:div.ui.form
    (field :text "Username" state username-path)
    (field :password "Password" state password-path)
    [:button.ui.button {:on-click #(on-click channel state)} "Login"]]])

(defmethod be-action action [channel message]
  (ws-send channel (validate message)))

(defmethod fe-action action [channel message state]
  (swap! state assoc state-key (state-key message)))
