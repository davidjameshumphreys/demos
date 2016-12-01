(ns logging.runner
  (:require [logging.configuration :as cfg]
            [taoensso.timbre :as log])
  (:gen-class))

(defn -main [& args]
  (cfg/setup-logging!)


  (log/info {:event ::started})

  (.start (Thread. (fn [] (Thread/sleep (+ 4000 (rand-int 3000)))
                     (throw (ex-info "unknown problem" {:event ::thread-problem})))))

  (log/debug {:event ::working
              :args (vec args)})

  (Thread/sleep 10000))