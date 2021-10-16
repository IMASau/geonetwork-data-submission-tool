(ns interop.blueprint
  (:require ["@blueprintjs/core" :as BlueprintCore]
            ["@blueprintjs/datetime" :as BlueprintDatetime]
            [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [goog.object :as gobject]
            ["react" :as React]
            [reagent.core :as r]))

(assert BlueprintCore)
(assert BlueprintDatetime)
(assert React)

(defn get-initial-state
  [this]
  {:value (:value (r/props this))})

(defn component-will-receive-props
  [this [_ {:keys [value]}]]
  (when (not= value (:value (r/props this)))
    (r/set-state this {:value value})))

(defn render [view this]
  (let [{:keys [onValueChange] :as props} (r/props this)
        {:keys [value] :as state} (r/state this)]

    (s/assert (s/keys :req-un [::value ::onValueChange]) props)
    (s/assert (s/keys :req-un [::value]) state)
    (s/assert nil? (:onChange props))
    (s/assert nil? (:onBlur props))

    [view
     (-> (dissoc props :onValueChange)
         (assoc :value value)
         (assoc :onChange (fn [e] (r/set-state this {:value (-> e .-target .-value)})))
         (assoc :onBlur #(onValueChange value)))]))

(def navbar (r/adapt-react-class BlueprintCore/Navbar))
(def navbar-group (r/adapt-react-class BlueprintCore/NavbarGroup))
(def navbar-heading (r/adapt-react-class BlueprintCore/NavbarHeading))
(def tab (r/adapt-react-class BlueprintCore/Tab))
(def tabs (r/adapt-react-class BlueprintCore/Tabs))
(def tabs-expander (r/adapt-react-class BlueprintCore/Tabs.Expander))
(def progress-bar (r/adapt-react-class BlueprintCore/ProgressBar))

(def textarea2 (r/adapt-react-class BlueprintCore/TextArea))
(def form-group (r/adapt-react-class BlueprintCore/FormGroup))
(def alignment (js->clj BlueprintCore/Alignment :keywordize-keys true))
(def classes (js->clj BlueprintCore/Classes :keywordize-keys true))