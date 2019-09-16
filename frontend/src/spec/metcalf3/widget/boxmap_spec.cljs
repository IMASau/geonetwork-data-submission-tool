(ns metcalf3.widget.boxmap-spec
  (:require [metcalf3.widget.boxmap :as gmap]
            [metcalf3.core-spec :as core]
            [cljs.spec.alpha :as s]
            [clojure.test.check.generators :as gen]))

(s/def ::gmap/lat-lng
  (s/with-gen #(instance? js/google.maps.LatLng %)
              #(gen/fmap (fn [[lat lng]] (js/google.maps.LatLng. lat lng))
                         (s/gen (s/tuple ::core/latitude ::core/longitude)))))

(s/def ::gmap/bounds
  (s/with-gen #(instance? js/google.maps.LatLngBounds %)
              #(gen/fmap (fn [[sw ne]] (js/google.maps.LatLngBounds. sw ne))
                         (s/gen (s/tuple ::gmap/lat-lng ::gmap/lat-lng)))))

(s/def ::clickable boolean?)
(s/def ::dragable boolean?)
(s/def ::editable boolean?)
(s/def ::visible boolean?)

(s/def ::gmap/rectangleOptions
  (s/keys :opt-un [::gmap/bounds ::clickable ::dragable ::editable ::visible]))

(s/def ::gmap/shape
  (s/with-gen #(instance? js/google.maps.Rectangle %)
              #(gen/fmap (fn [rectangleOptions]
                           (js/google.maps.Rectangle. (clj->js rectangleOptions)))
                         (s/gen ::gmap/rectangleOptions))))

(s/def ::gmap/map #(instance? js/google.maps.Map %))
(s/def ::gmap/on-change ifn?)
(s/def ::gmap/focus ::core/extent)
(s/def ::gmap/center ::gmap/lat-lng)
(s/def ::gmap/map-options (s/keys :req-un [::zoom ::gmap/center ::gmap/rectangleOptions]))
(s/def ::gmap/map-props (s/keys :req-un [::gmap/map-options ::core/extents ::focus]))

(s/fdef gmap/extent->bounds
  :args (s/cat :extent ::core/extent)
  :ret ::gmap/bounds)

(s/fdef gmap/bounds->extent
  :args (s/cat :bounds ::gmap/bounds)
  :ret ::core/extent)

(s/fdef gmap/shape->box-extent
  :args (s/cat :shape ::gmap/shape)
  :ret ::core/box-extent)

(s/fdef gmap/shape->extent
  :args (s/cat :shape ::gmap/shape)
  :ret ::core/extent)

(s/fdef gmap/shapes->extents
  :args (s/cat :shapes (s/every ::gmap/shape))
  :ret (s/coll-of ::core/box-extent))

(s/fdef gmap/init-map-props
  :ret ::gmap/map-props)

(s/fdef gmap/init-map
  :args (s/cat :div :html/element
               :map-options ::gmap/map-options)
  :ret ::gmap/map)

(s/fdef gmap/handle-focus-click
  :args (s/cat :owner any?
               :rect (s/nilable ::gmap/shape)))

(s/fdef gmap/focus-on
  :args (s/cat :owner any?
               :extent (s/nilable ::core/extent)))

(s/fdef gmap/show-all
  :args (s/cat :map ::gmap/map
               :shapes (s/every ::gmap/shape)))

(s/fdef gmap/remove-rect-by-extent
  :args (s/cat :owner any?
               :extent ::core/extent))

(s/fdef gmap/debounce
  :args (s/cat :f ifn?
               :timeout (s/? pos?)))

(s/fdef gmap/add-rect-by-extent
  :args (s/cat :owner any?
               :extent ::core/extent))

(s/fdef gmap/geographic-element->bounds
  :args (s/cat :ele ::gmap/geographicElement)
  :ret ::gmap/bounds)

(s/fdef gmap/value->box-extent
  :args (s/cat :val :value/geographicElement)
  :ret ::core/box-extent)

(s/fdef gmap/extent->value
  :args (s/cat :ext ::core/extent)
  :ret :value/geographicElement)

(s/fdef gmap/handle-delete-click
  :args ::s/any)

(s/fdef gmap/handle-add-click
  :args ::s/any)