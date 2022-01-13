import React from 'react';
import PropTypes from 'prop-types';
import {Button, InputGroup, Intent, Popover} from '@blueprintjs/core';
import * as BPDateTime from '@blueprintjs/datetime';
import moment from 'moment';
import {useCachedState} from "../utils";
import DayPicker from 'react-day-picker';

function isEmpty(s) {
    return (!s || s.length === 0);
}

function isValidDate(d) {
    return d instanceof Date && !isNaN(d);
}

function getMomentFormatter({ format, valueFormat}) {
    const formatDate = function (date) {
        try {
            return moment(date).format(format)
        } catch (error) {
            return 'Invalid value';
        }
    };
    const formatValue = function (d) {
        try {
            return d ? moment(d).format(valueFormat) : null
        } catch (e) {
            return null
        }
    };
    const parseValue = function (s) {
        try {
            return moment(s, valueFormat).toDate()
        } catch (error) {
            return null
        }
    };
    const parseDate = function (s) {
        try {
            return moment(s, format).toDate()
        } catch (error) {
            return null;
        }
    };
    const placeholder = format;
    return { formatDate, parseDate, formatValue, parseValue, placeholder }
}

// TODO: how to handle junk-in values?
export const DateField2 = ({value, disabled, onChange, hasError}) => {
    const {formatDate, parseDate, parseValue, formatValue, placeholder} = getMomentFormatter({
        format: 'DD/MM/YYYY',
        valueFormat: 'YYYY-MM-DD',
    })
    const dateValue = isEmpty(value) ? null : parseValue(value);
    const [stateValue, setStateValue] = useCachedState(dateValue ? formatDate(dateValue) : '');

    const handleChangeValue = (s) => {
        if (isEmpty(s)) {
            onChange(null)
        } else {
            let d = parseDate(s)
            if (isValidDate(d)) {
                onChange(formatValue(d))
            } else {
                onChange(null)
            }
        }
    }

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
                    selectedDays={dateValue}
                    month={dateValue}
                    onDayClick={(day, modifiers, e) => onChange(formatValue(day))}
                />
            </div>
        </Popover>
    )
    return (
        <div>
            <InputGroup
                value={stateValue}
                placeholder={disabled ? null : placeholder}
                fill={false}
                autoComplete="off"
                intent={!disabled && hasError ? Intent.DANGER : Intent.NONE}
                leftElement={calendarButton}
                type="text"
                disabled={disabled}
                readOnly={false}
                onChange={e => setStateValue(e.target.value)}
                onBlur={e => handleChangeValue(e.target.value)}
            />
        </div>
    )
}

DateField2.propTypes = {
    value: PropTypes.string,
    disabled: PropTypes.bool,
    onChange: PropTypes.func,
    hasError: PropTypes.bool,
}