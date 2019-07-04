(ns rooms.server
  (:use [compojure.route :only [files not-found]]
        [compojure.core :only [defroutes GET]]
        rooms.core
        org.httpkit.server)
  (:require [msgpack.core :as mp]
            msgpack.clojure-extensions
            [cheshire.core :as chjson]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(defn connect
  [registry room-id user request encoding]
  (let [_ (println room-id)
        pack (if (= encoding "msgpack") mp/pack chjson/generate-string)
        unpack (if (= encoding "msgpack") mp/unpack chjson/parse-string)
        deliver! (partial send-user-msg! room-id (:id user))
        subscription-id (str (System/currentTimeMillis) (:id user))]
    (with-channel request channel
      (println "Registry state" registry)
      (println "Connection from user" user)
      (println "Joining room" (get-room! room-id))

      (add-user! room-id user)

      (watch-room! room-id subscription-id
        #(let [room (get-room! room-id)
               view-fn (:view-fn room)
               current-user (or (get-in room [:users (:id user)]) user)
               visible-state (clojure.walk/stringify-keys (view-fn % current-user))
               _ (println "Sending visible-state: " visible-state)]
            (send! channel (pack {"state" visible-state}))))

      (on-close channel
        (fn [status]
          (do (println "channel closed: " status)
              (unwatch-room! room-id subscription-id)
              (remove-user! room-id (:id user)))))

      (on-receive channel #(do (println "Received message: " (unpack %))
                               (deliver! (unpack %))))

      (send! channel (pack {"greeting" "Hello"})))))

(defn- get-ip
  [req]
  (or (get-in req [:headers "x-forwarded-for"])
      (:remote-addr req)))

(defroutes api-routes
  (GET "/room/:id" [id encoding :as req]
    (let [user {:id (str (System/currentTimeMillis) "-" (get-ip req)) :joined-at (System/currentTimeMillis)}
          _ (println "ID " id "ENCODING " encoding)]
      (connect default-registry id user req encoding)))
  (not-found "<p>Page not found.</p>"))

(def test-page (atom "
<html>
<head>
  <title>Server Test</title>
  <style>
    * {
      font-family: monospace;
    }
  </style>
</head>
<body>
  <h1>Just testing the server...</h1>

  <h2>State:</h2>
  <pre id='state'></pre>
  <h2>Messages:</h2>
  <ul id='messages'></ul>

  <script src='https://code.jquery.com/jquery-3.4.1.min.js'
    integrity='sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo=' crossorigin='anonymous'></script>
  <script crossorigin src='https://unpkg.com/@msgpack/msgpack'></script>

  <script>
    const ws = new WebSocket('ws://localhost:8080/room/demo');
    ws.onopen = () => {
      setTimeout(() => {
        ws.send(JSON.stringify({ greeting: 'hello', from: navigator.userAgent }));
      }, 1000);
    };

    ws.onmessage = (msg) => {
      console.log(msg);
      let contents = msg.data;
      if (msg.data instanceof Blob) {
        blobBytes(msg.data).then(bz => {
          console.log('bz: ', bz);
          const decoded = MessagePack.decode(bz);
          console.log('decoded: ', decoded);
          $('#messages').append($(`<li><pre>${JSON.stringify(decoded)}</pre></li>`))
          $('#state').text(JSON.stringify(decoded.state, null, 2))
        });
      } else if (typeof msg.data === 'string') {
        const decoded = JSON.parse(msg.data);
        console.log('decoded JSON: ', decoded);
        $('#messages').append($(`<li><pre>${JSON.stringify(decoded)}</pre></li>`))
        $('#state').text(JSON.stringify(decoded.state, null, 2))
      } else {
        $('#messages').append($(`<li><pre>${contents}</pre></li>`))
      }
    }

    function blobBytes(blob) {
      return new Promise((resolve, reject) => {
        let reader = new FileReader();
        reader.addEventListener('loadend', function () {
          const buf = reader.result;
          const bz = new Uint8Array(buf, 0, buf.byteLength);
          resolve(bz);
        });
        reader.readAsArrayBuffer(blob);
      });
    }
  </script>
</body>
</html>
"))

(defroutes test-routes
  (GET "/test" [_ :as req] (identity {:status 200 :headers {"Content-Type" "text/html"} :body @test-page}))
  api-routes)

(defn start-server [opts]
  (run-server
    (wrap-defaults (if (:test opts)
                       (do (swap! test-page clojure.string/replace #"localhost:\d*" (str "localhost:" (:port opts)))
                           test-routes)
                       api-routes)
                   api-defaults)
    opts))
