import React from 'react';
import PropTypes from 'prop-types';
import { FormGroup, Intent } from '@blueprintjs/core';
import { DateInput, IDateFormatProps } from '@blueprintjs/datetime';
import moment from 'moment';

function getMomentFormatter(format: string): IDateFormatProps {
    return {
        formatDate: (date) => moment(date).format(format),
        parseDate: (str) => moment(str, format).toDate(),
        placeholder: format,
    }
};

export const DateField = ({ label, required, helperText, value, disabled, onChange, hasError, minDate, maxDate }) => {
    console.assert(maxDate>minDate);
    const intent = (hasError && !disabled) ? Intent.DANGER : Intent.NONE;
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

DateField.propTypes = {
  label: PropTypes.string,
  required: PropTypes.bool,
  helperText: PropTypes.string,
  value: PropTypes.instanceOf(Date),
  disabled: PropTypes.bool,
  onChange: PropTypes.func,
  hasError: PropTypes.bool,
  minDate: PropTypes.instanceOf(Date).isRequired,
  maxDate: PropTypes.instanceOf(Date).isRequired
}