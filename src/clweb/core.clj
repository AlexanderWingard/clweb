(ns clweb.core
  (:gen-class)
  (:require [clojure.edn :as edn]
            [clweb.components :as component]
            [clweb.state :as state]
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

(defonce state (state/new))

(defn ws-handler [req]
  (with-channel req channel
    (state/assoc-channel state channel)
    (on-close channel (fn [status] (state/dissoc-channel state channel)))
    (on-receive channel (fn [string] (component/be-action channel (edn/read-string string) state)))))

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
