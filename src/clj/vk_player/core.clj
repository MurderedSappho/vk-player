(ns vk-player.core)


(def tracks '({ :aid 1 :title "title1" :playing? false } { :aid 2 :title "title2" :playing? false } { :aid 3 :title "title3" :playing? true }))

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


(ffirst (take-while #(= (:playing? (second %) ) true) tr))

 (map #(let [aid (first %)
            track (assoc (second %) :playing? false)]
         assoc nil aid track) tr)

 (into
    {}
    (map
      #(hash-map
         (let
           [aid-key (keyword (str (:aid %)))]
             aid-key)
             %)
      (map #(let [aid (first %)
            track (assoc (second %) :playing? false)]
         assoc nil aid track) tr)))

(first (last (drop-while #(= (:playing (second %)) false) tr)))

(= ':0 ':0)

(update-in { :options { :repeat-always? false :volume 50 } } [:options :volume] (constantly 2))

(into
    {}
    (map
      #(hash-map
         (let
           [aid-key (keyword (str (:aid %)))]
             aid-key)
             %) ) )
