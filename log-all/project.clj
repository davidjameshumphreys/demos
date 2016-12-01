(defproject log-all "0.1.0-SNAPSHOT"
  :description "Some demo code to logging all the things"
  :url "https://github.com/davidjameshumphreys/demos/log-all"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"
  :pedantic? :warn
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.taoensso/timbre "4.7.4"]
                 [cheshire "5.6.3"]]

  :source-paths ["src"]
  :main logging.runner
)
