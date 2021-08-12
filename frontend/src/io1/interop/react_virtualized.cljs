(ns interop.react-virtualized
  (:require cljsjs.react
            cljsjs.react-virtualized))

(def ReactWindow* (js/React.createFactory js/ReactWindow.FixedSizeList))
(def AutoSizer* (js/React.createFactory js/ReactVirtualized.AutoSizer))
(def VirtualScroll* (js/React.createFactory js/ReactVirtualized.VirtualScroll))
