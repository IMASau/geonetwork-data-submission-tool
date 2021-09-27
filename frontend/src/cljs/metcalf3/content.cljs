(ns metcalf3.content)

(def responsible-party-defaults
  {:individualName        {:type        nil
                           :label       "Contact name"
                           :placeholder "Contact name"
                           :required    true
                           :page        :who}
   :uri                   {:type        nil
                           :label       "Person"
                           :placeholder "Person"
                           :required    false
                           :page        :who}
   :familyName            {:type        nil
                           :label       "Surname"
                           :placeholder "Surname"
                           :maxlength   50
                           :required    true
                           :page        :who}
   :givenName             {:type        nil
                           :label       "Given name"
                           :placeholder "Given name"
                           :maxlength   50
                           :required    true
                           :page        :who}
   :orcid                 {:type        nil
                           :label       "ORCID ID"
                           :placeholder "XXXX-XXXX-XXXX-XXXX"
                           :page        :who}
   :role                  {:type        nil
                           :label       "Role"
                           :placeholder "Role"
                           :required    true
                           :page        :who}
   :organisationName      {:type        nil
                           :label       "Organisation"
                           :placeholder "Start typing to filter list..."
                           :required    true
                           :page        :who}
   :phone                 {:type      nil
                           :label     "Phone number"
                           :maxlength 20
                           :page      :who}
   :facsimile             {:type      nil
                           :label     "Fax number"
                           :maxlength 20
                           :page      :who}
   :electronicMailAddress {:type      nil
                           :label     "Email address"
                           :maxlength 100
                           :page      :who}
   :address               {:deliveryPoint      {:type nil :maxlength 250}
                           :deliveryPoint2     {:type :hidden :maxlength 250 :aria-hidden true :class "hidden"}
                           :city               {:type nil :help "City" :maxlength 100}
                           :administrativeArea {:type nil :help "State/territory" :maxlength 100}
                           :postalCode         {:type nil :help "Postal / Zip code" :maxlength 100}
                           :country            {:type nil :help "Country" :maxlength 100}}
   :isUserAdded           {:type     nil
                           :required false
                           :value    false
                           :page     :who}})

(def contact-groups
  [{:path  [:form :fields :identificationInfo :pointOfContact]
    :title "Point of contact for dataset"}
   {:path  [:form :fields :identificationInfo :citedResponsibleParty]
    :title "Responsible parties for creating dataset"}])
