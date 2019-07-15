(ns rooms.core
  (:require [rooms.room :as room]
            [msgpack.core :as mp]
            msgpack.clojure-extensions
            [cheshire.core :as chjson]
            [org.httpkit.server :as httpkit]))

(defrecord RoomRegistry [rooms max-rooms])

(defn empty-room-registry
  "Create an empty room registry with a given limit"
  [max-rooms] (RoomRegistry. {} max-rooms))

(defn rooms-at-max?
  "Return whether a registry is equal its max room count"
  [reg] (-> reg (:rooms) (count) (= (:max-rooms reg))))

(defn create-room
  "Create a room in a registry given an id, state function, and view function"
  [reg room-id sf vf]
  (if (rooms-at-max? reg)
      false
      (assoc-in reg [:rooms room-id] (room/empty-room room-id sf vf))))

(defn get-room
  "Get a room from a registry by id"
  [reg room-id] (get-in reg [:rooms room-id]))

(defn remove-room
  "Remove a room from a registry with a given id"
  [reg room-id] (update reg :rooms dissoc room-id))

(defn get-user
  "Get a user by id in a room in a registry"
  [reg room-id user-id] (room/get-user (get-room reg room-id) user-id))

(defn add-users
  "Add users to a room in a registry"
  [reg room-id users] (room/add-users (get-room reg room-id) users))

(defn remove-users
  "Remove users from a room in a registry"
  [reg room-id users] (room/remove-users (get-room reg room-id) users))

(defn send-raw-msg
  "Send an anonymous message to a room"
  [reg room-id msg] (room/send-raw-msg (get-room reg room-id) msg))

(defn send-user-msg
  "Send a message from a user to a room"
  [reg room-id user-id msg] (room/send-user-msg (get-room reg room-id) user-id msg))

(defn watch-room
  "Add a watch to the state agent for a room given its ID and a watcher key (see https://clojuredocs.org/clojure.core/add-watch)"
  [reg room-id key cb] (room/watch-room (get-room reg room-id) key cb))

(defn unwatch-room
  "Remove a watch with a given key from a room's state agent (see https://clojuredocs.org/clojure.core/remove-watch)"
  [reg room-id key] (room/unwatch-room (get-room reg room-id) key))

(defn create-room!
  "Create a room in a registry atom"
  [reg-atom id sf vf] (swap! reg-atom create-room id sf vf))

(defn remove-room!
  "Remove a room in a registry atom"
  [reg-atom room-id] (swap! reg-atom remove-room room-id))

(defn get-room!
  "Get a room in a registry atom"
  [reg-atom room-id] (get-room @reg-atom room-id))

(defn get-user!
  "Get a user in a room in a registry atom"
  [reg-atom room-id user-id] (get-user @reg-atom room-id user-id))

(defn send-raw-msg!
  "Send an anonymous message to a room in a registry atom"
  [reg-atom room-id msg] (send-raw-msg @reg-atom room-id msg))

(defn send-user-msg!
  "Send a message from a user to a room in a registry atom"
  [reg-atom room-id user-id msg] (send-user-msg @reg-atom room-id user-id msg))

(defn watch-room!
  "Add a watch to a room in a registry atom"
  [reg-atom room-id key cb] (watch-room @reg-atom room-id key cb))

(defn unwatch-room!
  "Remove a watch from a room in a registry atom"
  [reg-atom room-id key] (unwatch-room @reg-atom room-id key))

(defn add-users!
  "Add users to a room in a registry atom"
  [reg-atom room-id users] (add-users @reg-atom room-id users))

(defn remove-users!
  "Remove users from a room in a registry atom"
  [reg-atom room-id user-ids] (remove-users @reg-atom room-id user-ids))

(defn connect!
  "Add a user to a room in a registry and establish a WebSocket channel via HttpKit (https://www.http-kit.org) through which messages are received from the user and room states (filtered by their view-fn) are sent to the user"
  [registry room-id user request encoding]
  (let [user-id (:id user)
        pack (if (= encoding "msgpack") mp/pack chjson/generate-string)
        unpack (if (= encoding "msgpack") mp/unpack chjson/parse-string)
        deliver! #(send-user-msg @registry room-id (:id user) %)
        subscription-id (str (System/currentTimeMillis) (:id user))]
    (httpkit/with-channel request channel
      (add-users! registry room-id [user])

      (watch-room! registry room-id subscription-id
        #(let [room (get-room @registry room-id)
               view-fn (:view-fn room)
               current-user (or (get-in room [:users user-id]) user)
               visible-state (clojure.walk/stringify-keys (view-fn % current-user))]
            (httpkit/send! channel (pack {"state" visible-state}))))

      (httpkit/on-close channel
        (fn [status]
          (do (println "Disconnected " user-id status)
              (unwatch-room! registry room-id subscription-id)
              (remove-users! registry room-id [user-id]))))

      (httpkit/on-receive channel (comp deliver! unpack)))))
