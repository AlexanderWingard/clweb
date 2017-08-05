(ns clweb.io
  #?(:clj (require [org.httpkit.server :refer [send!]])))

(defn ws-send [channel msg]
  (if (instance? #?(:clj clojure.lang.Atom
                    :cljs cljs.core/Atom)
                 channel)
    (swap! channel conj msg)
    (when (not (nil? channel))
      #?(:clj (send! channel (pr-str msg))
         :cljs (.send channel (pr-str msg)))))
  msg)

(defn publish-to-all [channels message]
  (doseq [channel channels]
    (ws-send channel message)))
