(ns metcalf.common.subs)


(defn get-input-field-with-label-props
  [db [_ db-path]]
  (get-in db (conj db-path :properties)))


(defn get-textarea-field-with-label-props
  [db [_ db-path]]
  (get-in db (conj db-path :properties)))


; FIXME: leaking empty strings instead of null from payload.forms.data
(defn get-date-field-with-label-props
  [db [_ db-path]]
  (get-in db (conj db-path :properties)))
