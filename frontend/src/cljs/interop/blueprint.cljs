(ns interop.blueprint
  (:require [reagent.core :as r]
            [cljsjs.blueprint.datetime]
            [cljsjs.blueprint.core]
            [cljsjs.blueprint.select]
            [cljsjs.blueprint.table]
            [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [oops.core :refer [gget]]
            [goog.object :as gobject]))

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

(defn build-controlled-value
  [view]
  (r/create-class
    {:get-initial-state            get-initial-state
     :component-will-receive-props component-will-receive-props
     :render                       (partial render view)}))

(def navbar (r/adapt-react-class (gget "BlueprintCore.Navbar")))
(def popover (r/adapt-react-class (gget "BlueprintCore.Popover")))
(def collapse (r/adapt-react-class (gget "BlueprintCore.Collapse")))
(def navbar-group (r/adapt-react-class (gget "BlueprintCore.NavbarGroup")))
(def navbar-divider (r/adapt-react-class (gget "BlueprintCore.NavbarDivider")))
(def navbar-heading (r/adapt-react-class (gget "BlueprintCore.NavbarHeading")))
(def alert (r/adapt-react-class (gget "BlueprintCore.Alert")))
(def spinner (r/adapt-react-class (gget "BlueprintCore.Spinner")))
(def card (r/adapt-react-class (gget "BlueprintCore.Card")))
(def suggest (r/adapt-react-class (gget "BlueprintSelect.Suggest")))
(def menu (r/adapt-react-class (gget "BlueprintCore.Menu")))
(def menu-item (r/adapt-react-class (gget "BlueprintCore.MenuItem")))
(def menu-divider (r/adapt-react-class (gget "BlueprintCore.MenuDivider")))
(def divider (r/adapt-react-class (gget "BlueprintCore.Divider")))

(def checkbox (r/adapt-react-class (gget "BlueprintCore.Checkbox")))
(def button (r/adapt-react-class (gget "BlueprintCore.Button")))
(def dialog (r/adapt-react-class (gget "BlueprintCore.Dialog")))
(def table (r/adapt-react-class (gget "BlueprintTable.Table")))
(def column (r/adapt-react-class (gget "BlueprintTable.Column")))
(def cell (r/adapt-react-class (gget "BlueprintTable.Cell")))
(def tag (r/adapt-react-class (gget "BlueprintCore.Tag")))
(def tab (r/adapt-react-class (gget "BlueprintCore.Tab")))
(def tabs (r/adapt-react-class (gget "BlueprintCore.Tabs")))
(def tabs-expander (r/adapt-react-class (gget "BlueprintCore.Tabs.Expander")))
(def non-ideal-state (r/adapt-react-class (gget "BlueprintCore.NonIdealState")))
(def progress-bar (r/adapt-react-class (gget "BlueprintCore.ProgressBar")))

(def input-group (r/adapt-react-class (gget "BlueprintCore.InputGroup")))
(def html-select (r/adapt-react-class (gget "BlueprintCore.HTMLSelect")))
(def date-input (r/adapt-react-class (gget "BlueprintDatetime.DateInput")))
(def date-picker (r/adapt-react-class (gget "BlueprintDatetime.DatePicker")))
(def icon (r/adapt-react-class (gget "BlueprintCore.Icon")))
(def textarea (build-controlled-value (r/adapt-react-class (gget "BlueprintCore.TextArea"))))
(def textarea2 (r/adapt-react-class (gget "BlueprintCore.TextArea")))
(def label (r/adapt-react-class (gget "BlueprintCore.Label")))
(def form-group (r/adapt-react-class (gget "BlueprintCore.FormGroup")))
(def control-group (r/adapt-react-class (gget "BlueprintCore.ControlGroup")))
(def time-picker (r/adapt-react-class (gget "BlueprintDatetime.TimePicker")))
(def hotkeys (r/adapt-react-class (gget "BlueprintCore.Hotkeys")))
(def hotkey (r/adapt-react-class (gget "BlueprintCore.Hotkey")))
(def resize-sensor (r/adapt-react-class (gget "BlueprintCore.ResizeSensor")))

(defn hotkeys-target
  [view hotkeys-view]
  (let [MyReact (fn [props context updater]
                  (this-as this
                    (js/React.Component.call this props context updater)))]

    (gobject/extend (.-prototype MyReact) js/React.Component.prototype
                    #js {:renderHotkeys (fn [_] (r/as-element hotkeys-view))
                         :render        (fn [_] (r/as-element view))})

    (r/adapt-react-class (js/BlueprintCore.HotkeysTarget MyReact))))

(def alignment (js->clj js/BlueprintCore.Alignment :keywordize-keys true))
(def classes (js->clj js/BlueprintCore.Classes :keywordize-keys true))
(def intent (js->clj js/BlueprintCore.Intent :keywordize-keys true))
(def time-precision (js->clj js/BlueprintDatetime.TimePrecision :keywordize-keys true))

(defn numeric-input
  [_]
  (letfn [(render [this]
            (let [{:keys [onValueChange] :as props} (r/props this)
                  {:keys [value] :as state} (r/state this)]

              (s/assert (s/keys :req-un [::value ::onValueChange]) props)
              (s/assert (s/keys :req-un [::value]) state)
              (s/assert (s/nilable (s/or :s string? :n number?)) value)

              [(r/adapt-react-class js/BlueprintCore.NumericInput)
               (-> props
                   (assoc :value (or value ""))
                   (assoc :onValueChange (fn [n s] (onValueChange (when-not (string/blank? s) n)))))]))]

    (r/create-class
      {:get-initial-state            get-initial-state
       :component-will-receive-props component-will-receive-props
       :render                       render})))


(defn html-input [props] [:input.bp3-input (merge {:class (:INPUT classes)} props)])
(def html-input2 (build-controlled-value html-input))


(defn button-checkbox
  [{:keys [states value on-change disabled]}]
  (let [[state next-state] (drop-while (fn [state] (not= value (:value state))) (conj states (first states)))]
    (s/assert (s/keys :req-un [::intent ::icon ::value]) state)
    (s/assert (s/keys :req-un [::intent ::icon ::value]) next-state)
    [button
     {:intent   (:intent state)
      :icon     (:icon state)
      :onClick  #(on-change (:value next-state))
      :small    true
      :disabled disabled}]))