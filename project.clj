(defproject rooms "0.2.2-SNAPSHOT"
  :description "Collaborative state with WebSockets"
  :url "https://github.com/notduncansmith/rooms"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[http-kit "2.3.0"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [clojure-msgpack "1.2.1"]
                 [cheshire "5.8.1"]]

  :plugins [[lein-cloverage "1.0.13"]
            [lein-shell "0.5.0"]
            [lein-ancient "0.6.15"]
            [lein-changelog "0.3.2"]]

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.0"]]}}

  :deploy-repositories {"clojars-https" {:url "https://clojars.org/repo"
                                         :sign-releases false}}

  :aliases {"update-readme-version" ["shell" "sed" "-i" "s/\\\\[rooms \"[0-9.]*\"\\\\]/[rooms \"${:version}\"]/" "README.md"]}

  :release-tasks [["shell" "git" "diff" "--exit-code"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["changelog" "release"]
                  ["update-readme-version"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["vcs" "push"]])
