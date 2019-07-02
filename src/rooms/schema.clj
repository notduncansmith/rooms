(ns rooms.schema
  (:require [hugsql.core :as sql]
            [dsm.sql :refer [create-migrations-table migrate-up]]))

(sql/def-db-fns-from-string "-- :name execute-raw :!\n:sql:raw")

(def migrations (sql/map-of-db-fns-from-string "
  -- :name -UP-m1-wal-pragma :*
  pragma journal_mode = WAL;

  -- :name -DOWN-m1-wal-pragma :*
  select true; -- WAL mode pragma is permanent

  -- :name -UP-m2-foreign-key-pragma :!
  pragma foreign_keys = on;

  -- :name -DOWN-m2-foreign-key-pragma :!
  pragma foreign_keys = off;

  -- :name -UP-m3-create-users-table :!
  create table if not exists \"users\"
    ( \"id\" integer primary key autoincrement not null,
      \"created_at\" integer not null,
      \"updated_at \" integer not null
      \"username\" char(128) not null,
      \"secret_hash\" char(128) not null);

  -- :name -DOWN-m3-drop-users-table :!
  drop table if exists \"users\";

  -- :name -UP-m4-create-unique-username-index :!
  create unique index if not exists user_usernames on \"users\" (\"username\");

  -- :name -DOWN-m4-drop-unique-username-index :!
  drop index if exists user_username;
"))

(sql/def-db-fns-from-string "-- :name execute-raw :!\n:sql:raw")

(migrate-up conn migrations)
