(ns clweb.components
  (:require
   [clweb.io :refer [ws-send]]
   #?@(:cljs
       [[reagent.core :refer [atom]]
        [reagent-forms.core :refer [bind-fields]]])))

#?(:clj (defn bind-fields [form doc]
          [:binding @doc form]))

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

(defn assign-error [state path error]
  (assoc-in state (conj path :error) error))

(defmulti be-action (fn [channel message] (:action message)))
(defmulti fe-action (fn [channel message state] (:action message)))
