(ns rooms.room-registry
  (:require [clj-ulid :as ulid]
            [rooms.room :as room]))

(defrecord RoomRegistry [rooms max-rooms lobby-min lobby-max lobby lobby-timeout])

(defn empty-room-registry
  ([max-rooms lobby-min lobby-max lobby-timeout]
   (RoomRegistry. {} max-rooms lobby-min lobby-max {} lobby-timeout))
  ([]
   (RoomRegistry. {} 200 5 10 {} 5)))

(defn lobby-count [reg] (count (:lobby reg)))
(defn rooms-above-max? [reg] (-> reg (:rooms) (count) (> (:max-rooms reg))))
(defn lobby-above-max? [reg] (> (lobby-count reg) (:max-users reg)))
(defn lobby-below-min? [reg] (< (lobby-count reg) (:min-users reg)))

(defn create-room
  ([reg room-id sf vf] (if (rooms-above-max? reg) false (assoc-in reg [:rooms room-id] (room/empty-room room-id sf vf))))
  ([reg sf vf] (create-room reg (ulid/ulid) sf vf)))

(defn remove-room [reg room-id] (update reg :rooms dissoc room-id))

(defn add-lobby-user
  [reg user]
  (if (lobby-above-max? reg)
      false
      (assoc-in reg [:lobby (:id user)] user)))

(defn remove-lobby-user [reg user-id] (update reg :lobby dissoc user-id))

(defn shift-lobby
  "Move all the users in the lobby to a room"
  [reg room-id]
  (if (lobby-below-min? reg)
      false
      (let [room (get-in reg [:rooms room-id])
            users (:lobby reg)]
        (-> reg (update-in [:rooms room-id] room/add-users users)
            (assoc :lobby {})))))
