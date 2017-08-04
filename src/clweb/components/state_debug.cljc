(ns clweb.components.state-debug
  (:require
   [clweb.io :refer [ws-send]]
   #?(:cljs [reagent.core :as reagent :refer [atom]])
   #?(:cljs [json-html.core :refer [edn->hiccup]])
   [clojure.walk :refer [prewalk-replace]]
   [clweb.components :refer [fe-action
                             be-action
                             assign-error
                             field
                             clear-errors]]))
(defonce full-server-state (atom {}))
(def action "full-server-state")

(defn render-clojure [atom]
  (prewalk-replace
   {:table.jh-type-object :table.jh-type-object.ui.celled.table}
   #?(:cljs (edn->hiccup @atom))))

(defn form [client-state]
  [:div
   [:h1.ui.header "Client state"]
   (render-clojure client-state)
   [:h1.ui.header "Full Server state"]
   (render-clojure full-server-state)])

(defmethod fe-action action [channel message state]
  (reset! full-server-state (:state message)))
