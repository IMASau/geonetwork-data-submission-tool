(ns metcalf.tern.core
  (:require [clojure.string :refer [blank?]]
            [metcalf.common.routing3 :as router]
            [metcalf.tern.config]
            [metcalf.tern.pages :refer [app-root]]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [reagent.dom :as rdom]))

(when-not ^boolean js/goog.DEBUG
  (set! (.-onbeforeunload js/window)
        (fn []
          (when @(rf/subscribe [:subs/get-form-dirty])
            "This will navigate away from the Data Submission Tool and all unsaved work will be lost. Are you sure you want to do this?"))))

(when-let [ele (.getElementById js/document "Content")]
  (when (-> @app-db :page :name nil?)
    (rf/dispatch-sync [::init-db (js->clj (aget js/window "payload") :keywordize-keys true)])
    (router/start! {:iref   app-db
                    :path   [:page :tab]
                    :->hash (fnil name "")
                    :<-hash #(if (blank? %) :data-identification (keyword %))}))
  (rdom/render [app-root] ele))
