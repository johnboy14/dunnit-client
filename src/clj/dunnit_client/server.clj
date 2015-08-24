(ns dunnit-client.server
  (:use [org.httpkit.server :only [run-server]])
  (:require [dunnit-client.handler :refer [app]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

 (defn -main [& args]
   (let [port (Integer/parseInt (or (env :port) "3000"))]
     ;(run-jetty app {:port port :join? false})
     (run-server app {:port port})))
