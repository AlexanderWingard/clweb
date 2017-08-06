(ns clweb.components.login-form
  (:require [clweb.components
             :refer
             [any-errors?
              assign-error
              be-action
              clear-errors
              fe-action
              field
              get-val]]
            [clweb.io :refer [ws-send]]
            [clweb.state :as state]))

(def action "login")
(def login-successful "login-successful")
(def state-key :login)
(def username-path [state-key :username])
(def password-path [state-key :password])
(def login-failed-path [state-key :login-failed])

(defn validate [msg state]
  (-> msg
      (clear-errors state-key)
      ((fn [msg] (if (not (state/user-exists? state (get-val msg username-path)))
                     (-> msg
                         (assign-error login-failed-path true)
                         (assign-error username-path "User not found"))
                     (if-not (state/correct-password? state
                                                  (get-val msg username-path)
                                                  (get-val msg password-path))
                       (-> msg
                           (assign-error login-failed-path true)
                           (assign-error password-path "Wrong password"))
                       msg))))))

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

(defmethod be-action action [channel message state]
  (let [checked-msg (validate message state)]
    (if (any-errors? checked-msg state-key)
      (ws-send channel checked-msg)
      (do
        (state/set-logged-in state channel (get-val message username-path))
        (ws-send channel {:action login-successful :user (get-val message username-path)})))))

(defmethod fe-action action [channel message state]
  (swap! state assoc state-key (state-key message)))

(defmethod fe-action login-successful [channel msg state]
  (swap! state dissoc state-key))
