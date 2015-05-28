(ns condense.autocomplete
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [cljs.core.async :as async :refer [put! <! alts! chan pub sub timeout dropping-buffer]]
            [om-tick.bootstrap :refer [select-template]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.match :refer-macros [match]]
            [sablono.core :as html :refer-macros [html]]
            [clojure.string :as string]
            goog.labs.userAgent.platform
            goog.dom.selection
            goog.style))


(def ESCAPE_KEY 27)
(def ENTER_KEY 13)
(def UP_KEY 38)
(def DOWN_KEY 40)
(def TAB_KEY 9)
(def SPACE_KEY 32)

(defn next-option
  "Given a value in a list of options, return the next option."
  [options value]
  (let [values (map first options)
        has-cursor? (contains? (set values) value)
        has-options? (not (empty? options))]
    (match [has-cursor? has-options?]
           [_ false] nil
           [false true] (first values)
           [true true] (or (second (drop-while (partial not= value) values)) value))))

(defn prev-option
  "Given a value in a list of options, return the previous option."
  [options value]
  (let [values (map first options)
        has-cursor? (contains? (set values) value)
        has-options? (not (empty? options))]
    (match [has-cursor? has-options?]
           [_ false] nil
           [false true] (first values)
           [true true] (or (last (take-while (partial not= value) values)) value))))

(defn filtered-options [options query]
  ; FIXME: dangerous regex builder
  (if (empty? query)
    options
    (let [re (re-pattern (str "(?i)" query))]
      (filter (fn [[k v]] (re-seq re v)) options))))

(defn AutoComplete [props owner]
  (reify
    om/IDisplayName (display-name [_] "AutoComplete")

    om/IInitState
    (init-state [_]
      {:state :blurred
       :cursor (:value props)
       :event-ch (chan)
       :query nil
       :search-fn filtered-options
       :limit 100})

    om/IDidUpdate
    (did-update [_ _ prev-state]
      (let [{:keys [state cursor query search-fn]} (om/get-state owner)
            options (search-fn (:options props) query)
            values (map first options)
            has-cursor? (contains? (set values) cursor)
            state-transition? (not= state (:state prev-state))]

        (match [state has-cursor?]
               [:opened true] (let [cursor-node (om/get-node owner (str "list-group " cursor))
                                    container-node (om/get-node owner "list-group")]
                                (goog.style/scrollIntoContainerView cursor-node container-node state-transition?))
               :else nil)

        (when (and (= :opened state) state-transition?)
          (goog.dom.selection/setCursorPosition (om/get-node owner "input") 1)
          ; GOTCHA: This won't give us a cursor and keyboard on iOS.  Seems hard to do this!
          (.focus (om/get-node owner "input")))

        (if (and (not= state :blurred)
                 ; GOTCHA: focusing on a select input in iOS reveals opens up the options
                 ; but we're fine without since user will tap to focus.
                 (not (goog.labs.userAgent.platform/isIos)))
          (.focus (om/get-node owner "input")))))

    om/IWillMount
    (will-mount [_]
      (let [event-ch (om/get-state owner :event-ch)]
        (go
          (loop []

            (let [[s e v] (<! event-ch)
                  {:keys [value options on-select]} (om/get-props owner)
                  {:keys [search-fn limit]} (om/get-state owner)
                  state (om/get-state owner :state)
                  cursor (om/get-state owner :cursor)
                  query (om/get-state owner :query)
                  filtered-options (->> (search-fn options query)
                                        (take limit))
                  open! #(do (om/set-state! owner :cursor value)
                             (om/set-state! owner :query nil))
                  highlight! #(om/set-state! owner :cursor %)
                  next! #(highlight! (next-option filtered-options cursor))
                  prev! #(highlight! (prev-option filtered-options cursor))
                  search! (fn [v]
                            (om/set-state! owner :query v)
                            (om/set-state! owner :cursor (next-option (->> (search-fn options v)
                                                                           (take limit)) nil)))
                  select! #(on-select %)
                  select-next! #(select! (next-option filtered-options value))
                  select-prev! #(select! (prev-option filtered-options value))]

              (if (not= state s)
                (.warn js/console "Got event from wrong state" (clj->js [state s e v])))

              (let [s1 (case [s e]
                         [:blurred :click] (do (open!) :opened)
                         [:blurred :focus] :focused
                         [:focused :blur] :blurred
                         [:focused :click] (do (open!) :opened)
                         [:focused :down] (do (select-next!) :focused)
                         [:focused :up] (do (select-prev!) :focused)
                         [:focused :type] (do (search! v) :opened)
                         [:focused :enter] (do (open!) :opened)
                         [:focused :select] (do (select! v) :focused)
                         [:opened :blur] :blurred
                         [:opened :mouse-over] (do (highlight! v) :opened)
                         [:opened :down] (do (next!) :opened)
                         [:opened :up] (do (prev!) :opened)
                         [:opened :type] (do (search! v) :opened)
                         [:opened :escape] :focused
                         [:opened :mouse-move] :opened
                         [:opened :mouse-leave] :opened
                         [:opened :enter] (do (select! cursor) :focused)
                         [:opened :select] (do (select! v) :focused))]
                (om/set-state! owner :state s1)
                (recur)))))))

    om/IRenderState
    (render-state [_ {:keys [state cursor event-ch query disabled search-fn limit]}]
      (let [{:keys [options is-hidden]} props
            event-fn #(do (put! event-ch %) nil)
            filtered-options (->> (search-fn options query)
                                  (take limit))
            disabled (or disabled (empty? options))]

        (html
          (if-not is-hidden
            [:div.AutoComplete
             (case (if disabled :disabled state)

               :disabled
               (select-template (assoc props :ref "input"
                                             :disabled disabled))

               :blurred
               (select-template (assoc props :ref "input"
                                             :on-mouse-down #(do (event-fn [:blurred :click])
                                                                 (.preventDefault %))
                                             :on-focus #(event-fn [:blurred :focus])))

               :focused
               (select-template (assoc props :ref "input"
                                             :on-mouse-down #(do (event-fn [:focused :click])
                                                                 (.preventDefault %))
                                             :on-blur     #(event-fn [:focused :blur])
                                             :on-change #(event-fn [:focused :select (.. % -target -value)])
                                             :on-focus #(.preventDefault %)
                                             :on-key-down (fn [e]
                                                            (let [code (.-keyCode e)
                                                                  value (str (char code))]
                                                              (if (re-matches #"\w" value)
                                                                (do (event-fn [:focused :type (string/lower-case value)])
                                                                    (.preventDefault e))
                                                                (condp = code
                                                                  DOWN_KEY (do (event-fn [:focused :down])
                                                                               (.preventDefault e))
                                                                  UP_KEY (do (event-fn [:focused :up])
                                                                             (.preventDefault e))
                                                                  ENTER_KEY (do (event-fn [:focused :enter])
                                                                                (.preventDefault e))
                                                                  SPACE_KEY (do (event-fn [:focused :enter])
                                                                                (.preventDefault e))
                                                                  nil))))))

               :opened
               [:div.autocomplete.dropdown {:style {:position "relative"}}
                [:input.form-control.focus {:ref         "input"
                                            :on-change   #(event-fn [:opened :type (.. % -target -value)])
                                            :on-key-down (fn [e]
                                                           (when-let [code (get {ESCAPE_KEY :escape
                                                                                 ENTER_KEY  :enter
                                                                                 DOWN_KEY   :down
                                                                                 UP_KEY     :up}
                                                                                (.-keyCode e))]
                                                             (event-fn [:opened code])
                                                             (.preventDefault e)))
                                            :on-blur     #(event-fn [:opened :blur])
                                            :value       query}]
                [:ul.dropdown-menu {:ref   "list-group"
                                    :style {:overflow-y "auto"
                                            :width      "100%"
                                            :max-height "22em"
                                            :position   "absolute"
                                            :display    "block"}}
                 (if-not (empty? filtered-options)
                   (for [[id text] filtered-options]
                     (let [attrs {:ref            (str "list-group " id)
                                  :on-mouse-enter #(event-fn [:opened :mouse-over id])
                                  :on-mouse-down  #(.preventDefault %)
                                  :on-click       #(do (event-fn [:opened :select id])
                                                       (.preventDefault %))
                                  :style          {:cursor "pointer"}}]
                       (if (= id cursor)
                         [:li.active attrs [:a.menuitem text]]
                         [:li attrs [:a.menuitem text]])))
                   [:li.disabled [:a "No results found"]])]])]))))))
