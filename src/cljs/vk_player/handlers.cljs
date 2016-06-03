(ns vk-player.handlers
  (:require [re-frame.core :as re-frame]
            [vk-player.db :as db]
            [vk-player.utils :as utils]))

(re-frame/register-handler
  :initialize-db
  (fn
    [db _]
    merge db db/default-db))

(re-frame/register-handler
  :check-login-status
  (fn
    [db [_]]
    (js/VK.Auth.getLoginStatus #(re-frame/dispatch [:progress-login %]))
    db))

(re-frame/register-handler
  :login
  (fn
    [db [_]]
    (js/VK.Auth.login #(re-frame/dispatch [:progress-login %]))
    db))

(re-frame/register-handler
  :progress-login
  (fn
    [db [_ response]]
    (let [auth-response (js->clj response :keywordize-keys true)
          logged-in? (= (:status auth-response) "connected")]
      (assoc db :logged-in? logged-in?))))

(re-frame/register-handler
  :search-tracks
  (fn
    [db [_]]
    (let [callback #(re-frame/dispatch [:process-search-track-response %])
          audio-query (:audio-query db)
          search-query (clj->js {:q (:search-track-text audio-query)})]
      (js/VK.Api.call "audio.search" search-query callback)
      (assoc db :loading? true))))

(re-frame/register-handler
  :load-tracks-by-scroll
  (fn
    [db [_]]
    (if (:loading? db)
      db
      (let [audio-query (:audio-query db)
            offset (+ (:offset audio-query) 25)
            search-text (:search-track-text audio-query)
            search-query (clj->js {:q search-text :offset offset})
            callback #(re-frame/dispatch [:process-load-tracks-by-scroll %])
            set-loading (assoc db :loading? true :offset offset)]
        (js/VK.Api.call "audio.search" search-query callback)
        (update-in set-loading [:audio-query :offset] (constantly offset))))))



(re-frame/register-handler
  :process-load-tracks-by-scroll
  (fn
    [db [_ response]]
    (let
      [vk-track-response (rest response.response)
       track-list (map #(merge (js->clj % :keywordize-keys true) utils/default-track-options) vk-track-response)
       founded-tracks (conj (utils/to-track-map track-list) (:founded-tracks db))]
      (assoc db :founded-tracks founded-tracks :loading? false))))

(re-frame/register-handler
  :process-search-track-response
  (fn
    [db [_ response]]
    (let
      [vk-track-response (rest response.response)
       track-list (map #(merge (js->clj % :keywordize-keys true) utils/default-track-options) vk-track-response)
       founded-tracks (utils/to-track-map track-list)
       active-track-aid (:active-track-aid db)
       active-track (active-track-aid (:founded-tracks db))
       hidden-active-track (assoc active-track :hidden? true)
       tracks-with-active (assoc founded-tracks active-track-aid hidden-active-track)]
      (assoc db :founded-tracks (if (nil? active-track) founded-tracks tracks-with-active) :loading? false))))


(re-frame/register-handler
  :search-track-text-changed
  (fn
    [db [_ value]]
    (update-in db [:audio-query] #(assoc % :search-track-text value :offset 0))))

(re-frame/register-handler
  :track-play
  (fn
    [db [_ aid]]
    (let [db-tracks (:founded-tracks db)
          stopped-tracks (utils/to-track-map
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

