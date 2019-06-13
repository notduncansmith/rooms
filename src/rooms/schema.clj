(ns rooms.schema
  (:require [hugsql.core :as sql]))

(sql/def-db-fns-from-string "-- :name execute-raw :!\n:sql:raw")

(sql/def-db-fns-from-string "
  -- :name create-migrations-table :!
  create table if not exists \"migrations\" 
    (\"id\" integer primary key autoincrement not null,
     \"name\" char(128) not null);

  -- :name count-saved-migrations :1
  select count(1) as count from migrations;

  -- :name record-migration :!
  insert into migrations (name) values (:name);
")

(def migrations (sql/map-of-db-fns-from-string "
  -- :name m0-wal-pragma :*
  pragma journal_mode = WAL;

  -- :name m1-foreign-key-pragma :!
  pragma foreign_keys = on;

  -- :name m3-create-users-table :!
  create table if not exists \"users\"
    ( \"id\" integer primary key autoincrement not null,
      \"created_at\" integer not null,
      \"updated_at \" integer not null
      \"username\" char(128) not null,
      \"access_token\" char(128) not null);

  -- :name m4-create-unique-username-index :!
  create unique index if not exists user_usernames on \"users\" (\"username\");
"))

(sql/def-db-fns-from-string "-- :name execute-raw :!\n:sql:raw")

(defn migration-version [conn] (-> (count-saved-migrations conn) (first) (:count)))

(defn- migrate-up
  "Run all migrations between the most-recently-run and `target-version`"
  [conn target-version]
  (let [current-version (migration-version conn)
        remaining-migrations (->> (keys migrations) (sort) (take target-version) (drop current-version))]
    (mapv #(do (println %
                        (((migrations %) :fn) conn)
                        (record-migration conn {:name %})))
          remaining-migrations)))

(defn migrate
  ([conn] (migrate conn (count migrations)))
  ([conn version] (do (create-migrations-table conn) (migrate-up conn version))))
