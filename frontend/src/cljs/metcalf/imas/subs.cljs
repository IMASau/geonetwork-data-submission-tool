(ns metcalf.imas.subs)

(def edit-tabs
  "Default edit tabs for imas deploy"
  [{:id :data-identification :text "Identification"}
   {:id :what :text "What"}
   {:id :when :text "When"}
   {:id :where :text "Where"}
   {:id :how :text "How"}
   {:id :who :text "Who"}
   {:id :about :text "About"}
   {:id :upload :text "Data sources"}
   {:id :lodge :text "Lodge"}])

(defn get-edit-tab-props
  [[page form-state]]
  (let [selected-tab (get page :tab :data-identification)]
    {:selected-tab selected-tab
     :tab-props    (mapv
                     (fn [{:keys [id text]}]
                       (let [progress (get-in form-state [:progress/score])
                             error-count (get-in progress [:page-errors id])]
                         {:id          id
                          :title       text
                          :has-errors? (and error-count (> error-count 0))}))
                     edit-tabs)}))
