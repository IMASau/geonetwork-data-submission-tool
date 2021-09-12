import * as BPCore from "@blueprintjs/core";
import {Button} from "@blueprintjs/core";
import {Tooltip2} from "@blueprintjs/popover2";

export function hasErrorIntent({hasError, disabled}) {
    return (hasError && !disabled) ? BPCore.Intent.DANGER : BPCore.Intent.NONE;
}

export function TooltipButton({toolTip}) {
    if (toolTip) {
        return (
            <Tooltip2 content={toolTip}>
                <Button icon="help" minimal={true} small={true}/>
            </Tooltip2>
        )
    } else {
        return <span/>
    }
}

export function requiredLabelInfo({required}) {
    return required ? "*" : null
}
