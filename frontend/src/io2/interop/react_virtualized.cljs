(ns interop.react-virtualized
  (:require ["react" :as React]
            ["react-virtualized" :as ReactVirtualized]
            ["react-window" :as ReactWindow]))

(assert React)
(assert ReactVirtualized)
(assert ReactWindow)

(def ReactWindow* (React/createFactory ReactWindow/FixedSizeList))
(def AutoSizer* (React/createFactory ReactVirtualized/AutoSizer))
(def VirtualScroll* (React/createFactory ReactVirtualized/VirtualScroll))
