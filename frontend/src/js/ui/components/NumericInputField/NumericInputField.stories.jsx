import React from 'react';

import {NumericInputField} from './NumericInputField';
import './NumericInputField.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import {FormGroup} from "../FormGroup/FormGroup";

export default {
    title: 'Example/NumericInputField',
    component: NumericInputField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};

const FieldTemplate = (args) => <NumericInputField {...args} />;

export const SimpleField = FieldTemplate.bind({});
SimpleField.args = {
    value: 15.123,
    placeholder: "",
    disabled: false,
    hasError: false,
    hasButtons: true,
};

export const SimpleFieldNoButtons = FieldTemplate.bind({});
SimpleFieldNoButtons.args = {
    value: 15.123,
    placeholder: "",
    disabled: false,
    hasError: false,
    hasButtons: false,
};

export const SimpleFieldWithLocalState = (args) => {
    const [value, setValue] = React.useState(args.value);
    return <NumericInputField {...args} value={value} onValueChange={(v, t) => setValue(v)}/>;
};
SimpleFieldWithLocalState.args = {
    placeholder: "This is the placeholder",
    disabled: false,
    hasError: false,
    hasButtons: true,
};

export const FieldDisabledState = FieldTemplate.bind({});
FieldDisabledState.args = {
    value: 15.123,
    placeholder: "",
    disabled: true,
    hasError: false,
    hasButtons: true,
};

export const FieldWithError = FieldTemplate.bind({});
FieldWithError.args = {
    value: 15.123,
    placeholder: "",
    disabled: false,
    hasError: true,
    hasButtons: true,
};


export const EmptyFieldWithPlaceholder = FieldTemplate.bind({});
EmptyFieldWithPlaceholder.args = {
    value: null,
    placeholder: "This is the placeholder",
    disabled: false,
    hasError: false,
    hasButtons: true,
};


import NOTES from './NOTES.mdx';
export const DesignDecisions = NOTES;
