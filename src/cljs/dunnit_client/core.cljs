(ns dunnit-client.core
    (:require-macros [cljs.core.async.macros :refer [go go-loop]])
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [cljsjs.react :as react]
              [cljs.core.async :refer [put! chan <!]]
              [cljs.core.async :as async :refer (<! >! put! chan)]
              [taoensso.sente  :as sente :refer (cb-success?)])
    (:import goog.History))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk" ; Note the same path as before
                                  {:type :auto ; e/o #{:auto :ajax :ws}
                                   })]
  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)   ; Watchable, read-only atom
  )

(enable-console-print!)

(def dunnits (atom []))

(go-loop []
  (print "Loop Started")
    (let [[event msg] (:event (<! ch-chsk))]
      (print event msg (type (last msg)))
      (if (and (= event :chsk/recv) (not (nil? msg)))
        (swap! dunnits #(conj @dunnits msg)))
      (recur)))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to dunnit-client"]
   [:div [:a {:href "#/about"} "go to about page"]]
   [:div [:a {:href "#/dunnit"} "go to dunnit dashboard"]]])

(defn about-page []
  [:div [:h2 "About dunnit-client"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn dunnit-dashboard []
  [:div [:h2 "Dunnit Dashboard"]
   [:div [:h4 "Dunnits"]
    [:ul
     (for [item @dunnits]
       [:li (str "Email: " (:emailAddress (last item)) " HistoryId: " (:historyId (last item)))])]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "/dunnit" []
                    (session/put! :current-page #'dunnit-dashboard))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
