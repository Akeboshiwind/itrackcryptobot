(ns main
  (:require [telegram.core :as tg]
            [telegram.updates :as u]

            [handlers.version :as version]
            [handlers.stats :as stats]
            [handlers.setup :as setup]))

;; TODO: Get from environment
(def bot {::tg/token "1234:ABCDEFG"})

(def handlers
  {"/version" version/handler
   "/stats" stats/handler
   "/setup" setup/handler})

(defn -main [& _args]
  (let [stop (u/handle-updates bot handlers)]
    stop))
