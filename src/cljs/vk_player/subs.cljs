(ns vk-player.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/register-sub
  :search-track-text
  (fn [db _]
    (reaction (:search-track-text @db))))

(re-frame/register-sub
  :founded-tracks
  (fn [db _]
    (reaction (:founded-tracks @db))))

(re-frame/register-sub
  :active-track
  (fn [db _]
    (reaction ((:active-track-aid @db) (:founded-tracks @db)))))

(re-frame/register-sub
  :options
  (fn [db _]
    (reaction (:options @db))))
