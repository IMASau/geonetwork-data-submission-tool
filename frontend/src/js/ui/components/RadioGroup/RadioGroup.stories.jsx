import React from 'react';

import {RadioGroup} from './RadioGroup';
import './RadioGroup.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';

export default {
    title: 'Example/RadioGroup',
    component: RadioGroup,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};


const FieldTemplate = (args) => <RadioGroup {...args} />;

const options = [
    {value: 'chocolate', label: 'Chocolate'},
    {value: 'strawberry', label: 'Strawberry'},
    {value: 'vanilla', label: 'Vanilla'}
]

export const SimpleRadio = FieldTemplate.bind({});
SimpleRadio.args = {
    value: null,
    options: options,
    disabled: false,
    hasError: false,
    getValue: (option => option.value),
    getLabel: (option => option.label)
};

export const RadioDisabledState = FieldTemplate.bind({});
RadioDisabledState.args = {
    value: options[0],
    options: options,
    disabled: true,
    getValue: (option => option.value),
    getLabel: (option => option.label)
};

export const RadioWithError = FieldTemplate.bind({});
RadioWithError.args = {
    value: options[0],
    options: options,
    hasError: true,
    getValue: (option => option.value),
    getLabel: (option => option.label)
};

export const RadioWithInvalidValue = FieldTemplate.bind({});
RadioWithInvalidValue.args = {
    value: 'marzipan',
    options: options,
    hasError: true,
    getValue: (option => option.value),
    getLabel: (option => option.label)
};

import NOTES from './NOTES.mdx';
export const DesignDecisions = NOTES;