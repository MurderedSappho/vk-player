(ns vk-player.utils)

(def default-track-options
  { :playing? false :progress 0 :hidden? false })

(defn to-track-map
  [track-list]
  (into
    {}
    (map
      #(hash-map
         (let
           [aid-key (keyword (str (:aid %)))]
           aid-key)
         %) track-list)))
