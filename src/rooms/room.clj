(ns rooms.room)

(defrecord Room [id agent state-fn view-fn])

(defn empty-room [id sf vf] (Room. id (agent {:id id :users {} :created-at (System/currentTimeMillis)}) sf vf))
(defn apply-msg [room state msg] ((:state-fn room) state msg))
(defn apply-user-msg [room state user-id msg] (apply-msg room state (assoc msg :user (get-in state [:users user-id]))))

(defn update-room [room f & args] (do (apply send (:agent room) f args) room))
(defn send-raw-msg [room msg] (update-room room #(apply-msg room % msg)))

(defn get-user [room user-id] (get-in @(:agent room) [:users user-id]))

(defn send-user-msg
  [room user-id msg]
  (update-room room #(apply-user-msg room % user-id msg)))

(defn add-users
  [room users]
  (update-room room
    (fn [state]
      (reduce #(do (println "Adding" %2 "to" %)
                   (apply-user-msg
                     room
                     (assoc-in % [:users (:id %2)] %2)
                     (:id %2)
                     {:data {"type" "connected"}}))
              state
              users))))

(defn remove-users
  [room user-ids]
  (update-room room
    (fn [state]
      (reduce #(-> room
                   (apply-user-msg % %2 {:data {"type" "disconnected"}})
                   (update :users dissoc %2))
              state
              user-ids))))

(defn watch-room
  [room key cb]
  (add-watch (:agent room) key (fn [k ref old-state new-state] (cb new-state))))

(defn unwatch-room
  [room key]
  (remove-watch (:agent room) key))
