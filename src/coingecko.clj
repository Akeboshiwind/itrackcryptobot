(ns coingecko
  (:refer-clojure :exclude [get])
  (:require [babashka.http-client :as http]
            [cheshire.core :as json]
            [clojure.string :as str]))

(def base-url "https://api.coingecko.com/api/v3")

(defn get
  "Perform a GET request to the CoinGecko API"
  [method & args]
  (let [url (str base-url "/" method)]
    (-> (apply http/get url args)
        :body
        (json/parse-string true))))

(defn ensure-comma-separated [value]
  (if (vector? value)
    (str/join "," value)
    value))

(defn simple-price [{:keys [ids vs-currencies]}]
  (let [ids (ensure-comma-separated ids)
        vs-currencies (ensure-comma-separated vs-currencies)]
    (get "/simple/price"
         {:query-params {:ids ids
                         :vs_currencies vs-currencies}})))

(defn market-chart [{:keys [id vs-currency days]}]
  (get (str "/coins/" id "/market_chart")
       {:query-params {:vs_currency vs-currency
                       :days days}}))

(comment
  (get "/ping")

  (simple-price {:ids "siacoin"
                 :vs-currencies "usd"})

  (market-chart {:id "siacoin"
                 :vs-currency "usd"
                 :days 1}))
