(ns interop.react-select
  (:require ["react" :default React]
            ["react-select" :default Select :rename {components SelectComponents}]
            ["react-select/async" :default AsyncSelect]
            ["react-select/creatable" :default Creatable]
            ["react-select/async-creatable" :default AsyncCreatable]))

(def ReactSelect* (React/createFactory Select))
(def SelectComponentsOption* (React/createFactory SelectComponents/Option))
(def SelectComponentsValueContainer* (React/createFactory SelectComponents/ValueContainer))
(def ReactSelectAsync* (React/createFactory AsyncSelect))
(def ReactSelectCreatable* (React/createFactory Creatable))
(def ReactSelectAsyncCreatable* (React/createFactory AsyncCreatable))
