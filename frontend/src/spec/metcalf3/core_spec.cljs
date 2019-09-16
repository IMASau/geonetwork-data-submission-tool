(ns metcalf3.core-spec
  (:require [cljs.spec.alpha :as s :include-macros true]
            [cljs.spec.test.alpha]
            [clojure.test.check.generators :as gen]))

(s/def :html/element #(instance? js/HTMLElement %))

(s/def ::latitude (s/with-gen
                    (s/and number? #(<= % 90) #(<= -90 %))
                    #(gen/double* {:min -90 :max 90})))
(s/def ::longitude (s/with-gen
                     (s/and number? #(<= % 180) #(<= -180 %))
                     #(gen/double* {:min -180 :max 180})))
(s/def ::zoom pos-int?)
(s/def ::extents (s/coll-of ::extent))

(s/def ::westBoundLongitude ::longitude)
(s/def ::southBoundLatitude ::latitude)
(s/def ::eastBoundLongitude ::longitude)
(s/def ::northBoundLatitude ::latitude)

(s/def ::extent (s/spec (s/cat :westBoundLongitude ::westBoundLongitude
                               :southBoundLatitude ::southBoundLatitude
                               :eastBoundLongitude ::eastBoundLongitude
                               :northBoundLatitude ::northBoundLatitude)))

(s/def ::geographicElement
  (s/and (s/keys :req-un [::westBoundLongitude
                          ::southBoundLatitude
                          ::eastBoundLongitude
                          ::northBoundLatitude])
         #(< (:westBoundLongitude %) (:eastBoundLongitude %))
         #(< (:southBoundLatitude %) (:northBoundLatitude %))))

(s/def ::box-extent (s/tuple #{:box} ::extent))


(s/def :latitude/value ::latitude)
(s/def :longitude/value ::longitude)
(s/def :value/northBoundLatitude (s/keys :req-un [:latitude/value]))
(s/def :value/westBoundLongitude (s/keys :req-un [:longitude/value]))
(s/def :value/eastBoundLongitude (s/keys :req-un [:longitude/value]))
(s/def :value/southBoundLatitude (s/keys :req-un [:latitude/value]))

(s/def :value/geographicElement
  (s/keys :req-un [:value/northBoundLatitude
                   :value/westBoundLongitude
                   :value/eastBoundLongitude
                   :value/southBoundLatitude]))

(s/def :field/type (s/nilable string?))
(s/def :field/help string?)
(s/def :field/placeholder string?)
(s/def :field/required boolean?)
(s/def :field/many boolean?)
(s/def :field/page keyword?)
(s/def :field/help string?)
(s/def :field/options (s/coll-of (s/tuple string? string?)))
(s/def :field/fields (s/map-of keyword? :form/field))

(s/def :form/any-field
  (s/keys :req-un [:field/type]
          :opt-un [:field/help
                   :field/placeholder
                   :field/required
                   :field/many
                   :field/page
                   :field/options
                   :field/fields]))

(s/def :form/fieldset
  (s/keys :req-un [:field/fields]))

(s/def :form/field
  (s/and :form/any-field
         (s/or :fieldset :form/fieldset
               :field ::s/any)))

(s/def :form/fields
  (s/or :field :form/field
        :branch (s/map-of keyword? :form/fields)))
