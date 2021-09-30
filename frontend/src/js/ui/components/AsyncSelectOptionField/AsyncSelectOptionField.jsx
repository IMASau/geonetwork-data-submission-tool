import React from 'react';
import PropTypes from 'prop-types';
import AsyncSelect from "react-select/async/dist/react-select.esm";
import {ReactSelectCustomStyles} from "../utils";


export function AsyncSelectOptionField({value, hasError, disabled, placeholder, onChange, loadOptions}) {
    const defaultOptions = !disabled
    return (
        <AsyncSelect
            styles={ReactSelectCustomStyles}
            value={value}
            placeholder={placeholder}
            onChange={(value) => onChange(value)}
            isClearable={true}
            isDisabled={disabled}
            loadOptions={loadOptions}
            defaultOptions={defaultOptions}
        >
        </AsyncSelect>
    );
}

AsyncSelectOptionField.propTypes = {
    value: PropTypes.object,
    loadOptions: PropTypes.func.isRequired,
    placeholder: PropTypes.string,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    onChange: PropTypes.func.isRequired,
}
