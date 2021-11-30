import React from 'react';
import PropTypes from 'prop-types';
import * as BPCore from '@blueprintjs/core';
import {hasErrorIntent, useCachedState} from "../utils";


export function UploadField({label, checked, hasError, disabled, onChange}) {
    const intent = hasErrorIntent({hasError, disabled});
    return (
        <BPCore.FileInput
            className={"intent-" + intent}
            disabled={disabled}
            onChange={(e) => onChange(e.target.checked)}
        >
        {label}
        </BPCore.FileInput>
    );
}

UploadField.propTypes = {
    label : PropTypes.string.isRequired,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    onChange: PropTypes.func,
}
