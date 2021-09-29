(ns metcalf4.views
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [goog.object :as gobj]
            [interop.react-imask :as react-imask]
            [reagent.core :as r]
            [metcalf3.utils :as utils3]))

; For pure views only, no re-frame subs/handlers

(defn label-template
  [{:keys [label required]}]
  (when label
    [:label label (when required " *")]))

(defn masked-text-widget
  [{:keys [mask value placeholder disabled on-change on-blur]}]
  [react-imask/masked-input
   {:mask        mask
    :disabled    disabled
    :value       value
    :class       "form-control"
    :placeholder placeholder
    :on-change   on-change
    :on-blur     on-blur}])

(defn OptionWidget [props]
  (let [[value display] props]
    [:option {:value value} display]))

(defn Checkbox [props]
  (let [{:keys [label checked on-change disabled help]
         :or   {checked false}} props
        input-control [:input (merge {:type     "checkbox"
                                      :checked  (boolean checked)
                                      :disabled disabled
                                      :onChange on-change})]]
    [:div.form-group {:class (utils3/validation-state props)}
     [:div.checkbox
      [:label input-control label]]
     [:p.help-block help]]))

(defn KeywordsThemeCell [rowData]
  (let [rowData (take-while (complement empty?) rowData)]
    [:div.topic-cell
     [:div.topic-path (string/join " > " (drop-last (rest rowData)))]
     [:div.topic-value (last rowData)]]))

(defn TopicCategoryCell [rowData]
  (let [rowData (take-while (complement empty?) rowData)]
    [:div.topic-cell
     [:div.topic-value (last rowData)]]))

(defn theme-option-renderer
  [props]
  (let [rowData (gobj/get props "rowData")]
    [:div
     [KeywordsThemeCell rowData]]))

(defn ->float [s]
  (let [f (js/parseFloat s)]
    (if (js/isNaN f) nil f)))

(defn breadcrumb-renderer [selected-option]
  (let [text (gobj/get selected-option "breadcrumb")
        term-text (gobj/get selected-option "term")
        alt-label (gobj/get selected-option "altLabel")]
    [:div.topic-cell {:key term-text}
     [:div.topic-path text]
     [:div.topic-value term-text]
     [:div {:style
            {:margin-left 10 :color "#929292" :font-size 11}}
      (if (clojure.string/blank? alt-label) "" (concat "also known as " alt-label))]]))

(defn nasa-list-renderer [option]
  (aget option "prefLabel"))

(defn other-term?
  [term vocabularyTermURL]
  (and (:value term) (empty? (:value vocabularyTermURL))))

(defn Tooltip
  [value]
  [:span " "
   [:i.icon-info-sign.tern-tooltip
    [:span.tern-tooltiptext value]]])

(defn FieldError [{:keys [errors label]}]
  [:span.FieldError label ": " (first errors)])

(defn ManyFieldError [{:keys [errors label]}]
  [:span.FieldError label ": " (or (first errors) "check field errors")])

(defn ResourceConstraints []
  [:div.ResourceConstraints
   [:p.help-block (str "Creative Commons - Attribution 4.0 International. The license allows others to copy,
   distribute, display, and create derivative works provided that they
   credit the original source and any other nominated parties.")]
   [:p [:a {:href   "https://creativecommons.org/licenses/by/4.0/"
            :target "_blank"}
        "https://creativecommons.org/licenses/by/4.0/"]]])

(defn LegacyIECompatibility
  [_ _]
  [:div.LegacyIECompatibility
   [:div.row
    [:div.col-md-6.col-md-offset-3
     [:div.container.box
      [:h1 "Browser not supported"]
      [:p.lead "The work request system doesn't support early versions of Internet Explorer."]
      [:p.lead "Please use Google Chrome to access this system or upgrade your browser."]
      [:hr]
      [:p "Related links:"]
      [:ul
       [:li [:a {:href "http://www.techtimes.com/articles/12659/20140811/dingdong-internet-explorer-8-is-dead-microsoft-will-end-its-life-in-january-2016.htm"}
             "Dingdong, Internet Explorer 8 is dead: Microsoft will end its life in January 2016 (TechTimes)"]
        " and IE 9 will follow a year later."]
       [:li [:a {:href "http://www.computerworld.com/article/2492571/web-apps/google-to-drop-support-for-ie8-on-nov--15.html"}
             "Google to drop support for IE8 on Nov 15 [2012]"]]
       [:li [:a {:href "http://www.w3counter.com/globalstats.php"}
             "Market share of IE8 and IE9 is around 2% each world wide."]]]
      [:br]]]]])
