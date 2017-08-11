(ns clweb.core
  (:gen-class)
  (:require [clojure.edn :as edn]
            [clweb.components :as component]
            [clweb.be-state :as bes]
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :refer [site]]
            [compojure.route :refer [not-found resources]]
            [clweb.components.login-form]
            [clweb.components.registration-form]
            [clweb.components.state-debug]
            [org.httpkit.server
             :refer
             [on-close on-receive run-server with-channel]]
            [ring.middleware.cljsjs :refer [wrap-cljsjs]]
            [ring.util.response :refer [resource-response]]))

(defonce be-state (bes/new))

(defn ws-handler [req]
  (with-channel req channel
    (bes/assoc-channel be-state channel)
    (on-close channel (fn [status] (bes/dissoc-channel be-state channel)))
    (on-receive channel (fn [string] (component/be-action channel (edn/read-string string) be-state)))))

(defroutes my-routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/ws" [] ws-handler)
  (wrap-cljsjs (resources "/"))
  (not-found "Page not found"))

(def ring-handler (site #'my-routes))

(defn -main
  [& args]
  (run-server ring-handler {:port 8080})
  (println "http://localhost:8080"))
