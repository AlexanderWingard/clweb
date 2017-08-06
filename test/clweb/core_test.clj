(ns clweb.core-test
  (:require
   [clojure.test :refer :all]
   [clweb.components :refer :all]
   [clweb.core :refer :all]
   [clweb.io :refer :all]
   [clweb.state :as state]
   [clweb.components.registration-form :as registration-form]
   [clweb.components.login-form :as login-form]
   [clweb.state :as state]))

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

(deftest error-handling-test
  (testing "Clearing of errors in form state"
    (is (= {:id {:a {:value "val"}
                 :b {:value "val"}}}
           (clear-errors {:id
                          {:a {:value "val" :error "err"}
                           :b {:value "val" :error "err b"}}}
                         :id))))

  (testing "Finding out if there are any errors"
    (let [state {:id
                 {:a {:value "val" :error "err"}
                  :b {:value "val" :error "err b"}}}]
      (is (any-errors? state :id))
      (is (not (any-errors? (clear-errors state :id) :id)))))

  (deftest login-test
    (testing "new logintest"
      (let [fe-state (-> {}
                         (assoc-val login-form/username-path "alexander")
                         (assoc-val login-form/password-path "password")
                         (atom))
            be-state (state/new)
            _ (state/register-user be-state "alexander" "password")
            request (login-form/on-click nil fe-state)
            response (be-action nil request be-state)]
        (is (= login-form/login-successful (:action response)))))))

(deftest registration-test
  (testing "successful registration"
    (let [client-state (-> {}
                           (assoc-val registration-form/username-path "alex")
                           (assoc-val registration-form/password-1-path "password")
                           (assoc-val registration-form/password-2-path "password")
                           (atom))
          request (registration-form/on-click nil client-state)
          response (be-action nil request (atom {}))]
      (fe-action nil response client-state)
      (is (not (contains? @client-state registration-form/state-key))))))

(deftest ws-send-test
  (testing "Checking sent msgs"
    (let [msgs (atom [])]
      (ws-send msgs "test1")
      (ws-send msgs "test2")
      (is (= ["test1" "test2"] @msgs)))))
