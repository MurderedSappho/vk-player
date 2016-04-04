(ns vk-player.handlers
    (:require [re-frame.core :as re-frame]
              [vk-player.db :as db]))

(re-frame/register-handler
 :initialize-db
 (fn
   [db _]
   merge db db/default-db))

(re-frame/register-handler
  :process-search-track-response
  (fn
    [db [_ response]]
    (let [founded-tracks (map
                           #(assoc (js->clj % :keywordize-keys true) :playing? false)
                           response.response)]
      (assoc db :founded-tracks founded-tracks))))

(re-frame/register-handler
 :search-track-text-changed
 (fn
   [db [_ value]]
   (js/VK.Api.call "audio.get" { "q" value } #(re-frame/dispatch [:process-search-track-response %]))
   (assoc db :search-track-text value)))

(re-frame/register-handler
 :track-play
 (fn
   [db [_ aid]]
     (let [founded-tracks (map
                            #(if (= (:aid %) aid)
                               (assoc % :playing? true)
                               (assoc % :playing? false))
                            (:founded-tracks db))]
       (assoc db :founded-tracks founded-tracks))))

(re-frame/register-handler
 :track-stop
 (fn
   [db [_ aid]]
     (let [founded-tracks (map
                            #(if (= (:aid %) aid) (assoc % :playing? false) %)
                            (:founded-tracks db))]
       (assoc db :founded-tracks founded-tracks))))

