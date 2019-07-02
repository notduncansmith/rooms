(ns rooms.server
  (:use [compojure.route :only [files not-found]]
        [compojure.core :only [defroutes GET POST DELETE ANY context]]
        org.httpkit.server))


(defn home-page-handler [req] "<h1>Hello</h1>")

(defn websocket-handler [request]
  ; authenticate request
  (with-channel request channel
    ; watch room for changes and broadcast to user
    (on-close channel (fn [status] (println "channel closed: " status)))
    (on-receive channel (fn [data] ; construct message envelope -> send message to room agent
                          (send! channel data)))))

(defn user-details-handler [req]
  ; auth as above
  ; display user details
  "not implemented")

(defroutes all-routes
  (GET "/" [] home-page-handler)
  (GET "/user/:id" [] user-details-handler)
  (GET "/socket" [] websocket-handler)
  (not-found "<p>Page not found.</p>"))

(run-server all-routes {:port 8080})