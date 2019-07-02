(ns rooms.server
  (:use [compojure.route :only [files not-found]]
        [compojure.core :only [defroutes GET]]
        rooms.core
        org.httpkit.server)
  (:require [msgpack.core :refer [pack unpack]]
            msgpack.clojure-extensions))
(defn connect
  [registry room-id user request]
  (let [_ (println room-id)
        deliver! (partial send-msg! room-id (:id user))
        subscription-id (str (System/currentTimeMillis) (:id user))]
    (with-channel request channel
      (println "Registry state" registry)
      (println "Connection from user" user)
      (println "Joining room" (get-room! room-id))
      (add-user! room-id user)
      (watch-room! room-id subscription-id
        #(do (send! channel (pack {"state" (clojure.walk/stringify-keys %)}))
             (println "Sending state: " (clojure.walk/stringify-keys %))))
      (on-close channel
        (fn [status]
          (do (println "channel closed: " status)
              (unwatch-room! room-id subscription-id)
              (remove-user! room-id (:id user)))))
      (on-receive channel #(do (println "Received message: " (unpack %))
                               (deliver! (unpack %))))
      (send! channel (pack {"greeting" "Hello"})))))

(defn- get-ip
  [req]
  (or (get-in req [:headers "x-forwarded-for"])
      (:remote-addr req)))

(defroutes all-routes
  (GET "/room/:id" [id :as req]
    (let [user {:id (str (System/currentTimeMillis) "-" (get-ip req)) :joined-at (System/currentTimeMillis)}]
      (connect default-registry id user req)))
  (not-found "<p>Page not found.</p>"))

; (defn create-demo-room! []
(create-room! "demo"
  (fn [s m] (assoc s :messages (conj (or (:messages s) []) m)))
  identity)

(defn start-server [opts] (run-server all-routes opts))