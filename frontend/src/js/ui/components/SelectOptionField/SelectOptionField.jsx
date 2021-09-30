import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';
import {getReactSelectCustomStyles} from "../utils";

export function SelectOptionField({value, options, hasError, disabled, placeholder, onChange}) {
    return (
        <Select
            styles={getReactSelectCustomStyles({hasError})}
            value={value}
            options={options}
            placeholder={placeholder}
            onChange={(value) => onChange(value)}
            isClearable={true}
            isDisabled={disabled}
            isLoading={false}
            isSearchable={false}
        />
    );
}

SelectOptionField.propTypes = {
    value: PropTypes.shape({
        value: PropTypes.string,
        label: PropTypes.string
    }),
    options: PropTypes.arrayOf(PropTypes.shape({
        value: PropTypes.string,
        label: PropTypes.string
    })),
    placeholder: PropTypes.string,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    onChange: PropTypes.func,
}
