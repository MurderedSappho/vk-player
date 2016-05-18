(ns vk-player.core)


(def tracks '({ :aid 1 :title "title1" } { :aid 2 :title "title2" } { :aid 3 :title "title3" }))

(def tr (into
    {}
    (map
      #(hash-map
         (let
           [aid-key (keyword (str (:aid %)))]
             aid-key)
             %)
      tracks)))

tr


(= tr { :2 {:aid 2, :title "title2"}, :1 {:aid 1, :title "title1"}})

(into {} (shuffle (into () tr)))


(first (take-while #(not (= (first %) :2)) tr))
