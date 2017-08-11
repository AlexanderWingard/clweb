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

(defn any-errors? [state id]
  (some (fn [[k v]] (contains? v :error)) (get state id)))

(defn assoc-val [m ks v]
  (assoc-in m (conj ks :value) v))

(defn get-val [m ks]
  (get-in m (conj ks :value)))

(defn assign-error [state path error]
  (assoc-in state (conj path :error) error))

(defmulti be-action (fn [channel message be-state] (:action message)))
(defmulti fe-action (fn [channel message fe-state] (:action message)))
