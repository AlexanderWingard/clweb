(ns clweb.components
  #?(:cljs
     (:require [reagent.core :refer [atom]]
               [reagent-forms.core :refer [bind-fields]])))

#?(:clj (defn bind-fields [form doc]
          [:binding @doc form]))

(defn right-menu []
  [:div.right.menu])

(defn top-menu []
  [:div.ui.menu
   [right-menu]])

(defn hello-world []
  (let [state (atom {:hello "world"})]
    (fn []
      [:div
       [bind-fields  [:input {:field :text :id :hello}] state]
       [:div (:hello @state)]])))
(defn field [type label state path]
  (let [error (get-in @state (conj path :error))]
    [:div.field {:class (when (some? error) "error")}
     [:label label]
     [bind-fields [:input {:field type :id (conj path :value)}] state]
     (when (some? error) [:div.ui.pointing.red.basic.label error])]))

(defn clear-errors [state id]
  (update
   state
   id
   (fn [state]
     (into {} (map (fn [[k v]] [k (dissoc v :error)]) state)))))

(defn registration-validate [state id]
  (-> state
      (clear-errors id)
      ((fn [state]
         (if (not (= (get-in state [id :password-2 :value])
                     (get-in state [id :password-1 :value])))
           (assoc-in state [id :password-2 :error] "Passwords don't match") state)))
      ((fn [state]
         (if (< (count (get-in state [id :password-1 :value])) 5)
           (assoc-in state [id :password-1 :error] "Password too short") state)))))

(defn registration [id state]
  [:div.ui.segment
   [:div.ui.form
    (field :text "Username" state [id :username])
    [:div.two.fields
     (field :password "Password" state [id :password-1])
     (field :password "Repeat" state [id :password-2])]
    [:button.ui.button {:on-click #(swap! state registration-validate id)
                        } "Register"]]])
