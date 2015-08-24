(ns dunnit-client.prod
  (:require [dunnit-client.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
