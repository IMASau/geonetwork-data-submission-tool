import React from 'react';
import PropTypes from 'prop-types';
import * as BPCore from '@blueprintjs/core';
import {hasErrorIntent, useCachedState} from "../utils";

// [:div.bp3-control-group
//     [:div.bp3-fill
//      [m4/input-field
//       {:form-id   [:form]
//        :data-path ["identificationInfo" "keywordsAdditional" "keywords"]}]]
//     [m4/list-add-button
//      {:form-id    [:form]
//       :data-path  ["identificationInfo" "keywordsAdditional" "keywords"]
//       :text       "Add"
//       :value-path ["value"]
//       :added-path ["isUserDefined"]}]]

export function TextAddField({hasError, disabled, placeholder, maxLength, onAddClick}) {
    const [stateValue, setStateValue] = useState("");
    const intent = hasErrorIntent({hasError, disabled});
    function onClick() {
        onAddClick(stateValue);
        setStateValue("")
    }
    return (
        <BPCore.InputGroup
            intent={intent}
            disabled={disabled}
            value={stateValue}
            placeholder={placeholder}
            maxLength={maxLength}
            onChange={(e) => setStateValue(e.target.value)}
        >
        </BPCore.InputGroup>
    );
}

TextAddField.propTypes = {
    placeholder: PropTypes.string,
    maxLength: PropTypes.number,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    onAddClick: PropTypes.func,
}
