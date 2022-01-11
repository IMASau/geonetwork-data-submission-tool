import React from 'react';

import {InputField} from './InputField';
import './InputField.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import {FormGroup} from "../FormGroup/FormGroup";

export default {
    title: 'Example/InputField',
    component: InputField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};

const FieldTemplate = (args) => <InputField {...args} />;

export const SimpleField = FieldTemplate.bind({});
SimpleField.args = {
    value: "Hello",
    placeholder: "",
    disabled: false,
    hasError: false,
};

export const FieldDiabledState = FieldTemplate.bind({});
FieldDiabledState.args = {
    value: "Hello",
    placeholder: "",
    disabled: true,
    hasError: false,
};

export const FieldWithError = FieldTemplate.bind({});
FieldWithError.args = {
    value: "Hello",
    placeholder: "",
    disabled: false,
    hasError: true,
};


export const EmptyFieldWithPlaceholder = FieldTemplate.bind({});
EmptyFieldWithPlaceholder.args = {
    value: "",
    placeholder: "This is the placeholder",
    disabled: false,
    hasError: false,
};

import NOTES from './NOTES.mdx';
export const DesignDecisions = NOTES;
