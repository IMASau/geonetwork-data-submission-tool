import React from 'react';
import PropTypes from 'prop-types';
import * as BPCore from '@blueprintjs/core';
import {hasErrorIntent, useCachedState} from "../utils";


export function CheckboxField({label, checked, hasError, disabled, onChange}) {
    const intent = hasErrorIntent({hasError, disabled});
    return (
        <BPCore.Checkbox
            className={"intent-" + intent}
            disabled={disabled}
            checked={checked}
            onChange={(e) => onChange(e.target.checked)}
        >
        {label}
        </BPCore.Checkbox>
    );
}

CheckboxField.propTypes = {
    label : PropTypes.string.isRequired,
    checked : PropTypes.bool,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    onChange: PropTypes.func,
}
