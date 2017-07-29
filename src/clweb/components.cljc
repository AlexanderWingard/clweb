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
