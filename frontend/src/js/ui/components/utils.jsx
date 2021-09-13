import {Intent} from "@blueprintjs/core";

export function hasErrorIntent({hasError, disabled}) {
    return (hasError && !disabled) ? Intent.DANGER : Intent.NONE;
}

