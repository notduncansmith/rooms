(ns rooms.room)

(defrecord Room [id agent state-fn view-fn])

(defn empty-room [id sf vf] (Room. id (agent {:users {}}) sf vf))
(defn update-room [room f & args] (do (apply send (:agent room) f args) room))

(defn send-msg
  [room user-id msg]
  (update-room room (:state-fn room) {:user (get-in room [:users user-id]) :data msg}))

(defn add-users
  [room users]
  (update-room room (fn [state] (reduce #(assoc-in % [:users (:id %2)] %2) state users))))

(defn remove-users
  [room user-ids]
  (update-room room update :users #(apply dissoc % user-ids)))

(defn watch-room
  [room user-id cb]
  (add-watch (:agent room) user-id (fn [k ref old-state new-state] (cb new-state))))

(defn unwatch-room
  [room user-id]
  (remove-watch (:agent room) user-id))