import React from 'react';
import * as BPCore from '@blueprintjs/core';
import PropTypes from "prop-types";
import {requiredLabelInfo} from "../utils";


export function ExpandingControl({label, required, hasError, disabled, children}) {
    const [isOpen, setOpen] = React.useState(false);
    return (
        <div className="ExpandingControl">
            <div className="ExpandingControlHeader" onClick={() => setOpen(!isOpen)}>
                <div>{label}{requiredLabelInfo({required})}</div>
                <BPCore.Button icon={isOpen ? "caret-up" : "caret-down"}
                               minimal={true}/>
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
    children: PropTypes.arrayOf(PropTypes.element)
}
