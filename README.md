# rooms - collaborative state with websockets

[![Clojars Project](https://img.shields.io/clojars/v/rooms.svg)](https://clojars.org/rooms)

```clj
[rooms "0.5.2-SNAPSHOT"]
```

Rooms is a Clojure library that aims to simplify the process of building WebSocket apps.

A `Room` is an agent-based state container that users can connect to via WebSockets. Incoming WebSocket messages are given to a function which updates the room state, which is then broadcast to connected users.

## Usage

### Server-side
```clj
(require '[rooms.demo :refer [start create-room!]])

(create-room!
  "demo" ; room id
  (fn [state msg] (assoc state :last-message msg)) ; state fn
  (fn [state user] state) ; view fn

(start {:port 8080})
```

### Client-side
```js
connect('demo', {
  onConnect() {
    setTimeout(() => {
      this.send(JSON.stringify({greeting: "hello", from: navigator.userAgent}));
    }, 1000);
  },

  onMessage(msg) {
    console.log(JSON.parse(msg.data));
  }
});

function connect(roomId, {onConnect, onMessage}) {
  const ws = new WebSocket('ws://localhost:8080/room/' + roomId);
  ws.onopen = onConnect.bind(ws);
  ws.onmessage = onMessage.bind(ws);
}
```

Check out [`rooms.demo`](https://github.com/notduncansmith/rooms/blob/master/src/rooms/demo.clj) for a more comprehensive look, including a test page you can visit in your browser.

**This project is graciously sponsored by Dubsado ❤️**

[![Dubsado CRM](https://global-uploads.webflow.com/5bd3a12688389fdba3a24e77/5bd3a12688389f0bc7a24ea8_dubsado-logo.png)](https://dubsado.com)

## License

Copyright © 2019 Duncan Smith

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
