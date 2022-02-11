(ns metcalf.tern.subs
  (:require [metcalf.common.blocks4 :as blocks4]))

(def edit-tabs
  "Default edit tabs for tern.  Can be overridden through app-db state.  See init-db."
  [{:id :data-identification :text "Identification"}
   {:id :what :text "What"}
   {:id :when :text "When"}
   {:id :where :text "Where"}
   {:id :who :text "Who"}
   {:id :how :text "How"}
   {:id :quality :text "Data Quality"}
   {:id :about :text "About"}
   {:id :upload :text "Data sources"}
   {:id :lodge :text "Lodge"}])

(defn get-edit-tabs
  "Sub to return edit-tab data.  Defaults to edit-tabs if not set in app-db."
  [db]
  (or (get db :low-code/edit-tabs) edit-tabs))

(defn- get-next-tab
  [tab-id edit-tabs]
  (let [successors (drop-while #(not= tab-id (:id %)) edit-tabs)]
    (doto (second successors) prn)))

(defn get-edit-tab-props
  "Sub to return edit-tab props for use in views.
   Returns selected-tab and tab-props.
   Each tab-prop includes an id, title and has-error? flag"
  [[page form-state edit-tabs]]
  (let [selected-tab (get page :tab :data-identification)]
    {:selected-tab selected-tab
     :next-tab     (get-next-tab selected-tab edit-tabs)
     :tab-props    (mapv
                     (fn [{:keys [id text]}]
                       (let [progress (get-in form-state [:progress/score])
                             error-count (get-in progress [:page-errors id])]
                         {:id           id
                          :title        text
                          :show-errors? (and error-count (> error-count 0))}))
                     edit-tabs)}))

(defn get-edit-tab-props2
  "Sub to return edit-tab props for use in views.
   Returns selected-tab and tab-props.
   Each tab-prop includes an id, title and has-error? flag"
  [[page form-state edit-tabs]]
  (letfn [(has-block-errors? [data-path]
            (let [block (get-in form-state (blocks4/block-path data-path))]
              (get-in block [:progress/score :progress/errors])))]
    (let [selected-tab (get page :tab :data-identification)]
      {:selected-tab selected-tab
       :next-tab     (get-next-tab selected-tab edit-tabs)
       :tab-props    (mapv
                       (fn [{:keys [id text data-paths]}]
                         (let [has-errors? (some has-block-errors? data-paths)]
                           {:id          id
                            :title       text
                            :has-errors? (boolean has-errors?)}))
                       edit-tabs)})))
