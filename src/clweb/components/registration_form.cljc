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
            [clweb.be-state :as bes]))

(def action "register")
(def registration-successful-msg "registration-successful-msg")
(def state-key :registration-form)
(def username-path [state-key :username])
(def password-1-path [state-key :password-1])
(def password-2-path [state-key :password-2])

(defn validate [channel be-state]
  (-> be-state
      (clear-errors state-key)
      ((fn [be-state]
         (if (< (count (get-val be-state username-path)) 3)
           (assign-error be-state username-path "Username too short")
           be-state)))
      ((fn [be-state]
         (if (< (count (get-val be-state password-1-path)) 5)
           (assign-error be-state password-1-path "Password too short")
           be-state)))
      ((fn [be-state]
         (if (not (= (str (get-val be-state password-1-path))
                     (str (get-val be-state password-2-path))))
           (assign-error be-state password-2-path "Passwords don't match")
           be-state)))))

(defn on-click [channel fe-state]
  (ws-send channel (assoc @fe-state :action action)))

(defn form [channel fe-state]
  [:div.ui.form
   [:h2 "Register new user"]
   (field :text "Username" fe-state username-path)
   [:div.two.fields
    (field :password "Password" fe-state password-1-path)
    (field :password "Repeat" fe-state password-2-path)]
   [:div {:style {:text-align "center"}}
    [:button.ui.button {:on-click #(on-click channel fe-state)
                        :class (when (any-errors? @fe-state state-key) "red")} "Register"]
    [:br]
    [:a {:href "#"} "Back to login"]]])

(defmethod be-action action [channel message be-state]
  (let [checked-state (validate channel message)]
    (if (any-errors? checked-state state-key)
      (ws-send channel checked-state)
      (do
        (bes/register-user be-state
                       (get-val message username-path)
                       (get-val message password-1-path))
        (bes/set-logged-in be-state channel (get-val message username-path))
        (ws-send channel {:action registration-successful-msg :user (get-val message username-path)})))))

(defmethod fe-action action [channel message fe-state]
  (swap! fe-state assoc state-key (state-key message)))

(defmethod fe-action registration-successful-msg [channel message fe-state]
  (swap! fe-state assoc :logged-in (:user message))
  (swap! fe-state dissoc state-key)
  #?(:cljs (set! (.-hash (.-location js/window)) "")))
