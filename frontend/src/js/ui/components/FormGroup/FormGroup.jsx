import React from 'react';
import PropTypes from 'prop-types';
import * as BPCore from '@blueprintjs/core';
import {Classes} from '@blueprintjs/core';
import {hasErrorIntent, requiredLabelInfo, TooltipButton} from '../utils';

function LabelInfo({required, toolTip}) {
    const labelInfo = requiredLabelInfo({required});
    return (
        <span className="FormGroupLabelInfo">
            <span className={Classes.TEXT_MUTED}> {labelInfo}</span>
            <TooltipButton toolTip={toolTip}/>
        </span>
    )
}

export function FormGroup({label, required, toolTip, helperText, hasError, disabled, children}) {
    const intent = hasErrorIntent({hasError, disabled});
    return (
        <BPCore.FormGroup
            className="FormGroup"
            label={label}
            helperText={helperText}
            intent={intent}
            disabled={disabled}
            labelInfo={<LabelInfo required={required} toolTip={toolTip}/>}
        >
            {children}
        </BPCore.FormGroup>
    );
}

FormGroup.propTypes = {
    label: PropTypes.string,
    required: PropTypes.bool,
    toolTip: PropTypes.string,
    helperText: PropTypes.string,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
}

export function InlineFormGroup({label, required, toolTip, helperText, hasError, disabled, children}) {
    const intent = hasErrorIntent({hasError, disabled});
    return (
        <BPCore.FormGroup
            className="InlineFormGroup"
            label={<span className="InlineFormGroupLabelText">{label}</span>}
            inline={true}
            intent={intent}
            disabled={disabled}
            labelInfo={<LabelInfo required={required} toolTip={toolTip}/>}
        >
            {children}
            <div class="bp3-form-helper-text">{helperText}</div>
        </BPCore.FormGroup>
    );
}

InlineFormGroup.propTypes = {
    label: PropTypes.string.isRequired,
    required: PropTypes.bool,
    toolTip: PropTypes.string,
    helperText: PropTypes.string,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
}
