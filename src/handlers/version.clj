(ns handlers.version
  (:require [clojure.edn :as edn]
            [tg-clj-server.utils :as u]))

(def version
  (-> (slurp "bb.edn")
      edn/read-string
      :version))

(defn handler [{u :update}]
  (-> {:op :sendMessage
       :request {:text (str "Version: " version)}}
      (u/reply-to u)))
