import React from 'react';
import PropTypes from 'prop-types';
import * as BPCore from '@blueprintjs/core';
import {hasErrorIntent} from "../utils";


export function TextareaField({value, hasError, disabled, placeholder, onChange}) {
    const intent = hasErrorIntent({hasError, disabled});
    return (
        <div className="TextareaField">
            <BPCore.TextArea
                growVertically={true}
                fill={true}
                intent={intent}
                disabled={disabled}
                value={value}
                placeholder={placeholder}
                onChange={(e) => onChange(e.target.value)}
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
