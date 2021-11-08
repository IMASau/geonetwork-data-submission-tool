(ns metcalf4.views
  (:require [clojure.string :as string]
            [goog.object :as gobj]
            [interop.react-imask :as react-imask]
            [metcalf3.utils :as utils3]
            [metcalf3.widget.modal :as modal]
            [metcalf3.views :as views3]
            [interop.moment :as moment]
            [metcalf4.low-code :as low-code]
            [clojure.edn :as edn]
            [interop.blueprint :as bp3]))

; For pure views only, no re-frame subs/handlers

(defn KeywordsThemeCell [rowData]
  (let [rowData (take-while (complement empty?) rowData)]
    [:div.topic-cell
     [:div.topic-path (string/join " > " (drop-last (rest rowData)))]
     [:div.topic-value (last rowData)]]))

(defn m4-modal-dialog-table-modal-edit-form
  [{:keys [form path title on-delete-click on-close-click on-save-click]}]
  [modal/Modal {:ok-copy      "Done"
                :modal-header [:span [:span.glyphicon.glyphicon-list] " Edit " title]
                :modal-body   [form path]
                :modal-footer [:div
                               [:a.btn.text-danger.pull-left
                                {:on-click #(do (.preventDefault %) (on-delete-click))}
                                [:span.glyphicon.glyphicon-remove] " Delete"]
                               [:button.btn.btn-primary {:on-click on-close-click} "Done"]]
                :on-dismiss   on-close-click
                :on-save      on-save-click}])

(defn m4-modal-dialog-table-modal-add-form
  [{:keys [form path title on-close-click on-save-click]}]
  [modal/Modal {:ok-copy      "Done"
                :modal-header [:span [:span.glyphicon.glyphicon-list] " Add " title]
                :modal-body   [form path]
                :on-dismiss   on-close-click
                :on-cancel    on-close-click
                :on-save      on-save-click}])

(defn modal-dialog-confirm
  [{:keys [message title on-dismiss on-cancel on-save]}]
  [modal/Modal
   {:modal-header [:span [:span.glyphicon.glyphicon-question-sign] " " title]
    :dialog-class "modal-sm"
    :modal-body   message
    :on-dismiss   on-dismiss
    :on-cancel    on-cancel
    :on-save      on-save}])

(defn modal-dialog-alert
  [{:keys [message on-dismiss on-save]}]
  [modal/Modal
   {:modal-header [:span [:span.glyphicon.glyphicon-exclamation-sign]
                   " " "Alert"]
    :dialog-class "modal-sm"
    :modal-body   message
    :on-dismiss   on-dismiss
    :on-save      on-save}])

(defn document-teaser
  [{:keys [doc on-archive-click on-delete-archived-click on-restore-click on-clone-click on-edit-click]}]
  (let [{:keys [title last_updated last_updated_by status transitions is_editor owner]} doc
        handle-archive-click (fn [e] (.stopPropagation e) (on-archive-click doc))
        handle-delete-archived-click (fn [e] (.stopPropagation e) (on-delete-archived-click doc))
        handle-restore-click (fn [e] (.stopPropagation e) (on-restore-click doc))
        handle-clone-click (fn [e] (.stopPropagation e) (on-clone-click doc))
        handle-edit-click (fn [e] (.stopPropagation e) (on-edit-click doc))
        transitions (set transitions)]

    [:div.bp3-card.bp3-interactive.DocumentTeaser
     {:on-click handle-edit-click}
     (when is_editor
       [:div.pull-right
        (when (contains? transitions "archive")
          [:span.btn.btn-default.noborder.btn-xs
           {:on-click handle-archive-click}
           [:span.glyphicon.glyphicon-trash] " archive"])
        (when (contains? transitions "delete_archived")
          [:span.btn.btn-default.noborder.btn-xs
           {:on-click handle-delete-archived-click}
           [:span.glyphicon.glyphicon-remove] " delete"])
        (when (contains? transitions "restore")
          [:span.btn.btn-default.noborder.btn-xs
           {:on-click handle-restore-click}
           [:span.glyphicon.glyphicon-open] " restore"])
        [:span.btn.btn-default.noborder.btn-xs
         {:on-click handle-clone-click}
         [:span.glyphicon.glyphicon-duplicate] " clone"]
        [:span.btn.btn-default.noborder.btn-xs {:on-click handle-edit-click}
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

(defn new-document-button
  [{:keys [on-click]}]
  [:button.btn.btn-primary {:on-click on-click}
   [:span.glyphicon.glyphicon-plus]
   " Create new record"])

(defn dashboard
  [{:keys [dashboard-props
           dashboard-create-click
           dashboard-show-all-click
           dashboard-toggle-status-filter
           document-archive-click
           document-delete-archived-click
           document-restore-click
           document-clone-click
           document-edit-click]}]
  (let [{:keys [filtered-docs status-filter has-documents? status-freq status relevant-status-filter]} dashboard-props]
    [:div
     [views3/navbar]
     [:div.container
      [:span.pull-right {:style {:margin-top 18}}
       [new-document-button
        {:on-click dashboard-create-click}]]
      [:h1 "My Records"]
      [:div.row
       [:div.col-sm-9
        (-> [:div.list-group]
            (into (for [filtered-doc filtered-docs]
                    ^{:key (:url filtered-doc)}
                    [document-teaser
                     {:doc                      filtered-doc
                      :on-archive-click         #(document-archive-click filtered-doc)
                      :on-delete-archived-click #(document-delete-archived-click filtered-doc)
                      :on-restore-click         #(document-restore-click filtered-doc)
                      :on-clone-click           #(document-clone-click filtered-doc)
                      :on-edit-click            #(document-edit-click filtered-doc)}]))
            (conj (if has-documents?
                    [:a.list-group-item {:on-click dashboard-create-click}
                     [:span.glyphicon.glyphicon-star.pull-right]
                     [:p.lead.list-group-item-heading " My first record "]
                     [:p.list-group-item-text "Welcome! Since you’re new here, "
                      [:span {:style {:text-decoration "underline"}} "Click here"] " to get started."]]
                    (when (empty? filtered-docs)
                      (if (= status-filter #{"Draft" "Submitted"})
                        [:div
                         [:p "You don't have any active records: "
                          [:a {:on-click dashboard-show-all-click}
                           "show all documents"] "."]
                         [new-document-button
                          {:on-click dashboard-create-click}]]
                        [:div
                         [:p "No documents match your filter: "
                          [:a {:on-click dashboard-show-all-click}
                           "show all documents"] "."]
                         [new-document-button
                          {:on-click dashboard-create-click}]])))))]
       [:div.col-sm-3
        (when-not (empty? status-freq)
          (into [:div]
                (for [[status-id sname] status]
                  (let [freq (get status-freq status-id)]
                    [:div [:label
                           [:input {:type      "checkbox"
                                    :disabled  (not freq)
                                    :checked   (contains? relevant-status-filter status-id)
                                    :on-change #(dashboard-toggle-status-filter {:status-id status-id :status-filter status-filter})}]
                           " " sname
                           (when freq [:span.freq " (" freq ")"])]]))))]]]]))

(defn progress-bar
  [{:keys [can-submit? value]}]
  [:div
   [:span.progressPercentage (str (int (* value 100)) "%")]
   [bp3/progress-bar {:animate false
                      :intent  (if can-submit? "success" "warning")
                      :stripes false
                      :value   value}]])


(defn edit-tabs
  [{:keys [form tab-props on-pick-tab]}]
  (let [{:keys [disabled]} form
        {:keys [selected-tab tab-props]} tab-props]
    (letfn [(pick-tab [id _ _] (on-pick-tab (edn/read-string id)))]
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
                       [views3/progress-bar]])])))])))

(defn PageViewEdit
  [{:keys [page context form dirty on-save-click on-archive-click
           tab-props on-pick-tab]}]
  (let [{:keys [urls user]} context
        {:keys [disabled]} form
        {:keys [status title last_updated last_updated_by is_editor owner]} (:document context)
        saving (::handlers3/saving? page)]
    [:div
     [views3/navbar]
     [:div.container
      [:div.pagehead
       [:div.pull-right
        (when is_editor
          [:button.btn.btn-default.text-warn {:on-click on-archive-click
                                              :disabled disabled}
           [:span.fa.fa-archive]
           " Archive"]) " "
        [:button.btn.btn-primary {:disabled (or disabled (not dirty) saving)
                                  :on-click on-save-click}
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
        [:strong (if (string/blank? title) "Untitled" title)]
        " "
        [:span.label.label-info {:style {:font-weight "normal"}} status]
        [:br]
        [:small [:i {:style {:color     "#aaa"
                             :font-size "1em"}}
                 "Last edited " (moment/from-now last_updated)
                 " by " (:username last_updated_by)]]]]]
     [:div.Home.container
      [edit-tabs
       {:form        form
        :tab-props   tab-props
        :on-pick-tab on-pick-tab}]
      [:div.PageViewBody
       [low-code/render-template {:template-id (get page :tab :data-identification)}]]]]))
