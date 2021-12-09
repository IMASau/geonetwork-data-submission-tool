import React from 'react';
import PropTypes from 'prop-types';
import { Button, Card, InputGroup, Intent, NonIdealState, Popover } from '@blueprintjs/core';
import * as BPDateTime from '@blueprintjs/datetime';
import moment from 'moment';
import { hasErrorIntent, useCachedState } from "../utils";
import DayPicker from 'react-day-picker';
import 'react-day-picker/lib/style.css';


function getMomentFormatter({ format, disabled }) {
    const formatDate = function (date) {
        try {
            return moment(date).format(format)
        } catch (error) {
            return 'Invalid value';
        }
    };
    const parseDate = function (str) {
        try {
            return moment(str, format).toDate() 
        } catch (error) {
            return null;
        }
    };
    const placeholder = format;
    if (disabled) {
        return { formatDate, parseDate }
    } else {
        return { formatDate, parseDate, placeholder }
    }
}

export const DateField2 = ({ value, disabled, onChange, hasError, minDate, maxDate }) => {
    const {formatDate, parseDate, placeholder} = getMomentFormatter({format: 'DD/MM/YYYY', disabled})
    const [stateValue, setStateValue] = useCachedState(value ? formatDate(value): '');

    const calendarButton = (
        <Popover
            disabled={disabled}
            autoFocus={false}
            minimal={true}
            openOnTargetFocus={false}
        >
            <Button
                disabled={disabled}
                icon={"calendar"}
                minimal={true}
                tabIndex={-1}
            />
            <div>
                <DayPicker
                    selectedDays={new Date(value)}
                    month={value}
                    onDayClick={(day, modifiers, e) => onChange(day)}
                />
            </div>
        </Popover>
    )
    return (
        <div>
            <InputGroup
                value={stateValue}
                placeholder={placeholder}
                fill={false}
                autoComplete="off"
                intent={!disabled && hasError ? Intent.DANGER : Intent.NONE}
                leftElement={calendarButton}
                type="text"
                disabled={disabled}
                readOnly={false}
                onChange={e => setStateValue(e.target.value)}
                onBlur={e => onChange(parseDate(e.target.value))}
            />
        </div>
    )
}

DateField2.propTypes = {
    value: PropTypes.instanceOf(Date),
    disabled: PropTypes.bool,
    onChange: PropTypes.func,
    hasError: PropTypes.bool,
}