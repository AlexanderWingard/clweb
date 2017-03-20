(ns clweb.core
  (:use [org.httpkit.server]
        [ring.util.response :only [resource-response]]
        [compojure.route :only [resources files not-found]]
        [compojure.handler :only [site]]
        [compojure.core :only [defroutes GET POST DELETE ANY context]])
  (:gen-class))

(defn ws-handler [req]
  (with-channel req channel
    (on-close channel (fn [status] (println "channel closed")))
    (on-receive channel (fn [data] (send! channel data)))))

(defroutes routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (resources "/")
  (not-found "Page not found"))

(defn -main
  [& args]
  (run-server (site #'routes) {:port 8080})
  (println "http://localhost:8080"))
