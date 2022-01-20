import React from 'react';
import PropTypes from 'prop-types';
import * as BPCore from '@blueprintjs/core';
import {Button, Classes, Icon} from '@blueprintjs/core';
import {Tooltip2} from "@blueprintjs/popover2";
import {hasErrorIntent, requiredLabelInfo} from '../utils';

function TooltipPopoverContent({toolTip}) {
    return <div className="FormGroupTooltipPopoverContent">{toolTip}</div>
}

function TooltipButton({toolTip}) {
    if (toolTip) {
        return (
            <Tooltip2 className="FormGroupTooltipButton"
                      content={TooltipPopoverContent({toolTip})}>
                <Button small={true} minimal={true} icon={<Icon icon="help" size={10}/>}/>
            </Tooltip2>
        )
    } else {
        return <span/>
    }
}

function NewTerm () {
    return (<span><Icon icon="star"/> New term</span>)
}

function LabelInfo({required, toolTip, isAdded}) {
    const labelInfo = requiredLabelInfo({required});
    return (
        <span className="FormGroupLabelInfo">
            <span className={Classes.TEXT_MUTED}> {labelInfo}</span>
            <TooltipButton toolTip={toolTip}/>
            {isAdded ? <span style={{float: "right"}}><NewTerm /></span>:<span/>}
        </span>
    )
}

export function FormGroup({label, required, toolTip, helperText, hasError, isAdded, disabled, children}) {
    const intent = hasErrorIntent({hasError, disabled});
    return (
        <BPCore.FormGroup
            className="FormGroup"
            label={label}
            helperText={helperText}
            intent={intent}
            disabled={disabled}
            labelInfo={<LabelInfo required={required} toolTip={toolTip} isAdded={isAdded}/>}
        >
            {children}
        </BPCore.FormGroup>
    );
}

FormGroup.propTypes = {
    label: PropTypes.string,
    required: PropTypes.bool,
    toolTip: PropTypes.node,
    helperText: PropTypes.string,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    isAdded: PropTypes.bool,
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
            <div className="bp3-form-helper-text">{helperText}</div>
        </BPCore.FormGroup>
    );
}

InlineFormGroup.propTypes = {
    label: PropTypes.string,
    required: PropTypes.bool,
    toolTip: PropTypes.node,
    helperText: PropTypes.string,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
}
