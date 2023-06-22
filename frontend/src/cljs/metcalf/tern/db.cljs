(ns metcalf.tern.db
  (:require [cljs.spec.alpha :as s]
            [metcalf.common.blocks4 :as blocks4]
            [metcalf.common.schema4 :as schema4]))

(s/def :modal/stack vector?)
(s/def :form/tick int?)
(s/def ::attachments vector?)
(s/def ::context map?)

(s/def ::schema ::schema4/schema)
(s/def ::state ::blocks4/block)
(s/def ::form (s/keys :req-un [::url ::data ::schema ::state]))

(s/def ::tab keyword?)
(s/def ::name string?)
(s/def ::page (s/keys :req-un [::name] :opt-un [::tab]))

(s/def ::db
  (s/keys :req [:modal/stack]
          :opt [:form/tick]
          :opt-un [::context
                   ::upload_form
                   ::attachment_data_form
                   ::attachments
                   ::page
                   ::form]))
