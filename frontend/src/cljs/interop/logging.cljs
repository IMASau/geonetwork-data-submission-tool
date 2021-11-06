(ns interop.logging)

(defn str-value
  [data]
  (binding [*print-level* 3
            *print-length* 5]
    (pr-str data)))

(defn console-value
  [data]
  (if goog/DEBUG
    data
    (str-value data)))

(defn console-error
  [msg data]
  (js/console.error msg (console-value data)))
