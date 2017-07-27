(ns clweb.core-test
  (:require [clojure.test :refer :all]
            [clweb.core :refer :all]
            [org.httpkit.server :as httpkit]))

(deftest a-test
  (testing "FXME, I fail."
    (is (= nil
           (with-redefs [httpkit/send! (fn [_ message] message)]
             (handle-msg nil (prn-str {:action "logout"})))))))
