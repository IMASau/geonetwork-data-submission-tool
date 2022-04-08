(ns metcalf.imas.subs
  (:require [metcalf.common.blocks4 :as blocks4]))

(def edit-tabs
  "Default edit tabs for imas deploy"
  [{:id         :data-identification
    :text       "Identification"
    :data-paths [["identificationInfo" "title"]
                 ["identificationInfo" "dateCreation"]
                 ["identificationInfo" "topicCategories"]
                 ["identificationInfo" "status"]
                 ["identificationInfo" "maintenanceAndUpdateFrequency"]]}
   {:id         :what
    :text       "What"
    :data-paths [["identificationInfo" "abstract"]
                 ["identificationInfo" "keywordsTheme"]]}
   {:id         :when
    :text       "When"
    :data-paths [["identificationInfo" "extents" "beginPosition"]
                 ["identificationInfo" "extents" "endPosition"]]}
   {:id         :where
    :text       "Where"
    :data-paths [["identificationInfo" "extents" "geographicElement"]
                 ["identificationInfo" "extents" "verticalElement"]]}
   {:id         :how
    :text       "How"
    :data-paths [["resourceLineage" "statement"]]}
   {:id         :who
    :text       "Who"
    :data-paths [["pointOfContact"]
                 ["identificationInfo" "citedResponsibleParty"]]}
   {:id         :about
    :text       "About"
    :data-paths [["identificationInfo" "creativeCommons"]]}
   {:id         :upload
    :text       "Data sources"
    :data-paths []}
   {:id         :lodge
    :text       "Lodge"
    :data-paths []}])

(defn get-edit-tabs
  "Sub to return edit-tab data.  Defaults to edit-tabs if not set in app-db."
  [db]
  (or (get db :low-code/edit-tabs) edit-tabs))

(defn- get-next-tab
  [tab-id]
  (let [successors (drop-while #(not= tab-id (:id %)) edit-tabs)]
    (second successors)))

(defn- get-next-tab2
  [tab-id edit-tabs]
  (let [successors (drop-while #(not= tab-id (:id %)) edit-tabs)]
    (second successors)))

(defn get-edit-tab-props
  [[page form-state]]
  (let [selected-tab (get page :tab :data-identification)]
    {:selected-tab selected-tab
     :next-tab     (get-next-tab selected-tab)
     :tab-props    (mapv
                     (fn [{:keys [id text]}]
                       (let [progress (get-in form-state [:progress/score])
                             error-count (get-in progress [:page-errors id])]
                         {:id          id
                          :title       text
                          :has-errors? (and error-count (> error-count 0))}))
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
       :next-tab     (get-next-tab2 selected-tab edit-tabs)
       :tab-props    (mapv
                       (fn [{:keys [id text data-paths]}]
                         (let [has-errors? (some has-block-errors? data-paths)
                               has-required-fields? (seq data-paths)]
                           {:id                   id
                            :title                text
                            :has-errors?          (boolean has-errors?)
                            :has-required-fields? (boolean has-required-fields?)}))
                       edit-tabs)})))

(defn get-unique-contacts-sub
  [state [_ {:keys [data-path source-path value-path]}]]
  (let [data-path (blocks4/block-path data-path)
        source-path (blocks4/block-path source-path)
        value-path (blocks4/block-path value-path)
        data (blocks4/as-data (get-in state data-path))
        source (blocks4/as-data (get-in state source-path))]
    (js/console.log "get-unique-contacts-sub")
    source))