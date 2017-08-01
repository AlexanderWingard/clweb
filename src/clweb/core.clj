(ns clweb.core
  (:use
   [compojure.core :only [defroutes GET POST DELETE ANY context]]
   [compojure.handler :only [site]]
   [compojure.route :only [resources files not-found]]
   [org.httpkit.server]
   [ring.middleware.cljsjs :only [wrap-cljsjs]]
   [ring.util.response :only [resource-response]])
  (:require
   [clojure.edn :as edn]
   [clweb.io :refer :all]
   [clweb.components :as component])
  (:gen-class))

(defonce clients (atom {}))
(def db (atom {"alex" 10 "andrej" 20}))

(defn publish-to-all [message]
  (doseq [channel (keys @clients)]
    (ws-send channel message)))

(add-watch clients :watcher
           (fn [key atom old-state new-state]
             (publish-to-all {:action "full-server-state" :state {:clients @clients :db @db}})))

(defn handle-msg [channel data]
  (case (:action data)

    "login" (if (contains? @db (:name data))
              (let [newstate (swap! clients assoc-in [channel :logged-in] (:name data))]
                (ws-send channel {:action "your-state" :state (get @db (:name data))}))
              (ws-send channel {:action "failed-login"}))

    "logout" (let [newstate (swap! clients update channel dissoc :logged-in)]
               (ws-send channel {:action "your-state" :state nil}))

    "register" (ws-send channel (component/registration-validate data))))

(defn on-msg [channel string]
  (handle-msg channel (edn/read-string string)))

(defn ws-handler [req]
  (with-channel req channel
    (swap! clients assoc channel {})
    (on-close channel (fn [status] (swap! clients dissoc channel)))
    (on-receive channel (fn [message] (#'on-msg channel message)))))

(defroutes routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/ws" [] ws-handler)
  (wrap-cljsjs (resources "/"))
  (not-found "Page not found"))

(def ring-handler (site #'routes))

(defn -main
  [& args]
  (run-server ring-handler {:port 8080})
  (println "http://localhost:8080"))
