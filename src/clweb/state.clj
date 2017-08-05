(ns clweb.state
  (:require [clweb.io :refer [publish-to-all]]
            [clweb.components.state-debug :as state-debug]))

(defn channels [state]
  (keys (:sessions @state)))

(defn assoc-channel [state channel]
  (swap! state assoc-in [:sessions channel] {}))

(defn dissoc-channel [state channel]
  (swap! state update-in [:sessions] dissoc channel))

(defn new []
  (let [state (atom
               {:sessions {}
                :db {}})]
    (add-watch state :state-debug
               (fn [key atom old-state new-state]
                 (publish-to-all (channels atom) (state-debug/create-message new-state))))
    state))
