(ns rooms.demo
  (:require [rooms.core :refer [create-room!]]
            [rooms.server :refer [start-server]]))

(def stopper (atom nil))

(defn start [& opts]
  (let [realopts (merge {:port 8080 :test true} (or (first opts) {}))]
    (do (reset! stopper (start-server realopts))
        (create-room! "demo"
          (fn [s m] (assoc s :messages (conj (or (:messages s) []) m)))
          (fn [s uid] s))
        (println "Started demo server on port " (:port realopts)))))

(defmacro reload-with [opts]
  `(do (@stopper)
       (println "Stopped server")
       (use 'rooms.core :reload-all)
       (start ~opts)))

(defmacro reload []
  `(do (@stopper)
       (println "Stopped server")
       (use 'rooms.core :reload-all)
       (start)))