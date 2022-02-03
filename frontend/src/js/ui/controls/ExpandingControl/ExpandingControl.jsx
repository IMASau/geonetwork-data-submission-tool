import React from 'react';
import * as BPCore from '@blueprintjs/core';
import PropTypes from "prop-types";
import {requiredLabelInfo} from "../utils";


export function ExpandingControl({label, required, defaultOpen, children, keepChildrenMounted}) {
    const [isOpen, setOpen] = React.useState(defaultOpen ? true: false);
    return (
        <div className="ExpandingControl">
            <div className="ExpandingControlHeader" onClick={() => setOpen(!isOpen)}>
                <div className="ExpandingControlHeaderLabel">
                    {label}
                    {requiredLabelInfo({required})}
                </div>
                <BPCore.Icon icon={isOpen ? "caret-up" : "caret-down"}/>
            </div>
            <BPCore.Collapse isOpen={isOpen} keepChildrenMounted={keepChildrenMounted}>
              {children}
            </BPCore.Collapse>
        </div>
    );
}

ExpandingControl.propTypes = {
    label: PropTypes.string.isRequired,
    required: PropTypes.bool,
    defaultOpen: PropTypes.bool,
    keepChildrenMounted: PropTypes.bool,
    children: PropTypes.node
}