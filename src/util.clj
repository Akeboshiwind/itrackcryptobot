(ns util)

(defn re-find-groups [re groups text]
  (->> (zipmap groups (rest (re-matches re text)))
       (remove (fn [[k v]] (or (nil? k) (nil? v))))
       (into {})))
