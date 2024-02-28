(ns handlers.setup
  (:require [config :as c]
            [util :as u]))

(defn extract-args [command]
  (-> (u/re-find-groups #"/setup ([^ ]+) ([^ ]+)( (\d+))?"
                        [:coin-id :vs-currency nil :amount]
                        command)
      (update :amount #(and % (Integer/parseInt %)))
      (update :amount #(or % 1))))

(defn valid-args? [args]
  (and (:coin-id args)
       (:vs-currency args)
       (number? (:amount args))
       (pos? (:amount args))))

(defn handler [_bot u]
  (let [chat-id (get-in u [:message :chat :id])
        message-id (get-in u [:message :message_id])
        text (get-in u [:message :text])
        args (extract-args text)]
    (if-not (and args (valid-args? args))
      {:op :sendMessage
       :request {:chat_id chat-id
                 :text "Expected /setup <coin id> <vs currency> [<amount>]"
                 :reply_parameters {:message_id message-id}}}
      (do (swap! c/chat->data assoc chat-id args)
          {:op :sendMessage
           :request {:chat_id chat-id
                     :text (format "Setup %s vs %s"
                                   (:coin-id args) (:vs-currency args))
                     :reply_parameters {:message_id message-id}}}))))
