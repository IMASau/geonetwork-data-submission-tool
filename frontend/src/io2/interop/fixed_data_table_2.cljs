(ns interop.fixed-data-table-2
  (:require ["fixed-data-table-2" :default FixedDataTable2]
            [reagent.core :as r]))

(def Table (r/adapt-react-class FixedDataTable2/Table))
(def Column (r/adapt-react-class FixedDataTable2/Column))
(def Cell (r/adapt-react-class FixedDataTable2/Cell))
