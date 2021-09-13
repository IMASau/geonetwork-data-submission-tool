import React from 'react';
import PropTypes from 'prop-types';
import * as BPCore from '@blueprintjs/core';
import {hasErrorIntent} from "../utils";


export function InputField({value, hasError, disabled, placeholder, onChange}) {
    const intent = hasErrorIntent({hasError, disabled});
    return (
        <BPCore.InputGroup
            intent={intent}
            disabled={disabled}
            value={value}
            placeholder={placeholder}
            onChange={(e) => onChange(e.target.value)}
        >
        </BPCore.InputGroup>
    );
}

InputField.propTypes = {
    value: PropTypes.string,
    placeholder: PropTypes.string,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    onChange: PropTypes.func,
}
