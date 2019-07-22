(ns rooms.room)

(defrecord Room [id agent state-fn view-fn])

(defn empty-room [id sf vf] (Room. id (agent {:id id :users {} :created-at (System/currentTimeMillis)}) sf vf))
(defn apply-msg
  ([room state msg] ((:state-fn room) state msg))
  ([room state user msg] (apply-msg room state (assoc msg :user user))))

(defn apply-user-msg [room state user-id msg] (apply-msg room state (get-in state [:users user-id]) msg))

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
      (reduce #(apply-msg room (assoc-in % [:users (:id %2)] %2)
                               %2
                               {:data {"type" "connected"}})
              state
              users))))

(defn remove-users
  [room user-ids]
  (update-room room
    (fn [state]
      (reduce #(if (nil? (get-in state [:users %2]))
                   state
                   (apply-msg room (update % :users dissoc %2)
                                   (get-in state [:users %2])
                                   {:data {"type" "disconnected"}}))
              state
              user-ids))))

(defn get-users [room] (:users @(:agent room)))

(defn remove-all-users [room] (remove-users room (keys (get-users room))))

(defn watch-room
  [room key cb]
  (add-watch (:agent room) key (fn [k ref old-state new-state] (cb new-state))))

(defn unwatch-room
  [room key]
  (remove-watch (:agent room) key))
