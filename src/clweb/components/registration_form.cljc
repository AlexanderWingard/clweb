(ns clweb.components.registration-form
  (:require [clweb.io :refer [ws-send]]
            [clweb.components :refer [fe-action
                                      be-action
                                      assign-error
                                      any-errors?
                                      get-val
                                      field
                                      clear-errors]]))

(def action "register")
(def registration-successful-msg "registration-successful-msg")
(def state-key :registration-form)
(def username-path [state-key :username])
(def password-1-path [state-key :password-1])
(def password-2-path [state-key :password-2])

(defn validate [channel state]
  (-> state
      (clear-errors state-key)
      ((fn [state]
         (if (< (count (get-val state username-path)) 3)
           (assign-error state username-path "Username too short")
           state)))
      ((fn [state]
         (if (< (count (get-val state password-1-path)) 5)
           (assign-error state password-1-path "Password too short")
           state)))
      ((fn [state]
         (if (not (= (str (get-val state password-1-path))
                     (str (get-val state password-2-path))))
           (assign-error state password-2-path "Passwords don't match")
           state)))))

(defn on-click [channel state]
  (ws-send channel (assoc @state :action action)))

(defn form [channel state]
  [:div.ui.segment
   [:div.ui.form
    (field :text "Username" state username-path)
    [:div.two.fields
     (field :password "Password" state password-1-path)
     (field :password "Repeat" state password-2-path)]
    [:button.ui.button {:on-click #(on-click channel state)
                        :class (when (any-errors? @state state-key) "red")} "Register"]]])

(defmethod be-action action [channel message db]
  (let [checked-state (validate channel message)]
    (if (any-errors? checked-state state-key)
      (ws-send channel checked-state)
      (ws-send channel {:action registration-successful-msg}))))

(defmethod fe-action action [channel message state]
  (swap! state assoc state-key (state-key message)))

(defmethod fe-action registration-successful-msg [channel message state]
  (swap! state dissoc state-key))
