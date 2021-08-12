(ns interop.react-virtualized
  (:require ["react" :as React]
            ["react-window" :as ReactWindow]
            ["react-virtualized" :as ReactVirtualized]
            ["react-virtualized-auto-sizer" :default AutoSizer]))

(def ReactWindow* (React/createFactory ReactWindow/FixedSizeList))
(def AutoSizer* (React/createFactory ReactVirtualized/AutoSizer))
(def VirtualScroll* (React/createFactory ReactVirtualized/VirtualScroll))
