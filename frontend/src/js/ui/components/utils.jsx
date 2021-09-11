import * as BPCore from "@blueprintjs/core";

export function hasErrorIntent({hasError, disabled}) {
    return (hasError && !disabled) ? BPCore.Intent.DANGER : BPCore.Intent.NONE;
}

export function requiredLabelInfo({required}) {
    return required ? "*" : null;
}
