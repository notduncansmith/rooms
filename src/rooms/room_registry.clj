(ns rooms.room-registry
  (:require [rooms.room :as room]))

(defrecord RoomRegistry [rooms max-rooms])

(defn empty-room-registry
  ([max-rooms] (RoomRegistry. {} max-rooms))
  ([] (RoomRegistry. {} 200)))

(defn rooms-above-max? [reg] (-> reg (:rooms) (count) (> (:max-rooms reg))))

(defn create-room
  [reg room-id sf vf]
  (if (rooms-above-max? reg)
      false
      (assoc-in reg [:rooms room-id] (room/empty-room room-id sf vf))))

(defn remove-room [reg room-id] (update reg :rooms dissoc room-id))