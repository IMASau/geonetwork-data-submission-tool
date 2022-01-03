import React from 'react';
import {Intent} from "@blueprintjs/core";
import { FocusStyleManager } from "@blueprintjs/core";

export function setupBlueprint() {
    FocusStyleManager.onlyShowFocusOnTabs();
}

export function useCachedState(value) {
    const [stateValue, setStateValue] = React.useState(value);
    React.useEffect(() => {
        setStateValue(value)
    }, [value])
    return [stateValue, setStateValue]
}

export function hasErrorIntent({hasError, disabled}) {
    return (hasError && !disabled) ? Intent.DANGER : Intent.NONE;
}

export function requiredLabelInfo({required}) {
    return required ? "*" : null
}
