(ns clweb.core
  (:use
   [compojure.core :only [defroutes GET POST DELETE ANY context]]
   [compojure.handler :only [site]]
   [compojure.route :only [resources files not-found]]
   [org.httpkit.server]
   [ring.middleware.cljsjs :only [wrap-cljsjs]]
   [ring.util.response :only [resource-response]]
   )
  (:require
   [clojure.edn :as edn])
  (:gen-class))

(defonce clients (atom {}))
(defn gen-uuid [] (str (java.util.UUID/randomUUID)))

(defn ws-handler [req]
  (with-channel req channel
    (let [uuid (gen-uuid)]
      (add-watch clients uuid
                 (fn [key atom old-state new-state]
                   (send! channel (prn-str {:action "server-state" :state new-state}))))
      (swap! clients assoc uuid {:public {:uuid uuid} :private {:channel channel}})
      (on-close channel (fn [status] (swap! clients dissoc uuid)))
      (on-receive channel (fn [string] (let [data (edn/read-string string)]
                                         (case (:action data)
                                           "get-uuid" (send! channel (prn-str (merge data (get-in @clients [uuid :public])))))))))))

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
