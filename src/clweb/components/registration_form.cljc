(ns clweb.components.registration-form
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
            [clweb.state :refer [register-user set-logged-in]]))

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
  [:div.ui.form
   [:h2 "Register new user"]
   (field :text "Username" state username-path)
   [:div.two.fields
    (field :password "Password" state password-1-path)
    (field :password "Repeat" state password-2-path)]
   [:div {:style {:text-align "center"}}
    [:button.ui.button {:on-click #(on-click channel state)
                        :class (when (any-errors? @state state-key) "red")} "Register"]
    [:br]
    [:a {:href "#"} "Back to login"]]])

(defmethod be-action action [channel message state]
  (let [checked-state (validate channel message)]
    (if (any-errors? checked-state state-key)
      (ws-send channel checked-state)
      (do
        (register-user state
                       (get-val message username-path)
                       (get-val message password-1-path))
        (set-logged-in state channel (get-val message username-path))
        (ws-send channel {:action registration-successful-msg :user (get-val message username-path)})))))

(defmethod fe-action action [channel message state]
  (swap! state assoc state-key (state-key message)))

(defmethod fe-action registration-successful-msg [channel message state]
  (swap! state assoc :logged-in (:user message))
  (swap! state dissoc state-key)
  #?(:cljs (set! (.-hash (.-location js/window)) "")))
