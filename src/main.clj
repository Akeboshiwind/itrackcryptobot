(ns main
  (:require [tg-clj.core :as tg]
            [tg-clj-server.defaults :as defaults]
            [tg-clj-server.middleware.global-admin :as admin]
            [tg-clj-server.poll :as tg-poll]

            [handlers.version :as version]
            [handlers.stats :as stats]
            [handlers.setup :as setup]

            [schedule :as s]
            [config :as config]

            [clojure.tools.logging :as log]))

(def bot
  (tg/make-client
   {:token (or (System/getenv "TELEGRAM_BOT_TOKEN")
               (throw (Exception. "TELEGRAM_BOT_TOKEN is not set")))}))

(def routes
  (merge
   {"/version" {:handler version/handler
                :admin-only true}
    "/stats" {:handler stats/handler
              :admin-only true}
    "/setup" {:handler setup/handler
              :admin-only true}}
   admin/global-admin-routes))

(def app
  (let [path (or (System/getenv "DATA_PATH") "/data/chat.edn")]
    (defaults/make-app routes {:middleware [admin/global-admin-middleware]
                               :store/path path
                               :store/atom config/store})))

(defn start []
  (let [stop-handle (future (tg-poll/run-server bot app))]
    (s/start bot)
    (log/info "Started bot!")
    #(do (future-cancel stop-handle)
         (s/stop))))

(comment
  (def stop (start))
  (stop))

(defn -main [& _args]
  (start)
  @(promise))
