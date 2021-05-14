(ns condense.derived)

(defn derived-atom!
  "
  This allows us to access derived values associated with atom state.

  The implementation of atom does not use deref so we aren't interfering
  with how state transitions.  They use .-state to access the pure attribute
  value.

  The implementation of om cursors uses deref to resolve the current value
  of state so we are able to apply our logic and have the result available
  via (value c) which means reference cursors will trigger updates based
  on changes to derived data too.
  "
  [iref derived-fn]
  (specify! iref
            IDeref (-deref [_] (derived-fn (.-state iref))))
  iref)