(ns main
  (:require [telegram.core :as tg]
            [telegram.updates :as u]

            [handlers.version :as version]
            [handlers.stats :as stats]
            [handlers.setup :as setup]

            [schedule :as s]

            [clojure.tools.logging :as log]))

;; TODO: Get from environment
(def bot {::tg/token (or (System/getenv "TELEGRAM_BOT_TOKEN")
                         (throw (Exception. "TELEGRAM_BOT_TOKEN is not set")))})

(def handlers
  {"/version" version/handler
   "/stats" stats/handler
   "/setup" setup/handler})

(defn start []
  (let [stop (u/handle-updates bot handlers)]
    (s/start bot)
    (log/info "Started bot!")
    #(do (stop)
         (s/stop))))

(comment
  (start))

(defn -main [& _args]
  (start)
  @(promise))
