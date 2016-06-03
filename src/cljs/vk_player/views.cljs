(ns vk-player.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

(defn main-panel
  [_]
  (let [search-track-text (re-frame/subscribe [:search-track-text])
        founded-tracks (re-frame/subscribe [:founded-tracks])
        active-track (re-frame/subscribe [:active-track])
        active-track-aid (re-frame/subscribe [:active-track-aid])
        options (re-frame/subscribe [:options])
        logged-in? (re-frame/subscribe [:logged-in?])
        database (re-frame/subscribe [:database])]
    (fn []
      (println @database)
      [:div.container-fluid
       [header @search-track-text @active-track @options @logged-in?]
       [:div.row
        [:div.col-lg-3]
        [:div.col-lg-6 { :style {:margin-top "120px"} }
         (let [volume (str (/ (:volume @options) 100))
               active-aid @active-track-aid]
           (for [track-item  @founded-tracks]
             (let [aid (get track-item 0)
                   track (get track-item 1)
                   active? (= aid active-aid)
                   hidden? (:hidden? track)]
               ^{:key aid} [track-control track aid volume active? hidden?])))]
        [:div.col-lg-3]]])))

(defn header
  [search-track-text active-track options logged-in?]
  [:nav.navbar.navbar-default.navbar-fixed-top
   [:div.container
    [:div.row
     [:div.collapse.navbar-collapse
      [active-track-control active-track]
      [options-control options]
      [search-track search-track-text]
      [auth-section logged-in?]]]]])

(defn search-track
  [search-text]
  [:div {:class "navbar-form navbar-right", :role "search"}
   [:div {:class "form-group"}
    [:input {:value search-text
             :on-change #(re-frame/dispatch
                           [:search-track-text-changed (-> % .-target .-value)])
             :on-key-up #(if (= 13 (-> % .-keyCode)) (re-frame/dispatch [:search-tracks]))
             :type "text"
             :class "form-control"
             :placeholder "Search"}]]])

(defn auth-section
  [is-logged-in]
  (if is-logged-in
    nil
    [:div
     [:div.col-lg-2.navbar-left
      [:form.navbar-form
       [:div.btn.btn-default { :on-click #(re-frame/dispatch [:login]) } "Login"]]]]))

(defn active-track-control
  [track]
  (let [aid (keyword (str (:aid track)))]
    (println track)
    (if (not (nil? track))
      [:div.col-lg-7
       [:form.navbar-form.navbar-left
        [:div.btn.btn-default {:on-click  #(re-frame/dispatch [:play-previous aid]) } "Prev" ]
        (if (:playing? track)
          [:div.btn.btn-default
           {:on-click #(re-frame/dispatch [:track-pause aid])  } "Pause"]
          [:div.btn.btn-default {:on-click  #(re-frame/dispatch [:track-play aid]) } "Play"])
        [:div.btn.btn-default {:on-click  #(re-frame/dispatch [:play-next aid]) } "Next"]]
       [:a (str (:title track) " — " (:artist track))]
       [:form.navbar-form     [:div.progress.col-lg-6
                               [:div.progress-bar { :style {:width (str (:progress track) "%")}}]]]
       [:ul.nav.navbar-nav ]])))

(defn options-control
  [options]
  (let [repeat (:repeat-always? options)
        repeat-button-class (if repeat "btn-primary" "btn-default")
        volume (:volume options)]
    [:div
     [:div.col-lg-1.navbar-right           [volume-slider volume]]
     [:div.col-lg-2.navbar-right

      [:form.navbar-form
       [:div.btn.btn-default { :on-click #(re-frame/dispatch [:shuffle]) } "Shuffle"]
       [:div.btn { :class repeat-button-class :on-click #(re-frame/dispatch [:repeat-always?])} "Repeat"]]]]))


(defn volume-slider
  [volume-value]
  [:input { :type "range" :value volume-value :min 0 :max 100
            :style { :width "100%" :-webkit-appearance "slider-vertical" :height "90px" }
            :on-change #(re-frame/dispatch [:volume-change (-> % .-target .-value)])}])

(defn track-control
  [track aid active? hidden?]
  (let [src "data"]
    (reagent/create-class
      {
        :component-will-receive-props
        (fn [component source]
          (let
            [track (nth source 1)
             playing? (:playing? track)
             position-null? (= (:progress track) 0)
             volume (nth source 3)
             audio (.getDOMNode component.refs.audio)]
            (set! (-> audio .-volume) volume)
            (if playing?
              (-> audio .play)
              (-> audio .pause))))

        :component-did-mount
        (fn [component source]
          (let [audio (.getDOMNode component.refs.audio)]
            (set! (.-ontimeupdate audio) #(re-frame/dispatch [:track-time-update {:track-time (.-currentTime audio)
                                                                                  :aid aid}]))
            (set! (.-onended audio) #(re-frame/dispatch [:track-end aid]))))

        :reagent-render
        (fn [track aid volume active? hidden?]
          (if hidden? [:audio {:src (:url track)
                               :ref "audio"
                               :preload "none" }]
            [:div.panel.panel-default { :class (if active? "panel-primary") }
             [:div.panel-heading (str (:title track) " — " (:artist track)) ]
             [:audio {:src (:url track)
                      :ref "audio"
                      :preload "none" }]
             [:div.panel-body
              [:div.progress
               [:div.progress-bar { :style {:width (str (:progress track) "%")}}]]
              [:div.col-lg-2
               [:div.btn-group.btn-group-justified
                (if (:playing? track)
                  [:div.btn.btn-default
                   {:on-click #(re-frame/dispatch [:track-pause aid])  } "Pause"]
                  [:div.btn.btn-default {:on-click  #(re-frame/dispatch [:track-play aid]) } "Play"])]]]]))})))
