(ns rooms.core
  (:require [rooms.room-registry :refer [empty-room-registry
                                         create-room
                                         remove-room]]
            [rooms.room :refer [send-user-msg
                                send-raw-msg
                                watch-room
                                unwatch-room
                                add-users
                                remove-users]]))

(def default-registry (atom (empty-room-registry)))

(defn create-room! ([id sf vf] (swap! default-registry create-room id sf vf))
                   ([sf vf] (swap! default-registry create-room sf vf)))

(defn get-room! [room-id] (get-in @default-registry [:rooms room-id]))
(defn update-room! [room-id f] (swap! default-registry update-in [:rooms room-id] f))
(defn get-user! [room-id user-id] (get-in @(:agent (get-room! room-id)) [:users user-id]))

(defn remove-room! [room-id] (swap! default-registry remove-room room-id))
(defn send-raw-msg! [room-id msg] (send-raw-msg (get-room! room-id) msg))
(defn send-user-msg! [room-id user-id msg] (send-user-msg (get-room! room-id) user-id msg))
(defn watch-room! [room-id user-id cb] (watch-room (get-room! room-id) user-id cb))
(defn unwatch-room! [room-id user-id] (unwatch-room (get-room! room-id) user-id))
(defn add-user! [room-id user] (update-room! room-id #(add-users % [user])))
(defn remove-user! [room-id user-id] (update-room! room-id #(remove-users % [user-id])))