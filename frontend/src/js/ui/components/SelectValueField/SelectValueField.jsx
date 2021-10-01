import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';
import {getReactSelectCustomStyles} from "../utils";


export function SelectValueField({value, options, hasError, disabled, placeholder, onChange}) {
    const valueOption = options && options.find(option => option.value === value)
    return (
        <Select
            styles={getReactSelectCustomStyles({hasError})}
            value={valueOption}
            options={options}
            placeholder={placeholder}
            onChange={(option) => onChange(option ? option.value : null)}
            isClearable={true}
            isDisabled={disabled}
            isLoading={false}
            isSearchable={true}
        >
        </Select>
    );
}

SelectValueField.propTypes = {
    value: PropTypes.string,
    options: PropTypes.arrayOf(PropTypes.shape({
        value: PropTypes.string,
        label: PropTypes.string
    })),
    placeholder: PropTypes.string,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    onChange: PropTypes.func,
}
