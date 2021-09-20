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
