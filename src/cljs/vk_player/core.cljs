(ns vk-player.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [vk-player.handlers]
              [vk-player.subs]
              [vk-player.views :as views]
              [vk-player.config :as config]))

(when config/debug?
  (println "dev mode"))

(defn mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (js/VK.init (clj->js {:apiId 4241324}) )
  (re-frame/dispatch-sync [:initialize-db])
  (re-frame/dispatch [:check-login-status])
  (mount-root))
