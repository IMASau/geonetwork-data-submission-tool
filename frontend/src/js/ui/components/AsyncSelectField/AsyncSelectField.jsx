import React from 'react';
import PropTypes from 'prop-types';
import AsyncSelect from 'react-select/async';

const customStyles = {
    clearIndicator: (provided, state) => {
        console.log("clearIndicator", {provided, state});
        return {
            ...provided,
            padding: 4
        }
    },
    container: (provided, state) => {
        console.log("container", {provided, state});
        return {
            ...provided,
        }
    },
    control: (provided, state) => {
        console.log("control", {provided, state});
        return {
            ...provided,
            minHeight: 30,
            "&:hover": null,
            border: "none",
            borderRadius: "3px",
            backgroundColor: state.isDisabled ? "rgba(206, 217, 224, 0.5)" : provided.backgroundColor,
            boxShadow:
                state.isFocused ? "0 0 0 1px #137cbd, 0 0 0 3px rgb(19 124 189 / 30%), inset 0 1px 1px rgb(16 22 26 / 20%)" :
                    state.isDisabled ? "none" :
                        "0 0 0 0 rgb(19 124 189 / 0%), 0 0 0 0 rgb(19 124 189 / 0%), inset 0 0 0 1px rgb(16 22 26 / 15%), inset 0 1px 1px rgb(16 22 26 / 20%)",
        }
    },
    dropdownIndicator: (provided, state) => {
        console.log("dropdownIndicator", {provided, state});
        return {
            ...provided,
            padding: 4
        }
    },
    group: (provided, state) => {
        console.log("group", {provided, state});
        return {
            ...provided,
        }
    },
    groupHeading: (provided, state) => {
        console.log("groupHeading", {provided, state});
        return {
            ...provided,
        }
    },
    indicatorsContainer: (provided, state) => {
        console.log("indicatorsContainer", {provided, state});
        return {
            ...provided,
        }
    },
    indicatorSeparator: (provided, state) => {
        console.log("indicatorSeparator", {provided, state});
        return {
            ...provided,
        }
    },
    input: (provided, state) => {
        console.log("input", {provided, state});
        return {
            ...provided,
        }
    },
    loadingIndicator: (provided, state) => {
        console.log("loadingIndicator", {provided, state});
        return {
            ...provided,
        }
    },
    loadingMessage: (provided, state) => {
        console.log("loadingMessage", {provided, state});
        return {
            ...provided,
        }
    },
    menu: (provided, state) => {
        console.log("menu", {provided, state});
        return {
            ...provided,
        }
    },
    menuList: (provided, state) => {
        console.log("menuList", {provided, state});
        return {
            ...provided,
        }
    },
    menuPortal: (provided, state) => {
        console.log("menuPortal", {provided, state});
        return {
            ...provided,
        }
    },
    multiValue: (provided, state) => {
        console.log("multiValue", {provided, state});
        return {
            ...provided,
        }
    },
    multiValueLabel: (provided, state) => {
        console.log("multiValueLabel", {provided, state});
        return {
            ...provided,
        }
    },
    multiValueRemove: (provided, state) => {
        console.log("multiValueRemove", {provided, state});
        return {
            ...provided,
        }
    },
    noOptionsMessage: (provided, state) => {
        console.log("noOptionsMessage", {provided, state});
        return {
            ...provided,
        }
    },
    option: (provided, state) => {
        console.log("option", {provided, state});
        return {
            ...provided,
        }
    },
    placeholder: (provided, state) => {
        console.log("placeholder", {provided, state});
        return {
            ...provided,
        }
    },
    singleValue: (provided, state) => {
        console.log("singleValue", {provided, state});
        return {
            ...provided,
            color: state.isDisabled ? "rgba(92, 112, 128, 0.6)" : "#182026",
        }
    },
    valueContainer: (provided, state) => {
        console.log("valueContainer", {provided, state});
        return {
            ...provided,
            padding: "0 6px"
        }
    },
}

export function AsyncSelectField({value, hasError, disabled, placeholder, onChange, loadOptions}) {
    const defaultOptions = !disabled
    return (
        <AsyncSelect
            className="AsyncSelectField"
            classNamePrefix="AsyncSelectFieldPrefix"
            styles={customStyles}
            value={value}
            placeholder={placeholder}
            onChange={(value) => onChange(value)}
            isClearable={true}
            isDisabled={disabled}
            loadOptions={defaultOptions}
            defaultOptions={true}
        >
        </AsyncSelect>
    );
}

AsyncSelectField.propTypes = {
    value: PropTypes.object,
    loadOptions: PropTypes.func.isRequired,
    placeholder: PropTypes.string,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    onChange: PropTypes.func.isRequired,
}
