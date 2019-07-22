(ns rooms.demo
  (:use [compojure.route :only [files not-found]]
        [compojure.core :only [defroutes GET]]
        org.httpkit.server)
  (:require [rooms.core :as rooms]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(def demo-registry (atom (rooms/empty-room-registry 2)))

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
  <h2>Messages Seen:</h2>
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

(def create-room! (partial rooms/create-room! demo-registry))

(defn- set-test-port!
  [port] (swap! test-page clojure.string/replace #"localhost:\d*" (str "localhost:" port)))

(defn- get-ip
  [req]
  (or (get-in req [:headers "x-forwarded-for"])
      (:remote-addr req)))

(defroutes demo-routes
  (GET "/test" [_ :as req] (identity {:status 200 :headers {"Content-Type" "text/html"} :body @test-page}))
  (GET "/room/:id" [id encoding :as req]
    (let [user {:id (str (System/currentTimeMillis) "-" (get-ip req)) :joined-at (System/currentTimeMillis)}
          _ (println "ID " id "ENCODING " (or encoding "json"))]
      (rooms/connect! demo-registry id user req encoding)))
  (not-found "<p>Page not found.</p>"))

(defn- start-server [opts]
  (do (set-test-port! (:port opts))
      (run-server
        (wrap-defaults demo-routes api-defaults)
        opts)))

(def stopper (atom nil))

(defn start [& opts]
  (let [realopts (merge {:port 8080} (or (first opts) {}))]
    (do (create-room! "demo"
          (fn [s m] (do (println "RCV:" m) (assoc s :last-message m)))
          (fn [s uid] s))
        (reset! stopper (start-server realopts))
        (println "Started demo server on port " (:port realopts)))))

(defn stop [] (@stopper))
