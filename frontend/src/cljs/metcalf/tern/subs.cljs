(ns metcalf.tern.subs)

; TODO: should be passed from server #low-code
(def edit-tabs
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

(defn get-edit-tab-props
  [[page derived-db]]
  (let [selected-tab (get page :tab :data-identification)]
    {:selected-tab selected-tab
     :tab-props    (mapv
                     (fn [{:keys [id text]}]
                       (let [progress (get-in derived-db [:progress])
                             error-count (get-in progress [:page-errors id])]
                         {:id          id
                          :title       text
                          :has-errors? (and error-count (> error-count 0))}))
                     edit-tabs)}))