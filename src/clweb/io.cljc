(ns clweb.io
  #?(:clj (require [org.httpkit.server :refer [send!]])))

(defn ws-send [channel msg]
  (when (not (nil? channel))
    #?(:clj (send! channel (pr-str msg))
       :cljs (.send channel (pr-str msg))))
  msg)
