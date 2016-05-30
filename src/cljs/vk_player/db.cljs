(ns vk-player.db)

(def default-db
  {:search-track-text ""
   :logged-in? true
   :founded-tracks {}
   :active-track nil
   :active-track-aid :0
   :options { :repeat-always? false
              :volume 50 }})
