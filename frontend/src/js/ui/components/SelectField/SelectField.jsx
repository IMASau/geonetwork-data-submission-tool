import React from 'react';
import PropTypes from 'prop-types';
import AsyncSelect from "react-select/async/dist/react-select.esm";
import Select, {components} from "react-select";

function getReactSelectCustomStyles({hasError}) {
    return {
        clearIndicator: (provided, state) => {
            return {
                ...provided,
                padding: 4
            }
        },
        container: (provided, state) => {
            return {
                ...provided,
            }
        },
        control: (provided, state) => {
            return {
                ...provided,
                minHeight: 30,
                "&:hover": null,
                border: "none",
                borderRadius: "3px",
                backgroundColor: state.isDisabled ? "rgba(206, 217, 224, 0.5)" :
                    provided.backgroundColor,
                boxShadow:
                    state.isFocused && hasError ? "0 0 0 1px #db3737, 0 0 0 3px rgb(219 55 55 / 30%), inset 0 1px 1px rgb(16 22 26 / 20%)" :
                        hasError ? "0 0 0 0 rgb(219 55 55 / 0%), 0 0 0 0 rgb(219 55 55 / 0%), inset 0 0 0 1px #db3737, inset 0 0 0 1px rgb(16 22 26 / 15%), inset 0 1px 1px rgb(16 22 26 / 20%)" :
                            state.isFocused ? "0 0 0 1px #137cbd, 0 0 0 3px rgb(19 124 189 / 30%), inset 0 1px 1px rgb(16 22 26 / 20%)" :
                                state.isDisabled ? "none" :
                                    "0 0 0 0 rgb(19 124 189 / 0%), 0 0 0 0 rgb(19 124 189 / 0%), inset 0 0 0 1px rgb(16 22 26 / 15%), inset 0 1px 1px rgb(16 22 26 / 20%)",
            }
        },
        dropdownIndicator: (provided, state) => {
            return {
                ...provided,
                padding: 4
            }
        },
        group: (provided, state) => {
            return {
                ...provided,
            }
        },
        groupHeading: (provided, state) => {
            return {
                ...provided,
            }
        },
        indicatorsContainer: (provided, state) => {
            return {
                ...provided,
            }
        },
        indicatorSeparator: (provided, state) => {
            return {
                ...provided,
            }
        },
        input: (provided, state) => {
            return {
                ...provided,
            }
        },
        loadingIndicator: (provided, state) => {
            return {
                ...provided,
            }
        },
        loadingMessage: (provided, state) => {
            return {
                ...provided,
            }
        },
        menu: (provided, state) => {
            return {
                ...provided,
            }
        },
        menuList: (provided, state) => {
            return {
                ...provided,
            }
        },
        menuPortal: (provided, state) => {
            return {
                ...provided,
            }
        },
        multiValue: (provided, state) => {
            return {
                ...provided,
            }
        },
        multiValueLabel: (provided, state) => {
            return {
                ...provided,
            }
        },
        multiValueRemove: (provided, state) => {
            return {
                ...provided,
            }
        },
        noOptionsMessage: (provided, state) => {
            return {
                ...provided,
            }
        },
        option: (provided, state) => {
            return {
                ...provided,
            }
        },
        placeholder: (provided, state) => {
            return {
                ...provided,
            }
        },
        singleValue: (provided, state) => {
            return {
                ...provided,
                color: state.isDisabled ? "rgba(92, 112, 128, 0.6)" : "#182026",
            }
        },
        valueContainer: (provided, state) => {
            return {
                ...provided,
                padding: "0 6px"
            }
        },
    }
}

export function getReactSelectComponents({Option}) {
    return {
        Option: (args) =>
            <components.Option {...args}><Option data={args.data}/></components.Option>
    }
}

export function SelectValueField({value, options, onChange, ...args}) {
    const valueOption = options && options.find(option => option.value === value)
    const onValueChange = (option) => onChange(option ? option.value : null)
    return (
        <SelectOptionField
            value={valueOption}
            onChange={onValueChange}
            {...args}
        >
        </SelectOptionField>
    );
}

SelectValueField.propTypes = {
    value: PropTypes.string,
    options: PropTypes.arrayOf(PropTypes.object).isRequired,
    placeholder: PropTypes.string,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    onChange: PropTypes.func.isRequired
}

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
            isSearchable={true}
        />
    );
}

SelectOptionField.propTypes = {
    value: PropTypes.object,
    options: PropTypes.arrayOf(PropTypes.object).isRequired,
    placeholder: PropTypes.string,
    disabled: PropTypes.bool,
    hasError: PropTypes.bool,
    onChange: PropTypes.func.isRequired,
}


export function AsyncSelectOptionField({value, hasError, disabled, placeholder, onChange, loadOptions}) {
    const defaultOptions = !disabled
    return (
        <AsyncSelect
            styles={getReactSelectCustomStyles({hasError})}
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
