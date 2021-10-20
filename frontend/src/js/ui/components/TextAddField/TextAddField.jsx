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

export function TextAddField({value, hasError, disabled, placeholder, maxLength, onAddClick, buttonText}) {
    const [stateValue, setStateValue] = useCachedState(value);
    const intent = hasErrorIntent({hasError, disabled});

    let hasValue = stateValue !== null && stateValue !== "";
    let buttonDisabled = disabled || !hasValue;

    function onClick() {
        if (hasValue) {
            onAddClick(stateValue);
            setStateValue("");
        }
    }
    return (
    <BPCore.ControlGroup fill={true}>
        <BPCore.InputGroup
            intent={intent}
            disabled={disabled}
            value={stateValue}
            placeholder={placeholder}
            maxLength={maxLength}
            onChange={(e) => setStateValue(e.target.value)} />
        <BPCore.Button className={BPCore.Classes.FIXED} onClick={onClick} disabled={buttonDisabled} intent={BPCore.Intent.PRIMARY}>{buttonText}</BPCore.Button>
    </BPCore.ControlGroup>
    );
}

TextAddField.propTypes = {
    placeholder: PropTypes.string,
    buttonText: PropTypes.string,
    maxLength: PropTypes.number,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    onAddClick: PropTypes.func,
}
