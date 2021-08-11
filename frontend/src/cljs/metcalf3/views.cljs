(ns metcalf3.views
  (:require-macros [cljs.core.async.macros :refer [go alt! go-loop]])
  (:require [ajax.core :as ajax]
            [cljs.core.async :as async :refer [put! <! alts! chan pub sub timeout dropping-buffer]]
            [cljsjs.moment]
            [clojure.string :as string]
            [clojure.string :refer [blank?]]
            [cuerdas.core :as cuerdas]
            [goog.events :as gevents]
            [goog.object :as gobj]
            [goog.style :as gstyle]
            [goog.userAgent :as guseragent]
            [metcalf3.content :refer [contact-groups]]
            [metcalf3.handlers :as handlers]
            [metcalf3.logic :as logic]
            [metcalf3.utils :as utils]
            [interop.masked-input :as masked-input]
            [metcalf3.widget.boxmap :as boxmap]
            [metcalf3.widget.modal :refer [Modal]]
            [metcalf3.widget.select :refer [ReactSelect ReactSelectAsync ReactSelectCreatable ReactSelectAsyncCreatable VirtualizedSelect SelectComponents.Option* SelectComponents.ValueContainer*]]
            [metcalf3.widget.table :refer [Table Column Cell]]
            [metcalf3.widget.tree :refer [Tree TermTree TermList]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [interop.blueprint :as bp3]
            [interop.moment :as moment]
            [clojure.edn :as edn]
            [cljs.spec.alpha :as s])
  (:import [goog.dom ViewportSizeMonitor]
           [goog.events FileDropHandler]
           [goog.events.EventType]
           [goog.events.FileDropHandler.EventType]))

(defn userDisplay
  [user]
  (if (and (blank? (:lastName user)) (blank? (:firstName user)))
    (if (blank? (:email user))
      (:username user)
      (:email user))
    (str (:firstName user) " " (:lastName user))))

(defmulti ModalDialog :type)

(defn label-template [{:keys [label required] :as props}]
  (when label
    [:label label (if required " *")]))

(defn validation-state [{:keys [errors show-errors]}]
  (when (and show-errors (not (empty? errors)))
    "has-error"))

(defn dp-term-paths [dp-type]
;  (when js/goog.DEBUG (js/console.log "DP TERM PATHS" {:dp-type dp-type}))
  {:term              (keyword (str (name dp-type) "_term"))
   :vocabularyTermURL (keyword (str (name dp-type) "_vocabularyTermURL"))
   :vocabularyVersion (keyword (str (name dp-type) "_vocabularyVersion"))
   :termDefinition    (keyword (str (name dp-type) "_termDefinition"))})

(defn masked-text-widget
  [{:keys [mask value placeholder disabled on-change on-blur]}]
  [masked-input/masked-input
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
              (utils/on-change props next-props [:value] #(r/set-state this {:input-value %}))))

          (render [this]
            (let [{:keys [addon-before addon-after help on-change disabled mask maxlength] :as props} (r/props this)
                  {:keys [input-value]} (r/state this)]
              (let [input-props (-> props
                                    (dissoc :show-errors)
                                    (assoc :value (or input-value ""))
                                    (dissoc :maxlength)
                                    (assoc :maxLength maxlength)
                                    (assoc :on-change #(r/set-state this {:input-value (.. % -target -value)}))
                                    (assoc :on-blur #(on-change input-value))
                                    (assoc :key "ifc"))]
                [:div.form-group {:class    (validation-state props)
                                  :disabled disabled}
                 (label-template props)
                 (if (or addon-after addon-before)
                   [:div.input-group {:key "ig"} addon-before [:input.form-control input-props] addon-after]
                   (if mask
                     [masked-text-widget input-props]
                     [:input.form-control input-props]))
                 [:p.help-block help]])))]
    (r/create-class
      {:get-initial-state            init-state
       :component-will-receive-props component-will-receive-props
       :render                       render})))

(defn filter-name [s]
  (s/assert string? s)
  (string/replace s #"[^a-zA-Z-', ]" ""))

(defn NameInputWidget
  [_]
  (letfn [(init-state [this]
            (let [{:keys [value]} (r/props this)]
              {:input-value value}))

          (handle-change [this s]
            (let [s (filter-name s)]
              (r/set-state this {:input-value s})))

          (handle-blur [this]
            (let [{:keys [on-change]} (r/props this)
                  {:keys [input-value]} (r/state this)]
              (on-change input-value)))

          (component-will-receive-props [this new-argv]
            (let [[_ next-props] new-argv
                  props (r/props this)]
              (utils/on-change props next-props [:value] #(r/set-state this {:input-value %}))))

          (render [this]
            (let [{:keys [addon-before addon-after help on-change disabled mask] :as props} (r/props this)
                  {:keys [input-value]} (r/state this)]
              (let [input-props (-> props
                                    (dissoc :show-errors)
                                    (assoc :maxLength (:maxlength props))
                                    (dissoc :maxlength)
                                    (assoc :value (or input-value ""))
                                    (assoc :on-change #(handle-change this (.. % -target -value)))
                                    (assoc :on-blur #(handle-blur this))
                                    (assoc :key "ifc"))]
                [:div.form-group {:class    (validation-state props)
                                  :disabled disabled}
                 (label-template props)
                 (if (or addon-after addon-before)
                   [:div.input-group {:key "ig"} addon-before [:input.form-control input-props] addon-after]
                   (if mask
                     [masked-text-widget input-props]
                     [:input.form-control input-props]))
                 [:p.help-block help]])))]
    (r/create-class
      {:get-initial-state            init-state
       :component-will-receive-props component-will-receive-props
       :render                       render})))

(defn SimpleInputWidget
  [{:keys [value addon-before addon-after help on-change disabled] :as props} _]
  (let [input-props (assoc props
                      :value (or value "")
                      :on-change #(on-change (.. % -target -value))
                      :key "ifc")]
    [:div.form-group {:class    (validation-state props)
                      :disabled disabled}
     (label-template props)
     (if (or addon-after addon-before)
       [:div.input-group {:key "ig"} addon-before [:input.form-control input-props] addon-after]
       [:input.form-control input-props])
     [:p.help-block help]]))

(defn ExpandingTextareaWidget
  "http://alistapart.com/article/expanding-text-areas-made-elegant"
  [_]
  (letfn [(init-state [this]
            (let [{:keys [value]} (r/props this)]
              {:input-value value}))
          (component-will-receive-props [this new-argv]
            (let [[_ next-props] new-argv
                  props (r/props this)]
              (utils/on-change props next-props [:value] #(r/set-state this {:input-value %}))))
          (render [this]
            (let [{:keys [on-change disabled maxlength] :as props} (r/props this)
                  {:keys [input-value]} (r/state this)]
              (let [{:keys [is-hidden help]} props]
                [:div.form-group {:class    (str (validation-state props) " "
                                                 (when is-hidden "hidden"))
                                  :disabled disabled}
                 (label-template props)
                 [:div.expandingArea.active {:style {:position "relative"}}
                  [:pre (assoc props
                          :class "form-control")
                   [:span input-value] [:br]]
                  [:textarea (assoc props
                               :value input-value
                               :on-change #(r/set-state this {:input-value (.. % -target -value)})
                               :on-blur #(on-change input-value)
                               :class "form-control"
                               :key "textarea")]]
                 [:p.help-block help]])))]
    (r/create-class
      {:get-initial-state            init-state
       :component-will-receive-props component-will-receive-props
       :render                       render})))

(defn format-columns
  "Generate row with columns matching given width constraints.

  `flex` is a collection of column flex property values.
  You can pass any valid flex value string (containing up two three parameters grow/shrink/basis combined),
  but in the simplest case just put

  * `nil` to keep width fixed when appropriate `fixed` value provided, or to set flex grow to 1 otherwise
  * 0 to mark column as not growing wider than required to fit content
  * positive number to tell what part of available space should column take if possible

  `fixed` is a collection of column width property values.

  If both `flex` and `fixed` will have non-nil values for the same column,
  then both styles will be generated and usually you will get \"flex wins\" behaviour.

  `columns` is a collection of column contents."
  [flex fixed columns]
  [:div {:style {:display "flex"}}
   (map-indexed
     (fn [i column]
       (let [width (get fixed i)
             flex (get flex i (when-not width 1))]
         [:div {:style {:flex flex :width width}} column]))
     columns)])

(defn filter-table
  "Default search for local datasource: case-insensitive substring match"
  [simple? table query]
  (let [col-match? (if simple?
                     #(cuerdas/starts-with? (-> % str cuerdas/lower) (-> query str cuerdas/lower))
                     #(cuerdas/includes? (-> % str cuerdas/lower) (-> query str cuerdas/lower)))]
    (filter
      (fn [row]
        (some col-match? (rest row)))
      table)))

(defn InputField [props this]
  (let [field @(rf/subscribe [:subs/get-derived-path (:path props)])]
    [InputWidget (-> field
                     (merge (dissoc props :path))
                     (assoc
                       :on-change #(rf/dispatch [:handlers/value-changed (:path props) %])))]))

; TODO: Don't dispatch value changes until blur
; TODO: Show errors after blur
(defn date-field
  [path defMinDate]
  (let [{:keys [label labelInfo helperText value disabled change-v intent minDate maxDate]} @(rf/subscribe [:date-field/get-props path])
        format "DD-MM-YYYY"]
    [bp3/form-group
     {:label      label
      :labelInfo  labelInfo
      :helperText helperText
      :intent     intent}
     [bp3/date-input
      (cond-> {:formatDate  (fn [date] (moment/format date format))
               :parseDate   (fn [str] (moment/to-date (moment/moment str format)))
               :placeholder format
               :disabled    disabled
               :value       value
               :onChange    #(rf/dispatch (conj change-v %))
               :inputProps  {:leftIcon "calendar"
                             :intent   intent}}
              minDate (assoc :minDate minDate)
              (not minDate) (assoc :minDate defMinDate)
              maxDate (assoc :maxDate maxDate))]]))

(defn OptionWidget [props this]
  (let [[value display] props]
    [:option {:value value} display]))

(defn SelectWidget [props this]
  (let [{:keys [label required value help disabled errors is-hidden on-change
                options default-option default-value loading
                show-errors]
         :or   {is-hidden false}} props
        disabled (or disabled loading)
        default-value (or default-value "")
        default-option (or default-option "Please select")]
    (when-not is-hidden
      [:div.form-group {:class (if (and show-errors (not (empty? errors)))
                                 "has-error")}
       (if label [:label label (if required " *")])
       (vec (concat
              [:select.form-control (assoc (dissoc props :default-option :show-errors :is-hidden)
                                      :on-change #(on-change (-> % .-target .-value))
                                      :value (or value default-value)
                                      :disabled disabled)
               (if options
                 [:option {:value default-value :disabled true} default-option])]
              (for [option options]
                [OptionWidget option])))
       (if help [:p.help-block help])])))

(defn SelectField [path this]
  (let [{:keys [options default-option disabled] :as field} @(rf/subscribe [:subs/get-derived-path path])]
    [SelectWidget (assoc field
                    :class "wauto"
                    :disabled (or disabled (empty? options))
                    :default-option (if-not (empty? options) default-option "")
                    :on-blur #(rf/dispatch [:handlers/show-errors path])
                    :on-change #(rf/dispatch [:handlers/value-changed path %]))]))

(defn TextareaFieldProps [props this]
  (let [{:keys [path]} props
        field @(rf/subscribe [:subs/get-derived-path path])]
    [ExpandingTextareaWidget (merge field (dissoc props :path)
                                    {:on-change #(rf/dispatch [:handlers/value-changed path %])})]))

(comment
  (defn TextareaField [path this]
    (let [field @(rf/subscribe [:subs/get-derived-path path])]
      [ExpandingTextareaWidget
       (assoc field :on-change (fn [value] (rf/dispatch [:handlers/value-changed path value])))])))

(defn textarea-widget
  [{:keys [label labelInfo helperText maxlength value disabled change-v intent placeholder] :as props}]
  [bp3/form-group
   {:label      label
    :labelInfo  labelInfo
    :helperText helperText
    :intent     intent}
   [bp3/textarea2
    {:key            @(rf/subscribe [:subs/get-form-tick])
     :growVertically true
     :onBlur         #(rf/dispatch (conj change-v (-> % .-target .-value)))
     :disabled       disabled
     :placeholder    placeholder
     :maxLength      maxlength
     :defaultValue   value
     :fill           true
     :intent         intent}]])

(defn textarea-field
  [path]
  [textarea-widget @(rf/subscribe [:textarea-field/get-props path])])

(defn Checkbox [props this]
  (let [{:keys [label checked on-change disabled help]
         :or {:checked false}} props
        input-control [:input (merge {:type     "checkbox"
                                      :checked  (boolean checked)
                                      :disabled disabled
                                      :onChange on-change})]]
    [:div.form-group {:class (validation-state props)}
     [:div.checkbox
      [:label input-control label]]
     [:p.help-block help]]))

(defn CheckboxField [path label this]
  (let [field @(rf/subscribe [:subs/get-derived-path path])]
    [Checkbox (assoc field :checked (:value field)
                           :on-blur #(rf/dispatch [:handlers/show-errors path])
                           :on-change #(rf/dispatch [:handlers/set-value path (-> % .-target .-checked)])
                           :label (or label (:label field)))]))

(defn BackButton [props this]
  (let [page @(rf/subscribe [:subs/get-derived-path [:page]])
        back (:back page)]
    (if back [:button.btn.btn-default.BackButton
              {:on-click #(rf/dispatch [:handlers/back])}
              [:span.glyphicon.glyphicon-chevron-left] " Back"])))

(defmulti PageView (fn [page this] (get page :name)) :default "404")

(defn getter [k row] (get row k))

(defn update-table-width [this]
  (let [{:keys [autowidth-id]} (r/state this)
        autowidth (js/document.getElementById autowidth-id)
        width (.-width (gstyle/getSize autowidth))]
    (r/set-state this {:width width})))

(defn KeywordsThemeCell [rowData this]
  (let [rowData (take-while (complement empty?) rowData)]
    [:div.topic-cell
     [:div.topic-path (string/join " > " (drop-last (rest rowData)))]
     [:div.topic-value (last rowData)]]))

(defn TopicCategoryCell [rowData this]
  (let [rowData (take-while (complement empty?) rowData)]
    [:div.topic-cell
     [:div.topic-value (last rowData)]]))

(defn KeywordsThemeTable
  [keyword-type this]
  (letfn [(init-state [this]
            {:columnWidths     [26 (- 900 26)]
             :isColumnResizing false
             :query            ""
             ;:selected-filter  false
             :width            900
             :scrollToRow      0
             :autowidth-id     (name (gensym "autowidth"))})
          (did-mount [this]
            (let [vsm (ViewportSizeMonitor.)]
              (gevents/listen vsm goog.events.EventType.RESIZE #(update-table-width this))
              (update-table-width this)))
          (render [this]
            (let [{:keys [query width columnWidths isColumnResizing scrollToRow autowidth-id]} (r/state this)]
              (let [keywords-path [:form :fields :identificationInfo keyword-type :keywords]
                    keywords @(rf/subscribe [:subs/get-derived-path keywords-path])
                    uuids (zipmap (map :value (:value keywords)) (range))
                    table @(rf/subscribe [:subs/get-derived-path [:theme keyword-type :table]])
                    results (if (blank? query)
                              table
                              (vec (filter-table false table query)))
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
                                                                                         (rf/dispatch [:handlers/del-value keywords-path (uuids uuid)])
                                                                                         (rf/dispatch [:handlers/add-value! keywords-path uuid])))}]])))
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
                 [:p "There are " (count table) " keywords in our database"]])))]
    (r/create-class
      {:get-initial-state   init-state
       :component-did-mount did-mount
       :render              render})))

(defn handle-highlight-new [this item]
  (r/set-state this {:highlight (conj (:highlight (r/state this)) item)})
  (go (<! (timeout 5000))
      (r/set-state this {:highlight (disj (:highlight (r/state this)) item)})))

(defn modal-dialog-table-modal-edit-form
  [_ _]
  (let [{:keys [form path title]} @(rf/subscribe [:subs/get-modal-props])
        many-field-path (drop-last 2 path)]

    (letfn [(handle-delete-confirm []
              (rf/dispatch [:handlers/del-value many-field-path (last path)])
              (rf/dispatch [:handlers/close-modal]))

            (handle-delete-click [e]
              (.preventDefault e)
              (rf/dispatch [:handlers/open-modal {:type       :confirm
                                                  :title      "Delete " title "?"
                                                  :message    "Are you sure you want to delete?"
                                                  :on-confirm handle-delete-confirm}]))

            (handle-close-click []
              (rf/dispatch [:handlers/close-modal]))]

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
  [_ _]
  (let [{:keys [form path title]} @(rf/subscribe [:subs/get-modal-props])
        many-field-path (drop-last 2 path)
        many-field @(rf/subscribe [:subs/get-derived-path many-field-path])
        handle-cancel (fn [] (rf/dispatch [:handlers/del-value many-field-path (last path)])
                        (rf/dispatch [:handlers/close-modal]))]
    [Modal {:ok-copy      "Done"
            :modal-header [:span [:span.glyphicon.glyphicon-list] " Add " title]
            :modal-body   [form path]
            :on-dismiss   handle-cancel
            :on-cancel    handle-cancel
            :on-save      #(rf/dispatch [:handlers/close-modal])}]))

(defn TableModalEdit
  [{:keys [ths tds-fn form title field-path placeholder maxlength default-field on-new-click add-label]
    :or   {tds-fn    #(list (:value %))
           add-label "Add new"}} this]
  (let [{:keys [disabled] :as many-field} @(rf/subscribe [:subs/get-derived-path field-path])]

    (letfn [(edit! [field-path]
              (rf/dispatch [:handlers/open-modal {:type  :TableModalEditForm
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
                        (rf/dispatch [:handlers/add-field! field-path default-field])
                        (rf/dispatch [:handlers/new-field! field-path]))
                      (rf/dispatch [:handlers/open-modal {:type  :TableModalAddForm
                                                          :title title
                                                          :form  form
                                                          :path  new-field-path}])))))]

      [:div.TableInlineEdit
       (when-let [help (:help many-field)]
         [:p.help-block help])
       (if (or (not placeholder) (-> many-field :value count pos?))
         [:table.table {:class (when-not (or disabled (empty? (:value many-field))) "table-hover")}
          (if ths [:thead (-> [:tr]
                              (into (for [th ths] [:th th]))
                              (conj [:th.xcell " "]))])
          (if (not-empty (:value many-field))
            (into [:tbody]
                  (for [[idx field] (map-indexed vector (:value many-field))]
                    (let [field-path (conj field-path :value idx)
                          has-error (not (logic/is-valid? {:fields (:value field)}))]
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


;TODO: for when the VirtualizedSelect gets fixed
(comment (defn theme-option-renderer
           [props option]
           (let [rowData (get option "rowData")]
             [:div
              {:style (gobj/get props "style")}
              [KeywordsThemeCell rowData]])))

(defn theme-option-renderer
  [props options]
  (let [rowData (gobj/get props "rowData")]
    [:div
     [KeywordsThemeCell rowData]]))

(defn modal-dialog-theme-keywords
  [keyword-type this]
  (let [{:keys [message on-confirm on-cancel]} @(rf/subscribe [:subs/get-modal-props])]
    [Modal {:ok-copy      "Done"
            :dialog-class "modal-lg"
            :modal-header [:span [:span.glyphicon.glyphicon-list] " " "Research theme keywords"]
            :modal-body   [:div
                           [:p.help-block "Select keyword(s) to add to record"]
                           [KeywordsThemeTable keyword-type]]
            :on-dismiss   #(rf/dispatch [:handlers/close-modal])
            :on-save      #(rf/dispatch [:handlers/close-modal])}]))

(defn TopicCategories
  [_ this]
  (letfn [(init-state [this]
            {:new-value  nil
             :input      ""
             :show-modal false
             :highlight  #{}})
          (render [this]
            (let [{:keys [new-value show-modal highlight options]} (r/state this)]
              (let [topic-categories-path [:form :fields :identificationInfo :topicCategory]
                    topicCategories @(rf/subscribe [:subs/get-derived-path topic-categories-path])
                    {:keys [value placeholder disabled help] :as props} topicCategories
                    table @(rf/subscribe [:subs/get-derived-path [:topicCategories :table]])
                    set-value! #(r/set-state this {:new-value %})
                    add! (fn [identifier] (when-not (empty? identifier)
                                            (when (not-any? (comp #{identifier} :value)
                                                            (:value topicCategories))
                                              (rf/dispatch [:handlers/add-value! topic-categories-path identifier]))
                                            (handle-highlight-new this identifier)
                                            (set-value! nil)))
                    lookup (fn [uuid] (first (filterv #(= uuid (first %)) table)))
                    options (into-array (for [[value & path :as rowData] table]
                                          #js {:value   value
                                               :rowData rowData
                                               :label   (string/join " > " path)}))]
                [:div.ThemeKeywords {:class (validation-state props)}
                 (label-template props)
                 [:p.help-block help]
                 [:table.table.keyword-table {:class (if-not disabled "table-hover")}
                  (into [:tbody]
                        (for [[i topicCategory] (utils/enum value)]
                          [:tr {:class (if disabled "active" (if (highlight (:value topicCategory)) "highlight"))}
                           [:td [TopicCategoryCell (lookup (:value topicCategory))]]
                           (if-not disabled
                             [:td [:button.btn.btn-warn.btn-xs.pull-right
                                   {:on-click #(rf/dispatch [:handlers/del-value topic-categories-path i])}
                                   [:span.glyphicon.glyphicon-minus]]])]))]
                 (if-not disabled
                   [:div.flex-row
                    [:div.flex-row-field
                     {;need this to make sure the drop down is rendered above any other input fields
                      :style {:position "relative"
                              :z-index  10}}
                     [VirtualizedSelect {:placeholder       placeholder
                                         :options           options
                                         :value             ""
                                         :getOptionValue    (fn [option]
                                                              (gobj/get option "value"))
                                         :formatOptionLabel (fn [props]
                                                              (r/as-element (theme-option-renderer props options)))
                                         :onChange          (fn [option]
                                                              (add! (gobj/get option "value")))}]]])])))]
    (r/create-class
      {:get-initial-state init-state
       :render            render})))

(defn ThemeKeywords
  [keyword-type this]
  (letfn [(init-state [this]
            {:new-value  nil
             :input      ""
             :show-modal false
             :highlight  #{}})
          (render [this]
            (let [{:keys [new-value show-modal highlight options]} (r/state this)]
              (let [keywords-theme-path [:form :fields :identificationInfo keyword-type]
                    keywords-path (conj keywords-theme-path :keywords)
                    {:keys [keywords]} @(rf/subscribe [:subs/get-derived-path keywords-theme-path])
                    {:keys [value placeholder disabled help] :as props} keywords
                    theme-table @(rf/subscribe [:subs/get-derived-path [:theme keyword-type :table]])
                    set-value! #(r/set-state this {:new-value %})
                    add! (fn [uuid] (when-not (empty? uuid)
                                      (when (not-any? (comp #{uuid} :value)
                                                      (:value keywords))
                                        (rf/dispatch [:handlers/add-value! keywords-path uuid]))
                                      (handle-highlight-new this uuid)
                                      (set-value! nil)))
                    lookup (fn [uuid] (first (filterv #(= uuid (first %)) theme-table)))
                    show-modal! #(rf/dispatch [:handlers/open-modal {:type         :ThemeKeywords
                                                                     :keyword-type keyword-type}])
                    options (into-array (for [[value & path :as rowData] theme-table]
                                          #js {:value   value
                                               :rowData rowData
                                               :label   (string/join " > " path)}))]
                [:div.ThemeKeywords {:class (validation-state props)}
                 (label-template props)
                 [:p.help-block help]
                 [:table.table.keyword-table {:class (if-not disabled "table-hover")}
                  (into [:tbody]
                        (for [[i keyword] (utils/enum value)]
                          [:tr {:class (if disabled "active" (if (highlight (:value keyword)) "highlight"))}
                           [:td [KeywordsThemeCell (lookup (:value keyword))]]
                           (if-not disabled
                             [:td [:button.btn.btn-warn.btn-xs.pull-right
                                   {:on-click #(rf/dispatch [:handlers/del-value keywords-path i])}
                                   [:span.glyphicon.glyphicon-minus]]])]))]
                 (if-not disabled
                   [:div.flex-row
                    [:div.flex-row-field
                     {;need this to make sure the drop down is rendered above any other input fields
                      :style {:position "relative"
                              :z-index  10}}
                     #_[VirtualizedSelect {:props             {:options     options
                                                               :onChange    (fn [option]
                                                                              (add! (gobj/get option "value")))
                                                               :isClearable true}
                                           :list-props        {}
                                           :children-renderer theme-option-renderer}]
                     [VirtualizedSelect {:placeholder       placeholder
                                         :options           options
                                         :value             ""
                                         :getOptionValue    (fn [option]
                                                              (gobj/get option "value"))
                                         :formatOptionLabel (fn [props]
                                                              (r/as-element (theme-option-renderer props options)))
                                         :onChange          (fn [option]
                                                              (add! (gobj/get option "value")))}]]
                    [:div.flex-row-button
                     [:button.btn.btn-default
                      {:on-click #(show-modal!)}
                      [:span.glyphicon.glyphicon-list] " Browse"]]])])))]
    (r/create-class
      {:get-initial-state init-state
       :render            render})))

(defn ThemeInputField
  [{:keys [value placeholder errors extra-help on-change on-blur on-submit maxlength] :as props} this]
  [:div.form-group {:class (validation-state props)}
   (label-template props)
   [:div.input-group {:key "ig"}
    [:input.form-control {:value       (or value "")
                          :placeholder placeholder
                          :errors      errors
                          :maxLength   maxlength
                          :on-key-down #(when (= (.-key %) "Enter")
                                          (on-submit))
                          :on-change   on-change
                          :on-blur     on-blur
                          :key         "ifc"}]
    [:span.input-group-btn
     [:button.btn.btn-primary {:disabled (string/blank? value)
                               :on-click on-submit}
      [:span.glyphicon.glyphicon-plus]]]]
   [:p.help-block extra-help]])

(defn ThemeKeywordsExtra
  [_ this]
  (letfn [(init-state [this]
            {:highlight #{}})
          (render [this]
            (let [{:keys [highlight]} (r/state this)]
              (let [keywords-path [:form :fields :identificationInfo :keywordsThemeExtra :keywords]
                    keywords-value-path (conj keywords-path :value)
                    {:keys [value placeholder disabled errors new-value help maxlength] :as props} @(rf/subscribe [:subs/get-derived-path keywords-path])]
                (letfn [(set-value! [v]
                          (rf/dispatch [:handlers/setter keywords-path :new-value v]))
                        (add-value! []
                          (when-not (empty? new-value)
                            (rf/dispatch [:handlers/add-keyword-extra keywords-value-path new-value])
                            (handle-highlight-new this new-value)
                            (set-value! "")
                            (rf/dispatch [:handlers/check-unsaved-keyword-input keywords-path])))
                        (del-value! [x]
                          (rf/dispatch [:handlers/del-keyword-extra keywords-value-path x]))]
                  [:div.ThemeKeywordsExtra {:class (validation-state props)}
                   (label-template props)
                   [:p.help-block help]
                   [:table.table.keyword-table {:class (if-not disabled "table-hover")}
                    (into [:tbody]
                          (for [keyword value]
                            [:tr {:class (if disabled "active" (if (highlight (:value keyword)) "highlight"))}
                             [:td (:value keyword)]
                             (if-not disabled
                               [:td
                                [:button.btn.btn-warn.btn-xs.pull-right
                                 {:on-click #(del-value! (:value keyword))}
                                 [:span.glyphicon.glyphicon-minus]]])]))]
                   (if-not disabled
                     [ThemeInputField {:value       new-value
                                       :on-submit   add-value!
                                       :placeholder placeholder
                                       :errors      errors
                                       :help        help
                                       :maxlength   maxlength
                                       :extra-help  "We will contact you to discuss appropriate keyword terms"
                                       :on-change   (fn [e]
                                                      (set-value! (.. e -target -value)))
                                       :on-blur     (fn [] (js/setTimeout #(rf/dispatch [:handlers/check-unsaved-keyword-input keywords-path]) 100))}])]))))]
    (r/create-class
      {:get-initial-state init-state
       :render            render})))

(defn TaxonKeywordsExtra
  [_ this]
  (letfn [(init-state [this]
            {:highlight #{}})
          (render [this]
            (let [{:keys [highlight]} (r/state this)]
              (let [keywords-path [:form :fields :identificationInfo :keywordsTaxonExtra :keywords]
                    keywords-value-path (conj keywords-path :value)
                    {:keys [value required help placeholder disabled maxlength errors new-value] :as props} @(rf/subscribe [:subs/get-derived-path keywords-path])]

                (letfn [(set-value! [v]
                          (rf/dispatch [:handlers/setter keywords-path :new-value v]))
                        (add-value! []
                          (when-not (empty? new-value)
                            (rf/dispatch [:handlers/add-keyword-extra keywords-value-path new-value])
                            (handle-highlight-new this new-value)
                            (set-value! nil)
                            (rf/dispatch [:handlers/check-unsaved-keyword-input keywords-path])))
                        (del-value! [x]
                          (rf/dispatch [:handlers/del-keyword-extra keywords-value-path x]))
                        (handle-input-change [e]
                          (set-value! (.. e -target -value)))
                        (handle-input-blur []
                          (js/setTimeout #(rf/dispatch [:handlers/check-unsaved-keyword-input keywords-path]) 100))]

                  [:div.TaxonKeywordsExtra {:class (validation-state props)}
                   [:label "Taxon keywords" (if required " *")]
                   [:p.help-block help]
                   [:table.table.keyword-table {:class (if-not disabled "table-hover")}
                    (into [:tbody]
                          (for [keyword value]
                            [:tr {:class (if disabled "active" (if (highlight (:value keyword)) "highlight"))}
                             [:td (:value keyword)]
                             (if-not disabled
                               [:td [:button.btn.btn-warn.btn-xs.pull-right
                                     {:on-click #(del-value! (:value keyword))}
                                     [:span.glyphicon.glyphicon-minus]]])]))]
                   (if-not disabled
                     [ThemeInputField {:value       new-value
                                       :on-submit   add-value!
                                       :placeholder placeholder
                                       :errors      errors
                                       :maxlength   maxlength
                                       :help        help
                                       :on-change   handle-input-change
                                       :on-blur     handle-input-blur}])]))))]
    (r/create-class
      {:get-initial-state init-state
       :render            render})))



(defn geographicElement->extent
  "Transform our API specific bbox data into something generic for Openlayers"
  [{:keys [northBoundLatitude westBoundLongitude eastBoundLongitude southBoundLatitude] :as props}]
  (map :value [westBoundLongitude southBoundLatitude eastBoundLongitude northBoundLatitude]))

(defn ->float [s]
  (let [f (js/parseFloat s)]
    (if (js/isNaN f) nil f)))

(defn CoordInputWidget
  [_]
  (letfn [(init-state [this]
            (let [{:keys [value]} (r/props this)]
              {:input-value value}))
          (component-will-receive-props [this new-argv]
            (let [[_ next-props] new-argv
                  props (r/props this)]
              (utils/on-change props next-props [:value] #(r/set-state this {:input-value %}))))

          (render [this]
            (let [{:keys [addon-before addon-after help on-change value] :as props} (r/props this)
                  {:keys [input-value]} (r/state this)]
              (let [input-props (assoc props
                                  :value (or value "")
                                  :key "ifc")]
                [:div.form-group {:class (validation-state props)}
                 (label-template props)
                 (if (or addon-after addon-before)
                   [:div.input-group {:key "ig"} addon-before [:input.form-control input-props] addon-after]
                   [:input.form-control
                    (assoc input-props
                      :value input-value
                      :on-change #(r/set-state this {:input-value (.. % -target -value)})
                      :on-blur (fn [e]
                                 (let [v (.. e -target -value)
                                       f (->float v)]
                                   (r/set-state this {:input-value (str f)})
                                   (on-change f))))])
                 [:p.help-block help]])))]
    (r/create-class
      {:get-initial-state            init-state
       :component-will-receive-props component-will-receive-props
       :render                       render})))

(defn CoordInputField [props this]
  (let [field @(rf/subscribe [:subs/get-derived-path (:path props)])]
    [CoordInputWidget
     (-> field
         (merge (dissoc props :path))
         (assoc
           :on-change (fn [value]
                        (rf/dispatch [:handlers/value-changed (:path props) value]))))]))


(defn CoordField [path this]
  (let [n-field [CoordInputField {:path (conj path :value :northBoundLatitude)}]
        e-field [CoordInputField {:path (conj path :value :eastBoundLongitude)}]
        s-field [CoordInputField {:path (conj path :value :southBoundLatitude)}]
        w-field [CoordInputField {:path (conj path :value :westBoundLongitude)}]]
    [:div.CoordField
     [:div.row [:div.col-sm-6.col-sm-offset-3.col-lg-4.col-lg-offset-2
                [:div.n-block n-field]]]
     [:div.row
      [:div.col-sm-6.col-lg-4 [:div.w-block w-field]]
      [:div.col-sm-6.col-lg-4 [:div.e-block e-field]]]
     [:div.row
      [:div.col-sm-6.col-sm-offset-3.col-lg-4.col-lg-offset-2
       [:div.s-block s-field]]]]))

(defprotocol IPrintNice
  (print-nice [x]))

(extend-protocol IPrintNice
  number
  (print-nice [x] (.toFixed x 3))
  object
  (print-nice [x] (pr-str x))
  nil
  (print-nice [x] "--"))

(defn GeographicCoverage
  [_ this]
  (letfn [(render [this]
            (let [{hasGeographicCoverage :value} @(rf/subscribe [:subs/get-derived-path [:form :fields :identificationInfo :geographicElement :hasGeographicCoverage]])
                  boxes-path [:form :fields :identificationInfo :geographicElement :boxes]
                  {:keys [disabled] :as boxes} @(rf/subscribe [:subs/get-derived-path boxes-path])]

              [:div.GeographicCoverage
               [:h4 "Geographic Coverage"]
               (when hasGeographicCoverage
                 [:div.row
                  [:div.col-sm-6
                   [boxmap/box-map2-fill
                    {:boxes-path boxes-path
                     :map-props  {:boxes boxes}
                     :ref        (fn [boxmap] (r/set-state this {:boxmap boxmap}))
                     :disabled   disabled}]]
                  [:div.col-sm-6
                   [textarea-field [:form :fields :identificationInfo :geographicElement :siteDescription]]
                   [:p [:span "Please input in decimal degrees in coordinate reference system WGS84. "
                        "Geoscience Australia provide a "
                        [:a {:href "http://www.ga.gov.au/geodesy/datums/redfearn_grid_to_geo.jsp" :target "blank"}
                         "Grid to Geographic converter"]]]
                   [TableModalEdit {:ths           ["North limit" "West limit" "South limit" "East limit"]
                                    :tds-fn        (fn [geographicElement]
                                                     (let [{:keys [northBoundLatitude westBoundLongitude
                                                                   eastBoundLongitude southBoundLatitude]}
                                                           (:value geographicElement)]
                                                       [(print-nice (:value northBoundLatitude))
                                                        (print-nice (:value westBoundLongitude))
                                                        (print-nice (:value southBoundLatitude))
                                                        (print-nice (:value eastBoundLongitude))]))
                                    :default-field (-> (logic/new-value-field boxes)
                                                       (update-in [:value :northBoundLatitude] merge (:northBoundLatitude 0))
                                                       (update-in [:value :southBoundLatitude] merge (:southBoundLatitude 0))
                                                       (update-in [:value :eastBoundLongitude] merge (:eastBoundLongitude 0))
                                                       (update-in [:value :westBoundLongitude] merge (:westBoundLongitude 0)))
                                    :form          CoordField
                                    :title         "Geographic Coordinates"
                                    :on-new-click  nil
                                    :field-path    boxes-path}]]])]))]
    (r/create-class
      {:render render})))

(defn VerticalCoverage [props this]
  (let [{hasVerticalExtent :value} @(rf/subscribe [:subs/get-derived-path [:form :fields :identificationInfo :verticalElement :hasVerticalExtent]])]
    [:div.VerticalCoverage
     [:h4 "Vertical Coverage"]
     [CheckboxField [:form :fields :identificationInfo :verticalElement :hasVerticalExtent]]
     (when hasVerticalExtent
       [:div
        [SelectField [:form :fields :identificationInfo :verticalElement :method]]
        [InputField
         {:path  [:form :fields :identificationInfo :verticalElement :elevation]
          :class "wauto"}]
        [InputField
         {:path  [:form :fields :identificationInfo :verticalElement :minimumValue]
          :class "wauto"}]
        [InputField
         {:path  [:form :fields :identificationInfo :verticalElement :maximumValue]
          :class "wauto"}]])]))

(defn term-option-parent?
  [child parent]
  (and (= (aget parent "tree_id") (aget child "tree_id"))
       (< (aget parent "lft") (aget child "lft"))
       (> (aget parent "rgt") (aget child "rgt"))))

(defn term-option-path
  [options option]
  (->> options
       (filter (partial term-option-parent? option))
       (sort-by #(aget % "lft"))))

(defn api-option-renderer [options option]
  (let [text (string/join " > " (map #(aget % "term") (term-option-path options option)))
        text (if (> (count text) 80)
               (str (subs text 0 30) "..." (subs text (- (count text) 30) (count text)))
               text)
        term-text (aget option "term")]
    [:div.topic-cell {:key term-text}
     [:div.topic-path text]
     [:div.topic-value term-text]]))

(defn breadcrumb-renderer [options selected-option]
  (let [text (goog.object/get selected-option "breadcrumb")
        term-text (goog.object/get selected-option "term")
        alt-label (goog.object/get selected-option "altLabel")]
    [:div.topic-cell {:key term-text}
     [:div.topic-path text]
     [:div.topic-value term-text]
     [:div {:style
            {:margin-left 10 :color "#929292" :font-size 11}}
      (if (clojure.string/blank? alt-label) "" (concat "also known as " alt-label))]]))

(defn nasa-list-renderer [options option]
  (aget option "prefLabel"))

(defn other-term?
  [term vocabularyTermURL]
  (and (:value term) (empty? (:value vocabularyTermURL))))

(defn NasaListSelectField
  [_ this]
  (letfn [(will-mount [this]
            (let [{:keys [keyword] :as props} (r/props this)]
              (rf/dispatch [:handlers/load-api-options [:api keyword]])))
          (render [this]
            (let [{:keys [keyword path on-change]} (r/props this)
                  path (conj path keyword)
                  value-path (conj path :value 0 :value)
                  {:keys [options]} @(rf/subscribe [:subs/get-derived-path [:api keyword]])
                  {:keys [prefLabel uri] :as freq} @(rf/subscribe [:subs/get-derived-path value-path])
                  path-value @(rf/subscribe [:subs/get-derived-path path])
                  {:keys [label help required errors show-errors]} path-value]
              [:div
               (if label [:label label (if required " *")])
               [:div.flex-row
                [:div.flex-row-field
                 [:div.form-group {:class (if (and show-errors (not (empty? errors))) "has-error")}
                  (ReactSelect
                    {:value             #js {:uri (:value uri) :prefLabel (:value prefLabel)}
                     :options           options
                     :is-searchable     true
                     :getOptionValue    (fn [option]
                                          (gobj/get option "prefLabel"))
                     :formatOptionLabel (fn [props]
                                          (r/as-element (nasa-list-renderer options props)))
                     :onChange          (fn [option]
                                          (rf/dispatch [:handlers/update-nasa-list-value value-path option]))
                     :noResultsText     "No results found.  Click browse to add a new entry."})
                  [:p.help-block help]]]]]))]
    (r/create-class
      {:component-will-mount will-mount
       :render               render})))

(defn Tooltip
  [value]
  [:span " "
   [:i.icon-info-sign.tern-tooltip
    [:span.tern-tooltiptext value]]])

(defn ElasticsearchSelectField
  [_ this]
  (letfn [(will-mount [this]
            (let [{:keys [api-path param-type] :as state} (r/props this)
                  form-position (get (get-in state [:dp-term-path]) 5)]
              (rf/dispatch [:handlers/search-es-options api-path ""])))
          (render [this]
            (let [{:keys [dp-type dp-term-path api-path param-type disabled tooltip] :as state} (r/props this)]
              (let [sub-paths (dp-term-paths dp-type)
                    {:keys [options]} @(rf/subscribe [:subs/get-derived-path api-path])
                    term @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:term sub-paths))])
                    vocabularyTermURL @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:vocabularyTermURL sub-paths))])
                    {:keys [value label help required errors show-errors tooltip]} term
                    selectable-options (into-array (filterv #(gobj/get % "is_selectable") options))
                    new-term? (other-term? term vocabularyTermURL)
                    form-position (get (get-in state [:dp-term-path]) 5)]
                [:div
                 (if new-term?
                   [:span.pull-right.new-term.text-primary
                    [:span.glyphicon.glyphicon-asterisk]
                    " New term"])
                 [:div.flex-row
                  [:div.flex-row-field
                   [:div.form-group {:class (if (and show-errors (not (empty? errors))) "has-error")}
                    (if label
                   [:label label
                    (if required " *")
                    (if tooltip [Tooltip tooltip])])
                    (if-not new-term?
                      (ReactSelect
                        {:value             (if (blank? (:value vocabularyTermURL))
                                              nil
                                              #js {:vocabularyTermURL (:value vocabularyTermURL) :term (:value term)})
                         :options           selectable-options
                         :placeholder       (:placeholder term)
                         :isClearable       true
                         :is-searchable     true
                         :onInputChange     (fn [query]
                                              (rf/dispatch [:handlers/search-es-options api-path query])
                                              query)
                         :getOptionValue    (fn [option]
                                              (gobj/get option "term"))
                         :formatOptionLabel (fn [props]
                                              (r/as-element (breadcrumb-renderer options props)))
                         :filterOption      (fn [option, rawInput]
                                              ; Return true always. This allows for matches on label as well as altLabel (or other fields available in the REST API).
                                              (boolean 0))
                         :onChange          (fn [option]
                                              (rf/dispatch [:handlers/update-dp-term dp-term-path sub-paths option]))
                         :noResultsText     "No results found.  Click browse to add a new entry."
                         :isDisabled        disabled})

                      (ReactSelect
                        {:value             #js {:vocabularyTermURL "(new term)" :term (:value term)}
                         :options           selectable-options
                         :placeholder       (:placeholder term)
                         :is-searchable     true
                         :isClearable       true
                         :getOptionValue    (fn [option]
                                              (gobj/get option "term"))
                         :formatOptionLabel (fn [props]
                                              (r/as-element (breadcrumb-renderer options props)))
                         :onChange          (fn [option]
                                              (rf/dispatch [:handlers/update-dp-term dp-term-path sub-paths option]))
                         :noResultsText     "No results found.  Click browse to add a new entry."}))
                    [:p.help-block help]]]
                  ; TODO: Re-enable this in the future to browse/create vocabulary terms.
;                  [:div.flex-row-button
;                   [:button.btn.btn-default
;                    {:style    {:vertical-align "top"}
;                     :on-click #(rf/dispatch [:handlers/open-modal
;                                              {:type         param-type
;                                               :api-path     api-path
;                                               :dp-term-path dp-term-path}])}
;                    [:span.glyphicon.glyphicon-edit] " Custom"]
;                   (when help [:p.help-block {:dangerouslySetInnerHTML {:__html "&nbsp;"}}])]
                  ]])))]
    (r/create-class
      {:component-will-mount will-mount
       :render               render})))

(defn ApiTermSelectField
  [_ this]
  (letfn [(will-mount [this]
            (let [{:keys [api-path]} (r/props this)]
              (rf/dispatch [:handlers/load-api-options api-path])))
          (render [this]
            (let [{:keys [dp-type dp-term-path api-path param-type] :as state} (r/props this)]
              (let [sub-paths (dp-term-paths dp-type)
                    {:keys [options uri]} @(rf/subscribe [:subs/get-derived-path api-path])
                    {:keys [URL_ROOT]} @(rf/subscribe [:subs/get-derived-path [:context]])
                    term @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:term sub-paths))])
                    vocabularyTermURL @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:vocabularyTermURL sub-paths))])
                    {:keys [value label help required errors show-errors]} term
                    selectable-options (into-array (filterv #(gobj/get % "is_selectable") options))
                    new-term? (other-term? term vocabularyTermURL)
                    selected-value (if (blank? (:value vocabularyTermURL))
                                     ""
                                     #js {:vocabularyTermURL (:value vocabularyTermURL) :term (:value term)})]
                [:div
                 (if new-term? [:span.pull-right.new-term.text-primary
                                [:span.glyphicon.glyphicon-asterisk]
                                " New term"])
                 (if label [:label label (if required " *")])
                 [:div.flex-row
                  [:div.flex-row-field
                   [:div.form-group {:class (if (and show-errors (not (empty? errors))) "has-error")}
                    (if-not new-term?
                      [VirtualizedSelect {:placeholder       (:placeholder term)
                                          :isClearable       true
                                          :is-searchable     true
                                          :height            "40px"
                                          :options           selectable-options
                                          :value             selected-value
                                          :getOptionValue    (fn [option]
                                                               (gobj/get option "term"))
                                          :isOptionSelected  (fn [option selected]
                                                               (= (gobj/get option "vocabularyTermURL") (gobj/get (first selected) "vocabularyTermURL")))
                                          :noResultsText     "No results found.  Click browse to add a new entry."
                                          :formatOptionLabel (fn [props]
                                                               (r/as-element (api-option-renderer options props)))
                                          :onChange          (fn [option]
                                                               (rf/dispatch [:handlers/update-dp-term dp-term-path sub-paths option]))}]


                      [VirtualizedSelect {:placeholder       (:placeholder term)
                                          :isClearable       true
                                          :is-searchable     true
                                          :height            "40px"
                                          :options           selectable-options
                                          :value             #js {:vocabularyTermURL "(new term)" :term (:value term)}
                                          :getOptionValue    (fn [option]
                                                               (gobj/get option "term"))
                                          :isOptionSelected  (fn [option selected]
                                                               (= (gobj/get option "vocabularyTermURL") (gobj/get (first selected) "vocabularyTermURL")))
                                          :noResultsText     "No results found.  Click browse to add a new entry."
                                          :formatOptionLabel (fn [props]
                                                               (r/as-element (api-option-renderer options props)))
                                          :onChange          (fn [option]
                                                               (rf/dispatch [:handlers/update-dp-term dp-term-path sub-paths option]))}])
                    [:p.help-block help]]]
                  ; TODO: Re-enable this in the future to browse/create vocabulary terms.
;                  [:div.flex-row-button
;                   [:button.btn.btn-default
;                    {:style    {:vertical-align "top"}
;                     :on-click #(rf/dispatch [:handlers/open-modal
;                                              {:type         param-type
;                                               :api-path     api-path
;                                               :dp-term-path dp-term-path}])}
;                    [:span.glyphicon.glyphicon-list] " Browse"]
;                   (when help [:p.help-block {:dangerouslySetInnerHTML {:__html "&nbsp;"}}])]
                  ]])))]
    (r/create-class
      {:component-will-mount will-mount
       :render               render})))

(defn PersonListWidget
  [_]
  (letfn [(will-mount [this]
            (let [{:keys [api-path value on-change]} (r/props this)]
              (rf/dispatch [:handlers/load-api-options api-path])))
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

(defn ApiListWidget
  [_]
  (letfn [(will-mount [this]
            (let [{:keys [api-path value on-change]} (r/props this)]
              (rf/dispatch [:handlers/load-api-options api-path])))
          (render [this]
            (let [{:keys [api-path value on-change]} (r/props this)]
              (when-let [{:keys [options]} @(rf/subscribe [:subs/get-derived-path api-path])]
                ; TODO: review performance
                (let [options (js->clj options :keywordize-keys true)
                      option-value (first (filter #(-> % :URI (= value)) options))]
                  [TermList
                   {:value     option-value
                    :value-key :URI
                    :options   options
                    :on-select on-change}]))))]
    (r/create-class
      {:component-will-mount will-mount
       :render               render})))

(defn ApiTreeWidget
  [_]
  (letfn [(will-mount [this]
            (let [{:keys [api-path value on-change]} (r/props this)]
              (rf/dispatch [:handlers/load-api-options api-path])))
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

(defn ApiTermListField
  [{:keys [api-path dp-term-path dp-type sort?]} this]
  (let [sub-paths (dp-term-paths dp-type)
        term @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:term sub-paths))])
        vocabularyTermURL @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:vocabularyTermURL sub-paths))])
        {:keys [value label help required errors show-errors]} term
        {:keys [options]} @(rf/subscribe [:subs/get-derived-path api-path])]
    [:div.form-group {:class (if (and show-errors (not (empty? errors))) "has-error")}
     (if label [:label label (if required " *")])
     [ApiListWidget
      {:api-path  api-path
       :value     (:value vocabularyTermURL)
       :options   options
       :on-change (fn [option]
                    (rf/dispatch [:handlers/update-dp-term dp-term-path sub-paths option]))}]
     [:p.help-block "There are " (count options) " terms in this vocabulary"]]))

(defn MethodListField
  [{:keys [api-path method-path sort?]} this]
  (let [{:keys [name uri description] :as method-term} @(rf/subscribe [:subs/get-derived-path method-path])
        {:keys [value label help required errors show-errors]} name
        {:keys [options]} @(rf/subscribe [:subs/get-derived-path api-path])]
    [:div.form-group {:class (if (and show-errors (not (empty? errors))) "has-error")}
     (if label [:label label (if required " *")])
     [ApiListWidget
      {:api-path  api-path
       :value     (:value uri)
       :options   options
       :on-change (fn [option]
                    (rf/dispatch [:handlers/update-method-term method-path option]))}]
     [:p.help-block "There are " (count options) " terms in this vocabulary"]]))

(defn ApiTermTreeField
  [{:keys [api-path dp-term-path dp-type sort?]} this]
  (let [sub-paths (dp-term-paths dp-type)
        term @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:term sub-paths))])
        vocabularyTermURL @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:vocabularyTermURL sub-paths))])
        {:keys [value label help required errors show-errors]} term
        {:keys [options]} @(rf/subscribe [:subs/get-derived-path api-path])]
    [:div.form-group {:class (if (and show-errors (not (empty? errors))) "has-error")}
     (if label [:label label (if required " *")])
     [ApiTreeWidget
      {:api-path  api-path
       :value     (:value vocabularyTermURL)
       :options   options
       :on-change (fn [option]
                    (rf/dispatch [:handlers/update-dp-term dp-term-path sub-paths option]))}]
     [:p.help-block "There are " (count options) " terms in this vocabulary"]]))

(defn TermOrOtherForm
  "docstring"
  [{:keys [api-path dp-term-path dp-type] :as props} this]
  (let [sub-paths (dp-term-paths dp-type)
        term @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:term sub-paths))])
        vocabularyTermURL @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:vocabularyTermURL sub-paths))])]
    [:div
     [:p "Select a term from the vocabulary"]
     [ApiTermTreeField props]
     [:p "Or define your own"]
     [InputWidget
      (assoc term
        :value (if (other-term? term vocabularyTermURL) (:value term) "")
        :on-change (fn [v]
                     (rf/dispatch [:handlers/update-dp-term dp-term-path sub-paths #js {:term              v
                                                                                        :vocabularyTermURL "http://linkeddata.tern.org.au/XXX"}]))
        :placeholder ""
        :maxlength 100)]]))

(defn UnitTermOrOtherForm
  "docstring"
  [{:keys [api-path dp-term-path dp-type] :as props} this]
  (let [sub-paths (dp-term-paths dp-type)
        term @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:term sub-paths))])
        vocabularyTermURL @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:vocabularyTermURL sub-paths))])]
    [:div
     [:p "Define your own unit"]
     [InputWidget
      (assoc term
        :value (if (other-term? term vocabularyTermURL) (:value term) "")
        :on-change (fn [v]
                     (rf/dispatch [:handlers/update-dp-term dp-term-path sub-paths #js {:term              v
                                                                                        :vocabularyTermURL "http://linkeddata.tern.org.au/XXX"}]))
        :placeholder ""
        :maxlength 100)]]))

(defn PersonListField
  [{:keys [api-path person-path sort?]} this]
  (let [{:keys [name uri description] :as person-term} @(rf/subscribe [:subs/get-derived-path person-path])
        {:keys [value label help required errors show-errors]} name
        {:keys [options]} @(rf/subscribe [:subs/get-derived-path api-path])]
    [:div.form-group {:class (if (and show-errors (not (empty? errors))) "has-error")}
     (if label [:label label (if required " *")])
     [PersonListWidget
      {:api-path  api-path
       :value     (:value uri)
       :options   options
       :on-change (fn [option]
                    (rf/dispatch [:handlers/update-person person-path option]))}]
     [:p.help-block "There are " (count options) " terms in this vocabulary"]]))

(defn PersonForm
  "docstring"
  [props this]
  (let [{:keys [path]} props
        props {:person-path path
               :api-path    [:api :person]}]
    [:div
     [:p "Select a person"]
     [PersonListField props]]))

(defn modal-dialog-parametername
  [_ this]
  (let [props @(rf/subscribe [:subs/get-modal-props])]
    [Modal {:ok-copy      "Done"
            :modal-header [:span [:span.glyphicon.glyphicon-list] " " "Browse parameter names"]
            :modal-body   [TermOrOtherForm (assoc props :dp-type :longName)]
            :on-dismiss   #(rf/dispatch [:handlers/close-modal])
            :on-save      #(rf/dispatch [:handlers/close-modal])}]))

(defn modal-dialog-parameterunit
  [_ this]
  (let [props @(rf/subscribe [:subs/get-modal-props])]
    [Modal {:ok-copy      "Done"
            :modal-header [:span [:span.glyphicon.glyphicon-list] " " "Add parameter unit"]
            :modal-body   [:div [UnitTermOrOtherForm (-> props
                                                         (assoc :sort? false)
                                                         (assoc :dp-type :unit))]]
            :on-dismiss   #(rf/dispatch [:handlers/close-modal])
            :on-save      #(rf/dispatch [:handlers/close-modal])}]))

(defn modal-dialog-parameterinstrument
  [_ this]
  (let [props @(rf/subscribe [:subs/get-modal-props])]
    [Modal {:ok-copy      "Done"
            :modal-header [:span [:span.glyphicon.glyphicon-list] " " "Browse parameter instruments"]
            :modal-body   [:div
                           [TermOrOtherForm (assoc props :dp-type :instrument)]]
            :on-dismiss   #(rf/dispatch [:handlers/close-modal])
            :on-save      #(rf/dispatch [:handlers/close-modal])}]))

(defn modal-dialog-parameterplatform
  [_ this]
  (let [props @(rf/subscribe [:subs/get-modal-props])]
    [Modal {:ok-copy      "Done"
            :modal-header [:span [:span.glyphicon.glyphicon-list] " " "Browse parameter platforms"]
            :modal-body   [:div
                           [TermOrOtherForm (assoc props :dp-type :platform)]]
            :on-dismiss   #(rf/dispatch [:handlers/close-modal])
            :on-save      #(rf/dispatch [:handlers/close-modal])}]))

(defn modal-dialog-person
  [_ this]
  (let [props @(rf/subscribe [:subs/get-modal-props])]
    [Modal {:ok-copy      "Done"
            :modal-header [:span [:span.glyphicon.glyphicon-list] " " "Browse people"]
            :modal-body   [:div
                           [PersonForm props]]
            :on-dismiss   #(rf/dispatch [:handlers/close-modal])
            :on-save      #(rf/dispatch [:handlers/close-modal])}]))

(defn DataParameterRowEdit [path this]
  (let [base-path (conj path :value)
        name-path (conj path :value :name)
        serialNumber-path (conj path :value :serialNumber)
        form-position (get name-path 5)
        selected-platform @(rf/subscribe [:subs/platform-selected? form-position])]
    [:div.DataParameterMaster
     [:form/fieldset.tern-fieldset
      [:div
       {:class "alert alert-success"}
       "Request new controlled vocabulary terms if they do not exist in the drop-down fields using the feedback button to the right of the screen."]
      [ElasticsearchSelectField {:param-type :parametername
                                 :api-path [:api :parametername]
                                 :dp-term-path base-path
                                 :dp-type :longName}]
      [InputField {:path name-path}]]
     [:form/fieldset.tern-fieldset
      [ElasticsearchSelectField {:param-type :parameterunit
                                 :api-path [:api :parameterunit]
                                 :dp-term-path base-path
                                 :dp-type :unit}]]
     [:form/fieldset.tern-fieldset
      [ElasticsearchSelectField {:param-type :parameterplatform
                                 :api-path [:api :parameterplatform]
                                 :dp-term-path base-path
                                 :dp-type :platform}]
      [ElasticsearchSelectField {:param-type :parameterinstrument
                                 :api-path [:api :parameterinstrument]
                                 :dp-term-path base-path
                                 :dp-type :instrument}]
       ; TODO: This should be enabled only when an instrument is created. Currently, creating vocabulary terms is disabled.
      [InputField
       {:path serialNumber-path
        :disabled (boolean (if selected-platform nil 0))}]]
     ]))

(defn DataParametersTable [path this]
  [:div.DataParametersTable
   [TableModalEdit
    {:ths        ["Name" "Units" "Instrument" "Serial No." "Platform"]
     :tds-fn     (fn [field]
                   (let [{:keys [longName_term unit_term instrument_term serialNumber platform_term]} (:value field)]
                     (mapv #(:value %) [longName_term unit_term instrument_term serialNumber platform_term])))
     :form       DataParameterRowEdit
     :title      "Parameter"
     :add-label  "Add data parameter"
     :field-path [:form :fields :identificationInfo :dataParameters]}]])

(defn upload! [this {:keys [url fields]} file reset-file-drop]
  (r/set-state this {:uploading true})
  (let [fd (js/FormData.)
        xhr (js/XMLHttpRequest.)]
    (.open xhr "POST" url true)
    (set! (.-onreadystatechange xhr)
          (fn []
            (when (= (.-readyState xhr) 4)
              (if (#{200 201} (.-status xhr))
                (rf/dispatch [:handlers/add-attachment (utils/map-keys keyword (js->clj (.parse js/JSON (.-response xhr))))])
                (rf/dispatch [:handlers/open-modal
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

(defn handle-file [this file]
  (let [{:keys [reset-ch max-filesize]} (r/props this)]
    (if (or (not max-filesize)
            (<= (.-size file) (* 1024 1024 max-filesize)))
      (r/set-state this {:file file})
      (when max-filesize
        (rf/dispatch [:handlers/open-modal
                      {:type    :alert
                       :message (str "Please, choose file less than " max-filesize "mb")}])
        (put! reset-ch true)))))

(defn FileDrop [{:keys [on-change reset-ch placeholder
                        reset-ch max-filesize]} this]
  (letfn [(init-state [this]
            {:file-id (name (gensym "file"))})
          (did-mount [this]
            (gevents/listen
              (FileDropHandler. js/document)
              goog.events.FileDropHandler.EventType.DROP
              #(handle-file this (.. % getBrowserEvent -dataTransfer -files (item 0))))
            (go-loop []
                     (when (<! reset-ch)
                       (r/set-state this {:file nil})
                       (recur))))
          (did-update [this [_ _ prev-state]]
            (let [{:keys [file]} (r/state this)]
              (when (and on-change (not= file (:file prev-state)))
                (on-change file))))
          (render [this]
            (let [{:keys [file uploading file-id]} (r/state this)]
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
                             :z-index  999
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
  (rf/dispatch [:handlers/open-modal
                {:type       :confirm
                 :title      "Delete?"
                 :message    "Are you sure you want to delete this file?"
                 :on-confirm #(rf/dispatch [:handlers/del-value attachments-path attachment-idx])}]))

(defn UploadData
  [_ this]
  (letfn [(init-state [this]
            {:reset-file-drop (chan)})
          (render [this]
            (let [{:keys [file reset-file-drop uploading]} (r/state this)]
              (let [attachments-path [:form :fields :attachments]
                    {:keys [disabled] :as attachments} @(rf/subscribe [:subs/get-derived-path attachments-path])
                    upload-form @(rf/subscribe [:subs/get-derived-path [:upload_form]])]
                [:div.UploadData {:class (if disabled "disabled")}
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
                     {:on-click #(upload! this upload-form file reset-file-drop)
                      :disabled (or uploading (not file))}
                     "Confirm Upload"]])])))]
    (r/create-class
      {:get-initial-state init-state
       :render            render})))

(defn save!
  []
  (rf/dispatch [:handlers/save-current-document]))

(defn handle-submit-click
  []
  (rf/dispatch [:handlers/lodge-click]))

(defn Lodge
  [_]
  (letfn [(init-state [this]
            {:is-open        false
             :is-open-inline false})
          (render [this]
            (let [{:keys [is-open is-open-inline]} (r/state this)]
              (let [page @(rf/subscribe [:subs/get-page-props])
                    saving (::handlers/saving? page)
                    {:keys [document urls site]} @(rf/subscribe [:subs/get-derived-path [:context]])
                    {:keys [portal_title portal_url email]} site
                    {:keys [errors]} @(rf/subscribe [:subs/get-derived-path [:progress]])
                    {:keys [terms_pdf]} @(rf/subscribe [:subs/get-derived-path [:context :site]])
                    {:keys [disabled dirty]} @(rf/subscribe [:subs/get-derived-path [:form]])
                    noteForDataManager @(rf/subscribe [:subs/get-derived-path [:form :fields :noteForDataManager]])
                    agreedToTerms @(rf/subscribe [:subs/get-derived-path [:form :fields :agreedToTerms]])
                    doiRequested @(rf/subscribe [:subs/get-derived-path [:form :fields :doiRequested]])
                    currentDoi @(rf/subscribe [:subs/get-derived-path [:form :fields :identificationInfo :doi]])
                    is-are (if (> errors 1) "are" "is")
                    plural (if (> errors 1) "s")
                    has-errors? (and errors (> errors 0))
                    submitted? (= (:status document) "Submitted")]
                [:div.Lodge
                 [:p "Are you finished? Use this page to lodge your completed metadata record."]
                 [:p "Any difficulties?  Please contact " [:a {:href (str "mailto:" email)} email]]
                 [:p "The Data Manager will be notified of your submission and will be in contact
               if any further information is required. Once approved, your data will be archived
               for discovery in the "
                  (if portal_url
                    [:a {:href portal_url :target "_blank"} [:span.portal_title portal_title]]
                    [:span.portal_title portal_title])
                  "."]
                 [:p "How complete is your data?"]
                 [:div
                  {:style {:padding-top    5
                           :padding-bottom 5}}
                  (if (= "Draft" (:status document))
                    [textarea-field [:form :fields :noteForDataManager]]
                    (when-not (string/blank? (:value noteForDataManager))
                      [:div
                       [:strong "Note for the data manager:"]
                       [:p (:value noteForDataManager)]]))]
                 [:div
                  [CheckboxField [:form :fields :doiRequested] [:span "Please mint a DOI for this submission (If the DOI already exists, input details in the field above)"]]]
                 (when (:value doiRequested) (if (blank? (:value currentDoi))
                                               [:p [:strong "DOI not yet minted"]]
                                               [:p [:strong "Minted DOI: "] (:value currentDoi)]))
                 [:div
                  [:a
                   {:onClick #(r/set-state this {:is-open (not is-open)})}
                   [:div [:b (str (if is-open "Hide" "Show") " Terms & Conditions ") (if is-open [:span.glyphicon.glyphicon-collapse-up]
                                                                                                 [:span.glyphicon.glyphicon-collapse-down])]]]
                  [bp3/collapse
                   {:isOpen is-open}
                   [:iframe {:width  "100%"
                             :height "600px"
                             :src    terms_pdf}]]
                  [CheckboxField [:form :fields :agreedToTerms] [:span "I have read and agree to the terms and conditions."]]
                  [:button.btn.btn-primary.btn-lg
                   {:disabled (or has-errors? saving disabled submitted? (not (:value agreedToTerms)))
                    :on-click handle-submit-click}
                   (when saving
                     [:img
                      {:src (str (:STATIC_URL urls)
                                 "metcalf3/img/saving.gif")}])
                   "Lodge data"]
                  " "

                  (if has-errors?
                    [:span.text-danger [:b "Unable to lodge: "]
                     "There " is-are " " [:span errors " error" plural
                                          " which must be corrected first."]]
                    [:span.text-success
                     [:b
                      (cond
                        saving "Submitting..."
                        (= (:status document) "Draft") "Ready to lodge"
                        (= (:status document) "Submitted") "Your record has been submitted."
                        :else (:status document))]])]

                 (let [download-props {:href     (str (:export_url document) "?download")
                                       :on-click #(when dirty
                                                    (.preventDefault %)
                                                    (rf/dispatch [:handlers/open-modal
                                                                  {:type    :alert
                                                                   :message "Please save changes before exporting."}]))}]
                   [:div.user-export
                    [:p [:strong "Want to keep a personal copy of your metadata record?"]]
                    [:p
                     [:a download-props "Click here"] " to generate an XML version of your metadata submission. "
                     "The file generated includes all of the details you have provided under the
                      tabs, but not files you have uploaded."]
                    [:p
                     "Please note: this XML file is not the recommended way to share your metadata.
                      We want you to submit your data via 'lodging' the information.
                      This permits multi-user access via the portal in a more friendly format."]])])))]
    (r/create-class
      {:get-initial-state init-state
       :render            render})))

(defn AddressField [address-path this]
  (let [address @(rf/subscribe [:subs/get-derived-path address-path])
        {:keys [city postalCode administrativeArea country deliveryPoint deliveryPoint2]} address]
    [:div.AddressField
     [InputWidget (assoc deliveryPoint
                    :on-change #(rf/dispatch [:handlers/value-changed (conj address-path :deliveryPoint) %]))]
     [InputWidget (assoc deliveryPoint2
                    :on-change #(rf/dispatch [:handlers/value-changed (conj address-path :deliveryPoint2) %]))]
     [:div.row
      [:div.col-xs-6
       [InputWidget (assoc city
                      :help "City"
                      :on-change #(rf/dispatch [:handlers/value-changed (conj address-path :city) %]))]]
      [:div.col-xs-6
       [InputWidget (assoc administrativeArea
                      :help "State/territory"
                      :on-change #(rf/dispatch [:handlers/value-changed (conj address-path :administrativeArea) %]))]]]
     [:div.row
      [:div.col-xs-6
       [InputWidget (assoc postalCode
                      :help "Postal / Zip code"
                      :on-change #(rf/dispatch [:handlers/value-changed (conj address-path :postalCode) %]))]]
      [:div.col-xs-6
       [InputWidget (assoc country
                      :help "Country"
                      :on-change #(rf/dispatch [:handlers/value-changed (conj address-path :country) %]))]]]]))

(defn organisation-option-renderer
  [{:keys [focusedOption focusOption option selectValue optionHeight] :as args}]
  (let [{:keys [organisationName]} option
        className (if (identical? option focusedOption)
                    "VirtualizedSelectOption VirtualizedSelectFocusedOption"
                    "VirtualizedSelectOption")]
    [:div
     {:class         className
      :on-click      #(selectValue option)
      :on-mouse-over #(focusOption option)
      :style         {:height optionHeight}}
     [:span organisationName]]))

(defn set-input-value
  [this]
  #_(when-let [select (some-> (om/get-ref this "picker") (.-refs) (gobj/get "select"))]
      (.setState select #js {:inputValue (or input "")})))

(defn OrganisationPickerWidget
  [_]
  (letfn [(component-did-mount [this]
            (set-input-value this))
          (render [this]
            (let [{:keys [on-input-change on-blur on-change disabled party-path] :as props} (r/props this)]
              (let [{:keys [URL_ROOT]} @(rf/subscribe [:subs/get-derived-path [:context]])
                    orgId (:value @(rf/subscribe [:subs/get-derived-path (conj party-path :value :organisationIdentifier)]))
                    orgName (:value @(rf/subscribe [:subs/get-derived-path (conj party-path :value :organisationName)]))
                    orgCity (:value @(rf/subscribe [:subs/get-derived-path (conj party-path :value :address :city)]))
                    value @(rf/subscribe [:subs/get-derived-path (conj party-path :value)])
                    {:keys [base-options]} @(rf/subscribe [:subs/get-derived-path [:api :institution]])
                    js-value #js {:uri              (or orgId "")
                                  :organisationName (or (if (blank? orgCity)
                                                          orgName
                                                          (str orgName " - " orgCity)) "")}
                    js-value (if orgId
                               js-value
                               nil)]
                ; TODO: this really doesn't need to be async
                (ReactSelectAsyncCreatable
                  {:value             js-value
                   :disabled          disabled
                   :defaultOptions    true
                   :getOptionValue    (fn [option]
                                        (str (gobj/get option "uri") "||" (gobj/get option "city")))
                   :formatOptionLabel (fn [props]
                                        (let [is-created? (gobj/get props "__isCreated__")]
                                          (if is-created?
                                            (str "Create new organisation \"" (gobj/get props "organisationName") "\"")
                                            (if (blank? (gobj/get props "city"))
                                              (gobj/get props "organisationName")
                                              (str (gobj/get props "organisationName") " - " (gobj/get props "city"))))))
                   :loadOptions       (fn [input callback]
                                        (ajax/GET (str URL_ROOT "/api/institution.json")
                                                  {:handler
                                                   (fn [{:strs [results] :as data}]
                                                     (callback (clj->js results)))
                                                   :error-handler
                                                   (fn [e]
                                                     (callback "Options loading error."))
                                                   :params
                                                   {:search input
                                                    :offset 0
                                                    :limit  1000}}))
                   :onChange          #(on-change (js->clj %))
                   :getNewOptionData  (fn [input]
                                        #js {:uri              (str "https://w3id.org/tern/resources/" (random-uuid))
                                             :organisationName input
                                             :__isCreated__    true})
                   :noResultsText     "No results found"
                   :onBlurResetsInput false
                   :isClearable       true
                   :isSearchable      true
                   :tabSelectsValue   false
                   :onInputChange     on-input-change
                   :onBlur            on-blur
                   :filterOption      (fn [option value]
                                        (string/includes? (string/lower-case (string/replace (get-in (js->clj option :keywordize-keys true) [:data :organisationName]) #"\s" ""))
                                                          (string/lower-case (string/replace value #"\s" ""))))

                   :isValidNewOption  (fn [input _ options]
                                        (let [input (string/lower-case (string/replace input #"\s" ""))
                                              match (filter (fn [x] (= input (string/lower-case (string/replace (:organisationName x) #"\s" "")))) (js->clj options :keywordize-keys true))]
                                          (empty? match)))
                   :placeholder       "Start typing to filter list..."}))))]
    (r/create-class
      {:component-did-mount component-did-mount
       :render              render})))

(defn PersonPickerWidget
  [_]
  (letfn [(component-did-mount [this]
            (set-input-value this))
          (component-will-receive-props [this new-argv]
            (let [[_ next-props] new-argv
                  props (r/props this)]
              (utils/on-change props next-props [:value] #(set-input-value this))))
          (render [this]
            (let [{:keys [on-input-change on-blur on-change disabled party-path] :as props} (r/props this)
                  {:keys [input-value]} (r/state this)]
              (let [{:keys [URL_ROOT]} @(rf/subscribe [:subs/get-derived-path [:context]])
                    uri-value @(rf/subscribe [:subs/get-derived-path (conj party-path :value :uri)])
                    preflabel-value @(rf/subscribe [:subs/get-derived-path (conj party-path :value :individualName)])
                    js-value #js {:prefLabel (or (:value preflabel-value) "")
                                  :uri       (:value uri-value)}
                    js-value (if (:value preflabel-value)
                               js-value
                               nil)]
                (ReactSelectAsync
                  {:value             js-value
                   :disabled          disabled
                   :defaultOptions    true
                   :getOptionValue    (fn [option]
                                        (gobj/get option "uri"))
                   :formatOptionLabel (fn [props]
                                        (gobj/get props "prefLabel"))
                   :loadOptions       (fn [input callback]
                                        (ajax/GET (str URL_ROOT "/api/person.json")
                                                  {:handler
                                                   (fn [{:strs [results] :as data}]
                                                     (callback (clj->js results)))
                                                   :error-handler
                                                   (fn [e]
                                                     (callback "Options loading error."))
                                                   :params
                                                   {:search input
                                                    :offset 0
                                                    :limit  100}}))
                   :onChange          #(on-change (js->clj %))
                   :noResultsText     "No results found"
                   :onBlurResetsInput false
                   :isClearable       true
                   :tabSelectsValue   false
                   :onInputChange     on-input-change
                   :onBlur            on-blur
                   :placeholder       "Start typing to filter list..."}))))]
    (r/create-class
      {:component-did-mount          component-did-mount
       :component-will-receive-props component-will-receive-props
       :render                       render})))

(defn SelectRoleWidget [role-path this]
  (let [role @(rf/subscribe [:subs/get-derived-path role-path])
        {:keys [options]} @(rf/subscribe [:subs/get-derived-path [:api :rolecode]])]
    [SelectWidget (assoc role
                    :options (for [option options
                                   :let [Identifier (gobj/get option "Identifier")]]
                               [Identifier (cuerdas/human Identifier)])
                    :on-change #(rf/dispatch [:handlers/value-changed role-path %]))]))

(defn PersonInputField
  "Input field for people which offers autocompletion of known
  people."
  [party-path this]
  (let [party-field @(rf/subscribe [:subs/get-derived-path party-path])
        uri (-> party-field :value :uri)]
    [:div.OrganisationInputField
     ; FIXME: replace with autocomplete if we can find one
     [PersonPickerWidget
      {:old-value  uri
       :party-path party-path
       :disabled   (:disabled uri)
       :on-change  (fn [option]
                     (rf/dispatch [:handlers/update-person (conj party-path :value) option]))}]]))

(defn OrganisationInputField
  "Input field for organisation which offers autocompletion of known
  institutions.  On autocomplete address details are updated."
  [party-path this]
  (let [party-field @(rf/subscribe [:subs/get-derived-path party-path])
        organisationName (-> party-field :value :organisationName)]
    [:div.OrganisationInputField
     ; FIXME: replace with autocomplete if we can find one
     [OrganisationPickerWidget
      {:old-value  organisationName
       :party-path party-path
       :disabled   (:disabled organisationName)
       ;manual maxlength implementation
       :on-input-change (fn [newvalue]
                          (subs newvalue 0 250))
       :on-change  (fn [option]
                     (rf/dispatch [:handlers/org-changed (conj party-path :value) option]))}]]))

(def orcid-mask "0000{-}0000{-}0000{-}000*")

(defn ResponsiblePartyField [party-path]
  (let [party-value-path (conj party-path :value)
        party-value @(rf/subscribe [:subs/get-derived-path party-value-path])
        {:keys [individualName givenName familyName phone facsimile orcid
                electronicMailAddress organisationName isUserAdded]} party-value
        electronicMailAddress (assoc electronicMailAddress :required (:value isUserAdded))
        ]
    [:div.ResponsiblePartyField


     [SelectRoleWidget (conj party-value-path :role)]


     [:div.flex-row
      [:div.flex-row-field
       {;need this to make sure the drop down is rendered above any other input fields
        :style {:position "relative"
                :z-index  10}}
       [:label "Contact name" (when (:required individualName) " *")]
       [PersonInputField party-path]
       [:p.help-block "If you cannot find the person in the list above, please enter details below"]]]
     [:div


      [:div.row
       [:div.col-md-6
        [NameInputWidget (assoc givenName
                           :on-change #(rf/dispatch [:handlers/person-detail-changed party-value-path :givenName % isUserAdded]))]]
       [:div.col-md-6
        [NameInputWidget (assoc familyName
                           :on-change #(rf/dispatch [:handlers/person-detail-changed party-value-path :familyName % isUserAdded]))]]]

      [InputWidget (assoc electronicMailAddress
                     :on-change #(rf/dispatch [:handlers/value-changed (conj party-value-path :electronicMailAddress) %]))]

      [InputWidget (assoc orcid
                     :on-change #(rf/dispatch [:handlers/value-changed (conj party-value-path :orcid) %])
                     :mask orcid-mask)]

      [:label "Organisation" (when (:required organisationName) " *")]
      [OrganisationInputField party-path]

      [:label "Postal address"]
      [AddressField (conj party-value-path :address)]

      [:div.ContactDetails

       [InputWidget (assoc phone
                      :on-change #(rf/dispatch [:handlers/value-changed (conj party-value-path :phone) %]))]

       [InputWidget (assoc facsimile
                      :on-change #(rf/dispatch [:handlers/value-changed (conj party-value-path :facsimile) %]))]

       ]]]))

(defn FieldError [{:keys [errors label]} this]
  [:span.FieldError label ": " (first errors)])

(defn ManyFieldError [{:keys [errors label]} this]
  [:span.FieldError label ": " (or (first errors) "check field errors")])

(defn PageErrors [{:keys [page path]} this]
  (let [{:keys [show-errors] :as form} @(rf/subscribe [:subs/get-derived-path path])
        fields (logic/page-fields form page)
        error-fields (remove #(logic/is-valid? {:fields %}) fields)
        msgs (for [field error-fields]
               (if (and (:many field) (not (logic/is-valid? {:fields field})))
                 [ManyFieldError field]
                 [FieldError field]))]
    (when (seq msgs)
      [:div.alert.alert-warning.alert-dismissable
       [:button {:type     "button" :class "close"
                 :on-click #(rf/dispatch [:handlers/hide-errors path])} "×"]
       (if (> (count msgs) 1)
         [:div
          [:b "There are multiple fields on this page that require your attention:"]
          (into [:ul] (for [msg msgs] [:li msg]))]
         (first msgs))])))

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
        {:keys [user urls]} @(rf/subscribe [:subs/get-derived-path [:context]])
        {:keys [title tag_line guide_pdf]} @(rf/subscribe [:subs/get-derived-path [:context :site]])]
    [bp3/navbar {:className "bp3-dark"}
      [bp3/navbar {:className "container"}
      [bp3/navbar-group {:align (:LEFT bp3/alignment)}
        ; [:a.bp3-button.bp3-minimal {:href Dashboard} [bp3/navbar-heading title]]
        ]
      [bp3/navbar-group {:align (:RIGHT bp3/alignment)}
        [:span {:style {:padding "5px 10px 5px 10px"}} (userDisplay user)]
        [:a.bp3-button.bp3-minimal {:href Dashboard} "My Records"]
        [:a.bp3-button.bp3-minimal {:href guide_pdf :target "_blank"} "Help"]
        ;; [help-menu]
        [:a.bp3-button.bp3-minimal {:href "/logout"} "Sign Out"]]]]))

(defmulti PageTabView (fn [page this] [(get page :name)
                                       (get page :tab :data-identification)]))

(defn PageView404
  [_ this]
  (let [{:keys [name]} @(rf/subscribe [:subs/get-page-props])]
    [:h1 "Page not found: " name]))

(defn PageViewError
  [_ this]
  (let [{:keys [text code detail]} @(rf/subscribe [:subs/get-page-props])]
    [:div
     [navbar]
     [:div.container
      [:div.PageViewBody
       [:p.lead "Oops! " (pr-str text)]
       [:p "The server responded with a " [:code code " " (pr-str text)] " error."]]]]))

(defmethod PageTabView ["Edit" :data-identification]
  [page this]
  [:div
   [PageErrors {:page :data-identification :path [:form]}]
   [:h2 "1. Data Identification"]
   [InputField {:path [:form :fields :identificationInfo :title]}]
   [date-field [:form :fields :identificationInfo :dateCreation] (js/Date. "1900-01-01")]
   [TopicCategories nil]
   [SelectField [:form :fields :identificationInfo :status]]
   [SelectField [:form :fields :identificationInfo :maintenanceAndUpdateFrequency]]
   [:div.link-right-container [:a.link-right {:href "#what"} "Next"]]])

(defmethod PageTabView ["Edit" :what]
  [page this]
  [:div
   [PageErrors {:page :what :path [:form]}]
   [:h2 "2. What"]
   [:span.abstract-textarea
    [textarea-field [:form :fields :identificationInfo :abstract]]]
   [:span.abstract-textarea
    [textarea-field [:form :fields :identificationInfo :purpose]]]
   [ThemeKeywords :keywordsTheme]
   [ThemeKeywords :keywordsThemeAnzsrc]
   [ThemeKeywordsExtra nil]
   [TaxonKeywordsExtra nil]
   [:div.link-right-container [:a.link-right {:href "#when"} "Next"]]])

(defmethod PageTabView ["Edit" :when]
  [page this]
  [:div
   [PageErrors {:page :when :path [:form]}]
   [:h2 "3. When was the data acquired?"]
   [date-field [:form :fields :identificationInfo :beginPosition] (js/Date. "1900-01-01")]
   [date-field [:form :fields :identificationInfo :endPosition]]
   [:div.row
    [:div.col-md-4
     [NasaListSelectField {:keyword :samplingFrequency
                           :path    [:form :fields :identificationInfo]}]]]
   [:div.link-right-container [:a.link-right {:href "#where"} "Next"]]])

(defmethod PageTabView ["Edit" :where]
  [page this]
  [:div
   [PageErrors {:page :where :path [:form]}]
   [:h2 "4. Where"]
   [GeographicCoverage nil]
   [VerticalCoverage nil]
   [:div.link-right-container [:a.link-right {:href "#how"} "Next"]]])

(defn CreditField [path this]
  [:div.CreditField [textarea-widget @(rf/subscribe [:textarea-field/get-many-field-props path :credit])]])

(defn delete-contact! [this group item e]
  (.stopPropagation e)
  (let [parties-path (:path (contact-groups group))
        parties @(rf/subscribe [:subs/get-derived-path parties-path])
        {:keys [selected-group selected-item]} (r/state this)]
    (rf/dispatch [:handlers/open-modal
                  {:type       :confirm
                   :title      "Delete?"
                   :message    "Are you sure you want to delete this person?"
                   :on-confirm (fn []
                                 (when (and (= group selected-group) (<= item selected-item))
                                   (r/set-state this {:selected-item
                                                      (when (> (count (:value parties)) 1)
                                                        (-> selected-item dec (max 0)))}))
                                 (rf/dispatch [:handlers/remove-party parties-path item]))}])))

(defn parties-list [this group]
  (let [{:keys [disabled] :as parties} @(rf/subscribe [:subs/get-derived-path (:path (contact-groups group))])
        {:keys [selected-group selected-item]} (r/state this)
        selected-item (when (= group selected-group) selected-item)
        ]
    (into [:div.list-group]
          (for [[item party] (-> parties :value utils/enum)]
            [:div
             [:a.list-group-item
              {:class    (if (= item selected-item) "active")
               :on-click (fn []
                           (r/set-state this {:selected-group group})
                           (r/set-state this {:selected-item item}))}
              [:span
               (let [name (get-in party [:value :individualName :value])
                     givenName (get-in party [:value :givenName :value])
                     familyName (get-in party [:value :familyName :value])
                     name (if (blank? name)
                            (str givenName " " familyName)
                            name)]
                 (if (blank? name) [:em "First name Last name"] name))
               (when-not disabled
                 [:button.btn.btn-warn.btn-xs.pull-right
                  {:on-click (partial delete-contact! this group item)}
                  [:i.glyphicon.glyphicon-minus]])]]]))))

(defn default-selected-group [this]
  (ffirst
    (filter
      #(first @(rf/subscribe [:subs/get-derived-path (conj (:path (second %)) :value)]))
      (utils/enum contact-groups))))

(defn Who [_ this]
  (letfn [(init-state [this]
            {:selected-item  0
             :selected-group 0})
          (render [this]
            (let [{:keys [selected-group selected-item open hold]} (r/state this)]
              (let [selected-group (or selected-group (default-selected-group this))
                    cursors (mapv (fn [{:keys [path]}]
                                    @(rf/subscribe [:subs/get-derived-path path]))
                                  contact-groups)
                    new! (fn [path group & [field]]
                           (let [many-field (cursors group)]
                             (if field
                               (rf/dispatch [:handlers/add-value! path (:value field)])
                               (rf/dispatch [:handlers/new-field! path]))
                             (r/set-state this {:selected-group group})
                             (r/set-state this {:selected-item (-> many-field :value count)})))
                    all-parties (mapv (comp set
                                            :value)
                                      cursors)
                    all-parties-set (apply clojure.set/union all-parties)]
                [:div
                 [PageErrors {:page :who :path [:form]}]
                 [:h2 "6: Who"]
                 [:div.row
                  (into [:div.col-sm-4]
                        (for [[group {:keys [title path]}] (utils/enum contact-groups)]
                          (let [parties (clojure.set/difference
                                          all-parties-set (all-parties group))]
                            [:div
                             [:h4 title (when (get-in cursors [group :required]) " *")]
                             (parties-list this group)
                             (when-not (get-in cursors [group :disabled])
                               [:div.dropdown
                                {:class   (if (= open group) "open")
                                 :on-blur #(let [{:keys [open]} (r/state this)
                                                 open' (when (or hold (not= open group)) open)]
                                             (r/set-state this {:open open'}))}
                                [:button.btn.btn-default.dropdown-toggle
                                 {:on-click #(if (zero? (count parties))
                                               (new! path group)
                                               (let [{:keys [open]} (r/state this)
                                                     open' (when (not= open group) group)]
                                                 (r/set-state this {:open open'})))}
                                 [:span.glyphicon.glyphicon-plus]
                                 " Add person"]
                                (-> [:ul.dropdown-menu
                                     {:on-mouse-enter #(r/set-state this {:hold true})
                                      :on-mouse-leave #(r/set-state this {:hold false})}
                                     [:li.dropdown-header "Copy person"]]
                                    (into (for [x parties]
                                            [:li [:a {:tab-index -1
                                                      :href      "#"
                                                      :on-click  (fn [e]
                                                                   (.preventDefault e)
                                                                   (new! path group x)
                                                                   (r/set-state this {:open false}))}
                                                  (get-in x [:value :individualName :value])]]))
                                    (conj [:li.divider]
                                          [:li [:a {:href     "#"
                                                    :on-click (fn [e]
                                                                (.preventDefault e)
                                                                (new! path group)
                                                                (r/set-state this {:open false}))}
                                                "New person"]]))])])))
                  [:div.col-sm-8
                   (when (and selected-group selected-item)
                     [ResponsiblePartyField
                      (-> contact-groups
                          (get-in [selected-group :path])
                          (conj :value selected-item))])]]

                 [:h2 "Other credits"]
                 [TableModalEdit
                  {:form       CreditField
                   :title      "Credit"
                   :field-path [:form :fields :identificationInfo :credit]}]])))]
    (r/create-class
      {:get-initial-state init-state
       :render            render})))

(defmethod PageTabView ["Edit" :who]
  [page this]
  [:div
   [Who nil]
   [:div.link-right-container [:a.link-right {:href "#about"} "Next"]]])

(defn MethodOrOtherForm
  "docstring"
  [path this]
  (let [method-path (conj path :value)
        {:keys [name description uri] :as dp-term} @(rf/subscribe [:subs/get-derived-path method-path])
        props {:method-path method-path
               :api-path    [:api :parameterunit]}]
    [:div
     ;TODO: put this back once method vocab exists
     ;[:p "Select a method from the list"]
     ;[MethodListField props]
     ;[:p "Or define your own method"]
     [InputWidget (assoc name :on-change (fn [option]
                                           (rf/dispatch [:handlers/update-method-name method-path option])))]
     [textarea-field (into [] (concat path [:value :description]))]]))

(defn Methods [path this]
  (let [list-field @(rf/subscribe [:subs/get-derived-path path])]
    [:div.SupplementalInformation
     (label-template list-field)
     [TableModalEdit
      {:ths         ["Name" "Description"]
       :tds-fn      (fn [field]
                      (let [{:keys [name description]} (:value field)]
                        (mapv (comp #(or % "--") :value) [name description])))
       :form        MethodOrOtherForm
       :title       "Method"
       :placeholder ""
       :add-label   "Add method"
       :field-path  path}]]))

(defmethod PageTabView ["Edit" :how]
  [page this]
  [:div
   [PageErrors {:page :how :path [:form]}]
   [:h2 "5: How"]
   [Methods [:form :fields :resourceLineage :processStep]]
   [textarea-field
    [:form :fields :dataQualityInfo :methods]]
   [textarea-field
    [:form :fields :dataQualityInfo :results]]
   [:div.link-right-container [:a.link-right {:href "#who"} "Next"]]])


(defn UseLimitationsFieldEdit [path]
  [textarea-widget @(rf/subscribe [:textarea-field/get-many-field-props path :useLimitations])])

(defn UseLimitations [path this]
  (let [list-field @(rf/subscribe [:subs/get-derived-path path])]
    [:div.SupplementalInformation
     (label-template list-field)
     [TableModalEdit
      {:form       UseLimitationsFieldEdit
       :title      "Use Limitation"
       :add-label  "Add use limitation"
       :field-path path}]]))

(defn SupplementalInformationRowEdit [path this]
  [textarea-widget @(rf/subscribe [:textarea-field/get-many-field-props path :supplementalInformation])])

(defn SupplementalInformation [path this]
  [:div
   [:label "Any supplemental information such as file naming conventions"]
   [TableModalEdit
    {:form        SupplementalInformationRowEdit
     :title       "Additional Detail"
     :placeholder ""
     :add-label   "Additional detail"
     :field-path  path}]])


(defn ResourceConstraints [props this]
  [:div.ResourceConstraints
   [:p.help-block (str "Creative Commons - Attribution 4.0 International. The license allows others to copy,
   distribute, display, and create derivative works provided that they
   credit the original source and any other nominated parties.")]
   [:p [:a {:href   "https://creativecommons.org/licenses/by/4.0/"
            :target "_blank"}
        "https://creativecommons.org/licenses/by/4.0/"]]])

(defn SupportingResourceFieldEdit [path this]
  [:div
   [InputField {:path (conj path :value :name)}]
   [InputField {:path (conj path :value :url)}]])

(defn SupportingResource [path this]
  [:div
   [:label "Any resources with hyperlinks (including Publications)"]
   [TableModalEdit
    {:ths         ["Title" "URL"]
     :tds-fn      (comp (partial map (comp #(or % "--") :value)) (juxt :name :url) :value)
     :form        SupportingResourceFieldEdit
     :title       "Publication"
     :placeholder ""
     :add-label   "Add publication"
     :field-path  path}]])

(defmethod PageTabView ["Edit" :about]
  [page this]
  [:div
   [PageErrors {:page :about :path [:form]}]
   [:h2 "7: About Dataset"]
   [:h4 "Data parameters"]
   [DataParametersTable [:form :fields :identificationInfo :dataParameters]]
   [:br]
   [:h4 "Pixel Size"]
   [:div.row
    [:div.col-md-6
     [NasaListSelectField {:keyword :horizontalResolution
                           :path    [:form :fields :identificationInfo]}]]]
   [:br]
   [:h4 "Resource constraints"]
   [ResourceConstraints nil]
   [UseLimitations [:form :fields :identificationInfo :useLimitations]]
   [:br]
   [:h4 "Supplemental information"]
   [SupportingResource [:form :fields :supportingResources]]
   [SupplementalInformation [:form :fields :identificationInfo :supplementalInformation]]
   [:br]
   [:h4 "Distribution"]
   [InputField {:path [:form :fields :distributionInfo :distributionFormat :name]}]
   [InputField {:path [:form :fields :distributionInfo :distributionFormat :version]}]
   [:span.abstract-textarea
    [textarea-field [:form :fields :resourceLineage :lineage]]]
   [:div.link-right-container [:a.link-right {:href "#upload"} "Next"]]])

(defn DataSourceRowEdit [path this]
  [:div
   [InputField {:path (conj path :value :description)}]
   [SelectField (conj path :value :protocol)]
   [InputField {:path (conj path :value :url)}]
   [InputField {:path (conj path :value :name)}]])

(defn DataSources [props this]
  [:div
   [TableModalEdit {:ths        ["Title" "URL" "Layer"]
                    :tds-fn     (comp (partial map (comp #(or % "--") :value)) (juxt :description :url :name) :value)
                    :form       DataSourceRowEdit
                    :title      "Data services"
                    :field-path [:form :fields :dataSources]}]])

(defmethod PageTabView ["Edit" :upload]
  [page this]
  [:div
   [PageErrors {:page :upload :path [:form]}]
   [:h2 "8: Upload Data"]
   [UploadData nil]
   [:h2 "Data Services"]
   [DataSources nil]
   [:div.link-right-container [:a.link-right {:href "#lodge"} "Next"]]])

(defmethod PageTabView ["Edit" :lodge]
  [page this]
  [:div
   [PageErrors {:page :lodge :path [:form]}]
   [:h2 "9: Lodge Metadata Draft"]
   [Lodge nil]
   [:div.link-right-container [:a.link-right {:href "#"} "Next"]]])

(defn progress-bar []
  (when-let [{:keys [can-submit? value]} @(rf/subscribe [:progress/get-props])]
    [:div
     [:span {:class "progressPercentage"} (str (int (* value 100)) "%")]
     [bp3/progress-bar {:animate false
                        :style   {:height "25px"}
                        :intent  (if can-submit? "success" "warning")
                        :stripes false
                        :value   value}]]))

(defn handle-archive-click
  []
  (rf/dispatch [:handlers/open-modal
                {:type       :confirm
                 :title      "Archive?"
                 :message    "Are you sure you want to archive this record?"
                 :on-confirm #(rf/dispatch [:handlers/archive-current-document])}]))

(defn edit-tabs
  []
  (let [{:keys [disabled]} @(rf/subscribe [:subs/get-derived-path [:form]])
        {:keys [selected-tab tab-props]} @(rf/subscribe [:subs/get-edit-tab-props])]
    (letfn [(pick-tab [id _ _] (rf/dispatch [:handlers/set-tab (edn/read-string id)]))]
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
               [:div {:style {:width 150 :height 20}}
                (conj [:div.hidden-xs.hidden-sm
                       [progress-bar]])])))])))

(defn PageViewEdit
  [_]
  (letfn [(render [this]
            (let [_ (r/state this)]
              (let [page @(rf/subscribe [:subs/get-page-props])
                    saving (::handlers/saving? page)
                    {:keys [user urls]} @(rf/subscribe [:subs/get-derived-path [:context]])
                    {:keys [dirty disabled] :as form} @(rf/subscribe [:subs/get-derived-path [:form]])
                    {:keys [status title last_updated]} @(rf/subscribe [:subs/get-derived-path [:context :document]])]
                [:div
                 [navbar]
                 [:div.container
                  [:div.pagehead
                  [:div.pull-right
                    [:button.btn.btn-default.text-warn {:on-click handle-archive-click
                                                        :disabled disabled}
                     [:span.fa.fa-archive]
                     " Archive"] " "
                    [:button.btn.btn-primary {:disabled (or disabled (not dirty) saving)
                                              :on-click save!}
                     (cond
                       saving [:img {:src (str (:STATIC_URL urls) "metcalf3/img/saving.gif")}]
                       dirty [:span.glyphicon.glyphicon-floppy-disk]
                       :else [:span.glyphicon.glyphicon-floppy-saved])
                     (cond
                        saving " Saving..."
                        dirty " Save"
                        :else " Saved")]]
                   [:p.lead [:b (if (blank? title) "Untitled" title)]
                    "   "
                    [:span.label.label-info {:style {:font-weight "normal"}} status]
                    [:small [:i {:style {:color     "#aaa"
                                         :font-size "1em"}}
                             "Last edited " (-> last_updated js/moment .fromNow)]]]]]
                 [:div.Home.container
                  [edit-tabs]
                  [:div.PageViewBody
                   [PageTabView page]
                    ]]])))]
    (r/create-class
      {:render render})))

(defn FormErrors [{:keys [path] :as props} this]
  (let [{:keys [fields show-errors] :as form} @(rf/subscribe [:subs/get-derived-path path])
        fields-with-errors (filter (comp :errors second) fields)]
    (if (and show-errors (seq fields-with-errors))
      [:div.alert.alert-danger
       [:p [:b "The following fields need your attention"]]
       (into [:ul] (for [[k {:keys [label errors]}] fields-with-errors]
                     [:li
                      (or label (name k)) ": "
                      (string/join ". " errors)]))])))

(defn NewDocumentForm [props this]
  [:div.NewDocumentForm
   [FormErrors {:path [:create_form]}]
   [InputField {:path [:create_form :fields :title]}]
   [SelectField [:create_form :fields :template]]])



(defn modal-dialog-dashboard-create-modal
  [_ _]
  [Modal {:ok-copy      "OK"
          :modal-header [:span [:span.glyphicon.glyphicon-list] " " "Create a new record"]
          :modal-body   [NewDocumentForm nil]
          :on-dismiss   #(rf/dispatch [:handlers/close-modal])
          :on-cancel    #(rf/dispatch [:handlers/close-modal])
          :on-save      #(rf/dispatch [:handlers/dashboard-create-save])}])

(defn NewDocumentButton [props]
  (letfn [(init-state [_]
            {:title ""})
          (render [this]
            (let [{:keys [title ch]} (r/state this)]
              [:button.btn.btn-primary {:on-click #(rf/dispatch [:handlers/open-modal {:type :DashboardCreateModal}])}
               [:span.glyphicon.glyphicon-plus]
               " Create new record"]))]
    (r/create-class
      {:get-initial-state init-state
       :render            render})))

(defn clone-doc [url event]
  (rf/dispatch [:handlers/open-modal
                {:type       :confirm
                 :title      "Clone?"
                 :message    (str "Are you sure you want to clone this record?")
                 :on-confirm #(rf/dispatch [:handlers/clone-document url])}])
  (.preventDefault event))

(defn DocumentTeaser [{:keys [url title last_updated status transitions
                              transition_url clone_url] :as doc} this]
  (let [transitions (set transitions)
        on-archive-click #(rf/dispatch [:handlers/archive-doc-click transition_url])
        on-delete-archived-click #(rf/dispatch [:handlers/delete-archived-doc-click transition_url])
        on-restore-click #(rf/dispatch [:handlers/restore-doc-click transition_url])
        on-clone-click (partial clone-doc clone_url)
        on-edit-click #(aset js/location "href" url)]

    [:div.list-group-item.DocumentTeaser
     [:div.pull-right
      (if (contains? transitions "archive")
        [:span.btn.btn-default.noborder.btn-xs
         {:on-click on-archive-click}
         [:span.glyphicon.glyphicon-trash] " archive"])
      (if (contains? transitions "delete_archived")
        [:span.btn.btn-default.noborder.btn-xs
         {:on-click on-delete-archived-click}
         [:span.glyphicon.glyphicon-remove] " delete"])
      (if (contains? transitions "restore")
        [:span.btn.btn-default.noborder.btn-xs
         {:on-click on-restore-click}
         [:span.glyphicon.glyphicon-open] " restore"])
      [:span.btn.btn-default.noborder.btn-xs
       {:on-click on-clone-click}
       [:span.glyphicon.glyphicon-duplicate] " clone"]
      [:span.btn.btn-default.noborder.btn-xs {:on-click on-edit-click}
       [:span.glyphicon.glyphicon-pencil] " edit"]]
     [:p.lead.list-group-item-heading
      [:span.link {:on-click on-edit-click}
        title]
      " "
      [:span.label.label-info {:style {:font-weight "normal"}} status]]
     [:p.list-group-item-text
      [:i {:style {:color     "#aaa"
                   :font-size "0.9em"}}
       (if-not (empty? last_updated)
         [:span
          "Last edited " (.fromNow (js/moment last_updated))
          " by " (:username (:this doc))]
         "Has not been edited yet")]]]))

(defn PageViewDashboard
  [_ this]
  (let [{:keys [filtered-docs status-filter has-documents? user page status-freq status relevant-status-filter urls]} @(rf/subscribe [:subs/get-dashboard-props])]
    [:div
     [navbar]
     [:div.container
      [:span.pull-right {:style {:margin-top 18}} [NewDocumentButton nil]]
      [:h1 "My Records"]
      [:div.row
       [:div.col-sm-9
        (-> [:div.list-group]
            (into (for [filtered-doc filtered-docs]
                    ^{:key (:url filtered-doc)} [DocumentTeaser filtered-doc]))
            (conj (if has-documents?
                    [:a.list-group-item {:on-click #(rf/dispatch [:handlers/dashboard-create-click])}
                     [:span.glyphicon.glyphicon-star.pull-right]
                     [:p.lead.list-group-item-heading " My first record "]
                     [:p.list-group-item-text "Welcome! Since you’re new here, "
                      [:span {:style {:text-decoration "underline"}} "Click here"] " to get started."]]
                    (if (empty? filtered-docs)
                      (if (= status-filter logic/active-status-filter)
                        [:div
                         [:p "You don't have any active records: "
                          [:a {:on-click #(rf/dispatch [:handlers/show-all-documents])}
                           "show all documents"] "."]
                         [NewDocumentButton nil]]
                        [:div
                         [:p "No documents match your filter: "
                          [:a {:on-click #(rf/dispatch [:handlers/show-all-documents])}
                           "show all documents"] "."]
                         [NewDocumentButton nil]])))))]
       [:div.col-sm-3
        (if-not (empty? status-freq)
          (into [:div]
                (for [[sid sname] status]
                  (let [freq (get status-freq sid)]
                    [:div [:label
                           [:input {:type      "checkbox"
                                    :disabled  (not freq)
                                    :checked   (contains? relevant-status-filter sid)
                                    :on-change #(rf/dispatch [:handlers/toggle-status-filter sid status-filter])}]
                           " " sname
                           (if freq [:span.freq " (" freq ")"])]]))))]]]]))

(defn PageViewTheme
  [_ _]
  [:div.PageViewTheme.container
   [BackButton nil]
   [:h1 "Research theme keywords"]
   [:p.help-block "Select keyword(s) to add to record"]
   [KeywordsThemeTable nil]])

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
  [_ this]
  (let [{:keys [message]} @(rf/subscribe [:subs/get-modal-props])]
    [Modal
     {:modal-header [:span [:span.glyphicon.glyphicon-exclamation-sign]
                     " " "Alert"]
      :dialog-class "modal-sm"
      :modal-body   message
      :on-dismiss   #(rf/dispatch [:handlers/close-modal])
      :on-save      #(rf/dispatch [:handlers/close-modal])}]))

(defn modal-dialog-confirm
  [_ this]
  (let [{:keys [message title]} @(rf/subscribe [:subs/get-modal-props])]
    [Modal
     {:modal-header [:span [:span.glyphicon.glyphicon-question-sign] " " title]
      :dialog-class "modal-sm"
      :modal-body   message
      :on-dismiss   #(rf/dispatch [:handlers/close-and-cancel])
      :on-cancel    #(rf/dispatch [:handlers/close-and-cancel])
      :on-save      #(rf/dispatch [:handlers/close-and-confirm])}]))

(defn ModalStack [_ this]
  (let [modal-props @(rf/subscribe [:subs/get-modal-props])]
    (when modal-props
      (case (:type modal-props)
        :TableModalEditForm [modal-dialog-table-modal-edit-form nil]
        :TableModalAddForm [modal-dialog-table-modal-add-form nil]
        :ThemeKeywords [modal-dialog-theme-keywords (:keyword-type modal-props)]
        :parametername [modal-dialog-parametername nil]
        :parameterunit [modal-dialog-parameterunit nil]
        :parameterinstrument [modal-dialog-parameterinstrument nil]
        :parameterplatform [modal-dialog-parameterplatform nil]
        :person [modal-dialog-person nil]
        :DashboardCreateModal [modal-dialog-dashboard-create-modal nil]
        :alert [modal-dialog-alert nil]
        :confirm [modal-dialog-confirm nil]))))

(defn AppRoot [_ this]
  (let [page-name @(rf/subscribe [:subs/get-page-name])]
    [:div [ModalStack nil]
     (if (and guseragent/IE (not (guseragent/isVersionOrHigher 10)))
       [LegacyIECompatibility nil]
       (case page-name
         "404" [PageView404 nil]
         "Error" [PageViewError nil]
         "Edit" [PageViewEdit nil]
         "Dashboard" [PageViewDashboard nil]
         "Theme" [PageViewTheme nil]
         nil))]))
