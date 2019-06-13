(ns rooms.core
  (:require [rooms.room-registry :refer [empty-room-registry 
                                         create-room
                                         remove-room
                                         add-lobby-user
                                         remove-lobby-user
                                         shift-lobby]]
            [rooms.user-registry :refer [create-user
                                         set-user-token
                                         remove-user
                                         find-user-with-token
                                         generate-access-token]]
            [rooms.schema :refer [migrate execute-raw]]))

(def conn {:classname "org.sqlite.JDBC"
           :subprotocol "sqlite"
           :subname (or (System/getenv "ROOMS_DB_PATH") "/Users/duncan/working/rooms/rooms.db")})

(defn sql! [sql-str] (execute-raw conn {:raw sql-str}))

(def default-registry (atom (empty-room-registry)))

(defn create-room! ([id] (swap! default-registry create-room id))
                   ([] (swap! default-registry create-room)))

(defn remove-room! [room-id] (swap! default-registry remove-room room-id))
(defn add-lobby-user! [user] (swap! default-registry add-lobby-user user))
(defn remove-lobby-user! [user-id] (swap! default-registry remove-lobby-user user-id))
(defn shift-lobby! [f] (swap! default-registry shift-lobby))

(defn create-user! [user] (create-user conn user))
(defn set-user-token! [user-id token] (set-user-token conn user-id token))
(defn remove-user! [user-id] (remove-user conn user-id))
(defn find-user-with-token! [username token] (find-user-with-token conn {:username username :token token}))

(defn migrate! [] (migrate conn))

