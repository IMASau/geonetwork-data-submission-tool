(ns metcalf3.widget.table
  (:require [cljsjs.fixed-data-table-2]
            [reagent.core :as r]))

(def Table (r/adapt-react-class js/FixedDataTable2.Table))
(def Column (r/adapt-react-class js/FixedDataTable2.Column))
(def Cell (r/adapt-react-class js/FixedDataTable2.Cell))
