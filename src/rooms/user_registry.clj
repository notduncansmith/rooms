(ns rooms.user-registry
  "The user registry is an SQLite database containing user records and their access tokens"
  (:require [hugsql.core :as sql]
            [digest :refer [sha-256]]
            [crypto.random :refer [hex]]))

(sql/def-db-fns-from-string "
  -- :name create-user :i!
  insert into users (created_at, username, secret_hash)
           values (:created-at, :username, :secret-hash);

  -- :name set-user-token :!
  update users set token = :token where id = :id;

  -- :name remove-user :! :n
  delete from users where id = :id;

  -- :name find-user-with-hash :?
  select * from users
    where username = :username and secret_hash = :hash
    limit 1;
")

(defn generate-secret [] (hex 16))

(defn register [conn username]
  (let [secret (generate-secret)]
    (create-user {:username username 
                  :created-at (System/currentTimeMillis) 
                  :secret_hash (sha-256 secret)})))

(defn find-user-with-secret [conn username secret]
  (find-user-with-hash conn {:username username :hash (sha-256 secret)}))
