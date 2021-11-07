(ns metcalf4.views
  (:require [clojure.string :as string]
            [goog.object :as gobj]
            [interop.react-imask :as react-imask]
            [metcalf3.utils :as utils3]
            [metcalf3.widget.modal :as modal]
            [metcalf3.views :as views3]
            [interop.moment :as moment]))

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

(defn dashboard
  [{:keys [dashboard-props
           dashboard-create-click
           dashboard-show-all-click
           dashboard-toggle-status-filter]}]
  (let [{:keys [filtered-docs status-filter has-documents? status-freq status relevant-status-filter]} dashboard-props]
    [:div
     [views3/navbar]
     [:div.container
      [:span.pull-right {:style {:margin-top 18}} [views3/NewDocumentButton]]
      [:h1 "My Records"]
      [:div.row
       [:div.col-sm-9
        (-> [:div.list-group]
            (into (for [filtered-doc filtered-docs]
                    ^{:key (:url filtered-doc)} [views3/DocumentTeaser filtered-doc]))
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
                         [views3/NewDocumentButton]]
                        [:div
                         [:p "No documents match your filter: "
                          [:a {:on-click dashboard-show-all-click}
                           "show all documents"] "."]
                         [views3/NewDocumentButton]])))))]
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
