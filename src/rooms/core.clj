(ns rooms.core
  (:require [rooms.room-registry :refer [empty-room-registry
                                         create-room
                                         get-room
                                         remove-room
                                         send-user-msg
                                         send-raw-msg
                                         get-user]]
            [rooms.room :refer [watch-room
                                unwatch-room
                                add-users
                                remove-users]]))

(def default-registry (atom (empty-room-registry)))

(defn create-room! ([id sf vf] (swap! default-registry create-room id sf vf))
                   ([sf vf] (swap! default-registry create-room sf vf)))

(defn get-room! [room-id] (get-room @default-registry room-id))
(defn remove-room! [room-id] (swap! default-registry remove-room room-id))

(defn get-user! [room-id user-id] (get-user @default-registry room-id user-id))

(defn send-raw-msg! [room-id msg] (send-raw-msg @default-registry room-id msg))
(defn send-user-msg! [room-id user-id msg] (send-user-msg @default-registry room-id user-id msg))

(defn watch-room! [room-id user-id cb] (watch-room (get-room! room-id) user-id cb))
(defn unwatch-room! [room-id user-id] (unwatch-room (get-room! room-id) user-id))

(defn add-user! [room-id user] (add-users (get-room! room-id) [user]))
(defn remove-user! [room-id user-id] (remove-users (get-room! room-id) [user-id]))