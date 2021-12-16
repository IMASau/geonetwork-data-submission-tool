import React from 'react';
import PropTypes from 'prop-types';
import * as BPCore from '@blueprintjs/core';
import {hasErrorIntent, useCachedState} from "../utils";

export function TextAddField({hasError, disabled, placeholder, maxLength, onAddClick, buttonText}) {
    const [stateValue, setStateValue] = React.useState("");
    const intent = hasErrorIntent({hasError, disabled});

    let hasValue = stateValue !== "";
    let buttonDisabled = disabled || !hasValue;

    function onClick() {
        if (hasValue) {
            onAddClick(stateValue);
            setStateValue("");
        }
    }
    function handleKeyPress (event) {
      if(event.key === 'Enter'){
        if (hasValue) {
            onAddClick(stateValue);
            setStateValue("");
        }
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
            onKeyPress={handleKeyPress}
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
