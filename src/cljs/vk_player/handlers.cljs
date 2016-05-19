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
    (let [founded-tracks (into {} (map #(hash-map (let [aid-key (keyword (str (:aid %)))] aid-key) %)
                                       (map
                                         #(assoc (js->clj % :keywordize-keys true) :playing? false :progress 0)
                                         (rest response.response))))]
      (assoc db :founded-tracks founded-tracks))))

(re-frame/register-handler
  :search-track-text-changed
  (fn
    [db [_ value]]
    (js/VK.Api.call "audio.search" (js-obj "q" value "count" 8) #(re-frame/dispatch [:process-search-track-response %]))
    (assoc db :search-track-text value)))

(re-frame/register-handler
  :track-play
  (fn
    [db [_ aid]]
    (let [db-tracks (:founded-tracks db)
          founded-tracks (update-in db-tracks [aid] #(assoc % :playing? true))]
      (assoc db :founded-tracks founded-tracks :active-track-aid aid))))

(re-frame/register-handler
  :track-pause
  (fn
    [db [_ aid]]
    (let [founded-tracks (update-in
                           (:founded-tracks db)
                           [aid]
                           #(assoc % :playing? false))]
      (assoc db :founded-tracks founded-tracks))))

(re-frame/register-handler
  :track-time-update
  (fn
    [db [_ data]]
    (let [aid (:aid data)
          track-time (:track-time data)
          founded-tracks (update-in (:founded-tracks db) [aid] #(assoc % :progress (* 100 (/ track-time (:duration %)))))]
      (assoc db :founded-tracks founded-tracks))))

(re-frame/register-handler
  :play-next
  (fn
    [db [_ aid]]
    (let [founded-tracks (:founded-tracks db)
          stop-all (update-in founded-tracks [aid] #(assoc % :playing? false :progress 0))
          new-aid (first (second (drop-while #(not (= (first %) aid)) stop-all)))
          next-aid (if (nil? new-aid) aid new-aid)
          play-next (update-in stop-all [next-aid] #(assoc % :playing? true))]
      (assoc db :founded-tracks play-next :active-track-aid next-aid))))

(re-frame/register-handler
  :play-previous
  (fn
    [db [_ aid]]
    (let [founded-tracks (:founded-tracks db)
          stop-all (update-in founded-tracks [aid] #(assoc % :playing? false :progress 0))
          new-aid (first (last (take-while #(not (= (first %) aid)) stop-all)))
          previous-aid (if (nil? new-aid) aid new-aid)
          play-previous (update-in stop-all [previous-aid] #(assoc % :playing? true))]
      (assoc db :founded-tracks play-previous :active-track-aid previous-aid))))

(re-frame/register-handler
  :track-end
  (fn
    [db [_ aid]]
    (let [repeat? (:repeat-always? (:options db))]
      (if (not repeat?)
        (re-frame/dispatch [:play-next aid]))
      db)))

(re-frame/register-handler
  :shuffle
  (fn [db [_ data]]
    (assoc db
      :founded-tracks
      (into {} (shuffle (:founded-tracks db))))))

(re-frame/register-handler
  :repeat-always?
  (fn [db [_ data]]
    (let [options (:options db)]
      (assoc db :options (update-in options [:repeat-always?] not)))))
