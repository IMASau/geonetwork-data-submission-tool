(ns metcalf3.widget.select
  (:require cljsjs.react-select
            cljsjs.react-virtualized
            [goog.object :as gobj]
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

(defn render-menu [this raw-args]
  (let [{:keys [focusedOption focusOption labelKey options selectValue valueArray]} (utils/js-lookup raw-args)
        {:keys [maxHeight optionHeight optionRenderer]} (r/props this)
        focusedOptionIndex (.indexOf options focusedOption)
        n-options (count options)
        height (js/Math.min maxHeight (* optionHeight n-options))

        default-option-renderer
        (fn [{:keys [focusedOption focusOption labelKey option selectValue]}]
          (let [{:keys [optionHeight]} (r/props this)
                className (if (identical? option focusedOption)
                            "VirtualizedSelectOption VirtualizedSelectFocusedOption"
                            "VirtualizedSelectOption")]
            (r/as-element [:div {:class         className
                                 :on-click      #(selectValue option)
                                 :on-mouse-over #(focusOption option)
                                 :style         {:height optionHeight}}
                           (aget option labelKey)])))

        innerRowRenderer (or optionRenderer default-option-renderer)

        wrapped-row-renderer
        (fn [args]
          (let [idx (aget args "index")
                option (aget options idx)]
            (innerRowRenderer
              (utils/js-lookup! #js {:focusedOption      focusedOption
                                     :focusedOptionIndex focusedOptionIndex
                                     :focusOption        focusOption
                                     :labelKey           labelKey
                                     :option             option
                                     :options            options
                                     :optionHeight       optionHeight
                                     :selectValue        selectValue
                                     :valueArray         valueArray}))))]

    (AutoSizer
      {:disableHeight true
       :children      (fn [args]
                        (let [{:keys [width]} (utils/js-lookup args)]
                          (VirtualScroll {:className        "VirtualSelectGrid"
                                          :height           height
                                          :overscanRowCount 0
                                          :rowCount         n-options
                                          :rowHeight        optionHeight
                                          :rowRenderer      wrapped-row-renderer
                                          :scrollToIndex    focusedOptionIndex
                                          :width            width})))})))

(defn VirtualizedSelect [props this]
  (ReactSelect (merge {:clearable    true
                       :searchable   true
                       :menuRenderer #(render-menu this %)
                       :menuStyle    #js {:overflow "hidden"}}
                      props)))

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