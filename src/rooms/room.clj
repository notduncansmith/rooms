(ns rooms.room)

(defrecord Room [id agent f])

(defn empty-room [id f] (Room. id (agent {:users {}}) f))
(defn send-msg [room user-id msg] (send (:agent room) (:f room) {:user-id user-id :data msg}))
(defn update-room [room f] (send (:agent room) f))
(defn add-users [room users] (update-room reduce #(assoc-in % [:users (:id %2)] %2) users))
(defn remove-users [room user-ids] (update-room update :users apply dissoc user-ids))
