(ns metcalf3.core
  (:require [clojure.string :refer [blank?]]
            [metcalf3.config]
            [metcalf3.fx]
            [metcalf3.handlers]
            [metcalf3.routing :as router]
            [metcalf3.subs]
            [metcalf3.views :refer [AppRoot]]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [reagent.core :as r]))

(when-not ^boolean js/goog.DEBUG
  (set! (.-onbeforeunload js/window)
        (fn []
          (when @(rf/subscribe [:metcalf3/form-dirty?])
            "This will navigate away from the Data Submission Tool and all unsaved work will be lost. Are you sure you want to do this?"))))

(when-let [ele (.getElementById js/document "Content")]
  (when (-> @app-db :page :name nil?)
    (rf/dispatch-sync [:handlers/init-db])
    (router/start! {:iref   app-db
                    :path   [:page :tab]
                    :->hash (fnil name "")
                    :<-hash #(if (blank? %) :data-identification (keyword %))}))
  (r/render [AppRoot] ele))
