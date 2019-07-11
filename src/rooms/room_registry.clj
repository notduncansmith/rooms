(ns rooms.room-registry
  (:require [rooms.room :as room]))

(defrecord RoomRegistry [rooms max-rooms])

(defn empty-room-registry
  ([max-rooms] (RoomRegistry. {} max-rooms))
  ([] (RoomRegistry. {} 200)))

(defn rooms-above-max?
  [reg]
  (-> reg (:rooms) (count) (> (:max-rooms reg))))

(defn create-room
  [reg room-id sf vf]
  (if (rooms-above-max? reg)
      false
      (assoc-in reg [:rooms room-id] (room/empty-room room-id sf vf))))

(defn get-room [reg room-id] (get-in reg [:rooms room-id]))
(defn remove-room [reg room-id] (update reg :rooms dissoc room-id))

(defn send-user-msg
  [reg room-id user-id msg]
  (room/send-user-msg (get-room reg room-id) user-id msg))

(defn send-raw-msg
  [reg room-id msg]
  (room/send-raw-msg (get-room reg room-id) msg))

(defn get-user
  [reg room-id user-id]
  (room/get-user (get-room reg room-id)) user-id)

(defn watch-room
  [reg room-id user-id cb]
  (watch-room (get-room reg room-id) user-id cb))

(defn unwatch-room
  [reg room-id user-id]
  (unwatch-room (get-room reg room-id) user-id))
