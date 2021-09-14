import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';
import {hasErrorIntent} from "../utils";

/*

(defn ElasticsearchAsyncSelectField
  [_]
  (letfn [(will-mount [this]
            (let [{:keys [api-path]} (r/props this)]
              (rf/dispatch [:handlers/search-es-options api-path ""])))
          (render [this]
            (let [{:keys [dp-type dp-term-path api-path disabled]} (r/props this)
                  sub-paths (dp-term-paths dp-type)
                  {:keys [options]} @(rf/subscribe [:subs/get-derived-path api-path])
                  term @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:term sub-paths))])
                  vocabularyTermURL @(rf/subscribe [:subs/get-derived-path (conj dp-term-path (:vocabularyTermURL sub-paths))])
                  {:keys [label help required errors show-errors tooltip]} term
                  selectable-options (into-array (filterv #(gobj/get % "is_selectable") options))
                  new-term? (other-term? term vocabularyTermURL)]
              [:div
               (when new-term?
                 [:span.pull-right.new-term.text-primary
                  [:span.glyphicon.glyphicon-asterisk]
                  " New term"])
               [:div.flex-row
                [:div.flex-row-field
                 [:div.form-group {:class (when (and show-errors (seq errors)) "has-error")}
                  (when label
                    [:label label
                     (when required " *")
                     (when tooltip [Tooltip tooltip])])
                  (if-not new-term?
                    (ReactSelect
                      {:value             (if (blank? (:value vocabularyTermURL))
                                            nil
                                            #js {:vocabularyTermURL (:value vocabularyTermURL) :term (:value term)})
                       :options           selectable-options
                       :placeholder       (:placeholder term)
                       :isClearable       true
                       :is-searchable     true
                       :onInputChange     (fn [query]
                                            (rf/dispatch [:handlers/search-es-options api-path query])
                                            query)
                       :getOptionValue    (fn [option]
                                            (gobj/get option "term"))
                       :formatOptionLabel (fn [props]
                                            (r/as-element (breadcrumb-renderer props)))
                       :filterOption      (fn [_ _]
                                            ; Return true always. This allows for matches on label as well as altLabel (or other fields available in the REST API).
                                            (boolean 0))
                       :onChange          (fn [option]
                                            (rf/dispatch [:handlers/update-dp-term dp-term-path sub-paths option]))
                       :noResultsText     "No results found.  Click browse to add a new entry."
                       :isDisabled        disabled})

                    (ReactSelect
                      {:value             #js {:vocabularyTermURL "(new term)" :term (:value term)}
                       :options           selectable-options
                       :placeholder       (:placeholder term)
                       :is-searchable     true
                       :isClearable       true
                       :getOptionValue    (fn [option]
                                            (gobj/get option "term"))
                       :formatOptionLabel (fn [props]
                                            (r/as-element (breadcrumb-renderer props)))
                       :onChange          (fn [option]
                                            (rf/dispatch [:handlers/update-dp-term dp-term-path sub-paths option]))
                       :noResultsText     "No results found.  Click browse to add a new entry."}))
                  [:p.help-block help]]]
                ; TODO: Re-enable this in the future to browse/create vocabulary terms.
                ;                  [:div.flex-row-button
                ;                   [:button.btn.btn-default
                ;                    {:style    {:vertical-align "top"}
                ;                     :on-click #(rf/dispatch [:handlers/open-modal
                ;                                              {:type         param-type
                ;                                               :api-path     api-path
                ;                                               :dp-term-path dp-term-path}])}
                ;                    [:span.glyphicon.glyphicon-edit] " Custom"]
                ;                   (when help [:p.help-block {:dangerouslySetInnerHTML {:__html "&nbsp;"}}])]
                ]]))]
    (r/create-class
      {:component-will-mount will-mount
       :render               render})))


 */

export function AsyncSelectField({value, options, hasError, disabled, placeholder, onChange}) {
    return (
        <Select
            className="AsyncSelectField"
            classNamePrefix="AsyncSelectFieldPrefix"
            value={value}
            options={options}
            placeholder={placeholder}
            onChange={(e) => onChange(e.target.value)}
            isClearable={true}
            isDisabled={disabled}
            isLoading={false}
            isSearchable={false}
        >
        </Select>
    );
}

AsyncSelectField.propTypes = {
    value: PropTypes.object,
    options: PropTypes.arrayOf(PropTypes.object),
    placeholder: PropTypes.string,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    onChange: PropTypes.func,
}
