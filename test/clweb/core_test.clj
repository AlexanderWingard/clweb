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
      (is (not (any-errors? (clear-errors state :id) :id))))))

(deftest ws-send-test
  (testing "Checking sent msgs"
    (let [msgs (atom [])]
      (ws-send msgs "test1")
      (ws-send msgs "test2")
      (is (= ["test1" "test2"] @msgs)))))

(defn test-interaction [action fe-state be-state]
  (fe-action nil (be-action nil (action nil fe-state) be-state) fe-state))

(deftest login-test
  (testing "new logintest"
    (let [fe-state (-> {}
                       (assoc-val login-form/username-path "alexander")
                       (assoc-val login-form/password-path "password")
                       (atom))
          be-state (->  (state/new)
                        (state/register-user "alexander" "password"))]
      (test-interaction login-form/on-click-login fe-state be-state)
      (is (nil? (login-form/state-key @fe-state)) "Login form is cleared")
      (is (= "alexander" (state/logged-in-user be-state nil)))
      (test-interaction  login-form/on-click-logout fe-state be-state)
      (is (= nil (login-form/logged-in-key @fe-state)))
      (is (= nil (state/logged-in-user be-state nil))))))

(deftest registration-test
  (testing "successful registration"
    (let [be-state (atom {})
          fe-state (-> {}
                           (assoc-val registration-form/username-path "alex")
                           (assoc-val registration-form/password-1-path "password")
                           (assoc-val registration-form/password-2-path "password")
                           (atom))]
      (test-interaction registration-form/on-click fe-state be-state)
      (is (not (contains? @fe-state registration-form/state-key)))
      (is (= "alex" (state/logged-in-user be-state nil))))))

