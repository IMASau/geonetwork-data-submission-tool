import React from 'react';
import PropTypes from 'prop-types';
import * as BPCore from '@blueprintjs/core';

export function RecordAddField({disabled, columns, onAddClick, buttonText}) {
    const emptyValue = columns.map(_ => "");
    const [stateValue, setStateValue] = React.useState(emptyValue);

    let hasValue = stateValue.every(v => v !== "");
    let buttonDisabled = disabled || !hasValue;

    function setColumnValue(idx, val) {
        const newValue = [...stateValue];
        newValue[idx]=val;
        setStateValue(newValue)
    }

    function submitValue(){
        if (hasValue) {
            onAddClick(stateValue);
            setStateValue(emptyValue);
        }
    }

    function handleKeyPress (idx, event) {
      if(event.key === 'Enter'){
        submitValue()
      }
    }

    const inputGroups = columns.map(({flex, placeholder, maxLength}, idx) => {
        const className="RecordAddFieldInputGroup" + idx
        return (
            <div style={{ flex: flex}}>
                <BPCore.InputGroup
                    key={idx}
                    disabled={disabled}
                    value={stateValue[idx]}
                    placeholder={placeholder}
                    maxLength={maxLength}
                    onKeyPress={(e) => handleKeyPress(idx, e)}
                    onChange={(e) => setColumnValue(idx, e.target.value)} />
            </div>
        )
    })

    if (disabled) {
        return null;
    } else {
        return (
            <BPCore.ControlGroup fill={true}>
                {inputGroups}
                <BPCore.Button className={BPCore.Classes.FIXED} onClick={submitValue} disabled={buttonDisabled} intent={BPCore.Intent.PRIMARY}>{buttonText}</BPCore.Button>
            </BPCore.ControlGroup>
        )
    };
}

RecordAddField.propTypes = {
    columns: PropTypes.arrayOf(PropTypes.shape({
        flex: PropTypes.number.isRequired,
        placeholder: PropTypes.string.isRequired,
        maxLength: PropTypes.number,
    })),
    buttonText: PropTypes.string.isRequired,
    disabled: PropTypes.bool,
    onAddClick: PropTypes.func,
}
