(ns handlers.setup
  (:require [tg-clj-server.utils :as tg-u]
            [util :as u]
            [clojure.string :as str]))

(defn extract-args [command]
  (-> (u/re-find-groups #"/setup (\d+) ([a-z]+(->[a-z]+)+)"
                        [:amount :ids]
                        command)
      (update :amount #(and % (Integer/parseInt %)))
      (update :ids #(and % (str/split % #"->")))))

(comment
  (extract-args "/setup 1 btc->usd")
  (extract-args "/setup 1234 siacoin->usd->gbp"))

(defn valid-args? [args]
  (and (:amount args)
       (:ids args)
       (>= (count (:ids args)) 2)))

(defn handler [{:keys [store] u :update}]
  (let [chat-id (get-in u [:message :chat :id])
        args (-> u (get-in [:message :text]) extract-args)]
    (if-not (and args (valid-args? args))
      (-> {:op :sendMessage
           :request {:text "Expected /setup {amount} {coin-id}[->{vs-currency}]+"}}
          (tg-u/reply-to u))
      (-> {:op :sendMessage
           :request {:text (str "Setup " (str/join "->" (:ids args)))}}
          (tg-u/reply-to u)
          (assoc :set-store (assoc-in store [:chat->data chat-id] args))))))
