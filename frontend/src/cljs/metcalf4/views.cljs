(ns metcalf4.views
  (:require [clojure.string :as string]
            [goog.object :as gobj]
            [interop.react-imask :as react-imask]
            [metcalf3.utils :as utils3]
            [metcalf3.widget.modal :as modal]))

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
