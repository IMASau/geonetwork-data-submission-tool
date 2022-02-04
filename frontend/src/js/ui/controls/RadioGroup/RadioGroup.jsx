import React from 'react';
import PropTypes from "prop-types";
import * as BPCore from '@blueprintjs/core';


export function RadioGroup({ value, options, inline, disabled, hasError, getLabel, getValue, onChange }) {
    const radioButtons = options.map((option, index) => <BPCore.Radio label={getLabel(option)} value={index} key={index}/>);
    const selectedIndex = value ? options.map(option => getValue(option)).indexOf(getValue(value)) : null;
    const hasErrorClass = !disabled && hasError ? 'hasError' : '';
    const isDisabledClass = disabled ? 'bp3-text-disabled' : '';
    const classes = `radio-group ${hasErrorClass} ${isDisabledClass}`;
    return (
        <div className={classes}>
            <BPCore.RadioGroup
                disabled={disabled}
                selectedValue={selectedIndex}
                onChange={(e) => onChange && onChange(options[e.target.value])}
                inline={inline}>
                {radioButtons}
            </BPCore.RadioGroup>
        </div>
    );
}

RadioGroup.propTypes = {
    inline: PropTypes.bool,
    value: PropTypes.any,
    options: PropTypes.arrayOf(PropTypes.object).isRequired,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    getValue: PropTypes.func.isRequired,
    getLabel: PropTypes.func.isRequired,
    onChange: PropTypes.func.isRequired,
}


export function RadioGroupSimple(args) {
    const {value, options, onChange, getValue} = args
    const valueOption = options && options.find(option => getValue(option) === value)
    const onValueChange = (option) => onChange(getValue(option))
    return (
            <RadioGroup
                {...args}
                value={valueOption}
                onChange={onValueChange}
                />
    );
}


RadioGroupSimple.propTypes = {
    inline: PropTypes.bool,
    value: PropTypes.any,
    options: PropTypes.arrayOf(PropTypes.object).isRequired,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    getValue: PropTypes.func.isRequired,
    getLabel: PropTypes.func.isRequired,
    onChange: PropTypes.func.isRequired,
}


