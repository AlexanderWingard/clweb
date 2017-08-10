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
            [clweb.state :as state]
            [clweb.fe-state :as fes]))

(def login-action "login")
(def logout-action "logout")
(def login-successful "login-successful")
(def logout-successful "logout-successful")
(def state-key :login)
(def username-path [state-key :username])
(def password-path [state-key :password])
(def login-failed-path [state-key :login-failed])

(defn validate [msg state]
  (-> msg
      (clear-errors state-key)
      ((fn [msg] (if (not (state/user-exists? state (get-val msg username-path)))
                     (-> msg
                         (assign-error username-path "User not found"))
                     (if-not (state/correct-password? state
                                                  (get-val msg username-path)
                                                  (get-val msg password-path))
                       (-> msg
                           (assign-error password-path "Wrong password"))
                       msg))))))

(defn on-click-login [channel state]
  (ws-send channel (assoc @state :action login-action)))

(defn on-click-logout [channel state]
  (fes/logout state)
  (ws-send channel {:action logout-action}))

(defn form [channel state]
  (let [login-failed (any-errors? @state state-key)]
    [:div
     [:div.ui.form
      (field :text "Username" state username-path)
      (field :password "Password" state password-path)
      [:div {:style {:text-align "center"}}
       [:button.ui.button {:on-click #(on-click-login channel state) :class (when login-failed "red")} "Login"]
       [:br]
       [:a {:href "#register"} "Register new user"]]]]))

(defmethod be-action login-action [channel message state]
  (let [checked-msg (validate message state)]
    (if (any-errors? checked-msg state-key)
      (ws-send channel checked-msg)
      (do
        (state/set-logged-in state channel (get-val message username-path))
        (ws-send channel {:action login-successful :user (get-val message username-path)})))))

(defmethod be-action logout-action [channel message state]
  (state/logout state channel)
  (ws-send channel {:action logout-successful}))

(defmethod fe-action login-action [channel message state]
  (swap! state assoc state-key (state-key message)))

(defmethod fe-action login-successful [channel msg state]
  (fes/login state (:user msg))
  (swap! state dissoc state-key))

(defmethod fe-action logout-successful [channel msg state]
  )
