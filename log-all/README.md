# LOG ALL THE THINGS #

A talk on logging given at
[Clojure eXchange 2016](https://skillsmatter.com/conferences/7430-clojure-exchange-2016).

The talk focussed on logging in the Clojure world.

Below are some notes from the talk.

## AvisoNovate's Pretty ##
[https://github.com/AvisoNovate/pretty](https://github.com/AvisoNovate/pretty),
it can reduce the size of stacktrace output for an exception.

```clojure
(require '[io.aviso.exception :as ex])
(binding [ex/*fonts* nil]
  (ex/analyze-exception
    exception-maybe-has-ex-data
    nil))
```

## Timbre Log library ##
[https://github.com/ptaoussanis/timbre](https://github.com/ptaoussanis/timbre),
it works well as a Clojure logging library with a lot of power and
flexibility.

Making a simple json-format output:

```clojure
(:require [clojure.walk :refer [postwalk]]
          [taoensso.encore :as enc]
          [taoensso.timbre :as log]
          [clojure.string :as str]
          [io.aviso.exception :as ex])

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

(def cfg {:timestamp-opts {:pattern  "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                           :locale   :jvm-default,
                           :timezone :utc}
          :output-fn      (fn [data]
                            (let [{:keys [level ?err vargs msg_ ?ns-str hostname_
                                          timestamp_ ?line]} data]
                              (str {:timestamp (force timestamp_)
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
                                                   (str (ex/analyze-exception (force err) nil))))
                                    })))
          :middleware     [clean-keys]})

(log/merge-config! cfg)

;; then start logging.

```

## Setting the level ##
You can set the log level with `TIMBRE_LEVEL=':info'` as an environment variable.

# Links #

## Blog post by Frankie Sardo ##
Some great insights from Frankie Sardo about logging.

[https://juxt.pro/blog/posts/logging.html](https://juxt.pro/blog/posts/logging.html)

## Blog post about println ##
An explanation of why println is not atomic.

[http://yellerapp.com/posts/2014-12-11-14-race-condition-in-clojure-println.html](http://yellerapp.com/posts/2014-12-11-14-race-condition-in-clojure-println.html)
