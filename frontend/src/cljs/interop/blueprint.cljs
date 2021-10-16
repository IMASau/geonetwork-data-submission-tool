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