import React from 'react';
import PropTypes from 'prop-types';
import * as BPCore from '@blueprintjs/core';
import {hasErrorIntent, useCachedState} from "../utils";

export function NumericInputField({value, hasError, hasButtons, disabled, placeholder, maxLength, onChange}) {
    const [stateValue, setStateValue] = useCachedState(value);
    const intent = hasErrorIntent({hasError, disabled});
    const buttonPosition = hasButtons ? BPCore.Position.RIGHT : "none";

    return (
        <BPCore.NumericInput
            intent={intent}
            disabled={disabled}
            value={stateValue}
            placeholder={placeholder}
            buttonPosition={buttonPosition}
            onValueChange={(value, strValue) => setStateValue(strValue)}
            onButtonClick={(value, strValue) => onChange(value)}
            onBlur={(e) => onChange((e.target.value === "") ? null : Number(e.target.value))}
        >
        </BPCore.NumericInput>
    );
}

NumericInputField.propTypes = {
    value: PropTypes.number,
    placeholder: PropTypes.string,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    hasButtons: PropTypes.bool,
    onChange: PropTypes.func,
}
