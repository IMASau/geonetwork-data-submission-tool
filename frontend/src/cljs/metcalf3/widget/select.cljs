(ns metcalf3.widget.select
  (:require cljsjs.react-select
            cljsjs.react-virtualized
            [goog.object :as gobj]
            [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [metcalf3.utils :as utils]
            [reagent.core :as r]))

(def ReactSelect* (js/React.createFactory js/Select))
(def ReactWindow* (js/React.createFactory js/ReactWindow.FixedSizeList))
(def SelectComponents.Option* (js/React.createFactory js/SelectComponents.Option))
(def SelectComponents.ValueContainer* (js/React.createFactory js/SelectComponents.ValueContainer))
(def ReactSelectAsync* (js/React.createFactory js/AsyncSelect))
(def AutoSizer* (js/React.createFactory js/ReactVirtualized.AutoSizer))
(def VirtualScroll* (js/React.createFactory js/ReactVirtualized.VirtualScroll))

(defn normalize-props [props]
  (utils/clj->js* (update props :options utils/clj->js* 1) 1))

(defn ReactWindow [props]
  (ReactWindow* (normalize-props props)))

(defn ReactSelect [props]
  (ReactSelect* (normalize-props props)))

(defn ReactSelectAsync [props]
  (ReactSelectAsync* (normalize-props props)))

(defn AutoSizer [props]
  (AutoSizer* (normalize-props props)))

(defn VirtualScroll [props]
  (VirtualScroll* (normalize-props props)))

(defn VirtualizedSelect [props]
  (let [{:keys [value options getOptionValue onChange placeholder formatOptionLabel]} props]

    (s/assert array? options)
    (s/assert ifn? onChange)
    (s/assert ifn? formatOptionLabel)
    (s/assert string? placeholder)

    (ReactSelect*
      #js {:value            value
           :options          options
           :clearable        true
           :searchable       true
           :onChange         onChange
           :placeholder      placeholder
           :styles           #js {:menuPortal (fn [base] (doto (gobj/clone base)
                                                           (gobj/set "zIndex" 9999)))}
           :menuPortalTarget js/document.body
           :components       #js {:Option   (fn [props]
                                              (let [data (gobj/get props "data")
                                                    props' (doto (gobj/clone props)
                                                             (gobj/set "children" #js [(formatOptionLabel data)]))]
                                                (SelectComponents.Option* props')))
                                  :MenuList (fn [this]
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
(comment (defn VirtualizedSelect [{:keys [props list-props children-renderer] :as args} this]
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