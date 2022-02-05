(ns metcalf.common.low-code4
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [metcalf.common.schema4 :as schema4]
            [metcalf.common.utils4 :as utils4]
            [re-frame.core :as rf]
            [cljs.pprint :as pprint]
            [reagent.core :as r]
            [interop.blueprint :as bp3]))

(defonce ^:dynamic component-registry {})
(defonce ^:dynamic template-registry {})
(defonce ^:dynamic not-found-hiccup '[:div "not found"])

; Private use template cache
(def *build-template-cache (atom {}))

(defn init!
  "Merge templates into template-registry."
  [{:keys [low-code/templates]}]
  (when (map? templates)
    (reset! *build-template-cache {})
    (set! template-registry (merge template-registry templates))))

(defn variable?
  "A variable is a simple symbol which starts with ?"
  [x]
  (and (simple-symbol? x) (string/starts-with? (name x) "?")))

(defn report-unregistered-syms
  [x]
  (when (and (symbol? x) (not (variable? x)))
    (utils4/log {:level :error
                 :msg   (str "Symbol not registered: " (pr-str x))
                 :data  {:sym x}}))
  x)

(defn report-unregistered-var
  [x]
  (when (variable? x)
    (utils4/log {:level :error
                 :msg   (str "Variable not bound: " (pr-str x))
                 :data  {:var x}}))
  x)

(defn check-missing-keys
  [{:keys [config settings] :as ctx}]
  (when-let [req-ks (get-in settings [::req-ks])]
    (let [missing-ks (remove (set (keys config)) req-ks)]
      (doseq [k missing-ks]
        (utils4/log {:level :error
                     :msg   (str "Missing required key (" (pr-str k) ") in config")
                     :data  {:k k :ctx ctx}})))))

(defn check-compatible-schema
  [{:keys [settings schema config]}]
  (when-let [schema2 (get-in settings [::schema])]
    (schema4/assert-compatible-schema {:schema1 schema :schema2 schema2 :path (schema4/schema-path (:data-path config))})))

(defn check-compatible-paths
  [{:keys [settings schema] :as ctx}]
  (doseq [path (remove nil? (get-in settings [::schema-paths]))]
    (if (= "array" (get-in schema [:type]))
      (when-not (schema4/contains-path? {:schema (:items schema) :path path})
        (utils4/log {:level :error
                     :msg   (str "Path not present in schema: " (pr-str path))
                     :data  {:ctx ctx :path path}}))
      (when-not (schema4/contains-path? {:schema schema :path path})
        (utils4/log {:level :error
                     :msg   (str "Path not present in schema: " (pr-str path))
                     :data  {:ctx ctx :path path}})))))

(defn log-view-inputs-wrapper
  [{:keys [config] :as ctx} view]
  (fn log-view-inputs
    [& args]
    (utils4/log {:level :debug
                 :msg   (str ::log-view-inputs)
                 :data  {:ctx  ctx
                         :args args
                         :subs {:block-props @(rf/subscribe [:metcalf.common.components4/get-block-props config])
                                :block-data  @(rf/subscribe [:metcalf.common.components4/get-block-data config])}}})
    (apply view args)))

; NOTE: experimental
(defn pre-str [x]
  (with-out-str
    (binding [pprint/*print-miser-width* 10]
      (pprint/pprint x))))

(defn component-controls-wrapper
  "Opens an overlay with debug info on shift-click"
  [{:keys [config settings schema] :as ctx} view]
  (fn log-view-inputs
    []
    (let [*open (r/atom false)]
      (fn [& args]
        (let [block-props @(rf/subscribe [:metcalf.common.components4/get-block-props config])
              block-data @(rf/subscribe [:metcalf.common.components4/get-block-data config])]
          [:div {:onMouseDown (fn [e]
                                (when (.-altKey e)
                                  (reset! *open true)
                                  (.. e stopPropagation)))}
           [bp3/overlay {:isOpen               @*open
                         :onClose              #(reset! *open false)
                         :autoFocus            true,
                         :canEscapeKeyClose    true,
                         :canOutsideClickClose true,
                         :enforceFocus         true,
                         :hasBackdrop          true,
                         :usePortal            true,
                         :className            "bp3-overlay-scroll-container"}
            [:div.bp3-card {:style {:width "80%" :margin "10%"}}
             [:h3 (str (get-in ctx [:sym]))]
             [bp3/tabs {}
              [bp3/tab {:id    "about"
                        :title "about"
                        :panel (r/as-element
                                 [:div {:style {:white-space "pre-line"}}
                                  (:doc (meta view))])}]
              [bp3/tab {:id    "args"
                        :title "args"
                        :panel (r/as-element [:pre.bp3-text-small (pre-str args)])}]
              [bp3/tab {:id    "block-props"
                        :title "block-props"
                        :panel (r/as-element [:pre.bp3-text-small (pre-str block-props)])}]
              [bp3/tab {:id    "block-data"
                        :title "block-data"
                        :panel (r/as-element [:pre.bp3-text-small (pre-str block-data)])}]
              [bp3/tab {:id    "block-schema"
                        :title "block-schema"
                        :panel (r/as-element [:pre.bp3-text-small (pre-str schema)])}]
              ; TODO: data is confusing.  Remove or translate for users?
              [bp3/tab {:id    "settings"
                        :title "settings"
                        :panel (r/as-element [:pre.bp3-text-small (pre-str settings)])}]]]]
           (into [view] args)])))))

(goog-define enable-component-controls false)

(defn build-component
  [sym reg-data]
  (s/assert map? reg-data)
  (let [{:keys [init view]} reg-data]
    (fn [raw-config & args]
      (let [config (utils4/if-contains-update raw-config :data-path utils4/massage-data-path)
            settings (when init (init config))
            schema @(rf/subscribe [::get-data-schema config])
            ctx {:sym sym :config config :settings settings :schema schema}
            view (cond->> view (:debug/log-view-inputs config false) (log-view-inputs-wrapper ctx))
            view (cond->> view enable-component-controls (component-controls-wrapper ctx))]
        ; TODO: put checks behind a flag?
        (check-missing-keys ctx)
        (check-compatible-schema ctx)
        (check-compatible-paths ctx)
        (into [view config] args)))))

(defn eval-or-val [ctx]
  (fn [x] (if-let [f (::eval x)] (f ctx) x)))

(defn compile-form
  [env x]
  (cond (vector? x)
        (let [fx (mapv (partial compile-form env) x)]
          (if (not-any? ::eval fx)
            fx
            {::eval (fn [ctx]
                      (mapv (eval-or-val ctx) fx))}))

        (map? x)
        (let [ks (map (partial compile-form env) (keys x))
              vs (map (partial compile-form env) (vals x))
              data-ks (when (not-any? ::eval ks) ks)
              data-vs (when (not-any? ::eval vs) vs)]
          (if (and data-ks data-vs)
            (zipmap ks vs)
            {::eval (fn [ctx]
                      (zipmap (or data-ks (map (eval-or-val ctx) ks))
                              (or data-vs (map (eval-or-val ctx) vs))))}))

        (variable? x)
        {::eval (fn [ctx]
                  (if (contains? (:variables ctx) x)
                    (get-in ctx [:variables x])
                    (report-unregistered-var x)))}

        (symbol? x)
        (if-let [reg-data (get-in env [:component-registry x])]
          (build-component x reg-data)
          (report-unregistered-syms x))

        :else
        x))

(defn prepare-template
  [{:keys [template-id template-registry component-registry]}]
  (let [hiccup (get template-registry template-id not-found-hiccup)
        template (compile-form {:component-registry component-registry} hiccup)]
    template))

(def prepare-template-once (utils4/memoize-to-atom prepare-template *build-template-cache))

(defn render-template
  [{:keys [template-id variables]}]
  (let [template (prepare-template-once
                   {:template-id        template-id
                    :template-registry  template-registry
                    :component-registry component-registry})]
    ((eval-or-val {:variables variables}) template)))
