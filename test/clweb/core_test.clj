(ns clweb.core-test
  (:require
   [clojure.test :refer :all]
   [clweb.components :refer :all]
   [clweb.core :refer :all]
   ))

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

(deftest component-test
  (testing "the menu"
    (component-contains top-menu :div.ui.menu))
  (testing "hello world"
    (component-contains hello-world {:field :text, :id :hello})))
