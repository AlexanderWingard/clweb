(ns clweb.fe-state
  #?(:cljs (:require [reagent.core :as reagent :refer [atom]])))

(defn login [state user]
  (swap! state assoc :logged-in user))

(defn logout [state]
  (swap! state dissoc :logged-in))

(defn logged-in? [state]
  (get @state :logged-in false))

(defn location
  ([state] (:location @state))
  ([state value] (swap! state assoc :location value)))

(defn location-is [state loc]
  (= loc (location state)))

(defn new []
  (atom {}))
