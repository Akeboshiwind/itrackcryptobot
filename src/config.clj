(ns config
  (:require [clojure.edn :as edn]))

(defn save [data path]
  (spit path (pr-str data)))

(defn load [path]
  (-> path slurp edn/read-string))

(comment
  (let [data {:amount 1000000
              :ids ["siacoin" "usd" "gbp"]}
        path "/tmp/test.edn"]
    (save data path)
    (assert (= data (load path)))))

(defonce chat->data (atom {}))

(def data-path (or (System/getenv "DATA_PATH") "/data/chat.edn"))

(defn swap! [f & args]
  (save (apply clojure.core/swap! chat->data f args)
        data-path))
