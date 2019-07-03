# rooms - collaborative state with websockets

[![Clojars Project](https://img.shields.io/clojars/v/rooms.svg)](https://clojars.org/rooms)

```clj
[rooms "0.1.3-SNAPSHOT"]
```

Rooms is a Clojure library that aims to simplify the process of building WebSocket apps.

A `Room` is an agent-based state container that users can connect to via WebSockets. Incoming WebSocket messages are given to a function which updates the room state, which is then broadcast to connected users.

## Usage

### Server-side
```clj
(require '[rooms.core :refer [create-room!]])
(require '[rooms.server :refer [start-server]])

(create-room!
  "demo"                                           ; room id
  (fn [state msg] (assoc state :last-message msg)) ; state fn
  (fn [state user-id] state)                       ; view fn

(start-server {:port 8080})
```

### Client-side
```js
const ws = new WebSocket("ws://localhost:8080/room/demo"); // note room id

ws.onopen = () => {
  setTimeout(() => {
    ws.send(JSON.stringify({greeting: "hello", from: navigator.userAgent}));
  }, 1000);
};

ws.onmessage = (msg) => {
  console.log(JSON.parse(msg.data));
}
```

**This project is graciously sponsored by Dubsado ❤️**

[![Dubsado CRM](https://global-uploads.webflow.com/5bd3a12688389fdba3a24e77/5bd3a12688389f0bc7a24ea8_dubsado-logo.png)](https://dubsado.com)

## License

Copyright © 2019 Duncan Smith

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
