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
            [metcalf3.content :refer []]
            [metcalf3.handlers :as handlers3]
            [metcalf3.logic :as logic3]
            [metcalf3.utils :as utils3]
            [metcalf3.widget.modal :refer [Modal]]
            [metcalf3.widget.tree :refer []]
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

(defn InputField
  [{:keys [path] :as props}]

  (let [field @(rf/subscribe [:subs/get-derived-path path])]
    [InputWidget (-> field
                     (merge (dissoc props :path))
                     (assoc
                       :on-change #(rf/dispatch [::value-changed path %])))]))

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
