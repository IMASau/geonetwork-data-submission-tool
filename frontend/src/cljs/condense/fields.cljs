(ns condense.fields
  "Simple data entry fields with bootstrap styling"
  (:require-macros [cljs.core.async.macros :refer [go-loop alt!]])
  (:require [cljs.core.async :as async :refer [put! <! chan timeout]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.match :refer-macros [match]]
            [sablono.core :as html :refer-macros [html]]
            [clojure.string :as string]
            [goog.dom :as gdom]
            cljsjs.pikaday.with-moment
            cljsjs.moment
            cljs-time.core
            cljs-time.format
            [clojure.walk :refer [postwalk]]
            [om-tick.field :refer [field-zipper field-edit reset-field]]
            [om-tick.bootstrap :refer [validation-state]]
            [condense.utils :refer [vec-remove]]))


(defn fmap [f m]
  (into (empty m) (for [[k v] m] [k (f v)])))


(defn load-data
  "Load data into a form"
  [fields data]
  (reduce
    (fn [fields-acc [k v]]
      (assoc-in fields-acc [k :value] v))
    fields data))


(defn del-value!
  "Helper to delete a value from a list field by index
   which assumes the standard :many field conventions"
  [many-field i]
  (om/transact! many-field :value #(vec-remove % i)))


(defn new-value-field
  [many-field]
  ; TODO: use field-postwalk to avoid inefficiencies with long options lists
  (let [fields (:fields (om/value many-field))]
    {:value (field-edit (field-zipper fields) reset-field)}))


(defn add-field!
  ([many-field]
   (let [new-field (new-value-field many-field)]
     (add-field! many-field new-field)))
  ([many-field field]
   (om/transact! many-field :value #(conj % (om/value field)))))


(defn add-value!
  "Helper to append a new value to a many-field.

  * Initialise new field map based on :fields
  * Load any data provided
  "
  [many-field value]
  (add-field! many-field (-> (new-value-field many-field)
                             (assoc :value (om/value value)))))


(defn label-template [{:keys [label required]}]
  (if label [:label label (if required " *")]))


(defn help-block-template [{:keys [errors show-errors help]}]
  [:p.help-block help])


(def empty-values #{nil "" [] {} #{}})

(defn validate-required-field
  "Validate required field adding error message to list if it exists"
  [field]
  (let [{:keys [required value errors]} field]
    (if (and required (contains? empty-values value))
      (assoc field :errors (conj errors "This field is required"))
      field)))


(defn input-template [props owner]
  (let [{:keys [value addon-before addon-after]} props
        input-control [:input.form-control (assoc props
                                             :value (or value "")
                                             :key "ifc")]]
    (html [:div.form-group {:class (validation-state props)}
           (label-template props)
           (if (or addon-after addon-before)
             [:div.input-group {:key "ig"}
              addon-before input-control addon-after]
             input-control)
           (help-block-template props)])))


(defn Input [props owner]
  (om/component
    (input-template props owner)))


; TODO: use html input date field for mobile devices
; http://caniuse.com/#search=input-date


(defn unpack-date [value]
  (let [m (js/moment value)]
    (if (.isValid m)
      [(.year m) (+ 1 (.month m)) (.date m)])))


(defn Date [props owner]
  (reify
    om/IDisplayName (display-name [_] "Date")
    om/IInitState
    (init-state [_] {:event-ch      (chan)
                     :current-state :idle
                     :year          ""
                     :month         ""
                     :day           ""})

    om/IWillMount
    (will-mount [_]
      (let [event-ch (om/get-state owner :event-ch)]
        (go-loop [state :idle]
          (om/set-state! owner :current-state state)
          (let [edit! #(let [[y m d] (unpack-date (:value (om/get-props owner)))]
                        (om/set-state! owner :year y)
                        (om/set-state! owner :month m)
                        (om/set-state! owner :day d))

                blur! #(let [{:keys [year month day]} (om/get-state owner)
                             value (js/moment (str year "-" month "-" day))
                             prev-value (js/moment (om/get-props owner :value))]
                        (if (and (.isValid value)
                                 (not= (take 3 (.toArray prev-value))
                                       (take 3 (.toArray value))))
                          (if-let [on-value-change (om/get-props owner :on-value-change)]
                            (on-value-change (.format value "YYYY-MM-DD"))))
                        (if-let [on-blur (om/get-props owner :on-blur)]
                          (on-blur nil)))

                change! (fn [k v]
                          (let [v' (string/replace v #"\D" "")]
                            (om/set-state! owner k v')))]

            (let [event (<! event-ch)]
              (recur (match [state event]
                       [:idle [:focus]] (do (edit!) :edit)
                       [:edit [:change k v]] (do (change! k v) :edit)
                       [:edit [:blur]] (do (blur!) :idle)
                       :else state)))))))

    om/IRenderState
    (render-state [_ {:keys [event-ch current-state year month day]}]
      (let [{:keys [value disabled]} props
            [yyyy mm dd] (if (= :idle current-state)
                           (unpack-date value)
                           [year month day])
            event! (fn [& args]
                     (put! event-ch (vec args)) nil)]
        (html [:div.Date {:class    (validation-state props)
                          :on-blur  #(let [related (.-relatedTarget %)]
                                      (if-not (and related (gdom/contains (.-currentTarget %) related))
                                        (event! :blur)))
                          :on-focus #(event! :focus)}
               (label-template props)
               [:div.DateInputs
                (input-template {:ref        "dd"
                                 :value      (or dd "")
                                 :disabled   disabled
                                 :max-length 2
                                 :on-change  #(event! :change :day (.. % -target -value))
                                 :help       "DD"
                                 :size       2} owner)
                [:span.slash " / "]
                (input-template {:ref        "mm"
                                 :value      (or mm "")
                                 :disabled   disabled
                                 :max-length 2
                                 :on-change  #(event! :change :month (.. % -target -value))
                                 :help       "MM"
                                 :size       2} owner)
                [:span.slash " / "]
                (input-template {:ref        "yyyy"
                                 :value      (or yyyy "")
                                 :disabled   disabled
                                 :max-length 4
                                 :on-change  #(event! :change :year (.. % -target -value))
                                 :help       "YYYY"
                                 :size       4} owner)
                #_[:div [:div.btn.btn-md [:span.glyphicon.glyphicon-calendar]]]]
               (help-block-template props)])))))

(defn update-fields! [owner value]
  (let [date (cljs-time.format/parse value)]
    (om/set-state! owner :day (cljs-time.core/day date))
    (om/set-state! owner :month (cljs-time.core/month date))
    (om/set-state! owner :year (cljs-time.core/year date))))


(defn Date1 [props owner]
  (reify
    om/IDisplayName (display-name [_] "Date")
    om/IInitState
    (init-state [_] {:year  ""
                     :month ""
                     :day   ""})

    om/IWillMount
    (will-mount [_]

      (update-fields! owner (:value props)))

    om/IDidUpdate
    (did-update [_ prev-props prev-state]
      (if-not (= (:value prev-props) (:value props))
        (print :update-fields! :owner (:value props))))

    om/IRenderState
    (render-state [_ {:keys [year month day]}]
      (let [{:keys [label value]} props
            ;date (cljs-time.format/parse value)
            ;day (cljs-time.core/day date)
            ;month (cljs-time.core/month date)
            ;year (cljs-time.core/year date)
            update-value! #(om/update! props :value
                                       (cljs-time.format/unparse
                                         (cljs-time.format/formatters :year-month-day)
                                         (cljs-time.core/date-time year month day)))]
        (html [:div.Date
               (label-template props)
               [:div.DateInputs
                (om/build Input {:value     day
                                 :on-blur   #(update-value!)
                                 :on-change #(om/set-state! owner :day (.. % -target -value))
                                 :help      "DD" :size 2}) [:span.slash " / "]
                (om/build Input {:value     month
                                 :on-blur   #(update-value!)
                                 :on-change #(om/set-state! owner :month (.. % -target -value))
                                 :help      "MM" :size 2}) [:span.slash " / "]
                (om/build Input {:value     year
                                 :on-blur   #(update-value!)
                                 :on-change #(om/set-state! owner :year (.. % -target -value))
                                 :help      "YYYY" :size 4})
                #_[:div [:div.btn.btn-md [:span.glyphicon.glyphicon-calendar]]]]
               (help-block-template props)])))))


(defn PikadayDate [props owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (let [{:keys [format on-pikaday-select]
             :or   {format "YYYY-MM-DD"}} props
            pickdate (new js/Pikaday #js {:field    (om/get-node owner "input")
                                          :format   format
                                          ; GOTCHA: Picaday will not trigger the on-change event and
                                          ; onSelect isn't event based so we require an additional
                                          ; on-select callback.
                                          :onSelect #(this-as this
                                                      (let [value (.. this (getMoment) (format format))]
                                                        (on-pikaday-select value)))})]
        (om/set-state! owner :pickdate pickdate)))

    om/IRender
    (render [_]
      (let [{:keys [required help errors placeholder]} props
            help-block (if-not (empty? errors) (string/join ", " errors) help)
            input-control [:input.form-control (assoc props
                                                 :key "ifc"
                                                 :ref "input"
                                                 :placeholder (if placeholder (str placeholder (if required " *"))))]]
        (html [:div.form-group {:class (validation-state props)}
               (label-template props)
               input-control
               (help-block-template props)])))))

(defn Option [props owner]
  (om/component
    (let [[value display] props]
      (dom/option #js {:value value} display))))

(defn Textarea [props owner]
  (om/component
    (let [{:keys [value]} props]
      (html [:div.form-group {:class (validation-state props)}
             (label-template props)
             [:textarea (assoc props
                          :value (or value "")
                          :class "form-control"
                          :key "textarea")]
             (help-block-template props)]))))

(defn ExpandingTextarea
  "http://alistapart.com/article/expanding-text-areas-made-elegant"
  [props owner]
  (om/component
    (let [{:keys [value is-hidden]} props]
      (html [:div.form-group {:class (str (validation-state props) " "
                                          (when is-hidden "hidden"))}
             (label-template props)
             [:div.expandingArea.active {:style {:position "relative"}}
              [:pre {:class "form-control"} [:span value] [:br ]]
              [:textarea (assoc props
                           :value (or value "")
                           :class "form-control"
                           :key "textarea")]]
             (help-block-template props)]))))

(defn Checkbox [props owner]
  (om/component
    (let [{:keys [label checked on-change disabled]} props
          input-control (dom/input #js {:type     "checkbox"
                                        :checked  checked
                                        :disabled disabled
                                        :onChange on-change})]
      (html [:div.form-group {:class (validation-state props)}
             [:div.checkbox
              [:label input-control label]]
             (help-block-template props)]))))
