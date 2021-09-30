import React from 'react';
import * as BPCore from "@blueprintjs/core";
import {Button, Icon} from "@blueprintjs/core";
import {Tooltip2} from "@blueprintjs/popover2";

export function useCachedState(value) {
    const [stateValue, setStateValue] = React.useState(value);
    React.useEffect(() => {
        setStateValue(value)
    }, [value])
    return [stateValue, setStateValue]
}

export function hasErrorIntent({hasError, disabled}) {
    return (hasError && !disabled) ? BPCore.Intent.DANGER : BPCore.Intent.NONE;
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

export function requiredLabelInfo({required}) {
    return required ? "*" : null
}

export function getReactSelectCustomStyles({hasError}) {
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