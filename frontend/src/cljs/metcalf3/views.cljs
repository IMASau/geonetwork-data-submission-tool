(ns metcalf3.views
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :as async :refer [<! chan put!]]
            [clojure.edn :as edn]
            [clojure.set :as set]
            [clojure.string :as string :refer [blank?]]
            [goog.events :as gevents]
            [goog.object :as gobj]
            [goog.style :as gstyle]
            [goog.userAgent :as guseragent]
            [interop.blueprint :as bp3]
            [interop.cljs-ajax :as ajax]
            [interop.cuerdas :as cuerdas]
            [interop.fixed-data-table-2 :refer [Cell Column Table]]
            [interop.moment :as moment]
            [interop.react-imask :as react-imask]
            [metcalf3.content :refer [contact-groups]]
            [metcalf3.handlers :as handlers3]
            [metcalf3.logic :as logic3]
            [metcalf3.utils :as utils3]
            [metcalf3.widget.modal :refer [Modal]]
            [metcalf3.widget.tree :refer [TermList TermTree]]
            [metcalf4.low-code :as low-code]
            [re-frame.core :as rf]
            [reagent.core :as r])
  (:import [goog.dom ViewportSizeMonitor]
           [goog.events FileDropHandler]
           [goog.events EventType]))

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

(defn InputWidget
  [_]
  (letfn [(init-state [this]
            (let [{:keys [value]} (r/props this)]
              {:input-value value}))

          (component-will-receive-props [this new-argv]
            (let [[_ next-props] new-argv
                  props (r/props this)]
              (utils3/on-change props next-props [:value] #(r/set-state this {:input-value %}))))

          (render [this]
            (let [{:keys [addon-before addon-after help on-change disabled mask maxlength] :as props} (r/props this)
                  {:keys [input-value]} (r/state this)
                  input-props (-> props
                                  (dissoc :show-errors)
                                  (assoc :value (or input-value ""))
                                  (dissoc :maxlength)
                                  (assoc :maxLength maxlength)
                                  (assoc :on-change #(r/set-state this {:input-value (.. % -target -value)}))
                                  (assoc :on-blur #(on-change input-value))
                                  (assoc :key "ifc"))]
              [:div.form-group {:class    (utils3/validation-state props)
                                :disabled disabled}
               (label-template props)
               (if (or addon-after addon-before)
                 [:div.input-group {:key "ig"} addon-before [:input.form-control input-props] addon-after]
                 (if mask
                   [masked-text-widget input-props]
                   [:input.form-control input-props]))
               [:p.help-block help]]))]
    (r/create-class
      {:get-initial-state            init-state
       :component-will-receive-props component-will-receive-props
       :render                       render})))

;(defn NameInputWidget
;  [_]
;  (letfn [(init-state [this]
;            (let [{:keys [value]} (r/props this)]
;              {:input-value value}))
;
;          (handle-change [this s]
;            (let [s (utils3/filter-name s)]
;              (r/set-state this {:input-value s})))
;
;          (handle-blur [this]
;            (let [{:keys [on-change]} (r/props this)
;                  {:keys [input-value]} (r/state this)]
;              (on-change input-value)))
;
;          (component-will-receive-props [this new-argv]
;            (let [[_ next-props] new-argv
;                  props (r/props this)]
;              (utils3/on-change props next-props [:value] #(r/set-state this {:input-value %}))))
;
;          (render [this]
;            (let [{:keys [addon-before addon-after help disabled mask] :as props} (r/props this)
;                  {:keys [input-value]} (r/state this)
;                  input-props (-> props
;                                  (dissoc :show-errors)
;                                  (assoc :maxLength (:maxlength props))
;                                  (dissoc :maxlength)
;                                  (assoc :value (or input-value ""))
;                                  (assoc :on-change #(handle-change this (.. % -target -value)))
;                                  (assoc :on-blur #(handle-blur this))
;                                  (assoc :key "ifc"))]
;              [:div.form-group {:class    (utils3/validation-state props)
;                                :disabled disabled}
;               (label-template props)
;               (if (or addon-after addon-before)
;                 [:div.input-group {:key "ig"} addon-before [:input.form-control input-props] addon-after]
;                 (if mask
;                   [masked-text-widget input-props]
;                   [:input.form-control input-props]))
;               [:p.help-block help]]))]
;    (r/create-class
;      {:get-initial-state            init-state
;       :component-will-receive-props component-will-receive-props
;       :render                       render})))

(defn SimpleInputWidget
  [{:keys [value addon-before addon-after help on-change disabled] :as props} _]
  (let [input-props (assoc props
                      :value (or value "")
                      :on-change #(on-change (.. % -target -value))
                      :key "ifc")]
    [:div.form-group {:class    (utils3/validation-state props)
                      :disabled disabled}
     (label-template props)
     (if (or addon-after addon-before)
       [:div.input-group {:key "ig"} addon-before [:input.form-control input-props] addon-after]
       [:input.form-control input-props])
     [:p.help-block help]]))

; TODO: Consider input-field-with-label
(defn InputField
  [{:keys [path] :as props}]

  (let [field @(rf/subscribe [:subs/get-derived-path path])]
    [InputWidget (-> field
                     (merge (dissoc props :path))
                     (assoc
                       :on-change #(rf/dispatch [::value-changed path %])))]))

; TODO: Consider date-field-with-label
(defn OptionWidget [props]
  (let [[value display] props]
    [:option {:value value} display]))

(defn SelectWidget [props]
  (let [{:keys [label required value help disabled errors is-hidden on-change
                options default-option default-value loading
                show-errors]
         :or   {is-hidden false}} props
        disabled (or disabled loading)
        default-value (or default-value "")
        default-option (or default-option "Please select")]
    (when-not is-hidden
      [:div.form-group {:class (when (and show-errors (seq errors))
                                 "has-error")}
       (when label [:label label (when required " *")])
       (vec (concat
              [:select.form-control (assoc (dissoc props :default-option :show-errors :is-hidden)
                                      :on-change #(on-change (-> % .-target .-value))
                                      :value (or value default-value)
                                      :disabled disabled)
               (when options
                 [:option {:value default-value :disabled true} default-option])]
              (for [option options]
                [OptionWidget option])))
       (when help [:p.help-block help])])))

(defn SelectField
  [{:keys [path]}]
  (let [{:keys [options default-option disabled] :as field} @(rf/subscribe [:subs/get-derived-path path])]
    [SelectWidget (assoc field
                    :class "wauto"
                    :disabled (or disabled (empty? options))
                    :default-option (if-not (empty? options) default-option "")
                    :on-blur #(rf/dispatch [::select-field-blur path])
                    :on-change #(rf/dispatch [::value-changed path %]))]))

;(defn textarea-widget
;  [{:keys [label labelInfo helperText maxlength value disabled change-v intent placeholder]}]
;  [bp3/form-group
;   {:label      label
;    :labelInfo  labelInfo
;    :helperText helperText
;    :intent     intent}
;   [bp3/textarea2
;    {:key            @(rf/subscribe [::get-textarea-widget-key])
;     :growVertically true
;     :onBlur         #(rf/dispatch (conj change-v (-> % .-target .-value)))
;     :disabled       disabled
;     :placeholder    placeholder
;     :maxLength      maxlength
;     :defaultValue   value
;     :fill           true
;     :intent         intent}]])

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

(defn getter [k row] (get row k))

(defn update-table-width [this]
  (let [{:keys [autowidth-id]} (r/state this)]
    (when-let [ele (js/document.getElementById autowidth-id)]
      (r/set-state this {:width (.-width (gstyle/getSize ele))}))))

(defn KeywordsThemeCell [rowData]
  (let [rowData (take-while (complement empty?) rowData)]
    [:div.topic-cell
     [:div.topic-path (string/join " > " (drop-last (rest rowData)))]
     [:div.topic-value (last rowData)]]))

(defn KeywordsThemeTable
  [{:keys [keyword-type keywords-path]}]
  (letfn [(init-state [_]
            {:columnWidths     [26 (- 900 26)]
             :isColumnResizing false
             :query            ""
             ;:selected-filter  false
             :width            900
             :scrollToRow      0
             :autowidth-id     (name (gensym "autowidth"))})
          (did-mount [this]
            (let [vsm (ViewportSizeMonitor.)]
              (gevents/listen vsm EventType.RESIZE #(update-table-width this))
              (update-table-width this)))
          (render [this]
            (let [{:keys [query width columnWidths isColumnResizing scrollToRow autowidth-id]} (r/state this)
                  keywords @(rf/subscribe [:subs/get-derived-path keywords-path])
                  uuids (zipmap (map :value (:value keywords)) (range))
                  table @(rf/subscribe [:subs/get-derived-path [:theme keyword-type :table]])
                  results (if (blank? query)
                            table
                            (vec (utils3/filter-table false table query)))
                  rowHeight 50
                  on-submit #(r/set-state this {:scrollToRow 0 :query %})]
              [:div.KeywordsThemeTable
               [SimpleInputWidget
                {:label     "Search"
                 :value     query
                 :on-change #(on-submit %)}]
               (if (> (count results) 0)
                 [:div {:id autowidth-id}
                  [Table
                   {:width                     width
                    :maxHeight                 400
                    :rowHeight                 rowHeight
                    :rowsCount                 (count results)
                    :headerHeight              30
                    :onColumnResizeEndCallback #(do (r/set-state this {[:columnWidths %2] (max %1 5)})
                                                    (r/set-state this {:isColumnResizing false}))
                    :overflowX                 "hidden"
                    :scrollToRow               scrollToRow
                    :onScrollEnd               #(r/set-state this {:scrollToRow (quot %2 rowHeight)})
                    :isColumnResizing          isColumnResizing}
                   [Column
                    {:label          (r/as-element [Cell ""])
                     :dataKey        0
                     :align          "right"
                     :cellDataGetter getter
                     :cell           (fn [props]
                                       (let [rowIndex (gobj/get props "rowIndex")
                                             uuid (first (get results rowIndex))]
                                         (r/as-element [Cell [Checkbox {:checked   (contains? uuids uuid)
                                                                        :on-change (fn [_]
                                                                                     (if (contains? uuids uuid)
                                                                                       (rf/dispatch [::del-value keywords-path (uuids uuid)])
                                                                                       (rf/dispatch [::keywords-theme-table-add-value keywords-path uuid])))}]])))
                     :width          (get columnWidths 0)
                     :isResizable    true}]
                   [Column
                    {:header         (r/as-element [Cell "Topic"])
                     :cellDataGetter getter
                     :dataKey        1
                     :cell           (fn [props]
                                       (let [rowIndex (gobj/get props "rowIndex")
                                             rowData (get results rowIndex)]
                                         (r/as-element [Cell [KeywordsThemeCell rowData]])))
                     :flexGrow       1
                     :width          (get columnWidths 1)
                     :isResizable    true}]]]
                 [:div.no-results "No results found."])
               [:p "There are " (count table) " keywords in our database"]]))]
    (r/create-class
      {:get-initial-state   init-state
       :component-did-mount did-mount
       :render              render})))

(defn modal-dialog-table-modal-edit-form
  [{:keys [form path title]}]
  (let [many-field-path (drop-last 2 path)]

    (letfn [(handle-delete-confirm []
              (rf/dispatch [::del-value many-field-path (last path)])
              (rf/dispatch [::close-modal]))

            (handle-delete-click [e]
              (.preventDefault e)
              (rf/dispatch [::open-modal {:type       :confirm
                                          :title      "Delete " title "?"
                                          :message    "Are you sure you want to delete?"
                                          :on-confirm handle-delete-confirm}]))

            (handle-close-click []
              (rf/dispatch [::close-modal]))]

      [Modal {:ok-copy      "Done"
              :modal-header [:span [:span.glyphicon.glyphicon-list] " Edit " title]
              :modal-body   [form path]
              :modal-footer [:div
                             [:a.btn.text-danger.pull-left
                              {:on-click handle-delete-click}
                              [:span.glyphicon.glyphicon-remove] " Delete"]
                             [:button.btn.btn-primary {:on-click handle-close-click} "Done"]]
              :on-dismiss   handle-close-click
              :on-save      handle-close-click}])))

(defn modal-dialog-table-modal-add-form
  [{:keys [form path title]}]
  (let [many-field-path (drop-last 2 path)
        handle-cancel (fn [] (rf/dispatch [::del-value many-field-path (last path)])
                        (rf/dispatch [::close-modal]))]
    [Modal {:ok-copy      "Done"
            :modal-header [:span [:span.glyphicon.glyphicon-list] " Add " title]
            :modal-body   [form path]
            :on-dismiss   handle-cancel
            :on-cancel    handle-cancel
            :on-save      #(rf/dispatch [::close-modal])}]))

(defn TableModalEdit
  [{:keys [ths tds-fn form title field-path placeholder default-field on-new-click add-label]
    :or   {tds-fn    #(list (:value %))
           add-label "Add new"}}]
  (let [{:keys [disabled] :as many-field} @(rf/subscribe [:subs/get-derived-path field-path])]

    (letfn [(edit! [field-path]
              (rf/dispatch [::open-modal {:type  :TableModalEditForm
                                          :title title
                                          :form  form
                                          :path  field-path}]))

            (new! [default-field]
              (let [values-ref (:value many-field)
                    values-len (count values-ref)
                    values-path (conj field-path :value)
                    new-field-path (conj values-path values-len)]
                (if on-new-click
                  (on-new-click {:default-field default-field
                                 :many-field    many-field
                                 :type          :TableModalAddForm
                                 :title         title
                                 :form          form
                                 :path          new-field-path})
                  (do (if default-field
                        (rf/dispatch [::table-modal-edit-add-field field-path default-field])
                        (rf/dispatch [::table-modal-edit-new-field field-path]))
                      (rf/dispatch [::open-modal {:type  :TableModalAddForm
                                                  :title title
                                                  :form  form
                                                  :path  new-field-path}])))))]

      [:div.TableInlineEdit
       (when-let [help (:help many-field)]
         [:p.help-block help])
       (if (or (not placeholder) (-> many-field :value count pos?))
         [:table.table {:class (when-not (or disabled (empty? (:value many-field))) "table-hover")}
          (when ths
            [:thead (-> [:tr]
                        (into (for [th ths] [:th th]))
                        (conj [:th.xcell " "]))])
          (if (not-empty (:value many-field))
            (into [:tbody]
                  (for [[idx field] (map-indexed vector (:value many-field))]
                    (let [field-path (conj field-path :value idx)
                          has-error (not (logic3/is-valid? {:fields (:value field)}))]
                      (-> [:tr.clickable-text {:class    (when has-error "warning")
                                               :ref      (str field-path)
                                               :on-click (when-not disabled
                                                           #(edit! field-path))}]
                          (into (for [td-value (tds-fn field)]
                                  [:td (if (empty? td-value) [:span {:style {:color "#ccc"}} "--"] td-value)]))
                          (conj [:td.xcell
                                 (when has-error
                                   [:span.glyphicon.glyphicon-alert.text-danger])])))))
            [:tbody (-> [:tr.noselect {:on-click #(when-not disabled (new! default-field))}]
                        (into (for [_ (or ths [nil])] [:td {:style {:color "#ccc"}} "--"]))
                        (conj [:td.xcell]))])]
         [:div {:style {:margin-bottom "1em"}} placeholder])
       (when-not disabled
         [:button.btn.btn-primary.btn-sm
          {:on-click #(new! default-field)}
          [:span.glyphicon.glyphicon-plus] " " add-label])])))

(defn modal-dialog-theme-keywords
  [{:keys [keyword-type keywords-path]}]
  [Modal {:ok-copy      "Done"
          :dialog-class "modal-lg"
          :modal-header [:span [:span.glyphicon.glyphicon-list] " " "Research theme keywords"]
          :modal-body   [:div
                         [:p.help-block "Select keyword(s) to add to record"]
                         [KeywordsThemeTable
                          {:keyword-type  keyword-type
                           :keywords-path keywords-path}]]
          :on-dismiss   #(rf/dispatch [::close-modal])
          :on-save      #(rf/dispatch [::close-modal])}])

(defprotocol IPrintNice
  (print-nice [x]))

(extend-protocol IPrintNice
  number
  (print-nice [x] (.toFixed x 3))
  object
  (print-nice [x] (pr-str x))
  nil
  (print-nice [x] "--"))

;(defn breadcrumb-renderer [selected-option]
;  (let [text (gobj/get selected-option "breadcrumb")
;        term-text (gobj/get selected-option "term")
;        alt-label (gobj/get selected-option "altLabel")]
;    [:div.topic-cell {:key term-text}
;     [:div.topic-path text]
;     [:div.topic-value term-text]
;     [:div {:style
;            {:margin-left 10 :color "#929292" :font-size 11}}
;      (if (clojure.string/blank? alt-label) "" (concat "also known as " alt-label))]]))

;(defn NasaListSelectField
;  [_]
;  (letfn [(will-mount [this]
;            (let [{:keys [keyword]} (r/props this)]
;              (rf/dispatch [::load-api-options3 [:api keyword]])))
;          (render [this]
;            (let [{:keys [keyword path]} (r/props this)
;                  path (conj path keyword)
;                  value-path (conj path :value 0 :value)
;                  {:keys [options]} @(rf/subscribe [:subs/get-derived-path [:api keyword]])
;                  {:keys [prefLabel uri]} @(rf/subscribe [:subs/get-derived-path value-path])
;                  path-value @(rf/subscribe [:subs/get-derived-path path])
;                  {:keys [label help required errors show-errors]} path-value]
;              [:div
;               (when label [:label label (when required " *")])
;               [:div.flex-row
;                [:div.flex-row-field
;                 [:div.form-group {:class (when (and show-errors (seq errors)) "has-error")}
;                  (ReactSelect
;                    {:value             #js {:uri (:value uri) :prefLabel (:value prefLabel)}
;                     :options           options
;                     :is-searchable     true
;                     :getOptionValue    (fn [option]
;                                          (gobj/get option "prefLabel"))
;                     :formatOptionLabel (fn [option]
;                                          (r/as-element (aget option "prefLabel")))
;                     :onChange          (fn [option]
;                                          (rf/dispatch [::nasa-list-select-field-change value-path option]))
;                     :noResultsText     "No results found.  Click browse to add a new entry."})
;                  [:p.help-block help]]]]]))]
;    (r/create-class
;      {:component-will-mount will-mount
;       :render               render})))

;(defn Tooltip
;  [value]
;  [:span " "
;   [:i.icon-info-sign.tern-tooltip
;    [:span.tern-tooltiptext value]]])

;(defn elasticsearch-select-field
;  [_]
;  (letfn [(will-mount [this]
;            (let [{:keys [api-path]} (r/props this)]
;              (rf/dispatch [::elasticsearch-select-field-mount api-path ""])))
;          (render [this]
;            (let [{:keys [dp-type dp-term-path api-path disabled]} (r/props this)
;                  sub-paths (utils3/dp-term-paths dp-type)
;                  {:keys [options]} @(rf/subscribe [:subs/get-derived-path api-path])
;                  term @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:term sub-paths))])
;                  vocabularyTermURL @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:vocabularyTermURL sub-paths))])
;                  {:keys [label help required errors show-errors tooltip]} term
;                  selectable-options (into-array (filterv #(gobj/get % "is_selectable") options))
;                  new-term? (utils3/other-term? term vocabularyTermURL)]
;              [:div
;               (when new-term?
;                 [:span.pull-right.new-term.text-primary
;                  [:span.glyphicon.glyphicon-asterisk]
;                  " New term"])
;               [:div.flex-row
;                [:div.flex-row-field
;                 [:div.form-group {:class (when (and show-errors (seq errors)) "has-error")}
;                  (when label
;                    [:label label
;                     (when required " *")
;                     (when tooltip [Tooltip tooltip])])
;                  (if-not new-term?
;                    (ReactSelect
;                      {:value             (if (blank? (:value vocabularyTermURL))
;                                            nil
;                                            #js {:vocabularyTermURL (:value vocabularyTermURL) :term (:value term)})
;                       :options           selectable-options
;                       :placeholder       (:placeholder term)
;                       :isClearable       true
;                       :is-searchable     true
;                       :onInputChange     (fn [query]
;                                            (rf/dispatch [::elasticsearch-select-field-input-change api-path query])
;                                            query)
;                       :getOptionValue    (fn [option]
;                                            (gobj/get option "term"))
;                       :formatOptionLabel (fn [props]
;                                            (r/as-element (breadcrumb-renderer props)))
;                       :filterOption      (fn [_ _]
;                                            ; Return true always. This allows for matches on label as well as altLabel (or other fields available in the REST API).
;                                            (boolean 0))
;                       :onChange          (fn [option]
;                                            (rf/dispatch [::update-dp-term dp-term-path sub-paths option]))
;                       :noResultsText     "No results found.  Click browse to add a new entry."
;                       :isDisabled        disabled})
;
;                    (ReactSelect
;                      {:value             #js {:vocabularyTermURL "(new term)" :term (:value term)}
;                       :options           selectable-options
;                       :placeholder       (:placeholder term)
;                       :is-searchable     true
;                       :isClearable       true
;                       :getOptionValue    (fn [option]
;                                            (gobj/get option "term"))
;                       :formatOptionLabel (fn [props]
;                                            (r/as-element (breadcrumb-renderer props)))
;                       :onChange          (fn [option]
;                                            (rf/dispatch [::update-dp-term dp-term-path sub-paths option]))
;                       :noResultsText     "No results found.  Click browse to add a new entry."}))
;                  [:p.help-block help]]]
;                ; TODO: Re-enable this in the future to browse/create vocabulary terms.
;                ;                  [:div.flex-row-button
;                ;                   [:button.btn.btn-default
;                ;                    {:style    {:vertical-align "top"}
;                ;                     :on-click #(rf/dispatch [:handlers/open-modalv
;                ;                                              {:type         param-type
;                ;                                               :api-path     api-path
;                ;                                               :dp-term-path dp-term-path}])}
;                ;                    [:span.glyphicon.glyphicon-edit] " Custom"]
;                ;                   (when help [:p.help-block {:dangerouslySetInnerHTML {:__html "&nbsp;"}}])]
;                ]]))]
;    (r/create-class
;      {:component-will-mount will-mount
;       :render               render})))

(defn PersonListWidget
  [_]
  (letfn [(will-mount [this]
            (let [{:keys [api-path]} (r/props this)]
              (rf/dispatch [::load-api-options3 api-path])))
          (render [this]
            (let [{:keys [api-path value on-change]} (r/props this)]
              (when-let [{:keys [options]} @(rf/subscribe [:subs/get-derived-path api-path])]
                ; TODO: review performance
                (let [options (js->clj options :keywordize-keys true)
                      options (into [] (map #(assoc % :is_selectable true) options))
                      option-value (first (filter #(-> % :uri (= value)) options))]
                  [TermList
                   {:value       option-value
                    :display-key :prefLabel
                    :value-key   :uri
                    :options     options
                    :on-select   on-change}]))))]
    (r/create-class
      {:component-will-mount will-mount
       :render               render})))

(defn ApiTreeWidget
  [_]
  (letfn [(will-mount [this]
            (let [{:keys [api-path]} (r/props this)]
              (rf/dispatch [::load-api-options3 api-path])))
          (render [this]
            (let [{:keys [api-path value on-change]} (r/props this)]
              (when-let [{:keys [options]} @(rf/subscribe [:subs/get-derived-path api-path])]
                ; TODO: review performance
                (let [options (doall (js->clj options :keywordize-keys true))
                      option-value (first (filter #(-> % :URI (= value)) options))]
                  [TermTree
                   {:value     option-value
                    :value-key :URI
                    :options   options

                    :on-select on-change}]))))]
    (r/create-class
      {:component-will-mount will-mount
       :render               render})))

;(defn ApiTermTreeField
;  [{:keys [api-path dp-term-path dp-type]}]
;  (let [sub-paths (utils3/dp-term-paths dp-type)
;        term @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:term sub-paths))])
;        vocabularyTermURL @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:vocabularyTermURL sub-paths))])
;        {:keys [label required errors show-errors]} term
;        {:keys [options]} @(rf/subscribe [:subs/get-derived-path api-path])]
;    [:div.form-group {:class (when (and show-errors (seq errors)) "has-error")}
;     (when label [:label label (when required " *")])
;     [ApiTreeWidget
;      {:api-path  api-path
;       :value     (:value vocabularyTermURL)
;       :options   options
;       :on-change (fn [option]
;                    (rf/dispatch [::update-dp-term dp-term-path sub-paths option]))}]
;     [:p.help-block "There are " (count options) " terms in this vocabulary"]]))

;(defn TermOrOtherForm
;  "docstring"
;  [{:keys [dp-term-path dp-type] :as props}]
;  (let [sub-paths (utils3/dp-term-paths dp-type)
;        term @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:term sub-paths))])
;        vocabularyTermURL @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:vocabularyTermURL sub-paths))])]
;    [:div
;     [:p "Select a term from the vocabulary"]
;     [ApiTermTreeField props]
;     [:p "Or define your own"]
;     [InputWidget
;      (assoc term
;        :value (if (utils3/other-term? term vocabularyTermURL) (:value term) "")
;        :on-change (fn [v]
;                     (rf/dispatch [::update-dp-term dp-term-path sub-paths #js {:term              v
;                                                                                :vocabularyTermURL "http://linkeddata.tern.org.au/XXX"}]))
;        :placeholder ""
;        :maxlength 100)]]))

;(defn UnitTermOrOtherForm
;  "docstring"
;  [{:keys [dp-term-path dp-type]}]
;  (let [sub-paths (utils3/dp-term-paths dp-type)
;        term @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:term sub-paths))])
;        vocabularyTermURL @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:vocabularyTermURL sub-paths))])]
;    [:div
;     [:p "Define your own unit"]
;     [InputWidget
;      (assoc term
;        :value (if (utils3/other-term? term vocabularyTermURL) (:value term) "")
;        :on-change (fn [v]
;                     (rf/dispatch [::update-dp-term dp-term-path sub-paths #js {:term              v
;                                                                                :vocabularyTermURL "http://linkeddata.tern.org.au/XXX"}]))
;        :placeholder ""
;        :maxlength 100)]]))

(defn person-list-field
  [{:keys [api-path person-path]}]
  (let [{:keys [name uri]} @(rf/subscribe [:subs/get-derived-path person-path])
        {:keys [label required errors show-errors]} name
        {:keys [options]} @(rf/subscribe [:subs/get-derived-path api-path])]
    [:div.form-group {:class (when (and show-errors (seq errors)) "has-error")}
     (when label [:label label (when required " *")])
     [PersonListWidget
      {:api-path  api-path
       :value     (:value uri)
       :options   options
       :on-change (fn [option]
                    (rf/dispatch [::person-list-field-change person-path option]))}]
     [:p.help-block "There are " (count options) " terms in this vocabulary"]]))

;(defn PersonForm
;  "docstring"
;  [props]
;  (let [{:keys [path]} props
;        props {:person-path path
;               :api-path    [:api :api/person]}]
;    [:div
;     [:p "Select a person"]
;     [person-list-field props]]))

;(defn modal-dialog-parametername
;  [props]
;  [Modal {:ok-copy      "Done"
;          :modal-header [:span [:span.glyphicon.glyphicon-list] " " "Browse parameter names"]
;          :modal-body   [TermOrOtherForm (assoc props :dp-type :longName)]
;          :on-dismiss   #(rf/dispatch [::close-modal])
;          :on-save      #(rf/dispatch [::close-modal])}])

;(defn modal-dialog-parameterunit
;  [props]
;  [Modal {:ok-copy      "Done"
;          :modal-header [:span [:span.glyphicon.glyphicon-list] " " "Add parameter unit"]
;          :modal-body   [:div [UnitTermOrOtherForm (-> props
;                                                       (assoc :sort? false)
;                                                       (assoc :dp-type :unit))]]
;          :on-dismiss   #(rf/dispatch [::close-modal])
;          :on-save      #(rf/dispatch [::close-modal])}])

;(defn modal-dialog-parameterinstrument
;  [props]
;  [Modal {:ok-copy      "Done"
;          :modal-header [:span [:span.glyphicon.glyphicon-list] " " "Browse parameter instruments"]
;          :modal-body   [:div
;                         [TermOrOtherForm (assoc props :dp-type :instrument)]]
;          :on-dismiss   #(rf/dispatch [::close-modal])
;          :on-save      #(rf/dispatch [::close-modal])}])

;(defn modal-dialog-parameterplatform
;  [props]
;  [Modal {:ok-copy      "Done"
;          :modal-header [:span [:span.glyphicon.glyphicon-list] " " "Browse parameter platforms"]
;          :modal-body   [:div
;                         [TermOrOtherForm (assoc props :dp-type :platform)]]
;          :on-dismiss   #(rf/dispatch [::close-modal])
;          :on-save      #(rf/dispatch [::close-modal])}])

;(defn modal-dialog-person
;  [props]
;  [Modal {:ok-copy      "Done"
;          :modal-header [:span [:span.glyphicon.glyphicon-list] " " "Browse people"]
;          :modal-body   [:div
;                         [PersonForm props]]
;          :on-dismiss   #(rf/dispatch [::close-modal])
;          :on-save      #(rf/dispatch [::close-modal])}])

;(defn DataParameterRowEdit [path]
;  (let [base-path (conj path :value)
;        name-path (conj path :value :name)
;        serialNumber-path (conj path :value :serialNumber)
;        form-position (get name-path 5)
;        selected-platform @(rf/subscribe [:subs/platform-selected? form-position])]
;    [:div.DataParameterMaster
;     [:form/fieldset.tern-fieldset
;      [:div
;       {:class "alert alert-success"}
;       "Request new controlled vocabulary terms if they do not exist in the drop-down fields using the feedback button to the right of the screen."]
;      [elasticsearch-select-field {:param-type   :parametername
;                                   :api-path     [:api :parametername]
;                                   :dp-term-path base-path
;                                   :dp-type      :longName}]
;      [InputField {:path name-path}]]
;     [:form/fieldset.tern-fieldset
;      [elasticsearch-select-field {:param-type   :parameterunit
;                                   :api-path     [:api :parameterunit]
;                                   :dp-term-path base-path
;                                   :dp-type      :unit}]]
;     [:form/fieldset.tern-fieldset
;      [elasticsearch-select-field {:param-type   :parameterplatform
;                                   :api-path     [:api :parameterplatform]
;                                   :dp-term-path base-path
;                                   :dp-type      :platform}]
;      [elasticsearch-select-field {:param-type   :parameterinstrument
;                                   :api-path     [:api :parameterinstrument]
;                                   :dp-term-path base-path
;                                   :dp-type      :instrument}]
;      ; TODO: This should be enabled only when an instrument is created. Currently, creating vocabulary terms is disabled.
;      [InputField
;       {:path     serialNumber-path
;        :disabled (boolean (if selected-platform nil 0))}]]
;     ]))

;; FIXME this is tern-specific.
;(defn DataParametersTable [{:keys [path]}]
;  [:div.DataParametersTable
;   [TableModalEdit
;    {:ths        ["Name" "Units" "Instrument" "Serial No." "Platform"]
;     :tds-fn     (fn [field]
;                   (let [{:keys [longName_term unit_term instrument_term serialNumber platform_term]} (:value field)]
;                     (mapv #(:value %) [longName_term unit_term instrument_term serialNumber platform_term])))
;     :form       DataParameterRowEdit
;     :title      "Parameter"
;     :add-label  "Add data parameter"
;     :field-path path}]])

(defn handle-file [this file]
  (let [{:keys [reset-ch max-filesize]} (r/props this)]
    (if (or (not max-filesize)
            (<= (.-size file) (* 1024 1024 max-filesize)))
      (r/set-state this {:file file})
      (when max-filesize
        (rf/dispatch [::open-modal
                      {:type    :alert
                       :message (str "Please, choose file less than " max-filesize "mb")}])
        (put! reset-ch true)))))

(defn FileDrop [{:keys [on-change placeholder reset-ch]}]
  (letfn [(init-state [_]
            {:file-id (name (gensym "file"))})
          (did-mount [this]
            (gevents/listen
              (FileDropHandler. js/document)
              goog.events.FileDropHandler.EventType.DROP
              (fn [^js e] (handle-file this (.. e getBrowserEvent -dataTransfer -files (item 0)))))
            (go-loop []
                     (when (<! reset-ch)
                       (r/set-state this {:file nil})
                       (recur))))
          (did-update [this [_ _ prev-state]]
            (let [{:keys [file]} (r/state this)]
              (when (and on-change (not= file (:file prev-state)))
                (on-change file))))
          (render [this]
            (let [{:keys [file file-id]} (r/state this)]
              [:div
               {:style {:position "relative"}}
               [:div.text-center.dropzone {:on-click #(.click (js/document.getElementById file-id))}
                [:h3
                 (or (and file (.-name file)) placeholder
                     "Drop file here or click here to upload")]
                [:span.help-block "Maximum file size 100 MB"]]
               [:input
                {:id        file-id
                 :type      "file"
                 :on-change #(handle-file this (.. % -target -files (item 0)))
                 :style     {:position "absolute"
                             :z-index  1
                             :opacity  0
                             :left     0
                             :top      0
                             :width    "100%"
                             :height   "100%"}}]]))]
    (r/create-class
      {:get-initial-state    init-state
       :component-did-mount  did-mount
       :component-did-update did-update
       :render               render})))

(defn delete-attachment!
  "Quick and dirty delete function"
  [attachments-path attachment-idx]
  (rf/dispatch [::open-modal
                {:type       :confirm
                 :title      "Delete?"
                 :message    "Are you sure you want to delete this file?"
                 :on-confirm #(rf/dispatch [::del-value attachments-path attachment-idx])}]))

(defn UploadData
  [_]
  (letfn [(confirm-upload-click
            [this {:keys [url fields]} file reset-file-drop]
            (r/set-state this {:uploading true})
            (let [fd (js/FormData.)
                  xhr (js/XMLHttpRequest.)]
              (.open xhr "POST" url true)
              (set! (.-onreadystatechange xhr)
                    (fn []
                      (when (= (.-readyState xhr) 4)
                        (if (#{200 201} (.-status xhr))
                          (rf/dispatch [::upload-data-confirm-upload-click-add-attachment (utils3/map-keys keyword (js->clj (.parse js/JSON (.-response xhr))))])
                          (rf/dispatch [::open-modal
                                        {:type    :alert
                                         :message "File upload failed. Please try again or contact administrator."}]))
                        (r/set-state this {:uploading false})
                        (put! reset-file-drop true))))
              (doto fd
                (.append "csrfmiddlewaretoken" (get-in fields [:csrfmiddlewaretoken :initial]))
                (.append "document" (get-in fields [:document :initial]))
                (.append "name" (.-name file))
                (.append "file" file))
              (.send xhr fd)))
          (init-state [_]
            {:reset-file-drop (chan)})
          (render [this]
            (let [{:keys [attachments-path]} (r/props this)
                  {:keys [file reset-file-drop uploading]} (r/state this)
                  {:keys [disabled] :as attachments} @(rf/subscribe [:subs/get-derived-path attachments-path])
                  upload-form @(rf/subscribe [:subs/get-derived-path [:upload_form]])]
              [:div.UploadData {:class (when disabled "disabled")}
               (if-not (empty? (:value attachments))
                 [:div
                  [:table.table.table-hover
                   [:thead
                    [:tr [:th "Name"]]]
                   [:tbody
                    (for [[attachment-idx attachment] (map-indexed vector (:value attachments))]
                      (let [{:keys [file name]} (:value attachment)]
                        [:tr
                         [:td
                          [:a {:href (:value file) :target "blank"} (:value name)]
                          [:button.btn.btn-warn.btn-xs.pull-right
                           {:on-click #(delete-attachment! attachments-path attachment-idx)
                            :disabled disabled}
                           [:span.glyphicon.glyphicon-minus]]]]))]]]
                 [:p "There are no data files attached to this record"])
               (when-not disabled
                 [:div
                  [FileDrop
                   {:name         "file"
                    :max-filesize 100
                    :reset-ch     reset-file-drop
                    :on-change    #(r/set-state this {:file %})}]
                  [:button.btn.btn-primary
                   {:on-click #(confirm-upload-click this upload-form file reset-file-drop)
                    :disabled (or uploading (not file))}
                   "Confirm Upload"]])]))]
    (r/create-class
      {:get-initial-state init-state
       :render            render})))

;(defn AddressField [address-path]
;  (let [address @(rf/subscribe [:subs/get-derived-path address-path])
;        {:keys [city postalCode administrativeArea country deliveryPoint deliveryPoint2]} address]
;    [:div.AddressField
;     [InputWidget (assoc deliveryPoint
;                    :on-change #(rf/dispatch [::value-changed (conj address-path :deliveryPoint) %]))]
;     [InputWidget (assoc deliveryPoint2
;                    :on-change #(rf/dispatch [::value-changed (conj address-path :deliveryPoint2) %]))]
;     [:div.row
;      [:div.col-xs-6
;       [InputWidget (assoc city
;                      :help "City"
;                      :on-change #(rf/dispatch [::value-changed (conj address-path :city) %]))]]
;      [:div.col-xs-6
;       [InputWidget (assoc administrativeArea
;                      :help "State/territory"
;                      :on-change #(rf/dispatch [::value-changed (conj address-path :administrativeArea) %]))]]]
;     [:div.row
;      [:div.col-xs-6
;       [InputWidget (assoc postalCode
;                      :help "Postal / Zip code"
;                      :on-change #(rf/dispatch [::value-changed (conj address-path :postalCode) %]))]]
;      [:div.col-xs-6
;       [InputWidget (assoc country
;                      :help "Country"
;                      :on-change #(rf/dispatch [::value-changed (conj address-path :country) %]))]]]]))

;(defn OrganisationPickerWidget
;  [props]
;  (let [{:keys [on-input-change on-blur on-change disabled party-path]} props
;        {:keys [URL_ROOT]} @(rf/subscribe [:subs/get-derived-path [:context]])
;        orgId (:value @(rf/subscribe [:subs/get-derived-path (conj party-path :value :organisationIdentifier)]))
;        orgName (:value @(rf/subscribe [:subs/get-derived-path (conj party-path :value :organisationName)]))
;        orgCity (:value @(rf/subscribe [:subs/get-derived-path (conj party-path :value :address :city)]))
;        js-value #js {:uri              (or orgId "")
;                      :organisationName (or (if (blank? orgCity)
;                                              orgName
;                                              (str orgName " - " orgCity)) "")}
;        js-value (if orgId
;                   js-value
;                   nil)]
;    ; TODO: this really doesn't need to be async
;    (ReactSelectAsyncCreatable
;      {:value             js-value
;       :disabled          disabled
;       :defaultOptions    true
;       :getOptionValue    (fn [option]
;                            (str (gobj/get option "uri") "||" (gobj/get option "city")))
;       :formatOptionLabel (fn [props]
;                            (let [is-created? (gobj/get props "__isCreated__")]
;                              (if is-created?
;                                (str "Create new organisation \"" (gobj/get props "organisationName") "\"")
;                                (if (blank? (gobj/get props "city"))
;                                  (gobj/get props "organisationName")
;                                  (str (gobj/get props "organisationName") " - " (gobj/get props "city"))))))
;       :loadOptions       (fn [input callback]
;                            (ajax/GET (str URL_ROOT "/api/institution.json")
;                                      {:handler
;                                       (fn [{:strs [results]}]
;                                         (callback (clj->js results)))
;                                       :error-handler
;                                       (fn [_]
;                                         (callback "Options loading error."))
;                                       :params
;                                       {:search input
;                                        :offset 0
;                                        :limit  1000}}))
;       :onChange          #(on-change (js->clj %))
;       :getNewOptionData  (fn [input]
;                            #js {:uri              (str "https://w3id.org/tern/resources/" (random-uuid))
;                                 :organisationName input
;                                 :__isCreated__    true})
;       :noResultsText     "No results found"
;       :onBlurResetsInput false
;       :isClearable       true
;       :isSearchable      true
;       :tabSelectsValue   false
;       :onInputChange     on-input-change
;       :onBlur            on-blur
;       :filterOption      (fn [option value]
;                            (string/includes? (string/lower-case (string/replace (get-in (js->clj option :keywordize-keys true) [:data :organisationName]) #"\s" ""))
;                                              (string/lower-case (string/replace value #"\s" ""))))
;
;       :isValidNewOption  (fn [input _ options]
;                            (let [input (string/lower-case (string/replace input #"\s" ""))
;                                  match (filter (fn [x] (= input (string/lower-case (string/replace (:organisationName x) #"\s" "")))) (js->clj options :keywordize-keys true))]
;                              (empty? match)))
;       :placeholder       "Start typing to filter list..."})))

;(defn PersonPickerWidget
;  [_]
;  (letfn [(render [this]
;            (let [{:keys [on-input-change on-blur on-change disabled party-path]} (r/props this)
;                  {:keys [URL_ROOT]} @(rf/subscribe [:subs/get-derived-path [:context]])
;                  uri-value @(rf/subscribe [:subs/get-derived-path (conj party-path :value :uri)])
;                  preflabel-value @(rf/subscribe [:subs/get-derived-path (conj party-path :value :individualName)])
;                  js-value #js {:prefLabel (or (:value preflabel-value) "")
;                                :uri       (:value uri-value)}
;                  js-value (if (:value preflabel-value)
;                             js-value
;                             nil)]
;              (ReactSelectAsync
;                {:value             js-value
;                 :disabled          disabled
;                 :defaultOptions    true
;                 :getOptionValue    (fn [option]
;                                      (gobj/get option "uri"))
;                 :formatOptionLabel (fn [props]
;                                      (gobj/get props "prefLabel"))
;                 :loadOptions       (fn [input callback]
;                                      (ajax/GET (str URL_ROOT "/api/person.json")
;                                                {:handler
;                                                 (fn [{:strs [results]}]
;                                                   (callback (clj->js results)))
;                                                 :error-handler
;                                                 (fn [_]
;                                                   (callback "Options loading error."))
;                                                 :params
;                                                 {:search input
;                                                  :offset 0
;                                                  :limit  100}}))
;                 :onChange          #(on-change (js->clj %))
;                 :noResultsText     "No results found"
;                 :onBlurResetsInput false
;                 :isClearable       true
;                 :tabSelectsValue   false
;                 :onInputChange     on-input-change
;                 :onBlur            on-blur
;                 :placeholder       "Start typing to filter list..."})))]
;    (r/create-class
;      {:render render})))

;(defn SelectRoleWidget [role-path]
;  (let [role @(rf/subscribe [:subs/get-derived-path role-path])
;        {:keys [options]} @(rf/subscribe [:subs/get-derived-path [:api :api/rolecode]])]
;    [SelectWidget (assoc role
;                    :options (for [option options
;                                   :let [Identifier (gobj/get option "Identifier")]]
;                               [Identifier (cuerdas/human Identifier)])
;                    :on-change #(rf/dispatch [::value-changed role-path %]))]))

;(defn person-input-field
;  "Input field for people which offers autocompletion of known
;  people."
;  [party-path]
;  (let [party-field @(rf/subscribe [:subs/get-derived-path party-path])
;        uri (-> party-field :value :uri)]
;    [:div.OrganisationInputField
;     ; FIXME: replace with autocomplete if we can find one
;     [PersonPickerWidget
;      {:old-value  uri
;       :party-path party-path
;       :disabled   (:disabled uri)
;       :on-change  (fn [option]
;                     (rf/dispatch [::person-input-field-picker-change (conj party-path :value) option]))}]]))

;(defn OrganisationInputField
;  "Input field for organisation which offers autocompletion of known
;  institutions.  On autocomplete address details are updated."
;  [party-path]
;  (let [party-field @(rf/subscribe [:subs/get-derived-path party-path])
;        organisationName (-> party-field :value :organisationName)]
;    [:div.OrganisationInputField
;     ; FIXME: replace with autocomplete if we can find one
;     [OrganisationPickerWidget
;      {:old-value       organisationName
;       :party-path      party-path
;       :disabled        (:disabled organisationName)
;       ;manual maxlength implementation
;       :on-input-change (fn [newvalue]
;                          (subs newvalue 0 250))
;       :on-change       (fn [option]
;                          (rf/dispatch [::organisation-input-field-change (conj party-path :value) option]))}]]))

;(def orcid-mask "0000{-}0000{-}0000{-}000*")

;(defn ResponsiblePartyField [party-path]
;  (let [party-value-path (conj party-path :value)
;        party-value @(rf/subscribe [:subs/get-derived-path party-value-path])
;        {:keys [individualName givenName familyName phone facsimile orcid
;                electronicMailAddress organisationName isUserAdded]} party-value
;        electronicMailAddress (assoc electronicMailAddress :required (:value isUserAdded))
;        ]
;    [:div.ResponsiblePartyField
;
;
;     [SelectRoleWidget (conj party-value-path :role)]
;
;
;     [:div.flex-row
;      [:div.flex-row-field
;       {;need this to make sure the drop down is rendered above any other input fields
;        :style {:position "relative"
;                :z-index  10}}
;       [:label "Contact name" (when (:required individualName) " *")]
;       [person-input-field party-path]
;       [:p.help-block "If you cannot find the person in the list above, please enter details below"]]]
;     [:div
;
;
;      [:div.row
;       [:div.col-md-6
;        [NameInputWidget (assoc givenName
;                           :on-change #(rf/dispatch [::responsible-party-field-given-name-changed party-value-path :givenName % isUserAdded]))]]
;       [:div.col-md-6
;        [NameInputWidget (assoc familyName
;                           :on-change #(rf/dispatch [::responsible-party-field-family-name-changed party-value-path :familyName % isUserAdded]))]]]
;
;      [InputWidget (assoc electronicMailAddress
;                     :on-change #(rf/dispatch [::value-changed (conj party-value-path :electronicMailAddress) %]))]
;
;      [InputWidget (assoc orcid
;                     :on-change #(rf/dispatch [::value-changed (conj party-value-path :orcid) %])
;                     :mask orcid-mask)]
;
;      [:label "Organisation" (when (:required organisationName) " *")]
;      [OrganisationInputField party-path]
;
;      [:label "Postal address"]
;      [AddressField (conj party-value-path :address)]
;
;      [:div.ContactDetails
;
;       [InputWidget (assoc phone
;                      :on-change #(rf/dispatch [::value-changed (conj party-value-path :phone) %]))]
;
;       [InputWidget (assoc facsimile
;                      :on-change #(rf/dispatch [::value-changed (conj party-value-path :facsimile) %]))]
;
;       ]]]))

;(defn FieldError [{:keys [errors label]}]
;  [:span.FieldError label ": " (first errors)])

;(defn ManyFieldError [{:keys [errors label]}]
;  [:span.FieldError label ": " (or (first errors) "check field errors")])

;(defn PageErrors [{:keys [page path]}]
;  (let [form @(rf/subscribe [:subs/get-derived-path path])
;        fields (logic3/page-fields form page)
;        error-fields (remove #(logic3/is-valid? {:fields %}) fields)
;        msgs (for [field error-fields]
;               (if (and (:many field) (not (logic3/is-valid? {:fields field})))
;                 [ManyFieldError field]
;                 [FieldError field]))]
;    (when (seq msgs)
;      [:div.alert.alert-warning.alert-dismissable
;       [:button {:type     "button" :class "close"
;                 :on-click #(rf/dispatch [::page-errors-hide-click path])} "×"]
;       (if (> (count msgs) 1)
;         [:div
;          [:b "There are multiple fields on this page that require your attention:"]
;          (into [:ul] (for [msg msgs] [:li msg]))]
;         (first msgs))])))

; (defn help-submenu
;   [menu-items]
;   (into [bp3/menu]
;         (for [[text event-v] menu-items]
;           [bp3/menu-item {:text text :on-click #(rf/dispatch event-v)}])))

; (defn help-menu
;   []
;   (when-let [menu-items @(rf/subscribe [:help/get-menuitems])]
;     [bp3/popover {:content (r/as-element [help-submenu menu-items])}
;      [:button.bp3-button.bp3-minimal "Help"]]))

(defn navbar
  []
  (let [{:keys [Dashboard account_profile account_logout]} @(rf/subscribe [:subs/get-derived-path [:context :urls]])
        {:keys [user]} @(rf/subscribe [:subs/get-derived-path [:context]])
        {:keys [title tag_line guide_pdf]} @(rf/subscribe [:subs/get-derived-path [:context :site]])]
    [bp3/navbar {:className "bp3-dark"}
     [:div.container
      [bp3/navbar-group {:align (:LEFT bp3/alignment)}
       [:a.bp3-button.bp3-minimal {:href Dashboard} [bp3/navbar-heading (str title " " tag_line)]]]
      [bp3/navbar-group {:align (:RIGHT bp3/alignment)}
       (if account_profile
         [:a.bp3-button.bp3-minimal {:href account_profile} (utils3/userDisplay user)]
         [:span {:style {:padding "5px 10px 5px 10px"}} (utils3/userDisplay user)])
       [:a.bp3-button.bp3-minimal {:href guide_pdf :target "_blank"} "Help"]
       [:a.bp3-button.bp3-minimal {:href account_logout} "Sign Out"]]]]))

(defn PageView404
  [_]
  (let [{:keys [name]} @(rf/subscribe [:subs/get-page-props])]
    [:h1 "Page not found: " name]))

(defn PageViewError
  [_]
  (let [{:keys [text code]} @(rf/subscribe [:subs/get-page-props])]
    [:div
     [navbar]
     [:div.container
      [:div.PageViewBody
       [:p.lead "Oops! " (pr-str text)]
       [:p "The server responded with a " [:code code " " (pr-str text)] " error."]]]]))

;(defn CreditField [path]
;  [:div.CreditField [textarea-widget @(rf/subscribe [:textarea-field/get-many-field-props path :credit])]])

;(defn parties-list [this group]
;  (let [{:keys [disabled] :as parties} @(rf/subscribe [:subs/get-derived-path (:path (contact-groups group))])
;        {:keys [selected-group selected-item]} (r/state this)
;        selected-item (when (= group selected-group) selected-item)]
;    (letfn [(delete-contact! [e item]
;              (.stopPropagation e)
;              (let [parties-path (:path (contact-groups group))
;                    {:keys [selected-group selected-item]} (r/state this)]
;                (rf/dispatch [::open-modal
;                              {:type       :confirm
;                               :title      "Delete?"
;                               :message    "Are you sure you want to delete this person?"
;                               :on-confirm (fn []
;                                             (when (and (= group selected-group) (<= item selected-item))
;                                               (r/set-state this {:selected-item
;                                                                  (when (> (count (:value parties)) 1)
;                                                                    (-> selected-item dec (max 0)))}))
;                                             (rf/dispatch [::parties-list-remove-party-confirm parties-path item]))}])))]
;
;      (into [:div.list-group]
;            (for [[item party] (-> parties :value utils3/enum)]
;              [:div
;               [:a.list-group-item
;                {:class    (when (= item selected-item) "active")
;                 :on-click (fn []
;                             (r/set-state this {:selected-group group})
;                             (r/set-state this {:selected-item item}))}
;                [:span
;                 (let [name (get-in party [:value :individualName :value])
;                       givenName (get-in party [:value :givenName :value])
;                       familyName (get-in party [:value :familyName :value])
;                       name (if (blank? name)
;                              (str givenName " " familyName)
;                              name)]
;                   (if (blank? name) [:em "First name Last name"] name))
;                 (when-not disabled
;                   [:button.btn.btn-warn.btn-xs.pull-right
;                    {:on-click #(delete-contact! % item)}
;                    [:i.glyphicon.glyphicon-minus]])]]])))))

;(defn default-selected-group []
;  (ffirst
;    (filter
;      #(first @(rf/subscribe [:subs/get-derived-path (conj (:path (second %)) :value)]))
;      (utils3/enum contact-groups))))

;(defn Who [_]
;  (letfn [(init-state [_]
;            {:selected-item  0
;             :selected-group 0})
;          (render [this]
;            (let [{:keys [selected-group selected-item open hold]} (r/state this)
;                  {:keys [credit-path]} (r/props this)
;                  selected-group (or selected-group (default-selected-group))
;                  cursors (mapv (fn [{:keys [path]}]
;                                  @(rf/subscribe [:subs/get-derived-path path]))
;                                contact-groups)
;                  new! (fn [path group & [field]]
;                         (let [many-field (cursors group)]
;                           (if field
;                             (rf/dispatch [::who-new-add-value path (:value field)])
;                             (rf/dispatch [::who-new-field path]))
;                           (r/set-state this {:selected-group group})
;                           (r/set-state this {:selected-item (-> many-field :value count)})))
;                  all-parties (mapv (comp set
;                                          :value)
;                                    cursors)
;                  all-parties-set (apply set/union all-parties)]
;              [:div
;               [PageErrors {:page :who :path [:form]}]
;               [:h2 "6: Who"]
;               [:div.row
;                (into [:div.col-sm-4]
;                      (for [[group {:keys [title path]}] (utils3/enum contact-groups)]
;                        (let [parties (clojure.set/difference
;                                        all-parties-set (all-parties group))]
;                          [:div
;                           [:h4 title (when (get-in cursors [group :required]) " *")]
;                           (parties-list this group)
;                           (when-not (get-in cursors [group :disabled])
;                             [:div.dropdown
;                              {:class   (when (= open group) "open")
;                               :on-blur #(let [{:keys [open]} (r/state this)
;                                               open' (when (or hold (not= open group)) open)]
;                                           (r/set-state this {:open open'}))}
;                              [:button.btn.btn-default.dropdown-toggle
;                               {:on-click #(if (zero? (count parties))
;                                             (new! path group)
;                                             (let [{:keys [open]} (r/state this)
;                                                   open' (when (not= open group) group)]
;                                               (r/set-state this {:open open'})))}
;                               [:span.glyphicon.glyphicon-plus]
;                               " Add person"]
;                              (-> [:ul.dropdown-menu
;                                   {:on-mouse-enter #(r/set-state this {:hold true})
;                                    :on-mouse-leave #(r/set-state this {:hold false})}
;                                   [:li.dropdown-header "Copy person"]]
;                                  (into (for [x parties]
;                                          [:li [:a {:tab-index -1
;                                                    :href      "#"
;                                                    :on-click  (fn [e]
;                                                                 (.preventDefault e)
;                                                                 (new! path group x)
;                                                                 (r/set-state this {:open false}))}
;                                                (get-in x [:value :individualName :value])]]))
;                                  (conj [:li.divider]
;                                        [:li [:a {:href     "#"
;                                                  :on-click (fn [e]
;                                                              (.preventDefault e)
;                                                              (new! path group)
;                                                              (r/set-state this {:open false}))}
;                                              "New person"]]))])])))
;                [:div.col-sm-8
;                 (when (and selected-group selected-item)
;                   [ResponsiblePartyField
;                    (-> contact-groups
;                        (get-in [selected-group :path])
;                        (conj :value selected-item))])]]
;
;               [:h2 "Other credits"]
;               [TableModalEdit
;                {:form       CreditField
;                 :title      "Credit"
;                 :field-path credit-path}]]))]
;    (r/create-class
;      {:get-initial-state init-state
;       :render            render})))

;(defn UseLimitationsFieldEdit [path]
;  [textarea-widget @(rf/subscribe [:textarea-field/get-many-field-props path :useLimitations])])

;(defn UseLimitations
;  [{:keys [path]}]
;  (let [list-field @(rf/subscribe [:subs/get-derived-path path])]
;    [:div.SupplementalInformation
;     (label-template list-field)
;     [TableModalEdit
;      {:form       UseLimitationsFieldEdit
;       :title      "Use Limitation"
;       :add-label  "Add use limitation"
;       :field-path path}]]))

;(defn SupplementalInformationRowEdit [path]
;  [textarea-widget @(rf/subscribe [:textarea-field/get-many-field-props path :supplementalInformation])])

;(defn IMASSupplementalInformation
;  [path]
;  [TableModalEdit
;   {:form        SupplementalInformationRowEdit
;    :title       "Add Publication"
;    :placeholder ""
;    :add-label   "Add publication"
;    :field-path  path}])

;(defn SupportingResourceFieldEdit [path]
;  [:div
;   [InputField {:path (conj path :value :name)}]
;   [InputField {:path (conj path :value :url)}]])

;(defn IMASSupportingResource
;  [{:keys [path]}]
;  [:div
;   [:label "Supporting resources"]
;   [TableModalEdit
;    {:ths         ["Title" "URL"]
;     :tds-fn      (comp (partial map (comp #(or % "--") :value)) (juxt :name :url) :value)
;     :form        SupportingResourceFieldEdit
;     :title       "Add supporting resource"
;     :placeholder ""
;     :add-label   "Add supporting resource"
;     :field-path  path}]])

(defn DataSourceRowEdit [path]
  [:div
   [InputField {:path (conj path :value :description)}]
   [SelectField {:path (conj path :value :protocol)}]
   [InputField {:path (conj path :value :url)}]
   [InputField {:path (conj path :value :name)}]])

(defn DataSources
  [{:keys [path]}]
  [:div
   [TableModalEdit {:ths        ["Title" "URL" "Layer"]
                    :tds-fn     (comp (partial map (comp #(or % "--") :value)) (juxt :description :url :name) :value)
                    :form       DataSourceRowEdit
                    :title      "Data services"
                    :field-path path}]])

(defn progress-bar []
  (when-let [{:keys [can-submit? value]} @(rf/subscribe [::get-progress-bar-props])]
    [:div
     [:span.progressPercentage (str (int (* value 100)) "%")]
     [bp3/progress-bar {:animate false
                        :intent  (if can-submit? "success" "warning")
                        :stripes false
                        :value   value}]]))

(defn edit-tabs
  []
  (let [{:keys [disabled]} @(rf/subscribe [:subs/get-derived-path [:form]])
        {:keys [selected-tab tab-props]} @(rf/subscribe [:subs/get-edit-tab-props])]
    (letfn [(pick-tab [id _ _] (rf/dispatch [::edit-tabs-pick-click (edn/read-string id)]))]
      [:div {:style {:min-width "60em"}}
       (-> [bp3/tabs {:selectedTabId            (pr-str selected-tab)
                      :onChange                 pick-tab
                      :renderActiveTabPanelOnly true}]
           (into (for [{:keys [id title has-errors?]} tab-props]
                   (let [title (if has-errors? (str title " *") title)]
                     [bp3/tab {:title title :id (pr-str id)}])))
           (conj
             [bp3/tabs-expander]
             (when-not disabled
               [:div {:style {:width 200 :height 25}}
                (conj [:div.hidden-xs.hidden-sm
                       [progress-bar]])])))])))

(defn PageViewEdit
  [_]
  (letfn [(handle-archive-click
            []
            (rf/dispatch [::open-modal
                          {:type       :confirm
                           :title      "Archive?"
                           :message    "Are you sure you want to archive this record?"
                           :on-confirm #(rf/dispatch [::page-view-edit-archive-click-confirm])}]))
          (render [_]
            (let [page @(rf/subscribe [:subs/get-page-props])
                  saving (::handlers3/saving? page)
                  {:keys [urls user]} @(rf/subscribe [:subs/get-derived-path [:context]])
                  {:keys [disabled]} @(rf/subscribe [:subs/get-derived-path [:form]])
                  dirty @(rf/subscribe [:subs/get-form-dirty [:form]])
                  {:keys [status title last_updated last_updated_by is_editor owner]} @(rf/subscribe [:subs/get-derived-path [:context :document]])]
              [:div
               [navbar]
               [:div.container
                [:div.pagehead
                 [:div.pull-right
                  (when is_editor
                    [:button.btn.btn-default.text-warn {:on-click handle-archive-click
                                                        :disabled disabled}
                     [:span.fa.fa-archive]
                     " Archive"]) " "
                  [:button.btn.btn-primary {:disabled (or disabled (not dirty) saving)
                                            :on-click #(rf/dispatch [::PageViewEdit-save-button-click])}
                   (cond
                     saving [:img {:src (str (:STATIC_URL urls) "metcalf3/img/saving.gif")}]
                     dirty [:span.glyphicon.glyphicon-floppy-disk]
                     :else [:span.glyphicon.glyphicon-floppy-saved])
                   (cond
                     saving " Saving..."
                     dirty " Save"
                     :else " Saved")]]
                 [:h4
                  [:span (:username owner)]
                  " / "
                  [:strong (if (blank? title) "Untitled" title)]
                  " "
                  [:span.label.label-info {:style {:font-weight "normal"}} status]
                  [:br]
                  [:small [:i {:style {:color     "#aaa"
                                       :font-size "1em"}}
                           "Last edited " (moment/from-now last_updated)
                           " by " (:username last_updated_by)]]]]]
               [:div.Home.container
                [edit-tabs]
                [:div.PageViewBody
                 [low-code/render-template {:template-id (get page :tab :data-identification)}]]]]))]
    (r/create-class
      {:render render})))

(defn FormErrors [{:keys [path]}]
  (let [{:keys [fields show-errors]} @(rf/subscribe [:subs/get-derived-path path])
        fields-with-errors (filter (comp :errors second) fields)]
    (when (and show-errors (seq fields-with-errors))
      [:div.alert.alert-danger
       [:p [:b "The following fields need your attention"]]
       (into [:ul] (for [[k {:keys [label errors]}] fields-with-errors]
                     [:li
                      (or label (name k)) ": "
                      (string/join ". " errors)]))])))

(defn NewDocumentForm []
  [:div.NewDocumentForm
   [FormErrors {:path [:create_form]}]
   [InputField {:path [:create_form :fields :title]}]
   [SelectField {:path [:create_form :fields :template]}]])

(defn modal-dialog-dashboard-create-modal
  [_ _]
  [Modal {:ok-copy      "OK"
          :modal-header [:span [:span.glyphicon.glyphicon-list] " " "Create a new record"]
          :modal-body   [NewDocumentForm]
          :on-dismiss   #(rf/dispatch [::close-modal])
          :on-cancel    #(rf/dispatch [::close-modal])
          :on-save      #(rf/dispatch [::modal-dialog-dashboard-create-modal-save-click])}])

(defn NewDocumentButton []
  [:button.btn.btn-primary {:on-click #(rf/dispatch [::open-modal {:type :DashboardCreateModal}])}
   [:span.glyphicon.glyphicon-plus]
   " Create new record"])

(defn clone-doc [url event]
  (rf/dispatch [::open-modal
                {:type       :confirm
                 :title      "Clone?"
                 :message    (str "Are you sure you want to clone this record?")
                 :on-confirm #(rf/dispatch [::clone-doc-confirm url])}])
  (.stopPropagation event))

(defn DocumentTeaser [{:keys [url title last_updated last_updated_by status transitions
                              transition_url clone_url is_editor owner] :as doc}]
  (let [transitions (set transitions)
        on-archive-click (fn [e] (.stopPropagation e) (rf/dispatch [::document-teaser-archive-click transition_url]))
        on-delete-archived-click (fn [e] (.stopPropagation e) (rf/dispatch [::document-teaser-delete-archived-click transition_url]))
        on-restore-click (fn [e] (.stopPropagation e) (rf/dispatch [::document-teaser-restore-click transition_url]))
        on-clone-click (fn [e] (clone-doc clone_url e))
        on-edit-click (fn [e] (.stopPropagation e) (aset js/location "href" url))]

    [:div.bp3-card.bp3-interactive.DocumentTeaser
     {:on-click on-edit-click}
     (when is_editor
       [:div.pull-right
        (when (contains? transitions "archive")
          [:span.btn.btn-default.noborder.btn-xs
           {:on-click on-archive-click}
           [:span.glyphicon.glyphicon-trash] " archive"])
        (when (contains? transitions "delete_archived")
          [:span.btn.btn-default.noborder.btn-xs
           {:on-click on-delete-archived-click}
           [:span.glyphicon.glyphicon-remove] " delete"])
        (when (contains? transitions "restore")
          [:span.btn.btn-default.noborder.btn-xs
           {:on-click on-restore-click}
           [:span.glyphicon.glyphicon-open] " restore"])
        [:span.btn.btn-default.noborder.btn-xs
         {:on-click on-clone-click}
         [:span.glyphicon.glyphicon-duplicate] " clone"]
        [:span.btn.btn-default.noborder.btn-xs {:on-click on-edit-click}
         [:span.glyphicon.glyphicon-pencil] " edit"]])
     [:h4
      [:span.link
       [:span (:username owner)]
       " / "
       [:strong title]]
      " "
      [:span.label.label-info {:style {:font-weight "normal"}} status]
      " "]
     [:p.list-group-item-text
      [:i {:style {:color     "#aaa"
                   :font-size "0.9em"}}
       (if-not (empty? last_updated)
         [:span
          "Last edited " (moment/from-now last_updated)
          " by " (:username last_updated_by)]
         "Has not been edited yet")]]]))

(defn dashboard
  [_]
  (let [{:keys [filtered-docs status-filter has-documents? status-freq status relevant-status-filter]} @(rf/subscribe [::get-dashboard-props])]
    [:div
     [navbar]
     [:div.container
      [:span.pull-right {:style {:margin-top 18}} [NewDocumentButton]]
      [:h1 "My Records"]
      [:div.row
       [:div.col-sm-9
        (-> [:div.list-group]
            (into (for [filtered-doc filtered-docs]
                    ^{:key (:url filtered-doc)} [DocumentTeaser filtered-doc]))
            (conj (if has-documents?
                    [:a.list-group-item {:on-click #(rf/dispatch [::dashboard-create-click])}
                     [:span.glyphicon.glyphicon-star.pull-right]
                     [:p.lead.list-group-item-heading " My first record "]
                     [:p.list-group-item-text "Welcome! Since you’re new here, "
                      [:span {:style {:text-decoration "underline"}} "Click here"] " to get started."]]
                    (when (empty? filtered-docs)
                      (if (= status-filter logic3/active-status-filter)
                        [:div
                         [:p "You don't have any active records: "
                          [:a {:on-click #(rf/dispatch [::dashboard-show-all-click])}
                           "show all documents"] "."]
                         [NewDocumentButton]]
                        [:div
                         [:p "No documents match your filter: "
                          [:a {:on-click #(rf/dispatch [::dashboard-show-all-click])}
                           "show all documents"] "."]
                         [NewDocumentButton]])))))]
       [:div.col-sm-3
        (when-not (empty? status-freq)
          (into [:div]
                (for [[sid sname] status]
                  (let [freq (get status-freq sid)]
                    [:div [:label
                           [:input {:type      "checkbox"
                                    :disabled  (not freq)
                                    :checked   (contains? relevant-status-filter sid)
                                    :on-change #(rf/dispatch [::dashboard-toggle-status-filter sid status-filter])}]
                           " " sname
                           (when freq [:span.freq " (" freq ")"])]]))))]]]]))

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

(defn modal-dialog-alert
  [{:keys [message]}]
  [Modal
   {:modal-header [:span [:span.glyphicon.glyphicon-exclamation-sign]
                   " " "Alert"]
    :dialog-class "modal-sm"
    :modal-body   message
    :on-dismiss   #(rf/dispatch [::close-modal])
    :on-save      #(rf/dispatch [::close-modal])}])

(defn modal-dialog-confirm
  [{:keys [message title]}]
  [Modal
   {:modal-header [:span [:span.glyphicon.glyphicon-question-sign] " " title]
    :dialog-class "modal-sm"
    :modal-body   message
    :on-dismiss   #(rf/dispatch [::modal-dialog-confirm-dismiss])
    :on-cancel    #(rf/dispatch [::modal-dialog-confirm-cancel])
    :on-save      #(rf/dispatch [::modal-dialog-confirm-save])}])

(defmulti modal :type)

(defn app-root [_]
  (let [page-name @(rf/subscribe [::get-app-root-page-name])]
    [:div
     (when-let [modal-props @(rf/subscribe [::get-app-root-modal-props])]
       [modal modal-props])
     (if (and guseragent/IE (not (guseragent/isVersionOrHigher 10)))
       [LegacyIECompatibility nil]
       (case page-name
         "404" [PageView404 nil]
         "Error" [PageViewError nil]
         "Edit" [PageViewEdit nil]
         "Dashboard" [dashboard nil]
         nil))]))
