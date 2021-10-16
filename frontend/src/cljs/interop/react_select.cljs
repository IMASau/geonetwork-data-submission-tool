(ns interop.react-select
  (:require ["react" :as React]
            ["react-select" :default Select :as ReactSelect]
            ["react-select/async" :default AsyncSelect]
            ["react-select/async-creatable" :default AsyncCreatable]
            ["react-select/creatable" :default Creatable]))

(def ReactSelect* (React/createFactory Select))
(def ReactSelectAsync* (React/createFactory AsyncSelect))
(def ReactSelectAsyncCreatable* (React/createFactory AsyncCreatable))
