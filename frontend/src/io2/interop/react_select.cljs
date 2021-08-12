(ns interop.react-select
  (:require ["react" :as React]
            ["react-select" :default Select :as ReactSelect]
            ["react-select/async" :default AsyncSelect]
            ["react-select/creatable" :default Creatable]
            ["react-select/async-creatable" :default AsyncCreatable]))

(def ReactSelect* (React/createFactory Select))
(def SelectComponentsOption* (React/createFactory ReactSelect/components.Option))
(def SelectComponentsValueContainer* (React/createFactory ReactSelect/components.ValueContainer))
(def ReactSelectAsync* (React/createFactory AsyncSelect))
(def ReactSelectCreatable* (React/createFactory Creatable))
(def ReactSelectAsyncCreatable* (React/createFactory AsyncCreatable))
