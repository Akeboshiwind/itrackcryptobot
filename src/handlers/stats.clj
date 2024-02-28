(ns handlers.stats
  (:require [config :as c]))

(defn handler [_bot u]
  (let [chat-id (get-in u [:message :chat :id])
        message-id (get-in u [:message :message_id])]
    {:op :sendMessage
     :request {:chat_id chat-id
               :text (format "Stats: %s" @c/chat->data)
               :reply_parameters {:message_id message-id}}}))
