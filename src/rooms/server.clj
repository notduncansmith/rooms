(ns rooms.server
  (:require [org.httpkit.server :refer [run-server on-receive send with-channel websocket?]]))

(defn async-handler [ring-request]
  (with-channel ring-request channel
    (if (websocket? channel)     
      (on-receive channel 
        (fn [data]
          (send! channel data)))
      (send! channel {:status 404 :body "Not found"}))))
