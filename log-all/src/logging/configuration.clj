(ns logging.configuration
  (:require [clojure.walk :refer [postwalk prewalk]]
            [taoensso.encore :as enc]
            [taoensso.timbre :as log]
            [clojure.string :as str]
            [io.aviso.exception :as ex]
            [cheshire.core :as json]))

(def sensitive-keys #{:password :s3-aws-secret-access-key :s3-aws-access-key-id})

(defn clean-keys [m]
  (postwalk (fn [node]
              (if (map? node)
                (as-> node N
                      ;; remove the predefined sensitive-keys
                      (apply dissoc N sensitive-keys)
                      ;; remove vals that are empty.
                      (enc/remove-vals (fn [v] (and (seq? v) (empty? v))) N)
                      ;; If you are super-paranoid, you could delay some keys until the logging happens.
                      (enc/map-vals force N))
                node)) m))

(def sample-map {:user     {:username "david"
                            :password "abc"}
                 :settings {:s3-aws-access-key-id     "1234"
                            :s3-aws-secret-access-key "secret"
                            :pooling                  2}})

(def cfg
  )




(defn setup-logging! []
  (let [cfg {:timestamp-opts {:pattern  "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                              :locale   :jvm-default,
                              :timezone :utc}
             :output-fn      (fn [data]
                               (let [{:keys [level ?err vargs ?ns-str hostname_
                                             timestamp_ ?line]} data]
                                 (json/generate-string
                                   {:timestamp (force timestamp_)
                                    :hostname  (force hostname_)
                                    :level     (str/upper-case (name level))
                                    :ns        (or ?ns-str "?")
                                    :line      (or ?line "?")
                                    :version   "1234"
                                    :message   (->> (force vargs)
                                                    (map-indexed (fn [i m]
                                                                   (if (map? m)
                                                                     m
                                                                     {(str "key-" i) m})))
                                                    (apply merge))
                                    :error     (when-let [err ?err]
                                                 (binding [ex/*fonts* nil]
                                                   (ex/analyze-exception (force err) nil)))})))
             :middleware     [clean-keys]}]
    (log/merge-config! cfg)

    (.. (Runtime/getRuntime)
        (addShutdownHook (Thread. #(log/info {:event ::system-exit}))))


    (Thread/setDefaultUncaughtExceptionHandler
      (reify Thread$UncaughtExceptionHandler
        (uncaughtException [_this _thread e]
          (log/error e {:event ::uncaught-exception}))))))

(comment
  (setup-logging!)

  (log/info sample-map "something" "else" "different")

  (log/info {:username "david" :password "LetMeIn!" :action ::user-login})

  (try
    (throw (ex-info "broken" {:moar :data}))
    (catch Exception e
      (log/error e {:event ::open-pod-doors})))

  (log/debug {:data (delay (json/parse-string "{\"a\":\"b\"}"))}))