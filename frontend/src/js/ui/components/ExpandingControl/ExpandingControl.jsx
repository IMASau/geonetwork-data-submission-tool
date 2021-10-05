import React from 'react';
import * as BPCore from '@blueprintjs/core';
import PropTypes from "prop-types";
import {requiredLabelInfo} from "../utils";


export function ExpandingControl({label, required, hasError, disabled, children}) {
    const [isOpen, setOpen] = React.useState(false);
    return (
        <div className="ExpandingControl">
            <div className="ExpandingControlHeader" onClick={() => setOpen(!isOpen)}>
                <div className="ExpandingControlHeaderLabel">
                    {label}
                    {requiredLabelInfo({required})}
                </div>
                <BPCore.Icon icon={isOpen ? "caret-up" : "caret-down"}/>
            </div>
            <BPCore.Collapse isOpen={isOpen}>
                {children}
            </BPCore.Collapse>
        </div>
    );
}

ExpandingControl.propTypes = {
    label: PropTypes.string.isRequired,
    value: PropTypes.bool,
    disabled: PropTypes.bool,
    required: PropTypes.bool,
    hasError: PropTypes.bool,
    children: PropTypes.oneOfType([
        PropTypes.arrayOf(PropTypes.node),
        PropTypes.node
    ]).isRequired
}