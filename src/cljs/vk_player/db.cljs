(ns vk-player.db)

(def default-db
  {:audio-query {:search-track-text ""
                 :offset 0
                 :count 20}
   :loading? false
   :logged-in? true
   :founded-tracks {}
   :active-track nil
   :active-track-aid :0
   :options {:repeat-always? false
             :volume 50}})
