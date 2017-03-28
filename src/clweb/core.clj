(ns clweb.core
  (:use [org.httpkit.server]
        [ring.util.response :only [resource-response]]
        [compojure.route :only [resources files not-found]]
        [compojure.handler :only [site]]
        [compojure.core :only [defroutes GET POST DELETE ANY context]]
        )
  (:require [clweb.types])
  (:import [clweb.types Greeting])
  (:gen-class))


(def greet (Greeting. "Hello World!"))

(defn ws-handler [req]
  (with-channel req channel
    (on-close channel (fn [status] (println "channel closed")))
    (on-receive channel (fn [data] (send! channel (pr-str greet))))))

(defroutes routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/ws" [] ws-handler)
  (resources "/")
  (not-found "Page not found"))

(def ring-handler (site #'routes))

(defn -main
  [& args]
  (run-server ring-handler {:port 8080})
  (println "http://localhost:8080"))
