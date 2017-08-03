(ns clweb.core-test
  (:require
   [clojure.test :refer :all]
   [clweb.components :refer :all]
   [clweb.core :refer :all]
   [clweb.io :refer :all]
   [clweb.components.login-form :as login-form]
   ))

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
    (let [db (atom {"alex" 10})
          client-state (atom {login-form/state-key {:username {:value "ale"}} :other-garbage "garb"})
          request (login-form/on-click nil client-state)
          response (be-action nil request db)]
      (fe-action nil response client-state)
      (is (some #(= :div.ui.pointing.red.basic.label %)
                (flatten (render (login-form/form nil client-state))))))))

(deftest ws-send-test
  (testing "Checking sent msgs"
    (let [msgs (atom [])]
      (ws-send msgs "test1")
      (ws-send msgs "test2")
      (is (= ["test1" "test2"] @msgs)))))
