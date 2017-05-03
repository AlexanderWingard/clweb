(ns clweb.core-test
  (:require [clweb.core :as sut]
            [cljs.test :refer-macros [deftest is testing run-tests]]))

(deftest test-the-truth
    (is (= true (sut/the-truth))))
(enable-console-print!)
(cljs.test/run-tests)
