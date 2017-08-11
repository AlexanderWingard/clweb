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
            [clweb.be-state :as bes]
            [clweb.fe-state :as fes]))

(def login-action "login")
(def logout-action "logout")
(def login-successful "login-successful")
(def logout-successful "logout-successful")
(def state-key :login)
(def username-path [state-key :username])
(def password-path [state-key :password])
(def login-failed-path [state-key :login-failed])

(defn validate [msg be-state]
  (-> msg
      (clear-errors state-key)
      ((fn [msg] (if (not (bes/user-exists? be-state (get-val msg username-path)))
                     (-> msg
                         (assign-error username-path "User not found"))
                     (if-not (bes/correct-password? be-state
                                                  (get-val msg username-path)
                                                  (get-val msg password-path))
                       (-> msg
                           (assign-error password-path "Wrong password"))
                       msg))))))

(defn on-click-login [channel fe-state]
  (ws-send channel (assoc @fe-state :action login-action)))

(defn on-click-logout [channel fe-state]
  (fes/logout fe-state)
  (ws-send channel {:action logout-action}))

(defn form [channel fe-state]
  (let [login-failed (any-errors? @fe-state state-key)]
    [:div
     [:div.ui.form
      (field :text "Username" fe-state username-path)
      (field :password "Password" fe-state password-path)
      [:div {:style {:text-align "center"}}
       [:button.ui.button {:on-click #(on-click-login channel fe-state) :class (when login-failed "red")} "Login"]
       [:br]
       [:a {:href "#register"} "Register new user"]]]]))

(defmethod be-action login-action [channel message be-state]
  (let [checked-msg (validate message be-state)]
    (if (any-errors? checked-msg state-key)
      (ws-send channel checked-msg)
      (do
        (bes/set-logged-in be-state channel (get-val message username-path))
        (ws-send channel {:action login-successful :user (get-val message username-path)})))))

(defmethod be-action logout-action [channel message be-state]
  (bes/logout be-state channel)
  (ws-send channel {:action logout-successful}))

(defmethod fe-action login-action [channel message fe-state]
  (swap! fe-state assoc state-key (state-key message)))

(defmethod fe-action login-successful [channel msg fe-state]
  (fes/login fe-state (:user msg))
  (swap! fe-state dissoc state-key))

(defmethod fe-action logout-successful [channel msg fe-state]
  )
