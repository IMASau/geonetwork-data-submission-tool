import React from 'react';

import {SelectValueField} from './SelectValueField';
import './SelectValueField.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import NOTES from './NOTES.mdx';

export default {
    title: 'Example/SelectValueField',
    component: SelectValueField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};

const FieldTemplate = (args) => <SelectValueField {...args} />;

const options = [
    {value: 'chocolate', label: 'Chocolate'},
    {value: 'strawberry', label: 'Strawberry'},
    {value: 'vanilla', label: 'Vanilla'}
]

export const SimpleField = FieldTemplate.bind({});
SimpleField.args = {
    value: null,
    options: options,
    placeholder: "",
    disabled: false,
    hasError: false,
};

export const FieldDiabledState = FieldTemplate.bind({});
FieldDiabledState.args = {
    value: options[0].value,
    options: options,
    disabled: true,
};

export const FieldWithError = FieldTemplate.bind({});
FieldWithError.args = {
    value: options[0].value,
    options: options,
    hasError: true,
};

export const FieldWithInvalidValue = FieldTemplate.bind({});
FieldWithInvalidValue.args = {
    value: 'marzipan',
    options: options,
    hasError: true,
};


export const EmptyFieldWithPlaceholder = FieldTemplate.bind({});
EmptyFieldWithPlaceholder.args = {
    placeholder: "This is the placeholder",
};

export const DesignDecisions = NOTES;
