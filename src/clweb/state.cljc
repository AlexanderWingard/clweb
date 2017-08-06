(ns clweb.state
  (:require [clweb.io :refer [publish-to-all]]
            [clweb.components.state-debug :as state-debug]))

(defn channels [state]
  (keys (:sessions @state)))

(defn assoc-channel [state channel]
  (swap! state assoc-in [:sessions channel] {}))

(defn dissoc-channel [state channel]
  (swap! state update-in [:sessions] dissoc channel))

(defn register-user [state username password]
  (swap! state assoc-in [:db username :password] password)
  state)

(defn user-exists? [state username]
  (not (nil? (get-in @state [:db username]))))

(defn correct-password? [state username password]
  (= password (get-in @state [:db username :password])))

(defn set-logged-in [state channel username]
  (swap! state assoc-in [:sessions channel :logged-in] username))

(defn new []
  (let [state (atom
               {:sessions {}
                :db {}})]
    (add-watch state :state-debug
               (fn [key atom old-state new-state]
                 (publish-to-all (channels atom) (state-debug/create-message new-state))))
    state))
