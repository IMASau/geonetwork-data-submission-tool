(ns interop.fixed-data-table-2
  (:require ["fixed-data-table-2" :as FixedDataTable2]
            [reagent.core :as r]))

(assert FixedDataTable2)

(def Table (r/adapt-react-class FixedDataTable2/Table))
(def Column (r/adapt-react-class FixedDataTable2/Column))
(def Cell (r/adapt-react-class FixedDataTable2/Cell))
