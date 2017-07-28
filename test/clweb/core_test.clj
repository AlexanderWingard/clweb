(ns clweb.core-test
  (:require
   [clojure.test :refer :all]
   [clweb.core :refer :all]))

(deftest login-test
  (testing "a login session"
    (let [messages (atom [])]
      (with-redefs [ws-send #(swap! messages conj %2)
                    db (atom {"testuser" 20})]
        (handle-msg nil (prn-str {:action "login" :name "unknown"}))
        (is (= {:action "failed-login"}
               (last @messages)))
        (handle-msg nil (prn-str {:action "login" :name "testuser"}))
        (is (= {:action "your-state" :state 20}
               (last @messages)))
        (handle-msg nil (prn-str {:action "logout"}))
        (is (= {:action "your-state", :state nil}
               (last @messages)) "logout works")))))
