import React from 'react';
import PropTypes from 'prop-types';
import * as BPCore from '@blueprintjs/core';
import {hasErrorIntent, useCachedState} from "../utils";


export function TextareaField({value, hasError, disabled, placeholder, onChange}) {
    const [stateValue, setStateValue] = useCachedState(value);
    const intent = hasErrorIntent({hasError, disabled});
    return (
        <div className="TextareaField">
            <BPCore.TextArea
                growVertically={true}
                fill={true}
                intent={intent}
                disabled={disabled}
                value={stateValue}
                placeholder={placeholder}
                onChange={(e) => setStateValue(e.target.value)}
                onBlur={(e) => onChange(e.target.value)}
            />
        </div>
    );
}

TextareaField.propTypes = {
    value: PropTypes.string,
    placeholder: PropTypes.string,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    onChange: PropTypes.func,
}
