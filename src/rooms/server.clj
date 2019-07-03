(ns rooms.server
  (:use [compojure.route :only [files not-found]]
        [compojure.core :only [defroutes GET]]
        rooms.core
        org.httpkit.server)
  (:require [msgpack.core :as mp]
            msgpack.clojure-extensions
            [cheshire.core :as chjson]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))
(defn connect
  [registry room-id user request encoding]
  (let [_ (println room-id)
        pack (if (= encoding "msgpack") mp/pack chjson/generate-string)
        unpack (if (= encoding "msgpack") mp/unpack chjson/parse-string)
        deliver! (partial send-msg! room-id (:id user))
        subscription-id (str (System/currentTimeMillis) (:id user))]
    (with-channel request channel
      (println "Registry state" registry)
      (println "Connection from user" user)
      (println "Joining room" (get-room! room-id))
      (add-user! room-id user)
      (watch-room! room-id subscription-id
        #(let [state (clojure.walk/stringify-keys %)
               _ (println "Sending state: " state)]
            (send! channel (pack {"state" state}))))

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
  (GET "/room/:id" [id encoding :as req]
    (let [user {:id (str (System/currentTimeMillis) "-" (get-ip req)) :joined-at (System/currentTimeMillis)}
          _ (println "ID " id "ENCODING " encoding)]
      (connect default-registry id user req encoding)))
  (not-found "<p>Page not found.</p>"))

(defn start-server [opts]
  (run-server (wrap-defaults all-routes api-defaults) opts))
