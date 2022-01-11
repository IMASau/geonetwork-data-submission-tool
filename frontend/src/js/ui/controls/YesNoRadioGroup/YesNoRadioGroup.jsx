import React from 'react';
import * as BPCore from '@blueprintjs/core';
import PropTypes from "prop-types";

// intent={intent}
// disabled={disabled}
// value={value}
// placeholder={placeholder}
// onChange={(e) => onChange(e.target.value)}

const valueLookup = {
    null: null,
    true: "yes",
    false: "no",
}

export function YesNoRadioGroup({label, value, hasError, disabled, onChange}) {
    const selectedValue = valueLookup[value];
    const hasErrorClass = !disabled && hasError ? 'hasError' : ''
    const isDisabledClass = disabled ? 'bp3-text-disabled' : ''
    const classes = `YesNoRadioGroup ${hasErrorClass} ${isDisabledClass}`
    return (
        <div className={classes}>
            <div className="YesNoRadioGroup-label">{label}</div>
            <BPCore.RadioGroup
                disabled={disabled}
                onChange={(e) => onChange && onChange(e.target.value === "yes")}
                selectedValue={selectedValue}
                inline={true}>
                <BPCore.Radio label="Yes" value={"yes"}/>
                <BPCore.Radio label="No" value={"no"}/>
            </BPCore.RadioGroup>
        </div>
    );
}

YesNoRadioGroup.propTypes = {
    label: PropTypes.string.isRequired,
    value: PropTypes.bool,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    onChange: PropTypes.func,
}
