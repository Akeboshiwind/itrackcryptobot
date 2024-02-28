(ns handlers.version
  (:require [clojure.edn :as edn]))

(def version
  (-> (slurp "bb.edn")
      edn/read-string
      :version))

(defn handler [_bot u]
  (let [chat-id (get-in u [:message :chat :id])
        message-id (get-in u [:message :message_id])]
    {:op :sendMessage
     :request {:chat_id chat-id
               :text (str "Version: " version)
               :reply_parameters
               {:message_id message-id}}}))
