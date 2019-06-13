(defproject rooms "0.0.0"
  :description "FIXME: write description"
  :url "https://github.com/notduncansmith/rooms"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  
  :dependencies [[clj-ulid "0.1.0-SNAPSHOT"]
                 [org.xerial/sqlite-jdbc "3.23.1"]
                 [com.layerware/hugsql "0.4.9"]
                 [http-kit "2.3.0"]
                 [compojure "1.6.1"]
                 [crypto-random "1.2.0"]
                 [digest "1.4.9"]]
  
  :plugins [[lein-cloverage "1.0.13"]
            [lein-shell "0.5.0"]
            [lein-ancient "0.6.15"]
            [lein-changelog "0.3.2"]]
  
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.0"]]}}
  :deploy-repositories [["releases" :clojars]]
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
