(ns schedule
  (:require [overtone.at-at :as at]
            [alerts :as a])
  (:import (java.time ZonedDateTime LocalTime)))

(def my-pool (at/mk-pool))

(def hour (* 60 60 1000))
(def day (* 24 hour))

(defn zdt->unix-timestamp [zdt]
  (.toEpochMilli (.toInstant zdt)))

(defn next-10am []
  (let [now (ZonedDateTime/now)
        now10am (.with now (LocalTime/of 10 0))]
    (zdt->unix-timestamp
      ; is now10am before now?
     (if (.isBefore now10am now)
       (.plusDays now10am 1)
       now10am))))

(defn daily [f]
  (at/every day f my-pool :initial-delay (- (next-10am) (at/now))))

(defn next-hour []
  (let [now (ZonedDateTime/now)
        next-hour (.plusHours now 1)]
    ; truncate to the hour
    (zdt->unix-timestamp
     (.with next-hour (LocalTime/of (.getHour next-hour) 0)))))

(defn hourly [f]
  (at/every hour f my-pool :initial-delay (- (next-hour) (at/now))))

(defn start [client]
  (hourly #(a/post-prices client))
  (daily #(a/post-charts client)))

(def stop #(at/stop-and-reset-pool! my-pool))
