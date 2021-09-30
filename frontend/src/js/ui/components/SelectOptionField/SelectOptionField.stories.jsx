import React from 'react';

import {SelectOptionField} from './SelectOptionField';
import './SelectOptionField.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import {FormGroup} from "../FormGroup/FormGroup";

export default {
    title: 'Example/SelectOptionField',
    component: SelectOptionField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};

const FieldTemplate = (args) => <SelectOptionField {...args} />;

const options = [
    { value: 'chocolate', label: 'Chocolate' },
    { value: 'strawberry', label: 'Strawberry' },
    { value: 'vanilla', label: 'Vanilla' }
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
    value: options[0],
    options: options,
    disabled: true,
};

export const FieldWithError = FieldTemplate.bind({});
FieldWithError.args = {
    value: options[0],
    options: options,
    hasError: true,
};

export const FieldWithInvalidValue = FieldTemplate.bind({});
FieldWithInvalidValue.args = {
    value: { value: 'marzipan', label: 'Marzipan' },
    options: options,
    hasError: true,
};


export const EmptyFieldWithPlaceholder = FieldTemplate.bind({});
EmptyFieldWithPlaceholder.args = {
    placeholder: "This is the placeholder",
};

import NOTES from './NOTES.mdx';

export const DesignDecisions = NOTES;
