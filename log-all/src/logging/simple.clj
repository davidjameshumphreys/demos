(ns logging.simple
  (:require [cheshire.core :as json]))


(defn log [data]
  (print (str (json/generate-string data) \newline))
  (flush))