(ns clweb.core-test
  (:require
   [clojure.test :refer :all]
   [clweb.components :refer :all]
   [clweb.core :refer :all]
   [clweb.io :refer :all]
   [clweb.components.login-form :as login-form]
   ))

(deftest login-test
  (testing "a login session"
    (let [messages (atom [])]
      (with-redefs [ws-send #(swap! messages conj %2)
                    db (atom {"testuser" 20})]
        (handle-msg nil  {:action "login" :name "unknown"})
        (is (= {:action "failed-login"}
               (last @messages)))
        (handle-msg nil {:action "login" :name "testuser"})
        (is (= {:action "your-state" :state 20}
               (last @messages)))
        (handle-msg nil {:action "logout"})
        (is (= {:action "your-state", :state nil}
               (last @messages)) "logout works")))))

(defn render [tree]
  (cond
    (fn? tree)
    (render (tree))

    (not (vector? tree))
    tree

    (fn? (first tree))
    (render (apply (first tree) (rest tree)))

    (vector? tree)
    (mapv render tree)))

(defn component-contains [component elem]
  (is (some #(= elem %)
            (flatten (render (component))))))

(deftest clear-errors-test
  (testing "Clearing of errors in form state"
    (is (= {:id {:a { :value "val"}
                 :b {:value "val"}}}
           (clear-errors {:id
                           {:a { :value "val" :error "err" }
                            :b { :value "val" :error "err b" }}}
                          :id)))))

(defn click-button [form]
  ((:on-click (first (filter #(and (map? %1) ( contains? %1 :on-click ))
                             (flatten form))))))


(deftest login-test
  (testing "logging in"
    (let [client-state (atom {login-form/state-key {:username {:value "alex"}} :other-garbage "garb"})
          form (render (login-form/form nil client-state))
          request (login-form/on-click nil client-state)
          response (be-action nil request)]
      (fe-action nil response client-state)
      (is (some #(= :div.ui.pointing.red.basic.label %)
                (flatten (render (login-form/form nil client-state))))))))
