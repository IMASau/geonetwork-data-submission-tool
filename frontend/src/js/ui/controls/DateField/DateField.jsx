import React from 'react';
import PropTypes from 'prop-types';
import {Button, Card, InputGroup, Intent, NonIdealState, Popover} from '@blueprintjs/core';
import * as BPDateTime from '@blueprintjs/datetime';
import moment from 'moment';
import {hasErrorIntent} from "../utils";

function getMomentFormatter({format, disabled}) {
    const formatDate = (date) => moment(date).format(format);
    const parseDate = (str) => moment(str, format).toDate();
    const placeholder = format;
    if (disabled) {
        return { formatDate, parseDate }
    } else {
        return { formatDate, parseDate, placeholder }
    }
}

export const OutOfRangeValue = ({formatDate, value, disabled, hasError, rightElement, onClearClick}) => {
    return (
        <Popover disabled={disabled}>
            <InputGroup className="OutOfRangeValue"
                        value={formatDate(value)}
                        fill={false}
                        autoComplete="off"
                        intent={!disabled && hasError ? Intent.DANGER : Intent.NONE}
                        leftIcon="calendar"
                        type="text"
                        disabled={disabled}
                        readOnly={true}
            />
            <Card>
                <NonIdealState
                    icon="error"
                    title="Out of range"
                    description="Date is outside of the legal range."
                    action={<Button onClick={onClearClick} text="Clear"/>}
                />
            </Card>
        </Popover>
    )
}

export const DateField = ({value, disabled, onChange, hasError, minDate, maxDate}) => {
    console.assert(maxDate > minDate);
    const intent = hasErrorIntent({hasError, disabled});
    const outOfRange = value && (value < minDate || value > maxDate);
    const onClearClick = () => onChange(null);
    const format = "DD-MM-YYYY";
    if (outOfRange) {
        return <OutOfRangeValue {...getMomentFormatter({format, disabled})}
                                value={value}
                                disabled={disabled}
                                hasError={hasError}
                                onClearClick={onClearClick}/>
    } else {
        return (
            <BPDateTime.DateInput
                {...getMomentFormatter({format, disabled})}
                disabled={disabled}
                value={value}
                onChange={(selectedDate, isUserChange) => {
                    onChange(selectedDate);
                }}
                inputProps={{"leftIcon": "calendar", "intent": intent}}
                popoverProps={{
                    shouldReturnFocusOnClose: false
                }}
                minDate={minDate}
                maxDate={maxDate}/>
        );
    }
};

DateField.propTypes = {
    value: PropTypes.instanceOf(Date),
    disabled: PropTypes.bool,
    onChange: PropTypes.func,
    hasError: PropTypes.bool,
    minDate: PropTypes.instanceOf(Date).isRequired,
    maxDate: PropTypes.instanceOf(Date).isRequired
}