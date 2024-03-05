(ns handlers.stats
  (:require [tg-clj-server.utils :as u]))

(defn handler [{:keys [store] u :update}]
  (-> {:op :sendMessage
       :request {:text (format "Stats: %s" store)}}
      (u/reply-to u)))
