(ns handlers.setup
  (:require [config :as c]
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

(defn handler [_bot u]
  (let [chat-id (get-in u [:message :chat :id])
        message-id (get-in u [:message :message_id])
        text (get-in u [:message :text])
        args (extract-args text)]
    (if-not (and args (valid-args? args))
      {:op :sendMessage
       :request {:chat_id chat-id
                 :text "Expected /setup {amount} {coin-id}[->{vs-currency}]+"
                 :reply_parameters {:message_id message-id}}}
      (do (swap! c/chat->data assoc chat-id args)
          {:op :sendMessage
           :request {:chat_id chat-id
                     :text (str "Setup " (str/join "->" (:ids args)))
                     :reply_parameters {:message_id message-id}}}))))
