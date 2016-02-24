(ns metcalf.core
  (:require-macros [cljs.core.async.macros :refer [go alt! go-loop]])
  (:require [cljs.core.async :as async :refer [put! <! alts! chan pub sub timeout dropping-buffer]]
            [clojure.string :refer [blank?]]
            [clojure.set :as set]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.match :refer-macros [match]]
            [sablono.core :as html :refer-macros [html]]
            [clojure.string :as string]
            [clojure.walk :refer [postwalk]]
            [ajax.core :refer [GET POST DELETE]]
            [goog.net.Cookies :as Cookies]
            [condense.fields :refer [Input Checkbox ExpandingTextarea validate-required-field
                                     help-block-template label-template
                                     del-value! add-value! add-field!]]
            [om-tick.form :refer [is-valid? load-errors reset-form extract-data]]
            [om-tick.field :refer [field-zipper field-edit reset-field field?]]
            [clojure.zip :as zip :refer [zipper]]
            [om-tick.bootstrap :refer [Select Date validation-state]]
            [condense.autocomplete :refer [AutoComplete]]
            [openlayers-om-components.geographic-element :refer [BoxMap]]
            [metcalf.logic :refer [derived-state extract-field-values]]
            [metcalf.content :refer [default-payload contact-groups]]
            [condense.derived :refer [derived-atom!]]
            [condense.utils :refer [fmap memoize-last title-case keys-in
                                    int-assoc-in map-keys vec-remove enum]]
            cljsjs.moment
            cljsjs.fixed-data-table
            [tailrecursion.priority-map :refer [priority-map]]
            condense.watch-state
            condense.performance
            [condense.history :as history]
            goog.dom
            goog.dom.classes
            goog.dom.ViewportSizeMonitor
            goog.events
            goog.events.EventType
            goog.events.FileDropHandler
            goog.events.FileDropHandler.EventType
            goog.labs.userAgent.platform
            goog.net.EventType
            goog.net.IframeIo
            select-om-all.core
            select-om-all.utils
            [metcalf.routing :as router]))


(defonce app-state (derived-atom! (atom {}) (memoize-last derived-state)))
(def pub-chan (chan))
(def notif-chan (pub pub-chan :topic))

(defn ^:export app-state-js []
  (clj->js @app-state))

(defn ref-path
  "Return a ref cursor at a specified path"
  [path]
  (let [rc (om/root-cursor app-state)]
    (assert (get-in rc path) (str "No value found in app-state at: " path))
    (-> rc (get-in path) om/ref-cursor)))

(defn observe-path
  "Observes and returns a reference cursor at path and it's value including any derived state."
  [owner path]
  {:pre [(om/component? owner)]}
  (om/observe owner (ref-path path)))

(defn deep-merge
  "Recursively merges maps. If keys are not maps, the last value wins."
  [& vals]
  (if (every? map? vals)
    (apply merge-with deep-merge vals)
    (last vals)))

(defn theme-option [[uuid & tokens]]
  [uuid (string/join " | " (take-while (complement empty?) tokens))])

(defn init-theme-options [{:keys [table] :as theme}]
  (assoc theme :options (into (priority-map) (map theme-option table))))


(defn path-values
  [data]
  (let [get-value #(get-in data %)
        get-path #(mapcat concat (partition-by number? %) (repeat [:value]))]
    (map (juxt get-path get-value)
         (keys-in data))))


(defn reduce-field-values [fields values]
  (reduce (fn [m [p v]]
            (try (int-assoc-in m p v)
                 (catch :default e
                   (js/console.error (clj->js [m p v]) e)
                   m)))
          fields (path-values values)))

(defn path-fields [data]
  (into (sorted-set)
    (keep (fn [path]
            (let [[parent [i k]] (split-with (complement integer?) path)]
              (when k
                (let [parent (vec parent)]
                  [(conj parent :fields k)
                   (conj parent :value i :value k)]))))
          (keys-in data))))

(defn reduce-many-field-templates
  "For each many field value "
  [fields values]
  (reduce (fn [m [tpl-path value-path]]
            (try
              (int-assoc-in m value-path (get-in fields tpl-path))
              (catch js/Error e m)))
          fields (path-fields values)))

(defn initialise-form
  ([{:keys [data] :as form}]
   (initialise-form form data))
  ([form data]
   (-> (reset-form form)
       (assoc :data data)
       (update :fields reduce-many-field-templates data)
       (update :fields reduce-field-values data))))

(defn initial-state
  "Massage raw payload for use as app-state"
  [payload]
  (-> (deep-merge default-payload payload)
      (update :form initialise-form)
      (update :theme init-theme-options)))

(defn field-update! [owner field v]
  (om/update! field :value v)
  (put! (:pub-chan (om/get-shared owner)) {:topic (om/path field) :value v}))

(defn handle-value-change [owner field event]
  (field-update! owner field (-> event .-target .-value)))

(defn handle-checkbox-change [owner field event]
  (field-update! owner field (-> event .-target .-checked)))

;;;;;;;;;
(defn InputField [props owner]
  (reify
    om/IDisplayName (display-name [_] "InputField")
    om/IRenderState
    (render-state [_ {:keys []}]
      (let [field (observe-path owner (:path props))]
        (om/build Input (-> field
                            (merge (dissoc props :path))
                            (assoc
                              :on-blur #(om/update! field :show-errors true)
                              :on-change #(handle-value-change owner field %))))))))

(defn DecimalField [path owner]
  (reify
    om/IDisplayName (display-name [_] "DecimalField")
    om/IRenderState
    (render-state [_ {:keys []}]
      (let [field (observe-path owner path)]
        (om/build Input (assoc field
                          :class "wauto"
                          :on-blur #(om/update! field :show-errors true)
                          :on-change #(handle-value-change owner field %)))))))

(defn DateField [path owner]
  (reify
    om/IDisplayName (display-name [_] "DateField")
    om/IRender
    (render [_]
      (let [field (observe-path owner path)]
        (om/build Date (assoc field :class "wauto"
                                    :display-format "DD-MM-YYYY"
                                    :on-blur #(om/update! field :show-errors true)
                                    :on-date-change #(om/update! field :value %)))))))

(defn SelectField [path owner]
  (reify
    om/IDisplayName (display-name [_] "SelectField")
    om/IRender
    (render [_]
      (let [{:keys [options default-option disabled] :as field} (observe-path owner path)]
        (om/build Select (assoc field
                           :class "wauto"
                           :disabled (or disabled (empty? options))
                           :default-option (if-not (empty? options) default-option "")
                           :on-blur #(om/update! field :show-errors true)
                           :on-change #(handle-value-change owner field %)))))))

(defn AutoCompleteField [path owner]
  (reify
    om/IDisplayName (display-name [_] "AutoCompleteField")
    om/IRenderState
    (render-state [_ {:keys []}]
      (let [{:keys [options default-option disabled] :as field} (observe-path owner path)]
        (om/build AutoComplete (assoc field
                                 :disabled (or disabled (empty? options))
                                 :default-option (if-not (empty? options) default-option "")
                                 :on-change #(handle-value-change owner field %)))))))

(defn TextareaFieldProps [props owner]
  (reify
    om/IDisplayName (display-name [_] "TextareaField")
    om/IRenderState
    (render-state [_ {:keys []}]
      (let [{:keys [path]} props
            field (observe-path owner path)]
        (om/build ExpandingTextarea (merge field (dissoc props :path)
                                           {:on-change #(handle-value-change owner field %)
                                            :on-blur #(om/update! field :show-errors true)}))))))

(defn TextareaField [path owner]
  (reify
    om/IDisplayName (display-name [_] "TextareaField")
    om/IRenderState
    (render-state [_ {:keys []}]
      (let [field (observe-path owner path)]
        (om/build ExpandingTextarea (assoc field
                                      :on-blur #(om/update! field :show-errors true)
                                      :on-change #(handle-value-change owner field %)))))))


(defn CheckboxField [path owner]
  (reify
    om/IDisplayName (display-name [_] "CheckboxField")
    om/IRenderState
    (render-state [_ {:keys []}]
      (let [field (observe-path owner path)]
        (om/build Checkbox (assoc field :checked (:value field)
                                        :on-blur #(om/update! field :show-errors true)
                                        :on-change #(handle-checkbox-change owner field %)))))))

;;;;;;;;;
(defn goto-page [page-state new-page]
  (assoc new-page :back (om/value page-state)))

(defn goto-page!
  ([page-name]
   (.scrollIntoView (goog.dom.getElementByClass "container"))
   (swap! app-state update-in [:page] goto-page {:name page-name}))
  ([page-name page-attrs]
   (.scrollIntoView (goog.dom.getElementByClass "container"))
   (swap! app-state update-in [:page] goto-page (assoc page-attrs :name page-name))))

(defn BackButton [props owner]
  (reify
    om/IDisplayName (display-name [_] "BackButton")
    om/IRender
    (render [_]
      (let [page (observe-path owner [:page])
            back (:back page)]
        (html (if back [:button.btn.btn-default.BackButton
                        {:on-click #(swap! app-state assoc :page (into {} back))}
                        [:span.glyphicon.glyphicon-chevron-left] " Back"]))))))


(defmulti PageView (fn [page owner] (get page :name)) :default "404")


(def Table (js/React.createFactory js/FixedDataTable.Table))
(def Column (js/React.createFactory js/FixedDataTable.Column))
(def ColumnGroup (js/React.createFactory js/FixedDataTable.ColumnGroup))

(defn getter [k row] (get row k))

(defn update-table-width [owner]
  (let [autowidth (om/get-node owner "autowidth")
        width (.-width (goog.style.getSize autowidth))]
    (om/set-state! owner :width width)))


(defn KeywordsThemeCell [rowData owner]
  (reify
    om/IDisplayName (display-name [_] "KeywordThemeCell")
    om/IRender
    (render [_]
      (let [rowData (take-while (complement empty?) rowData)]
        (html [:div.topic-cell
               [:div.topic-path (string/join " > " (drop-last (rest rowData)))]
               [:div.topic-value (last rowData)]])))))


(defn KeywordsThemeTable [props owner]
  (reify
    om/IDisplayName (display-name [_] "TestThemeTable")
    om/IInitState (init-state [_]
                    {:columnWidths     [26 (- 900 26)]
                     :isColumnResizing false
                     :query            ""
                     ;:selected-filter  false
                     :width            900
                     :scrollToRow      0})
    om/IDidMount
    (did-mount [_]
      (let [vsm (goog.dom.ViewportSizeMonitor.)]
        (goog.events.listen vsm goog.events.EventType.RESIZE #(update-table-width owner))
        (update-table-width owner)))
    om/IRenderState
    (render-state [_ {:keys [query width columnWidths isColumnResizing scrollToRow]}]
      (let [keywords (observe-path owner [:form :fields :identificationInfo :keywordsTheme :keywords])
            uuids (zipmap (map :value (:value keywords)) (range))
            table (observe-path owner [:theme :table])
            search-fn (partial select-om-all.utils/default-local-search false)
            results (if (blank? query)
                      table
                      (vec (search-fn (map (juxt rest identity) table) query)))
            rowHeight 50]
        (html [:div.KeywordsThemeTable
               (om/build Input {:label    "Search"
                                :value    query
                                :onChange #(do
                                            (om/set-state-nr! owner :scrollToRow 0)
                                            (om/set-state! owner :query (.. % -target -value)))})
               #_(om/build Checkbox {:label "Selected keywords only"
                                   :checked selected-filter
                                   :on-change #(om/set-state! owner :selected-filter (not selected-filter))})
               [:div {:ref "autowidth"}
                (Table
                  #js {:width                     width
                       :maxHeight                 400
                       :rowHeight                 rowHeight
                       :rowGetter                 #(get results %)
                       :rowsCount                 (count results)
                       :headerHeight              30
                       :onColumnResizeEndCallback #(do (om/set-state! owner [:columnWidths %2] (max %1 5))
                                                       (om/set-state! owner :isColumnResizing false))
                       :overflowX                 "hidden"
                       :scrollToRow               scrollToRow
                       :onScrollEnd               #(om/set-state! owner :scrollToRow (mod %2 rowHeight))
                       :isColumnResizing          isColumnResizing}
                  (Column
                    #js {:label          ""
                         :dataKey        0
                         :align          "right"
                         :cellDataGetter getter
                         :cellRenderer   #(om/build Checkbox {:checked   (contains? uuids %)
                                                              :on-change (fn [_]
                                                                           (if (contains? uuids %)
                                                                             (del-value! keywords (uuids %))
                                                                             (add-value! keywords %)))})
                         :width          (get columnWidths 0)
                         :isResizable    true})
                  (Column
                    #js {:label          "Topic"
                         :cellDataGetter getter
                         :dataKey        1
                         :cellRenderer   (fn [cellData dataKey rowData]
                                           (om/build KeywordsThemeCell rowData))
                         :flexGrow       1
                         :width          (get columnWidths 1)
                         :isResizable    true}))]
               [:p "There are " (count table) " keywords in our database"]])))))


(defn handle-highlight-new [owner item]
  (om/set-state! owner :highlight (conj (om/get-state owner :highlight) item))
  (go (<! (timeout 5000))
      (om/set-state! owner :highlight (disj (om/get-state owner :highlight) item))))


(defn TableInlineEdit [{:keys [ths tds-fn form field-path
                               placeholder default-field]
                        :or   {tds-fn #(list (:value %))}} owner]
  (reify
    om/IDisplayName (display-name [_] "TableInlineEdit")
    om/IInitState (init-state [_] {:cursor nil
                                   :highlight #{}})
    om/IRenderState
    (render-state [_ {:keys [cursor highlight]}]
      (let [{:keys [disabled] :as many-field} (observe-path owner field-path)
            col-span (if ths (count ths) 1)
            edit! (fn [field-path]
                    (om/set-state! owner :cursor field-path))
            delete! (fn [field]
                      (om/set-state! owner :cursor nil)
                      (del-value! many-field (last (om/path field))))
            new! (fn [default-field]
                   (let [values-ref (:value many-field)
                         values-len (count values-ref)]
                     (if default-field
                       (add-field! many-field default-field)
                       (add-field! many-field))
                     (let [new-cursor (conj (om/path values-ref) values-len)]
                       (om/set-state! owner :cursor new-cursor)
                       (handle-highlight-new owner new-cursor))))]
        (html [:div.TableInlineEdit
               (help-block-template many-field)
               (if (or (not placeholder) (-> many-field :value count pos?))
                 [:table.table {:class (when-not disabled "table-hover")}
                  (if ths [:thead [:tr (for [th ths]
                                         [:th th])
                                   [:th.xcell " "]]])
                  [:tbody
                   (for [field (:value many-field)]
                     (let [field-path (om/path field)
                           highlight-class (if (highlight field-path) "highlight")]
                       (if (= cursor field-path)

                         [:tr.active {:ref "edit"}
                          [:td {:class highlight-class
                                :col-span col-span}
                           (om/build form cursor)
                           [:button.btn.btn-primary {:on-click #(edit! nil)} "Done"] " "
                           [:a.text-danger.pull-right {:on-click #(delete! field)}
                            [:span.glyphicon.glyphicon-remove] " Delete"]]
                          [:td.xcell {:class highlight-class}
                           [:span.clickable-text
                            {:on-click #(edit! nil)}
                            [:span.glyphicon.glyphicon-remove]]]]


                         [:tr.noselect {:ref      field-path
                                        :on-click (when-not disabled
                                                    #(edit! field-path))
                                        :class    (if (= field-path cursor) "info")}
                          (for [td-value (tds-fn field)]
                            [:td td-value])
                          [:td.xcell
                           (when-not disabled
                             [:span.glyphicon.glyphicon-edit.hover-only])]])))]]
                 [:div {:style {:margin-bottom "1em"}} placeholder])
               (when-not disabled
                 [:button.btn.btn-primary
                  {:on-click #(new! default-field)}
                  [:span.glyphicon.glyphicon-plus] " Add new"])])))))



(def ESCAPE-KEY-CODE 27)

(defn Modal [props owner]
  (reify

    om/IDidMount
    (did-mount [_]
      (goog.dom.classes.add js/document.body "modal-open")
      (let [key-down-callback (fn [e] (if (= ESCAPE-KEY-CODE (.-keyCode e))
                                        (if-let [on-dismiss (:on-dismiss (om/get-props owner))]
                                          (on-dismiss e))))]
        (.addEventListener js/window "keydown" key-down-callback)
        (om/set-state! owner :key-down-callback key-down-callback)))

    om/IWillUnmount
    (will-unmount [_]
      (goog.dom.classes.remove js/document.body "modal-open")
      (.removeEventListener js/window "keydown" (om/get-state owner :key-down-callback)))

    om/IRender
    (render [_]
      (let [{:keys [modal-header modal-body dialog-class hide-footer
                    on-save on-cancel on-dismiss ok-copy loading]
             :or   {on-save identity on-cancel identity on-dismiss identity}} props]
        (html [:div.modal-open
               [:div.modal.in {:style {:display "block"}    ;:tabIndex -1
                               }
                [:div.modal-dialog {:class dialog-class}
                 [:div.modal-content
                  [:div.modal-header
                   [:button.close {:disabled loading :on-click #(on-dismiss %)}
                    [:span {:dangerouslySetInnerHTML {:__html "&times;"}}]]
                   [:h4.modal-title modal-header]]
                  [:div.modal-body modal-body]
                  (if-not hide-footer
                    [:div.modal-footer
                     (if loading [:span [:span.fa.fa-spinner.fa-spin] " "])
                     [:button.btn.btn-default {:disabled loading
                                               :on-click #(on-cancel %)} "Cancel"]
                     [:button.btn.btn-primary {:disabled loading
                                               :on-click #(on-save %)} (or ok-copy "OK")]])]]]
               [:div.modal-backdrop.in {:style    (if (goog.labs.userAgent.platform/isIos)
                                                    {:position "sticky" :top 0} ; NOTE: attempt to avoid keyboard bug
                                                    {:position "fixed"} ;GOTCHA: Large modals / scrolling is messy
                                                    )
                                        :disabled loading
                                        :on-click #(on-dismiss %)}]])))))



(defn add-keyword [keywords value]
  (when-not (empty? value)
    (om/update! keywords (vec (conj keywords {:value value})))))

(defn del-keyword [keywords value]
  (om/update! keywords (vec (remove #(= value (:value %)) keywords))))

(defn ThemeKeywords [_ owner]
  (reify
    om/IDisplayName (display-name [_] "ThemeKeywords")
    om/IInitState (init-state [_] {:new-value nil
                                   :show-modal false
                                   :highlight #{}})
    om/IRenderState
    (render-state [_ {:keys [new-value show-modal highlight]}]
      (let [{:keys [keywords]} (observe-path owner [:form :fields :identificationInfo :keywordsTheme])
            {:keys [value placeholder disabled] :as props} keywords
            theme-options (observe-path owner [:theme :options])
            theme-table (observe-path owner [:theme :table])
            set-value! #(om/set-state! owner :new-value %)
            add! (fn [new-value] (when-not (empty? new-value)
                                   (let [uuid (first new-value)]
                                     (when (not-any? (comp #{uuid} :value)
                                                     (:value keywords))
                                       (add-value! keywords uuid)))
                                   (handle-highlight-new owner new-value)
                                   (set-value! nil)))
            lookup (fn [uuid] (first (filterv #(= uuid (first %)) theme-table)))
            show-modal! #(om/set-state! owner :show-modal true)
            hide-modal! #(om/set-state! owner :show-modal false)]
          (html [:div.ThemeKeywords {:class (validation-state props)}
                 (if show-modal (om/build Modal (assoc props
                                            :ok-copy "OK"
                                            :dialog-class "modal-lg"
                                            :modal-header (html [:span [:span.glyphicon.glyphicon-list] " " "Research theme keywords"])
                                            :modal-body (html [:div
                                                               [:p.help-block "Select keyword(s) to add to record"]
                                                               (om/build KeywordsThemeTable nil)])
                                            :on-dismiss #(hide-modal!)
                                            :hide-footer true)))
               (label-template props)
               (help-block-template props)
               [:table.table.keyword-table {:class (if-not disabled "table-hover")}
                [:tbody
                 (for [[i keyword] (enum value)]
                   [:tr {:class (if disabled "active" (if (highlight (:value keyword)) "highlight"))}
                    [:td (om/build KeywordsThemeCell (lookup (:value keyword)))]
                    (if-not disabled
                      [:td [:button.btn.btn-warn.btn-xs.pull-right
                            {:on-click #(del-value! keywords i)}
                            [:span.glyphicon.glyphicon-minus]]])])]]
                 (if-not disabled
                   [:div.row
                    [:div.col-sm-8
                     (om/build select-om-all.core/AutoComplete
                               {:placeholder placeholder

                                :value       ""
                                :datasource  theme-table
                                :get-cols    (fn [x]
                                               [(om/build KeywordsThemeCell x)])
                                :rowHeight   50
                                :index-fn    rest
                                :display-fn  (fn [] "")
                                :on-change   #(do (add! %)
                                                  [:select-om-all.core/set ""])})]
                    [:div.col-sm-4
                     [:div {:style {:whitespace :no-wrap}}
                      [:button.btn.btn-default
                       {:on-click #(show-modal!)}
                       [:span.glyphicon.glyphicon-list] " Browse"]]]])])))))


(defn ThemeKeywordsExtra [_ owner]
  (reify
    om/IDisplayName (display-name [_] "ThemeKeywordsExtra")
    om/IInitState (init-state [_] {:new-value ""
                                   :highlight #{}})
    om/IRenderState
    (render-state [_ {:keys [new-value highlight]}]
      (let [{:keys [value placeholder disabled] :as props} (observe-path owner [:form :fields :identificationInfo :keywordsThemeExtra :keywords])
            set-value! (fn [v]
                         (om/set-state! owner :new-value v))
            add-value! (fn []
                         (when-not (empty? new-value)
                           (add-keyword value new-value)
                           (handle-highlight-new owner new-value)
                           (set-value! "")))
            del-value! #(del-keyword value %)]
        (html [:div.ThemeKeywordsExtra {:class (validation-state props)}
               (label-template props)
               (help-block-template props)
               [:table.table.keyword-table {:class (if-not disabled "table-hover")}
                [:tbody
                 (for [keyword value]
                   (do
                     [:tr {:class (if disabled "active" (if (highlight (:value keyword)) "highlight"))}
                      [:td (:value keyword)]
                      (if-not disabled
                        [:td
                         [:button.btn.btn-warn.btn-xs.pull-right
                          {:on-click #(del-value! (:value keyword))}
                          [:span.glyphicon.glyphicon-minus]]])]))]]
               (if-not disabled
                 [:div
                  (om/build Input {:placeholder placeholder
                                   :value       new-value
                                   :on-change   #(set-value! (.. % -target -value))
                                   :on-key-down #(match [(.-key %)]
                                                        ["Enter"] (add-value!)
                                                        :else nil)
                                   :addon-after (html [:span.input-group-btn
                                                       [:button.btn.btn-primary
                                                        {:on-click add-value!}
                                                        [:span.glyphicon.glyphicon-plus]]])})])])))))

(defn TaxonKeywordsExtra [_ owner]
  (reify
    om/IDisplayName (display-name [_] "TaxonKeywordsExtra")
    om/IInitState (init-state [_] {:new-value ""
                                   :highlight #{}})
    om/IRenderState
    (render-state [_ {:keys [new-value highlight]}]
      (let [{:keys [value required placeholder disabled] :as props} (observe-path owner [:form :fields :identificationInfo :keywordsTaxonExtra :keywords])
            set-value! #(om/set-state! owner :new-value %)
            add-value! #(when-not (empty? new-value)
                         (add-keyword value new-value)
                         (handle-highlight-new owner new-value)
                         (set-value! nil))
            del-value! #(del-keyword value %)]
        (html [:div.TaxonKeywordsExtra {:class (validation-state props)}
               [:label "Taxon keywords" (if required " *")]
               (help-block-template props)
               [:table.table.keyword-table {:class (if-not disabled "table-hover")}
                [:tbody
                 (for [keyword value]
                   (do
                     [:tr {:class (if disabled "active" (if (highlight (:value keyword)) "highlight"))}
                      [:td (:value keyword)]
                      (if-not disabled
                        [:td [:button.btn.btn-warn.btn-xs.pull-right
                              {:on-click #(del-value! (:value keyword))}
                              [:span.glyphicon.glyphicon-minus]]])]))]]
               (if-not disabled
                 [:div
                  (om/build Input {:placeholder placeholder
                                   :value       new-value
                                   :on-change   #(set-value! (.. % -target -value))
                                   :on-key-down #(match [(.-key %)]
                                                        ["Enter"] (add-value!)
                                                        :else nil)
                                   :addon-after (html [:span.input-group-btn
                                                       [:button.btn.btn-primary
                                                        {:on-click add-value!}
                                                        [:span.glyphicon.glyphicon-plus]]])})])])))))


(defn geographicElement->extent
  "Transform our API specific bbox data into something generic for Openlayers"
  [{:keys [northBoundLatitude westBoundLongitude eastBoundLongitude southBoundLatitude]}]
  (map :value [westBoundLongitude southBoundLatitude eastBoundLongitude northBoundLatitude]))

(defn extent->geographicElement
  [[westBoundLongitude southBoundLatitude eastBoundLongitude northBoundLatitude]]
  (let [northBoundLatitude (+ northBoundLatitude (if (= northBoundLatitude southBoundLatitude) 1e-6 0))
        eastBoundLongitude (+ eastBoundLongitude (if (= westBoundLongitude eastBoundLongitude) 1e-6 0))]
    {:westBoundLongitude {:value westBoundLongitude}
     :southBoundLatitude {:value southBoundLatitude}
     :eastBoundLongitude {:value eastBoundLongitude}
     :northBoundLatitude {:value northBoundLatitude}}))

(defn add-extent! [geographicElements extent]
  (om/transact! geographicElements #(conj % {:value (extent->geographicElement extent)})))

(defn update-extent! [geographicElements i [_ extent]]
  (om/update! geographicElements [i :value] (extent->geographicElement extent)))

(defn del-element! [geographicElements element]
  (om/transact! geographicElements #(vec (remove (partial = {:value element}) %))))


(defn ->float [s]
  (let [f (js/parseFloat s)]
    (if (js/isNaN f) nil f)))


(defn CoordInputField [{:keys [on-change abbr min max] :as props} owner]
  (reify
    om/IDisplayName (display-name [_] "CoordInputField")
    om/IInitState (init-state [_] {:value nil})
    om/IWillMount
    (will-mount [_]
      (om/set-state! owner :value (om/get-props owner :value)))
    om/IDidUpdate
    (did-update [_ prev-props _]
      (let [p0 (:value prev-props)
            p1 (:value props)]
        (if-not (= p0 p1)
          (om/set-state! owner :value p1))))
    om/IRenderState
    (render-state [_ {:keys [value errors] :as state}]
      (let [change! (fn [e]
                      (om/set-state! owner :errors nil)
                      (let [v (-> (.. e -target -value)
                                  (or "")
                                  (string/replace #"[^\d\.-]" "")
                                  (->float))]
                        (if (and (not (js/isNaN v))
                                 (or (not min) (<= min v))
                                 (or (not max) (<= v max)))
                          (on-change v)
                          (om/set-state! owner :errors [true]))))]
        (om/build Input {:value       (or value "")
                         :show-errors true
                         :errors      errors
                         :on-blur     #(change! %)
                         :on-change   #(om/set-state! owner :value (.. % -target -value))
                         :addon-after (html [:span.input-group-addon [:span.coord-after "°" [:span.coord-abbr abbr]]])})))))


(defn CoordField [path owner]
  (reify
    om/IInitState
    (init-state [_] {})
    om/IRenderState
    (render-state [_ {:keys []}]
      (let [props (observe-path owner path)
            {:keys [northBoundLatitude westBoundLongitude eastBoundLongitude southBoundLatitude]} (:value props)]
        (let [n-field (om/build CoordInputField {:abbr        "N"
                                                 :max         90 :min -90
                                                 :placeholder "Northbound" :value (:value northBoundLatitude)
                                                 :on-change   #(om/update! northBoundLatitude :value %)})
              e-field (om/build CoordInputField {:abbr "E"
                                                 :max 180 :min -180
                                                 :placeholder "Eastbound" :value (:value eastBoundLongitude)
                                                 :on-change   #(om/update! eastBoundLongitude :value %)})
              s-field (om/build CoordInputField {:abbr "S"
                                                 :max 90 :min -90
                                                 :placeholder "Southbound" :value (:value southBoundLatitude)
                                                 :on-change   #(om/update! southBoundLatitude :value %)})
              w-field (om/build CoordInputField {:abbr "W"
                                                 :max 180 :min -180
                                                 :placeholder "Westbound" :value (:value westBoundLongitude)
                                                 :on-change   #(om/update! westBoundLongitude :value %)})]
          (html [:div.CoordField
                 [:div.row [:div.col-sm-6.col-sm-offset-3.col-lg-4.col-lg-offset-2
                            [:div.n-block n-field]]]
                 [:div.row
                  [:div.col-sm-6.col-lg-4 [:div.w-block w-field]]
                  [:div.col-sm-6.col-lg-4 [:div.e-block e-field]]]
                 [:div.row
                  [:div.col-sm-6.col-sm-offset-3.col-lg-4.col-lg-offset-2
                   [:div.s-block s-field]]]]))))))


(defprotocol IPrintNice
  (print-nice [x]))

(extend-protocol IPrintNice
  number
  (print-nice [x] (.toFixed x 3))
  object
  (print-nice [x] (pr-str x))
  nil
  (print-nice [x] "--"))


(defn GeographicCoverage [_ owner]
  (reify
    om/IDisplayName (display-name [_] "GeographicCoverage")
    om/IRenderState
    (render-state [_ {:keys [boundaries]}]
      (let [{:keys [disabled] :as geographicElement} (observe-path owner [:form :fields :identificationInfo :geographicElement])
            geographicElements (:value geographicElement)
            extents (map (comp geographicElement->extent :value) geographicElements)]
        (html [:div.GeographicCoverage
               [:h4 "Geographic Coverage"]
               [:div.row
                [:div.col-sm-6
                 (om/build BoxMap
                           {:value                (mapv (fn [extent] [:box extent]) extents)
                            :disabled             disabled
                            :center               [147 -42]
                            :zoom                 6
                            :on-boxend            (partial add-extent! geographicElements)
                            :on-view-change       (fn [extent]
                                                    (om/set-state! owner :boundaries
                                                                   (mapv #(-> %2 (- %1) (* 0.25) (+ %1)) extent (->> extent cycle (drop 2)))))
                            :mark-change-debounce 400
                            :on-mark-change       (partial update-extent! geographicElements)})

                 [:em "Hold down shift to draw a box."]]
                [:div.col-sm-6
                 (om/build TableInlineEdit {:ths           ["North" "West" "South" "East"]
                                            :tds-fn        (fn [geographicElement]
                                                             (let [{:keys [northBoundLatitude westBoundLongitude
                                                                           eastBoundLongitude southBoundLatitude]}
                                                                   (:value geographicElement)]
                                                               [(print-nice (:value northBoundLatitude))
                                                                (print-nice (:value westBoundLongitude))
                                                                (print-nice (:value southBoundLatitude))
                                                                (print-nice (:value eastBoundLongitude))]))
                                            :default-field {:value (extent->geographicElement boundaries)}
                                            :form          CoordField
                                            :field-path    [:form :fields :identificationInfo :geographicElement]
                                            :placeholder   [:em {:style {:color "#a94442"}} "Specify the location(s) of this study."]})]]])))))

(defn VerticalCoverage [props owner]
  (reify
    om/IDisplayName (display-name [_] "VerticalCoverage")
    om/IRender
    (render [_]
      (let [{hasVerticalExtent :value} (observe-path owner [:form :fields :identificationInfo :verticalElement :hasVerticalExtent])]
        (html [:div.VerticalCoverage
               [:h4 "Vertical Coverage"]
               (om/build CheckboxField [:form :fields :identificationInfo :verticalElement :hasVerticalExtent])
               (if hasVerticalExtent
                 [:div
                  (om/build SelectField [:form :fields :identificationInfo :verticalElement :verticalCRS])
                  (om/build InputField
                            {:path [:form :fields :identificationInfo :verticalElement :minimumValue]
                             :class "wauto"})
                  (om/build InputField
                            {:path [:form :fields :identificationInfo :verticalElement :maximumValue]
                             :class "wauto"})])])))))

(defn MasterDetail
  "
  Take a many value field and present a master/detail view.
  Selecting a master exposes the detail information.
  "
  [props owner]
  (reify
    om/IDisplayName (display-name [_] "MasterDetail")
    om/IInitState (init-state [_] {:cursor (first (:value props))})
    om/IRenderState
    (render-state [_ {:keys [cursor]}]
      (let [{:keys [master detail value]} props]
        (html [:div.row.MasterDetail
               [:div.col-sm-4
                [:div.list-group
                 (for [item value]
                   [:a.list-group-item
                    {:class    (if (= item cursor) "active")
                     :on-click #(om/set-state! owner :cursor item)}
                    (if-not (empty? (:errors item))
                      [:span.badge (count (:errors item))])
                    (om/build master item)])]]
               [:div.col-sm-8
                (if cursor
                  (om/build detail cursor))]])))))


(defn DataParameterRowEdit [path owner]
  (reify
    om/IDisplayName (display-name [_] "DataParameterDetail")
    om/IRender
    (render [_]
      (let [props (observe-path owner path)
            {:keys [name longName parameterDescription unit]} (:value props)]
        (html [:div.DataParameterMaster
               [:h3 "Edit parameter"]
               (om/build InputField {:path (om/path longName)})
               [:div.row
                [:div.col-sm-6
                 (om/build InputField {:path (om/path name)})]
                [:div.col-sm-6
                 (om/build InputField {:path (om/path unit)})]]
               [:label "Additional parameter info"]
               (om/build TextareaFieldProps
                         {:path (om/path parameterDescription)})])))))


(defn DataParametersTable [path owner]
  (reify
    om/IDisplayName (display-name [_] "DataParameters")
    om/IRender
    (render [_]
      (html [:div.DataParametersTable
             (om/build TableInlineEdit {:ths        ["Name" "Long name" "Unit of measurement" "Description"]
                                        :tds-fn     (fn [field]
                                                      (let [{:keys [parameterDescription unit name longName]}
                                                            (fmap (comp #(or % "--") :value) (:value field))]
                                                        [name longName unit parameterDescription]))
                                        :form       DataParameterRowEdit
                                        :field-path [:form :fields :identificationInfo :dataParameters]})]))))


(defn upload! [owner]
  (let [form (om/get-node owner "upload-form")
        io (goog.net.IframeIo.)]
    (goog.events.listen
      io goog.net.EventType.COMPLETE #(js/console.log "COMPLETE"))
    (goog.events.listen
      io goog.net.EventType.SUCCESS (fn [_]
                                      (put!
                                        (om/get-state owner :reset-file-drop)
                                        true)
                                      (->> io
                                           .getResponseJson
                                           js->clj
                                           (map-keys keyword)
                                           (swap! app-state update-in
                                                  [:attachments] conj))))
    (goog.events.listen
      io goog.net.EventType.ERROR #(js/console.log "ERROR"))
    (goog.events.listen
      io goog.net.EventType.TIMEOUT #(js/console.log "TIMEOUT"))
    (.sendFromForm io form)))

(defn FileDrop [{:keys [on-change reset-ch max-filesize] :as props} owner]
  (reify
    om/IDisplayName (display-name [_] "FileDrop")
    om/IDidMount
    (did-mount [_]
      (goog.events.listen
        (goog.events.FileDropHandler. js/document)
        goog.events.FileDropHandler.EventType.DROP
        #(set!
          (.-files (om/get-state owner :holder))
          (.. % getBrowserEvent -dataTransfer -files)))
      (go-loop []
        (when (<! reset-ch)
          (let [holder (goog.dom.createDom "input" #js {:type "file"})]
            (goog.events.listen
              holder goog.events.EventType.CHANGE
              (fn [e]
                (let [file (.. e -target -files (item 0))]
                  (if (or (not max-filesize)
                          (<= (.-size file) (* 1024 1024 max-filesize)))
                    (om/set-state! owner :file (.-name file))
                    (when max-filesize
                      (js/alert (str "Please, choose file less than "
                                     max-filesize "mb"))
                      (put! reset-ch true))))))
            (om/set-state! owner {:holder holder :file nil}))
          (recur)))
      (put! reset-ch true))
    om/IDidUpdate
    (did-update [_ _ prev-state]
      (set! (.-files (om/get-node owner "input"))
            (.-files (om/get-state owner :holder)))
      (let [file (om/get-state owner :file)]
        (when (and on-change (not= file (:file prev-state)))
          (on-change file))))
    om/IRenderState
    (render-state [_ {:keys [file holder]}]
      (html [:div
             [:div.text-center.dropzone {:on-click #(.click holder)}
              [:h3
               (or file (:placeholder props)
                   "Drop file here or click to upload")]
              [:span.help-block "Maximum file size 20 MB"]]
             [:br]
             [:input.hidden
              {:ref "input" :type "file" :name (:name props)}]]))))


(defn delete-attachment!
  "Quick and dirty delete function"
  [attachments-ref {:keys [delete_url] :as attachment}]
  (if (js/confirm "Are you sure you want to delete this file?")
    (let []
      (DELETE delete_url {:handler         (fn [{:keys [message document] :as data}]
                                             (let [pred #(= % attachment)]
                                               (om/transact! attachments-ref #(vec (remove pred %)))))
                        :error-handler   (fn [{:keys [status failure response status-text] :as data}]
                                           (js/alert "Unable to delete file"))
                        :headers         {"X-CSRFToken" (.get (goog.net.Cookies. js/document) "csrftoken")}
                        :format          :json
                        :response-format :json
                        :keywords?       true}))))


(defn UploadData [_ owner]
  (reify
    om/IDisplayName (display-name [_] "UploadData")
    om/IInitState
    (init-state [_]
      {:reset-file-drop (chan)})
    om/IRenderState
    (render-state [_ {:keys [file filename reset-file-drop]}]
      (let [attachments (observe-path owner [:attachments])
            upload-form (observe-path owner [:upload_form])
            uff (:fields upload-form)]
        (html [:div.UploadData
               (if-not (empty? attachments)
                 [:div
                  [:table.table.table-hover
                   [:thead
                    [:tr [:th "Name"]]]
                   [:tbody
                    (for [a attachments]
                      [:tr
                       [:td
                        [:a {:href (:file a) :target "blank"} (:name a)]

                        [:button.btn.btn-warn.btn-xs.pull-right
                         {:on-click #(delete-attachment! attachments a)}
                         [:span.glyphicon.glyphicon-minus]]]])]]]
                 [:p "There are no data files attached to this record"])
               [:form#upload-form
                {:ref      "upload-form"
                 :method   "POST"
                 :action   (:url upload-form)
                 :enc-type "multipart/form-data"}
                [:input {:type  "hidden"
                         :name  "csrfmiddlewaretoken"
                         :value (get-in uff [:csrfmiddlewaretoken :initial])
                          #_(.get (goog.net.Cookies. js/document) "csrftoken")}]
                [:input {:type "hidden"
                         :name "document"
                         :value (get-in uff [:document :initial])}]
                [:div.form-group
                 [:input.form-control
                  {:type      "hidden"
                   :name      "name"
                   :value     filename
                   :on-change #(om/set-state! owner :filename
                                              (.. % -target -value))}]]
                (om/build FileDrop
                          {:name        "file"
                           :max-filesize 20
                           :reset-ch    reset-file-drop
                           :on-change   (fn [name]
                                          (om/set-state-nr! owner :file name)
                                          (when (or (= filename file)
                                                    (blank? filename))
                                            (om/set-state!
                                              owner :filename name)))})]
               [:button.btn.btn-primary
                {:on-click #(upload! owner)
                 :disabled (when-not (not-any? blank? [file filename]) "disabled")}
                "Upload"]])))))

(defn save!
  "Quick and dirty save function"
  [owner & [callback]]
  (om/set-state! owner :saving true)
  (let [state @app-state
        done (chan)
        wait (async/map vector [(timeout 500) done])
        data (-> state :form :fields extract-field-values)]
    (go (<! wait) (om/set-state! owner :saving false))
    (POST (get-in state [:form :url])
          {:params          (clj->js data)
           :format          :json
           :response-format :json
           :keywords?       true
           :handler         (fn [resp]
                              (swap! app-state
                                     #(-> %
                                          (assoc-in [:form :data] data)
                                          (update-in
                                            [:context :document] merge
                                            (get-in resp [:form :document]))))
                              (put! done true)
                              (when callback (callback)))
           :error-handler   (fn [{:keys [status failure response status-text]}]
                              (put! done true))

           :headers         {"X-CSRFToken" (.get (goog.net.Cookies. js/document) "csrftoken")}})))

(defn submit!
  "Submit a doc"
  [owner event {:keys [transition_url] :as doc}]
  (.preventDefault event)
  (save! owner
         (fn []
           (om/set-state! owner :saving true)
           (POST transition_url
                 {:params          #js {:transition "submit"}
                  :handler         (fn [{:keys [document] :as data}]
                                     (swap! app-state assoc-in [:context :document] document)
                                     (om/set-state! owner :saving false))
                  :error-handler   (fn [{:keys [status failure response status-text] :as data}]
                                     (om/set-state! owner :saving false)
                                     (js/alert (str "Unable to submit: " status " " failure)))
                  :headers         {"X-CSRFToken" (.get (goog.net.Cookies. js/document) "csrftoken")}
                  :format          :json
                  :response-format :json
                  :keywords?       true}))))


(defn Lodge [_ owner]
  (reify
    om/IDisplayName (display-name [_] "Lodge")
    om/IRenderState
    (render-state [_ {:keys [saving]}]
      (let [{:keys [document urls site]} (observe-path owner [:context])
            {:keys [portal_title portal_url]} site
            {:keys [errors]} (observe-path owner [:progress])
            {:keys [disabled]} (observe-path owner [:form])
            is-are (if (> errors 1) "are" "is")
            plural (if (> errors 1) "s")
            has-errors? (and errors (> errors 0))
            submitted? (= (:status document) "Submitted")]
        (html [:div.Lodge
               [:p "Are you finished? Use this page to lodge your completed metadata record."]
               [:p "The Data Manager will be notified of your submission and will be in contact
               if any further information is required. Once approved, your data will be archived
               for discovery in the "
                (if portal_url
                  [:a {:href portal_url :target "_blank"} [:span.portal_title portal_title]]
                  [:span.portal_title portal_title])
                "."]
               [:p "How complete is your data?"]

               [:p

                [:button.btn.btn-primary.btn-lg
                 {:disabled (or has-errors? saving disabled submitted?)
                  :on-click #(submit! owner % document)}
                 (when saving
                   (list
                     [:img
                      {:src (str (:STATIC_URL urls)
                                 "metcalf/resources/public/img/saving.gif")}]
                     " "))
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
                      :else (:status document))]])]])))))

; TODO: Move hardcoded content to metcalf.content namespace
(defn AddressField [address owner]
  (reify
    om/IDisplayName (display-name [_] "AddressField")
    om/IRender
    (render [_]
      (let [{:keys [city postalCode administrativeArea country deliveryPoint deliveryPoint2]} address]
        (html [:div.AddressField
               (om/build Input (assoc deliveryPoint
                                 :on-blur #(om/update! deliveryPoint :show-errors true)
                                 :on-change #(handle-value-change owner deliveryPoint %)))
               (om/build Input (assoc deliveryPoint2
                                 :on-blur #(om/update! deliveryPoint2 :show-errors true)
                                 :on-change #(handle-value-change owner deliveryPoint2 %)))
               [:div.row
                [:div.col-xs-6
                 (om/build Input (assoc city
                                   :help "City"
                                   :on-change #(handle-value-change owner city %)))]
                [:div.col-xs-6
                 (om/build Input (assoc administrativeArea
                                   :help "State/territory"
                                   :on-change #(handle-value-change owner administrativeArea %)))]]
               [:div.row
                [:div.col-xs-6
                 (om/build Input (assoc postalCode
                                   :help "Postal / Zip code"
                                   :on-change #(handle-value-change owner postalCode %)))]
                [:div.col-xs-6
                 (om/build Input (assoc country
                                   :help "Country"
                                   :on-change #(handle-value-change owner country %)))]]])))))

(defn ror
  "Reverse OR:
   use it to update source value only if destination value is not falsey."
  [a b]
  (or b a))

(defn update-address! [contact {:keys [city organisationName deliveryPoint deliveryPoint2
                                       postalCode country administrativeArea]}]
  (om/transact! contact
                #(-> %
                     (assoc-in [:value :organisationName :value] organisationName)
                     (update-in [:value :address :deliveryPoint :value] ror deliveryPoint)
                     (update-in [:value :address :deliveryPoint2 :value] ror deliveryPoint2)
                     (update-in [:value :address :city :value] ror city)
                     (update-in [:value :address :administrativeArea :value] ror administrativeArea)
                     (update-in [:value :address :postalCode :value] ror postalCode)
                     (update-in [:value :address :country :value] ror country))))

(defn re-escape
  [s]
  (string/replace s #"[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]" #(str "\\" %)))

(defn OrganisationInputField0 [props owner]
  (reify
    om/IDisplayName (display-name [_] "OrganisationInputField")
    om/IInitState
    (init-state [_] {:open?    false
                     :event-ch (chan)})

    om/IWillMount
    (will-mount [_]

      (let [contact (om/get-props owner)
            {:keys [organisationName]} (:value contact)
            {:keys [event-ch]} (om/get-state owner)
            open! #(om/set-state! owner :open? true)
            close! #(om/set-state! owner :open? false)
            change! #(do (field-update! owner organisationName %))
            select! #(update-address! contact %)]

        (go (loop [state :idle]
              (om/set-state! owner :state state)
              (let [[event data] (<! event-ch)]
                (match [state event data]
                  [:idle :focus _] (do (open!)
                                       (recur :active))
                  [:active :blur _] (do (close!)
                                        (recur :idle))
                  [:active :change value] (do (change! value)
                                              (open!)
                                              (recur :active))
                  [_ :select institution] (do (select! institution)
                                              (close!)
                                              (recur :active))
                  :else (recur state)))))))

    om/IRenderState
    (render-state [_ {:keys [open? event-ch]}]
      (let [{:keys [organisationName]} (:value props)
            search-fn #(re-seq (re-pattern (str "(?i)" (re-escape (:value organisationName)))) %)
            institutions (observe-path owner [:institutions])
            event! (fn [& args] (do (put! event-ch args) nil))]
        (html [:div.OrganisationInputField
               (om/build Input (assoc organisationName
                                 :class "InputWithDropdown"
                                 :on-focus #(event! :focus)
                                 :on-blur #(event! :blur)
                                 :on-change #(event! :change (.. % -target -value))))
               [:div {:class (if open? "open")
                      :style {:position "absolute"}}
                [:ul.dropdown-menu {:style {:max-height "10em"}}
                 (for [institution (->> institutions
                                        (filter #(search-fn (:organisationName %)))
                                        (take 10))]
                   [:li
                    [:a.menuitem
                     {:on-mouse-down #(do (event! :select institution)
                                          (.preventDefault %))}
                     (:organisationName institution)]])]]])))))

(defn OrganisationInputField
  "Input field for organisation which offers autocompletion of known
  institutions.  On autocomplete address details are updated."
  [party-path owner]
  (reify
    om/IDisplayName (display-name [_] "OrganisationInputField")
    om/IRender
    (render [_]
      (let [party-field (observe-path owner party-path)
            organisation-name (-> party-field :value :organisationName :value)
            disabled (get-in party-field [:value :organisationName :disabled])
            institutions (observe-path owner [:institutions])
            value (or (first (filter #(= organisation-name (:organisationName %)) institutions))
                      {:organisationName organisation-name})]
        (html [:div.OrganisationInputField
               (om/build select-om-all.core/AutoComplete
                         {:placeholder  "Organisation"
                          :default      value
                          :value        value
                          :editable?    true
                          :disabled?    disabled
                          :datasource   institutions
                          :get-cols     (comp vector :organisationName)
                          :display-fn   :organisationName
                          :index-fn     :organisationName
                          :undisplay-fn (fn [s] {:organisationName s})
                          :on-change    (fn [institution]
                                          (update-address! party-field institution))})])))))

(defn ResponsiblePartyField [path owner]
  (reify
    om/IDisplayName (display-name [_] "ResponsiblePartyField")
    om/IRender
    (render [_]
      (let [party (observe-path owner path)
            {:keys [individualName phone facsimile orcid
                    electronicMailAddress address role organisationName]} (:value party)]
        (html [:div.ResponsiblePartyField

               [:h4 (:value individualName)]
               (om/build Input (assoc individualName
                                 :on-change #(handle-value-change owner individualName %)))

               (om/build Input (assoc orcid
                                 :on-change #(handle-value-change owner orcid %)))

               (om/build Select (assoc role
                                  :on-change #(handle-value-change owner role %)))

               [:label "Organisation" (when (:required organisationName) " *")]
               (om/build OrganisationInputField path)

               [:label "Postal address"]
               (om/build AddressField address)

               [:div.ContactDetails

                (om/build Input (assoc phone
                                  :on-change #(handle-value-change owner phone %)))

                (om/build Input (assoc facsimile
                                  :on-change #(handle-value-change owner facsimile %)))

                (om/build Input (assoc electronicMailAddress
                                  :on-change #(handle-value-change owner electronicMailAddress %)))]])))))


(defn FieldError [{:keys [errors label]} owner]
  (reify
    om/IDisplayName (display-name [_] "FieldError")
    om/IInitState (init-state [_] {})
    om/IRender
    (render [_]
      (html [:span.FieldError label ": " (first errors)]))))



(defn page-fields
  [state page]
  (let [zipper (field-zipper state)]
    (loop [loc zipper
           acc []]
      (if (zip/end? loc)
        acc
        (let [node (zip/node loc)]
          (recur (zip/next loc) (if (and (field? node) (= page (:page node)))
                                  (conj acc node)
                                  acc)))))))


(defn PageErrors [{:keys [page path]} owner]
  (reify
    om/IDisplayName (display-name [_] "PageErrors")
    om/IRender
    (render [_]
      (let [{:keys [show-errors] :as form} (observe-path owner path)
            fields (->> (page-fields form page)
                        (remove #(empty? (:errors %))))]
        (if (and show-errors (not (empty? fields)))
          (html [:div.alert.alert-info.alert-dismissable
                 [:button {:type "button" :class "close"
                           :on-click #(om/update! form :show-errors false)} "×"]
                 (if (> (count fields) 1)
                   [:div
                    [:b "There are multiple fields on this page that require your attention:"]
                    [:ul (for [field fields]
                           [:li (om/build FieldError field)])]]
                   (om/build FieldError (first fields)))]))))))

(defn NavbarHeader [props owner]
  (reify
    om/IDisplayName (display-name [_] "NavbarHeader")
    om/IRender
    (render [_]
      (let [{:keys [Dashboard]} (observe-path owner [:context :urls])
            {:keys [title tag_line]} (observe-path owner [:context :site])]
        (html [:div.navbar-header
               [:a.navbar-brand {:href Dashboard}
                title " " tag_line]])))))

(defn NavbarForm [props owner]
  (reify
    om/IDisplayName (display-name [_] "NavbarForm")
    om/IRender
    (render [_]
      (html [:form.navbar-form.navbar-left {:role "search"}
             [:div.form-group [:input.form-control {:placeholder "Search"}]]
             [:button.btn.btn-default "Submit"]]))))

(defn Navbar [props owner]
  (reify
    om/IDisplayName (display-name [_] "PageNavigation")
    om/IRender
    (render [_]
      (let [{:keys [username]} (observe-path owner [:context :user])
            {:keys [account_profile account_logout]} (ref-path [:context :urls])
            {:keys [guide_pdf]} (observe-path owner [:context :site])]
        (html [:nav.navbar.navbar-inverse
               [:div.container
                (om/build NavbarHeader nil)
                [:ul.nav.navbar-nav.navbar-right
                 [:li [:a {:on-click #(swap! app-state update :form reset-form)
                           :title    "Reset form (for testing)"}
                       [:span.glyphicon.glyphicon-fire]
                       " "]]
                 [:li [:a {:href guide_pdf :target "_blank" :title "Help"}
                       [:span.glyphicon.glyphicon-book]
                       " Help"]]
                 [:li [:a {:href  account_profile
                           :title "Profile"}
                       [:span.glyphicon.glyphicon-user]
                       " " username]]
                 [:li [:a {:href  account_logout
                           :title "Logout"} [:span.glyphicon.glyphicon-log-out]
                       " Sign out"]]]]])))))


(defmulti PageTabView (fn [page owner] [(get page :name)
                                        (get page :tab :data-identification)]))

(defmethod PageView "404"
  [page owner]
  (om/component
    (.log js/console "App State is" (clj->js @app-state))
    (dom/div nil
             (dom/h1 nil "Page not found: " (get page :name))
             (dom/pre nil (.stringify js/JSON (clj->js @app-state) nil "  ")))))


(defmethod PageView "Error"
  [{:keys [text code detail]} owner]
  (om/component
    (html [:div
           (om/build Navbar nil)
           [:div.container
            [:div.PageViewBody
             [:p.lead "Oops! " (pr-str text)]
             [:p "The server responded with a " [:code code " " (pr-str text)] " error."]
             [:pre (pr-str detail)]]]])))


(defmethod PageTabView ["Edit" :data-identification]
  [page owner]
  (om/component
    (html [:div
           (om/build PageErrors {:page :data-identification :path [:form]})
           [:h2 "1. Data Identification"]
           (om/build TextareaField [:form :fields :identificationInfo :title])
           (om/build DateField [:form :fields :identificationInfo :dateCreation])
           (om/build SelectField [:form :fields :identificationInfo :topicCategory])
           (om/build SelectField [:form :fields :identificationInfo :status])
           (om/build SelectField [:form :fields :identificationInfo :maintenanceAndUpdateFrequency])])))


(defmethod PageTabView ["Edit" :what]
  [page owner]
  (om/component
    (html [:div
           (om/build PageErrors {:page :what :path [:form]})
           [:h2 "2. What"]
           [:span.abstract-textarea
            (om/build TextareaField [:form :fields :identificationInfo :abstract])]
           (om/build ThemeKeywords nil)
           (om/build ThemeKeywordsExtra nil)
           (om/build TaxonKeywordsExtra nil)])))

(defmethod PageTabView ["Edit" :when]
  [page owner]
  (om/component
    (html [:div
           (om/build PageErrors {:page :when :path [:form]})
           [:h2 "3. When"]
           (om/build DateField [:form :fields :identificationInfo :beginPosition])
           (om/build DateField [:form :fields :identificationInfo :endPosition])
           (om/build SelectField [:form :fields :identificationInfo :samplingFrequency])])))

(defmethod PageTabView ["Edit" :where]
  [page owner]
  (om/component
    (html [:div
           (om/build PageErrors {:page :where :path [:form]})
           [:h2 "4. Where"]
           (om/build GeographicCoverage nil)
           (om/build VerticalCoverage nil)])))

(defn CreditField [path owner]
  (reify
    om/IDisplayName (display-name [_] "CreditField")
    om/IRender
    (render [_]
      (html [:div.CreditField (om/build TextareaField path)]))))

(defn delete-contact! [owner group item e]
  (.stopPropagation e)
  (let [parties (-> group contact-groups :path ref-path)
        {:keys [selected-group selected-item]} (om/get-state owner)]
    (when (js/confirm "Are you sure you want to delete this person?")
      (when (and (= group selected-group) (<= item selected-item))
        (om/set-state!
          owner :selected-item (when (> (count (:value parties)) 1)
                                 (-> selected-item dec (max 0)))))
      (om/transact! parties #(update % :value vec-remove item)))))

(defn parties-list [owner group]
  (let [{:keys [disabled] :as parties} (-> group contact-groups :path ref-path)
        {:keys [selected-group selected-item]} (om/get-state owner)
        selected-item (when (= group selected-group) selected-item)]
    [:div.list-group
     (for [[item party] (-> parties :value enum)]
       [:a.list-group-item
        {:class    (if (= item selected-item) "active")
         :on-click #(om/set-state! owner {:selected-group group
                                          :selected-item  item})}
        [:span
         (let [name (get-in party [:value :individualName :value])]
           (if (blank? name) [:em "Untitled"] name))
         (when-not disabled
           [:button.btn.btn-warn.btn-xs.pull-right
            {:on-click (partial delete-contact! owner group item)}
            [:i.glyphicon.glyphicon-minus]])]])]))

(defmethod PageTabView ["Edit" :who]
  [page owner]
  (reify
    om/IDisplayName (display-name [_] "")
    om/IInitState
    (init-state [_]
      {:selected-group (ffirst
                         (filter
                           #(-> % second :path (conj :value) ref-path first)
                           (enum contact-groups)))
       :selected-item  0})
    om/IRenderState
    (render-state [_ {:keys [selected-group selected-item open hold]}]
      (let [cursors (mapv (comp (partial observe-path owner) :path) contact-groups)
            new! (fn [group & [field]]
                   (let [many-field (cursors group)]
                     (if field
                       (add-value! many-field (:value field))
                       (add-field! many-field))
                     (om/set-state!
                       owner {:selected-group group
                              :selected-item  (-> many-field :value count)})))
            all-parties (mapv (comp set
                                    (partial remove (comp blank? #(get-in % [:value :individualName :value])))
                                    :value)
                              cursors)
            all-parties-set (apply clojure.set/union all-parties)]
        (html [:div
               (om/build PageErrors {:page :who :path [:form]})
               [:h2 "6: Who"]
               [:div.row
                [:div.col-sm-4
                 (for [[group {:keys [title]}] (enum contact-groups)]
                   (let [parties (clojure.set/difference
                                   all-parties-set (all-parties group))]
                     (list
                       [:h4 title (when (get-in cursors [group :required]) " *")]
                       (parties-list owner group)
                       (when-not (get-in cursors [group :disabled])
                         [:div.dropdown
                          {:class   (if (= open group) "open")
                           :on-blur #(om/update-state!
                                      owner :open
                                      (fn [x] (when (or hold (not= x group)) x)))}
                          [:button.btn.btn-default.dropdown-toggle
                           {:on-click #(if (zero? (count parties))
                                        (new! group)
                                        (om/update-state!
                                          owner :open
                                          (fn [x] (when (not= x group) group))))}
                           [:span.glyphicon.glyphicon-plus]
                           " Add person"]
                          [:ul.dropdown-menu
                           {:on-mouse-enter #(om/set-state! owner :hold true)
                            :on-mouse-leave #(om/set-state! owner :hold false)}
                           [:li.dropdown-header "Copy person"]
                           (for [x parties]
                             [:li [:a {:tab-index -1
                                       :href      "#"
                                       :on-click  (fn [e]
                                                    (.preventDefault e)
                                                    (new! group x)
                                                    (om/set-state! owner :open false))}
                                   (get-in x [:value :individualName :value])]])
                           [:li.divider]
                           [:li [:a {:href     "#"
                                     :on-click (fn [e]
                                                 (.preventDefault e)
                                                 (new! group)
                                                 (om/set-state! owner :open false))}
                                 "New person"]]]]))))]
                [:div.col-sm-8
                 (when (and selected-group selected-item)
                   (om/build ResponsiblePartyField
                             (-> contact-groups
                                 (get-in [selected-group :path])
                                 (conj :value selected-item))))]]

               [:h2 "Other credits"]
               (om/build
                 TableInlineEdit
                 {:form       CreditField
                  :field-path [:form :fields :identificationInfo :credit]})])))))

(defmethod PageTabView ["Edit" :how]
  [page owner]
  (om/component
    (html [:div
           (om/build PageErrors {:page :how :path [:form]})
           [:h2 "5: How"]
           (om/build TextareaField
                     [:form :fields :dataQualityInfo :statement])])))

(defn ResourceConstraints [props owner]
  (reify
    om/IDisplayName (display-name [_] "ResourceConstraints")
    om/IRender
    (render [_]
      (html [:div.ResourceConstraints
             (om/build SelectField [:form :fields :identificationInfo :creativeCommons])
             [:p.help-block "Learn more about which license is right for you at "
              [:a {:href "http://creativecommons.org.au/learn/licenses/"
                   :target "_blank"}
               "Creative Commons Australia"]]
             ;(om/build Checkbox {:label   "Other constraints" :checked true})
             (om/build TextareaField [:form :fields :identificationInfo :useLimitation])]))))


(defn SupplementalFieldEdit [path owner]
  (reify
    om/IDisplayName (display-name [_] "SupplementalFieldEdit")
    om/IRender
    (render [_]
      (om/build TextareaFieldProps {:path path
                                    :rows 3}))))


(defn SupplementalInformation [path owner]
  (reify
    om/IDisplayName (display-name [_] "SupplementalInformation")
    om/IRender
    (render [_]
      (let [list-field (observe-path owner path)]
        (html [:div.SupplementalInformation
               (label-template list-field)
               (om/build TableInlineEdit {:form       SupplementalFieldEdit
                                          :field-path path})])))))

(defmethod PageTabView ["Edit" :about]
  [page owner]
  (om/component
    (html [:div
           (om/build PageErrors {:page :about :path [:form]})
           [:h2 "7: About Dataset"]
           (om/build DataParametersTable [:form :fields :identificationInfo :dataParameters])
           (om/build ResourceConstraints nil)
           (om/build TextareaField [:form :fields :identificationInfo :otherConstraints])
           (om/build SupplementalInformation [:form :fields :identificationInfo :supplementalInformation])
           (om/build InputField {:path [:form :fields :distributionInfo :distributionFormat :name]})
           (om/build InputField {:path [:form :fields :distributionInfo :distributionFormat :version]})])))

(defmethod PageTabView ["Edit" :upload]
  [page owner]
  (om/component
    (html [:div
           (om/build PageErrors {:page :upload :path [:form]})
           [:h2 "8: Upload Data"]
           (om/build UploadData nil)])))

(defmethod PageTabView ["Edit" :lodge]
  [page owner]
  (om/component
    (html [:div
           (om/build PageErrors {:page :lodge :path [:form]})
           [:h2 "9: Lodge Metadata Draft"]
           (om/build Lodge nil)])))

(defn ProgressBar [props owner]
  (reify
    om/IDisplayName (display-name [_] "ProgressBar")
    om/IRender
    (render [_]
      (let [{:keys [fields errors empty required required-errors]
             :as progress} (observe-path owner [:progress])
            can-submit? (= errors 0)
            pct (-> (- fields empty) (/ fields) (* 100) int (str "%"))]
        (html
          [:div {:style {:height 20}}
           [:div.ProgressBar {:style {:width   120
                                      :display "inline-block"}
                              :title "Required"}
            [:div.progress
             [:div.progress-bar {:class (if can-submit?
                                          "progress-bar-success"
                                          "progress-bar-danger")
                                 :style {:width pct}}
              pct]]]])))))


(defn archive!
  "Quick and dirty delete function"
  [owner]
  (if (js/confirm "Are you sure you want to archive this record?")
    (let [state @app-state
          transition_url (-> state :context :document :transition_url)
          success_url (-> state :context :urls :Dashboard)]
      (POST transition_url {:params #js {:transition "archive"}
                            :handler         (fn [{:keys [message document] :as data}]
                                               (aset js/location "href" success_url))
                            :error-handler   (fn [{:keys [status failure response status-text] :as data}]
                                               (js/alert "Unable to delete"))
                            :headers         {"X-CSRFToken" (.get (goog.net.Cookies. js/document) "csrftoken")}
                            :format          :json
                            :response-format :json
                            :keywords?       true}))))


(defmethod PageView "Edit"
  [page owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [saving]}]
      (let [progress (observe-path owner [:progress])
            {:keys [user urls]} (observe-path owner [:context])
            {:keys [dirty disabled] :as form} (observe-path owner [:form])
            {:keys [status title last_updated]} (observe-path owner [:context :document])]
        (html [:div
               (om/build Navbar nil)
               [:div.pagehead
                [:div.container
                 [:div.pull-right
                  [:button.btn.btn-default.text-warn {:on-click #(archive! owner)
                                                      :disabled disabled}
                   [:span.glyphicon.glyphicon-trash]
                   " Archive"] " "
                  [:button.btn.btn-primary {:disabled (or disabled (not dirty) saving)
                                            :on-click #(save! owner)}
                   (cond
                     saving [:img {:src (str (:STATIC_URL urls) "metcalf/resources/public/img/saving.gif")}]
                     dirty  [:span.glyphicon.glyphicon-floppy-disk]
                     :else  [:span.glyphicon.glyphicon-floppy-saved])
                   " Save"]]
                 [:p.lead [:b (:username user)] " / " (if (blank? title) "Untitled" title)
                  " "
                  [:span.label.label-info {:style {:font-weight "normal"}} status]
                  [:br]
                  [:small [:i {:style {:color     "#aaa"
                                       :font-size "0.7em"}}
                           "Last edited " (-> last_updated js/moment .fromNow)]]]]]
               [:div.Home.container
                [:ul.nav.nav-tabs
                 (for [[id text] [[:data-identification "Data identification"]
                                  [:what "What"]
                                  [:when "When"]
                                  [:where "Where"]
                                  [:how "How"]
                                  [:who "Who"]
                                  [:about "About"]
                                  [:upload "Upload"]
                                  [:lodge "Lodge"]]]
                   (let [error-count (get-in progress [:page-errors id])
                         has-errors? (and error-count (> error-count 0))
                         text [:span text " " (if has-errors?
                                                [:b.text-warning "*"])]]
                     [:li {:class (if (= id (get page :tab :data-identification)) "active")}
                      [:a {:style    {:cursor "pointer"}
                           :on-click #(do #_(save!)
                                       (if has-errors? (om/update! form :show-errors true))
                                       (om/update! page [:tab] id))} text]]))
                 [:div.pull-right.hidden-xs.hidden-sm
                  (when-not disabled (om/build ProgressBar nil))]]
                [:div.PageViewBody
                 (om/build PageTabView page)]]])))))


(defn create-document-ch
  [{:keys [url] :as form}]
  (let [result-ch (chan)]
    (POST url
          {:params          (extract-data form)
           :format          :json
           :response-format :json
           :keywords?       true
           :handler         (fn [data]
                              (put! result-ch {:success true :data data}))
           :error-handler   (fn [data]
                              (put! result-ch {:success false :data data}))
           :headers         {"X-CSRFToken" (.get (goog.net.Cookies. js/document) "csrftoken")}})
    result-ch))


(defn FormErrors [{:keys [path] :as props} owner]
  (reify
    om/IDisplayName (display-name [_] "FormErrors")
    om/IRender
    (render [_]
      (let [{:keys [fields show-errors] :as form} (observe-path owner path)
            fields-with-errors (filter (comp :errors second) fields)]
        (html (if (and show-errors (seq fields-with-errors))
                [:div.alert.alert-danger
                 [:p [:b "The following fields need your attention"]]
                 [:ul (for [[k {:keys [label errors]}] fields-with-errors]
                        [:li
                         (or label (name k)) ": "
                         (string/join ". " errors)])]]))))))


(defn NewDocumentForm [props owner]
  (reify
    om/IDisplayName (display-name [_] "NewDocumentForm")
    om/IRender
    (render [_]
      (html [:div/NewDocumentForm
             (om/build FormErrors {:path [:create_form]})
             (om/build InputField {:path [:create_form :fields :title]})
             (om/build SelectField [:create_form :fields :template])]))))


(defn handle-dashboard-create-save [owner e]
  (let [form-ref (ref-path [:create_form])
        page-ref (ref-path [:page])]
    (if (is-valid? form-ref)
      (go (let [{:keys [success data]} (<! (create-document-ch (om/value form-ref)))]
            (if success
              (do
                (om/transact! form-ref reset-form)
                (om/update! form-ref :show-errors false)
                (om/update! page-ref :show-create-modal false)
                (aset js/location "href" (-> data :document :url)))
              (if (= (:status data) 400)
                (om/update! page-ref {:name   "Error"
                                      :text   (-> data :response :message)
                                      :code   (-> data :status)
                                      :detail (-> data :response)})
                (do
                  (om/update! form-ref load-errors (:response data))
                  (om/update! form-ref :show-errors true))))))
      (om/update! form-ref :show-errors true))
    nil))


(defn DashboardCreateModal [props owner]
  (reify
    om/IDisplayName (display-name [_] "DashboardCreateModal")
    om/IRender
    (render [_]
      (let [page (observe-path owner [:page])
            hide-modal! #(om/update! page :show-create-modal false)]
        (html [:div.DashboardCreateModal
               (om/build Modal {:ok-copy      "OK"
                                :modal-header (html [:span [:span.glyphicon.glyphicon-list] " " "Create a new record"])
                                :modal-body   (om/build NewDocumentForm nil)
                                :on-dismiss   #(hide-modal!)
                                :on-save      #(handle-dashboard-create-save owner %)
                                :on-cancel    #(hide-modal!)})])))))

(defn NewDocumentButton [props owner]
  (reify
    om/IDisplayName (display-name [_] "NewDocumentForm")
    om/IInitState (init-state [_] {:title ""})
    om/IRenderState
    (render-state [_ {:keys [title ch]}]
      (html [:button.btn.btn-primary {:on-click #(om/update! (ref-path [:page])
                                                             :show-create-modal true)}
             [:span.glyphicon.glyphicon-plus]
             " Create new record"]))))


(defn transite-doc [url transition event]
  (let [trans-name (first (clojure.string/split transition "_"))]
    (if (js/confirm (str "Are you sure you want to " trans-name " this record?"))
      (POST url {:handler         (fn [{{:keys [uuid] :as doc} :document}]
                                    (swap! app-state update-in [:context :documents]
                                           (fn [docs]
                                             (reduce #(if (= uuid (:uuid %2))
                                                       (if (= transition "delete_archived")
                                                         %1
                                                         (conj %1 doc))
                                                       (conj %1 %2))
                                                     [] docs))))
                 :error-handler   (fn [{:keys [status failure response status-text] :as data}]
                                    (js/alert (str "Unable to " trans-name)))
                 :headers         {"X-CSRFToken" (.get (goog.net.Cookies. js/document) "csrftoken")}
                 :params          #js {:transition transition}
                 :format          :json
                 :response-format :json
                 :keywords?       true})))
  (.preventDefault event))

(defn clone-doc [url event]
  (if (js/confirm (str "Are you sure you want to clone this record?"))
    (POST url {:handler         #(aset js/location "href" (get-in % [:document :url]))
               :error-handler   (fn [{:keys [status failure response status-text] :as data}]
                                  (js/alert (str "Unable to clone")))
               :headers         {"X-CSRFToken" (.get (goog.net.Cookies. js/document) "csrftoken")}
               :format          :json
               :response-format :json
               :keywords?       true}))
  (.preventDefault event))



(defn test-transition [transition]
  (let [{:keys [transition_url] :as doc} (get-in @app-state [:context :document])]
    (POST transition_url
          {:params          #js {:transition transition}
           :handler         #(println [:transition-success %])
           :error-handler   #(println [:transition-failure %])
           :headers         {"X-CSRFToken" (.get (goog.net.Cookies. js/document) "csrftoken")}
           :format          :json
           :response-format :json
           :keywords?       true})))



(defn DocumentTeaser [{:keys [url title last_updated status transitions
                              transition_url clone_url] :as doc} owner]
  (reify
    om/IDisplayName (display-name [_] "DocumentTeaser")
    om/IInitState (init-state [_] {})
    om/IRenderState
    (render-state [_ {:keys []}]
      (let [transitions (set transitions)
            transite (partial transite-doc transition_url)]
        (html [:div.list-group-item.DocumentTeaser
               [:div.pull-right
                (if (contains? transitions "archive")
                  [:span.btn.btn-default.noborder.btn-xs
                   {:on-click (partial transite "archive")}
                   [:span.glyphicon.glyphicon-trash] " archive"])
                (if (contains? transitions "delete_archived")
                  [:span.btn.btn-default.noborder.btn-xs
                   {:on-click (partial transite "delete_archived")}
                   [:span.glyphicon.glyphicon-remove] " delete"])
                (if (contains? transitions "restore")
                  [:span.btn.btn-default.noborder.btn-xs
                   {:on-click (partial transite "restore")}
                   [:span.glyphicon.glyphicon-open] " restore"])
                [:span.btn.btn-default.noborder.btn-xs
                 {:on-click (partial clone-doc clone_url)}
                 [:span.glyphicon.glyphicon-duplicate] " clone"]
                [:span.btn.btn-default.noborder.btn-xs {:on-click #(aset js/location "href" url)}
                 [:span.glyphicon.glyphicon-pencil] " edit"]]
               [:p.lead.list-group-item-heading
                [:span.link {:on-click #(aset js/location "href" url)}
                 [:b (:username (:owner doc))] " / " title]
                " "
                [:span.label.label-info {:style {:font-weight "normal"}}  status]]
               [:p.list-group-item-text
                [:i {:style {:color     "#aaa"
                             :font-size "0.9em"}}
                 (if-not (empty? last_updated)
                   [:span
                    "Last edited " (.fromNow (js/moment last_updated))
                    " by " (:username (:owner doc))]
                   "Has not been edited yet")]]])))))


(defn toggle-status-filter
  [page-ref status-filter status]
  (if (contains? status-filter status)
    (om/update! page-ref :status-filter (disj status-filter status))
    (om/update! page-ref :status-filter (conj status-filter status))))


(def active-status-filter #{"Draft" "Submitted"})


(defmethod PageView "Dashboard"
  [{:keys [show-create-modal status-filter]
    :or {status-filter active-status-filter}
    :as page} owner]
  (om/component
    (let [{:keys [documents status urls user]} (observe-path owner [:context])
          status-freq (frequencies (map :status documents))
          all-statuses (set (keys status-freq))
          relevant-status-filter (set/intersection status-filter all-statuses)
          filtered-docs (->> documents
                             (filter (fn [{:keys [status]}]
                                       (contains? relevant-status-filter status)))
                             (sort-by :last_updated)
                             (reverse))]
      (html [:div
             (om/build Navbar nil)
             (if show-create-modal (om/build DashboardCreateModal nil))
             [:div.container
              [:span.pull-right (om/build NewDocumentButton nil)]
              [:h1 "My Records"]
              [:div.row
               [:div.col-sm-9
                [:div.list-group
                 (om/build-all DocumentTeaser filtered-docs)
                 (if (empty? documents)
                   [:a.list-group-item {:on-click #(do (om/update! page :show-create-modal true)
                                                       (.preventDefault %))
                                        :href (:Create urls)}
                    [:span.glyphicon.glyphicon-star.pull-right]
                    [:p.lead.list-group-item-heading [:b (:username user)] " / My first record "
                     ]
                    [:p.list-group-item-text "Welcome!  Since you're new here, we've created your first record. "
                     [:span {:style {:text-decoration "underline"}} "Click here"] " to get started."]]
                   (if (empty? filtered-docs)
                     (if (= status-filter active-status-filter)
                       [:div
                        [:p "You don't have any active records: "
                         [:a {:on-click #(om/update! page :status-filter (set (keys status-freq)))}
                          "show all documents"] "."]
                        (om/build NewDocumentButton nil)]
                       [:div
                        [:p "No documents match your filter: "
                         [:a {:on-click #(om/update! page :status-filter (set (keys status-freq)))}
                          "show all documents"] "."]
                        (om/build NewDocumentButton nil)])))]]
               [:div.col-sm-3
                (if-not (empty? status-freq)
                  [:div
                   (for [[sid sname] status]
                     (let [freq (get status-freq sid)]

                       [:div [:label
                              [:input {:type     "checkbox"
                                       :disabled (not freq)
                                       :checked  (contains? relevant-status-filter sid)
                                       :on-click #(toggle-status-filter page status-filter sid)}]
                              " " sname
                              (if freq [:span.freq " (" freq ")"])
                              ]]))])]]]]))))


(defn LegacyIECompatibility [props owner]
  (reify
    om/IDisplayName
    (display-name [_] "LegacyIECompatibility")
    om/IRender
    (render [_]
      (html [:div.LegacyIECompatibility

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
                [:br]]]]]))))


(defn AppRoot [app owner]
  (reify
    om/IDisplayName (display-name [_] "AppRoot")
    om/IRender
    (render [_]
      (if (and goog.userAgent.IE (not (goog.userAgent.isVersionOrHigher 10)))
        (om/build LegacyIECompatibility nil)
        (om/build PageView (:page app))))))


(set! (.-onbeforeunload js/window)
      (fn []
        (if (get-in @app-state [:form :dirty])
          "This will navigate away from the Data Submission Tool and all unsaved work will be lost. Are you sure you want to do this?")))

(defmethod PageView "Theme"
  [page owner]
  (om/component
    (html [:div.PageViewTheme.container
           (om/build BackButton nil)
           [:h1 "Research theme keywords"]
           [:p.help-block "Select keyword(s) to add to record"]
           (om/build KeywordsThemeTable nil)])))

(defn main []
  (when-let [ele (.getElementById js/document "Content")]
    (condense.watch-state/enable-state-change-reporting app-state)
    ;(condense.performance/enable-performance-reporting)
    (when (-> @app-state :page :name nil?)
      (reset! app-state (initial-state (js->clj (aget js/window "payload") :keywordize-keys true)))
      (router/start! {:iref app-state
                      :path [:page :tab]
                      :->hash (fnil name "")
                      :<-hash #(if (blank? %) :data-identification (keyword %))}))
    (om/root
      AppRoot
      app-state
      {:target     ele
       :shared     {:notif-chan notif-chan
                    :pub-chan   pub-chan}
       :instrument condense.performance/performance-instrument})))
