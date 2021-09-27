(ns metcalf.common.handlers)


(defn input-field-with-label-value-changed
  [{:keys [db]} [_ db-path value]]
  {:db (-> db
           (assoc-in (conj db-path :props :value) value)
           (assoc-in (conj db-path :props :show-errors) true))})


(defn textarea-field-with-label-value-changed
  [{:keys [db]} [_ db-path value]]
  {:db (-> db
           (assoc-in (conj db-path :props :value) value)
           (assoc-in (conj db-path :props :show-errors) true))})


(defn date-field-with-label-value-changed
  [{:keys [db]} [_ db-path value]]
  {:db (-> db
           (assoc-in (conj db-path :props :value) value)
           (assoc-in (conj db-path :props :show-errors) true))})
