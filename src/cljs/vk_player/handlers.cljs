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
    (let
      [vk-track-response (rest response.response)
       default-track-options { :playing? false :progress 0 }
       track-list (map #(merge (js->clj % :keywordize-keys true) default-track-options) vk-track-response)
       founded-tracks (to-track-map track-list)]
      (assoc db :founded-tracks founded-tracks))))

(defn to-track-map [track-list]
  (into
    {}
    (map
      #(hash-map
         (let
           [aid-key (keyword (str (:aid %)))]
           aid-key)
         %) track-list)))

(re-frame/register-handler
  :search-track-text-changed
  (fn
    [db [_ value]]
    (js/VK.Api.call "audio.search" (js-obj "q" value) #(re-frame/dispatch [:process-search-track-response %]))
    (assoc db :search-track-text value)))

(re-frame/register-handler
  :track-play
  (fn
    [db [_ aid]]
    (let [db-tracks (:founded-tracks db)
          stopped-tracks (to-track-map
                             (map #(let [aid (first %)
                                         track (assoc (second %) :playing? false)]
                                     assoc nil aid track) db-tracks))
          founded-tracks (update-in stopped-tracks [aid] #(assoc % :playing? true))]
      (assoc db :founded-tracks founded-tracks :active-track-aid aid))))

(re-frame/register-handler
  :track-pause
  (fn
    [db [_ aid]]
    (let [db-tracks (:founded-tracks db)
          founded-tracks (update-in db-tracks [aid] #(assoc % :playing? false))]
      (assoc db :founded-tracks founded-tracks))))

(re-frame/register-handler
  :track-stop
  (fn
    [db [_ aid]]
    (re-frame/dispatch [:track-pause aid])
    (re-frame/dispatch [:track-progress-reset aid])
    db))

(re-frame/register-handler
  :track-time-update
  (fn
    [db [_ data]]
    (let [aid (:aid data)
          track-time (:track-time data)
          founded-tracks (update-in (:founded-tracks db) [aid] #(assoc % :progress (* 100 (/ track-time (:duration %)))))]
      (assoc db :founded-tracks founded-tracks))))

(re-frame/register-handler
  :track-progress-reset
  (fn
    [db [_ aid]]
    (let [founded-tracks (:founded-tracks db)
          progress-reset (update-in founded-tracks [aid] #(assoc % :progress 0))]
      (assoc db :founded-tracks progress-reset))))

(re-frame/register-handler
  :play-next
  (fn
    [db [_ aid]]
    (let [founded-tracks (:founded-tracks db)
          new-aid (first (second (drop-while #(not (= (first %) aid)) founded-tracks)))
          next-aid (if (nil? new-aid) aid new-aid)]
      (re-frame/dispatch [:track-stop aid])
      (re-frame/dispatch [:track-play next-aid])
      db)))

(re-frame/register-handler
  :play-previous
  (fn
    [db [_ aid]]
    (let [founded-tracks (:founded-tracks db)
          new-aid (first (last (take-while #(not (= (first %) aid)) founded-tracks)))
          previous-aid (if (nil? new-aid) aid new-aid)]
      (re-frame/dispatch [:track-stop aid])
      (re-frame/dispatch [:track-play previous-aid])
      db)))

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

(re-frame/register-handler
  :volume-change
  (fn [db [_ value]]
      (assoc db :options (update-in options [:volume] (constantly value)))))
















