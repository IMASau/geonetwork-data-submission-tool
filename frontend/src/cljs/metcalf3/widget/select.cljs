(ns metcalf3.widget.select
  (:require [cljs.spec.alpha :as s]
            [goog.object :as gobj]
            [interop.react-select :refer [ReactSelect* ReactSelectAsync* ReactSelectAsyncCreatable* SelectComponentsOption*]]
            [interop.react-virtualized :refer [ReactWindow*]]
            [metcalf3.utils :as utils]
            [reagent.core :as r]))

(defn normalize-props [props]
  (utils/clj->js* (update props :options utils/clj->js* 1) 1))

(defn ReactSelect [props]
  (ReactSelect* (normalize-props props)))

(defn ReactSelectAsyncCreatable [props]
  (ReactSelectAsyncCreatable* (normalize-props props)))

(defn ReactSelectAsync [props]
  (ReactSelectAsync* (normalize-props props)))

(defn VirtualizedSelect [props]
  (let [{:keys [value options getOptionValue onChange placeholder formatOptionLabel isOptionSelected]} props]

    (s/assert array? options)
    (s/assert ifn? onChange)
    (s/assert ifn? formatOptionLabel)
    (s/assert string? placeholder)
    (s/assert not-empty options)                            ; NOTE: not currently supported

    (ReactSelect*
      #js {:value            value
           :options          options
           :clearable        true
           :searchable       true
           :onChange         onChange
           :getOptionValue   getOptionValue
           :isOptionSelected isOptionSelected
           :placeholder      placeholder
           :styles           #js {:menuPortal (fn [base] (doto (gobj/clone base)
                                                           (gobj/set "zIndex" 9999)))}
           :menuPortalTarget js/document.body
           :components       #js {:Option      (fn [props]
                                                 (let [data (gobj/get props "data")
                                                       props' (doto (gobj/clone props)
                                                                (gobj/set "children" #js [(formatOptionLabel data)]))]
                                                   (SelectComponentsOption* props')))
                                  :SingleValue (fn [props]
                                                 (:term (js->clj (gobj/get props "data") :keywordize-keys true)))
                                  :MenuList    (fn [this]
                                                 (let [children (gobj/get this "children")
                                                       options (gobj/get this "options")
                                                       maxHeight (gobj/get this "maxHeight")
                                                       getValue (gobj/get this "getValue")
                                                       itemCount (.-length children)
                                                       value (getValue)
                                                       height 40
                                                       initialOffset (* height (.indexOf options value))]
                                                   (ReactWindow*
                                                     #js {:children            (fn [props]
                                                                                 (let [index (gobj/get props "index")
                                                                                       style (gobj/get props "style")
                                                                                       child (aget children index)]
                                                                                   (r/as-element [:div {:style style} child])))
                                                          :height              maxHeight
                                                          :itemSize            height
                                                          :itemCount           itemCount
                                                          :initialScrollOffset initialOffset})))}})))

;the actual virtualized implementation. Missing select and hover (kind of important)
(comment (defn VirtualizedSelect [{:keys [props list-props children-renderer] :as args}]
           (ReactSelect
             (merge
               {:components #js {:MenuList (fn [this]
                                             (ReactWindow (merge {:children  (fn [props]
                                                                               ;TODO this is clearly a bad and ugly way to get the option
                                                                               (r/as-element (children-renderer props (nth (js->clj (gobj/get this "options")) (gobj/get props "index")))))
                                                                  :height    400
                                                                  :itemSize  40
                                                                  :itemCount (count (:options props))}
                                                                 list-props)))}}
               props))))