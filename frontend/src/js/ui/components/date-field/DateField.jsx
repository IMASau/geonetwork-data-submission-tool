import React from 'react';
import PropTypes from 'prop-types';
import { FormGroup, Intent } from '@blueprintjs/core';
import { DateInput, IDateFormatProps } from '@blueprintjs/datetime';
import moment from 'moment';

function getMomentFormatter(format: string): IDateFormatProps {
    // note that locale argument comes from locale prop and may be undefined
    return {
        formatDate: (date) => moment(date).format(format),
        parseDate: (str) => moment(str, format).toDate(),
        placeholder: format,
    }
};

export const DateField = ({ label, required, helperText, value, disabled, onChange, hasError, minDate, maxDate }) => {
    const intent = (hasError && !disabled) ? Intent.DANGER : Intent.NONE;
    console.log({ label, required, helperText, value, disabled, onChange, hasError, minDate, maxDate })
    return (
        <FormGroup
            label={label}
            labelInfo={required ? "*": null}
            helperText={helperText}
            intent={intent}
            disabled={disabled}
        >
            <DateInput {...getMomentFormatter("DD-MM-YYYY")}
                disabled={disabled}
                value={value}
                onChange={onChange}
                inputProps={{ "leftIcon": "calendar", "intent": intent}}
                minDate={minDate}
                maxDate={maxDate}/>
        </FormGroup>
    );
};
