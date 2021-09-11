import React from 'react';
import PropTypes from 'prop-types';
import * as BPCore from '@blueprintjs/core';
import {hasErrorIntent, requiredLabelInfo} from '../utils';

export function FormGroup({label, required, inline, helperText, hasError, disabled, children}) {
    const intent = hasErrorIntent({hasError, disabled});
    const labelInfo = requiredLabelInfo({required});
    return (
        <BPCore.FormGroup
            className="FormGroup"
            label={label}
            inline={inline}
            labelInfo={labelInfo}
            helperText={helperText}
            intent={intent}
            disabled={disabled}
        >
            {children}
        </BPCore.FormGroup>
    );
}

FormGroup.propTypes = {
    label: PropTypes.string,
    required: PropTypes.bool,
    inline: PropTypes.bool,
    helperText: PropTypes.string,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
}