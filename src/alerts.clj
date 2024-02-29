(ns alerts
  (:require [coingecko :as cg]
            [cheshire.core :as json]
            [clojure.string :as str]
            [babashka.process :refer [shell]]
            [telegram.core :as tg]
            [clojure.java.io :as io]
            [config :as config]))

(defn ->price-vega [amount data]
  (let [data (->> (:prices data)
                  (map #(zipmap [:ts :price] %))
                  (map #(update % :price * amount)))]
    {:data {:values data}
     :mark :line
     :encoding
     {:x {:field :ts :type "temporal"}
      :y {:field :price
          :type "quantitative"
          :scale {:zero false}}}}))

(defn convert-amount [amount ids]
  (reduce (fn [acc [from to]]
            (let [data (cg/simple-price
                        {:ids from :vs-currencies to})]
              (* acc (get-in data [(keyword from) (keyword to)]))))
          amount
          (partition 2 1 ids)))

(defonce chat-id->last-price (atom {}))

(defn post-prices [client]
  (doseq [[chat-id {:keys [amount ids]}]
          @config/chat->data]
    (let [price (convert-amount amount ids)
          last-price (get @chat-id->last-price chat-id)
          text (str "Current price: " (format "%.2f" price)
                    " " (str/upper-case (last ids))
                    (when last-price
                      (cond
                        (> price last-price) "🔼 "
                        (> last-price price) "🔽 ")))]
      (swap! chat-id->last-price assoc chat-id price)
      (tg/invoke client {:op :sendMessage
                         :request {:chat_id chat-id
                                   :text text}}))))

#_{:clj-kondo/ignore [:unresolved-namespace]}
(comment
  (post-prices main/bot))

(defn post-charts [client]
  (doseq [[chat-id {:keys [amount ids]}]
          @config/chat->data]
    (let [coin-id (first ids)
          vs-currency (second ids)
          ->final-currency (rest ids)
          vs-currency->final-currency (convert-amount 1 ->final-currency)
          data-file "/tmp/chart.json"
          img-file "/tmp/chart.png"]
      (->> (cg/market-chart {:id coin-id
                             :vs-currency vs-currency
                             :days 1})
           (->price-vega (* amount vs-currency->final-currency))
           json/generate-string
           (spit data-file))
      (shell "vl-convert vl2png" "-i" data-file "-o" img-file)
      (tg/invoke client {:op :sendPhoto
                         :request {:chat_id chat-id
                                   :photo (io/file img-file)}}))))

#_{:clj-kondo/ignore [:unresolved-namespace]}
(comment
  (post-charts main/bot))
