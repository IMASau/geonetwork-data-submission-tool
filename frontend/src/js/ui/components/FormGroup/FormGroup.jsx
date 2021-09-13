import React from 'react';
import PropTypes from 'prop-types';
import * as BPCore from '@blueprintjs/core';
import {Classes} from '@blueprintjs/core';
import {hasErrorIntent} from '../utils';

export function requiredLabelInfo({required}) {
    return required ? "*" : null
}

export function TooltipButton({toolTip}) {
    if (toolTip) {
        return (
            <Tooltip2 content={toolTip}>
                <Button small={true} minimal={true} icon={<Icon icon="help" size={10}/>}/>
            </Tooltip2>
        )
    } else {
        return <span/>
    }
}

function LabelInfo({required, toolTip}) {
    const labelInfo = requiredLabelInfo({required});
    return (
        <span className="FormGroupLabelInfo">
            <span className={Classes.TEXT_MUTED}>{labelInfo}</span>
            <TooltipButton toolTip={toolTip}/>
        </span>
    )
}

export function FormGroup({label, required, inline, toolTip, helperText, hasError, disabled, children}) {
    const intent = hasErrorIntent({hasError, disabled});
    return (
        <BPCore.FormGroup
            className="FormGroup"
            label={label}
            inline={inline}
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
    inline: PropTypes.bool,
    helperText: PropTypes.string,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
}