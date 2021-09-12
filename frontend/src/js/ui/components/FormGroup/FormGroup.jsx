import React from 'react';
import PropTypes from 'prop-types';
import * as BPCore from '@blueprintjs/core';
import {hasErrorIntent, requiredLabelInfo, TooltipButton} from '../utils';
import {Classes} from "@blueprintjs/core";

function FormGroupLabel({labelFor, label, labelInfo, toolTip}) {
    return (
        <span className="FormGroupLabel">
            <label className={Classes.LABEL} htmlFor={labelFor}>
                {label}
            </label>
            <span className={Classes.TEXT_MUTED}>{labelInfo}</span>
            <TooltipButton toolTip={toolTip}/>
        </span>
    )
}

export function FormGroup({label, required, inline, toolTip, helperText, hasError, disabled, children}) {
    const intent = hasErrorIntent({hasError, disabled});
    const labelInfo = requiredLabelInfo({required});
    return (
        <BPCore.FormGroup
            className="FormGroup"
            label={<FormGroupLabel label={label} labelInfo={labelInfo} toolTip={toolTip}/>}
            inline={inline}
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