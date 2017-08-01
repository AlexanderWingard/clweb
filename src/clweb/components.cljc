(ns clweb.components
  #?(:clj (require [clweb.io :refer :all])
     :cljs
     (:require [clweb.io :refer [ws-send]]
               [reagent.core :refer [atom]]
               [reagent-forms.core :refer [bind-fields]])))

#?(:clj (defn bind-fields [form doc]
          [:binding @doc form]))

(defn right-menu []
  [:div.right.menu])

(defn top-menu []
  [:div.ui.menu
   [right-menu]])

(defn field [type label state path]
  (let [error (get-in @state (conj path :error))]
    [:div.field {:class (when (some? error) "error")}
     [:label label]
     [bind-fields [:input {:field type :id (conj path :value)}] state]
     (when (some? error) [:div.ui.pointing.red.basic.label error])]))

(defn clear-errors [state id]
  (update state id
   (fn [state]
     (into {} (map (fn [[k v]] [k (dissoc v :error)]) state)))))

(def registration-key :registration-form)

(defn registration-validate [state]
  (-> state
      (clear-errors registration-key)
      ((fn [state]
         (if (not (= (get-in state [registration-key :password-2 :value])
                     (get-in state [registration-key :password-1 :value])))
           (assoc-in state [registration-key :password-2 :error] "Passwords don't match") state)))
      ((fn [state]
         (if (< (count (get-in state [registration-key :password-1 :value])) 5)
           (assoc-in state [registration-key :password-1 :error] "Password too short") state)))))

(defn registration [channel state]
  [:div.ui.segment
   [:div.ui.form
    (field :text "Username" state [registration-key :username])
    [:div.two.fields
     (field :password "Password" state [registration-key :password-1])
     (field :password "Repeat" state [registration-key :password-2])]
    [:button.ui.button {:on-click #(ws-send channel (assoc @state :action "register"))} "Register"]]])
