(ns telegram.core
  (:require [babashka.http-client :as http]
            [cheshire.core :as json]))

(def base-url "https://api.telegram.org/")

(defn post
  "Perform a POST request to the Telegram API"
  [client method & args]
  (let [url (str base-url "bot" (::token client) "/" method)]
    (-> (apply http/post url args)
        :body
        (json/parse-string true))))

(def file? (partial instance? java.io.File))

(defn request->multipart [request]
  (->> request
       (map (fn [[k v]]
              {:name (name k)
               :content
               (cond
                 (or (string? v) (file? v)) v
                 :else (json/encode v))}))))

(defn invoke
  "Given a map of:

  :op      - the method to call
  :request - the parameters for the method

  Perform a POST request to the Telegram API"
  [client {:keys [op request]}]
  (post client (name op)
        (when request
          ; Prefer application/json, not sure why
          ; ChatGPT says it's more efficient :shrug:
          (if-not (some file? (vals request))
            {:headers {"Content-Type" "application/json"}
             :body (json/encode request)}
            {:headers {"Content-Type" "multipart/form-data"}
             :multipart (request->multipart request)}))))

#_{:clj-kondo/ignore [:unresolved-namespace]}
(comment
  (let [bot main/bot
        chat-id 1234]
    #_(invoke bot
              {:op :sendMessage
               :request {:chat_id chat-id
                         :text "Hello, world!"
                         :reply_text {:message_id 3398}}})
    (invoke bot
            {:op :sendPhoto
             :request {:chat_id chat-id
                       :photo (io/file "/tmp/out.png")
                       :reply_parameters {:message_id 3398}}})

    (request->multipart
     {:chat_id chat-id
      :photo (io/file "/tmp/out.png")
      :reply_parameters {:message_id 3398}})))

; TODO: Move to updates.clj?
(defn get-me [client]
  (invoke client {:op :getMe}))

(defonce me (atom nil))

(defn get-updates [client opts]
  (when-not @me
    (reset! me (:result (get-me client))))
  (-> (invoke client
              {:op :getUpdates
               :request opts})
      (update :result #(map (fn [u] (assoc u ::me @me)) %))))

(defn valid-command? [cmd]
  (re-matches #"/[a-z0-9_]+" cmd))

(defn command? [cmd u]
  (assert (valid-command? cmd) (str "Invalid command: " cmd))
  (when-let [text (get-in u [:message :text])]
    (let [username (get-in u [::me :username])
          pattern (str "(^| )" cmd "($|@" username "| )")]
      (re-find (re-pattern pattern) text))))

(comment
  ;; TODO: Turn into tests
  (let [cmd? (partial command? "/version")
        username "mybot"
        ->msg (fn [text] {::me {:username username}
                          :message {:text text}})]
    (and (not (cmd? (->msg "/ver")))
         (not (cmd? (->msg "/versionxxx")))
         (not (cmd? (->msg "/version@notmybot")))
         (not (cmd? (->msg "something/version")))
         (cmd? (->msg "/version"))
         (cmd? (->msg "/version@mybot"))
         (cmd? (->msg "some /version text")))))
