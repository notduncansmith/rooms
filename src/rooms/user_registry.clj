(ns rooms.user-registry
  (:require [hugsql.core :as sql]
            [digest :refer [sha-256]]
            [crypto.random :refer [hex]]))

(sql/def-db-fns-from-string "
  -- :name create-user :i!
  insert into users (created_at, username, access_token)
           values (:created-at, :username, :access-token);

  -- :name set-user-token :!
  update users set token = :token where id = :id;

  -- :name remove-user :! :n
  delete from users where id = :id;

  -- :name find-user-with-token :?
  select * from users
    where username = :username and access_token = :token
    limit 1;
")

(defn generate-access-token [user-id created-at-ms] 
  (sha-256 (str user-id created-at-ms (hex 16))))
