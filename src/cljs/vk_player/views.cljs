(ns vk-player.views
    (:require [re-frame.core :as re-frame]
              [reagent.core :as reagent]))

(defn main-panel
  [_]
  (let [search-track-text (re-frame/subscribe [:search-track-text])
        founded-tracks (re-frame/subscribe [:founded-tracks])]
    (fn []
      [:div.container
        [header @search-track-text]
          [:div (for [track  @founded-tracks]
             ^{:key (:aid track)} [track-control track])]])))

(defn header
  [search-track-text]
  [:div {:class "collapse navbar-collapse" }
    [search-track search-track-text]])

(defn search-track
  [search-text]
  [:form {:class "navbar-form navbar-left", :role "search"}
      [:div {:class "form-group"}
        [:input {:value search-text
                 :on-change #(re-frame/dispatch
                               [:search-track-text-changed (-> % .-target .-value)])
                 :type "text"
                 :class "form-control"
                 :placeholder "Search"}]]])

(defn track-control
  [track]
  (let [src "data"]
     (reagent/create-class
       {:component-will-receive-props
        (fn [component source]
          (let
            [playing? (:playing? (nth source 1))
             audio (.getDOMNode component.refs.audio)]
              (if playing?
                (-> audio .play)
                (-> audio .pause))))

        :reagent-render
         (fn [track]
              [:div.panel.panel-default
                [:div.panel-heading (:title track)]
                [:audio {:src (:url track) :preload "none" :ref "audio" } ]
                [:div.panel-body
                 [:div.progress
                   [:div.progress-bar {:width "0%"}]]
                 [:div.col-lg-2
                   [:div.btn-group.btn-group-justified
                     (if (:playing? track)
                       [:div.btn.btn-default
                        {:on-click #(re-frame/dispatch [:track-stop (:aid track)])  } "Stop"]
                       [:div.btn.btn-default {:on-click  #(re-frame/dispatch [:track-play (:aid track)]) } "Play"])
                [:div.btn.btn-default {:on-click #(re-frame/dispatch [:track-stop (:aid track)])  } "Pause"]]]]])})))
