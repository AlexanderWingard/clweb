(ns clweb.core
  (:use
   [compojure.core :only [defroutes GET POST DELETE ANY context]]
   [compojure.handler :only [site]]
   [compojure.route :only [resources files not-found]]
   [org.httpkit.server]
   [ring.middleware.cljsjs :only [wrap-cljsjs]]
   [ring.util.response :only [resource-response]])
  (:require
   [clojure.edn :as edn])
  (:gen-class))

(defonce clients (atom {}))
(def db (atom {"alex" 10 "andrej" 20}))
(defn publish-to-all [message]
  (doseq [channel (keys @clients)]
    (send! channel message)))

(add-watch clients :watcher
           (fn [key atom old-state new-state]
             (publish-to-all (prn-str {:action "full-server-state" :state {:clients @clients :db @db}}))))

(defn handle-msg [channel string]
  (let [data (edn/read-string string)]
    (case (:action data)
      "login" (if (contains? @db (:name data))
                (let [newstate (swap! clients assoc-in [channel :logged-in] (:name data))]
                  (send! channel (pr-str {:action "your-state" :state (get @db (:name data))})))
                (send! channel (pr-str {:action "failed-login"})))
      "logout" (let [newstate (swap! clients update channel dissoc :logged-in)]
                 (send! channel (pr-str {:action "your-state" :state nil}))))))

(defn ws-handler [req]
  (with-channel req channel
    (swap! clients assoc channel {})
    (on-close channel (fn [status] (swap! clients dissoc channel)))
    (on-receive channel (fn [message] (#'handle-msg channel message)))))

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
