(ns telegram.updates
  (:require [telegram.core :as tg]
            [clojure.tools.logging :as log]))

(defn process-handlers [handlers]
  (->> handlers
       (map (fn [[pred handler]]
              [(if (string? pred)
                 (partial tg/command? pred)
                 pred)
               handler]))))

(defn find-handler [handlers update]
  (some (fn [[pred handler]]
          (when (pred update)
            handler))
        handlers))

(defn handle-update [client handlers update]
  (when-let [handler (find-handler handlers update)]
    (when-let [request (handler client update)]
      (tg/invoke client request))))

(defn handle-updates
  "Starts a loop that fetches updates and dispatches them to the handlers.
  Returns a function that can be called to stop the loop.

  handlers  - a vector of pairs of [`predicate` `handler`], the first predicate which matches will have it's update called.

  predicate - a string or predicate which is passed an update.
  handler   - a function that takes a client and an update, *may* return a request for telegram.core/invoke to call.
  
  Example:
  (def handlers
    [[#(tg/command? \"/hello\" %)
      (fn [_client u]
        {:op :sendMessage
         :request {:chat_id (get-in u [:message :chat :id])
                   :text \"Hello, world!\"}})]
     ; As a special case a valid command string can be provided
     ; It will be transformed into #(tg/command? <string> update)
     [\"/hi\"
      (fn [_client u]
        {:op :sendMessage
         :request {:chat_id (get-in u [:message :chat :id])
                   :text \"Hello, world!\"}})]]"
  ([client handlers]
   (handle-updates client handlers {}))
  ([client handlers opts]
   (let [handlers (process-handlers handlers)
         stop-handle
         (future
           (loop [offset 0]
             (recur
              (try
                (let [opts (merge {:timeout 5} opts {:offset offset})
                      updates (-> (tg/get-updates client opts) :result)]
                  (if (seq updates)
                    (do (doseq [update updates]
                          (log/info "Handling update" update)
                          (handle-update client handlers update))
                        (->> updates (map :update_id) (apply max) inc))
                    offset))
                (catch Exception e
                  (log/error e "Failed to handle update, waiting 5s")
                  (Thread/sleep 5000)
                  offset)))))]
     #(future-cancel stop-handle))))
