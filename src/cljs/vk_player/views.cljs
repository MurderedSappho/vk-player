(ns vk-player.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

(defn main-panel
  [_]
  (let [search-track-text (re-frame/subscribe [:search-track-text])
        founded-tracks (re-frame/subscribe [:founded-tracks])
        active-track (re-frame/subscribe [:active-track])
        options (re-frame/subscribe [:options])]
    (fn []
      [:div.container-fluid
       [header @search-track-text @active-track @options]
       [:div.row
        [:div.col-lg-3]
        [:div.col-lg-6 (for [track  @founded-tracks]
                         ^{:key (get track 0)} [track-control (get track 1) (get track 0)])]
        [:div.col-lg-3]]])))

(defn header
  [search-track-text active-track options]
  [:nav.navbar.navbar-default.navbar-fixed-top
   [:div.container.fluid
    [:div.collapse.navbar-collapse
     [active-track-control active-track]
     [options-control options]
     [search-track search-track-text]]]])

(defn search-track
  [search-text]
  [:form {:class "navbar-form navbar-right", :role "search"}
   [:div {:class "form-group"}
    [:input {:value search-text
             :on-change #(re-frame/dispatch
                           [:search-track-text-changed (-> % .-target .-value)])
             :type "text"
             :class "form-control"
             :placeholder "Search"}]]])

(defn active-track-control
  [track]
  (let [aid (keyword (str (:aid track)))]
    (if (not (nil? track))
    [:div
   [:form.navbar-form.navbar-left
    [:div.btn.btn-default {:on-click  #(re-frame/dispatch [:play-previous aid]) } "Prev" ]
    (if (:playing? track)
      [:div.btn.btn-default
       {:on-click #(re-frame/dispatch [:track-pause aid])  } "Pause"]
      [:div.btn.btn-default {:on-click  #(re-frame/dispatch [:track-play aid]) } "Play"])
    [:div.btn.btn-default {:on-click  #(re-frame/dispatch [:play-next aid]) } "Next"]]
   [:form.navbar-form     [:div.progress.col-lg-2
                           [:div.progress-bar { :style {:width (str (:progress track) "%")}}]]]])))

(defn options-control
  [options]
  (let [repeat (:repeat-always? options)
        repeat-button-class (if repeat "btn-primary" "btn-default")]
    [:div
   [:form.navbar-form.navbar-right
    [:div.btn.btn-default { :on-click #(re-frame/dispatch [:shuffle]) } "Shuffle"]
    [:div.btn { :class repeat-button-class :on-click #(re-frame/dispatch [:repeat-always?])} "Repeat"]]]))


(defn track-control
  [track aid]
  (let [src "data"]
    (reagent/create-class
      {
        :component-will-receive-props
        (fn [component source]
          (let
            [props (nth source 1)
             playing? (:playing? props)
             position-null? (= (:progress props) 0)
             audio (.getDOMNode component.refs.audio)]
            (if playing?
              (-> audio .play)
              (-> audio .pause))))

        :component-did-mount
        (fn [component source]
          (let [audio (.getDOMNode component.refs.audio)]
            (set! (.-ontimeupdate audio) #(re-frame/dispatch [:track-time-update {:track-time (.-currentTime audio)
                                                                                  :aid aid}]))
            (set! (.-onended audio) #(re-frame/dispatch [:play-next aid]))))

        :reagent-render
        (fn [track]
          [:div.panel.panel-default
           [:div.panel-heading (:title track)]
           [:audio {:src (:url track)
                    :ref "audio"
                    :preload "node" }]
           [:div.panel-body
            [:div.progress
             [:div.progress-bar { :style {:width (str (:progress track) "%")}}]]
            [:div.col-lg-2
             [:div.btn-group.btn-group-justified
              (if (:playing? track)
                [:div.btn.btn-default
                 {:on-click #(re-frame/dispatch [:track-pause aid])  } "Pause"]
                [:div.btn.btn-default {:on-click  #(re-frame/dispatch [:track-play aid]) } "Play"])]]]])})))
