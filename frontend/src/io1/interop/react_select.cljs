(ns interop.react-select
  (:require cljsjs.react
            cljsjs.react-select
            cljsjs.react-virtualized))

(def ReactSelect* (js/React.createFactory js/Select))
(def SelectComponentsOption* (js/React.createFactory js/SelectComponents.Option))
(def SelectComponentsValueContainer* (js/React.createFactory js/SelectComponents.ValueContainer))
(def ReactSelectAsync* (js/React.createFactory js/AsyncSelect))
(def ReactSelectCreatable* (js/React.createFactory js/Creatable))
(def ReactSelectAsyncCreatable* (js/React.createFactory js/AsyncCreatable))
