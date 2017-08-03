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
(def login-failed-path [state-key :login-failed])

(defn validate [state db]
  (-> state
      (clear-errors state-key)
      ((fn [state] (if (nil? (get @db (get-in state (conj username-path :value))))
                     (-> state
                         (assign-error login-failed-path true)
                         (assign-error username-path "User not found"))
                     state)))))

(defn on-click [channel state]
  (ws-send channel (assoc @state :action action)))

(defn form [channel state]
  (let [login-failed (= true (get-in @state (conj login-failed-path :error)))]
    [:div.ui.segment
     [:div.ui.form
      (field :text "Username" state username-path)
      (field :password "Password" state password-path)
      [:button.ui.button {:on-click #(on-click channel state) :class (when login-failed "red")} "Login"]
      (when login-failed
        [:div.ui.left.pointing.red.basic.label "Login failed"])]]))

(defmethod be-action action [channel message db]
  (ws-send channel (validate message db)))

(defmethod fe-action action [channel message state]
  (swap! state assoc state-key (state-key message)))
