import React from 'react';
import * as BPCore from '@blueprintjs/core';
import PropTypes from "prop-types";


export function ExpandingControl({label, hasError, disabled, children}) {
    console.log({label, hasError, disabled, children})
    const [isOpen, setOpen] = React.useState(false);
    return (
        <div className="ExpandingControl">
            <div className="ExpandingControlHeader" onClick={() => setOpen(!isOpen)}>
                <label>{label}</label>
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
    hasError: PropTypes.bool,
    children: PropTypes.element
}
