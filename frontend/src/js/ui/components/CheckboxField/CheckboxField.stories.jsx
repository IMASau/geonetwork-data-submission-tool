import React from 'react';

import {CheckboxField} from './CheckboxField';
import './CheckboxField.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import {FormGroup} from "../FormGroup/FormGroup";

export default {
    title: 'Example/CheckboxField',
    component: CheckboxField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};

const FieldTemplate = (args) => <CheckboxField {...args} />;

export const SimpleField = FieldTemplate.bind({});
SimpleField.args = {
    checked: false,
    disabled: false,
    hasError: false,
};

export const SimpleFieldWithLocalState = (args) => {
    const [value, setValue] = React.useState(args.checked);
    return <CheckboxField {...args} checked={value} onChange={(v, t) => setValue(v)}/>;
};
SimpleFieldWithLocalState.args = {
    label: "Agree?",
};

export const FieldDisabledState = FieldTemplate.bind({});
FieldDisabledState.args = {
    label: "Agree?",
    disabled: true
};

export const FieldWithError = FieldTemplate.bind({});
FieldWithError.args = {
    label: "Agree?",
    hasError: true
};

export const FieldWithValueAndError = FieldTemplate.bind({});
FieldWithValueAndError.args = {
    checked: true,
    disabled: false,
    hasError: true,
};

import NOTES from './NOTES.mdx';
export const DesignDecisions = NOTES;
